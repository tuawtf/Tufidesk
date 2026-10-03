package com.example.data.ai

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class CourseBenefitItem(
    val courseName: String,
    val benefit: String,
    val whyItMatters: String,
    val futureOrYksImpact: String
)

data class StudyRecommendationItem(
    val courseName: String,
    val whatToStudy: String,
    val howToStudy: String,
    val dereceTechnique: String
)

data class TufiAICoachAdvice(
    val headline: String,
    val summary: String,
    val courseBenefits: List<CourseBenefitItem>,
    val studyRecommendations: List<StudyRecommendationItem>,
    val dereceTactic: String,
    val motivationQuote: String,
    val timestampMillis: Long = System.currentTimeMillis(),
    val isFromApi: Boolean = true
)

object TufiAIService {
    private const val TAG = "TufiAIService"
    private const val PREFS_NAME = "tufiai_cache_prefs"
    private const val PREF_KEY_ADVICE_PREFIX = "advice_"
    private const val PREF_KEY_CUSTOM_API_KEY = "custom_deepseek_api_key"

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

    suspend fun getCoachingAdvice(
        context: Context,
        courses: List<String>,
        homeworkTitles: List<String>,
        forceRefresh: Boolean = false
    ): TufiAICoachAdvice = withContext(Dispatchers.IO) {
        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedJson = prefs.getString("$PREF_KEY_ADVICE_PREFIX$todayKey", null)

        if (!forceRefresh && !cachedJson.isNullOrBlank()) {
            try {
                val parsed = parseAdviceFromJson(JSONObject(cachedJson))
                if (parsed != null) {
                    return@withContext parsed
                }
            } catch (e: Exception) {
                Log.e(TAG, "Cache parse error", e)
            }
        }

        val apiKey = getApiKey(context)
        val effectiveCourses = if (courses.isNotEmpty()) courses else listOf("Matematik", "Türkçe", "Fizik", "Tarih")

        if (apiKey.isNotBlank()) {
            try {
                val apiAdvice = callDeepSeekApi(apiKey, effectiveCourses, homeworkTitles)
                if (apiAdvice != null) {
                    // Save to cache
                    try {
                        val serialized = serializeAdviceToJson(apiAdvice).toString()
                        prefs.edit().putString("$PREF_KEY_ADVICE_PREFIX$todayKey", serialized).apply()
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to cache advice", e)
                    }
                    return@withContext apiAdvice
                }
            } catch (e: Exception) {
                Log.e(TAG, "DeepSeek API call failed, falling back to built-in Derece intelligence", e)
            }
        }

        // Offline / Fallback Derece Intelligence
        val fallback = generateBuiltInDereceAdvice(effectiveCourses, homeworkTitles)
        fallback
    }

