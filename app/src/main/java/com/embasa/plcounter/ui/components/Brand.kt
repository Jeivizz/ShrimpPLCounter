package com.embasa.plcounter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.R
import kotlin.math.PI
import kotlin.math.sin

private val TWO_PI = (2.0 * PI).toFloat()

/** Logotipo horizontal. A largura segue a proporção do arquivo; ajuste só a altura. */
@Composable
fun IAquaLogo(
    modifier: Modifier = Modifier,
    logoHeight: Dp = 34.dp,
) {
    Image(
        painter = painterResource(R.drawable.iaqua_logo),
        contentDescription = "IAqua",
        modifier = modifier.height(logoHeight),
    )
}

@Composable
fun WaveFill(
    brush: Brush,
    modifier: Modifier = Modifier,
    amplitude: Dp = 8.dp,
    wavelength: Dp = 200.dp,
    phase: () -> Float = { 0f },
) {
    Canvas(modifier = modifier) {
        val amp = amplitude.toPx()
        val length = wavelength.toPx()
        val shift = phase()

        fun surfaceY(x: Float): Float = amp + amp * sin(x / length * TWO_PI + shift)

        val path = Path().apply {
            moveTo(0f, surfaceY(0f))
            var x = 6f
            while (x < size.width) {
                lineTo(x, surfaceY(x))
                x += 6f
            }
            lineTo(size.width, surfaceY(size.width))
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, brush)
    }
}

@Composable
fun WaveFill(
    color: Color,
    modifier: Modifier = Modifier,
    amplitude: Dp = 8.dp,
    wavelength: Dp = 200.dp,
    phase: () -> Float = { 0f },
) = WaveFill(SolidColor(color), modifier, amplitude, wavelength, phase)