package com.embasa.plcounter.ui.growth

import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthReference
import com.embasa.plcounter.domain.growth.GrowthStatus

class ChartPoint(
    val week: Double,
    val weightG: Double,
    /** Null quando a semana está fora da tabela de referência. */
    val status: GrowthStatus?,
    /** O ponto da semana 0 (peso de uma PL da estimativa), desenhado diferente das pesagens. */
    val isBaseline: Boolean,
)

/** Tudo que o gráfico precisa desenhar, já calculado (sem depender do Compose, para testar). */
class GrowthChartModel(
    val xAxis: ChartMath.Axis,
    val yAxis: ChartMath.Axis,
    val bandWeeks: List<Double>,
    val bandMin: List<Double>,
    val bandMax: List<Double>,
    val typical: List<Double>,
    val points: List<ChartPoint>,
) {
    companion object {
        /** O eixo das semanas mostra pelo menos [minWeeks], para a curva esperada aparecer à frente. */
        fun build(lot: GrowthLot, reference: GrowthReference, log: Boolean, minWeeks: Double = 8.0): GrowthChartModel {
            val points = buildList {
                lot.baselineWeightG?.let { add(ChartPoint(0.0, it, reference.evaluate(0.0, it)?.status, true)) }
                lot.sortedWeighIns().forEach { w ->
                    val week = lot.weekOf(w.epochDay)
                    if (week >= 0.0) add(ChartPoint(week, w.weightG, reference.evaluate(week, w.weightG)?.status, false))
                }
            }

            val lastWeek = points.maxOfOrNull { it.week } ?: 0.0
            val xAxis = ChartMath.weekAxis(maxOf(lastWeek + 1.0, minWeeks))

            // Faixa esperada amostrada a cada 0,25 semana, só onde a tabela tem dados.
            val weeks = generateSequence(0.0) { it + 0.25 }
                .takeWhile { it <= xAxis.max + 1e-9 }
                .filter { reference.rangeAt(it) != null }
                .toList()
            val ranges = weeks.map { reference.rangeAt(it)!! }

            val values = points.map { it.weightG } + ranges.flatMap { listOf(it.minG, it.maxG) }
            val lowest = values.minOrNull() ?: 0.001
            val highest = values.maxOrNull() ?: 10.0
            val yAxis = if (log) ChartMath.logAxis(lowest, highest) else ChartMath.linearAxis(highest)

            return GrowthChartModel(
                xAxis = xAxis,
                yAxis = yAxis,
                bandWeeks = weeks,
                bandMin = ranges.map { it.minG },
                bandMax = ranges.map { it.maxG },
                typical = ranges.map { it.typicalG },
                points = points,
            )
        }
    }
}