    private fun callDeepSeekApi(
        apiKey: String,
        courses: List<String>,
        homeworkTitles: List<String>
    ): TufiAICoachAdvice? {
        val prompt = buildPrompt(courses, homeworkTitles)

        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "system")
                put("content", """
                    Sen TufiAI'sın; YKS ve lise sınavlarında Türkiye 1.si ve ilk 100'e giren derece öğrencilerinin çalışma disiplini, turlama tekniği, MEB kazanım testleri, analiz/yanlış defteri metotları ve çalışma psikolojisini mükemmel bilen samimi, motive edici ve stratejik bir yapay zeka derece koçusun.
                    
                    Görevin: Öğrencinin bugünkü derslerini analiz ederek:
                    1. Bu derslerin bize ne faydası olduğunu, zihinsel kapasiteye, problem çözme yeteneğine ve YKS/hayat başarısına somut katkısını açıklamak.
                    2. Bugün neye çalışmamız gerektiğini (kritik konular, soru tipleri) netleştirmek.
                    3. Derece öğrencilerinin bu derslere nasıl çalıştığını (somut taktikler, süreler, soru adetleri, kaynak sıralaması) anlatmak.
                    4. Derece öğrencisi altın tavsiyesi ve motivasyon notu vermek.
                    
                    LÜTFEN SADECE AŞAĞIDAKİ GEÇERLİ JSON FORMATINDA CEVAP VER. Markdown bloğu veya fazladan metin ekleme, doğrudan JSON nesnesi döndür:
                    {
                      "headline": "kısa ve etkileyici koçluk başlığı",
                      "summary": "1-2 cümlelik bugünkü stratejik özet",
                      "courseBenefits": [
                        {
                          "courseName": "Ders Adı",
                          "benefit": "Bu dersin bize kazandırdığı temel zihinsel güç ve beceri",
                          "whyItMatters": "Neden öğrenmeliyiz ve hayattaki karşılığı nedir?",
                          "futureOrYksImpact": "YKS'de ve kariyerdeki kritik etkisi"
                        }
                      ],
                      "studyRecommendations": [
                        {
                          "courseName": "Ders Adı",
                          "whatToStudy": "Bugün tam olarak hangi konu/kazanıma odaklanılmalı?",
                          "howToStudy": "Adım adım çalışma yöntemi (konu-soru dengesi)",
                          "dereceTechnique": "Derece öğrencisi taktiği (örn: Analiz Defteri, 3 Kaynak Turlaması)"
                        }
                      ],
                      "dereceTactic": "Türkiye derece öğrencilerinin uyguladığı günün altın stratejisi",
                      "motivationQuote": "Motive edici derece sözü"
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
                val errBody = response.body?.string() ?: ""
                Log.e(TAG, "DeepSeek API error ${response.code}: $errBody")
                return null
            }

            val responseBody = response.body?.string() ?: return null
            val respJson = JSONObject(responseBody)
            val choices = respJson.optJSONArray("choices") ?: return null
            if (choices.length() == 0) return null

            val message = choices.getJSONObject(0).optJSONObject("message") ?: return null
            val content = message.optString("content", "").trim()

            // Clean markdown code blocks if wrapped in ```json ... ```
            val cleanJsonString = when {
                content.startsWith("```json") -> content.removePrefix("```json").substringBeforeLast("```").trim()
                content.startsWith("```") -> content.removePrefix("```").substringBeforeLast("```").trim()
                else -> content
            }

            val adviceJson = JSONObject(cleanJsonString)
            return parseAdviceFromJson(adviceJson, isFromApi = true)
        }
    }

    private fun buildPrompt(courses: List<String>, homeworkTitles: List<String>): String {
        val coursesStr = courses.joinToString(", ")
        val hwStr = if (homeworkTitles.isNotEmpty()) {
            "Bugün teslim veya çalışma listesindeki ödevler: ${homeworkTitles.joinToString(", ")}."
        } else {
            "Bugün özel bir ödev girilmemiş, genel ders çalışma programı."
        }

        return """
            Bugünkü derslerimiz: $coursesStr.
            $hwStr
            
            Lütfen derece öğrencilerinin çalışma disiplini ve tecrübeleriyle bu dersleri bize neden çalıştığımızı, neye ve nasıl çalışmamız gerektiğini somut taktiklerle anlat. Belirtilen JSON şablonunda cevap ver.
        """.trimIndent()
    }

    private fun parseAdviceFromJson(json: JSONObject, isFromApi: Boolean = true): TufiAICoachAdvice? {
        try {
            val headline = json.optString("headline", "Bugünün Stratejik Koçluk Planı")
            val summary = json.optString("summary", "Bugünkü derslerin zihinsel gelişimin ve sınav hedefin için büyük bir fırsat.")
            val dereceTactic = json.optString("dereceTactic", "Çözemediğin her soruyu kesip 'Analiz Defteri'ne yapıştır. 3 gün sonra tekrar çöz!")
            val motivationQuote = json.optString("motivationQuote", "“Zor sorular seni eler değil, seni dereceye taşır.”")

            val benefits = mutableListOf<CourseBenefitItem>()
            val benefitsArray = json.optJSONArray("courseBenefits")
            if (benefitsArray != null) {
                for (i in 0 until benefitsArray.length()) {
                    val item = benefitsArray.getJSONObject(i)
                    benefits.add(
                        CourseBenefitItem(
                            courseName = item.optString("courseName", "Ders"),
                            benefit = item.optString("benefit", ""),
                            whyItMatters = item.optString("whyItMatters", ""),
                            futureOrYksImpact = item.optString("futureOrYksImpact", "")
                        )
                    )
                }
            }

            val recommendations = mutableListOf<StudyRecommendationItem>()
            val recArray = json.optJSONArray("studyRecommendations")
            if (recArray != null) {
                for (i in 0 until recArray.length()) {
                    val item = recArray.getJSONObject(i)
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = item.optString("courseName", "Ders"),
                            whatToStudy = item.optString("whatToStudy", ""),
                            howToStudy = item.optString("howToStudy", ""),
                            dereceTechnique = item.optString("dereceTechnique", "")
                        )
                    )
                }
            }

            return TufiAICoachAdvice(
                headline = headline,
                summary = summary,
                courseBenefits = benefits,
                studyRecommendations = recommendations,
                dereceTactic = dereceTactic,
                motivationQuote = motivationQuote,
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
            put("dereceTactic", advice.dereceTactic)
            put("motivationQuote", advice.motivationQuote)

            val benefitsArray = JSONArray()
            advice.courseBenefits.forEach { b ->
                benefitsArray.put(JSONObject().apply {
                    put("courseName", b.courseName)
                    put("benefit", b.benefit)
                    put("whyItMatters", b.whyItMatters)
                    put("futureOrYksImpact", b.futureOrYksImpact)
                })
            }
            put("courseBenefits", benefitsArray)

            val recArray = JSONArray()
            advice.studyRecommendations.forEach { r ->
                recArray.put(JSONObject().apply {
                    put("courseName", r.courseName)
                    put("whatToStudy", r.whatToStudy)
                    put("howToStudy", r.howToStudy)
                    put("dereceTechnique", r.dereceTechnique)
                })
            }
            put("studyRecommendations", recArray)
        }
    }

    /**
     * Built-in rich intelligence collected from top Türkiye YKS champions & Derece students.
     * Ensures the user receives profound, high-value coaching even without network connectivity.
     */
    fun generateBuiltInDereceAdvice(
        courses: List<String>,
        homeworkTitles: List<String>
    ): TufiAICoachAdvice {
        val benefits = mutableListOf<CourseBenefitItem>()
        val recommendations = mutableListOf<StudyRecommendationItem>()

        val distinctCourses = courses.distinct().ifEmpty { listOf("Matematik", "Türkçe", "Fizik") }

        distinctCourses.forEach { course ->
            val norm = course.lowercase(Locale("tr"))
            when {
                norm.contains("mat") -> {
                    benefits.add(
                        CourseBenefitItem(
                            courseName = course,
                            benefit = "Soyut bağlantı kurma, yüksek analitik modelleme ve adım adım problem çözme yeteneği kazandırır.",
                            whyItMatters = "Gelecekte yazılım, yapay zeka, finans ve stratejik karar alma mekanizmalarının temel zihinsel altyapısıdır.",
                            futureOrYksImpact = "YKS TYT & AYT'de en yüksek standart sapmaya ve puan katsayısına sahip belirleyici branştır."
                        )
                    )
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = course,
                            whatToStudy = "Konu eksiği varsa temel kavram mantığını kavra, ardından doğrudan yeni nesil problemlere geç.",
                            howToStudy = "Formülleri ezberleme; ispatını bir kez kağıda yaz. 1 orta seviye soru bankasından sonra hemen deneme formatına geç.",
                            dereceTechnique = "3 Kaynak Turlaması: Kolay kaynakta oyalanma; doğrudan MEB OGM Materyal + Orta + İleri düzey kaynak tara."
                        )
                    )
                }
                norm.contains("fizik") -> {
                    benefits.add(
                        CourseBenefitItem(
                            courseName = course,
                            benefit = "Evrenin mantığını matematiksel gözle modellemeyi, sebep-sonuç ilişkisini net görmeyi sağlar.",
                            whyItMatters = "Mühendislik, teknoloji ve gerçek hayat mekaniğinde olayların 'neden' öyle çalıştığını anlamanı sağlar.",
                            futureOrYksImpact = "Öğrencilerin en çok ön yargıyla yaklaştığı, bu yüzden doğru çözenin binlerce kişiyi elediği derstir."
                        )
                    )
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = course,
                            whatToStudy = "Kuvvet, hareket, enerji veya elektrik alanındaki kavram yanılgılarını temizle.",
                            howToStudy = "Şekil çizerek çöz. Soruyu okurken verilenleri ve isteneni mutlaka kağıda şematize et.",
                            dereceTechnique = "MEB Deneyleri Analizi: Derece öğrencileri MEB fizik kitabındaki şekilli kazanım sorularını satır satır inceler."
                        )
                    )
                }
                norm.contains("kimya") -> {
                    benefits.add(
                        CourseBenefitItem(
                            courseName = course,
                            benefit = "Maddenin mikroskobik dünyası ile makroskobik sonuçları arasındaki dönüşümleri anlamayı öğretir.",
                            whyItMatters = "Tıp, eczacılık, malzeme bilimi ve enerji sektörlerinin can damarıdır.",
                            futureOrYksImpact = "AYT Sayısalda en hızlı nete dönüştürülen ve doğru çalışıldığında fullenmesi en kolay derstir."
                        )
                    )
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = course,
                            whatToStudy = "Mol kavramı, periyodik sistem veya organik/denge bağıntıları.",
                            howToStudy = "Bileşik adlandırma ve tepkime denkleştirme refleks haline gelene kadar pratik yap.",
                            dereceTechnique = "Tablo & Duvar Notları: İstisnaları (gaz yasaları, orbital sıraları) göz hizana asıp her gün 2 dakika göz gezdir."
                        )
                    )
                }
                norm.contains("biyoloji") -> {
                    benefits.add(
                        CourseBenefitItem(
                            courseName = course,
                            benefit = "Canlı sistemlerin muazzam dengesini, genetik ve hücresel mekanizmaları keşfetmeni sağlar.",
                            whyItMatters = "Biyoteknoloji, sağlık, beslenme ve çevre korumanın temelidir.",
                            futureOrYksImpact = "Bilgi doğruluğu gerektirir; yorum soruları ancak sağlam terminolojiyle eksiksiz çözülür."
                        )
                    )
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = course,
                            whatToStudy = "Hücre bölünmeleri, kalıtım veya sistemler (dolaşım, sinir).",
                            howToStudy = "Pasif okuma yapma! Boş bir kağıt alıp sistemi baştan sona kendi cümlelerinle çiz ve anlat (Feynman Tekniği).",
                            dereceTechnique = "Renkli Kavram Haritası: Derece öğrencileri MEB kitabındaki tüm şemaları kendi elleriyle yeniden çizer."
                        )
                    )
                }
                norm.contains("türkçe") || norm.contains("edebiyat") -> {
                    benefits.add(
                        CourseBenefitItem(
                            courseName = course,
                            benefit = "Hızlı anlama, derin muhakeme, odaklanma ve kelimelerin arkasındaki niyeti kavrama gücü verir.",
                            whyItMatters = "Hangi meslekte olursan ol, ikna kabiliyeti, hitabet ve doğru ifade etme gücü kariyerini belirler.",
                            futureOrYksImpact = "YKS'nin zaman tuzaklarını aşmanın tek anahtarıdır; Türkçeyi 30-35 dakikada bitiren dereceye koşar."
                        )
                    )
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = course,
                            whatToStudy = "Paragrafta ana fikir, akışı bozan cümle ve dil bilgisinde karma soru çözümleri.",
                            howToStudy = "Her sabah zihnin en berrakken 20 paragraf sorusunu kronometre açarak çöz. Seçenekleri eleme gerekçeni netleştir.",
                            dereceTechnique = "Zamanlı Paragraf Rutini: Derece öğrencileri son 1 yıl boyunca hiçbir sabahı 20 paragraf çözmeden geçirmez."
                        )
                    )
                }
                norm.contains("tarih") || norm.contains("coğrafya") || norm.contains("sosyal") || norm.contains("felsefe") -> {
                    benefits.add(
                        CourseBenefitItem(
                            courseName = course,
                            benefit = "Geçmişin tecrübeleriyle geleceği öngörebilme ve dünyadaki mekânsal/kültürel dinamikleri anlama vizyonu sağlar.",
                            whyItMatters = "Genel kültür, coğrafi farkındalık ve olayları çok yönlü değerlendirme olgunluğu kazandırır.",
                            futureOrYksImpact = "TYT Sosyal netleri ilk 1000'de eşit ağırlık ve sayısalcıları birbirinden ayıran gizli silahtır."
                        )
                    )
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = course,
                            whatToStudy = "Harita bilgisi, ilk çağ uygarlıkları, inkılap tarihi kronolojisi ve felsefe kavramları.",
                            howToStudy = "Ezberlemek yerine olaylar arası kronolojik köprü kur (Neden-Sonuç zinciri).",
                            dereceTechnique = "Dilsiz Harita Çalışması: Derece öğrencileri coğrafyada dilsiz dünya/Türkiye haritası üzerinde iklim ve boğazları işaretler."
                        )
                    )
                }
                else -> {
                    benefits.add(
                        CourseBenefitItem(
                            courseName = course,
                            benefit = "Disiplinli zihin inşası ve entelektüel derinlik kazandırır.",
                            whyItMatters = "Öğrenmeyi öğrenme refleksini pekiştirir.",
                            futureOrYksImpact = "Hedefindeki üniversite ve sıralama için her bir net altın değerindedir."
                        )
                    )
                    recommendations.add(
                        StudyRecommendationItem(
                            courseName = course,
                            whatToStudy = "Haftalık plandaki eksik kazanımların tespit edilip soruyla pekiştirilmesi.",
                            howToStudy = "Konu tekrarını soru üzerinden yap. Yapamadığın soruların doğru çözüm videosunu izle ve kendi başına tekrar çöz.",
                            dereceTechnique = "Hata Defteri: Çözemediğin her sorunun çözümünü öğrenip 3 gün sonra tekrar dene."
                        )
                    )
                }
            }
        }

        return TufiAICoachAdvice(
            headline = "Bugünün Derece Stratejisi: Odak ve Analiz Günü! 🎯",
            summary = "Bugün programındaki ${distinctCourses.joinToString(", ")} dersleri, doğru yöntemle çalışıldığında seni hedefine dev bir adım yaklaştıracak.",
            courseBenefits = benefits,
            studyRecommendations = recommendations,
            dereceTactic = "🏆 Derece Öğrencilerinin Sırrı: 'Turlama Tekniği & Analiz Defteri'. Soru bankasında takıldığın soruda inatlaşma, işaret koyup devam et. Günün sonunda yapamadığın soruları bir uzmana sorup öğren ve 'Hata Defteri'ne kaydet.",
            motivationQuote = "“Rakiplerin yorulduğunda bir soru daha çözenler derece yapar.” — YKS Türkiye 1.si Çalışma Prensibi",
            isFromApi = false
        )
    }
}
