package com.example.data.speech

import android.content.Context
import android.util.Log
import com.example.data.ai.TufiAIService
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.sin

sealed class TranscriptionResult {
    data class Success(val text: String, val wasAiCleaned: Boolean = false) : TranscriptionResult()
    data class SilenceDetected(val reason: String = "Ses algılanamadı.") : TranscriptionResult()
    data class ModelNotAvailable(val message: String) : TranscriptionResult()
    data class Error(val message: String, val cause: Throwable? = null) : TranscriptionResult()
}

data class BenchmarkResult(
    val audioDurationSec: Float,
    val elapsedSec: Float,
    val speedMultiplier: Float,
    val isRealTimeOrFaster: Boolean,
    val summaryText: String,
    val recommendationText: String
)

object WhisperEngine {
    private const val TAG = "WhisperEngine"

    private var recognizer: OfflineRecognizer? = null
    private var loadedModelType: WhisperModelType? = null
    private val mutex = Mutex()

    /**
     * Initializes the recognizer if not already loaded with the given model type.
     * Safely catches any Throwable (including JNI / UnsatisfiedLinkError).
     * Passes null for AssetManager so sherpa-onnx loads directly from file system.
     */
    suspend fun getOrInitRecognizer(context: Context, modelType: WhisperModelType): OfflineRecognizer? = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (recognizer != null && loadedModelType == modelType) {
                return@withContext recognizer
            }

            val files = ModelManager.getModelFiles(context, modelType)
            if (files == null) {
                Log.w(TAG, "Model dosyaları mevcut değil: ${modelType.id}")
                return@withContext null
            }

