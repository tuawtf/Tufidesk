package com.example.data.cloud

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import com.example.data.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.UUID

object R2ImageManager {
    private const val TAG = "R2ImageManager"
    private const val MAX_DIMENSION = 1920
    private const val JPEG_QUALITY = 85

    /**
     * content:// veya file:// URI'sindeki görseli okur:
     * - Bellek aşımını önlemek için inSampleSize ile optimize okur
     * - EXIF yönelimini (orientation) korur
     * - Uzun kenarı en fazla 1920 px olacak şekilde orantılı küçültür
     * - JPEG kalite 85 ile sıkıştırıp bayt dizisi döndürür
     */
    suspend fun compressImage(context: Context, uriString: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)

            // 1. Önce boyutları öğren
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(inputStream, null, boundsOptions)
            inputStream?.close()

            val srcWidth = boundsOptions.outWidth
            val srcHeight = boundsOptions.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) {
                Log.w(TAG, "Görsel boyutları okunamadı: $uriString")
                return@withContext null
            }

            // 2. inSampleSize hesapla
            var inSampleSize = 1
            val maxSrcDim = maxOf(srcWidth, srcHeight)
            while ((maxSrcDim / inSampleSize) > MAX_DIMENSION * 1.5) {
                inSampleSize *= 2
            }

            // 3. Bitmap'i hafızaya al
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            inputStream = context.contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) {
                Log.w(TAG, "Görsel decode edilemedi: $uriString")
                return@withContext null
            }

            // 4. EXIF oryantasyonunu kontrol et
            var rotationDegrees = 0f
            try {
                context.contentResolver.openInputStream(uri)?.use { exifStream ->
                    val exif = android.media.ExifInterface(exifStream)
                    val orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                    )
                    rotationDegrees = when (orientation) {
                        android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                        android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                        android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                        else -> 0f
                    }
                }
            } catch (_: Exception) {}

            // 5. 1920 px sınırına göre ölçekle
            val currentMax = maxOf(bitmap.width, bitmap.height)
            val scale = if (currentMax > MAX_DIMENSION) {
                MAX_DIMENSION.toFloat() / currentMax.toFloat()
            } else {
                1.0f
            }

            if (scale < 1.0f || rotationDegrees != 0f) {
                val matrix = Matrix()
                if (scale < 1.0f) {
                    matrix.postScale(scale, scale)
                }
                if (rotationDegrees != 0f) {
                    matrix.postRotate(rotationDegrees)
                }
                val transformedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                if (transformedBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = transformedBitmap
                }
            }

            // 6. JPEG 85 sıkıştır
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, baos)
            bitmap.recycle()
            baos.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "Görsel sıkıştırma hatası: ${e.message}", e)
            null
        }
    }

    /**
     * Eğer notta yerel bir görsel varsa ve R2 ayarları etkinse:
     * Görseli sıkıştırır ve "notes/img-{UUID}.jpg" anahtarıyla R2'ye yükler.
     * Başarılıysa note.imageUri = "r2://notes/img-{UUID}.jpg" olan yeni not nesnesini döner.
     * Başarısız olursa veya R2 kapalıysa yerel URI'li mevcut notu korur, çökmez.
     */
    suspend fun uploadNoteImageIfEligible(context: Context, note: Note): Note = withContext(Dispatchers.IO) {
        val uri = note.imageUri
        if (uri.isNullOrBlank()) return@withContext note

        // Zaten r2:// formatındaysa tekrar yükleme yapma
        if (uri.startsWith("r2://")) {
            return@withContext note
        }

        // Yalnızca content:// veya file:// yerel URI'leri için işlem yap
        if (!uri.startsWith("content://") && !uri.startsWith("file://")) {
            return@withContext note
        }

        // R2 yapılandırılmamış veya resim notları yüklemesi kapatılmışsa yerel olarak bırak
        if (!R2Settings.isR2Configured(context) || !R2Settings.isImageNotesUploadEnabled(context)) {
            return@withContext note
        }

        val compressedBytes = compressImage(context, uri) ?: return@withContext note

        val uuid = UUID.randomUUID().toString()
        val fileKey = "notes/img-$uuid.jpg"

        val client = R2Client(context)
        val uploadResult = client.upload(fileKey, compressedBytes, contentType = "image/jpeg")

        return@withContext if (uploadResult is R2Result.Success) {
            val r2Uri = "r2://$fileKey"
            note.copy(imageUri = r2Uri, updatedAtMillis = System.currentTimeMillis())
        } else {
            // Yükleme başarısız olursa yerel URI'yi koru, notu kaydet, uygulama çökmesin
            val errMsg = (uploadResult as? R2Result.Error)?.message ?: "Bilinmeyen hata"
            Log.w(TAG, "R2 görsel yükleme başarısız oldu (yerel URI korundu): $errMsg")
            note
        }
    }

    /**
     * Not silindiğinde, eğer imageUri r2:// ile başlıyorsa R2'den best-effort siler.
     */
    suspend fun deleteR2FileIfPresent(context: Context, imageUri: String?) = withContext(Dispatchers.IO) {
        if (imageUri.isNullOrBlank() || !imageUri.startsWith("r2://")) return@withContext
        if (!R2Settings.isR2Configured(context)) return@withContext

        val key = imageUri.removePrefix("r2://").trimStart('/')
        try {
            val client = R2Client(context)
            client.delete(key)
        } catch (e: Exception) {
            Log.w(TAG, "R2 dosyasını best-effort silme hatası ($key): ${e.message}")
        }
    }
}
