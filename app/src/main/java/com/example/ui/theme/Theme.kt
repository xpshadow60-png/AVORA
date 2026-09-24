package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Primary500,
    onPrimary = Color.White,
    primaryContainer = Primary600,
    onPrimaryContainer = Color.White,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = CyanAccent,
    tertiary = CoralPriority,
    onTertiary = Color.White,
    background = DarkBg,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC0BDDB),
    outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = Primary500,
    onPrimary = Color.White,
    primaryContainer = Primary100,
    onPrimaryContainer = Primary600,
    secondary = CyanAccentDark,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = Primary600,
    tertiary = CoralPriority,
    onTertiary = Color.White,
    background = LightBg,
    onBackground = Color(0xFF1C1B2B),
    surface = LightSurface,
    onSurface = Color(0xFF1C1B2B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF565373),
    outline = LightBorder
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Composable
fun StudyPulseTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Disabled by default for cohesive StudyPulse branding
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
