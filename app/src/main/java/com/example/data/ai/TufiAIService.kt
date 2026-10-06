package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Course
import com.example.data.model.HomeworkWithCourse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Tufi AI sadeleştirilmiş ve doğrudan ödevlere odaklı koçluk modeli.
 */
data class TufiAICoachAdvice(
    val headline: String,
    val summary: String,
    val todayTasksInsight: String,       // Bugünkü ödevler hakkında somut veri ve strateji
    val missingTasksInsight: String,     // Eksik / bekleyen / geciken ödevler analizi
    val weeklyInsight: String,           // Hafta hakkında genel veri ve tempo
    val stepByStepPlan: List<String>,    // Bugünkü ödevleri nasıl ilerletmeli (adım adım yol haritası)
    val studyMethodTip: String,          // Nasıl çalışmalı (taktik, odak, pomodoro)
    val timestampMillis: Long = System.currentTimeMillis(),
    val isFromApi: Boolean = true
)

/**
 * Tufi AI minimalist soru-cevap sohbet modeli (ChatGPT / Gemini / Claude tarzı).
 */
data class TufiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)

/**
 * Tufi AI çoklu sohbet oturumu modeli (ChatGPT tarzı geçmiş sohbetler).
 */
data class TufiChatSession(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val messages: List<TufiChatMessage> = emptyList()
)

