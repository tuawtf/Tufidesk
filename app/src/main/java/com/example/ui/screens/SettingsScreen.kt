package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.CrashActivity
import com.example.data.ai.TufiAIService
import com.example.data.firebase.SyncState
import com.example.data.model.UserAccount
import com.example.ui.components.TufiAISettingsDialog
import com.example.ui.components.UserFlowerAvatar
import com.example.ui.screens.settings.SearchableSettingItem
import com.example.ui.screens.settings.SettingsSection
import com.example.ui.screens.speech.SpeechSettingsSection
import com.example.ui.screens.cloud.R2StorageSettingsCard
import com.example.ui.HomeworkViewModel
import com.google.firebase.FirebaseApp

sealed class UpdateCheckResult {
    data class NewVersion(val version: String, val releaseUrl: String) : UpdateCheckResult()
    data class UpToDate(val version: String) : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

data class MaterialPaletteOption(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val isDynamic: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
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
    lastSyncTime: Long? = null,
    syncErrorMessage: String? = null,
    onClose: () -> Unit = {},
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
    viewModel: HomeworkViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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

    // Güncelleme kontrolü durumu
    var isCheckingUpdate by remember { mutableStateOf(false) }
    var updateCheckResult by remember { mutableStateOf<UpdateCheckResult?>(null) }

    fun checkForUpdates() {
        isCheckingUpdate = true
        coroutineScope.launch {
            try {
                val (latestTag, htmlUrl) = withContext(Dispatchers.IO) {
                    val url = java.net.URL("https://api.github.com/repos/tuawtf/Tufidesk/releases/latest")
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Accept", "application/vnd.github.v3+json")
                    conn.setRequestProperty("User-Agent", "Tufidesk-Android-App")
                    conn.connectTimeout = 8000
                    conn.readTimeout = 8000
                    val responseCode = conn.responseCode
                    if (responseCode == 200) {
                        val stream = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = org.json.JSONObject(stream)
                        val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                        val urlStr = json.optString("html_url", "https://github.com/tuawtf/Tufidesk/releases")
                        Pair(tagName, urlStr)
                    } else {
                        throw Exception("Sunucu yanıt vermedi (Kod: $responseCode)")
                    }
                }
                val currentClean = appVersion.removePrefix("v").trim()
                if (latestTag.isNotEmpty() && latestTag != currentClean) {
                    updateCheckResult = UpdateCheckResult.NewVersion(latestTag, htmlUrl)
                } else {
                    updateCheckResult = UpdateCheckResult.UpToDate(appVersion)
                }
            } catch (e: Exception) {
                updateCheckResult = UpdateCheckResult.Error("Güncelleme kontrolü başarısız oldu: ${e.localizedMessage ?: "Bağlantı hatası"}")
            } finally {
                isCheckingUpdate = false
            }
        }
    }

    // Navigasyon State: Seçili kategori (null ise ana ayarlar listesi)
    var selectedSection by rememberSaveable { mutableStateOf<String?>(null) }
    var showDeveloperSettingsScreen by rememberSaveable { mutableStateOf(false) }

    // Arama state
    var searchQuery by rememberSaveable { mutableStateOf("") }

    // Dialog State'leri
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPaletteDialog by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableIntStateOf(0) }
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

