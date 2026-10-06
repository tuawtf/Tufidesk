package com.example.data.speech

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.StatFs
import java.util.Locale

data class DeviceSpecs(
    val totalRamBytes: Long,
    val availRamBytes: Long,
    val cpuCores: Int,
    val socModel: String,
    val primaryAbi: String,
    val freeStorageBytes: Long,
    val totalStorageBytes: Long,
    val isPowerSaveMode: Boolean
) {
    val totalRamGb: Float
        get() = totalRamBytes.toFloat() / (1024 * 1024 * 1024)

    val availRamGb: Float
        get() = availRamBytes.toFloat() / (1024 * 1024 * 1024)

    val freeStorageGb: Float
        get() = freeStorageBytes.toFloat() / (1024 * 1024 * 1024)

    val totalRamFormatted: String
        get() = String.format(Locale.US, "%.1f GB", totalRamGb)

    val availRamFormatted: String
        get() = String.format(Locale.US, "%.1f GB", availRamGb)

    val freeStorageFormatted: String
        get() = String.format(Locale.US, "%.1f GB", freeStorageGb)
}

data class ModelRecommendation(
    val recommendedModel: WhisperModelType,
    val reason: String,
    val isDeviceHighEnd: Boolean
)

data class ModelSuitability(
    val isSuitable: Boolean,
    val warningMessage: String? = null
)

object DeviceAnalyzer {

    // Eşik Değerleri (kolayca değiştirilebilir sabitler)
    const val RAM_THRESHOLD_LOW_MB = 3800L    // < 4 GB RAM -> Tiny veya Base
    const val RAM_THRESHOLD_HIGH_MB = 7500L   // >= 8 GB RAM -> Small
    const val CPU_CORES_HIGH_END = 8          // >= 8 Çekirdek
    const val STORAGE_MIN_MARGIN_MB = 400L    // En az 400 MB serbest alan payı

    fun analyze(context: Context): DeviceSpecs {
        // 1. RAM Bilgileri
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availRam = memoryInfo.availMem

        // 2. CPU Çekirdek Sayısı
        val cores = Runtime.getRuntime().availableProcessors()

        // 3. İşlemci Modeli (Build.SOC_MODEL, API 31+; yoksa Build.HARDWARE)
        val soc = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val socModel = Build.SOC_MODEL
            if (!socModel.isNullOrBlank() && socModel != Build.UNKNOWN) socModel else Build.HARDWARE
        } else {
            Build.HARDWARE.ifBlank { Build.BOARD }
        }

        // 4. Desteklenen Mimari (ABI)
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

        // 5. Boş Depolama Alanı (StatFs)
        val stat = StatFs(context.filesDir.path)
        val freeStorage = stat.availableBlocksLong * stat.blockSizeLong
        val totalStorage = stat.blockCountLong * stat.blockSizeLong

        // 6. Pil Tasarrufu Durumu
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isPowerSave = powerManager?.isPowerSaveMode ?: false

        return DeviceSpecs(
            totalRamBytes = totalRam,
            availRamBytes = availRam,
            cpuCores = cores,
            socModel = soc.ifBlank { "Standart SoC" },
            primaryAbi = abi,
            freeStorageBytes = freeStorage,
            totalStorageBytes = totalStorage,
            isPowerSaveMode = isPowerSave
        )
    }

    fun getRecommendation(specs: DeviceSpecs): ModelRecommendation {
        val totalRamMb = specs.totalRamBytes / (1024 * 1024)
        val freeStorageMb = specs.freeStorageBytes / (1024 * 1024)

        return when {
            // Depolama çok kısıtlıysa veya RAM < 3 GB ise -> Tiny
            freeStorageMb < 300 || totalRamMb < 3200 -> {
                ModelRecommendation(
                    recommendedModel = WhisperModelType.TINY,
                    reason = "Kısıtlı bellek veya düşük depolama alanı nedeniyle en az kaynak tüketen Tiny önerildi.",
                    isDeviceHighEnd = false
                )
            }
            // RAM 8 GB ve üzeri + 8 çekirdek -> Small
            totalRamMb >= RAM_THRESHOLD_HIGH_MB && specs.cpuCores >= CPU_CORES_HIGH_END -> {
                ModelRecommendation(
                    recommendedModel = WhisperModelType.SMALL,
                    reason = "Cihazınız yüksek donanıma (${specs.totalRamFormatted} RAM, ${specs.cpuCores} CPU) sahip. En yüksek Türkçe doğruluğu için Small önerildi.",
                    isDeviceHighEnd = true
                )
            }
            // RAM 4-8 GB arası -> Base (Dengeli)
            totalRamMb >= RAM_THRESHOLD_LOW_MB -> {
                ModelRecommendation(
                    recommendedModel = WhisperModelType.BASE,
                    reason = "Dengeli işlem hızı ve yeterli Türkçe kelime doğruluğu için Base modeli önerildi.",
                    isDeviceHighEnd = false
                )
            }
            // Diğer durumlar -> Tiny
            else -> {
                ModelRecommendation(
                    recommendedModel = WhisperModelType.TINY,
                    reason = "Düşük kaynak tüketimi ve anlık yüksek yanıt hızı için Tiny modeli önerildi.",
                    isDeviceHighEnd = false
                )
            }
        }
    }

    fun getModelSuitability(model: WhisperModelType, specs: DeviceSpecs): ModelSuitability {
        val totalRamMb = specs.totalRamBytes / (1024 * 1024)
        val freeStorageMb = specs.freeStorageBytes / (1024 * 1024)

        // Modelin gereksinim duyduğu tahmini depolama
        val neededStorageMb = model.approximateSizeMb + STORAGE_MIN_MARGIN_MB
        if (freeStorageMb < neededStorageMb) {
            return ModelSuitability(
                isSuitable = false,
                warningMessage = "Cihazda yetersiz depolama alanı (${specs.freeStorageFormatted} boş). İndirme başarısız olabilir."
            )
        }

        if (model == WhisperModelType.SMALL && totalRamMb < 4000) {
            return ModelSuitability(
                isSuitable = false,
                warningMessage = "Small modeli en az 4 GB RAM tavsiye eder (Cihazınız: ${specs.totalRamFormatted}). Transkripsiyon sırasında yavaşlama olabilir."
            )
        }

        if (specs.isPowerSaveMode && model == WhisperModelType.SMALL) {
            return ModelSuitability(
                isSuitable = true,
                warningMessage = "Pil tasarrufu açık. CPU performansı kısıtlandığından transkripsiyon süresi uzayabilir."
            )
        }

        return ModelSuitability(isSuitable = true, warningMessage = null)
    }
}
