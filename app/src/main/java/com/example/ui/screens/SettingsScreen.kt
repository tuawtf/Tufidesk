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
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.verticalScroll
import com.example.data.firebase.SyncState
import com.example.data.model.UserAccount
import com.example.ui.components.UserFlowerAvatar
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.OutlinedButton

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
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPaletteDialog by remember { mutableStateOf(false) }
    var versionTapCount by remember { mutableStateOf(0) }
    var isDeveloperModeActive by rememberSaveable { mutableStateOf(false) }
    var showDevOptionsDialog by remember { mutableStateOf(false) }

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

        // Section 4: Developer Options & Diagnostics (Active after 5 taps on version)
        if (isDeveloperModeActive) {
            item {
                SettingsCategoryCard(title = "🛠️ Geliştirici Seçenekleri & Teşhis") {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeveloperMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Senkronizasyon ve Hata Teşhisi",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Bulut senkronizasyonunun neden başarısız olduğunu, Firebase bağlantısını ve hata loglarını canlı olarak inceleyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showDevOptionsDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BugReport,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hata ve Teşhis Paneli")
                            }
                        }
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
