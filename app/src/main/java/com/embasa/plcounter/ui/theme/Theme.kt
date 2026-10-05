package com.embasa.plcounter.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val IAquaColorScheme = lightColorScheme(
    primary = Cobalto,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3EEFF),
    onPrimaryContainer = Mare,
    secondary = Alvorada,
    onSecondary = Color.White,
    secondaryContainer = AlvoradaTint,
    onSecondaryContainer = Color(0xFF7A3A0E),
    background = Espuma,
    onBackground = Mare,
    surface = Color.White,
    onSurface = Mare,
    surfaceVariant = Color(0xFFE8EFF8),
    onSurfaceVariant = TextoSuave,
    outline = Linha,
    outlineVariant = Linha,
    error = Coral,
)

private val IAquaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun IAquaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IAquaColorScheme,
        typography = IAquaTypography,
        shapes = IAquaShapes,
        content = content,
    )
}