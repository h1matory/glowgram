package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ExteraDarkColorScheme =
  darkColorScheme(
    primary = TgBlue,
    onPrimary = TgTextPrimary,
    primaryContainer = TgBadgeBg,
    onPrimaryContainer = TgBadgeText,
    secondary = TgCyan,
    onSecondary = TgBgDeep,
    tertiary = TgPurple,
    background = TgBgDeep,
    onBackground = TgTextPrimary,
    surface = TgSurface,
    onSurface = TgTextPrimary,
    surfaceVariant = TgCard,
    onSurfaceVariant = TgTextSecondary,
    outline = TgBorder,
    outlineVariant = TgDivider,
  )

@Composable
fun ExteraNftTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = ExteraDarkColorScheme, typography = Typography, content = content)
}
