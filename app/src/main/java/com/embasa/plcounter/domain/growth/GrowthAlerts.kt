package com.embasa.plcounter.domain.growth

import kotlin.math.floor
import kotlin.math.ln

enum class AlertSeverity { Info, Attention, Critical }

enum class AlertKind {
    BelowRange,
    AboveRange,
    SlowGrowth,
    OverdueWeighing,
}

data class GrowthAlert(val kind: AlertKind, val severity: AlertSeverity, val count: Int)

data class GrowthAlertConfig(
    val weeksToAlert: Int = 2,
    val weeksCritical: Int = 3,
    val slowRateRatio: Double = 0.5,
    val overdueDays: Int = 10,
) {
    init {
        require(weeksToAlert >= 1) { "alerta_semanas precisa ser >= 1" }
        require(weeksCritical >= weeksToAlert) { "alerta_critico_semanas precisa ser >= alerta_semanas" }
        require(slowRateRatio > 0.0 && slowRateRatio < 1.0) { "alerta_ritmo precisa estar entre 0 e 1" }
        require(overdueDays >= 1) { "pesagem_atrasada_dias precisa ser >= 1" }
    }

    companion object {
        fun parse(text: String): GrowthAlertConfig {
            val values = mutableMapOf<String, String>()
            val directive = Regex("""^#\s*(\w+)\s*:\s*(\S+)""")
            text.lineSequence().forEach { line ->
                directive.find(line.trim())?.let { values[it.groupValues[1].lowercase()] = it.groupValues[2] }
            }
            val d = GrowthAlertConfig()

            fun int(key: String, default: Int): Int = values[key]?.let {
                it.toIntOrNull() ?: throw IllegalArgumentException("$key: valor inválido \"$it\"")
            } ?: default

            fun dbl(key: String, default: Double): Double = values[key]?.let {
                it.replace(',', '.').toDoubleOrNull() ?: throw IllegalArgumentException("$key: valor inválido \"$it\"")
            } ?: default

            return GrowthAlertConfig(
                weeksToAlert = int("alerta_semanas", d.weeksToAlert),
                weeksCritical = int("alerta_critico_semanas", d.weeksCritical),
                slowRateRatio = dbl("alerta_ritmo", d.slowRateRatio),
                overdueDays = int("pesagem_atrasada_dias", d.overdueDays),
            )
        }
    }
}

object GrowthAlerts {

    private class Entry(val week: Double, val weightG: Double, val status: GrowthStatus, val typicalG: Double)

    fun evaluate(
        lot: GrowthLot,
        reference: GrowthReference,
        config: GrowthAlertConfig,
        todayEpochDay: Long,
    ): List<GrowthAlert> {
        val series = series(lot, reference)
        val alerts = mutableListOf<GrowthAlert>()

        rangeStreak(series, config)?.let { alerts += it }
        slowGrowth(series, reference, config)?.let { alerts += it }
        overdue(lot, config, todayEpochDay)?.let { alerts += it }

        return alerts.sortedByDescending { it.severity }
    }

    private fun series(lot: GrowthLot, reference: GrowthReference): List<Entry> =
        lot.sortedWeighIns()
            .filter { lot.weekOf(it.epochDay) >= 0.0 }
            .groupBy { floor(lot.weekOf(it.epochDay)).toInt() }
            .values
            .map { sameWeek -> sameWeek.maxByOrNull { it.epochDay }!! }
            .sortedBy { it.epochDay }
            .mapNotNull { w ->
                val week = lot.weekOf(w.epochDay)
                val eval = reference.evaluate(week, w.weightG) ?: return@mapNotNull null
                Entry(week, w.weightG, eval.status, eval.range.typicalG)
            }

    private fun rangeStreak(series: List<Entry>, config: GrowthAlertConfig): GrowthAlert? {
        val last = series.lastOrNull()?.status ?: return null
        if (last == GrowthStatus.Within) return null
        val count = series.takeLastWhile { it.status == last }.size
        return when (last) {
            GrowthStatus.Below -> when {
                count >= config.weeksCritical -> GrowthAlert(AlertKind.BelowRange, AlertSeverity.Critical, count)
                count >= config.weeksToAlert -> GrowthAlert(AlertKind.BelowRange, AlertSeverity.Attention, count)
                else -> null
            }
            GrowthStatus.Above ->
                if (count >= config.weeksToAlert) GrowthAlert(AlertKind.AboveRange, AlertSeverity.Info, count) else null
            GrowthStatus.Within -> null
        }
    }

    private fun slowGrowth(series: List<Entry>, reference: GrowthReference, config: GrowthAlertConfig): GrowthAlert? {
        if (series.size < 2) return null
        val slow = (1 until series.size).map { i ->
            val expected = ln(series[i].typicalG / series[i - 1].typicalG)
            if (expected <= 1e-9) {
                false
            } else {
                val actual = ln(series[i].weightG / series[i - 1].weightG)
                actual / expected < config.slowRateRatio
            }
        }
        val count = slow.takeLastWhile { it }.size
        return when {
            count >= config.weeksCritical -> GrowthAlert(AlertKind.SlowGrowth, AlertSeverity.Critical, count)
            count >= config.weeksToAlert -> GrowthAlert(AlertKind.SlowGrowth, AlertSeverity.Attention, count)
            else -> null
        }
    }

    private fun overdue(lot: GrowthLot, config: GrowthAlertConfig, today: Long): GrowthAlert? {
        val since = lot.latest()?.epochDay ?: lot.startEpochDay
        val days = (today - since).toInt()
        return if (days > config.overdueDays) GrowthAlert(AlertKind.OverdueWeighing, AlertSeverity.Info, days) else null
    }
}