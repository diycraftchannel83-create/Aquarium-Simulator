package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OceanPrimary,
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFF80CBC4),
    secondary = OceanSecondary,
    onSecondary = Color(0xFF003822),
    secondaryContainer = Color(0xFF005234),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = OceanTertiary,
    onTertiary = Color(0xFF5C0028),
    background = OceanBackground,
    onBackground = OceanTextPrimary,
    surface = OceanSurface,
    onSurface = OceanTextPrimary,
    surfaceVariant = OceanSurfaceElevated,
    onSurfaceVariant = OceanTextSecondary,
    outline = Color(0xFF29B6F6)
)

private val LightColorScheme = lightColorScheme(
    primary = LightOceanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2EBF2),
    onPrimaryContainer = Color(0xFF00363A),
    secondary = LightOceanSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F3D0),
    onSecondaryContainer = Color(0xFF003822),
    tertiary = LightOceanTertiary,
    onTertiary = Color.White,
    background = LightOceanBackground,
    onBackground = LightOceanTextPrimary,
    surface = LightOceanSurface,
    onSurface = LightOceanTextPrimary,
    surfaceVariant = LightOceanSurfaceElevated,
    onSurfaceVariant = LightOceanTextSecondary,
    outline = Color(0xFF0097A7)
)

@Composable
fun AquaZenTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
