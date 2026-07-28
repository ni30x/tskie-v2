package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = FlatTextPrimary,
    onPrimary = Color.White,
    primaryContainer = FlatBlackCard,
    onPrimaryContainer = Color.White,
    secondary = FlatTextSecondary,
    onSecondary = FlatTextPrimary,
    background = FlatLightBackground,
    onBackground = FlatTextPrimary,
    surface = FlatWhiteCard,
    onSurface = FlatTextPrimary,
    surfaceVariant = FlatPillBadge,
    onSurfaceVariant = FlatTextSecondary,
    outline = FlatCardBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = FlatDarkBackground,
    primaryContainer = FlatDarkCard,
    onPrimaryContainer = Color.White,
    secondary = FlatTextSecondary,
    onSecondary = Color.White,
    background = FlatDarkBackground,
    onBackground = Color.White,
    surface = FlatDarkCard,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = FlatDarkCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Default to light theme matching the reference graphic canvas
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

