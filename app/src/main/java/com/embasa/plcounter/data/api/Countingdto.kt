package com.embasa.plcounter.data.api

import androidx.annotation.Keep
import com.embasa.plcounter.domain.model.Detection
import com.embasa.plcounter.domain.model.SampleResult
import com.google.gson.annotations.SerializedName


@Keep
data class DetectionDto(
    @SerializedName("x") val x: Int,
    @SerializedName("y") val y: Int,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int,
    @SerializedName("area") val area: Int,
    @SerializedName("center_x") val centerX: Double,
    @SerializedName("center_y") val centerY: Double,
)

@Keep
data class CountingResponseDto(
    @SerializedName("count") val count: Int,
    @SerializedName("image_width") val imageWidth: Int,
    @SerializedName("image_height") val imageHeight: Int,
    @SerializedName("scale") val scale: Double,
    @SerializedName("detections") val detections: List<DetectionDto>,
    @SerializedName("annotated_image_base64") val annotatedImageBase64: String? = null,
)

fun CountingResponseDto.toDomain() = SampleResult(
    imageWidth = imageWidth,
    imageHeight = imageHeight,
    detections = detections.map {
        Detection(
            centerX = it.centerX.toFloat(),
            centerY = it.centerY.toFloat(),
            width = it.width,
            height = it.height,
        )
    },
)