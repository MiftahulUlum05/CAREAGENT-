package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LightBlue,
    onPrimary = NavyDark,
    primaryContainer = NavyCard,
    onPrimaryContainer = SkySoft,
    secondary = PurpleAi,
    onSecondary = Color.White,
    secondaryContainer = PurpleAiDark,
    onSecondaryContainer = PurpleAiLight,
    tertiary = LightBlue,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextSecondaryDark,
    outline = NavyBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = SkySoft,
    onPrimaryContainer = NavyDeep,
    secondary = PurpleAiDark,
    onSecondary = Color.White,
    secondaryContainer = PurpleAiLight,
    onSecondaryContainer = NavyDeep,
    tertiary = LightBlue,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SkyTint,
    onSurfaceVariant = TextSecondaryLight,
    outline = BorderLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
