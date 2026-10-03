package com.embasa.plcounter.vision


data class VisionParams(
    // preprocessing.py
    val blurKernel: Int = 25,
    // segmentation.py
    val thresholdBlock: Int = 21,
    val thresholdC: Double = 5.0,
    val openKernel: Int = 5,
    // filtering.py / settings.py
    val minArea: Int = 30,
    val maxArea: Int = 500,
    val maxGap: Int = 15,
    val fragRatio: Double = 0.5,
    // counting_service.py / estimate_scale
    val maxInputSide: Int = 1600,
    val targetMedianArea: Double = 130.0,
    val scaleMin: Double = 0.5,
    val scaleMax: Double = 2.0,
    val scaleMinArea: Int = 40,
    val scaleMaxArea: Int = 500,
)

/** Um blob detectado (equivale ao dict de detect_components). */
data class Component(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
    val area: Int,
    val centerX: Double,
    val centerY: Double,
)