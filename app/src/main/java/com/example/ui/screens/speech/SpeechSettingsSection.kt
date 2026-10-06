package com.example.ui.screens.speech

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.speech.BenchmarkResult
import com.example.data.speech.CloudWhisperService
import com.example.data.speech.DeviceAnalyzer
import com.example.data.speech.DeviceSpecs
import com.example.data.speech.ModelManager
import com.example.data.speech.ModelRecommendation
import com.example.data.speech.SpeechMode
import com.example.data.speech.SpeechSettingsManager
import com.example.data.speech.WhisperEngine
import com.example.data.speech.WhisperModelType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SpeechSettingsSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Ayar durumları
    var speechMode by remember { mutableStateOf(SpeechSettingsManager.getSpeechMode(context)) }
    var cloudModelName by remember { mutableStateOf(SpeechSettingsManager.getCloudModelName(context)) }
    var openRouterKeyInput by remember { mutableStateOf(SpeechSettingsManager.getOpenRouterApiKey(context)) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var isTestingKey by remember { mutableStateOf(false) }
    var keyTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }

    var selectedOfflineModel by remember { mutableStateOf(SpeechSettingsManager.getSelectedModel(context)) }
    var isAiCleanupEnabled by remember { mutableStateOf(SpeechSettingsManager.isAiCleanupEnabled(context)) }
    var isDownloadWifiOnly by remember { mutableStateOf(SpeechSettingsManager.isDownloadWifiOnlyEnabled(context)) }

    // Cihaz Analizi
    var deviceSpecs by remember { mutableStateOf<DeviceSpecs?>(null) }
    var recommendation by remember { mutableStateOf<ModelRecommendation?>(null) }

    // Model İndirme Durumu
    var downloadingModelType by remember { mutableStateOf<WhisperModelType?>(null) }
    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var downloadStatusText by remember { mutableStateOf("") }
    var downloadJob by remember { mutableStateOf<Job?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // Silme Diyaloğu
    var modelToDelete by remember { mutableStateOf<WhisperModelType?>(null) }

    // Hız Testi (Benchmark) Durumu
    var isBenchmarking by remember { mutableStateOf(false) }
    var benchmarkResult by remember { mutableStateOf<BenchmarkResult?>(null) }
    var benchmarkError by remember { mutableStateOf<String?>(null) }

    // Cihaz analizini yükle
    LaunchedEffect(Unit) {
        val specs = DeviceAnalyzer.analyze(context)
        deviceSpecs = specs
        recommendation = DeviceAnalyzer.getRecommendation(specs)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BAŞLIK
        Text(
            text = "Sesli Dikte",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // 1. DİKTE MODU SEÇİMİ (Material 3 SingleChoiceSegmentedButtonRow)
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeechMode.entries.forEachIndexed { index, mode ->
                val isSelected = speechMode == mode
                SegmentedButton(
                    selected = isSelected,
                    onClick = {
                        speechMode = mode
                        SpeechSettingsManager.setSpeechMode(context, mode)
                        WhisperEngine.release()
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = SpeechMode.entries.size),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = isSelected) {
                            Icon(
                                imageVector = if (mode == SpeechMode.CLOUD_OPENROUTER) Icons.Default.Cloud else Icons.Default.Smartphone,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    }
                ) {
                    Text(
                        text = if (mode == SpeechMode.CLOUD_OPENROUTER) "Bulut (OpenRouter)" else "Çevrimdışı (Cihazda)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // ==========================================
        // SEÇENEK A: TUFİ AI BULUT WHISPER (OPENROUTER)
        // ==========================================
        AnimatedVisibility(
            visible = speechMode == SpeechMode.CLOUD_OPENROUTER,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cloud,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "TufiAI Whisper (Bulut API)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "OpenRouter ses-yazı uç noktası üzerinden çalışır. İnternet ve API anahtarı gerekir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 1. Model İsmi Düzenleme Alanı
                    OutlinedTextField(
                        value = cloudModelName,
                        onValueChange = {
                            cloudModelName = it
                            SpeechSettingsManager.setCloudModelName(context, it)
                        },
                        label = { Text("Transkripsiyon Modeli") },
                        placeholder = { Text(SpeechSettingsManager.DEFAULT_CLOUD_MODEL) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (cloudModelName != SpeechSettingsManager.DEFAULT_CLOUD_MODEL) {
                                IconButton(onClick = {
                                    cloudModelName = SpeechSettingsManager.DEFAULT_CLOUD_MODEL
                                    SpeechSettingsManager.setCloudModelName(context, SpeechSettingsManager.DEFAULT_CLOUD_MODEL)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Varsayılana Sıfırla"
                                    )
                                }
                            }
                        },
                        supportingText = {
                            Text("Varsayılan: ${SpeechSettingsManager.DEFAULT_CLOUD_MODEL}")
                        }
                    )

                    // 2. OpenRouter API Anahtarı
                    OutlinedTextField(
                        value = openRouterKeyInput,
                        onValueChange = {
                            openRouterKeyInput = it
                            keyTestResult = null
                        },
                        label = { Text("OpenRouter API Anahtarı") },
                        placeholder = { Text("sk-or-v1-...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isKeyVisible) "Gizle" else "Göster"
                                )
                            }
                        },
                        supportingText = {
                            Text(
                                if (SpeechSettingsManager.isOpenRouterKeyConfigured(context))
                                    "Anahtar yapılandırılmış ve kullanıma hazır."
                                else
                                    "OpenRouter sitesinden aldığınız API anahtarını buraya girin."
                            )
                        }
                    )

                    // Anahtar Kaydet & Test Butonları
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                SpeechSettingsManager.saveCustomOpenRouterApiKey(context, openRouterKeyInput)
                                Toast.makeText(context, "API anahtarı kaydedildi.", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kaydet")
                        }

                        OutlinedButton(
                            onClick = {
                                isTestingKey = true
                                keyTestResult = null
                                coroutineScope.launch {
                                    val result = CloudWhisperService.testApiKey(context, openRouterKeyInput)
                                    isTestingKey = false
                                    keyTestResult = result
                                }
                            },
                            enabled = openRouterKeyInput.isNotBlank() && !isTestingKey,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isTestingKey) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Et")
                            }
                        }
                    }

                    // Test Sonucu Bildirimi
                    val testRes = keyTestResult
                    if (testRes != null) {
                        val (success, msg) = testRes
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = if (success) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (success) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = msg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (success) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SEÇENEK B: ÇEVRİMDIŞI WHISPER (CİHAZDA)
        // ==========================================
        AnimatedVisibility(
            visible = speechMode == SpeechMode.OFFLINE_SHERPA,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // 1. CİHAZ ANALİZİ KARTI
                val currentSpecs = deviceSpecs
                if (currentSpecs != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "Cihaz Donanım Analizi",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            // Donanım Parametreleri Grid
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                DeviceSpecChip(label = "RAM", value = "${currentSpecs.availRamFormatted} / ${currentSpecs.totalRamFormatted}")
                                DeviceSpecChip(label = "İşlemci", value = "${currentSpecs.cpuCores} Çekirdek (${currentSpecs.socModel})")
                                DeviceSpecChip(label = "Mimari", value = currentSpecs.primaryAbi)
                                DeviceSpecChip(label = "Boş Alan", value = currentSpecs.freeStorageFormatted)
                                if (currentSpecs.isPowerSaveMode) {
                                    DeviceSpecChip(label = "Pil", value = "Tasarruf Modu Açık ⚠️")
                                }
                            }

                            // Öneri Kutucuğu
                            val currentRec = recommendation
                            if (currentRec != null) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Column {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "Önerilen Paket: ${currentRec.recommendedModel.displayName}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                SuggestionChip(
                                                    onClick = {
                                                        selectedOfflineModel = currentRec.recommendedModel
                                                        SpeechSettingsManager.setSelectedModel(context, currentRec.recommendedModel)
                                                        WhisperEngine.release()
                                                    },
                                                    label = { Text("Uygula", style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.height(28.dp),
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                )
                                            }
                                            Text(
                                                text = currentRec.reason,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. YALNIZCA WI-FI İLE İNDİR ANAHTARI
                ListItem(
                    headlineContent = { Text("Yalnızca Wi-Fi ile İndir") },
                    supportingContent = { Text("Mobil veri kullanımını engellemek için modelleri sadece kablosuz ağda indir.") },
                    leadingContent = {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        Switch(
                            checked = isDownloadWifiOnly,
                            onCheckedChange = {
                                isDownloadWifiOnly = it
                                SpeechSettingsManager.setDownloadWifiOnlyEnabled(context, it)
                            }
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                )

                // 3. AKTİF İNDİRME İLERLEMESİ KARTI
                val activeDownloadingType = downloadingModelType
                if (activeDownloadingType != null) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text(
                                        text = "${activeDownloadingType.displayName} İndiriliyor",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        downloadJob?.cancel()
                                        downloadJob = null
                                        downloadingModelType = null
                                        Toast.makeText(context, "İndirme iptal edildi.", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("İptal")
                                }
                            }

                            LinearProgressIndicator(
                                progress = { downloadProgress },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = downloadStatusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 4. MODEL SEÇENEKLERİ LİSTESİ (Tiny / Base / Small)
                Text(
                    text = "Kullanılabilir Çevrimdışı Modeller",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                WhisperModelType.entries.forEach { model ->
                    val isSelected = selectedOfflineModel == model
                    val isDownloaded = remember(refreshTrigger) { ModelManager.isModelDownloaded(context, model) }
                    val isRecommended = recommendation?.recommendedModel == model
                    val isCurrentlyDownloadingThis = downloadingModelType == model
                    val suitability = deviceSpecs?.let { DeviceAnalyzer.getModelSuitability(model, it) }

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedOfflineModel = model
                                SpeechSettingsManager.setSelectedModel(context, model)
                                WhisperEngine.release()
                            },
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedOfflineModel = model
                                            SpeechSettingsManager.setSelectedModel(context, model)
                                            WhisperEngine.release()
                                        }
                                    )
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = model.displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (isRecommended) {
                                                SuggestionChip(
                                                    onClick = {},
                                                    label = { Text("Önerilen", style = MaterialTheme.typography.labelSmall) },
                                                    modifier = Modifier.height(24.dp),
                                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                                        containerColor = MaterialTheme.colorScheme.primary,
                                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Hız: ${model.speedLabel}  •  Doğruluk: ${model.accuracyLabel}  •  ~${model.approximateSizeMb} MB",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // İndirme & Durum İkonu
                                if (isDownloaded) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "İndirildi",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        IconButton(
                                            onClick = { modelToDelete = model }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Modeli Sil",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            if (downloadingModelType != null) {
                                                Toast.makeText(context, "Zaten devam eden bir indirme var.", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            downloadingModelType = model
                                            downloadProgress = 0f
                                            downloadStatusText = "Bağlanıyor..."
                                            downloadJob = coroutineScope.launch {
                                                val result = ModelManager.downloadModel(context, model) { prog, msg ->
                                                    downloadProgress = prog
                                                    downloadStatusText = msg
                                                }
                                                downloadingModelType = null
                                                if (result.isSuccess) {
                                                    refreshTrigger++
                                                    Toast.makeText(context, "${model.displayName} hazırlandı! 🎉", Toast.LENGTH_LONG).show()
                                                } else {
                                                    val err = result.exceptionOrNull()?.message ?: "İndirme başarısız oldu."
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        enabled = !isCurrentlyDownloadingThis,
                                        modifier = Modifier.height(36.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("İndir", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }

                            // Uygunsuzluk / Tavsiye uyarısı (varsa)
                            val warnMsg = suitability?.warningMessage
                            if (warnMsg != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = warnMsg,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. HIZ TESTİ (BENCHMARK) KARTI
                val isSelectedDownloaded = remember(refreshTrigger, selectedOfflineModel) {
                    ModelManager.isModelDownloaded(context, selectedOfflineModel)
                }

                if (isSelectedDownloaded) {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Model Hız Testi (Benchmark)",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Seçili ${selectedOfflineModel.displayName} modeli ile örnek ses test edilir.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        isBenchmarking = true
                                        benchmarkResult = null
                                        benchmarkError = null
                                        coroutineScope.launch {
                                            val bench = WhisperEngine.runSpeedBenchmark(context, selectedOfflineModel)
                                            isBenchmarking = false
                                            if (bench.isSuccess) {
                                                benchmarkResult = bench.getOrNull()
                                            } else {
                                                benchmarkError = bench.exceptionOrNull()?.message ?: "Hız testi başarısız oldu."
                                            }
                                        }
                                    },
                                    enabled = !isBenchmarking,
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    if (isBenchmarking) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Hız Testi Yap")
                                    }
                                }
                            }

                            // Benchmark Sonuç Bildirimi
                            val currentBench = benchmarkResult
                            if (currentBench != null) {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = if (currentBench.isRealTimeOrFaster) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = currentBench.summaryText,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentBench.isRealTimeOrFaster) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = currentBench.recommendationText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (currentBench.isRealTimeOrFaster) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            val currentBenchErr = benchmarkError
                            if (currentBenchErr != null) {
                                Text(
                                    text = currentBenchErr,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // ORTAK AYAR: YAPAY ZEKA METİN DÜZELTME
        // ==========================================
        ListItem(
            headlineContent = { Text("Metni Yapay Zekayla Düzelt") },
            supportingContent = {
                Text("Dikte edilen ham metni Tufi AI (DeepSeek) ile yalnızca noktalama, büyük harf ve dolgu kelimeleri (ııı, şey) için temizle (anlam değiştirilmez).")
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                Switch(
                    checked = isAiCleanupEnabled,
                    onCheckedChange = {
                        isAiCleanupEnabled = it
                        SpeechSettingsManager.setAiCleanupEnabled(context, it)
                    }
                )
            },
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }

    // MODEL SİLME ONAY DİYALOĞU
    val currentDeleteTarget = modelToDelete
    if (currentDeleteTarget != null) {
        AlertDialog(
            onDismissRequest = { modelToDelete = null },
            icon = {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("${currentDeleteTarget.displayName} Modelini Sil") },
            text = {
                Text("Bu model dosyalarını (${ModelManager.getDownloadedSizeFormatted(context, currentDeleteTarget)}) cihaz hafızasından silmek istediğinize emin misiniz? Dilediğiniz zaman tekrar indirebilirsiniz.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deleted = ModelManager.deleteModel(context, currentDeleteTarget)
                        modelToDelete = null
                        refreshTrigger++
                        WhisperEngine.release()
                        if (deleted) {
                            Toast.makeText(context, "${currentDeleteTarget.displayName} silindi.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { modelToDelete = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun DeviceSpecChip(label: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
