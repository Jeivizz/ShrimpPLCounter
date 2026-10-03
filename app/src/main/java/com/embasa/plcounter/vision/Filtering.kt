package com.embasa.plcounter.vision

import kotlin.math.hypot
import kotlin.math.max

object Filtering {
    internal fun gap(a: Component, b: Component): Double {
        val dx = max(max(a.x - (b.x + b.width), b.x - (a.x + a.width)), 0)
        val dy = max(max(a.y - (b.y + b.height), b.y - (a.y + a.height)), 0)
        return hypot(dx.toDouble(), dy.toDouble())
    }

    fun filter(components: List<Component>, params: VisionParams): List<Component> {
        val valid = components.filter { it.area in params.minArea..params.maxArea }
        if (valid.isEmpty()) return emptyList()

        val median = median(valid.map { it.area })

        return valid.filter { c ->
            val isSmall = c.area < params.fragRatio * median
            val nearLarger = valid.any { o ->
                o !== c && o.area > c.area && gap(c, o) <= params.maxGap
            }
            !(isSmall && nearLarger)
        }
    }
}