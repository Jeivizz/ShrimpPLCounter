package com.embasa.plcounter.domain

import com.embasa.plcounter.domain.model.Agreement
import com.embasa.plcounter.domain.model.WeightComparison
import com.embasa.plcounter.domain.model.WeightEstimate
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.math.sqrt

object WeightCalculator {
    const val GOOD_AGREEMENT = 0.10
    const val MODERATE_AGREEMENT = 0.20

    fun estimate(
        sampleCounts: List<Int>,
        sampleWeightsG: List<Double>,
        estimatedTotalCount: Long,
    ): WeightEstimate {
        require(sampleCounts.isNotEmpty() && sampleCounts.size == sampleWeightsG.size) {
            "Cada amostra precisa de uma contagem e um peso"
        }
        require(sampleCounts.all { it >= 0 }) { "Contagens não podem ser negativas" }
        require(sampleWeightsG.all { it > 0 }) { "Os pesos precisam ser maiores que zero" }
        val totalCount = sampleCounts.sum()
        require(totalCount > 0) { "Nenhuma PL contada nas amostras" }

        val perPl = sampleWeightsG.sum() / totalCount

        val perSample = sampleCounts.indices
            .filter { sampleCounts[it] > 0 }
            .map { sampleWeightsG[it] / sampleCounts[it] }

        val cv = if (perSample.size >= 2) {
            val mean = perSample.average()
            val variance = perSample.sumOf { (it - mean) * (it - mean) } / (perSample.size - 1)
            sqrt(variance) / mean
        } else {
            null
        }

        return WeightEstimate(
            weightPerPlG = perPl,
            expectedTotalWeightG = perPl * estimatedTotalCount,
            perSampleWeightPerPlG = perSample,
            coefficientOfVariation = cv,
        )
    }

    fun compare(
        estimate: WeightEstimate,
        estimatedTotalCount: Long,
        measuredTotalWeightG: Double,
    ): WeightComparison {
        require(estimatedTotalCount > 0) { "A estimativa por imagem precisa ser maior que zero" }
        require(measuredTotalWeightG > 0) { "O peso medido precisa ser maior que zero" }

        val byWeight = (measuredTotalWeightG / estimate.weightPerPlG).roundToLong()
        val deviation = (byWeight - estimatedTotalCount).toDouble() / estimatedTotalCount
        val agreement = when {
            abs(deviation) <= GOOD_AGREEMENT -> Agreement.GOOD
            abs(deviation) <= MODERATE_AGREEMENT -> Agreement.MODERATE
            else -> Agreement.LOW
        }
        return WeightComparison(measuredTotalWeightG, byWeight, deviation, agreement)
    }
}