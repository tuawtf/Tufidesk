package com.example.data.speech

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.Locale
import java.util.concurrent.TimeUnit

object ModelManager {
    private const val TAG = "ModelManager"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }

    fun getModelDirectory(context: Context, type: WhisperModelType): File {
        val baseDir = File(context.filesDir, "whisper_models")
        val modelDir = File(baseDir, type.folderName)
        if (!modelDir.exists()) {
            modelDir.mkdirs()
        }
        return modelDir
    }

    fun getTokensFile(context: Context, type: WhisperModelType): File {
        return File(getModelDirectory(context, type), type.tokensFileName)
    }

    fun getEncoderFile(context: Context, type: WhisperModelType): File {
        return File(getModelDirectory(context, type), type.encoderFileName)
    }

    fun getDecoderFile(context: Context, type: WhisperModelType): File {
        return File(getModelDirectory(context, type), type.decoderFileName)
    }

    fun isModelDownloaded(context: Context, type: WhisperModelType): Boolean {
        val tokens = getTokensFile(context, type)
        val encoder = getEncoderFile(context, type)
        val decoder = getDecoderFile(context, type)

        // Tokens file should be > 100 KB, encoder > 10 MB, decoder > 30 MB
        return tokens.exists() && tokens.length() > 100_000 &&
               encoder.exists() && encoder.length() > 10_000_000 &&
               decoder.exists() && decoder.length() > 30_000_000
    }

    fun getDownloadedSizeBytes(context: Context, type: WhisperModelType): Long {
        val tokens = getTokensFile(context, type)
        val encoder = getEncoderFile(context, type)
        val decoder = getDecoderFile(context, type)
        var total = 0L
        if (tokens.exists()) total += tokens.length()
        if (encoder.exists()) total += encoder.length()
        if (decoder.exists()) total += decoder.length()
        return total
    }

    fun getDownloadedSizeFormatted(context: Context, type: WhisperModelType): String {
        val bytes = getDownloadedSizeBytes(context, type)
        if (bytes <= 0) return "0 MB"
        val mb = bytes.toDouble() / (1024 * 1024)
        return String.format(Locale.US, "%.1f MB", mb)
    }

    fun deleteModel(context: Context, type: WhisperModelType): Boolean {
        val dir = getModelDirectory(context, type)
        return try {
            dir.deleteRecursively()
        } catch (e: Exception) {
            Log.e(TAG, "Model silinirken hata: ${e.message}", e)
            false
        }
    }

    fun getModelFiles(context: Context, type: WhisperModelType): Triple<File, File, File>? {
        if (!isModelDownloaded(context, type)) return null
        return Triple(
            getTokensFile(context, type),
            getEncoderFile(context, type),
            getDecoderFile(context, type)
        )
    }

    data class DownloadItem(
        val url: String,
        val targetFile: File,
        val label: String,
        val weight: Float // relative weight for overall progress
    )

    suspend fun downloadModel(
        context: Context,
        type: WhisperModelType,
        onProgress: (overallProgress: Float, statusMessage: String) -> Unit
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val tokens = getTokensFile(context, type)
        val encoder = getEncoderFile(context, type)
        val decoder = getDecoderFile(context, type)

        val items = listOf(
            DownloadItem(type.tokensUrl, tokens, "Belirteçler (Tokens)", 0.05f),
            DownloadItem(type.encoderUrl, encoder, "Kodlayıcı (Encoder)", 0.35f),
            DownloadItem(type.decoderUrl, decoder, "Çözücü (Decoder)", 0.60f)
        )

        var completedWeights = 0f

        for (item in items) {
            val success = downloadSingleFileWithResume(item.url, item.targetFile) { fileProgress ->
                val currentOverall = completedWeights + (fileProgress * item.weight)
                val percentInt = (currentOverall * 100).coerceIn(0f, 100f).toInt()
                onProgress(currentOverall, "${item.label} indiriliyor (%$percentInt)...")
            }
            if (!success) {
                return@withContext Result.failure(
                    Exception("${item.label} dosyası indirilemedi. Lütfen internet bağlantınızı kontrol edip tekrar deneyin.")
                )
            }
            completedWeights += item.weight
        }

        onProgress(1.0f, "Model başarıyla hazırlandı!")
        Result.success(Unit)
    }

    private fun downloadSingleFileWithResume(
        url: String,
        targetFile: File,
        onFileProgress: (Float) -> Unit
    ): Boolean {
        val tempFile = File(targetFile.parentFile, "${targetFile.name}.downloading")
        var existingBytes = if (tempFile.exists()) tempFile.length() else 0L

        try {
            // First attempt with Range header if partially downloaded
            var requestBuilder = Request.Builder().url(url)
            if (existingBytes > 0) {
                requestBuilder = requestBuilder.addHeader("Range", "bytes=$existingBytes-")
            }

            var response = httpClient.newCall(requestBuilder.build()).execute()

            // If range not satisfiable (416) or server ignored range (200 with full content), restart from 0
            if (response.code == 416 || (existingBytes > 0 && response.code == 200)) {
                response.close()
                tempFile.delete()
                existingBytes = 0L
                response = httpClient.newCall(Request.Builder().url(url).build()).execute()
            }

            if (!response.isSuccessful && response.code != 206) {
                Log.e(TAG, "İndirme başarısız kod: ${response.code}")
                response.close()
                return false
            }

            val body = response.body ?: return false
            val contentLength = body.contentLength()
            val totalBytes = if (contentLength > 0) existingBytes + contentLength else -1L

            val randomAccessFile = RandomAccessFile(tempFile, "rw")
            randomAccessFile.seek(existingBytes)

            val buffer = ByteArray(8192)
            var bytesRead: Int
            var currentBytes = existingBytes
            val inputStream = body.byteStream()

            var lastReportTime = System.currentTimeMillis()

            try {
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    randomAccessFile.write(buffer, 0, bytesRead)
                    currentBytes += bytesRead

                    val now = System.currentTimeMillis()
                    if (now - lastReportTime > 100 || currentBytes == totalBytes) {
                        lastReportTime = now
                        val progress = if (totalBytes > 0) (currentBytes.toFloat() / totalBytes) else 0.5f
                        onFileProgress(progress.coerceIn(0f, 1f))
                    }
                }
            } finally {
                randomAccessFile.close()
                inputStream.close()
                body.close()
            }

            // Verify file has non-zero size and rename
            if (tempFile.exists() && tempFile.length() > 0) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                val renamed = tempFile.renameTo(targetFile)
                if (!renamed) {
                    // Fallback copy
                    tempFile.copyTo(targetFile, overwrite = true)
                    tempFile.delete()
                }
                return true
            }
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Dosya indirme hatası: ${e.message}", e)
            return false
        }
    }
}
