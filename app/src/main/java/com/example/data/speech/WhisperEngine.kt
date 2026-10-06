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

sealed class TranscriptionResult {
    data class Success(val text: String, val wasAiCleaned: Boolean = false) : TranscriptionResult()
    data class SilenceDetected(val reason: String = "Ses algılanamadı.") : TranscriptionResult()
    data class ModelNotAvailable(val message: String) : TranscriptionResult()
    data class Error(val message: String, val cause: Throwable? = null) : TranscriptionResult()
}

object WhisperEngine {
    private const val TAG = "WhisperEngine"

    private var recognizer: OfflineRecognizer? = null
    private var loadedModelType: WhisperModelType? = null
    private val mutex = Mutex()

    /**
     * Initializes the recognizer if not already loaded with the given model type.
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
                // Release existing recognizer if any
                recognizer?.release()
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

                recognizer = OfflineRecognizer(context.assets, recognizerConfig)
                loadedModelType = modelType
                Log.i(TAG, "Sherpa-ONNX Whisper (${modelType.displayName}) başarıyla yüklendi.")
                return@withContext recognizer
            } catch (e: Exception) {
                Log.e(TAG, "Whisper başlatılamadı: ${e.message}", e)
                recognizer = null
                loadedModelType = null
                return@withContext null
            }
        }
    }

    /**
     * Release recognizer instance and memory.
     */
    fun release() {
        try {
            recognizer?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Recognizer serbest bırakılırken hata: ${e.message}")
        } finally {
            recognizer = null
            loadedModelType = null
        }
    }

    /**
     * Transcribes normalized PCM 16kHz audio samples.
     * Runs strictly on background threads (Dispatchers.Default / IO).
     */
    suspend fun transcribe(
        context: Context,
        audioResult: AudioRecordResult,
        preferredModelType: WhisperModelType = SpeechSettingsManager.getSelectedModel(context)
    ): TranscriptionResult = withContext(Dispatchers.Default) {
        // 1. Silence check protection (prevents hallucinating subtitle artifacts like "Altyazı M.K.")
        if (audioResult.isSilent || audioResult.samples.isEmpty()) {
            return@withContext TranscriptionResult.SilenceDetected("Ses algılanamadı, lütfen mikrofona daha yakın konuşun.")
        }

        // 2. Model readiness check
        if (!ModelManager.isModelDownloaded(context, preferredModelType)) {
            return@withContext TranscriptionResult.ModelNotAvailable(
                "Whisper modeli (${preferredModelType.displayName}) henüz indirilmedi."
            )
        }

        val rec = getOrInitRecognizer(context, preferredModelType)
            ?: return@withContext TranscriptionResult.Error("Whisper modeli bellekten başlatılamadı.")

        val rawText = try {
            val stream = rec.createStream()
            try {
                stream.acceptWaveform(audioResult.samples, 16000)
                rec.decode(stream)
                val result = rec.getResult(stream)
                result.text.trim()
            } finally {
                stream.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Transkripsiyon sırasında hata: ${e.message}", e)
            return@withContext TranscriptionResult.Error("Ses çözümlenirken hata oluştu: ${e.localizedMessage}", e)
        }

        // Hallucination filter for empty or common whisper artifacts on near-silent audio
        val cleanedArtifact = filterWhisperArtifacts(rawText)
        if (cleanedArtifact.isBlank()) {
            return@withContext TranscriptionResult.SilenceDetected("Net bir konuşma anlaşılamadı.")
        }

        // 3. Optional AI Post-processing (DeepSeek / Gemini clean up)
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
            } catch (e: Exception) {
                Log.w(TAG, "Yapay zeka metin düzeltmesi başarısız, ham metin kullanılıyor: ${e.message}")
            }
        }

        TranscriptionResult.Success(text = finalText, wasAiCleaned = wasCleaned)
    }

    /**
     * Filters out standard Whisper silence/hallucination artifacts in Turkish.
     */
    private fun filterWhisperArtifacts(text: String): String {
        val trimmed = text.trim()
        val lower = trimmed.lowercase()

        // Known Whisper hallucination phrases in Turkish on silence / noise
        val artifacts = listOf(
            "altyazı",
            "altyazı m.k.",
            "altyazı mk",
            "izlediğiniz için teşekkürler",
            "izlediğiniz için teşekkür ederim",
            "abone olmayı unutmayın",
            "beğenmeyi unutmayın",
            "görüşmek üzere",
            "hoşçakalın",
            "...",
            "."
        )

        for (art in artifacts) {
            if (lower == art || lower.startsWith("$art.") || lower == "[$art]") {
                return ""
            }
        }

        return trimmed
    }
}
