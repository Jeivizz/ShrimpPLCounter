package com.embasa.plcounter.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

enum class IconKind { Camera, Gallery, Close, Chevron, Check, Scale, Bulb, Drop, Waves, Info, Chart, Plus, Trash, Calendar }


@Composable
fun IAquaIcon(
    kind: IconKind,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    iconSize: Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(iconSize)) {
        val u = this.size.minDimension / 24f
        val stroke = Stroke(width = 1.9f * u, cap = StrokeCap.Round, join = StrokeJoin.Round)

        fun pt(x: Float, y: Float) = Offset(x * u, y * u)

        fun line(vararg points: Pair<Float, Float>) {
            val path = Path()
            points.forEachIndexed { i, (x, y) ->
                if (i == 0) path.moveTo(x * u, y * u) else path.lineTo(x * u, y * u)
            }
            drawPath(path, tint, style = stroke)
        }

        when (kind) {
            IconKind.Camera -> {
                drawRoundRect(tint, pt(3f, 7f), Size(18f * u, 13f * u), CornerRadius(3f * u), style = stroke)
                line(8f to 7f, 9.5f to 4.5f, 14.5f to 4.5f, 16f to 7f)
                drawCircle(tint, radius = 3.6f * u, center = pt(12f, 13.5f), style = stroke)
            }
            IconKind.Gallery -> {
                drawRoundRect(tint, pt(3f, 4f), Size(18f * u, 16f * u), CornerRadius(3f * u), style = stroke)
                drawCircle(tint, radius = 1.6f * u, center = pt(9f, 9.5f))
                line(4.5f to 18f, 10f to 13f, 14f to 17f, 16.5f to 14.5f, 19.5f to 17.5f)
            }
            IconKind.Close -> {
                line(6f to 6f, 18f to 18f)
                line(18f to 6f, 6f to 18f)
            }
            IconKind.Chevron -> line(6f to 9f, 12f to 15f, 18f to 9f)
            IconKind.Check -> line(5f to 12.5f, 10f to 17.5f, 19f to 7f)
            IconKind.Scale -> {
                line(12f to 5f, 12f to 20f)
                line(7f to 20f, 17f to 20f)
                line(5f to 7.5f, 19f to 7.5f)
                line(5f to 7.5f, 2.8f to 13f, 7.2f to 13f, 5f to 7.5f)
                line(19f to 7.5f, 16.8f to 13f, 21.2f to 13f, 19f to 7.5f)
            }
            IconKind.Bulb -> {
                drawCircle(tint, radius = 5.2f * u, center = pt(12f, 10f), style = stroke)
                line(9.5f to 16.5f, 14.5f to 16.5f)
                line(10.3f to 19.5f, 13.7f to 19.5f)
            }
            IconKind.Drop -> {
                val path = Path().apply {
                    moveTo(12f * u, 3f * u)
                    cubicTo(12f * u, 3f * u, 5.5f * u, 10.5f * u, 5.5f * u, 14.5f * u)
                    cubicTo(5.5f * u, 18.1f * u, 8.4f * u, 21f * u, 12f * u, 21f * u)
                    cubicTo(15.6f * u, 21f * u, 18.5f * u, 18.1f * u, 18.5f * u, 14.5f * u)
                    cubicTo(18.5f * u, 10.5f * u, 12f * u, 3f * u, 12f * u, 3f * u)
                    close()
                }
                drawPath(path, tint, style = stroke)
            }
            IconKind.Waves -> {
                for (row in listOf(8f, 12f, 16f)) {
                    val path = Path()
                    var x = 3f
                    path.moveTo(x * u, row * u)
                    while (x <= 21f) {
                        val y = row + 1.3f * sin(((x - 3f) / 18f * 3f * 2f * PI).toFloat())
                        path.lineTo(x * u, y * u)
                        x += 0.5f
                    }
                    drawPath(path, tint, style = stroke)
                }
            }
            IconKind.Chart -> {
                line(4f to 4f, 4f to 20f, 20f to 20f)
                line(7.5f to 15f, 11.5f to 11f, 14.5f to 13.5f, 19f to 7f)
            }
            IconKind.Plus -> {
                line(12f to 5f, 12f to 19f)
                line(5f to 12f, 19f to 12f)
            }
            IconKind.Trash -> {
                line(4.5f to 7f, 19.5f to 7f)
                line(9f to 7f, 9f to 4.5f, 15f to 4.5f, 15f to 7f)
                line(6.5f to 7f, 7.5f to 20f, 16.5f to 20f, 17.5f to 7f)
            }
            IconKind.Calendar -> {
                drawRoundRect(tint, pt(4f, 6f), Size(16f * u, 14f * u), CornerRadius(3f * u), style = stroke)
                line(4f to 10.5f, 20f to 10.5f)
                line(8.5f to 3.8f, 8.5f to 7.8f)
                line(15.5f to 3.8f, 15.5f to 7.8f)
            }
            IconKind.Info -> {
                drawCircle(tint, radius = 9f * u, center = pt(12f, 12f), style = stroke)
                line(12f to 11f, 12f to 16.5f)
                drawCircle(tint, radius = 1f * u, center = pt(12f, 7.8f))
            }
        }
    }
}