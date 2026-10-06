package com.example.data.speech

enum class SpeechMode(
    val id: String,
    val title: String,
    val subtitle: String
) {
    CLOUD_OPENROUTER(
        id = "cloud_openrouter",
        title = "TufiAI Whisper (Bulut)",
        subtitle = "OpenRouter bulut API'si (openai/whisper-large-v3-turbo). Hızlı, en yüksek doğruluk."
    ),
    OFFLINE_SHERPA(
        id = "offline_sherpa",
        title = "Çevrimdışı Whisper (Cihazda)",
        subtitle = "Cihaz üzerinde yerel model (sherpa-onnx). İnternetsiz çalışır, ses cihazdan çıkmaz."
    );

    companion object {
        fun fromId(id: String?): SpeechMode {
            return entries.firstOrNull { it.id == id } ?: CLOUD_OPENROUTER
        }
    }
}
