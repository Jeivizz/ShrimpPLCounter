package com.embasa.plcounter.domain

import com.embasa.plcounter.domain.model.BatchConfig
import com.embasa.plcounter.domain.model.LotEstimate
import kotlin.math.roundToLong
import kotlin.math.sqrt

object EstimateCalculator {

    const val DEFAULT_CV_WARNING = 0.20

    fun estimate(
        config: BatchConfig,
        counts: List<Int>,
        cvWarning: Double = DEFAULT_CV_WARNING,
    ): LotEstimate {
        require(counts.isNotEmpty()) { "É preciso ao menos uma amostra" }
        require(counts.all { it >= 0 }) { "Contagens não podem ser negativas" }

        val mean = counts.average()
        val cv = if (counts.size >= 2 && mean > 0) {
            val variance = counts.sumOf { (it - mean) * (it - mean) } / (counts.size - 1)
            sqrt(variance) / mean
        } else {
            null
        }

        return LotEstimate(
            sampleCounts = counts,
            meanCount = mean,
            estimatedTotal = (mean * config.scaleFactor).roundToLong(),
            coefficientOfVariation = cv,
            highVariability = cv != null && cv > cvWarning,
        )
    }
}