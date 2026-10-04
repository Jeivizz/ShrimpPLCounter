package com.embasa.plcounter.domain.model

data class BatchConfig(
    val sampleVolumeMl: Double,
    val totalVolumeLiters: Double,
) {
    init {
        require(sampleVolumeMl > 0) { "Volume da amostra deve ser maior que zero" }
        require(totalVolumeLiters > 0) { "Volume total deve ser maior que zero" }
    }

    val scaleFactor: Double get() = totalVolumeLiters * 1000.0 / sampleVolumeMl
}

data class Detection(
    val centerX: Float,
    val centerY: Float,
    val width: Int = 0,
    val height: Int = 0,
    val isManual: Boolean = false,
)

data class SampleResult(
    val imageWidth: Int,
    val imageHeight: Int,
    val detections: List<Detection>,
) {
    val count: Int get() = detections.size
}

data class LotEstimate(
    val sampleCounts: List<Int>,
    val meanCount: Double,
    val estimatedTotal: Long,
    val coefficientOfVariation: Double?,
    val highVariability: Boolean,
)

data class WeightEstimate(
    val weightPerPlG: Double,
    val expectedTotalWeightG: Double,
    val perSampleWeightPerPlG: List<Double>,
    val coefficientOfVariation: Double?,
)

enum class Agreement { GOOD, MODERATE, LOW }
data class WeightComparison(
    val measuredTotalWeightG: Double,
    val countByWeight: Long,
    val deviation: Double,
    val agreement: Agreement,
)