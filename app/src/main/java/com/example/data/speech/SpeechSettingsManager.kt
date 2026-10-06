package com.example.data.speech

import android.content.Context
import android.content.SharedPreferences

enum class WhisperModelType(
    val id: String,
    val displayName: String,
    val description: String,
    val folderName: String,
    val tokensFileName: String,
    val encoderFileName: String,
    val decoderFileName: String,
    val tokensUrl: String,
    val encoderUrl: String,
    val decoderUrl: String,
    val approximateSizeMb: Int
) {
    SMALL(
        id = "small",
        displayName = "Small (Önerilen)",
        description = "Daha yüksek doğruluk ve zengin Türkçe kelime dağarcığı (~240 MB)",
        folderName = "whisper_small",
        tokensFileName = "small-tokens.txt",
        encoderFileName = "small-encoder.int8.onnx",
        decoderFileName = "small-decoder.int8.onnx",
        tokensUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-tokens.txt",
        encoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-encoder.int8.onnx",
        decoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-decoder.int8.onnx",
        approximateSizeMb = 240
    ),
    BASE(
        id = "base",
        displayName = "Base (Hızlı)",
        description = "Düşük kaynak tüketimi ve daha hızlı transkripsiyon (~75 MB)",
        folderName = "whisper_base",
        tokensFileName = "base-tokens.txt",
        encoderFileName = "base-encoder.int8.onnx",
        decoderFileName = "base-decoder.int8.onnx",
        tokensUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-tokens.txt",
        encoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-encoder.int8.onnx",
        decoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-decoder.int8.onnx",
        approximateSizeMb = 75
    );

    companion object {
        fun fromId(id: String): WhisperModelType {
            return entries.firstOrNull { it.id == id } ?: SMALL
        }
    }
}

object SpeechSettingsManager {
    private const val PREFS_NAME = "speech_recognition_prefs"
    private const val KEY_SELECTED_MODEL = "selected_whisper_model"
    private const val KEY_AI_CLEANUP = "ai_text_cleanup_enabled"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSelectedModel(context: Context): WhisperModelType {
        val modelId = getPrefs(context).getString(KEY_SELECTED_MODEL, WhisperModelType.SMALL.id)
            ?: WhisperModelType.SMALL.id
        return WhisperModelType.fromId(modelId)
    }

    fun setSelectedModel(context: Context, modelType: WhisperModelType) {
        getPrefs(context).edit().putString(KEY_SELECTED_MODEL, modelType.id).apply()
    }

    fun isAiCleanupEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AI_CLEANUP, false)
    }

    fun setAiCleanupEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AI_CLEANUP, enabled).apply()
    }
}
