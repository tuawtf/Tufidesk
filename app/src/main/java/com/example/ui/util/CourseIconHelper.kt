package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SportsBasketball
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.vector.ImageVector

object CourseIconHelper {
    fun getIconForCourse(courseName: String): ImageVector {
        val lower = courseName.lowercase().trim()
        return when {
            lower.contains("fizik") -> Icons.Default.Bolt
            lower.contains("kimya") -> Icons.Default.Science
            lower.contains("biyoloji") -> Icons.Default.Biotech
            lower.contains("problem") -> Icons.Default.Extension
            lower.contains("matematik") || lower.contains("geometri") || lower.contains("cebir") -> Icons.Default.Calculate
            lower.contains("edebiyat") || lower.contains("türkçe") || lower.contains("dil ve anlatım") -> Icons.Default.MenuBook
            lower.contains("tarih") || lower.contains("inkılap") -> Icons.Default.AccountBalance
            lower.contains("coğrafya") -> Icons.Default.Public
            lower.contains("ingilizce") || lower.contains("almanca") || lower.contains("fransızca") || lower.contains("yabancı dil") -> Icons.Default.Translate
            lower.contains("felsefe") || lower.contains("mantık") || lower.contains("sosyoloji") -> Icons.Default.Psychology
            lower.contains("bilgisayar") || lower.contains("bilişim") || lower.contains("kodlama") || lower.contains("yazılım") -> Icons.Default.Computer
            lower.contains("müzik") -> Icons.Default.MusicNote
            lower.contains("resim") || lower.contains("görsel sanatlar") || lower.contains("sanat") -> Icons.Default.Palette
            lower.contains("beden") || lower.contains("spor") -> Icons.Default.SportsBasketball
            else -> Icons.Default.School
        }
    }
}
