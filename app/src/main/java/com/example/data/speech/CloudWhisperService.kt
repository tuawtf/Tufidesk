package com.example.data.speech

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class CloudTranscriptionResult {
    data class Success(val text: String) : CloudTranscriptionResult()
    data class Error(val message: String, val statusCode: Int? = null, val cause: Throwable? = null) : CloudTranscriptionResult()
}

object CloudWhisperService {
    private const val TAG = "CloudWhisperService"
    private const val OPENROUTER_TRANSCRIPTION_URL = "https://openrouter.ai/api/v1/audio/transcriptions"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Transcribes audio using OpenRouter Audio Transcriptions API.
     * Takes WAV byte array (16 kHz, mono, 16-bit PCM).
     */
    suspend fun transcribeAudio(
        context: Context,
        wavBytes: ByteArray,
        modelName: String = SpeechSettingsManager.getCloudModelName(context)
    ): CloudTranscriptionResult = withContext(Dispatchers.IO) {
        if (!isOnline(context)) {
            return@withContext CloudTranscriptionResult.Error(
                message = "İnternet bağlantısı kurulamadı. Lütfen ağınızı kontrol edin veya Ayarlar'dan 'Çevrimdışı Whisper' seçeneğine geçin."
            )
        }

        val apiKey = SpeechSettingsManager.getOpenRouterApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext CloudTranscriptionResult.Error(
                message = "OpenRouter API anahtarı yapılandırılmamış. Lütfen Ayarlar > Sesli Dikte bölümünden bir OpenRouter anahtarı girin.",
                statusCode = 401
            )
        }

        val base64Audio = Base64.encodeToString(wavBytes, Base64.NO_WRAP)

        // 1. Önce resmi OpenRouter JSON input_audio formatı ile dene
        val jsonPayload = JSONObject().apply {
            put("model", modelName)
            put("language", "tr")
            val inputAudioObj = JSONObject().apply {
                put("data", base64Audio)
                put("format", "wav")
            }
            put("input_audio", inputAudioObj)
        }

        val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(OPENROUTER_TRANSCRIPTION_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("HTTP-Referer", "https://tufidesk.local")
            .addHeader("X-Title", "TufiDESK")
            .post(requestBody)
            .build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                Log.d(TAG, "OpenRouter STT yanıt kodu: ${response.code}")

                if (response.isSuccessful) {
                    val json = JSONObject(responseBody)
                    val text = json.optString("text", "").trim()
                    if (text.isNotBlank()) {
                        return@withContext CloudTranscriptionResult.Success(text)
                    } else {
                        return@withContext CloudTranscriptionResult.Error("API'den metin döndürülmedi (boş yanıt).")
                    }
                }

                // Hata kodları analizi
                return@withContext when (response.code) {
                    401 -> CloudTranscriptionResult.Error(
                        message = "OpenRouter API anahtarı geçersiz veya yetkisiz (401). Lütfen Ayarlar'dan anahtarınızı kontrol edin.",
                        statusCode = 401
                    )
                    402, 429 -> CloudTranscriptionResult.Error(
                        message = "OpenRouter istek limiti veya kredi bakiyesi yetersiz (429/402). Lütfen OpenRouter hesabınızı kontrol edin.",
                        statusCode = response.code
                    )
                    400 -> {
                        // Eğer JSON input_audio model tarafından desteklenmediyse multipart/form-data ile yedek dene
                        val fallbackResult = tryMultipartFallback(apiKey, modelName, wavBytes)
                        if (fallbackResult != null) {
                            fallbackResult
                        } else {
                            val errMsg = parseErrorMessage(responseBody)
                            CloudTranscriptionResult.Error("İstek hatası (400): $errMsg", statusCode = 400)
                        }
                    }
                    else -> {
                        val errMsg = parseErrorMessage(responseBody)
                        CloudTranscriptionResult.Error("OpenRouter hatası (${response.code}): $errMsg", statusCode = response.code)
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "OpenRouter STT ağ hatası", t)
            return@withContext CloudTranscriptionResult.Error(
                message = "Ses sunucuya iletilirken ağ hatası oluştu: ${t.localizedMessage ?: t.javaClass.simpleName}",
                cause = t
            )
        }
    }

    /**
     * OpenAI uyumlu multipart/form-data yedek istek
     */
    private fun tryMultipartFallback(
        apiKey: String,
        modelName: String,
        wavBytes: ByteArray
    ): CloudTranscriptionResult? {
        try {
            val filePart = wavBytes.toRequestBody("audio/wav".toMediaType())
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", modelName)
                .addFormDataPart("language", "tr")
                .addFormDataPart("file", "audio.wav", filePart)
                .build()

            val request = Request.Builder()
                .url(OPENROUTER_TRANSCRIPTION_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://tufidesk.local")
                .addHeader("X-Title", "TufiDESK")
                .post(multipartBody)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val json = JSONObject(body)
                    val text = json.optString("text", "").trim()
                    if (text.isNotBlank()) {
                        return CloudTranscriptionResult.Success(text)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Multipart fallback failed: ${e.message}")
        }
        return null
    }

    private fun parseErrorMessage(responseBody: String): String {
        return try {
            val json = JSONObject(responseBody)
            if (json.has("error")) {
                val errObj = json.optJSONObject("error")
                if (errObj != null) {
                    errObj.optString("message", responseBody.take(150))
                } else {
                    json.optString("error", responseBody.take(150))
                }
            } else {
                responseBody.take(150)
            }
        } catch (_: Exception) {
            responseBody.take(150)
        }
    }

    /**
     * OpenRouter API anahtarını canlı test etme
     */
    suspend fun testApiKey(context: Context, testKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val trimmedKey = testKey.trim()
        if (trimmedKey.isBlank()) {
            return@withContext Pair(false, "API anahtarı boş olamaz.")
        }
        if (!isOnline(context)) {
            return@withContext Pair(false, "İnternet bağlantısı yok.")
        }

        try {
            val request = Request.Builder()
                .url("https://openrouter.ai/api/v1/auth/key")
                .addHeader("Authorization", "Bearer $trimmedKey")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    return@withContext Pair(true, "OpenRouter API anahtarı geçerli ve aktif! ✅")
                } else if (response.code == 401) {
                    return@withContext Pair(false, "Geçersiz API anahtarı (401 Yetkisiz).")
                } else {
                    val msg = parseErrorMessage(body)
                    return@withContext Pair(false, "Doğrulama hatası (${response.code}): $msg")
                }
            }
        } catch (t: Throwable) {
            return@withContext Pair(false, "Bağlantı hatası: ${t.localizedMessage ?: t.javaClass.simpleName}")
        }
    }
}
