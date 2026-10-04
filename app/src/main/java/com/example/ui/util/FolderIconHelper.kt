package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

data class FolderIconItem(
    val key: String,
    val title: String,
    val icon: ImageVector,
    val category: String
)

object FolderIconHelper {
    val ALL_ICONS: List<FolderIconItem> = listOf(
        // Popüler & Özel Simgeler
        FolderIconItem("Favorite", "Kalp", Icons.Default.Favorite, "Özel"),
        FolderIconItem("Star", "Yıldız", Icons.Default.Star, "Özel"),
        FolderIconItem("Knight", "Satranç / Kalkan", Icons.Default.Shield, "Özel"),
        FolderIconItem("Smile", "Gülümseme", Icons.Default.SentimentSatisfied, "Özel"),
        FolderIconItem("Fire", "Alev", Icons.Default.LocalFireDepartment, "Özel"),
        FolderIconItem("Rocket", "Roket", Icons.Default.RocketLaunch, "Özel"),
        FolderIconItem("Lightbulb", "Fikir", Icons.Default.Lightbulb, "Özel"),
        FolderIconItem("Trophy", "Kupa", Icons.Default.EmojiEvents, "Özel"),
        FolderIconItem("Bookmark", "Yer İmi", Icons.Default.Bookmark, "Özel"),
        FolderIconItem("Music", "Müzik", Icons.Default.MusicNote, "Özel"),
        FolderIconItem("Palette", "Sanat", Icons.Default.Palette, "Özel"),
        FolderIconItem("Code", "Yazılım", Icons.Default.Code, "Özel"),
        FolderIconItem("Folder", "Klasör", Icons.Default.Folder, "Özel"),

        // Derslere Ait Simgeler
        FolderIconItem("Math", "Matematik", Icons.Default.Calculate, "Dersler"),
        FolderIconItem("Physics", "Fizik", Icons.Default.Science, "Dersler"),
        FolderIconItem("Biology", "Biyoloji", Icons.Default.Biotech, "Dersler"),
        FolderIconItem("Literature", "Türkçe & Edebiyat", Icons.Default.MenuBook, "Dersler"),
        FolderIconItem("History", "Tarih", Icons.Default.AccountBalance, "Dersler"),
        FolderIconItem("Geography", "Coğrafya", Icons.Default.Public, "Dersler"),
        FolderIconItem("Language", "Yabancı Dil", Icons.Default.Translate, "Dersler"),
        FolderIconItem("Philosophy", "Felsefe & Mantık", Icons.Default.Psychology, "Dersler"),
        FolderIconItem("Geometry", "Geometri", Icons.Default.Category, "Dersler"),
        FolderIconItem("Exam", "Deneme & Sınav", Icons.Default.Assignment, "Dersler"),
        FolderIconItem("School", "Genel Ders", Icons.Default.School, "Dersler"),
        FolderIconItem("Computer", "Bilişim", Icons.Default.Computer, "Dersler")
    )

    fun getIcon(iconName: String?): ImageVector {
        if (iconName.isNullOrBlank()) return Icons.Default.Folder
        return ALL_ICONS.find { it.key.equals(iconName, ignoreCase = true) }?.icon ?: Icons.Default.Folder
    }
}
