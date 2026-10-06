package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.google.firebase.FirebaseApp
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.font.FontFamily
import android.content.Intent
import com.example.CrashActivity
import com.example.data.firebase.SyncState
import com.example.data.model.UserAccount
import com.example.ui.components.UserFlowerAvatar
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.OutlinedButton
import com.example.data.ai.TufiAIService
import com.example.ui.components.TufiAISettingsDialog
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.data.speech.SpeechSettingsManager
import com.example.data.speech.WhisperModelType
import com.example.data.speech.ModelManager
import com.example.data.speech.WhisperEngine

data class MaterialPaletteOption(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val isDynamic: Boolean = false
)

@Composable
fun SettingsScreen(
    currentThemeMode: String,
    onThemeModeChange: (String) -> Unit,
    currentColorPalette: String,
    onColorPaletteChange: (String) -> Unit,
    hideCompleted: Boolean,
    onHideCompletedChange: (Boolean) -> Unit,
    isFullScreen: Boolean = false,
    onFullScreenChange: (Boolean) -> Unit = {},
    floatingBottomBar: Boolean = true,
    onFloatingBottomBarChange: (Boolean) -> Unit = {},
    onResetDemoData: () -> Unit = {},
    onClearAllData: () -> Unit = {},
    currentUser: UserAccount? = null,
    onSwitchProfile: () -> Unit = {},
    onSelectProfile: (UserAccount) -> Unit = {},
    isOnline: Boolean = true,
    syncState: SyncState = SyncState.IDLE,
    syncErrorMessage: String? = null,
    onManualSync: () -> Unit = {},
    onLogout: () -> Unit = {},
    canManage: Boolean = true,
    isNoteGrid: Boolean = true,
    onNoteGridChange: (Boolean) -> Unit = {},
    moveCheckedToBottom: Boolean = true,
    onMoveCheckedToBottomChange: (Boolean) -> Unit = {},
    onResetFirebaseData: ((Boolean, String) -> Unit) -> Unit = {},
    onClearAllFirebaseData: ((Boolean, String) -> Unit) -> Unit = {},
    onExportFirebaseJson: ((Boolean, String) -> Unit) -> Unit = {},
    onImportFirebaseJson: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showDeveloperSettingsScreen by rememberSaveable { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPaletteDialog by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableStateOf(0) }
    var isDeveloperModeActive by rememberSaveable { mutableStateOf(true) }
    var showDevOptionsDialog by remember { mutableStateOf(false) }

    var showResetFirebaseConfirmDialog by remember { mutableStateOf(false) }
    var isResettingFirebase by remember { mutableStateOf(false) }

    var showClearFirebaseAllConfirmDialog by remember { mutableStateOf(false) }
    var isClearingFirebase by remember { mutableStateOf(false) }

    var showExportJsonDialog by remember { mutableStateOf(false) }
    var exportedJsonText by remember { mutableStateOf("") }
    var isExportingJson by remember { mutableStateOf(false) }

    var showImportJsonDialog by remember { mutableStateOf(false) }
    var importJsonInput by remember { mutableStateOf("") }
    var isImportingJson by remember { mutableStateOf(false) }

    val paletteOptions = listOf(
        MaterialPaletteOption(
            id = "DYNAMIC",
            name = "Material You (Dinamik Sistem)",
            description = "Android duvar kağıdı ve sistem rengine uyumlu",
            primaryColor = MaterialTheme.colorScheme.primary,
            secondaryColor = MaterialTheme.colorScheme.secondary,
            isDynamic = true
        ),
        MaterialPaletteOption(
            id = "INDIGO",
            name = "Material Indigo",
            description = "Klasik akademik mavi ve zümrüt",
            primaryColor = Color(0xFF1B65D4),
            secondaryColor = Color(0xFF006874)
        ),
        MaterialPaletteOption(
            id = "TEAL",
            name = "Material Teal",
            description = "Huzurlu zümrüt ve camgöbeği",
            primaryColor = Color(0xFF006A60),
            secondaryColor = Color(0xFF4A635F)
        ),
        MaterialPaletteOption(
            id = "VIOLET",
            name = "Material Violet",
            description = "Material 3 standart lavantavi mor",
            primaryColor = Color(0xFF6750A4),
            secondaryColor = Color(0xFF625B71)
        ),
        MaterialPaletteOption(
            id = "ROSE",
            name = "Material Rose",
            description = "Zarif gül kurusu ve terracotta",
            primaryColor = Color(0xFF984061),
            secondaryColor = Color(0xFF74565F)
        ),
        MaterialPaletteOption(
            id = "FOREST",
            name = "Material Forest",
            description = "Doğal orman yeşili ve adaçayı",
            primaryColor = Color(0xFF2E6A38),
            secondaryColor = Color(0xFF526350)
        ),
        MaterialPaletteOption(
            id = "AMBER",
            name = "Material Amber",
            description = "Sıcak kehribar ve sonbahar tonları",
            primaryColor = Color(0xFF865300),
            secondaryColor = Color(0xFF705B40)
        )
    )

    val context = androidx.compose.ui.platform.LocalContext.current
    val appVersion = remember(context) {
        try {
            val pInfo = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            pInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }
    }

    if (showDeveloperSettingsScreen) {
        BackHandler { showDeveloperSettingsScreen = false }
        DeveloperSettingsContent(
            onNavigateBack = { showDeveloperSettingsScreen = false },
            isOnline = isOnline,
            syncState = syncState,
            syncErrorMessage = syncErrorMessage,
            onClearAllFirebaseClick = { showClearFirebaseAllConfirmDialog = true },
            onResetFirebaseClick = { showResetFirebaseConfirmDialog = true },
            onExportJsonClick = {
                isExportingJson = true
                onExportFirebaseJson { success, result ->
                    isExportingJson = false
                    if (success) {
                        exportedJsonText = result
                        showExportJsonDialog = true
                    } else {
                        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
                    }
                }
            },
            isExportingJson = isExportingJson,
            onImportJsonClick = {
                importJsonInput = ""
                showImportJsonDialog = true
            },
            onOpenDiagnosticsClick = { showDevOptionsDialog = true },
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .testTag("settings_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Section 0: User Profile
        item {
            SettingsCategoryCard(title = "Aktif Profil") {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserFlowerAvatar(
                            user = currentUser,
                            size = 56.dp,
                            shape = RoundedCornerShape(16.dp),
                            isSelected = true
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser?.name ?: "Kullanıcı",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Öğrenci Hesabı",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = onSwitchProfile,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Değiştir", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // Section 1: Appearance & Theme
        item {
            SettingsCategoryCard(title = "Görünüm ve Tema") {
                val themeLabel = when (currentThemeMode) {
                    "LIGHT" -> "Açık Tema"
                    "DARK" -> "Koyu Tema"
                    else -> "Sistem Teması (Otomatik)"
                }
                val themeIcon = when (currentThemeMode) {
                    "LIGHT" -> Icons.Default.LightMode
                    "DARK" -> Icons.Default.DarkMode
                    else -> Icons.Default.BrightnessAuto
                }

                SettingsClickableItem(
                    icon = themeIcon,
                    title = "Uygulama Teması",
                    subtitle = themeLabel,
                    onClick = { showThemeDialog = true }
                )

                Spacer(modifier = Modifier.height(4.dp))

                val currentOption = paletteOptions.find { it.id == currentColorPalette } ?: paletteOptions.first()
                SettingsClickableItem(
                    icon = Icons.Default.Palette,
                    title = "Materyal Renk Paleti",
                    subtitle = currentOption.name,
                    onClick = { showPaletteDialog = true }
                )

                Spacer(modifier = Modifier.height(6.dp))

                SettingsSwitchItem(
                    icon = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    title = "Tam Ekran Yap",
                    subtitle = "Durum ve gezinme çubuklarını gizleyerek odaklanmış tam ekran deneyimi sağlar",
                    checked = isFullScreen,
                    onCheckedChange = onFullScreenChange
                )

                Spacer(modifier = Modifier.height(6.dp))

                SettingsSwitchItem(
                    icon = if (floatingBottomBar) Icons.Default.Layers else Icons.Default.GridView,
                    title = if (floatingBottomBar) "Yüzen Ada Alt Bar" else "Sabit Alt Bar",
                    subtitle = if (floatingBottomBar) "Havada süzülen ada kapsülü görünümü (Açık: Yüzen Ada, Kapalı: Sabit Alt Bar)" else "Ekrana tam oturan standart sabit Material 3 alt gezinme çubuğu",
                    checked = floatingBottomBar,
                    onCheckedChange = onFloatingBottomBarChange
                )
            }
        }

        // Section 2: Homework Preferences
        item {
            SettingsCategoryCard(title = "Ödev Tercihleri") {
                SettingsSwitchItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "Tamamlananları Gizle",
                    subtitle = "Tamamlanan ödevler ana listede gizlensin",
                    checked = hideCompleted,
                    onCheckedChange = onHideCompletedChange
                )
            }
        }

        // Section 2.2: Not Tercihleri
        item {
            SettingsCategoryCard(title = "Not Tercihleri") {
                Column {
                    SettingsSwitchItem(
                        icon = Icons.Default.GridView,
                        title = "2 Sütunlu Kılavuz (Grid)",
                        subtitle = "Notlar çift sütunlu düzen veya tek sütun liste olarak gösterilsin",
                        checked = isNoteGrid,
                        onCheckedChange = onNoteGridChange
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Check,
                        title = "İşaretlenen Öğeleri En Alta Taşı",
                        subtitle = "Tamamlanan kontrol listesi maddeleri otomatik alta taşınsın",
                        checked = moveCheckedToBottom,
                        onCheckedChange = onMoveCheckedToBottomChange
                    )
                }
            }
        }

        // Section 2.5: Cloud Sync (Firebase)
        item {
            SettingsCategoryCard(title = "Bulut Senkronizasyonu (Firebase)") {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOnline) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOnline) "İnternet Bağlantısı Aktif" else "Çevrimdışı Mod",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        val syncStatusText = when (syncState) {
                            SyncState.SYNCED -> "Eşitlendi"
                            SyncState.SYNCING -> "Eşitleniyor..."
                            SyncState.ERROR -> "Hata"
                            SyncState.OFFLINE -> "Çevrimdışı"
                            SyncState.IDLE -> "Hazır"
                        }
                        val syncStatusColor = when (syncState) {
                            SyncState.SYNCED -> Color(0xFF10B981)
                            SyncState.SYNCING -> Color(0xFFF59E0B)
                            SyncState.ERROR -> MaterialTheme.colorScheme.error
                            SyncState.OFFLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                            SyncState.IDLE -> MaterialTheme.colorScheme.primary
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = syncStatusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = syncStatusText,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = syncStatusColor),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (syncState == SyncState.ERROR && syncErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = syncErrorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onManualSync,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Şimdi Bulut ile Eşitle",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Section: Tufi AI & Yapay Zeka Ayarları
        item {
            var showAiSettingsDialog by remember { mutableStateOf(false) }
            val isAiConfigured = remember { TufiAIService.isApiKeyConfigured(context) }

            SettingsCategoryCard(title = "Yapay Zeka (Tufi AI)") {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showAiSettingsDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isAiConfigured) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.18f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isAiConfigured) Color(0xFF059669) else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Tufi AI & API Anahtarı Ayarları",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isAiConfigured) "🟢 Canlı API Bağlantısı Aktif" else "⚠️ API Anahtarı Tanımlı Değil (Çevrimdışı Mod)",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isAiConfigured) Color(0xFF065F46) else Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = "Düzenle ❯",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (showAiSettingsDialog) {
                TufiAISettingsDialog(
                    onDismiss = { showAiSettingsDialog = false }
                )
            }
        }

        // Section: Ses Tanıma & Cihaz İçi Whisper
        item {
            var selectedModel by remember { mutableStateOf(SpeechSettingsManager.getSelectedModel(context)) }
            var isAiCleanupEnabled by remember { mutableStateOf(SpeechSettingsManager.isAiCleanupEnabled(context)) }
            var downloadProgress by remember { mutableStateOf<Float?>(null) }
            var downloadStatus by remember { mutableStateOf("") }
            var showDeleteConfirmDialog by remember { mutableStateOf(false) }
            var refreshTrigger by remember { mutableStateOf(0) }

            val scope = rememberCoroutineScope()

            val isSelectedModelDownloaded = remember(selectedModel, refreshTrigger) {
                ModelManager.isModelDownloaded(context, selectedModel)
            }
            val downloadedSizeText = remember(selectedModel, refreshTrigger) {
                ModelManager.getDownloadedSizeFormatted(context, selectedModel)
            }

            SettingsCategoryCard(title = "Ses Tanıma (Cihaz İçi Whisper)") {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Çevrimdışı Türkçe Ses Tanıma",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Ses kayıtlarınız internete gitmeden cihazınızda işlenir.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Model Seçimi:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Model Selection Cards (Small vs Base)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Small Option
                        val isSmallSelected = selectedModel == WhisperModelType.SMALL
                        val isSmallDownloaded = remember(refreshTrigger) { ModelManager.isModelDownloaded(context, WhisperModelType.SMALL) }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedModel = WhisperModelType.SMALL
                                    SpeechSettingsManager.setSelectedModel(context, WhisperModelType.SMALL)
                                    WhisperEngine.release()
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSmallSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.5.dp,
                                if (isSmallSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Small",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSmallSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isSmallDownloaded) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "İndirildi",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Yüksek Doğruluk (~240 MB)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSmallSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSmallSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Varsayılan",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Base Option
                        val isBaseSelected = selectedModel == WhisperModelType.BASE
                        val isBaseDownloaded = remember(refreshTrigger) { ModelManager.isModelDownloaded(context, WhisperModelType.BASE) }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedModel = WhisperModelType.BASE
                                    SpeechSettingsManager.setSelectedModel(context, WhisperModelType.BASE)
                                    WhisperEngine.release()
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isBaseSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                1.5.dp,
                                if (isBaseSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Base",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isBaseSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isBaseDownloaded) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "İndirildi",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Daha Hızlı (~75 MB)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isBaseSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "Hafif Cihazlar",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Download / Status Row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${selectedModel.displayName} Durumu",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = if (isSelectedModelDownloaded) "🟢 Cihazda İndirildi ($downloadedSizeText)" else "⚪ Henüz indirilmedi",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelectedModelDownloaded) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelectedModelDownloaded) {
                                    OutlinedButton(
                                        onClick = { showDeleteConfirmDialog = true },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sil", style = MaterialTheme.typography.labelSmall)
                                    }
                                } else if (downloadProgress == null) {
                                    Button(
                                        onClick = {
                                            downloadProgress = 0f
                                            downloadStatus = "Bağlantı kuruluyor..."
                                            scope.launch {
                                                val result = ModelManager.downloadModel(context, selectedModel) { prog, msg ->
                                                    downloadProgress = prog
                                                    downloadStatus = msg
                                                }
                                                downloadProgress = null
                                                if (result.isSuccess) {
                                                    refreshTrigger++
                                                    Toast.makeText(context, "${selectedModel.displayName} modeli başarıyla indirildi!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "İndirme başarısız", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Modeli İndir", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                }
                            }

                            // Download Progress Indicator
                            if (downloadProgress != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                LinearProgressIndicator(
                                    progress = { downloadProgress ?: 0f },
                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = downloadStatus,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Switch: AI Post-processing (Metni yapay zekayla düzelt)
                    SettingsSwitchItem(
                        icon = Icons.Default.AutoAwesome,
                        title = "Metni Yapay Zekayla Düzelt",
                        subtitle = "Dikte bittikten sonra noktalama işaretlerini, büyük harfleri ve gereksiz konuşma dolgularını (ııı, şey) Tufi AI ile düzeltir.",
                        checked = isAiCleanupEnabled,
                        onCheckedChange = { checked ->
                            isAiCleanupEnabled = checked
                            SpeechSettingsManager.setAiCleanupEnabled(context, checked)
                        }
                    )
                }
            }

            // Delete Model Confirmation Dialog
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    title = { Text("Modeli Sil") },
                    text = { Text("${selectedModel.displayName} modeli cihazınızdan silinecek. İleride ses tanımayı kullanmak için modeli tekrar indirmeniz gerekecektir.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                WhisperEngine.release()
                                ModelManager.deleteModel(context, selectedModel)
                                refreshTrigger++
                                showDeleteConfirmDialog = false
                                Toast.makeText(context, "${selectedModel.displayName} modeli silindi.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Sil", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Vazgeç")
                        }
                    }
                )
            }
        }

        // Section 3: About
        item {
            SettingsCategoryCard(title = "Uygulama Hakkında") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (!isDeveloperModeActive) {
                                versionTapCount++
                                if (versionTapCount >= 5) {
                                    isDeveloperModeActive = true
                                    Toast.makeText(context, "🎉 Geliştirici Seçenekleri Aktif Edildi!", Toast.LENGTH_SHORT).show()
                                } else {
                                    val remaining = 5 - versionTapCount
                                    Toast.makeText(context, "Geliştirici modu için $remaining dokunuş kaldı", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                showDevOptionsDialog = true
                            }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TufiDESK",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sürüm $appVersion • Material Design 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (isDeveloperModeActive) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "DEV",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Geliştirici Ayarları Giriş Kartı
        item {
            SettingsCategoryCard(title = "Gelişmiş Seçenekler") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDeveloperSettingsScreen = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeveloperMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Geliştirici Ayarları",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Firebase sıfırlama, demo veriler, JSON içe/dışa aktar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Geliştirici Ayarlarını Aç",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

    // Theme Mode Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Tema Modu Seçin") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val options = listOf(
                        "SYSTEM" to "Sistem Teması (Otomatik)",
                        "LIGHT" to "Açık Tema",
                        "DARK" to "Koyu Tema"
                    )
                    options.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onThemeModeChange(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (currentThemeMode == mode) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentThemeMode == mode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (currentThemeMode == mode) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // Material Color Palette Selection Dialog
    if (showPaletteDialog) {
        AlertDialog(
            onDismissRequest = { showPaletteDialog = false },
            title = { Text("Materyal Renk Paleti") },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(paletteOptions.size) { index ->
                        val option = paletteOptions[index]
                        val isSelected = currentColorPalette == option.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    onColorPaletteChange(option.id)
                                    showPaletteDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color Preview Swatch
                            if (option.isDynamic) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(option.primaryColor)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(option.secondaryColor)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = option.description,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPaletteDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // Developer Options & Sync Diagnostics Dialog
    if (showDevOptionsDialog) {
        val isFirebaseInitialized = remember(context) {
            try {
                FirebaseApp.getApps(context).isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        val diagnosticSummary = buildString {
            appendLine("=== TUFIDESK SENKRONİZASYON VE HATA RAPORU ===")
            appendLine("Tarih: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine("Uygulama Sürümü: $appVersion")
            appendLine("Paket Adı: ${context.packageName}")
            appendLine("----------------------------------------")
            appendLine("1. FİREBASE DURUMU:")
            appendLine("   - FirebaseApp Başlatıldı: ${if (isFirebaseInitialized) "EVET (Aktif)" else "HAYIR (google-services.json eksik veya başlatılamadı)"}")
            appendLine("   - Proje Yapılandırması: ${if (isFirebaseInitialized) "google-services.json yüklü" else "Eksik"}")
            appendLine()
            appendLine("2. AĞ VE İNTERNET:")
            appendLine("   - İnternet Erişimi: ${if (isOnline) "ONLINE (Bağlantı Var)" else "OFFLINE (Bağlantı Yok)"}")
            appendLine()
            appendLine("3. SENKRONİZASYON DURUMU:")
            appendLine("   - Durum Kodu: ${syncState.name}")
            appendLine("   - Hata Mesajı: ${syncErrorMessage ?: "Kayıtlı bir hata yok."}")
            appendLine("----------------------------------------")
            if (!isFirebaseInitialized) {
                appendLine("👉 TEŞHİS: google-services.json dosyası APK içinde bulunamadı. GitHub Actions'ta bu dosyayı derleme sürecine ekleyin.")
            } else if (syncErrorMessage?.contains("PERMISSION_DENIED", ignoreCase = true) == true) {
                appendLine("👉 TEŞHİS: Firestore Güvenlik Kuralları kilitli! Firebase Console > Firestore Database > Kurallar (Rules) sekmesinde 'allow read, write: if true;' izni vermelisiniz.")
            } else if (syncErrorMessage?.contains("NOT_FOUND", ignoreCase = true) == true) {
                appendLine("👉 TEŞHİS: Firebase projesinde Cloud Firestore veritabanı henüz oluşturulmamış. Firebase Console'dan veritabanını oluşturun.")
            } else if (syncErrorMessage?.contains("UNAVAILABLE", ignoreCase = true) == true) {
                appendLine("👉 TEŞHİS: Firebase sunucularına erişilemiyor. İnternet bağlantınızı, DNS veya operatör kısıtlamalarını kontrol edin.")
            } else if (!isOnline) {
                appendLine("👉 TEŞHİS: Telefonunuz çevrimdışı modda. Veriler yerel Room veritabanında güvenle saklanıyor.")
            } else {
                appendLine("👉 TEŞHİS: Sistem sağlıklı ve senkronizasyon çalışıyor.")
            }
        }

        AlertDialog(
            onDismissRequest = { showDevOptionsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Geliştirici Teşhis Paneli", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Firebase Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFirebaseInitialized) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, if (isFirebaseInitialized) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isFirebaseInitialized) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = if (isFirebaseInitialized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isFirebaseInitialized) "Firebase Bağlantısı: Hazır ✅" else "Firebase Başlatılamadı ❌",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = if (isFirebaseInitialized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isFirebaseInitialized) "google-services.json yüklü ve Firestore motoru aktif." else "google-services.json dosyası APK'ya dahil edilmemiş. GitHub Actions build sürecini kontrol edin.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isFirebaseInitialized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    // Network & Sync State
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ağ Durumu:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(if (isOnline) "Çevrimiçi (Online) 🟢" else "Çevrimdışı (Offline) 🔴", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Senkronizasyon:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(syncState.name, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = if (syncState == SyncState.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Paket Kimliği:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(context.packageName, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    // Error Log / Analysis
                    if (!syncErrorMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Son Hata Kaydı:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = syncErrorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Recommendations
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "💡 Teşhis & Çözüm İpucu:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val advice = when {
                                !isFirebaseInitialized -> "GitHub Actions ile derlenen APK'ya google-services.json dosyası eklenmemiş. GitHub reponuza veya GitHub Secret olarak google-services.json ekleyin."
                                syncErrorMessage?.contains("PERMISSION_DENIED", ignoreCase = true) == true -> "Firebase Console > Firestore > Kurallar (Rules) sekmesinde izinleri 'allow read, write: if true;' olarak güncelleyin."
                                syncErrorMessage?.contains("NOT_FOUND", ignoreCase = true) == true -> "Firebase projenizde Cloud Firestore henüz oluşturulmamış. Firebase Console'dan 'Create database' yapın."
                                syncErrorMessage?.contains("UNAVAILABLE", ignoreCase = true) == true -> "Firestore sunucularına ulaşılamadı. Farklı bir Wi-Fi veya mobil internet ile tekrar deneyin."
                                !isOnline -> "Cihazınızda internet bağlantısı yok. Yerel Room veritabanı aktif şekilde çalışıyor."
                                else -> "Firebase ve senkronizasyon sağlıklı çalışıyor. Herhangi bir blokaj yok."
                            }
                            Text(
                                text = advice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("TufiDESK Teşhis Raporu", diagnosticSummary)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Teşhis raporu panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kopyala", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = {
                                onManualSync()
                                Toast.makeText(context, "Eşitleme tetiklendi...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Et", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Button(
                        onClick = {
                            showDevOptionsDialog = false
                            CrashActivity.start(
                                context,
                                RuntimeException("MANUEL GELİŞTİRİCİ ÇÖKME TESTİ:\nGeliştirici Teşhis Panelinden BSOD ekranı tetiklendi.\nHata Kodu: CRITICAL_PROCESS_DIED (0x000000EF)")
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D7)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("💥 Manuel Çökme & BSOD Testi", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }

                    TextButton(
                        onClick = {
                            isDeveloperModeActive = false
                            versionTapCount = 0
                            showDevOptionsDialog = false
                            Toast.makeText(context, "Geliştirici modu kapatıldı", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Geliştirici Modunu Kapat", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDevOptionsDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // 0. Clear All Firebase Confirmation Dialog (Tamamen Sil ve Boşalt)
    if (showClearFirebaseAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isClearingFirebase) showClearFirebaseAllConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Firebase'deki Tüm Veriler Kalıcı Olarak Silinsin mi?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bu işlem buluttaki (Firestore) ve yerel cihazdaki TÜM ödevleri, dersleri, notları ve klasörleri kalıcı olarak silecek ve veritabanını tamamen boş bırakacaktır.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Demo veri YÜKLENMEZ. Tüm veritabanı sıfırlanır ve tertemiz boş bırakılır.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                    if (isClearingFirebase) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Firebase'deki tüm veriler siliniyor...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isClearingFirebase = true
                        onClearAllFirebaseData { success, message ->
                            isClearingFirebase = false
                            showClearFirebaseAllConfirmDialog = false
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isClearingFirebase,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Hepsini Sil (Boşalt)", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearFirebaseAllConfirmDialog = false },
                    enabled = !isClearingFirebase
                ) {
                    Text("İptal")
                }
            }
        )
    }

    // 1. Reset Firebase Confirmation Dialog
    if (showResetFirebaseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isResettingFirebase) showResetFirebaseConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Firebase'deki Tüm Veriler Silinsin mi?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Bu işlem buluttaki (Firestore) TÜM ödevleri, dersleri, notları ve klasörleri kalıcı olarak temizleyecektir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Ardından uygulama için gerekli ve geçerli kurallara (haftalık Pazar teslimatlı ödevler, dersler, notlar) sahip temiz demo veriler Firebase'e yüklenecektir.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isResettingFirebase) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Firebase temizleniyor ve demo veriler yükleniyor...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isResettingFirebase = true
                        onResetFirebaseData { success, message ->
                            isResettingFirebase = false
                            showResetFirebaseConfirmDialog = false
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isResettingFirebase,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Evet, Sıfırla ve Yükle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetFirebaseConfirmDialog = false },
                    enabled = !isResettingFirebase
                ) {
                    Text("İptal")
                }
            }
        )
    }

    // 2. Export JSON Dialog
    if (showExportJsonDialog) {
        AlertDialog(
            onDismissRequest = { showExportJsonDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Firebase JSON Dışa Aktarma",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Firebase ve yerel veritabanındaki veriler JSON formatında hazırlandı:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = exportedJsonText,
                                    color = Color(0xFFF1F1F1),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("TufiDESK JSON Export", exportedJsonText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "JSON panoya kopyalandı!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Kopyala", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                try {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, exportedJsonText)
                                        type = "application/json"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "JSON Paylaş"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Paylaşım başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paylaş", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportJsonDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }

    // 3. Import JSON Dialog
    if (showImportJsonDialog) {
        AlertDialog(
            onDismissRequest = { if (!isImportingJson) showImportJsonDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Firebase JSON İçe Aktar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Daha önce dışa aktarılmış veya geçerli JSON metnini aşağıya yapıştırın. Veriler Firebase Firestore'a ve yerel veritabanınıza yazılacaktır:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString()
                            if (!clipText.isNullOrBlank()) {
                                importJsonInput = clipText
                                Toast.makeText(context, "Panodan yapıştırıldı", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Pano boş!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Panodan Yapıştır", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedTextField(
                        value = importJsonInput,
                        onValueChange = { importJsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("{\"courses\": [...], \"homeworks\": [...]}") },
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                    )

                    if (isImportingJson) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("JSON ayrıştırılıyor ve Firebase'e yazılıyor...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (importJsonInput.isBlank()) {
                            Toast.makeText(context, "Lütfen geçerli bir JSON metni yapıştırın", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isImportingJson = true
                        onImportFirebaseJson(importJsonInput) { success, message ->
                            isImportingJson = false
                            if (success) {
                                showImportJsonDialog = false
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isImportingJson && importJsonInput.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("İçe Aktar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportJsonDialog = false },
                    enabled = !isImportingJson
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

@Composable
private fun SettingsCategoryCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDestructive: Boolean = false
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun DeveloperSettingsContent(
    onNavigateBack: () -> Unit,
    isOnline: Boolean,
    syncState: SyncState,
    syncErrorMessage: String?,
    onClearAllFirebaseClick: () -> Unit,
    onResetFirebaseClick: () -> Unit,
    onExportJsonClick: () -> Unit,
    isExportingJson: Boolean,
    onImportJsonClick: () -> Unit,
    onOpenDiagnosticsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Navigation Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Ayarlara Geri Dön",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Geliştirici Ayarları",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Firebase Veritabanı & Geliştirici Araçları",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Info Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isOnline) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnline) "Bulut Bağlantısı Aktif (Online)" else "Çevrimdışı (Offline)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Durum: ${syncState.name} ${if (syncErrorMessage != null) "• $syncErrorMessage" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 1. Firebase Reset & Demo Seed
            item {
                SettingsCategoryCard(title = "🔥 Firebase Veritabanı Sıfırlama & Temizleme") {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Firebase Bulut Veritabanı İşlemleri",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Firebase Firestore bulut veritabanındaki koleksiyonları (ödevler, dersler, notlar, klasörler) yönetebilir veya tamamen silebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 1. TAMAMEN SİL VE BOŞALT BUTONU
                        Button(
                            onClick = onClearAllFirebaseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Firebase'deki Tüm Verileri Sil (Boşalt)", fontWeight = FontWeight.Bold)
                        }

                        // 2. SIFIRLA VE DEMO VERİLERİ YÜKLE
                        Button(
                            onClick = onResetFirebaseClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Firebase'i Sıfırla ve Demo Verileri Yükle", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. JSON Export & Import
            item {
                SettingsCategoryCard(title = "📦 JSON Veri Yedekleme & Geri Yükleme") {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Export
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isExportingJson, onClick = onExportJsonClick)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Firebase Verilerini Dışa Aktar (JSON)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tüm ders, ödev ve not verilerini JSON formatında dışa aktarın, kopyalayın veya paylaşın.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isExportingJson) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                }
                            }
                        }

                        // Import
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onImportJsonClick)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Firebase Verilerini İçe Aktar (JSON)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "JSON yedeğini yapıştırarak Firebase'e ve yerel veritabanına geri yükleyin.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Diagnostics Panel
            item {
                SettingsCategoryCard(title = "🔍 Teşhis & Test") {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Bağlantı & Senkronizasyon Teşhis Paneli",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Firebase bağlantı testleri, hata logları, paket bilgileri ve sistem teşhis raporu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = onOpenDiagnosticsClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teşhis Raporunu Görüntüle", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // 4. Manuel Çökme & BSOD Testi
            item {
                SettingsCategoryCard(title = "💥 Çökme & BSOD Ekranı Testi") {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = ":( Windows BSOD & Hata Ekranı",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color(0xFF0078D7)
                        )
                        Text(
                            text = "Çökme ekranını ve Windows temalı log kopyalama arayüzünü görmek için manuel olarak çökme tetikleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                CrashActivity.start(
                                    context,
                                    RuntimeException("MANUEL GELİŞTİRİCİ ÇÖKME TESTİ:\nKullanıcı Geliştirici Ayarlarından BSOD ekranını test etmek için manuel çökme tetikledi.\nHata Kodu: STATUS_SYSTEM_PROCESS_TERMINATED (0xC000021A)\nModül: com.example.ui.screens.SettingsScreen")
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0078D7)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manuel Çökmeyi Başlat (BSOD Test)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
