package com.example.data.cloud

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

data class UpdateInfo(
    val versionName: String,
    val versionCode: Long,
    val apkKey: String,
    val sha256: String,
    val size: Long
)

sealed class UpdateCheckStatus {
    data class NewVersionAvailable(val updateInfo: UpdateInfo) : UpdateCheckStatus()
    data class UpToDate(val currentVersionName: String, val currentVersionCode: Long) : UpdateCheckStatus()
    data class Error(val message: String, val isNotConfigured: Boolean = false) : UpdateCheckStatus()
}

sealed class DownloadStatus {
    data class Progress(val downloadedBytes: Long, val totalBytes: Long) : DownloadStatus()
    data class Success(val apkFile: File) : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

object R2UpdateManager {

    private const val UPDATES_DIR_NAME = "updates"
    private const val APK_FILE_NAME = "app-update.apk"

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * R2'deki updates/latest.json dosyasını kontrol eder ve yeni sürüm olup olmadığını sorgular.
     */
    suspend fun checkForUpdate(context: Context): UpdateCheckStatus = withContext(Dispatchers.IO) {
        if (!R2Settings.isR2Configured(context)) {
            return@withContext UpdateCheckStatus.Error(
                message = "Cloudflare R2 (Worker URL ve Token) yapılandırılmamış. Lütfen önce Bulut Depolama ayarlarını yapın.",
                isNotConfigured = true
            )
        }

        val client = R2Client(context)
        val result = client.download("updates/latest.json")

        when (result) {
            is R2Result.Success -> {
                try {
                    val jsonStr = String(result.data, Charsets.UTF_8)
                    val json = JSONObject(jsonStr)

                    val versionName = json.optString("versionName", "1.0.0")
                    val versionCode = json.optLong("versionCode", 0L)
                    val apkKey = json.optString("apkKey", "").trimStart('/')
                    val sha256 = json.optString("sha256", "").lowercase().trim()
                    val size = json.optLong("size", 0L)

                    if (apkKey.isBlank()) {
                        return@withContext UpdateCheckStatus.Error("latest.json dosyasında APK indirme anahtarı (apkKey) bulunamadı.")
                    }

                    val updateInfo = UpdateInfo(
                        versionName = versionName,
                        versionCode = versionCode,
                        apkKey = apkKey,
                        sha256 = sha256,
                        size = size
                    )

                    val currentCode = try { BuildConfig.VERSION_CODE.toLong() } catch (_: Exception) { 1L }

                    if (versionCode > currentCode) {
                        UpdateCheckStatus.NewVersionAvailable(updateInfo)
                    } else {
                        val currentName = try { BuildConfig.VERSION_NAME } catch (_: Exception) { "1.0.0" }
                        UpdateCheckStatus.UpToDate(currentName, currentCode)
                    }
                } catch (e: Exception) {
                    UpdateCheckStatus.Error("Güncelleme bilgisi (latest.json) işlenemedi: ${e.localizedMessage ?: "Format hatası"}")
                }
            }
            is R2Result.Error -> {
                val msg = when (result.statusCode) {
                    404 -> "Henüz R2'de yayımlanmış güncelleme bulunamadı (updates/latest.json mevcut değil)."
                    401 -> "Worker erişim yetkisi reddedildi (401). Lütfen R2 Token bilginizi kontrol edin."
                    else -> result.message
                }
                UpdateCheckStatus.Error(msg)
            }
        }
    }

