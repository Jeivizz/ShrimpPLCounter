package com.embasa.plcounter.ui.growth

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/** Cálculo dos eixos do gráfico (sem desenho, para poder testar). */
object ChartMath {

    /** [ticks] recebem rótulo; [minor] são só linhas de grade mais claras. */
    class Axis(val min: Double, val max: Double, val ticks: List<Double>, val minor: List<Double> = emptyList(), val log: Boolean = false) {
        /** Posição de [value] no eixo, de 0 (min) a 1 (max). */
        fun position(value: Double): Double {
            val v = if (log) log10(value.coerceAtLeast(min)) else value
            val lo = if (log) log10(min) else min
            val hi = if (log) log10(max) else max
            return ((v - lo) / (hi - lo)).coerceIn(0.0, 1.0)
        }
    }

    /** Eixo logarítmico: de uma potência de 10 abaixo do menor valor a uma acima do maior. */
    fun logAxis(minValue: Double, maxValue: Double): Axis {
        val lo = floor(log10(minValue.coerceAtLeast(1e-6))).toInt()
        var hi = ceil(log10(maxValue.coerceAtLeast(1e-6))).toInt()
        if (hi <= lo) hi = lo + 1
        val ticks = (lo..hi).map { 10.0.pow(it) }
        val minor = (lo until hi).flatMap { e -> listOf(2.0, 5.0).map { it * 10.0.pow(e) } }
        return Axis(ticks.first(), ticks.last(), ticks, minor, log = true)
    }

    /** Eixo linear a partir de zero, com passo "redondo" (1, 2, 2,5, 5 x 10^k). */
    fun linearAxis(maxValue: Double): Axis {
        val safeMax = maxValue.coerceAtLeast(1e-6)
        val raw = safeMax / 4.0
        val magnitude = 10.0.pow(floor(log10(raw)))
        val step = listOf(1.0, 2.0, 2.5, 5.0, 10.0).map { it * magnitude }.first { it >= raw - 1e-12 }
        val top = ceil(safeMax / step - 1e-9) * step
        val count = Math.round(top / step).toInt()
        return Axis(0.0, top, (0..count).map { it * step })
    }

    /** Eixo das semanas: passo 1 até 12 semanas, 2 até 26, 4 acima. */
    fun weekAxis(maxWeek: Double): Axis {
        val needed = ceil(maxWeek.coerceAtLeast(1.0)).toInt()
        val step = when {
            needed <= 12 -> 1
            needed <= 26 -> 2
            else -> 4
        }
        val top = ceil(needed / step.toDouble()).toInt() * step
        return Axis(0.0, top.toDouble(), (0..top step step).map { it.toDouble() })
    }

    fun nearlyEqual(a: Double, b: Double, eps: Double = 1e-9) = abs(a - b) <= eps
}