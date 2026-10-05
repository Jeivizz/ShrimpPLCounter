package com.embasa.plcounter.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleOwner
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.PrimaryButton
import com.embasa.plcounter.ui.components.RoundIconButton
import java.io.File

@Composable
fun CameraCaptureScreen(
    onCaptured: (Uri) -> Unit,
    onClose: () -> Unit,
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val lifecycleOwner = remember(context) { context.findLifecycleOwner() }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Prévia e foto no MESMO formato 4:3, para o círculo da tela coincidir com o recorte da foto.
    val resolutionSelector = remember {
        ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .build()
    }
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FIT_CENTER }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .build()
    }
    val providerHolder = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var capturing by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Liga a câmera quando houver permissão; desliga ao sair da tela.
    DisposableEffect(hasPermission, lifecycleOwner) {
        if (hasPermission) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                val provider = future.get()
                providerHolder.value = provider
                val preview = Preview.Builder().setResolutionSelector(resolutionSelector).build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                )
            }, executor)
        }
        onDispose { providerHolder.value?.unbindAll() }
    }

    // Fundo preto da câmera: ícones claros nas barras do sistema enquanto esta tela estiver aberta.
    val view = LocalView.current
    DisposableEffect(view) {
        val window = context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = true
            controller?.isAppearanceLightNavigationBars = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            // O quadro inteiro da câmera (3:4 em retrato) com o círculo desenhado por cima.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .aspectRatio(CameraGuide.FRAME_ASPECT),
            ) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                CircleGuideOverlay(modifier = Modifier.fillMaxSize())
            }

            // Topo: fechar + instrução
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoundIconButton(icon = IconKind.Close, onClick = onClose)
                Spacer(Modifier.width(12.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        "Centralize o recipiente no círculo",
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "O que ficar fora do círculo será descartado",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Base: obturador
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                errorText?.let {
                    Text(
                        it,
                        color = Color(0xFFFF8A80),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                // Anel + miolo: o miolo escurece enquanto salva a foto.
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(7.dp)
                        .clip(CircleShape)
                        .background(if (capturing) Color.Gray else Color.White)
                        .clickable(enabled = !capturing) {
                            capturing = true
                            errorText = null
                            val file = File(context.cacheDir, "captura_${System.currentTimeMillis()}.jpg")
                            val options = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture.takePicture(
                                options,
                                executor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        capturing = false
                                        onCaptured(Uri.fromFile(file))
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        capturing = false
                                        errorText = "Não foi possível tirar a foto. Tente novamente."
                                    }
                                },
                            )
                        },
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                IAquaIcon(IconKind.Camera, tint = Color.White, iconSize = 48.dp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Precisamos da permissão da câmera para fotografar a amostra.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = "Permitir câmera",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                )
                Spacer(Modifier.height(10.dp))
                TextButton(onClick = onClose) { Text("Voltar", color = Color.White) }
            }
        }
    }
}

/** Escurece tudo fora do círculo: é uma prévia do que ficará preto na foto. */
@Composable
private fun CircleGuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = minOf(size.width, size.height) * CameraGuide.DIAMETER_FRACTION / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addOval(Rect(center = center, radius = radius))
        }
        drawPath(path, Color.Black.copy(alpha = 0.85f))
        drawCircle(
            color = Color.White,
            radius = radius,
            center = center,
            style = Stroke(width = 3.dp.toPx()),
        )
    }
}

private fun Context.findLifecycleOwner(): LifecycleOwner {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is LifecycleOwner) return ctx
        ctx = ctx.baseContext
    }
    error("Contexto sem LifecycleOwner")
}

private fun Context.findActivity(): Activity? {
    var ctx: Context = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}