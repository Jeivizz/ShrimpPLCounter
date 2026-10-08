package com.embasa.plcounter.ui.growth

import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthReference
import com.embasa.plcounter.domain.growth.GrowthStatus
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

class ChartPoint(
    val week: Double,
    val weightG: Double,
    /** Null quando a semana está fora da tabela de referência. */
    val status: GrowthStatus?,
    /** O ponto da semana 0 (peso de uma PL da estimativa), desenhado diferente das pesagens. */
    val isBaseline: Boolean,
)

/**
 * Tudo que o gráfico precisa, já calculado (sem depender do Compose, para testar).
 *  - [fit]: janela que enquadra os dados (é o que aparece de início e ao tocar em "Ajustar");
 *  - [bounds]: tudo que existe (curva inteira + pesagens); é o limite ao afastar o zoom.
 */
class GrowthChartModel(
    val fit: ChartViewport,
    val bounds: ChartViewport,
    val minXSpan: Double,
    val minYSpan: Double,
    val bandWeeks: List<Double>,
    val bandMin: List<Double>,
    val bandMax: List<Double>,
    val typical: List<Double>,
    val points: List<ChartPoint>,
) {

    fun constrain(viewport: ChartViewport): ChartViewport = viewport.clampedTo(bounds, minXSpan, minYSpan)

    companion object {
        private const val BAND_STEP = 0.1
        private const val DEFAULT_WEEKS = 8.0
        private const val MIN_FIT_WEEKS = 3.0

        fun build(lot: GrowthLot, reference: GrowthReference): GrowthChartModel {
            val points = buildList {
                lot.baselineWeightG?.let { add(ChartPoint(0.0, it, reference.evaluate(0.0, it)?.status, true)) }
                lot.sortedWeighIns().forEach { w ->
                    val week = lot.weekOf(w.epochDay)
                    if (week >= 0.0) add(ChartPoint(week, w.weightG, reference.evaluate(week, w.weightG)?.status, false))
                }
            }

            val firstWeek = points.minOfOrNull { it.week }
            val lastWeek = points.maxOfOrNull { it.week }
            val fitX0 = if (firstWeek == null) 0.0 else max(0.0, floor(firstWeek) - 1.0)
            var fitX1 = if (lastWeek == null) DEFAULT_WEEKS else ceil(lastWeek) + 1.0
            if (fitX1 - fitX0 < MIN_FIT_WEEKS) fitX1 = fitX0 + MIN_FIT_WEEKS

            val boundsX1 = max(max(reference.lastWeek.toDouble(), fitX1), MIN_FIT_WEEKS)
            val samples = (0..Math.round(boundsX1 / BAND_STEP).toInt()).map { it * BAND_STEP }
            val weeks = samples.filter { reference.rangeAt(it) != null }
            val ranges = weeks.map { reference.rangeAt(it)!! }

            val dataEnd = max(lastWeek ?: fitX1, fitX0 + 1.0)
            val inWindow = weeks.indices.filter { weeks[it] >= fitX0 - 1e-9 && weeks[it] <= dataEnd + 1e-9 }
            val visible = points.map { it.weightG } + inWindow.flatMap { listOf(ranges[it].minG, ranges[it].maxG) }
            val lo = visible.minOrNull() ?: 0.0
            val hi = visible.maxOrNull()?.takeIf { it > 0 } ?: 1.0

            val pad = max((hi - lo) * 0.08, hi * 0.02)

            val rawY0 = if (lo >= 0.4 * hi) max(0.0, lo - pad) else 0.0
            val rawY1 = hi + pad
            val fitStep = ChartMath.niceTicks(rawY0, rawY1).step
            val fitY0 = floor(rawY0 / fitStep + 1e-9) * fitStep
            val fitY1 = ceil(rawY1 / fitStep - 1e-9) * fitStep
            val fit = ChartViewport(fitX0, fitX1, fitY0, fitY1)

            val allMax = max(points.maxOfOrNull { it.weightG } ?: 0.0, ranges.maxOfOrNull { it.maxG } ?: 0.0)
            val boundsY1 = max(fit.yMax, ceilToStep(allMax * 1.08))
            val bounds = ChartViewport(0.0, max(boundsX1, fit.xMax), 0.0, boundsY1)

            return GrowthChartModel(
                fit = fit,
                bounds = bounds,
                // Dá para aproximar até 10x além da janela ajustada; nunca menos que ela, senão
                // "Ajustar" e o primeiro gesto deslocariam a janela dos dados.
                minXSpan = fit.xSpan / 10.0,
                minYSpan = fit.ySpan / 10.0,
                bandWeeks = weeks,
                bandMin = ranges.map { it.minG },
                bandMax = ranges.map { it.maxG },
                typical = ranges.map { it.typicalG },
                points = points,
            )
        }

        private fun ceilToStep(value: Double): Double {
            if (value <= 0) return 1.0
            val step = ChartMath.niceTicks(0.0, value).step
            return ceil(value / step - 1e-9) * step
        }
    }
}