object TufiAIService {
    private const val TAG = "TufiAIService"
    private const val PREFS_NAME = "tufiai_cache_prefs"
    private const val PREF_KEY_ADVICE_PREFIX = "advice_v2_"
    private const val PREF_KEY_SIG_PREFIX = "sig_v2_"
    private const val PREF_KEY_CUSTOM_API_KEY = "custom_deepseek_api_key"
    private const val PREF_KEY_LAST_SAVED_MESSAGE = "last_saved_advice_v2"
    private const val PREF_KEY_LAST_SAVED_SIG = "last_saved_sig_v2"
    private const val PREF_KEY_CHAT_HISTORY = "tufiai_chat_history_v1"
    private const val PREF_KEY_CHAT_SESSIONS = "tufiai_chat_sessions_v2"
    private const val PREF_KEY_ACTIVE_SESSION_ID = "tufiai_active_session_id_v2"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(25, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Aktif API anahtarını döner:
     * 1. Kullanıcının arayüzden girdiği özel anahtar (SharedPreferences)
     * 2. BuildConfig içerisinde tanımlı anahtarlar (DEEPSEEK_API_KEY veya GEMINI_API_KEY)
     */
    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val customKey = prefs.getString(PREF_KEY_CUSTOM_API_KEY, null)?.trim()
        if (!customKey.isNullOrBlank()) {
            return customKey
        }
        val buildKey = try {
            BuildConfig.DEEPSEEK_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_DEEPSEEK_API_KEY") buildKey else ""
    }

    fun isApiKeyConfigured(context: Context): Boolean {
        return getApiKey(context).isNotBlank()
    }

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_CUSTOM_API_KEY, key.trim()).apply()
    }

    /**
     * API anahtarını canlı olarak test eder.
     * Hem DeepSeek (sk-...) hem de Google Gemini (AIzaSy...) anahtarlarını destekler.
     */
    suspend fun testConnection(context: Context, key: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val trimmedKey = key.trim()
        if (trimmedKey.isBlank()) {
            return@withContext Pair(false, "API anahtarı boş olamaz.")
        }

        val isGemini = trimmedKey.startsWith("AIzaSy")
        val startTime = System.currentTimeMillis()

        try {
            if (isGemini) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$trimmedKey"
                val json = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", "Merhaba, bağlantı testi.") })
                            })
                        })
                    })
                }
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute().use { response ->
                    val duration = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        Pair(true, "Gemini API bağlantısı başarılı! (${duration}ms)")
                    } else {
                        val code = response.code
                        val errorMsg = when (code) {
                            400 -> "Geçersiz istek (400): Anahtar formatını kontrol edin."
                            403 -> "Yetki hatası (403): API anahtarınız Gemini API için etkinleştirilmemiş."
                            404 -> "Model bulunamadı (404)."
                            429 -> "Hız sınırı aşıldı (429): Lütfen biraz bekleyin."
                            else -> "HTTP $code hatası: ${response.message}"
                        }
                        Pair(false, errorMsg)
                    }
                }
            } else {
                // DeepSeek API
                val url = "https://api.deepseek.com/chat/completions"
                val json = JSONObject().apply {
                    put("model", "deepseek-chat")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "ping")
                        })
                    })
                    put("max_tokens", 5)
                }
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer $trimmedKey")
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val duration = System.currentTimeMillis() - startTime
                    if (response.isSuccessful) {
                        Pair(true, "DeepSeek API bağlantısı başarılı! (${duration}ms)")
                    } else {
                        val code = response.code
                        val errorMsg = when (code) {
                            401 -> "Yetkilendirme hatası (401): API anahtarı geçersiz."
                            402 -> "Yetersiz bakiye (402): DeepSeek hesabınızda kullanılabilir bakiye kalmamış."
                            429 -> "İstek sınırı aşıldı (429): Lütfen biraz bekleyin."
                            else -> "HTTP $code hatası: ${response.message}"
                        }
                        Pair(false, errorMsg)
                    }
                }
            }
        } catch (e: Exception) {
            Pair(false, "Bağlantı hatası: ${e.localizedMessage ?: "Sunucuya ulaşılamadı"}")
        }
    }

    // ==========================================
    // CHAT HISTORY STORAGE (Sohbet Geçmişi)
    // ==========================================

    fun loadChatHistory(context: Context): List<TufiChatMessage> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(PREF_KEY_CHAT_HISTORY, null) ?: return emptyList()
        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TufiChatMessage>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TufiChatMessage(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        isUser = obj.optBoolean("isUser", false),
                        text = obj.optString("text", ""),
                        timestampMillis = obj.optLong("timestampMillis", System.currentTimeMillis())
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Chat history parse error", e)
            emptyList()
        }
    }

    fun saveChatHistory(context: Context, messages: List<TufiChatMessage>, activeSessionId: String? = null): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        messages.takeLast(60).forEach { msg ->
            val obj = JSONObject().apply {
                put("id", msg.id)
                put("isUser", msg.isUser)
                put("text", msg.text)
                put("timestampMillis", msg.timestampMillis)
            }
            array.put(obj)
        }
        prefs.edit().putString(PREF_KEY_CHAT_HISTORY, array.toString()).apply()

        // Oturumu da güncelle veya oluştur
        val targetSessionId = activeSessionId ?: getActiveSessionId(context) ?: UUID.randomUUID().toString()
        val firstUserMsg = messages.firstOrNull { it.isUser }?.text?.take(40) ?: "Sohbet"
        val session = TufiChatSession(
            id = targetSessionId,
            title = firstUserMsg,
            createdAtMillis = messages.firstOrNull()?.timestampMillis ?: System.currentTimeMillis(),
            updatedAtMillis = System.currentTimeMillis(),
            messages = messages
        )
        saveSession(context, session)
        setActiveSessionId(context, targetSessionId)
        return targetSessionId
    }

    fun clearChatHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PREF_KEY_CHAT_HISTORY).apply()
        setActiveSessionId(context, null)
    }

    // ==========================================
    // CHAT SESSIONS (Çoklu Oturum Yönetimi)
    // ==========================================

    fun loadAllSessions(context: Context): List<TufiChatSession> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(PREF_KEY_CHAT_SESSIONS, null)
        if (jsonStr.isNullOrBlank()) {
            val oldMsgs = loadChatHistory(context)
            if (oldMsgs.isNotEmpty()) {
                val firstUserMsg = oldMsgs.firstOrNull { it.isUser }?.text?.take(40) ?: "Önceki Sohbet"
                val initialSession = TufiChatSession(
                    id = UUID.randomUUID().toString(),
                    title = firstUserMsg,
                    createdAtMillis = oldMsgs.first().timestampMillis,
                    updatedAtMillis = oldMsgs.last().timestampMillis,
                    messages = oldMsgs
                )
                // Doğrudan SharedPreferences'a yaz, saveSession çağırma (StackOverflow / döngü engellenir)
                val array = JSONArray()
                val sObj = JSONObject().apply {
                    put("id", initialSession.id)
                    put("title", initialSession.title)
                    put("createdAtMillis", initialSession.createdAtMillis)
                    put("updatedAtMillis", initialSession.updatedAtMillis)
                    val msgsArray = JSONArray()
                    initialSession.messages.forEach { m ->
                        msgsArray.put(JSONObject().apply {
                            put("id", m.id)
                            put("isUser", m.isUser)
                            put("text", m.text)
                            put("timestampMillis", m.timestampMillis)
                        })
                    }
                    put("messages", msgsArray)
                }
                array.put(sObj)
                prefs.edit()
                    .putString(PREF_KEY_CHAT_SESSIONS, array.toString())
                    .putString(PREF_KEY_ACTIVE_SESSION_ID, initialSession.id)
                    .apply()
                return listOf(initialSession)
            }
            return emptyList()
        }

        return try {
            val array = JSONArray(jsonStr)
            val list = mutableListOf<TufiChatSession>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val msgsArray = obj.optJSONArray("messages") ?: JSONArray()
                val msgs = mutableListOf<TufiChatMessage>()
                for (j in 0 until msgsArray.length()) {
                    val mObj = msgsArray.getJSONObject(j)
                    msgs.add(
                        TufiChatMessage(
                            id = mObj.optString("id", UUID.randomUUID().toString()),
                            isUser = mObj.optBoolean("isUser", false),
                            text = mObj.optString("text", ""),
                            timestampMillis = mObj.optLong("timestampMillis", System.currentTimeMillis())
                        )
                    )
                }
                val sessionId = obj.optString("id").ifBlank { UUID.randomUUID().toString() }
                list.add(
                    TufiChatSession(
                        id = sessionId,
                        title = obj.optString("title", "Sohbet").ifBlank { "Sohbet" },
                        createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                        updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis()),
                        messages = msgs
                    )
                )
            }
            list.distinctBy { it.id }.sortedByDescending { it.updatedAtMillis }
        } catch (e: Exception) {
            Log.e(TAG, "Chat sessions parse error", e)
            emptyList()
        }
    }

    fun saveSession(context: Context, session: TufiChatSession) {
        val currentSessions = loadAllSessions(context).toMutableList()
        val index = currentSessions.indexOfFirst { it.id == session.id }
        if (index >= 0) {
            currentSessions[index] = session
        } else {
            currentSessions.add(0, session)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        currentSessions.take(40).forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("createdAtMillis", s.createdAtMillis)
                put("updatedAtMillis", s.updatedAtMillis)
                val msgsArray = JSONArray()
                s.messages.takeLast(60).forEach { m ->
                    msgsArray.put(JSONObject().apply {
                        put("id", m.id)
                        put("isUser", m.isUser)
                        put("text", m.text)
                        put("timestampMillis", m.timestampMillis)
                    })
                }
                put("messages", msgsArray)
            }
            array.put(sObj)
        }
        prefs.edit().putString(PREF_KEY_CHAT_SESSIONS, array.toString()).apply()
    }

    fun deleteSession(context: Context, sessionId: String) {
        val currentSessions = loadAllSessions(context).filterNot { it.id == sessionId }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val array = JSONArray()
        currentSessions.forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("createdAtMillis", s.createdAtMillis)
                put("updatedAtMillis", s.updatedAtMillis)
                val msgsArray = JSONArray()
                s.messages.forEach { m ->
                    msgsArray.put(JSONObject().apply {
                        put("id", m.id)
                        put("isUser", m.isUser)
                        put("text", m.text)
                        put("timestampMillis", m.timestampMillis)
                    })
                }
                put("messages", msgsArray)
            }
            array.put(sObj)
        }
        prefs.edit().putString(PREF_KEY_CHAT_SESSIONS, array.toString()).apply()

        if (getActiveSessionId(context) == sessionId) {
            clearChatHistory(context)
        }
    }

    fun clearAllSessions(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PREF_KEY_CHAT_SESSIONS).apply()
        clearChatHistory(context)
    }

    fun getActiveSessionId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(PREF_KEY_ACTIVE_SESSION_ID, null)
    }

    fun setActiveSessionId(context: Context, sessionId: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (sessionId == null) {
            prefs.edit().remove(PREF_KEY_ACTIVE_SESSION_ID).apply()
        } else {
            prefs.edit().putString(PREF_KEY_ACTIVE_SESSION_ID, sessionId).apply()
        }
    }

    // ==========================================
    // VOICE TRANSCRIPTION CLEANUP (Sesli Dikte Düzeltme)
    // ==========================================

    /**
     * Whisper ile çıkarılan ham metni Tufi AI (DeepSeek / Gemini) üzerinden
     * sadece noktalama, büyük harf ve dolgu kelimeleri ("ııı", "şey", "yani", "ee" vb.)
     * temizleyerek düzenler. Anlam kesinlikle değiştirilmez.
     * Başarısız olursa veya anahtar yoksa ham metni döner.
     */
    suspend fun cleanTranscriptionWithAI(context: Context, rawText: String): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank() || rawText.isBlank()) return@withContext rawText

        val systemPrompt = "Sen bir sesli dikte düzeltme asistanısın. Kullanıcının konuştuğu Türkçe ham ses kaydı dökümünü düzenle. Kurallar:\n1. Anlamı ve söylenen kelimeleri kesinlikle değiştirme veya yeni bilgi ekleme.\n2. Sadece Türkçe imla kurallarına göre noktalama işaretlerini (. , ? !) ve büyük-küçük harfleri düzelt.\n3. Gereksiz konuşma dolgularını ('ııı', 'ımm', 'şey', 'yani', 'ee') temizle.\n4. Çıktı olarak SADECE ve YALNIZCA düzeltilmiş metni yaz. Hiçbir selamlama, tırnak işareti, açıklama veya ek not yazma."

        try {
            val isGemini = apiKey.startsWith("AIzaSy")
            if (isGemini) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val json = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", "$systemPrompt\n\nHam Metin: $rawText") })
                            })
                        })
                    })
                }
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: ""
                        val root = JSONObject(respBody)
                        val text = root.optJSONArray("candidates")
                            ?.optJSONObject(0)
                            ?.optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.optJSONObject(0)
                            ?.optString("text", "")
                            ?.trim()
                        if (!text.isNullOrBlank()) return@withContext text
                    }
                }
            } else {
                val url = "https://api.deepseek.com/chat/completions"
                val json = JSONObject().apply {
                    put("model", "deepseek-chat")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "system")
                            put("content", systemPrompt)
                        })
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", rawText)
                        })
                    })
                    put("temperature", 0.1)
                }
                val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder().url(url).post(body).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: ""
                        val root = JSONObject(respBody)
                        val text = root.optJSONArray("choices")
                            ?.optJSONObject(0)
                            ?.optJSONObject("message")
                            ?.optString("content", "")
                            ?.trim()
                        if (!text.isNullOrBlank()) return@withContext text
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Yapay zeka ses temizleme hatası: ${e.message}")
        }
        rawText
    }

    // ==========================================
    // QUESTION ASKING (Soru Sorma & Sohbet)
    // ==========================================

    suspend fun askTufiAI(
        context: Context,
        question: String,
        history: List<TufiChatMessage>,
        courses: List<Course>,
        homeworks: List<HomeworkWithCourse>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey(context)
        if (apiKey.isBlank()) {
            val local = generateLocalChatAnswer(question, courses, homeworks)
            return@withContext """
                ⚠️ **Tufi AI API Anahtarı Tanımlanmadı**

                GitHub'dan indirilen projelerde gizli API anahtarları yer almaz.
                Sağ üstteki **Anahtar 🔑** simgesine dokunarak kendi **DeepSeek** (`sk-...`) veya **Google Gemini** (`AIzaSy...`) API anahtarınızı tanımlayabilirsiniz.

                ---
                🤖 **Tufi AI (Çevrimdışı Mod):**
                $local
            """.trimIndent()
        }

        try {
            val isGemini = apiKey.startsWith("AIzaSy")
            val apiResponse = if (isGemini) {
                callGeminiChatApi(apiKey, question, history, courses, homeworks)
            } else {
                callDeepSeekChatApi(apiKey, question, history, homeworks)
            }

            if (!apiResponse.isNullOrBlank()) {
                return@withContext apiResponse
            }
        } catch (e: Exception) {
            Log.e(TAG, "AI chat API call failed", e)
            val local = generateLocalChatAnswer(question, courses, homeworks)
            val errorMsg = e.localizedMessage ?: "Bağlantı hatası"
            return@withContext """
                ⚠️ **Canlı Yapay Zeka Yanıtı Alınamadı**
                *(Hata: $errorMsg)*
                
                Sağ üstteki **Anahtar 🔑** butonundan API anahtarınızı ve bakiye durumunuzu kontrol edebilirsiniz.

                ---
                🤖 **Tufi AI (Çevrimdışı Mod):**
                $local
            """.trimIndent()
        }

        generateLocalChatAnswer(question, courses, homeworks)
    }

    private fun callDeepSeekChatApi(
        apiKey: String,
        question: String,
        history: List<TufiChatMessage>,
        homeworks: List<HomeworkWithCourse>
    ): String? {
        val contextInfo = buildHomeworkContext(homeworks)

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", """
                    Sen Tufi AI'sın. TufiDESK ödev ve çalışma takip uygulamasında öğrencinin samimi, pratik, doğrudan ve motive edici yapay zeka koçusun.
                    ChatGPT, Gemini ve Claude gibi doğrudan, sade, gereksiz laf kalabalığı yapmadan, net ve uygulanabilir yanıtlar ver.
                    $contextInfo
                    Cevaplarını Türkçe ver. Her zaman anlaşılır ve düzenli Markdown formatında yaz (başlıklar için ## veya ###, önemli yerler için **kalın**, maddeler için • veya -, kodlar için ``` kod blokları kullan).
                """.trimIndent())
            })

            // Geçmiş son 6 mesajı bağlam olarak ekle
            history.takeLast(6).forEach { msg ->
                put(JSONObject().apply {
                    put("role", if (msg.isUser) "user" else "assistant")
                    put("content", msg.text)
                })
            }

            put(JSONObject().apply {
                put("role", "user")
                put("content", question)
            })
        }

        val requestJson = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", messagesArray)
            put("temperature", 0.6)
            put("stream", false)
        }

        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://api.deepseek.com/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: ""
                Log.e(TAG, "DeepSeek chat error (${response.code}): $err")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            val respJson = JSONObject(responseBody)
            val choices = respJson.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val message = choices.getJSONObject(0).optJSONObject("message") ?: return null
            return message.optString("content", "").trim()
        }
    }

    private fun callGeminiChatApi(
        apiKey: String,
        question: String,
        history: List<TufiChatMessage>,
        courses: List<Course>,
        homeworks: List<HomeworkWithCourse>
    ): String? {
        val contextInfo = buildHomeworkContext(homeworks)

        val systemInstructionText = """
            Sen Tufi AI'sın. TufiDESK ödev ve çalışma takip uygulamasında öğrencinin samimi, pratik, doğrudan ve motive edici yapay zeka koçusun.
            Doğrudan, sade, gereksiz laf kalabalığı yapmadan, net ve uygulanabilir yanıtlar ver.
            $contextInfo
            Cevaplarını Türkçe ver. Her zaman anlaşılır ve düzenli Markdown formatında yaz (başlıklar için ## veya ###, önemli yerler için **kalın**, maddeler için • veya -, kod ve formüller için ``` kod blokları kullan).
        """.trimIndent()

        val contentsArray = JSONArray()
        history.takeLast(6).forEach { msg ->
            contentsArray.put(JSONObject().apply {
                put("role", if (msg.isUser) "user" else "model")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", msg.text) })
                })
            })
        }
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", question) })
            })
        })

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                })
            })
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.6)
            })
        }

        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val err = response.body?.string() ?: ""
                Log.e(TAG, "Gemini chat error (${response.code}): $err")
                return null
            }
            val responseBody = response.body?.string() ?: return null
            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return null
            val parts = contentObj.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            return parts.getJSONObject(0).optString("text", "").trim()
        }
    }

    private fun buildHomeworkContext(homeworks: List<HomeworkWithCourse>): String {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfToday = cal.timeInMillis

        val pending = homeworks.filter { !it.homework.isCompleted }
        val dueToday = pending.filter { it.homework.dueDateMillis in startOfToday..endOfToday }
        val overdue = pending.filter { it.homework.dueDateMillis in 1 until startOfToday }

        return buildString {
            append("Öğrencinin anlık durumu: ")
            append("Bugün teslim edilecek ${dueToday.size} ödev var")
            if (dueToday.isNotEmpty()) {
                append(" (${dueToday.joinToString { "${it.course?.name ?: "Ders"}: ${it.homework.title}" }})")
            }
            append(", geciken/eksik ${overdue.size} ödev var")
            if (overdue.isNotEmpty()) {
                append(" (${overdue.take(2).joinToString { "${it.course?.name ?: "Ders"}: ${it.homework.title}" }})")
            }
            append(", toplam bekleyen: ${pending.size}.")
        }
    }

    fun generateLocalChatAnswer(
        question: String,
        courses: List<Course>,
        homeworks: List<HomeworkWithCourse>
    ): String {
        val q = question.lowercase(Locale.forLanguageTag("tr-TR")).trim()

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfToday = cal.timeInMillis

        val pending = homeworks.filter { !it.homework.isCompleted }
        val dueToday = pending.filter { it.homework.dueDateMillis in startOfToday..endOfToday }
        val overdue = pending.filter { it.homework.dueDateMillis in 1 until startOfToday }

        return when {
            q.contains("bugün") || q.contains("nereden başla") || q.contains("hangisinden") || q.contains("plan") -> {
                if (dueToday.isNotEmpty()) {
                    val titles = dueToday.joinToString("\n") { "• ${it.course?.name ?: "Ders"}: ${it.homework.title}" }
                    "Bugün teslim edilecek ${dueToday.size} ödevin var:\n$titles\n\n🎯 **Stratejik Önerim:**\n1. En zor veya yüksek öncelikli ödevle başla (zihnin tazeyken).\n2. İlk 35 dakika masanda telefon olmadan odaklan.\n3. 10 dk mola verip diğer ödevlerini sırayla tamamla. Başarılar! 🚀"
                } else if (overdue.isNotEmpty()) {
                    val titles = overdue.take(2).joinToString("\n") { "• ${it.course?.name ?: "Ders"}: ${it.homework.title}" }
                    "Bugün doğrudan yeni bir teslim ödevin yok! Bu büyük bir fırsat.\n\n⚠️ Bekleyen ${overdue.size} eksik ödevin var:\n$titles\n\n💡 Bugün 25 dakikalık bir odak seansı ayırıp bunlardan birini kapatırsan haftayı çok rahat geçirirsin."
                } else {
                    "Bugün için acil bir ödevin bulunmuyor! Harika bir durumdasın. 🌟\n\nGelecek haftanın ders konularına 20 dakika göz gezdirebilir veya deneme analizi yapabilirsin."
                }
            }
            q.contains("eksik") || q.contains("gecik") || q.contains("yetiş") || q.contains("kaldı") -> {
                if (overdue.isNotEmpty()) {
                    "Şu an geciken veya eksik kalan ${overdue.size} ödevin var.\n\n📌 **Eritme Taktiği:**\n• Hepsini aynı günde bitirmeye çalışıp gözünde büyütme.\n• Günde sadece 1 eksik ödevi tamamla (yaklaşık 20-30 dk).\n• 2-3 gün içinde tüm eksiklerin tamamen temizlenmiş olacak!"
                } else {
                    "Harika haber! Sistemde hiç geciken veya eksik ödevin yok. Düzenli temponu tebrik ederim! 👏"
                }
            }
            q.contains("pomodoro") || q.contains("süre") || q.contains("mola") || q.contains("zaman") -> {
                "⏱️ **Derece Öğrencisi Pomodoro Tekniği:**\n\n1. **25 Dakika Odak:** Sadece tek bir derse ve tek bir soruya odaklan. Bildirimler ve telefon tamamen kapalı olsun.\n2. **5 Dakika Mola:** Ekrana bakma, ayağa kalkıp su iç ve esne.\n3. **4 Tur Sonrası:** 20-25 dakikalık uzun mola ver.\n\n*İpucu: Sayaç sekmesinden Pomodoro'yu hemen başlatabilirsin!*"
            }
            q.contains("mat") || q.contains("geometri") -> {
                "📐 **Matematik & Geometri Çalışma Rotası:**\n\n• **Formül Ezberleme:** Bir formülü ezberlemek yerine nereden geldiğini kağıda 1 kez ispatla.\n• **MEB Kazanım Testleri:** Yeni nesil sorulara geçmeden önce MEB kazanımlarını eksiksiz temizle.\n• **Analiz Defteri:** Çözemediğin her soruyu kes veya not al. 3 gün sonra temiz bir kağıda yeniden çözmeye çalış."
            }
            q.contains("motivasyon") || q.contains("yoruldum") || q.contains("bıktım") || q.contains("stres") || q.contains("kaygı") -> {
                "🌱 **Unutma:**\nBüyük başarılar bir gecede değil; her gün zorlandığın anlarda masadan kalkmayıp attığın o küçük adımlarla inşa edilir.\n\nBugün mükemmel olmak zorunda değilsin, sadece dünden 1 adım ileride olman yeterli. Derin bir nefes al ve sadece sıradaki 1 soruya odaklan. Yanındayım! 💪"
            }
            q.contains("deneme") || q.contains("net") || q.contains("sınav") -> {
                "📊 **Net Artırma Taktikleri:**\n\n1. **Turlama Tekniği:** Denemede takıldığın soruyla 1 dakikadan fazla inatlaşma, yanına işaret koyup turu bitirince dön.\n2. **Yanlış Analizi:** Deneme biter bitmez yanlış ve boş bıraktığın soruların video çözümünü izle.\n3. **Hata Defteri:** Yanlış yaptığın konuları TufiDESK Notlar sekmesine ekleyip haftalık tekrar listene al."
            }
            else -> {
                "Sorduğun soru için çalışma koçluğu önerim:\n\n• **Net Hedef:** Bu çalışmayı küçük parçalara böl (örn. 20 soru veya 30 dakika).\n• **Tek Odak:** Masanda yalnızca çalıştığın kaynağı bulundur.\n• **Öz Değerlendirme:** Çalışma bittiğinde tamamlananları TufiDESK'te işaretleyerek ilerlemeni somutlaştır.\n\nBaşka bir konuda veya ödevlerinde detaylı plan istersen buradayım! 🚀"
            }
        }
    }

    // ==========================================
    // DAILY COACHING ADVICE (Günlük Ödev Özeti)
    // ==========================================

    suspend fun getCoachingAdvice(
        context: Context,
        courses: List<Course> = emptyList(),
        homeworks: List<HomeworkWithCourse> = emptyList(),
        forceRefresh: Boolean = false
    ): TufiAICoachAdvice = withContext(Dispatchers.IO) {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val hwSignature = homeworks.map { "${it.homework.id}_${it.homework.isCompleted}_${it.homework.dueDateMillis}" }.sorted().joinToString(";")
        val currentSignature = "$todayKey|$hwSignature"

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = prefs.getString("$PREF_KEY_ADVICE_PREFIX$todayKey", null)
            ?: prefs.getString(PREF_KEY_LAST_SAVED_MESSAGE, null)
        val cachedSig = prefs.getString("$PREF_KEY_SIG_PREFIX$todayKey", null)
            ?: prefs.getString(PREF_KEY_LAST_SAVED_SIG, null)

        // 1. Önbellek kontrolü (Token Tasarrufu)
        if (!forceRefresh && !cachedJson.isNullOrBlank()) {
            try {
                if (cachedSig == currentSignature || cachedJson.isNotBlank()) {
                    val parsed = parseAdviceFromJson(JSONObject(cachedJson))
                    if (parsed != null) {
                        Log.d(TAG, "Token Tasarrufu: Kayıtlı Tufi AI mesajı kullanıldı.")
                        return@withContext parsed
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cache parse error", e)
            }
        }

        // 2. API Çağrısı (API anahtarı varsa)
        val apiKey = getApiKey(context)
        if (apiKey.isNotBlank()) {
            try {
                val isGemini = apiKey.startsWith("AIzaSy")
                val apiAdvice = if (isGemini) {
                    callGeminiAdviceApi(apiKey, homeworks)
                } else {
                    callDeepSeekAdviceApi(apiKey, homeworks)
                }

                if (apiAdvice != null) {
                    try {
                        val serialized = serializeAdviceToJson(apiAdvice).toString()
                        prefs.edit()
                            .putString("$PREF_KEY_ADVICE_PREFIX$todayKey", serialized)
                            .putString("$PREF_KEY_SIG_PREFIX$todayKey", currentSignature)
                            .putString(PREF_KEY_LAST_SAVED_MESSAGE, serialized)
                            .putString(PREF_KEY_LAST_SAVED_SIG, currentSignature)
                            .apply()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to cache advice", e)
                    }
                    return@withContext apiAdvice
                }
            } catch (e: Exception) {
                Log.e(TAG, "AI Advice API call failed, falling back to built-in guidance", e)
            }
        }

        // 3. Fallback: Çevrimdışı ve yerel akıllı ödev analizi
        if (!cachedJson.isNullOrBlank()) {
            try {
                val parsed = parseAdviceFromJson(JSONObject(cachedJson))
                if (parsed != null) return@withContext parsed
            } catch (_: Exception) {}
        }

        generateBuiltInAdvice(homeworks)
    }

    private fun callDeepSeekAdviceApi(
        apiKey: String,
        homeworks: List<HomeworkWithCourse>
    ): TufiAICoachAdvice? {
        val prompt = buildAdvicePrompt(homeworks)

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", """
                    Sen Tufi AI'sın. Öğrencinin doğrudan bugünkü ödevlerine, eksik kalanlarına, haftalık temposuna ve bunları nasıl ilerletmesi gerektiğine odaklanan pratik, net ve samimi bir çalışma koçusun.
                    
                    Görevin:
                    1. Bugünkü ödevler hakkında net durum ve öncelik tespiti yapmak.
                    2. Eksik veya bekleyen ödevler varsa bunların nasıl toparlanacağını söylemek.
                    3. Bu haftanın genel yoğunluğu ve haftayı nasıl planlayacağı hakkında bilgi vermek.
                    4. Bugünkü ödevleri nasıl ilerletmesi gerektiğine dair 3 somut adım ve 1 verimli çalışma taktiği (Pomodoro vb.) sunmak.
                    
                    LÜTFEN SADECE VE DOĞRUDAN AŞAĞIDAKİ JSON FORMATINDA CEVAP VER. Markdown etiketi koyma:
                    {
                      "headline": "Kısa ve motive edici başlık (örn: Bugünün Rotası: 2 Kritik Ödev)",
                      "summary": "1-2 cümlelik genel değerlendirme",
                      "todayTasksInsight": "Bugünkü ödevler hakkında net bilgi ve öneri",
                      "missingTasksInsight": "Eksik / bekleyen ödevlerin durumu ve toparlama tüyosu",
                      "weeklyInsight": "Bu hafta hakkında veri, tempo ve plan önerisi",
                      "stepByStepPlan": [
                        "1. Adım: ...",
                        "2. Adım: ...",
                        "3. Adım: ..."
                      ],
                      "studyMethodTip": "Bugünkü çalışmayı hızlandıracak pratik bir taktik (örn. Pomodoro süresi veya mola kuralı)"
                    }
                """.trimIndent())
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            })
        }

        val requestJson = JSONObject().apply {
            put("model", "deepseek-chat")
            put("messages", messagesArray)
            put("temperature", 0.5)
            put("stream", false)
        }

        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("https://api.deepseek.com/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseBody = response.body?.string() ?: return null
            val respJson = JSONObject(responseBody)
            val choices = respJson.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val message = choices.getJSONObject(0).optJSONObject("message") ?: return null
            val content = message.optString("content", "").trim()

            val cleanJsonString = when {
                content.startsWith("```json") -> content.removePrefix("```json").substringBeforeLast("```").trim()
                content.startsWith("```") -> content.removePrefix("```").substringBeforeLast("```").trim()
                else -> content
            }

            val adviceJson = JSONObject(cleanJsonString)
            return parseAdviceFromJson(adviceJson, isFromApi = true)
        }
    }

    private fun callGeminiAdviceApi(
        apiKey: String,
        homeworks: List<HomeworkWithCourse>
    ): TufiAICoachAdvice? {
        val prompt = buildAdvicePrompt(homeworks)

        val systemInstructionText = """
            Sen Tufi AI'sın. Öğrencinin doğrudan bugünkü ödevlerine, eksik kalanlarına ve bunları nasıl ilerletmesi gerektiğine odaklanan pratik, net ve samimi bir çalışma koçusun.
            LÜTFEN SADECE VE DOĞRUDAN JSON FORMATINDA CEVAP VER. Markdown etiketi ekleme:
            {
              "headline": "Kısa ve motive edici başlık",
              "summary": "1-2 cümlelik genel değerlendirme",
              "todayTasksInsight": "Bugünkü ödevler hakkında net bilgi ve öneri",
              "missingTasksInsight": "Eksik / bekleyen ödevlerin durumu ve toparlama tüyosu",
              "weeklyInsight": "Bu hafta hakkında veri, tempo ve plan önerisi",
              "stepByStepPlan": [
                "1. Adım: ...",
                "2. Adım: ...",
                "3. Adım: ..."
              ],
              "studyMethodTip": "Bugünkü çalışmayı hızlandıracak pratik bir taktik"
            }
        """.trimIndent()

        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", prompt) })
                })
            })
        }

        val requestJson = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemInstructionText) })
                })
            })
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.4)
                put("responseMimeType", "application/json")
            })
        }

        val body = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val responseBody = response.body?.string() ?: return null
            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val contentObj = candidates.getJSONObject(0).optJSONObject("content") ?: return null
            val parts = contentObj.optJSONArray("parts") ?: return null
            if (parts.length() == 0) return null
            val content = parts.getJSONObject(0).optString("text", "").trim()

            val cleanJsonString = when {
                content.startsWith("```json") -> content.removePrefix("```json").substringBeforeLast("```").trim()
                content.startsWith("```") -> content.removePrefix("```").substringBeforeLast("```").trim()
                else -> content
            }

            val adviceJson = JSONObject(cleanJsonString)
            return parseAdviceFromJson(adviceJson, isFromApi = true)
        }
    }

    private fun buildAdvicePrompt(homeworks: List<HomeworkWithCourse>): String {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfToday = cal.timeInMillis
        val endOfWeek = startOfToday + 7 * 86400000L

        val pending = homeworks.filter { !it.homework.isCompleted }
        val completed = homeworks.filter { it.homework.isCompleted }
        val dueToday = pending.filter { it.homework.dueDateMillis in startOfToday..endOfToday }
        val overdue = pending.filter { it.homework.dueDateMillis in 1 until startOfToday }
        val dueThisWeek = pending.filter { it.homework.dueDateMillis in (endOfToday + 1)..endOfWeek }

        return buildString {
            appendLine("Öğrencinin Mevcut Ödev Durumu:")
            appendLine("- Bugün teslim edilecek ödev sayısı: ${dueToday.size}")
            if (dueToday.isNotEmpty()) {
                appendLine("  Bugünküler: ${dueToday.joinToString { "${it.course?.name ?: "Ders"}: ${it.homework.title}" }}")
            }
            appendLine("- Gecikmiş / eksik ödev sayısı: ${overdue.size}")
            if (overdue.isNotEmpty()) {
                appendLine("  Eksikler: ${overdue.joinToString { "${it.course?.name ?: "Ders"}: ${it.homework.title}" }}")
            }
            appendLine("- Bu hafta içinde teslim edilecek diğer ödevler: ${dueThisWeek.size}")
            appendLine("- Toplam ödev: ${homeworks.size}, Tamamlanan: ${completed.size}, Bekleyen: ${pending.size}")
        }
    }

    private fun parseAdviceFromJson(json: JSONObject, isFromApi: Boolean = true): TufiAICoachAdvice? {
        try {
            val headline = json.optString("headline", "Bugünün Çalışma Rotası")
            val summary = json.optString("summary", "Ödevlerini düzenli tamamlayarak gününü verimli kapat.")
            val todayTasksInsight = json.optString("todayTasksInsight", "Bugünkü ödevlerini öncelik sırasına göre tamamlamaya başla.")
            val missingTasksInsight = json.optString("missingTasksInsight", "Eksik ödevlerini biriktirmeden kısa bloklarla eritebilirsin.")
            val weeklyInsight = json.optString("weeklyInsight", "Bu haftanın temposunu koruyarak ilerle.")
            val studyMethodTip = json.optString("studyMethodTip", "25 dakika çalışma, 5 dakika mola kuralını (Pomodoro) uygula.")

            val steps = mutableListOf<String>()
            val stepsArray = json.optJSONArray("stepByStepPlan")
            if (stepsArray != null) {
                for (i in 0 until stepsArray.length()) {
                    steps.add(stepsArray.getString(i))
                }
            }
            if (steps.isEmpty()) {
                steps.add("1. Adım: Teslim tarihi en yakın ödevine 30 dakika odaklan.")
                steps.add("2. Adım: Kısa bir mola verip bekleyen soruları çöz.")
                steps.add("3. Adım: Tamamladığın ödevleri işaretleyip gününü tamamla.")
            }

            return TufiAICoachAdvice(
                headline = headline,
                summary = summary,
                todayTasksInsight = todayTasksInsight,
                missingTasksInsight = missingTasksInsight,
                weeklyInsight = weeklyInsight,
                stepByStepPlan = steps,
                studyMethodTip = studyMethodTip,
                timestampMillis = System.currentTimeMillis(),
                isFromApi = isFromApi
            )
        } catch (e: Exception) {
            Log.e(TAG, "Advice parse error", e)
            return null
        }
    }

    private fun serializeAdviceToJson(advice: TufiAICoachAdvice): JSONObject {
        return JSONObject().apply {
            put("headline", advice.headline)
            put("summary", advice.summary)
            put("todayTasksInsight", advice.todayTasksInsight)
            put("missingTasksInsight", advice.missingTasksInsight)
            put("weeklyInsight", advice.weeklyInsight)
            put("studyMethodTip", advice.studyMethodTip)
            put("isFromApi", advice.isFromApi)
            put("timestampMillis", advice.timestampMillis)
            val stepsArr = JSONArray()
            advice.stepByStepPlan.forEach { stepsArr.put(it) }
            put("stepByStepPlan", stepsArr)
        }
    }

    fun generateBuiltInAdvice(homeworks: List<HomeworkWithCourse>): TufiAICoachAdvice {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endOfToday = cal.timeInMillis

        val pending = homeworks.filter { !it.homework.isCompleted }
        val dueToday = pending.filter { it.homework.dueDateMillis in startOfToday..endOfToday }
        val overdue = pending.filter { it.homework.dueDateMillis in 1 until startOfToday }

        val headline: String
        val summary: String
        val todayInsight: String
        val missingInsight: String
        val weeklyInsight: String
        val steps = mutableListOf<String>()
        val tip: String

        if (dueToday.isNotEmpty()) {
            headline = "Bugünün Rotası: ${dueToday.size} Kritik Ödev"
            summary = "Günün önceliği teslim tarihi bugün dolan ödevlerini temizleyerek kafanı rahatlatmak."
            todayInsight = "Bugün ${dueToday.size} ödev teslim edilecek: ${dueToday.joinToString { "${it.course?.name ?: "Ders"}: ${it.homework.title}" }}."
            steps.add("1. Adım: Teslim tarihi bugün olan ilk ödevine 35 dakika tam odaklan.")
            steps.add("2. Adım: 10 dakikalık mola verip ikinci ödevin sorularını tamamla.")
            steps.add("3. Adım: Tamamlanan ödevleri TufiDESK'te yeşil tikle işaretle.")
        } else if (overdue.isNotEmpty()) {
            headline = "Fırsat Günü: Eksikleri Toparlama Zamanı"
            summary = "Bugün yeni teslimin yok! Bekleyen eksik ödevlerini eritmek için ideal bir fırsat penceresi."
            todayInsight = "Bugün teslim edilecek acil ödevin bulunmuyor. Temponu korumak için mükemmel bir gün."
            steps.add("1. Adım: Geciken ödevlerden en kısa olanını seçip 25 dakikada bitir.")
            steps.add("2. Adım: Kalan ödevler için yarın ve sonraki güne 1'er görev planla.")
            steps.add("3. Adım: 15 dakika kitap oku veya zihnini dinlendir.")
        } else {
            headline = "Mükemmel İlerleme: Tüm Ödevler Kontrol Altında! 🌟"
            summary = "Harika bir disiplin! Masanda acil veya geciken ödev baskısı olmadan rahatça ilerliyorsun."
            todayInsight = "Bekleyen acil teslimin yok. Tüm süreç düzenli ilerliyor."
            steps.add("1. Adım: Gelecek haftanın ders konularına 20 dakika göz gezdir.")
            steps.add("2. Adım: TufiDESK Denemeler sekmesinden son denemenin net analizini incele.")
            steps.add("3. Adım: Kendine kaliteli bir dinlenme vakti ayır.")
        }

        if (overdue.isNotEmpty()) {
            missingInsight = "Sistemde ${overdue.size} adet eksik/gecikmiş ödev bekliyor. Bunları tek seferde bitirmeye çalışma; günde sadece 1 tanesini kapat."
        } else {
            missingInsight = "Tebrikler! Hiçbir eksik veya gecikmiş ödevin bulunmuyor."
        }

        weeklyInsight = "Haftalık planını sürdürürken günde ortalama 2 Pomodoro bloğu (50 dk) ayırmak tüm ödev yükünü rahatça eritmeni sağlar."
        tip = "💡 Çalışırken telefonunu başka bir odaya bırak. Sadece 25 dakika tek bir derse odaklandığında hızının 2 katına çıktığını göreceksin."

        return TufiAICoachAdvice(
            headline = headline,
            summary = summary,
            todayTasksInsight = todayInsight,
            missingTasksInsight = missingInsight,
            weeklyInsight = weeklyInsight,
            stepByStepPlan = steps,
            studyMethodTip = tip,
            timestampMillis = System.currentTimeMillis(),
            isFromApi = false
        )
    }
}
