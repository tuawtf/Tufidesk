package com.example.data.speech

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig

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
    val approximateSizeMb: Int,
    val speedLabel: String,
    val accuracyLabel: String
) {
    TINY(
        id = "tiny",
        displayName = "Tiny (int8)",
        description = "Minimum kaynak ve RAM kullanımı, en yüksek işlem hızı (~40 MB)",
        folderName = "whisper_tiny",
        tokensFileName = "tiny-tokens.txt",
        encoderFileName = "tiny-encoder.int8.onnx",
        decoderFileName = "tiny-decoder.int8.onnx",
        tokensUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-tokens.txt",
        encoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-encoder.int8.onnx",
        decoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main/tiny-decoder.int8.onnx",
        approximateSizeMb = 40,
        speedLabel = "Çok Hızlı ⚡",
        accuracyLabel = "Temel"
    ),
    BASE(
        id = "base",
        displayName = "Base (int8)",
        description = "Hızlı işlem ve dengeli kelime algılama (~75 MB)",
        folderName = "whisper_base",
        tokensFileName = "base-tokens.txt",
        encoderFileName = "base-encoder.int8.onnx",
        decoderFileName = "base-decoder.int8.onnx",
        tokensUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-tokens.txt",
        encoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-encoder.int8.onnx",
        decoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-base/resolve/main/base-decoder.int8.onnx",
        approximateSizeMb = 75,
        speedLabel = "Hızlı 🚀",
        accuracyLabel = "İyi / Dengeli"
    ),
    SMALL(
        id = "small",
        displayName = "Small (int8)",
        description = "Zengin Türkçe kelime dağarcığı ve yüksek doğruluk (~240 MB)",
        folderName = "whisper_small",
        tokensFileName = "small-tokens.txt",
        encoderFileName = "small-encoder.int8.onnx",
        decoderFileName = "small-decoder.int8.onnx",
        tokensUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-tokens.txt",
        encoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-encoder.int8.onnx",
        decoderUrl = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-small/resolve/main/small-decoder.int8.onnx",
        approximateSizeMb = 240,
        speedLabel = "Dengeli 🎯",
        accuracyLabel = "Yüksek"
    );

    companion object {
        fun fromId(id: String?): WhisperModelType {
            return entries.firstOrNull { it.id == id } ?: BASE
        }
    }
}

object SpeechSettingsManager {
    private const val PREFS_NAME = "speech_recognition_prefs"

    private const val KEY_SPEECH_MODE = "pref_speech_mode"
    private const val KEY_CLOUD_MODEL = "pref_cloud_whisper_model"
    private const val KEY_CUSTOM_OPENROUTER_KEY = "pref_openrouter_api_key"
    private const val KEY_SELECTED_OFFLINE_MODEL = "selected_whisper_model"
    private const val KEY_AI_CLEANUP = "ai_text_cleanup_enabled"
    private const val KEY_DOWNLOAD_WIFI_ONLY = "pref_download_wifi_only"

    const val DEFAULT_CLOUD_MODEL = "openai/whisper-large-v3-turbo"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // --- Sesli Dikte Modu ---
    fun getSpeechMode(context: Context): SpeechMode {
        val raw = getPrefs(context).getString(KEY_SPEECH_MODE, SpeechMode.CLOUD_OPENROUTER.id)
        return SpeechMode.fromId(raw)
    }

    fun setSpeechMode(context: Context, mode: SpeechMode) {
        getPrefs(context).edit().putString(KEY_SPEECH_MODE, mode.id).apply()
    }

    // --- Bulut Whisper Model Adı ---
    fun getCloudModelName(context: Context): String {
        val saved = getPrefs(context).getString(KEY_CLOUD_MODEL, DEFAULT_CLOUD_MODEL)?.trim()
        return if (!saved.isNullOrBlank()) saved else DEFAULT_CLOUD_MODEL
    }

    fun setCloudModelName(context: Context, modelName: String) {
        val cleaned = modelName.trim().ifBlank { DEFAULT_CLOUD_MODEL }
        getPrefs(context).edit().putString(KEY_CLOUD_MODEL, cleaned).apply()
    }

    // --- OpenRouter API Key ---
    fun getOpenRouterApiKey(context: Context): String {
        val customKey = getPrefs(context).getString(KEY_CUSTOM_OPENROUTER_KEY, null)?.trim()
        if (!customKey.isNullOrBlank()) {
            return customKey
        }
        val buildKey = try {
            BuildConfig.OPENROUTER_API_KEY.trim()
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_OPENROUTER_API_KEY") buildKey else ""
    }

    fun isOpenRouterKeyConfigured(context: Context): Boolean {
        return getOpenRouterApiKey(context).isNotBlank()
    }

    fun saveCustomOpenRouterApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_CUSTOM_OPENROUTER_KEY, key.trim()).apply()
    }

    // --- Çevrimdışı Model Seçimi ---
    fun getSelectedModel(context: Context): WhisperModelType {
        val modelId = getPrefs(context).getString(KEY_SELECTED_OFFLINE_MODEL, WhisperModelType.BASE.id)
            ?: WhisperModelType.BASE.id
        return WhisperModelType.fromId(modelId)
    }

    fun setSelectedModel(context: Context, modelType: WhisperModelType) {
        getPrefs(context).edit().putString(KEY_SELECTED_OFFLINE_MODEL, modelType.id).apply()
    }

    // --- Metni Yapay Zeka ile Düzelt ---
    fun isAiCleanupEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AI_CLEANUP, false)
    }

    fun setAiCleanupEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AI_CLEANUP, enabled).apply()
    }

    // --- Yalnızca Wi-Fi ile İndir ---
    fun isDownloadWifiOnlyEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DOWNLOAD_WIFI_ONLY, true)
    }

    fun setDownloadWifiOnlyEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_DOWNLOAD_WIFI_ONLY, enabled).apply()
    }
}
