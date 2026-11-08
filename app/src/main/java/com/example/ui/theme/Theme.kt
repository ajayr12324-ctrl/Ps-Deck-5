package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PS5ColorScheme = darkColorScheme(
    primary = PS5Cyan,
    onPrimary = Color.Black,
    primaryContainer = PS5Blue,
    onPrimaryContainer = Color.White,
    secondary = PS5Light,
    onSecondary = Color.Black,
    secondaryContainer = PS5SurfaceElevated,
    onSecondaryContainer = Color.White,
    tertiary = TrophyGold,
    onTertiary = Color.Black,
    background = PS5Background,
    onBackground = Color.White,
    surface = PS5SurfaceDark,
    onSurface = Color.White,
    surfaceVariant = PS5SurfaceElevated,
    onSurfaceVariant = PS5TextSecondary,
    outline = PS5TextTertiary,
    outlineVariant = PS5SurfaceCard
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PS5ColorScheme,
        typography = Typography,
        content = content
    )
}
