package com.embasa.plcounter.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.embasa.plcounter.camera.CameraCaptureScreen
import com.embasa.plcounter.ui.config.ConfigScreen
import com.embasa.plcounter.ui.result.ResultScreen
import com.embasa.plcounter.ui.sampling.SamplingScreen
import com.embasa.plcounter.ui.splash.SplashScreen
import com.embasa.plcounter.ui.weighing.WeighingScreen

/** Navegação por estado, com troca suave entre telas. */
@Composable
fun IAquaApp(vm: BatchViewModel) {
    val state by vm.state.collectAsState()

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.onImagePicked(uri)
    }

    // O Surface define a cor padrão do texto (Mare) para tudo que não declara cor.
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        AnimatedContent(
            targetState = state.screen,
            transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(140)) },
            label = "tela",
        ) { screen ->
            when (screen) {
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
    }
}