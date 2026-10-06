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
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

data class AudioRecordResult(
    val samples: FloatArray,
    val rawPcmBytes: ByteArray,
    val durationMs: Long,
    val isSilent: Boolean,
    val maxRms: Double
) {
    fun toWavByteArray(sampleRate: Int = AudioRecorder.SAMPLE_RATE, channels: Short = 1, bitsPerSample: Short = 16): ByteArray {
        val pcmDataSize = rawPcmBytes.size
        val totalDataLen = pcmDataSize + 36
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray())
        header.putInt(totalDataLen)
        header.put("WAVE".toByteArray())
        header.put("fmt ".toByteArray())
        header.putInt(16) // Subchunk1Size
        header.putShort(1.toShort()) // AudioFormat 1 = PCM
        header.putShort(channels)
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort((channels * bitsPerSample / 8).toShort()) // BlockAlign
        header.putShort(bitsPerSample)
        header.put("data".toByteArray())
        header.putInt(pcmDataSize)

        val wavBytes = ByteArray(44 + pcmDataSize)
        System.arraycopy(header.array(), 0, wavBytes, 0, 44)
        System.arraycopy(rawPcmBytes, 0, wavBytes, 44, pcmDataSize)
        return wavBytes
    }

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

        // Sessizlik eşiği: mikrofon dip gürültüsünden yüksek anlamlı konuşma var mı?
        private const val SILENCE_RMS_THRESHOLD = 200.0
        private const val MIN_RECORDING_DURATION_MS = 400L
    }

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var tickerJob: Job? = null
    private val isRecording = AtomicBoolean(false)

    private val rawByteStream = ByteArrayOutputStream(64 * 1024)
    private val streamLock = Any()
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

            synchronized(streamLock) {
                rawByteStream.reset()
            }
            maxRecordedRms = 0.0
            recordingStartTime = System.currentTimeMillis()
            audioRecord?.startRecording()
            isRecording.set(true)

            // Süre sayacı
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

            // Ses okuma döngüsü
            recordingJob = scope.launch(Dispatchers.IO) {
                val byteBuffer = ByteArray(2048)
                while (isActive && isRecording.get()) {
                    val readBytes = audioRecord?.read(byteBuffer, 0, byteBuffer.size) ?: -1
                    if (readBytes > 0) {
                        synchronized(streamLock) {
                            rawByteStream.write(byteBuffer, 0, readBytes)
                        }

                        // RMS hesaplama
                        var sumSquares = 0.0
                        val sampleCount = readBytes / 2
                        for (i in 0 until sampleCount) {
                            val low = byteBuffer[i * 2].toInt() and 0xFF
                            val high = byteBuffer[i * 2 + 1].toInt()
                            val sample = (high shl 8) or low
                            sumSquares += sample * sample
                        }
                        val rms = sqrt(sumSquares / sampleCount.coerceAtLeast(1))
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
        } catch (e: Throwable) {
            Log.e(TAG, "Kayıt başlatılırken hata: ${e.message}", e)
            try {
                audioRecord?.release()
            } catch (_: Throwable) {}
            audioRecord = null
            isRecording.set(false)
            return@withContext false
        }
    }

    suspend fun stopRecording(): AudioRecordResult = withContext(Dispatchers.IO) {
        isRecording.set(false)
        tickerJob?.cancel()
        tickerJob = null

        try {
            recordingJob?.cancelAndJoin()
        } catch (_: Throwable) {}
        recordingJob = null

        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Kayıt durdurulurken uyarı: ${e.message}")
        }
        try {
            audioRecord?.release()
        } catch (_: Throwable) {}
        audioRecord = null

        val duration = System.currentTimeMillis() - recordingStartTime

        val rawBytes = synchronized(streamLock) {
            rawByteStream.toByteArray()
        }

        val sampleCount = rawBytes.size / 2
        val floatArray = FloatArray(sampleCount)
        val byteBuf = ByteBuffer.wrap(rawBytes).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until sampleCount) {
            val shortVal = byteBuf.short
            floatArray[i] = (shortVal.toFloat() / 32768.0f).coerceIn(-1.0f, 1.0f)
        }

        // Sessizlik denetimi
        val isSilent = duration < MIN_RECORDING_DURATION_MS || maxRecordedRms < SILENCE_RMS_THRESHOLD || sampleCount < 1600

        Log.d(TAG, "Kayıt tamamlandı. Örnek sayısı: $sampleCount, Süre: ${duration}ms, MaxRMS: $maxRecordedRms, Sessiz: $isSilent")

        return@withContext AudioRecordResult(
            samples = floatArray,
            rawPcmBytes = rawBytes,
            durationMs = duration,
            isSilent = isSilent,
            maxRms = maxRecordedRms
        )
    }

    fun isCurrentlyRecording(): Boolean = isRecording.get()
}
