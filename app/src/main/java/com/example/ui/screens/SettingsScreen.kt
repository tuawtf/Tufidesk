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
    showWordCount: Boolean = true,
    onShowWordCountChange: (Boolean) -> Unit = {},
    confirmDeleteNote: Boolean = true,
    onConfirmDeleteNoteChange: (Boolean) -> Unit = {},
    addNewToBottom: Boolean = false,
    onAddNewToBottomChange: (Boolean) -> Unit = {},
    moveCheckedToBottom: Boolean = true,
    onMoveCheckedToBottomChange: (Boolean) -> Unit = {},
    richLinkPreviews: Boolean = true,
    onRichLinkPreviewsChange: (Boolean) -> Unit = {},
    morningTime: String = "08:00",
    onMorningTimeChange: (String) -> Unit = {},
    afternoonTime: String = "13:00",
    onAfternoonTimeChange: (String) -> Unit = {},
    eveningTime: String = "18:00",
    onEveningTimeChange: (String) -> Unit = {},
    sharingEnabled: Boolean = true,
    onSharingEnabledChange: (Boolean) -> Unit = {},
    vaultPassword: String = "1234",
    onVaultPasswordChange: (String) -> Unit = {},
    onExportNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showPaletteDialog by remember { mutableStateOf(false) }
    var showChangePinDialog by remember { mutableStateOf(false) }
    var editingTimeLabel by remember { mutableStateOf<String?>(null) }
    var editingTimeValue by remember { mutableStateOf("") }
    var onEditingTimeSave by remember { mutableStateOf<(String) -> Unit>({}) }

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

        // Section 2.2: Google Keep Notlar & Rapor Ayarları
        item {
            SettingsCategoryCard(title = "Google Notlar Ayarları") {
                Column {
                    // Display & Layout
                    SettingsSwitchItem(
                        icon = Icons.Default.GridView,
                        title = "2 Sütunlu Kılavuz (Grid)",
                        subtitle = "Notlar Google Keep çift sütunlu düzen veya liste olarak gösterilsin",
                        checked = isNoteGrid,
                        onCheckedChange = onNoteGridChange
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.FormatListBulleted,
                        title = "Yeni Öğeleri En Alta Ekle",
                        subtitle = "Listelerde yeni eklenen maddeler otomatik listenin altına eklensin",
                        checked = addNewToBottom,
                        onCheckedChange = onAddNewToBottomChange
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Check,
                        title = "İşaretlenen Öğeleri En Alta Taşı",
                        subtitle = "Tamamlanan kontrol listesi maddeleri otomatik alta taşınsın",
                        checked = moveCheckedToBottom,
                        onCheckedChange = onMoveCheckedToBottomChange
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.Link,
                        title = "Zengin Bağlantı Önizlemeleri",
                        subtitle = "Notlardaki web ve kaynak bağlantılarını kart olarak göster",
                        checked = richLinkPreviews,
                        onCheckedChange = onRichLinkPreviewsChange
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.TextFields,
                        title = "Kelime & Karakter Sayacı",
                        subtitle = "Not düzenleme ekranının altında canlı sayaç göster",
                        checked = showWordCount,
                        onCheckedChange = onShowWordCountChange
                    )
                    SettingsSwitchItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Not Silme Onayı",
                        subtitle = "Bir not veya liste silinirken onay penceresi göster",
                        checked = confirmDeleteNote,
                        onCheckedChange = onConfirmDeleteNoteChange
                    )

                    // Reminder Times
                    SettingsClickableItem(
                        icon = Icons.Default.WbSunny,
                        title = "Sabah Hatırlatıcı Saati",
                        subtitle = "Varsayılan: $morningTime",
                        onClick = {
                            editingTimeLabel = "Sabah Hatırlatıcı Saati"
                            editingTimeValue = morningTime
                            onEditingTimeSave = onMorningTimeChange
                        }
                    )
                    SettingsClickableItem(
                        icon = Icons.Default.Schedule,
                        title = "Öğleden Sonra Hatırlatıcı Saati",
                        subtitle = "Varsayılan: $afternoonTime",
                        onClick = {
                            editingTimeLabel = "Öğleden Sonra Hatırlatıcı Saati"
                            editingTimeValue = afternoonTime
                            onEditingTimeSave = onAfternoonTimeChange
                        }
                    )
                    SettingsClickableItem(
                        icon = Icons.Default.DarkMode,
                        title = "Akşam Hatırlatıcı Saati",
                        subtitle = "Varsayılan: $eveningTime",
                        onClick = {
                            editingTimeLabel = "Akşam Hatırlatıcı Saati"
                            editingTimeValue = eveningTime
                            onEditingTimeSave = onEveningTimeChange
                        }
                    )

                    // Sharing & Security
                    SettingsSwitchItem(
                        icon = Icons.Default.Share,
                        title = "Not Paylaşımını Etkinleştir",
                        subtitle = "Ders notlarını ve deneme raporlarını aile ve öğretmenle paylaş",
                        checked = sharingEnabled,
                        onCheckedChange = onSharingEnabledChange
                    )
                    SettingsClickableItem(
                        icon = Icons.Default.Key,
                        title = "Gizli Kasa PIN Şifresi",
                        subtitle = "Kilitli notlara erişim şifresini belirleyin (Mevcut: ••••)",
                        onClick = { showChangePinDialog = true }
                    )
                    SettingsClickableItem(
                        icon = Icons.Default.FileDownload,
                        title = "Notları Yedekle / Dışa Aktar",
                        subtitle = "Tüm not, liste ve raporlarınızı JSON yedeği olarak dışa aktarın",
                        onClick = onExportNotes
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
                    Column {
                        Text(
                            text = "TufiDESK",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sürüm 1.2.0 • Material You / Material Design 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    // Change Vault PIN Dialog
    if (showChangePinDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showChangePinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kasa PIN Kodunu Değiştir")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Gizli notlarınızı kilitlemek için 4 haneli yeni bir PIN belirleyin.")
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = {
                            if (it.length <= 6) newPin = it
                            pinError = null
                        },
                        label = { Text("Yeni PIN Kodu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = {
                            if (it.length <= 6) confirmPin = it
                            pinError = null
                        },
                        label = { Text("Yeni PIN Kodu (Tekrar)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (pinError != null) {
                        Text(
                            text = pinError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        if (newPin.length < 4) {
                            pinError = "PIN kodu en az 4 haneli olmalıdır."
                        } else if (newPin != confirmPin) {
                            pinError = "PIN kodları birbiriyle eşleşmiyor!"
                        } else {
                            onVaultPasswordChange(newPin)
                            showChangePinDialog = false
                        }
                    }
                ) {
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePinDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    // Edit Reminder Time Dialog (Google Keep style)
    if (editingTimeLabel != null) {
        var tempTime by remember(editingTimeLabel) { mutableStateOf(editingTimeValue) }
        val presetTimes = listOf("07:00", "08:00", "09:00", "12:00", "13:00", "15:00", "18:00", "20:00")

        AlertDialog(
            onDismissRequest = { editingTimeLabel = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(editingTimeLabel ?: "Saat Belirle")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Not hatırlatıcı bildirimleri için varsayılan zamanı seçin veya yazın:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = tempTime,
                        onValueChange = { if (it.length <= 5) tempTime = it },
                        label = { Text("Saat (SS:DD)") },
                        placeholder = { Text("08:00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        "Hızlı Seçenekler:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetTimes.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (tempTime == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { tempTime = preset }
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (tempTime == preset) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (tempTime == preset) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        if (tempTime.isNotBlank()) {
                            onEditingTimeSave(tempTime.trim())
                        }
                        editingTimeLabel = null
                    }
                ) {
                    Text("Tamam")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingTimeLabel = null }) {
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
