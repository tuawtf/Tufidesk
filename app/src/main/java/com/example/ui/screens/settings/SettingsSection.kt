package com.example.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Ayarlar ekranı 8 ana kategori tanımı.
 */
enum class SettingsSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    PROFILE(
        title = "Profil",
        subtitle = "Aktif profil ve hesap değiştirme",
        icon = Icons.Default.AccountCircle
    ),
    APPEARANCE(
        title = "Görünüm",
        subtitle = "Uygulama teması, renk paleti ve tam ekran",
        icon = Icons.Default.Palette
    ),
    HOMEWORK(
        title = "Ödevler",
        subtitle = "Tamamlanan ödevleri gizleme tercihleri",
        icon = Icons.Default.Task
    ),
    NOTES(
        title = "Notlar",
        subtitle = "Not düzeni, kılavuz ve kontrol listesi tercihleri",
        icon = Icons.Default.Description
    ),
    AI_AND_SPEECH(
        title = "Yapay Zeka ve Dikte",
        subtitle = "Tufi AI, API anahtarı ve Whisper sesli dikte",
        icon = Icons.Default.AutoAwesome
    ),
    DATA_AND_SYNC(
        title = "Veri ve Senkronizasyon",
        subtitle = "Bulut senkronizasyonu (Firebase) ve veri yedekleme",
        icon = Icons.Default.CloudSync
    ),
    ADVANCED(
        title = "Gelişmiş",
        subtitle = "Geliştirici seçenekleri, teşhis paneli ve test araçları",
        icon = Icons.Default.Tune
    ),
    ABOUT(
        title = "Hakkında",
        subtitle = "Uygulama sürümü, lisanslar ve sistem bilgisi",
        icon = Icons.Default.Info
    )
}

/**
 * Arama için taranabilir ayar öğesi modeli.
 */
data class SearchableSettingItem(
    val title: String,
    val subtitle: String,
    val section: SettingsSection,
    val icon: ImageVector,
    val keywords: List<String> = emptyList()
)
