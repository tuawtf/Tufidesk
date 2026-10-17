package com.example.data.cloud

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Cloudflare R2 / Worker depolama ayarlarını cihazda güvenli (şifreli) olarak saklar.
 * Hassas bilgiler (Worker URL ve Token) loglara yazılmaz ve koda gömülmez.
 */
object R2Settings {
    private const val ENCRYPTED_PREFS_NAME = "r2_secure_cloud_prefs"
    private const val FALLBACK_PREFS_NAME = "r2_cloud_prefs"

    private const val KEY_WORKER_URL = "pref_r2_worker_url"
    private const val KEY_WORKER_TOKEN = "pref_r2_worker_token"
    private const val KEY_UPLOAD_IMAGE_NOTES = "pref_r2_upload_image_notes"
    private const val KEY_UPLOAD_LARGE_BLOBS = "pref_r2_upload_large_blobs"

    @Volatile
    private var sharedPreferences: SharedPreferences? = null

    private fun getPrefs(context: Context): SharedPreferences {
        sharedPreferences?.let { return it }
        synchronized(this) {
            sharedPreferences?.let { return it }
            val prefs = try {
                val masterKey = MasterKey.Builder(context.applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context.applicationContext,
                    ENCRYPTED_PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (_: Exception) {
                // KeyStore / emulator / donanım kısıtlamasında standart güvenli moda geri dön
                context.applicationContext.getSharedPreferences(FALLBACK_PREFS_NAME, Context.MODE_PRIVATE)
            }
            sharedPreferences = prefs
            return prefs
        }
    }

    /**
     * Kullanıcının ayarlardan elle girdiği özel Worker URL'si.
     */
    fun getCustomWorkerUrl(context: Context): String {
        return getPrefs(context).getString(KEY_WORKER_URL, "")?.trim()?.trimEnd('/') ?: ""
    }

    /**
     * Aktif Cloudflare Worker URL'si.
     * Kullanıcı özel URL girdiyse onu kullanır; boşsa BuildConfig.R2_WORKER_URL değerine düşer.
     * "MY_" ile başlayan placeholder değerleri boş sayılır.
     */
    fun getWorkerUrl(context: Context): String {
        val customUrl = getCustomWorkerUrl(context)
        if (customUrl.isNotBlank()) {
            return customUrl
        }
        val buildUrl = try {
            com.example.BuildConfig.R2_WORKER_URL.trim().trimEnd('/')
        } catch (_: Exception) {
            ""
        }
        return if (buildUrl.isNotBlank() && !buildUrl.startsWith("MY_")) buildUrl else ""
    }

    fun isWorkerUrlFromBuildConfig(context: Context): Boolean {
        return getCustomWorkerUrl(context).isBlank() && getWorkerUrl(context).isNotBlank()
    }

    fun setWorkerUrl(context: Context, url: String) {
        val sanitized = url.trim().trimEnd('/')
        getPrefs(context).edit().putString(KEY_WORKER_URL, sanitized).apply()
    }

    /**
     * Kullanıcının ayarlardan elle girdiği özel Token.
     */
    fun getCustomWorkerToken(context: Context): String {
        return getPrefs(context).getString(KEY_WORKER_TOKEN, "")?.trim() ?: ""
    }

    /**
     * Aktif Cloudflare Worker Authorization Bearer belirteci (token).
     * Kullanıcı özel Token girdiyse onu kullanır; boşsa BuildConfig.R2_API_TOKEN değerine düşer.
     * "MY_" ile başlayan placeholder değerleri boş sayılır.
     */
    fun getWorkerToken(context: Context): String {
        val customToken = getCustomWorkerToken(context)
        if (customToken.isNotBlank()) {
            return customToken
        }
        val buildToken = try {
            com.example.BuildConfig.R2_API_TOKEN.trim()
        } catch (_: Exception) {
            ""
        }
        return if (buildToken.isNotBlank() && !buildToken.startsWith("MY_")) buildToken else ""
    }

    fun isWorkerTokenFromBuildConfig(context: Context): Boolean {
        return getCustomWorkerToken(context).isBlank() && getWorkerToken(context).isNotBlank()
    }

    fun setWorkerToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_WORKER_TOKEN, token.trim()).apply()
    }

    /**
     * Resim notlarının otomatik olarak R2'ye yüklenmesi açık mı?
     */
    fun isImageNotesUploadEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_UPLOAD_IMAGE_NOTES, true)
    }

    fun setImageNotesUploadEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_UPLOAD_IMAGE_NOTES, enabled).apply()
    }

    /**
     * Büyük not verilerinin (drawingData veya reportData > 100k karakter) R2'ye yüklenmesi açık mı?
     */
    fun isLargeBlobUploadEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_UPLOAD_LARGE_BLOBS, true)
    }

    fun setLargeBlobUploadEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_UPLOAD_LARGE_BLOBS, enabled).apply()
    }

    /**
     * Worker URL ve Token'ın her ikisinin de girilip girilmediğini kontrol eder.
     */
    fun isR2Configured(context: Context): Boolean {
        val url = getWorkerUrl(context)
        val token = getWorkerToken(context)
        return url.isNotBlank() && token.isNotBlank()
    }

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
