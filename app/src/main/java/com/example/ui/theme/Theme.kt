package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun MyApplicationTheme(
    themeMode: String = "SYSTEM",
    colorPalette: String = "DYNAMIC",
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> systemInDark
    }

    val context = LocalContext.current
    val supportsDynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    val colorScheme: ColorScheme = when {
        colorPalette == "DYNAMIC" && supportsDynamic -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        colorPalette == "TEAL" -> if (isDark) TealDark else TealLight
        colorPalette == "VIOLET" -> if (isDark) VioletDark else VioletLight
        colorPalette == "ROSE" -> if (isDark) RoseDark else RoseLight
        colorPalette == "FOREST" -> if (isDark) ForestDark else ForestLight
        colorPalette == "AMBER" -> if (isDark) AmberDark else AmberLight
        else -> if (isDark) IndigoDark else IndigoLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
