package com.example.data.speech

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

data class AudioRecordResult(
    val samples: FloatArray,
    val durationMs: Long,
    val isSilent: Boolean,
    val maxRms: Double
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AudioRecordResult
        return samples.contentEquals(other.samples) &&
                durationMs == other.durationMs &&
                isSilent == other.isSilent
    }

    override fun hashCode(): Int {
        var result = samples.contentHashCode()
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + isSilent.hashCode()
        return result
    }
}

class AudioRecorder(private val context: Context) {
    companion object {
        private const val TAG = "AudioRecorder"
        const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT

        // Silence threshold: If maximum RMS throughout the recording is below this,
        // it means only microphone background hiss was recorded (no audible speech).
        // 16-bit PCM ranges from -32768 to 32767. Whispered/ambient speech typically produces RMS > 350-500.
        private const val SILENCE_RMS_THRESHOLD = 200.0
        private const val MIN_RECORDING_DURATION_MS = 400L
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var tickerJob: Job? = null
    private val isRecording = AtomicBoolean(false)

    private val pcmSamples = mutableListOf<Short>()
    private var recordingStartTime = 0L
    private var maxRecordedRms = 0.0

    @SuppressLint("MissingPermission")
    suspend fun startRecording(
        scope: CoroutineScope,
        onDurationTick: (seconds: Int, formattedTime: String) -> Unit,
        onAmplitudeTick: (amplitude: Float) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        if (isRecording.get()) return@withContext false

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        if (minBufferSize == AudioRecord.ERROR || minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Geçersiz ses tampon boyutu.")
            return@withContext false
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                Log.e(TAG, "AudioRecord başlatılamadı.")
                audioRecord?.release()
                audioRecord = null
                return@withContext false
            }

            pcmSamples.clear()
            maxRecordedRms = 0.0
            recordingStartTime = System.currentTimeMillis()
            audioRecord?.startRecording()
            isRecording.set(true)

            // Duration ticker
            tickerJob = scope.launch(Dispatchers.Main) {
                while (isRecording.get()) {
                    val elapsedSec = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
                    val minutes = elapsedSec / 60
                    val seconds = elapsedSec % 60
                    val formatted = String.format("%02d:%02d", minutes, seconds)
                    onDurationTick(elapsedSec, formatted)
                    delay(500)
                }
            }

            // Audio read loop
            recordingJob = scope.launch(Dispatchers.IO) {
                val readBuffer = ShortArray(1024)
                while (isActive && isRecording.get()) {
                    val readCount = audioRecord?.read(readBuffer, 0, readBuffer.size) ?: -1
                    if (readCount > 0) {
                        var sumSquares = 0.0
                        for (i in 0 until readCount) {
                            val sample = readBuffer[i]
                            pcmSamples.add(sample)
                            sumSquares += sample * sample
                        }
                        val rms = sqrt(sumSquares / readCount)
                        if (rms > maxRecordedRms) {
                            maxRecordedRms = rms
                        }
                        val normalizedAmp = (rms / 8000.0).coerceIn(0.0, 1.0).toFloat()
                        scope.launch(Dispatchers.Main) {
                            onAmplitudeTick(normalizedAmp)
                        }
                    }
                }
            }

            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Kayıt başlatılırken hata: ${e.message}", e)
            audioRecord?.release()
            audioRecord = null
            isRecording.set(false)
            return@withContext false
        }
    }

    suspend fun stopRecording(): AudioRecordResult = withContext(Dispatchers.IO) {
        isRecording.set(false)
        tickerJob?.cancel()
        tickerJob = null
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Kayıt durdurulurken uyarı: ${e.message}")
        }
        audioRecord?.release()
        audioRecord = null

        val duration = System.currentTimeMillis() - recordingStartTime

        // Convert short samples to float array normalized to [-1.0, 1.0]
        val floatArray = FloatArray(pcmSamples.size)
        for (i in pcmSamples.indices) {
            floatArray[i] = (pcmSamples[i].toFloat() / 32768.0f).coerceIn(-1.0f, 1.0f)
        }

        // Silence check
        val isSilent = duration < MIN_RECORDING_DURATION_MS || maxRecordedRms < SILENCE_RMS_THRESHOLD

        Log.d(TAG, "Kayıt bitti. Örnek sayısı: ${floatArray.size}, Süre: ${duration}ms, MaxRMS: $maxRecordedRms, Sessiz: $isSilent")

        return@withContext AudioRecordResult(
            samples = floatArray,
            durationMs = duration,
            isSilent = isSilent,
            maxRms = maxRecordedRms
        )
    }

    fun isCurrentlyRecording(): Boolean = isRecording.get()
}
