package com.example.data.verification

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Base64
import android.util.Log
import com.example.data.ai.TufiAIService
import com.example.data.speech.SpeechSettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

data class PageAiAnalysis(
    val pageNumbers: List<Int> = emptyList(),
    val titleOrSubject: String? = null,
    val hasHandwriting: Boolean = false,
    val quality: String = "good", // "good", "blurry", "dark", "cropped"
    val isScreenPhoto: Boolean = false
)

sealed class VerificationDecision {
    data class Success(val hash: Long, val message: String = "Onaylandı") : VerificationDecision()
    data class Rejected(val reason: String, val hash: Long = 0L) : VerificationDecision()
    data class Error(val errorMessage: String) : VerificationDecision()
}

object HomeworkVerificationService {
    private const val TAG = "HomeworkVerification"
    private const val OPENROUTER_CHAT_URL = "https://openrouter.ai/api/v1/chat/completions"

    private val httpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getApiKey(context: Context): String {
        val openRouterKey = SpeechSettingsManager.getOpenRouterApiKey(context)
        if (openRouterKey.isNotBlank()) return openRouterKey
        return TufiAIService.getApiKey(context)
    }

    fun resizeAndCompressImage(bitmap: Bitmap, maxDimension: Int = 1280, quality: Int = 80): ByteArray {
        var width = bitmap.width
        var height = bitmap.height
        if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            if (ratio > 1) {
                width = maxDimension
                height = (maxDimension / ratio).toInt().coerceAtLeast(1)
            } else {
                height = maxDimension
                width = (maxDimension * ratio).toInt().coerceAtLeast(1)
            }
        }
        val scaled = if (width != bitmap.width || height != bitmap.height) {
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }
        val outputStream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        if (scaled != bitmap) {
            scaled.recycle()
        }
        return outputStream.toByteArray()
    }

    suspend fun verifyPagePhoto(
        context: Context,
        photoBitmap: Bitmap,
        expectedPage: Int,
        existingHashes: List<Long>
    ): VerificationDecision = withContext(Dispatchers.IO) {
        if (!isNetworkAvailable(context)) {
            return@withContext VerificationDecision.Error("İnternet bağlantısı yok. Lütfen bağlantınızı kontrol edin.")
        }

        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) {
            return@withContext VerificationDecision.Error("OpenRouter API anahtarı ayarlanmamış. Lütfen Ayarlar > Sesli Dikte veya AI bölümünden anahtar girin.")
        }

        // 1. dHash hesapla ve mükerrer fotoğraf kontrolü yap (Hamming mesafesi < 6)
        val imageHash = DHashCalculator.calculateDHash(photoBitmap)
        if (DHashCalculator.isDuplicate(imageHash, existingHashes, threshold = 6)) {
            return@withContext VerificationDecision.Rejected("Bu fotoğraf daha önce kullanıldı", imageHash)
        }

        // 2. Resmi 1280px ve JPEG 80 kalitesinde küçült
        val jpegBytes = resizeAndCompressImage(photoBitmap, maxDimension = 1280, quality = 80)
        val base64Data = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        val dataUrl = "data:image/jpeg;base64,$base64Data"

        val model = HomeworkVerificationSettings.getVerificationModel(context)
        val requireHandwriting = HomeworkVerificationSettings.isHandwritingRequired(context)

        // 3. OpenRouter API Çağrısı (Bozuk JSON gelirse 1 kez tekrar dene)
        var analysis: PageAiAnalysis? = null
        var lastErrorMessage: String? = null

        for (attempt in 1..2) {
            try {
                val responseJson = callOpenRouterVision(apiKey, model, dataUrl)
                analysis = parseAiResponse(responseJson)
                if (analysis != null) {
                    HomeworkVerificationSettings.incrementMonthlyCheckedPhotoCount(context)
                    break
                } else {
                    lastErrorMessage = "Yapay zeka yanıtı ayrıştırılamadı."
                }
            } catch (e: SocketTimeoutException) {
                lastErrorMessage = "Sunucu yanıt vermedi (Zaman aşımı). Lütfen tekrar deneyin."
            } catch (e: IOException) {
                val msg = e.message ?: ""
                lastErrorMessage = when {
                    msg.contains("401") -> "API anahtarı geçersiz veya yetkisiz (401). Lütfen OpenRouter API anahtarınızı kontrol edin."
                    msg.contains("429") -> "API istek limiti aşıldı (429). Lütfen bir süre sonra tekrar deneyin veya kredinizi kontrol edin."
                    else -> "Bağlantı hatası: ${e.localizedMessage ?: "Sunucuya ulaşılamadı"}"
                }
                if (msg.contains("401") || msg.contains("429")) {
                    break // Tekrar deneme
                }
            } catch (e: Exception) {
                lastErrorMessage = "Bir hata oluştu: ${e.localizedMessage ?: e.javaClass.simpleName}"
            }
        }

        if (analysis == null) {
            return@withContext VerificationDecision.Error(lastErrorMessage ?: "Doğrulama yapılamadı.")
        }

        // 4. Karar KODDA verilsin:
        // Kalite kontrolü
        val quality = analysis.quality.lowercase()
        if (quality == "blurry" || quality == "dark") {
            return@withContext VerificationDecision.Rejected("Fotoğraf bulanık", imageHash)
        }

        // Sayfa numarası kontrolü
        if (analysis.pageNumbers.isEmpty()) {
            return@withContext VerificationDecision.Rejected("Sayfa numarası okunamadı, tekrar çek", imageHash)
        }

        if (expectedPage !in analysis.pageNumbers) {
            val readPages = analysis.pageNumbers.joinToString(", ")
            return@withContext VerificationDecision.Rejected("Yanlış sayfa (okunan: $readPages, beklenen: $expectedPage)", imageHash)
        }

        // El yazısı kontrolü (ayar açıksa)
        if (requireHandwriting && !analysis.hasHandwriting) {
            return@withContext VerificationDecision.Rejected("Sayfada yazı bulunamadı", imageHash)
        }

        return@withContext VerificationDecision.Success(imageHash, "Onaylandı")
    }

    private fun callOpenRouterVision(apiKey: String, model: String, dataUrl: String): String {
        val rootJson = JSONObject().apply {
            put("model", model)
            put("temperature", 0)
            put("max_tokens", 300)

            val messagesArray = JSONArray()

            // System message
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", "Sadece JSON döndür. Sayfadaki cevapları değerlendirme.")
            })

            // User message with text and image_url
            val userContent = JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "text")
                    put("text", "Bu fotoğrafın ödev sayfası olup olmadığını kontrol et. Görünen basılı sayfa numaralarını, başlığı/konuyu, el yazısı olup olmadığını ve kalitesini belirle. Yanıtını SADECE şu JSON şemasında ver: {\"page_numbers\":[int], \"title_or_subject\":string|null, \"has_handwriting\":bool, \"quality\":\"good\"|\"blurry\"|\"dark\"|\"cropped\", \"is_screen_photo\":bool}")
                })
                put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", JSONObject().apply {
                        put("url", dataUrl)
                    })
                })
            }

            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", userContent)
            })

            put("messages", messagesArray)
        }

        val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(OPENROUTER_CHAT_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("HTTP-Referer", "https://github.com/tufidesk")
            .addHeader("X-Title", "TufiDESK")
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            val code = response.code
            Log.e(TAG, "OpenRouter API error $code: $responseBody")
            throw IOException("HTTP $code: $responseBody")
        }

        return responseBody
    }

    private fun parseAiResponse(responseJsonStr: String): PageAiAnalysis? {
        return try {
            val responseObj = JSONObject(responseJsonStr)
            val choices = responseObj.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val choice = choices.getJSONObject(0)
            val message = choice.optJSONObject("message") ?: return null
            val content = message.optString("content", "")

            val cleaned = cleanJsonMarkdown(content)
            val obj = JSONObject(cleaned)

            val pageNumbers = mutableListOf<Int>()
            val pagesArr = obj.optJSONArray("page_numbers")
            if (pagesArr != null) {
                for (i in 0 until pagesArr.length()) {
                    val p = pagesArr.optInt(i, -1)
                    if (p > 0) pageNumbers.add(p)
                }
            }

            val titleOrSubject = if (obj.isNull("title_or_subject")) null else obj.optString("title_or_subject", null)
            val hasHandwriting = obj.optBoolean("has_handwriting", false)
            val quality = obj.optString("quality", "good")
            val isScreenPhoto = obj.optBoolean("is_screen_photo", false)

            PageAiAnalysis(
                pageNumbers = pageNumbers,
                titleOrSubject = titleOrSubject,
                hasHandwriting = hasHandwriting,
                quality = quality,
                isScreenPhoto = isScreenPhoto
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse AI response JSON: ${e.message}")
            null
        }
    }

    private fun cleanJsonMarkdown(raw: String): String {
        var text = raw.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1)
        }
        return text.trim()
    }
}
