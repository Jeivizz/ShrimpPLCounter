package com.embasa.plcounter.ui.growth

import com.embasa.plcounter.ui.components.Fmt
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

class Ticks(val values: List<Double>, val step: Double)


data class ChartViewport(val xMin: Double, val xMax: Double, val yMin: Double, val yMax: Double) {
    val xSpan: Double get() = xMax - xMin
    val ySpan: Double get() = yMax - yMin
    val xCenter: Double get() = (xMin + xMax) / 2
    val yCenter: Double get() = (yMin + yMax) / 2

    fun zoomed(factor: Double, focusX: Double, focusY: Double) = ChartViewport(
        xMin = focusX - (focusX - xMin) / factor,
        xMax = focusX + (xMax - focusX) / factor,
        yMin = focusY - (focusY - yMin) / factor,
        yMax = focusY + (yMax - focusY) / factor,
    )

    fun panned(dx: Double, dy: Double) = ChartViewport(xMin + dx, xMax + dx, yMin + dy, yMax + dy)

    fun clampedTo(bounds: ChartViewport, minXSpan: Double, minYSpan: Double): ChartViewport {
        val (x0, x1) = clampAxis(xMin, xMax, bounds.xMin, bounds.xMax, minXSpan)
        val (y0, y1) = clampAxis(yMin, yMax, bounds.yMin, bounds.yMax, minYSpan)
        return ChartViewport(x0, x1, y0, y1)
    }

    private fun clampAxis(lo: Double, hi: Double, boundLo: Double, boundHi: Double, minSpan: Double): Pair<Double, Double> {
        val total = boundHi - boundLo
        val span = (hi - lo).coerceIn(min(minSpan, total), total)
        var newLo = (lo + hi) / 2 - span / 2
        var newHi = newLo + span
        if (newLo < boundLo) { newLo = boundLo; newHi = boundLo + span }
        if (newHi > boundHi) { newHi = boundHi; newLo = boundHi - span }
        return newLo to newHi
    }
}

object ChartMath {

    fun niceTicks(min: Double, max: Double, target: Int = 5): Ticks {
        require(max > min) { "intervalo vazio" }
        val raw = (max - min) / target
        val magnitude = 10.0.pow(floor(log10(raw)))
        val step = listOf(1.0, 2.0, 2.5, 5.0, 10.0).map { it * magnitude }.first { it >= raw - 1e-12 }
        val first = ceil(min / step - 1e-9).roundToLong()
        val last = floor(max / step + 1e-9).roundToLong()
        return Ticks((first..last).map { it * step }, step)
    }

    private fun decimalsFor(step: Double): Int {
        for (d in 0..3) {
            val scaled = step * 10.0.pow(d)
            if (abs(scaled - Math.round(scaled)) < 1e-6) return d
        }
        return 3
    }

    fun weightLabels(ticks: Ticks): List<String> {
        val inMg = (ticks.values.maxOrNull() ?: 0.0) < 0.1 - 1e-12
        val scale = if (inMg) 1000.0 else 1.0
        val decimals = decimalsFor(ticks.step * scale)
        val unit = if (inMg) "mg" else "g"
        return ticks.values.map { if (abs(it) < 1e-12) "0" else "${Fmt.fixed(it * scale, decimals)} $unit" }
    }

    fun weekLabels(ticks: Ticks): List<String> {
        val decimals = decimalsFor(ticks.step)
        return ticks.values.map { Fmt.fixed(it, decimals) }
    }
}