            try {
                try {
                    recognizer?.release()
                } catch (_: Throwable) {}
                recognizer = null

                val (tokensFile, encoderFile, decoderFile) = files

                val whisperConfig = OfflineWhisperModelConfig(
                    encoder = encoderFile.absolutePath,
                    decoder = decoderFile.absolutePath,
                    language = "tr",
                    task = "transcribe"
                )

                val modelConfig = OfflineModelConfig(
                    whisper = whisperConfig,
                    tokens = tokensFile.absolutePath,
                    numThreads = 2.coerceAtMost(Runtime.getRuntime().availableProcessors()),
                    debug = false,
                    modelType = "whisper"
                )

                val featConfig = FeatureConfig(
                    sampleRate = 16000,
                    featureDim = 80
                )

                val recognizerConfig = OfflineRecognizerConfig(
                    featConfig = featConfig,
                    modelConfig = modelConfig,
                    decodingMethod = "greedy_search"
                )

                // AssetManager null verilmeli çünkü modeller context.filesDir içinde yer alıyor
                val newRecognizer = OfflineRecognizer(null, recognizerConfig)
                recognizer = newRecognizer
                loadedModelType = modelType
                Log.i(TAG, "Sherpa-ONNX Whisper (${modelType.displayName}) başarıyla yüklendi.")
                return@withContext recognizer
            } catch (t: Throwable) {
                Log.e(TAG, "Whisper başlatılamadı: ${t.message}", t)
                try {
                    recognizer?.release()
                } catch (_: Throwable) {}
                recognizer = null
                loadedModelType = null
                return@withContext null
            }
        }
    }

    /**
     * Release recognizer instance and native memory safely.
     */
    fun release() {
        try {
            recognizer?.release()
        } catch (t: Throwable) {
            Log.w(TAG, "Recognizer serbest bırakılırken hata: ${t.message}")
        } finally {
            recognizer = null
            loadedModelType = null
        }
    }

    /**
     * Transcribes normalized PCM 16kHz audio samples.
     * Runs strictly on background threads (Dispatchers.Default / IO).
     * Never throws unhandled exceptions.
     */
    suspend fun transcribe(
        context: Context,
        audioResult: AudioRecordResult,
        preferredModelType: WhisperModelType = SpeechSettingsManager.getSelectedModel(context)
    ): TranscriptionResult = withContext(Dispatchers.Default) {
        try {
            // 1. Sessizlik kontrolü
            if (audioResult.isSilent || audioResult.samples.isEmpty()) {
                return@withContext TranscriptionResult.SilenceDetected("Ses algılanamadı, lütfen mikrofona daha yakın konuşun.")
            }

            // 2. Model hazır mı?
            if (!ModelManager.isModelDownloaded(context, preferredModelType)) {
                return@withContext TranscriptionResult.ModelNotAvailable(
                    "Çevrimdışı Whisper modeli (${preferredModelType.displayName}) henüz indirilmedi."
                )
            }

            val rec = getOrInitRecognizer(context, preferredModelType)
                ?: return@withContext TranscriptionResult.Error(
                    "Whisper modeli cihazda başlatılamadı. Model dosyaları eksik veya cihazınızın mimarisi ile uyumsuz olabilir."
                )

            val rawText = try {
                val stream = rec.createStream()
                try {
                    stream.acceptWaveform(audioResult.samples, 16000)
                    rec.decode(stream)
                    val result = rec.getResult(stream)
                    result.text.trim()
                } finally {
                    try {
                        stream.release()
                    } catch (_: Throwable) {}
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Transkripsiyon sırasında hata: ${t.message}", t)
                return@withContext TranscriptionResult.Error(
                    "Ses çözümlenirken hata oluştu: ${t.localizedMessage ?: t.javaClass.simpleName}",
                    t
                )
            }

            // Whisper halüsinasyon filtreleme
            val cleanedArtifact = filterWhisperArtifacts(rawText)
            if (cleanedArtifact.isBlank()) {
                return@withContext TranscriptionResult.SilenceDetected("Net bir konuşma anlaşılamadı.")
            }

            // 3. İsteğe bağlı AI metin düzenleme (DeepSeek)
            var finalText = cleanedArtifact
            var wasCleaned = false

            if (SpeechSettingsManager.isAiCleanupEnabled(context) && TufiAIService.isApiKeyConfigured(context)) {
                try {
                    val aiCleaned = withContext(Dispatchers.IO) {
                        TufiAIService.cleanTranscriptionWithAI(context, cleanedArtifact)
                    }
                    if (aiCleaned.isNotBlank()) {
                        finalText = aiCleaned
                        wasCleaned = true
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "Yapay zeka ses temizleme hatası: ${t.message}")
                }
            }

            TranscriptionResult.Success(text = finalText, wasAiCleaned = wasCleaned)
        } catch (t: Throwable) {
            Log.e(TAG, "transcribe genel hata: ${t.message}", t)
            TranscriptionResult.Error(
                "Ses işlenirken beklenmedik bir hata oluştu: ${t.localizedMessage ?: t.javaClass.simpleName}",
                t
            )
        }
    }

    /**
     * Hız Testi (Benchmark): Kısa örnek ses (3.5 saniye) ile cihazın gerçek zamanlı işlem hızını ölçer.
     */
    suspend fun runSpeedBenchmark(
        context: Context,
        modelType: WhisperModelType = SpeechSettingsManager.getSelectedModel(context)
    ): Result<BenchmarkResult> = withContext(Dispatchers.Default) {
        if (!ModelManager.isModelDownloaded(context, modelType)) {
            return@withContext Result.failure(IllegalStateException("Hız testi yapabilmek için önce modelin indirilmesi gerekir."))
        }

        val rec = getOrInitRecognizer(context, modelType)
            ?: return@withContext Result.failure(IllegalStateException("Whisper motoru başlatılamadı."))

        // 3.5 saniyelik sentetik konuşma frekans dalgası oluştur (16 kHz, 56000 örnek)
        val sampleRate = 16000
        val durationSec = 3.5f
        val totalSamples = (sampleRate * durationSec).toInt()
        val syntheticSamples = FloatArray(totalSamples)

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            // Konuşma formatları karışımı (220 Hz, 440 Hz, 880 Hz)
            val wave = 0.4 * sin(2.0 * Math.PI * 220.0 * t) +
                       0.3 * sin(2.0 * Math.PI * 440.0 * t) +
                       0.2 * sin(2.0 * Math.PI * 880.0 * t)
            syntheticSamples[i] = wave.toFloat()
        }

        val startTime = System.currentTimeMillis()
        try {
            val stream = rec.createStream()
            try {
                stream.acceptWaveform(syntheticSamples, sampleRate)
                rec.decode(stream)
                rec.getResult(stream)
            } finally {
                try { stream.release() } catch (_: Throwable) {}
            }
            val elapsedMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
            val elapsedSec = elapsedMs / 1000f
            val multiplier = durationSec / elapsedSec
            val isFast = multiplier >= 1.0f

            val summaryText = String.format(
                Locale.US,
                "%.1f sn ses %.2f sn içinde işlendi (%.1fx gerçek zamanlı hız)",
                durationSec,
                elapsedSec,
                multiplier
            )

            val recommendation = when {
                multiplier >= 2.0f -> "Harika! Cihazınız bu modeli son derece akıcı ve gerçek zamandan iki kat daha hızlı çalıştırıyor. 🚀"
                multiplier >= 1.0f -> "İyi performans. Konuşma süresiyle başa baş veya daha hızlı işleniyor. 👍"
                else -> "Model cihazınızda biraz yavaş çalışıyor (gerçek zamandan daha yavaş). Daha hızlı bir deneyim için Base veya Tiny modeline geçmeyi düşünebilirsiniz. ⚡"
            }

            Result.success(
                BenchmarkResult(
                    audioDurationSec = durationSec,
                    elapsedSec = elapsedSec,
                    speedMultiplier = multiplier,
                    isRealTimeOrFaster = isFast,
                    summaryText = summaryText,
                    recommendationText = recommendation
                )
            )
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private fun filterWhisperArtifacts(text: String): String {
        val trimmed = text.trim()
        val lower = trimmed.lowercase(Locale.forLanguageTag("tr-TR"))

        // Whisper'ın sessizlikte sıkça halüsinasyon olarak ürettiği kalıplar
        val commonArtifacts = listOf(
            "altyazı", "m.k.", "izlediğiniz için teşekkürler",
            "abone olmayı unutmayın", "görüşmek üzere", "hoşça kalın",
            "thank you", "subtitles by", "translated by", "..."
        )

        for (art in commonArtifacts) {
            if (lower == art || lower == "[$art]" || lower == "($art)") {
                return ""
            }
        }

        if (trimmed.length <= 1 && !trimmed[0].isLetterOrDigit()) {
            return ""
        }

        return trimmed
    }
}