    /**
     * APK dosyasını R2'den indirir, ilerleme bildirir ve SHA-256 doğrulamasını yapar.
     */
    suspend fun downloadApk(
        context: Context,
        apkKey: String,
        expectedSha256: String,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit
    ): DownloadStatus = withContext(Dispatchers.IO) {
        val workerUrl = R2Settings.getWorkerUrl(context)
        val token = R2Settings.getWorkerToken(context)

        if (workerUrl.isBlank() || token.isBlank()) {
            return@withContext DownloadStatus.Error("Cloudflare R2 bilgileri eksik.")
        }

        val cleanKey = apkKey.trimStart('/')
        val url = "$workerUrl/files/$cleanKey"

        val updatesDir = File(context.cacheDir, UPDATES_DIR_NAME)
        if (!updatesDir.exists()) {
            updatesDir.mkdirs()
        }

        val destinationFile = File(updatesDir, APK_FILE_NAME)
        if (destinationFile.exists()) {
            destinationFile.delete()
        }

        try {
            val request = Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer $token")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code
                val errMessage = when (code) {
                    404 -> "APK dosyası R2 sunucusunda bulunamadı ($cleanKey)."
                    401 -> "Erişim yetkisiz (401). Token bilginizi kontrol edin."
                    else -> "APK indirme başarısız oldu (HTTP $code)."
                }
                return@withContext DownloadStatus.Error(errMessage)
            }

            val body = response.body ?: return@withContext DownloadStatus.Error("Sunucudan boş yanıt döndü.")
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            body.byteStream().use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        coroutineContext.ensureActive()
                        outputStream.write(buffer, 0, read)
                        downloadedBytes += read
                        onProgress(downloadedBytes, totalBytes)
                    }
                    outputStream.flush()
                }
            }

            // SHA-256 Doğrulaması
            if (expectedSha256.isNotBlank()) {
                val calculatedHash = calculateSha256(destinationFile)
                if (!calculatedHash.equals(expectedSha256.trim().lowercase(), ignoreCase = true)) {
                    destinationFile.delete()
                    return@withContext DownloadStatus.Error(
                        "İndirilen APK dosyasının SHA-256 doğrulaması başarısız oldu!\n" +
                                "Beklenen: ${expectedSha256.take(12)}...\n" +
                                "Hesaplanan: ${calculatedHash.take(12)}..."
                    )
                }
            }

            DownloadStatus.Success(destinationFile)
        } catch (e: kotlinx.coroutines.CancellationException) {
            if (destinationFile.exists()) destinationFile.delete()
            throw e
        } catch (e: SocketTimeoutException) {
            if (destinationFile.exists()) destinationFile.delete()
            DownloadStatus.Error("Ağ bağlantısı zaman aşımına uğradı.")
        } catch (e: UnknownHostException) {
            if (destinationFile.exists()) destinationFile.delete()
            DownloadStatus.Error("Sunucu adresine ulaşılamadı. İnternet bağlantınızı kontrol edin.")
        } catch (e: IOException) {
            if (destinationFile.exists()) destinationFile.delete()
            DownloadStatus.Error("Dosya indirilirken ağ hatası oluştu: ${e.localizedMessage}")
        } catch (e: Exception) {
            if (destinationFile.exists()) destinationFile.delete()
            DownloadStatus.Error("APK indirilemedi: ${e.localizedMessage}")
        }
    }

    /**
     * İndirilen APK için kurulum Intent'ini çalıştırır.
     * Bilinmeyen kaynak izni yoksa "PERMISSION_REQUIRED" metnini döner.
     */
    fun installApk(context: Context, apkFile: File): String? {
        try {
            if (!apkFile.exists()) return "Kurulum dosyası bulunamadı."

            // Android 8.0+ (API 26) Bilinmeyen kaynak izni kontrolü
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                    return "PERMISSION_REQUIRED"
                }
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            return null
        } catch (e: Exception) {
            return "Kurulum başlatılamadı: ${e.localizedMessage}"
        }
    }

    /**
     * Geçici olarak indirilen güncelleme APK'larını temizler.
     */
    fun clearUpdatesCache(context: Context) {
        try {
            val updatesDir = File(context.cacheDir, UPDATES_DIR_NAME)
            if (updatesDir.exists()) {
                updatesDir.deleteRecursively()
            }
        } catch (_: Exception) {}
    }

    private fun calculateSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { inputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
