package com.embasa.plcounter.ui

import android.graphics.Bitmap
import com.embasa.plcounter.domain.model.BatchConfig
import com.embasa.plcounter.domain.model.LotEstimate
import com.embasa.plcounter.domain.model.SampleResult

const val SAMPLE_COUNT = 3

enum class Screen { Splash, Config, Sampling, Camera, Result }

data class SampleSlot(
    val preview: Bitmap? = null,
    val result: SampleResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

data class UiState(
    val screen: Screen = Screen.Splash,
    val sampleVolumeText: String = "100",
    val totalVolumeText: String = "1000",
    val configError: String? = null,
    val config: BatchConfig? = null,
    val slots: List<SampleSlot> = List(SAMPLE_COUNT) { SampleSlot() },
    val currentIndex: Int = 0,
    val estimate: LotEstimate? = null,
)