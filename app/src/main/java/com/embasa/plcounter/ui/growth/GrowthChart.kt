package com.embasa.plcounter.ui.growth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Manrope
import com.embasa.plcounter.ui.theme.Raso
import com.embasa.plcounter.ui.theme.TextoSuave

/**
 * Peso de um camarão x semanas: faixa esperada (área), peso típico (tracejado) e as pesagens reais
 * (linha com pontos coloridos pela situação). Desenhado direto no Canvas, sem biblioteca de gráficos.
 */
@Composable
fun GrowthChart(model: GrowthChartModel, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val labelStyle = remember { TextStyle(fontFamily = Manrope, fontSize = 10.sp, color = TextoSuave) }
    val description = "Gráfico do peso de um camarão por semana, com ${model.points.size} pontos e a faixa esperada."

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .semantics { contentDescription = description },
    ) {
        val left = 54.dp.toPx()
        val right = 12.dp.toPx()
        val top = 12.dp.toPx()
        val bottom = 28.dp.toPx()
        val width = size.width - left - right
        val height = size.height - top - bottom

        fun px(week: Double): Float = left + (model.xAxis.position(week) * width).toFloat()
        fun py(weight: Double): Float = top + ((1.0 - model.yAxis.position(weight)) * height).toFloat()

        val thin = 1.dp.toPx()

        // Grade horizontal + rótulos de peso
        model.yAxis.minor.forEach { value ->
            drawLine(Linha.copy(alpha = 0.5f), Offset(left, py(value)), Offset(left + width, py(value)), thin)
        }
        model.yAxis.ticks.forEach { value ->
            val y = py(value)
            drawLine(Linha, Offset(left, y), Offset(left + width, y), thin)
            val label = measurer.measure(Fmt.axisWeight(value), labelStyle)
            drawText(label, topLeft = Offset(left - 8.dp.toPx() - label.size.width, y - label.size.height / 2f))
        }

        // Grade vertical + rótulos de semana
        model.xAxis.ticks.forEach { week ->
            val x = px(week)
            drawLine(Linha.copy(alpha = 0.6f), Offset(x, top), Offset(x, top + height), thin)
            val label = measurer.measure(Fmt.number(week), labelStyle)
            drawText(label, topLeft = Offset(x - label.size.width / 2f, top + height + 6.dp.toPx()))
        }

        clipRect(left, top, left + width, top + height) {
            // Faixa esperada
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

        // Pontos (fora do clip, para não cortar o da semana 0)
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