    var showAiSettingsDialog by remember { mutableStateOf(false) }

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
        ),
        MaterialPaletteOption(
            id = "TWILIGHT",
            name = "Twilight Sparkle",
            description = "Gece moru, canlı fuşya ve lila tonları",
            primaryColor = Color(0xFFF42B76),
            secondaryColor = Color(0xFF382956)
        )
    )

    // Aranabilir ayar öğeleri dizini
    val searchableItems = remember {
        listOf(
            SearchableSettingItem("Aktif Profil", "Öğrenci hesabı ve profil değiştirme", SettingsSection.PROFILE, Icons.Default.SwapHoriz, listOf("hesap", "öğrenci", "profil")),
            SearchableSettingItem("Uygulama Teması", "Açık, koyu veya sistem teması", SettingsSection.APPEARANCE, Icons.Default.BrightnessAuto, listOf("karanlık", "aydınlık", "dark", "light", "renk")),
            SearchableSettingItem("Materyal Renk Paleti", "Material You dinamik renkler veya özel temalar", SettingsSection.APPEARANCE, Icons.Default.Palette, listOf("palet", "renk", "dynamic", "mavi", "yeşil", "indigo")),
            SearchableSettingItem("Tam Ekran Yap", "Durum ve gezinme çubuklarını gizler", SettingsSection.APPEARANCE, Icons.Default.Fullscreen, listOf("statusbar", "odaklanma", "ekran")),
            SearchableSettingItem("Alt Gezinme Barı Görünümü", "Yüzen ada kapsülü veya sabit alt bar", SettingsSection.APPEARANCE, Icons.Default.Layers, listOf("ada", "navigasyon", "bar")),
            SearchableSettingItem("Tamamlananları Gizle", "Tamamlanan ödevler ana listede gizlensin", SettingsSection.HOMEWORK, Icons.Default.VisibilityOff, listOf("ödev", "gizle", "tamamlanan")),
            SearchableSettingItem("2 Sütunlu Kılavuz (Grid)", "Notlar çift sütunlu düzen veya tek sütun liste", SettingsSection.NOTES, Icons.Default.GridView, listOf("not", "ızgara", "sütun", "liste")),
            SearchableSettingItem("İşaretlenen Öğeleri En Alta Taşı", "Tamamlanan kontrol listesi maddeleri otomatik alta taşınsın", SettingsSection.NOTES, Icons.Default.Check, listOf("madde", "kontrol", "yapılacak")),
            SearchableSettingItem("Tufi AI & API Anahtarı Ayarları", "DeepSeek / Gemini API anahtarı ve canlı model", SettingsSection.AI_AND_SPEECH, Icons.Default.AutoAwesome, listOf("ai", "yapay zeka", "tufi", "deepseek", "gemini", "api", "anahtar")),
            SearchableSettingItem("Sesli Dikte (Whisper)", "Bulut ve çevrimdışı Whisper ses-yazı tanıma motoru", SettingsSection.AI_AND_SPEECH, Icons.Default.AutoAwesome, listOf("ses", "dikte", "whisper", "mikrofon", "konuşma", "çevrimdışı")),
            SearchableSettingItem("Bulut Senkronizasyonu (Firebase)", "İnternet bağlantısı ve anlık senkronizasyon durumu", SettingsSection.DATA_AND_SYNC, Icons.Default.CloudSync, listOf("firebase", "bulut", "eşitleme", "online", "sync")),
            SearchableSettingItem("Bulut Depolama (Cloudflare R2)", "Not görselleri için R2 Worker URL ve Token ayarları", SettingsSection.DATA_AND_SYNC, Icons.Default.CloudUpload, listOf("r2", "cloudflare", "worker", "depolama", "resim", "fotoğraf", "token", "cloud")),
            SearchableSettingItem("JSON Veri Yedekleme & Geri Yükleme", "Ders, ödev ve notları JSON ile dışa/içe aktar", SettingsSection.DATA_AND_SYNC, Icons.Default.CloudDownload, listOf("yedek", "json", "dışa aktar", "içe aktar", "export", "import")),
            SearchableSettingItem("Firebase Veritabanı Sıfırlama", "Buluttaki verileri silme veya demo yükleme", SettingsSection.DATA_AND_SYNC, Icons.Default.DeleteForever, listOf("sıfırla", "sil", "temizle", "firestore")),
            SearchableSettingItem("Geliştirici Ayarları & Teşhis Paneli", "Firebase logları, sistem teşhis raporu", SettingsSection.ADVANCED, Icons.Default.DeveloperMode, listOf("dev", "teşhis", "log", "hata", "bağlantı")),
            SearchableSettingItem("Çökme & BSOD Testi", "Windows temalı mavi ekran ve hata logu testi", SettingsSection.ADVANCED, Icons.Default.ErrorOutline, listOf("çökme", "crash", "bsod", "test")),
            SearchableSettingItem("Uygulama Hakkında", "TufiDESK sürümü ve geliştirici modu", SettingsSection.ABOUT, Icons.Default.Info, listOf("sürüm", "version", "tufidesk", "hakkında", "bilgi"))
        )
    }

    val filteredSearchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            searchableItems.filter { item ->
                item.title.lowercase().contains(q) ||
                item.subtitle.lowercase().contains(q) ||
                item.section.title.lowercase().contains(q) ||
                item.keywords.any { it.contains(q) }
            }
        }
    }

    // Geliştirici ayarları tam ekranı açıksa
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
        return
    }

    // Geri tuşu yönetimi: Kategori alt ekranındaysak ana ayarlara dön
    BackHandler(enabled = selectedSection != null) {
        selectedSection = null
    }

    // Kategori içerik lambda'sı
    @Composable
    fun RenderCategoryContent(section: SettingsSection) {
        when (section) {
            SettingsSection.PROFILE -> {
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
                                    contentDescription = "Profili Değiştir",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Değiştir", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
            SettingsSection.APPEARANCE -> {
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
            SettingsSection.HOMEWORK -> {
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
            SettingsSection.NOTES -> {
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
            SettingsSection.AI_AND_SPEECH -> {
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
                                        contentDescription = "Tufi AI Durumu",
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

                Spacer(modifier = Modifier.height(16.dp))

                // Sesli Dikte (Bulut Whisper & Çevrimdışı Whisper)
                SpeechSettingsSection()
            }
            SettingsSection.DATA_AND_SYNC -> {
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
                                    contentDescription = if (isOnline) "İnternet aktif" else "İnternet yok",
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
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

                                if (lastSyncTime != null && lastSyncTime > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    val formattedLastSync = remember(lastSyncTime) {
                                        val now = java.util.Calendar.getInstance()
                                        val syncCal = java.util.Calendar.getInstance().apply { timeInMillis = lastSyncTime }
                                        val timeFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTime))
                                        if (now.get(java.util.Calendar.YEAR) == syncCal.get(java.util.Calendar.YEAR) &&
                                            now.get(java.util.Calendar.DAY_OF_YEAR) == syncCal.get(java.util.Calendar.DAY_OF_YEAR)) {
                                            "Son eşitleme: bugün $timeFormat"
                                        } else {
                                            val dateFormat = java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTime))
                                            "Son eşitleme: $dateFormat"
                                        }
                                    }
                                    Text(
                                        text = formattedLastSync,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
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
                                        contentDescription = "Senkronizasyon uyarısı",
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
                                contentDescription = "Bulut ile Eşitle",
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

                Spacer(modifier = Modifier.height(16.dp))

                // JSON Yedekleme & Geri Yükleme Kartı
                SettingsCategoryCard(title = "📦 JSON Veri Yedekleme & Geri Yükleme") {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isExportingJson) {
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
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Verileri Dışa Aktar",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Verileri Dışa Aktar (JSON)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tüm ders, ödev ve not verilerini JSON formatında dışa aktarın veya paylaşın.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isExportingJson) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    importJsonInput = ""
                                    showImportJsonDialog = true
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = "Verileri İçe Aktar",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(26.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Verileri İçe Aktar (JSON)",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "JSON yedeğini yapıştırarak veritabanına geri yükleyin.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Bulut Depolama (Cloudflare R2) Kartı
                if (viewModel != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    R2StorageSettingsCard(viewModel = viewModel)
                }
            }
            SettingsSection.ADVANCED -> {
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
                                contentDescription = "Geliştirici Modu",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Geliştirici Ayarları Paneli",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Firebase sıfırlama, tam temizlik, test verileri ve gelişmiş araçlar",
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

                Spacer(modifier = Modifier.height(16.dp))

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
                            onClick = { showDevOptionsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.BugReport, contentDescription = "Teşhis Raporu", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Teşhis Raporunu Görüntüle", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = "BSOD Testi", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Manuel Çökmeyi Başlat (BSOD Test)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            SettingsSection.ABOUT -> {
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
                            contentDescription = "Uygulama Bilgisi",
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

                    Spacer(modifier = Modifier.height(6.dp))

                    SettingsClickableItem(
                        title = "Güncellemeleri Kontrol Et",
                        subtitle = if (isCheckingUpdate) "Kontrol ediliyor..." else "En son sürümü GitHub üzerinden denetle",
                        icon = Icons.Default.CloudDownload,
                        onClick = { if (!isCheckingUpdate) checkForUpdates() }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    SettingsClickableItem(
                        title = "GitHub'da Görüntüle",
                        subtitle = "tuawtf/Tufidesk açık kaynak projesi",
                        icon = Icons.Default.Code,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tuawtf/Tufidesk"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }

    // Arama Çubuğu Composable
    @Composable
    fun SearchBarHeader(modifier: Modifier = Modifier) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Ayarlarda ara...", style = MaterialTheme.typography.bodyMedium) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Arama",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Aramayı Temizle",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }

    // Kategori Listesi Composable
    @Composable
    fun CategoryListPane(
        selectedSectionName: String?,
        onSectionClick: (SettingsSection) -> Unit,
        modifier: Modifier = Modifier
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(SettingsSection.entries.toTypedArray()) { section ->
                val isSelected = selectedSectionName == section.name
                val containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color(0xFF1E1D3B)
                }
                val cardBorder = if (isSelected) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                } else {
                    BorderStroke(1.dp, Color(0xFF2E2C56))
                }

                Card(
                    onClick = { onSectionClick(section) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    border = cardBorder,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = {
                            Text(
                                text = section.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                            )
                        },
                        supportingContent = {
                            Text(
                                text = section.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f) else Color(0xFFB8B0D0)
                            )
                        },
                        leadingContent = {
                            Surface(
                                shape = CircleShape,
                                color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = section.icon,
                                        contentDescription = section.title,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Aç",
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFB8B0D0),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }
        }
    }

    // Arama Sonuçları Paneli Composable
    @Composable
    fun SearchResultsPane(
        results: List<SearchableSettingItem>,
        onResultClick: (SettingsSection) -> Unit,
        modifier: Modifier = Modifier
    ) {
        if (results.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Sonuç yok",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sonuç bulunamadı",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"$searchQuery\" ile eşleşen bir ayar bulunamadı.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results) { item ->
                    Card(
                        onClick = {
                            searchQuery = ""
                            onResultClick(item.section)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                )
                            },
                            supportingContent = {
                                Column {
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = item.section.title,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Git",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                        )
                    }
                }
            }
        }
    }

    // Genişlik Tespiti (Tablet / Telefon ayrımı)
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isExpandedTablet = maxWidth >= 680.dp

        if (isExpandedTablet) {
            // TABLET DÜZENİ: İki bölmeli (Sol kategori listesi, Sağ seçili kategori içeriği)
            val currentTabletSection = selectedSection?.let { sName ->
                try { SettingsSection.valueOf(sName) } catch (_: Exception) { SettingsSection.PROFILE }
            } ?: SettingsSection.PROFILE

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, bottom = 16.dp, end = 16.dp)
                ) {
                    // Sol Sütun: Kategoriler (Genişliği 320 dp)
                    Box(
                        modifier = Modifier
                            .width(320.dp)
                            .fillMaxHeight()
                    ) {
                        CategoryListPane(
                            selectedSectionName = currentTabletSection.name,
                            onSectionClick = { section -> selectedSection = section.name }
                        )
                    }

                    // Dikey ayırıcı çizgi
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .padding(vertical = 8.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )

                    // Sağ Sütun: Seçili Kategori İçeriği (Geniş ekranda maks 800 dp ortalanmış)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 800.dp)
                                .align(Alignment.TopCenter),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item {
                                Text(
                                    text = currentTabletSection.title,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            item {
                                RenderCategoryContent(currentTabletSection)
                            }
                        }
                    }
                }
            }
        } else {
            // TELEFON DÜZENİ: İki seviyeli (Ana Ekran -> Kategori Alt Ekranı)
            AnimatedContent(
                targetState = selectedSection,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> -width } + fadeOut()
                        )
                    } else {
                        (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                            slideOutHorizontally { width -> width } + fadeOut()
                        )
                    }
                },
                label = "SettingsScreenNavigation"
            ) { targetSectionName ->
                if (targetSectionName == null) {
                    // 1. ANA AYARLAR EKRANI
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "Ayarlar",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = onClose) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Geri",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { selectedSection = SettingsSection.ABOUT.name }) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Uygulama Hakkında",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            SearchBarHeader()

                            if (searchQuery.isNotBlank()) {
                                SearchResultsPane(
                                    results = filteredSearchResults,
                                    onResultClick = { section -> selectedSection = section.name }
                                )
                            } else {
                                CategoryListPane(
                                    selectedSectionName = null,
                                    onSectionClick = { section -> selectedSection = section.name }
                                )
                            }
                        }
                    }
                } else {
                    // 2. KATEGORİ ALT EKRANI
                    val section = try {
                        SettingsSection.valueOf(targetSectionName)
                    } catch (_: Exception) {
                        SettingsSection.PROFILE
                    }

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = section.title,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )
                                },
                                navigationIcon = {
                                    IconButton(onClick = { selectedSection = null }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Ayarlara Geri Dön",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    ) { innerPadding ->
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            item {
                                RenderCategoryContent(section)
                            }
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // DIALOGLAR (Mevcut Mantık Korundu)
    // ==========================================

    // 1. Tema Modu Dialogu
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
                                    contentDescription = "Seçili",
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

    // 2. Materyal Renk Paleti Dialogu
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
                                        contentDescription = "Dinamik Sistem",
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
                                    contentDescription = "Seçili",
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

    // 3. Teşhis Paneli Dialogu
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
                        contentDescription = "Hata Raporu",
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
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", modifier = Modifier.size(16.dp))
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
                            Icon(Icons.Default.Refresh, contentDescription = "Test Et", modifier = Modifier.size(16.dp))
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
                        Icon(Icons.Default.ErrorOutline, contentDescription = "BSOD", modifier = Modifier.size(16.dp))
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

    // 4. Clear All Firebase Dialog
    if (showClearFirebaseAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isClearingFirebase) showClearFirebaseAllConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Tümünü Sil",
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

    // 5. Reset Firebase Dialog
    if (showResetFirebaseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (!isResettingFirebase) showResetFirebaseConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Sıfırla",
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
                        text = "Ardından uygulama için gerekli temiz demo veriler Firebase'e yüklenecektir.",
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

    // 6. Export JSON Dialog
    if (showExportJsonDialog) {
        AlertDialog(
            onDismissRequest = { showExportJsonDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudDownload,
                    contentDescription = "Dışa Aktar",
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
                            Icon(Icons.Default.ContentCopy, contentDescription = "Kopyala", modifier = Modifier.size(16.dp))
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
                            Icon(Icons.Default.Share, contentDescription = "Paylaş", modifier = Modifier.size(16.dp))
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

    // 7. Import JSON Dialog
    if (showImportJsonDialog) {
        AlertDialog(
            onDismissRequest = { if (!isImportingJson) showImportJsonDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "İçe Aktar",
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
                        Icon(Icons.Default.ContentPaste, contentDescription = "Panodan Yapıştır", modifier = Modifier.size(16.dp))
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

    // 8. Tufi AI Ayarları Dialogu
    if (showAiSettingsDialog) {
        TufiAISettingsDialog(
            onDismiss = { showAiSettingsDialog = false }
        )
    }

    // 9. Güncelleme Kontrolü Dialogu
    updateCheckResult?.let { result ->
        when (result) {
            is UpdateCheckResult.NewVersion -> {
                AlertDialog(
                    onDismissRequest = { updateCheckResult = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    title = {
                        Text(
                            text = "Yeni Sürüm Mevcut!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Text(
                            text = "TufiDESK v${result.version} yayınlandı.\nMevcut sürümünüz: v$appVersion.\n\nİndirmek için GitHub release sayfasını açabilirsiniz.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.releaseUrl))
                                context.startActivity(intent)
                                updateCheckResult = null
                            }
                        ) {
                            Text("İndir")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { updateCheckResult = null }) {
                            Text("Daha Sonra")
                        }
                    }
                )
            }
            is UpdateCheckResult.UpToDate -> {
                AlertDialog(
                    onDismissRequest = { updateCheckResult = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981)
                        )
                    },
                    title = {
                        Text(
                            text = "Uygulamanız Güncel",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Text(
                            text = "En son sürümü (v${result.version}) kullanıyorsunuz.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { updateCheckResult = null }) {
                            Text("Tamam")
                        }
                    }
                )
            }
            is UpdateCheckResult.Error -> {
                AlertDialog(
                    onDismissRequest = { updateCheckResult = null },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(
                            text = "Güncelleme Kontrolü",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    text = {
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { updateCheckResult = null }) {
                            Text("Tamam")
                        }
                    }
                )
            }
        }
    }
}

// ==========================================
// YARDIMCI BİLEŞENLER
// ==========================================

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
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1A36)),
            border = BorderStroke(1.dp, Color(0xFF2E2C56)),
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
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
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
    val context = LocalContext.current

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
