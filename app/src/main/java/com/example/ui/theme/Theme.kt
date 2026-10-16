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
import com.example.data.model.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = JamesBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = JamesSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF115E59),
    tertiary = JamesAccent,
    background = JamesLightBackground,
    onBackground = JamesLightTextPrimary,
    surface = JamesLightSurface,
    onSurface = JamesLightTextPrimary,
    surfaceVariant = JamesLightSurfaceVariant,
    onSurfaceVariant = JamesLightTextSecondary,
    error = JamesErrorRed,
    outline = Color(0xFFCBD5E1)
)

private val DarkColorScheme = darkColorScheme(
    primary = JamesBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = JamesSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF134E4A),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = JamesBlueGlow,
    background = JamesDarkBackground,
    onBackground = JamesDarkTextPrimary,
    surface = JamesDarkSurface,
    onSurface = JamesDarkTextPrimary,
    surfaceVariant = JamesDarkSurfaceVariant,
    onSurfaceVariant = JamesDarkTextSecondary,
    error = JamesErrorRed,
    outline = Color(0xFF475569)
)

private val AmoledColorScheme = darkColorScheme(
    primary = JamesBlueLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = JamesSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF115E59),
    onSecondaryContainer = Color(0xFF99F6E4),
    tertiary = JamesBlueGlow,
    background = JamesAmoledBackground,
    onBackground = Color.White,
    surface = JamesAmoledSurface,
    onSurface = Color.White,
    surfaceVariant = JamesAmoledSurfaceVariant,
    onSurfaceVariant = Color(0xFFA1A1AA),
    error = JamesErrorRed,
    outline = Color(0xFF27272A)
)

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.AMOLED -> AmoledColorScheme
        ThemeMode.SYSTEM -> {
            val context = LocalContext.current
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (systemInDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (systemInDark) DarkColorScheme else LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun JamesChatTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MyApplicationTheme(
        themeMode = themeMode,
        dynamicColor = dynamicColor,
        content = content
    )
}
