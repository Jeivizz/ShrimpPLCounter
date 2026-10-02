package com.embasa.plcounter.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.embasa.plcounter.ui.config.ConfigScreen
import com.embasa.plcounter.ui.result.ResultScreen
import com.embasa.plcounter.ui.sampling.SamplingScreen
import com.embasa.plcounter.ui.splash.SplashScreen

@Composable
fun IAquaApp(vm: BatchViewModel) {
    val state by vm.state.collectAsState()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.onImagePicked(uri)
    }

    when (state.screen) {
        Screen.Splash -> SplashScreen(onFinished = vm::onSplashFinished)

        Screen.Config -> ConfigScreen(
            state = state,
            onSampleVolumeChange = vm::onSampleVolumeChange,
            onTotalVolumeChange = vm::onTotalVolumeChange,
            onStart = vm::startSampling,
        )

        Screen.Sampling -> SamplingScreen(
            state = state,
            onPickGallery = {
                pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCount = vm::countCurrent,
            onNext = vm::nextOrFinish,
            onBack = vm::backToConfig,
        )

        Screen.Result -> state.estimate?.let { estimate ->
            ResultScreen(
                estimate = estimate,
                onNewBatch = vm::newBatch,
                onBack = vm::backToConfig,
            )
        }
    }
}