package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

object R2BlobManager {
    private const val TAG = "R2BlobManager"
    const val BLOB_THRESHOLD = 100_000 // 100.000 karakter sınırı
    const val R2BLOB_PREFIX = "r2blob://"

    private const val CACHE_PREFS_NAME = "r2_blob_hash_cache"

    private fun getCachePrefs(context: Context) =
        context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)

    fun sha256(text: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(text.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            text.hashCode().toString()
        }
    }

    private fun getCachedBlobUri(context: Context, noteId: Long, field: String, currentHash: String): String? {
        val savedHash = getCachePrefs(context).getString("hash_${noteId}_$field", null)
        val savedUri = getCachePrefs(context).getString("uri_${noteId}_$field", null)
        return if (savedHash == currentHash && !savedUri.isNullOrBlank()) savedUri else null
    }

    private fun cacheBlobUri(context: Context, noteId: Long, field: String, hash: String, uri: String) {
        getCachePrefs(context).edit()
            .putString("hash_${noteId}_$field", hash)
            .putString("uri_${noteId}_$field", uri)
            .apply()
    }

    /**
     * Firestore'a gönderilmek üzere hazırlanan not verisinde;
     * drawingData veya reportData 100.000 karakteri aşıyorsa ve R2 açıksa:
     * veriyi R2'ye "notes/blob-{UUID}.json" olarak yükler ve Firestore için
     * "r2blob://notes/blob-{UUID}.json" referans metnini döner.
     * İçerik değişmemişse (SHA-256 eşleşiyorsa) tekrar R2'ye yüklemez (sonsuz döngü engellenir).
     */
    suspend fun resolveNoteDataForFirestore(
        context: Context,
        note: Note
    ): Map<String, Any?> = withContext(Dispatchers.IO) {
        var finalDrawing = note.drawingData ?: ""
        var finalReport = note.reportData ?: ""

        val isR2Active = R2Settings.isR2Configured(context) && R2Settings.isLargeBlobUploadEnabled(context)

        if (isR2Active) {
            val client = R2Client(context)

            // 1. drawingData kontrolü
            if (finalDrawing.length > BLOB_THRESHOLD && !finalDrawing.startsWith(R2BLOB_PREFIX)) {
                val currentHash = sha256(finalDrawing)
                val cachedUri = getCachedBlobUri(context, note.id, "drawing", currentHash)

                if (cachedUri != null) {
                    finalDrawing = cachedUri
                } else {
                    val key = "notes/blob-${UUID.randomUUID()}.json"
                    val bytes = finalDrawing.toByteArray(Charsets.UTF_8)
                    val result = client.upload(key, bytes, contentType = "application/json")
                    if (result is R2Result.Success) {
                        val r2Uri = "$R2BLOB_PREFIX$key"
                        cacheBlobUri(context, note.id, "drawing", currentHash, r2Uri)
                        finalDrawing = r2Uri
                    } else {
                        Log.w(TAG, "Drawing R2 blob upload failed, using original or truncated text for cloud")
                    }
                }
            }

            // 2. reportData kontrolü
            if (finalReport.length > BLOB_THRESHOLD && !finalReport.startsWith(R2BLOB_PREFIX)) {
                val currentHash = sha256(finalReport)
                val cachedUri = getCachedBlobUri(context, note.id, "report", currentHash)

                if (cachedUri != null) {
                    finalReport = cachedUri
                } else {
                    val key = "notes/blob-${UUID.randomUUID()}.json"
                    val bytes = finalReport.toByteArray(Charsets.UTF_8)
                    val result = client.upload(key, bytes, contentType = "application/json")
                    if (result is R2Result.Success) {
                        val r2Uri = "$R2BLOB_PREFIX$key"
                        cacheBlobUri(context, note.id, "report", currentHash, r2Uri)
                        finalReport = r2Uri
                    } else {
                        Log.w(TAG, "Report R2 blob upload failed, using original text for cloud")
                    }
                }
            }
        }

        mapOf(
            "id" to note.id,
            "title" to note.title,
            "content" to note.content,
            "folderId" to (note.folderId ?: 0L),
            "colorHex" to note.colorHex,
            "imageUri" to (note.imageUri ?: ""),
            "drawingData" to finalDrawing,
            "reportData" to finalReport,
            "isPinned" to note.isPinned,
            "isLocked" to note.isLocked,
            "assignedTo" to note.assignedTo,
            "type" to note.type.name,
            "createdAtMillis" to note.createdAtMillis,
            "updatedAtMillis" to note.updatedAtMillis
        )
    }

    /**
     * Firestore'dan indirilen bir alandaki metin "r2blob://" ile başlıyorsa R2'den indirir.
     * İndirme başarısız olursa yerel notun mevcut verisini korur, silmez.
     */
    suspend fun resolveRemoteBlobString(
        context: Context,
        noteId: Long,
        fieldName: String,
        remoteValue: String?,
        localExistingValue: String?
    ): String? = withContext(Dispatchers.IO) {
        if (remoteValue.isNullOrBlank()) return@withContext remoteValue
        if (!remoteValue.startsWith(R2BLOB_PREFIX)) return@withContext remoteValue

        // Yerel veritabanında zaten tam içerik varsa ve referans eşleşiyorsa tekrar R2'den indirme
        if (!localExistingValue.isNullOrBlank() &&
            !localExistingValue.startsWith(R2BLOB_PREFIX) &&
            localExistingValue.length > BLOB_THRESHOLD
        ) {
            val localHash = sha256(localExistingValue)
            val cachedUri = getCachedBlobUri(context, noteId, fieldName, localHash)
            if (cachedUri == remoteValue) {
                return@withContext localExistingValue
            }
        }

        // R2'den indir
        val key = remoteValue.removePrefix(R2BLOB_PREFIX).trimStart('/')
        val client = R2Client(context)
        val result = client.download(key)

        if (result is R2Result.Success) {
            val downloadedText = String(result.data, Charsets.UTF_8)
            val downloadedHash = sha256(downloadedText)
            cacheBlobUri(context, noteId, fieldName, downloadedHash, remoteValue)
            downloadedText
        } else {
            Log.w(TAG, "R2 blob download failed ($key) for field $fieldName. Retaining local existing data.")
            // İndirme başarısız olursa yereldeki tam içeriği koru
            if (!localExistingValue.isNullOrBlank() && !localExistingValue.startsWith(R2BLOB_PREFIX)) {
                localExistingValue
            } else {
                remoteValue
            }
        }
    }
}
