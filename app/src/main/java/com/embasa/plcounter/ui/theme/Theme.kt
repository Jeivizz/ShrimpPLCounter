package com.embasa.plcounter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Só tema claro, para manter a identidade visual do design (sem cores dinâmicas do Android).
private val IAquaColorScheme = lightColorScheme(
    primary = IAquaBlue,
    onPrimary = IAquaSurface,
    primaryContainer = IAquaChip,
    onPrimaryContainer = IAquaTitleBlue,
    secondary = IAquaOrange,
    background = IAquaBackground,
    onBackground = IAquaTextPrimary,
    surface = IAquaSurface,
    onSurface = IAquaTextPrimary,
    surfaceVariant = IAquaCaptureArea,
    onSurfaceVariant = IAquaTextMuted,
    outline = IAquaTrackInactive,
)

@Composable
fun IAquaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IAquaColorScheme,
        content = content,
    )
}