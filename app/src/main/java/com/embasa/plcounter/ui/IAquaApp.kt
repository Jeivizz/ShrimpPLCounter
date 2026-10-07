package com.embasa.plcounter.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.embasa.plcounter.camera.CameraCaptureScreen
import com.embasa.plcounter.ui.components.AppBottomBar
import com.embasa.plcounter.ui.components.AppTab
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.config.ConfigScreen
import com.embasa.plcounter.ui.growth.GrowthRoot
import com.embasa.plcounter.ui.growth.GrowthViewModel
import com.embasa.plcounter.ui.growth.NewLotDialog
import com.embasa.plcounter.ui.growth.todayEpochDay
import com.embasa.plcounter.ui.result.ResultScreen
import com.embasa.plcounter.ui.sampling.SamplingScreen
import com.embasa.plcounter.ui.splash.SplashScreen
import com.embasa.plcounter.ui.weighing.WeighingScreen

@Composable
fun IAquaApp(vm: BatchViewModel, growthVm: GrowthViewModel) {
    val state by vm.state.collectAsState()
    val growth by growthVm.state.collectAsState()

    var tab by rememberSaveable { mutableStateOf(AppTab.Count) }
    var trackFromWeighing by remember { mutableStateOf(false) }
    var newLotManual by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) vm.onImagePicked(uri)
    }

    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val showBar = (tab == AppTab.Growth || state.screen == Screen.Config) && !keyboardOpen

    // Voltar na lista de lotes leva de volta à aba Contagem (o detalhe trata o próprio "voltar").
    BackHandler(enabled = tab == AppTab.Growth && growth.selectedId == null) { tab = AppTab.Count }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // Com a barra de abas visível, ela já cuida do espaço da barra de navegação do sistema.
                    .then(if (showBar) Modifier.consumeWindowInsets(WindowInsets.navigationBars) else Modifier),
            ) {
                if (tab == AppTab.Growth) {
                    GrowthRoot(growthVm = growthVm, onNewLot = { newLotManual = true })
                } else {
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
                                    onTrackGrowth = { trackFromWeighing = true },
                                    onBack = vm::closeWeighing,
                                )
                            }
                        }
                    }
                }
            }

            if (showBar) AppBottomBar(selected = tab, onSelect = { tab = it })
        }
    }

    // Lote criado a partir da estimativa por pesagem: o peso de uma PL já vem preenchido.
    if (trackFromWeighing) {
        NewLotDialog(
            initialName = "Lote ${Fmt.date(todayEpochDay())}",
            initialBaselineG = state.weightEstimate?.weightPerPlG,
            onConfirm = { name, day, baseline ->
                val id = growthVm.createLot(name, day, baseline)
                growthVm.select(id)
                trackFromWeighing = false
                tab = AppTab.Growth
            },
            onDismiss = { trackFromWeighing = false },
        )
    }

    // Lote criado direto na aba Crescimento.
    if (newLotManual) {
        NewLotDialog(
            initialName = "Lote ${Fmt.date(todayEpochDay())}",
            initialBaselineG = null,
            onConfirm = { name, day, baseline ->
                growthVm.select(growthVm.createLot(name, day, baseline))
                newLotManual = false
            },
            onDismiss = { newLotManual = false },
        )
    }
}