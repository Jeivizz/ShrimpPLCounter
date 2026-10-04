package com.embasa.plcounter.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.embasa.plcounter.camera.CameraCaptureScreen
import com.embasa.plcounter.ui.config.ConfigScreen
import com.embasa.plcounter.ui.result.ResultScreen
import com.embasa.plcounter.ui.sampling.SamplingScreen
import com.embasa.plcounter.ui.splash.SplashScreen
import com.embasa.plcounter.ui.weighing.WeighingScreen

/** Navegação simples por estado: sem dependência extra. */
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
            onOpenCamera = vm::openCamera,
            onCount = vm::countCurrent,
            onNext = vm::nextOrFinish,
            onBack = vm::backToConfig,
        )

        Screen.Camera -> CameraCaptureScreen(
            onCaptured = vm::onCameraCaptured,
            onClose = vm::closeCamera,
        )

        Screen.Result -> state.estimate?.let { estimate ->
            ResultScreen(
                estimate = estimate,
                config = state.config,
                slots = state.slots,
                onWeighing = vm::openWeighing,
                onNewBatch = vm::newBatch,
                onBack = vm::backToConfig,
            )
        }

        Screen.Weighing -> state.estimate?.let { estimate ->
            WeighingScreen(
                state = state,
                estimate = estimate,
                onSampleWeightChange = vm::onSampleWeightChange,
                onTotalWeightChange = vm::onTotalWeightChange,
                onBack = vm::closeWeighing,
            )
        }
    }
}