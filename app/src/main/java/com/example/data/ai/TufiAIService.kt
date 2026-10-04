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

object TufiAIService {
    private const val TAG = "TufiAIService"
    private const val PREFS_NAME = "tufiai_cache_prefs"
    private const val PREF_KEY_ADVICE_PREFIX = "advice_v2_"
    private const val PREF_KEY_SIG_PREFIX = "sig_v2_"
    private const val PREF_KEY_CUSTOM_API_KEY = "custom_deepseek_api_key"
    private const val PREF_KEY_LAST_SAVED_MESSAGE = "last_saved_advice_v2"
    private const val PREF_KEY_LAST_SAVED_SIG = "last_saved_sig_v2"
    private const val PREF_KEY_CHAT_HISTORY = "tufiai_chat_history_v1"

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

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

    fun saveCustomApiKey(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(PREF_KEY_CUSTOM_API_KEY, key.trim()).apply()
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
            emptyList()
        }
    }

    fun saveChatHistory(context: Context, messages: List<TufiChatMessage>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val array = JSONArray()
            // Bellek ve performans için son 40 mesaj saklanır
            messages.takeLast(40).forEach { msg ->
                val obj = JSONObject().apply {
                    put("id", msg.id)
                    put("isUser", msg.isUser)
                    put("text", msg.text)
                    put("timestampMillis", msg.timestampMillis)
                }
                array.put(obj)
            }
            prefs.edit().putString(PREF_KEY_CHAT_HISTORY, array.toString()).apply()
        } catch (_: Exception) {}
    }

    fun clearChatHistory(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(PREF_KEY_CHAT_HISTORY).apply()
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
        if (apiKey.isNotBlank()) {
            try {
                val apiResponse = callDeepSeekChatApi(apiKey, question, history, homeworks)
                if (!apiResponse.isNullOrBlank()) {
                    return@withContext apiResponse
                }
            } catch (e: Exception) {
                Log.e(TAG, "DeepSeek chat API call failed, using built-in coach", e)
            }
        }
        generateLocalChatAnswer(question, courses, homeworks)
    }

    private fun callDeepSeekChatApi(
        apiKey: String,
        question: String,
        history: List<TufiChatMessage>,
        homeworks: List<HomeworkWithCourse>
    ): String? {
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

        val contextInfo = buildString {
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

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", """
                    Sen Tufi AI'sın. TufiDESK ödev ve çalışma takip uygulamasında öğrencinin samimi, pratik, doğrudan ve motive edici yapay zeka koçusun.
                    ChatGPT, Gemini ve Claude gibi doğrudan, sade, gereksiz laf kalabalığı yapmadan, net ve uygulanabilir yanıtlar ver.
                    $contextInfo
                    Cevaplarını Türkçe ver, önemli noktaları madde işaretleriyle listele.
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
            if (!response.isSuccessful) return null
            val responseBody = response.body?.string() ?: return null
            val respJson = JSONObject(responseBody)
            val choices = respJson.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null
            val message = choices.getJSONObject(0).optJSONObject("message") ?: return null
            return message.optString("content", "").trim()
        }
    }

    fun generateLocalChatAnswer(
        question: String,
        courses: List<Course>,
        homeworks: List<HomeworkWithCourse>
    ): String {
        val q = question.lowercase(Locale("tr")).trim()

        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = now }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
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
                    "Şu an geciken veya eksik kalan ${overdue.size} ödevin var.\n\n📌 **Eritme Taktigi:**\n• Hepsini aynı günde bitirmeye çalışıp gözünde büyütme.\n• Günde sadece 1 eksik ödevi tamamla (yaklaşık 20-30 dk).\n• 2-3 gün içinde tüm eksiklerin tamamen temizlenmiş olacak!"
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
                val apiAdvice = callDeepSeekAdviceApi(apiKey, homeworks)
                if (apiAdvice != null) {
                    try {
                        val serialized = serializeAdviceToJson(apiAdvice).toString()
                        prefs.edit()
                            .putString("$PREF_KEY_ADVICE_PREFIX$todayKey", serialized)
                            .putString("$PREF_KEY_SIG_PREFIX$todayKey", currentSignature)
                            .putString(PREF_KEY_LAST_SAVED_MESSAGE, serialized)
                            .putString(PREF_KEY_LAST_SAVED_SIG, currentSignature)
                            .apply()

                        try {
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            val docData = hashMapOf(
                                "todayKey" to todayKey,
                                "signature" to currentSignature,
                                "adviceJson" to serialized,
                                "updatedAt" to System.currentTimeMillis()
                            )
                            db.collection("tufi_ai_cache").document(todayKey).set(docData)
                        } catch (_: Exception) {}
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to cache advice", e)
                    }
                    return@withContext apiAdvice
                }
            } catch (e: Exception) {
                Log.e(TAG, "DeepSeek API call failed, falling back to built-in guidance", e)
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
                    Sen Tufi AI'sın. Öğrencinin karmaşık felsefi ders açıklamalarına değil; doğrudan bugünkü ödevlerine, eksik kalanlarına, haftalık temposuna ve bunları nasıl ilerletmesi gerektiğine odaklanan pratik, net ve samimi bir çalışma koçusun.
                    
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
                isFromApi = isFromApi
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse advice json", e)
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

            val stepsArray = JSONArray()
            advice.stepByStepPlan.forEach { stepsArray.put(it) }
            put("stepByStepPlan", stepsArray)
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
        val endOfWeek = startOfToday + 7 * 86400000L

        val pending = homeworks.filter { !it.homework.isCompleted }
        val completed = homeworks.filter { it.homework.isCompleted }
        val dueToday = pending.filter { it.homework.dueDateMillis in startOfToday..endOfToday }
        val overdue = pending.filter { it.homework.dueDateMillis in 1 until startOfToday }
        val dueThisWeek = pending.filter { it.homework.dueDateMillis in (endOfToday + 1)..endOfWeek }

        val headline: String
        val summary: String

        if (dueToday.isNotEmpty()) {
            headline = "Bugünün Rotası: ${dueToday.size} Teslim Ödevi"
            summary = "Bugün teslim edilecek ${dueToday.size} ödevin var. İlk odak noktan bunlar olmalı."
        } else if (overdue.isNotEmpty()) {
            headline = "Toparlama Günü: ${overdue.size} Eksik Ödev"
            summary = "Bugün yeni teslim yok; bekleyen ${overdue.size} ödevi eritmek için harika bir fırsat."
        } else if (pending.isNotEmpty()) {
            headline = "Dengeli Tempo: ${pending.size} Bekleyen Ödev"
            summary = "Günü rahat geçirebilir, haftalık ödevlerinden birini erkenden tamamlayabilirsin."
        } else {
            headline = "Tebrikler! Tüm Ödevler Tamam"
            summary = "Şu anda bekleyen hiçbir ödevin yok. Konu tekrarı yapabilir veya dinlenebilirsin."
        }

        val todayTasksInsight = if (dueToday.isNotEmpty()) {
            val names = dueToday.take(3).joinToString { "${it.course?.name ?: "Ders"} (${it.homework.title})" }
            "Bugün teslim edilmesi gereken ${dueToday.size} ödevin var: $names. Bunları günün erken saatlerinde, zihnin tazeyken bitirmen hız kazandıracaktır."
        } else {
            "Bugün doğrudan teslim edilmesi gereken acil bir ödevin bulunmuyor. Bu zamanı yarınki derslere veya eksiklere ayırabilirsin."
        }

        val missingTasksInsight = if (overdue.isNotEmpty()) {
            val names = overdue.take(2).joinToString { "${it.course?.name ?: "Ders"}: ${it.homework.title}" }
            "Geciken veya tamamlanmamış ${overdue.size} ödevin bulunuyor ($names). Bunları ertelemek yerine bugün 20'şer dakikalık bloklarla hafifletebilirsin."
        } else if (pending.isNotEmpty()) {
            "Şu anda geciken hiçbir ödevin yok. Toplam ${pending.size} bekleyen ödevin planlanan takvimde ilerliyor."
        } else {
            "Sistemde hiç eksik ödevin yok! Zaman yönetimini harika şekilde sürdürüyorsun."
        }

        val weeklyTotal = dueToday.size + dueThisWeek.size
        val weeklyInsight = if (weeklyTotal > 0) {
            val pct = if (homeworks.isNotEmpty()) (completed.size * 100) / homeworks.size else 0
            "Bu hafta toplam $weeklyTotal ödev teslimi bulunuyor (Genel başarı: %$pct). Günde ortalama 1-2 ödev bitirerek hafta sonunu tamamen boş bırakabilirsin."
        } else {
            "Bu hafta için planlanan tüm ödevler yolunda. Önümüzdeki günlerin yükü hafif."
        }

        val steps = mutableListOf<String>()
        if (dueToday.isNotEmpty()) {
            steps.add("1. Adım: Teslim tarihi bugün olan ${dueToday.first().homework.title} ödevine 35 dk kesintisiz odaklan.")
            steps.add("2. Adım: 10 dk mola ver; ardından kalan bugünkü çalışmaları tamamla.")
            steps.add("3. Adım: Bitirdiğin ödevleri TufiDESK'te onaylayıp yarının listesini kontrol et.")
        } else if (overdue.isNotEmpty()) {
            steps.add("1. Adım: Geciken ödevlerden ${overdue.first().homework.title} ödevini 25 dk içinde bitirmeyi hedefle.")
            steps.add("2. Adım: Masanı toparlayıp 5 dk nefes al, ardından kalanları gözden geçir.")
            steps.add("3. Adım: Listeni güncelleyip günün tamamlanma oranını yükselt.")
        } else {
            steps.add("1. Adım: Gelecek haftanın önemli ders konularına 20 dk göz at.")
            steps.add("2. Adım: Eksik hissettiğin bir dersten 20 soru çözerek pratiğini koru.")
            steps.add("3. Adım: TufiDESK ödev takvimine yeni haftalık hedeflerini ekle.")
        }

        val studyMethodTip = "💡 Pomodoro Kuralı: 25 dakika çalışıp 5 dakika tam mola ver. Çalışma esnasında bildirimleri kapatmak odaklanma süreni 2 katına çıkarır."

        return TufiAICoachAdvice(
            headline = headline,
            summary = summary,
            todayTasksInsight = todayTasksInsight,
            missingTasksInsight = missingTasksInsight,
            weeklyInsight = weeklyInsight,
            stepByStepPlan = steps,
            studyMethodTip = studyMethodTip,
            isFromApi = false
        )
    }
}
