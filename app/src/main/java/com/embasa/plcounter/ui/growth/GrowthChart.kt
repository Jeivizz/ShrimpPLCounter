package com.embasa.plcounter.ui.growth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Manrope
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.Raso
import com.embasa.plcounter.ui.theme.TextoSuave

private val InsetLeft = 54.dp
private val InsetRight = 12.dp
private val InsetTop = 12.dp
private val InsetBottom = 28.dp

@Composable
fun GrowthChart(
    model: GrowthChartModel,
    viewport: ChartViewport,
    onViewportChange: (ChartViewport) -> Unit,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontFamily = Manrope, fontSize = 10.sp, color = TextoSuave) }
    val description = "Gráfico do peso de um camarão por semana, com ${model.points.size} pontos e a faixa esperada."

    // Os gestos leem sempre a janela e a função mais recentes, sem reiniciar a cada movimento.
    val currentViewport by rememberUpdatedState(viewport)
    val currentOnChange by rememberUpdatedState(onViewportChange)
    val zoomed = viewport != model.fit

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .semantics { contentDescription = description }
            .pointerInput(model) {
                detectTapGestures(onDoubleTap = { currentOnChange(model.fit) })
            }
            .pointerInput(model, zoomed) {
                val left = InsetLeft.toPx()
                val right = InsetRight.toPx()
                val top = InsetTop.toPx()
                val bottom = InsetBottom.toPx()
                val plotWidth = size.width - left - right
                val plotHeight = size.height - top - bottom

                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val fingers = event.changes.count { it.pressed }
                        if (fingers >= 2 || zoomed) {
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val centroid = event.calculateCentroid(useCurrent = true)
                            if ((zoom != 1f || pan != Offset.Zero) && centroid != Offset.Unspecified) {
                                val vp = currentViewport
                                // ponto dos dados sob os dedos: ele deve ficar parado ao ampliar
                                val focusX = vp.xMin + (centroid.x - left) / plotWidth * vp.xSpan
                                val focusY = vp.yMax - (centroid.y - top) / plotHeight * vp.ySpan
                                var next = if (zoom != 1f) vp.zoomed(zoom.toDouble(), focusX, focusY) else vp
                                next = next.panned(
                                    dx = -pan.x / plotWidth * next.xSpan,
                                    dy = pan.y / plotHeight * next.ySpan,
                                )
                                currentOnChange(model.constrain(next))
                                event.changes.forEach { if (it.positionChanged()) it.consume() }
                            }
                        }
                    } while (event.changes.any { it.pressed })
                }
            },
    ) {
        val left = InsetLeft.toPx()
        val right = InsetRight.toPx()
        val top = InsetTop.toPx()
        val bottom = InsetBottom.toPx()
        val width = size.width - left - right
        val height = size.height - top - bottom

        fun px(week: Double): Float = left + ((week - viewport.xMin) / viewport.xSpan * width).toFloat()
        fun py(weight: Double): Float = top + ((viewport.yMax - weight) / viewport.ySpan * height).toFloat()

        val thin = 1.dp.toPx()

        val yTicks = ChartMath.niceTicks(viewport.yMin, viewport.yMax, 5)
        val yLabels = ChartMath.weightLabels(yTicks)
        yTicks.values.forEachIndexed { i, value ->
            val y = py(value)
            drawLine(Linha, Offset(left, y), Offset(left + width, y), thin)
            val label = measurer.measure(yLabels[i], labelStyle)
            drawText(label, topLeft = Offset(left - 8.dp.toPx() - label.size.width, y - label.size.height / 2f))
        }

        val xTicks = ChartMath.niceTicks(viewport.xMin, viewport.xMax, 6)
        val xLabels = ChartMath.weekLabels(xTicks)
        xTicks.values.forEachIndexed { i, week ->
            val x = px(week)
            drawLine(Linha.copy(alpha = 0.6f), Offset(x, top), Offset(x, top + height), thin)
            val label = measurer.measure(xLabels[i], labelStyle)
            drawText(label, topLeft = Offset(x - label.size.width / 2f, top + height + 6.dp.toPx()))
        }

        clipRect(left, top, left + width, top + height) {
            if (model.bandWeeks.size >= 2) {

                val band = Path()
                model.bandWeeks.forEachIndexed { i, week ->
                    val x = px(week)
                    val y = py(model.bandMax[i])
                    if (i == 0) band.moveTo(x, y) else band.lineTo(x, y)
                }
                for (i in model.bandWeeks.indices.reversed()) {
                    band.lineTo(px(model.bandWeeks[i]), py(model.bandMin[i]))
                }
                band.close()
                drawPath(band, Raso.copy(alpha = 0.22f))

                // Peso típico, tracejado
                val typical = Path()
                model.bandWeeks.forEachIndexed { i, week ->
                    val x = px(week)
                    val y = py(model.typical[i])
                    if (i == 0) typical.moveTo(x, y) else typical.lineTo(x, y)
                }
                drawPath(
                    typical,
                    Raso,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
                    ),
                )
            }

            // Linha do peso real
            if (model.points.size >= 2) {
                val real = Path()
                model.points.forEachIndexed { i, p ->
                    val x = px(p.week)
                    val y = py(p.weightG)
                    if (i == 0) real.moveTo(x, y) else real.lineTo(x, y)
                }
                drawPath(real, Cobalto, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }

        val margin = 8.dp.toPx()
        clipRect(left - margin, top - margin, left + width + margin, top + height + margin) {
            model.points.forEach { p ->
                val center = Offset(px(p.week), py(p.weightG))
                if (p.isBaseline) {
                    drawCircle(Color.White, radius = 6.dp.toPx(), center = center)
                    drawCircle(Cobalto, radius = 6.dp.toPx(), center = center, style = Stroke(2.5f.dp.toPx()))
                } else {
                    drawCircle(Color.White, radius = 7.dp.toPx(), center = center)
                    drawCircle(statusColor(p.status), radius = 5.dp.toPx(), center = center)
                }
            }
        }
    }
}

@Composable
fun ChartZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onFit: () -> Unit,
    fitEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ZoomButton(IconKind.Minus, onZoomOut)
        ZoomButton(IconKind.Plus, onZoomIn)
        Text(
            text = "Ajustar",
            style = MaterialTheme.typography.labelLarge,
            color = if (fitEnabled) Cobalto else TextoSuave.copy(alpha = 0.5f),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(enabled = fitEnabled, onClick = onFit)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ZoomButton(icon: IconKind, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IAquaIcon(icon, tint = Mare, iconSize = 18.dp)
    }
}

@Composable
fun GrowthLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem("Faixa esperada") {
            Box(Modifier.size(width = 16.dp, height = 10.dp).background(Raso.copy(alpha = 0.3f)))
        }
        LegendItem("Típico") {
            Canvas(Modifier.size(width = 18.dp, height = 4.dp)) {
                drawLine(
                    Raso,
                    Offset(0f, size.height / 2),
                    Offset(size.width, size.height / 2),
                    2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                )
            }
        }
        LegendItem("Pesagens") {
            Box(Modifier.size(10.dp).clip(CircleShape).background(Cobalto))
        }
    }
}

@Composable
private fun LegendItem(label: String, marker: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        marker()
        Spacer(Modifier.width(6.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextoSuave)
    }
}