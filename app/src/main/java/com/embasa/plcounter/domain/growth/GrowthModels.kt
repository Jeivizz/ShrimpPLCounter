package com.embasa.plcounter.domain.growth

/** Uma pesagem de UM camarão. Peso em gramas; data em dias desde 1970 (LocalDate.toEpochDay). */
data class WeighIn(
    val epochDay: Long,
    val weightG: Double,
)

/**
 * Um lote acompanhado ao longo das semanas.
 * [baselineWeightG] é o peso de uma PL calculado na estimativa por pesagem (semana 0); pode ser nulo
 * quando o lote é criado manualmente, sem essa estimativa.
 */
data class GrowthLot(
    val id: String,
    val name: String,
    val startEpochDay: Long,
    val baselineWeightG: Double?,
    val weighIns: List<WeighIn> = emptyList(),
) {
    /** Idade em semanas (fracionária) numa data. Negativa se a data for anterior ao início. */
    fun weekOf(epochDay: Long): Double = (epochDay - startEpochDay) / 7.0

    fun sortedWeighIns(): List<WeighIn> = weighIns.sortedBy { it.epochDay }

    fun latest(): WeighIn? = weighIns.maxByOrNull { it.epochDay }

    fun withWeighIn(weighIn: WeighIn): GrowthLot = copy(weighIns = weighIns + weighIn)

    fun withoutWeighIn(weighIn: WeighIn): GrowthLot {
        val index = weighIns.indexOf(weighIn)
        return if (index < 0) this else copy(weighIns = weighIns.filterIndexed { i, _ -> i != index })
    }
}