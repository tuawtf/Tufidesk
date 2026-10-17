package com.example.data.cloud

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class R2Result<out T> {
    data class Success<out T>(val data: T) : R2Result<T>()
    data class Error(val message: String, val statusCode: Int? = null, val cause: Throwable? = null) : R2Result<Nothing>()
}

/**
 * Cloudflare Worker üzerinden Cloudflare R2 dosya depolama işlemlerini yürüten istemci.
 * Tüm istekler Cloudflare Worker uç noktasına yönlendirilir:
 *  - PUT    {workerUrl}/files/{key}
 *  - GET    {workerUrl}/files/{key}
 *  - DELETE {workerUrl}/files/{key}
 *  - Header: Authorization: Bearer {token}
 */
class R2Client(private val context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun getFileEndpoint(key: String): String? {
        val workerUrl = R2Settings.getWorkerUrl(context)
        if (workerUrl.isBlank()) return null
        val cleanKey = key.trimStart('/')
        return "$workerUrl/files/$cleanKey"
    }

    private fun getAuthToken(): String {
        return R2Settings.getWorkerToken(context)
    }

    /**
     * Dosya baytlarını belirtilen anahtarla Cloudflare R2'ye yükler (PUT isteği).
     */
    suspend fun upload(
        key: String,
        bytes: ByteArray,
        contentType: String = "image/jpeg"
    ): R2Result<Unit> = withContext(Dispatchers.IO) {
        val url = getFileEndpoint(key)
            ?: return@withContext R2Result.Error("Cloudflare Worker URL'si tanımlanmamış. Lütfen Ayarlar > Bulut Depolama (R2) bölümünden Worker URL girin.")
        val token = getAuthToken()
        if (token.isBlank()) {
            return@withContext R2Result.Error("Cloudflare Worker Token tanımlanmamış. Lütfen Ayarlar bölümünden Token girin.")
        }

        try {
            val mediaType = contentType.toMediaType()
            val requestBody = bytes.toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .header("Authorization", "Bearer $token")
                .header("Content-Type", contentType)
                .header("Content-Length", bytes.size.toString())
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    R2Result.Success(Unit)
                } else {
                    val code = response.code
                    val errorBody = try { response.body?.string() } catch (_: Exception) { null }
                    val message = formatErrorMessage(code, errorBody)
                    R2Result.Error(message, statusCode = code)
                }
            }
        } catch (e: SocketTimeoutException) {
            R2Result.Error("Bağlantı zaman aşımına uğradı. İnternet bağlantınızı veya Worker adresinizi kontrol edin.", cause = e)
        } catch (e: UnknownHostException) {
            R2Result.Error("Sunucu adresine ulaşılamadı. Worker URL'sini kontrol edin.", cause = e)
        } catch (e: IOException) {
            R2Result.Error("Ağ bağlantı hatası oluştu: ${e.localizedMessage ?: "Bağlantı kurulamadı"}", cause = e)
        } catch (e: Exception) {
            R2Result.Error("Beklenmeyen hata: ${e.localizedMessage ?: "Bilinmeyen hata"}", cause = e)
        }
    }

    /**
     * Dosyayı Cloudflare R2'den indirir (GET isteği).
     */
    suspend fun download(key: String): R2Result<ByteArray> = withContext(Dispatchers.IO) {
        val url = getFileEndpoint(key)
            ?: return@withContext R2Result.Error("Cloudflare Worker URL'si tanımlanmamış.")
        val token = getAuthToken()
        if (token.isBlank()) {
            return@withContext R2Result.Error("Cloudflare Worker Token tanımlanmamış.")
        }

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer $token")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val data = response.body?.bytes()
                    if (data != null && data.isNotEmpty()) {
                        R2Result.Success(data)
                    } else {
                        R2Result.Error("Sunucudan boş dosya içeriği döndü.")
                    }
                } else {
                    val code = response.code
                    val errorBody = try { response.body?.string() } catch (_: Exception) { null }
                    val message = formatErrorMessage(code, errorBody)
                    R2Result.Error(message, statusCode = code)
                }
            }
        } catch (e: SocketTimeoutException) {
            R2Result.Error("İndirme zaman aşımına uğradı.", cause = e)
        } catch (e: IOException) {
            R2Result.Error("İndirme sırasında ağ bağlantısı kesildi: ${e.localizedMessage}", cause = e)
        } catch (e: Exception) {
            R2Result.Error("İndirme hatası: ${e.localizedMessage}", cause = e)
        }
    }

    /**
     * Dosyayı Cloudflare R2'den siler (DELETE isteği).
     */
    suspend fun delete(key: String): R2Result<Unit> = withContext(Dispatchers.IO) {
        val url = getFileEndpoint(key)
            ?: return@withContext R2Result.Error("Cloudflare Worker URL'si tanımlanmamış.")
        val token = getAuthToken()
        if (token.isBlank()) {
            return@withContext R2Result.Error("Cloudflare Worker Token tanımlanmamış.")
        }

        try {
            val request = Request.Builder()
                .url(url)
                .delete()
                .header("Authorization", "Bearer $token")
                .build()

            httpClient.newCall(request).execute().use { response ->
                // 200..299 veya 404 (zaten silinmiş/mevcut değil) başarılı kabul edilebilir
                if (response.isSuccessful || response.code == 404) {
                    R2Result.Success(Unit)
                } else {
                    val code = response.code
                    val errorBody = try { response.body?.string() } catch (_: Exception) { null }
                    val message = formatErrorMessage(code, errorBody)
                    R2Result.Error(message, statusCode = code)
                }
            }
        } catch (e: Exception) {
            R2Result.Error("Silme işlemi başarısız: ${e.localizedMessage}", cause = e)
        }
    }

    /**
     * Cloudflare Worker ve R2 depolama bağlantısını tam döngü test eder:
     * 1. Test dosyası yükler (PUT)
     * 2. Test dosyasını geri okur (GET)
     * 3. Test dosyasını siler (DELETE)
     */
    suspend fun testConnection(): R2Result<String> = withContext(Dispatchers.IO) {
        if (!R2Settings.isR2Configured(context)) {
            return@withContext R2Result.Error("Lütfen önce geçerli bir Worker URL ve Token girin.")
        }

        val testKey = "test/connection-test-${System.currentTimeMillis()}.txt"
        val testContent = "TufiDesk Cloudflare R2 Connection Test - OK".toByteArray(Charsets.UTF_8)

        // 1. Yükleme testi
        val uploadRes = upload(testKey, testContent, contentType = "text/plain")
        if (uploadRes is R2Result.Error) {
            return@withContext R2Result.Error(
                "Yükleme testi başarısız: ${uploadRes.message}",
                statusCode = uploadRes.statusCode
            )
        }

        // 2. Okuma testi
        val downloadRes = download(testKey)
        if (downloadRes is R2Result.Error) {
            delete(testKey)
            return@withContext R2Result.Error(
                "Okuma testi başarısız: ${downloadRes.message}",
                statusCode = downloadRes.statusCode
            )
        }

        // 3. Silme testi
        delete(testKey)

        R2Result.Success("Cloudflare R2 bağlantısı başarıyla doğrulandı! (PUT, GET, DELETE başarılı) ✅")
    }

    private fun formatErrorMessage(code: Int, responseBody: String?): String {
        return when (code) {
            401 -> "Yetkisiz erişim (401): Lütfen R2 Worker Token bilginizi kontrol edin."
            403 -> "Erişim reddedildi (403): Worker bu işlemi yürütme yetkisine sahip değil."
            404 -> "Dosya bulunamadı (404)."
            413 -> "Dosya boyutu çok büyük (413): Cloudflare Worker dosya yükleme sınırı aşıldı."
            in 500..599 -> "Sunucu hatası ($code): Cloudflare Worker veya R2 depolama servisi yanıt veremedi."
            else -> {
                val detail = responseBody?.take(150)?.trim()
                if (!detail.isNullOrBlank()) "İstek başarısız ($code): $detail"
                else "İstek başarısız oldu (HTTP $code)."
            }
        }
    }
}
