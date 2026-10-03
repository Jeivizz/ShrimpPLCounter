package com.embasa.plcounter.camera

import android.Manifest
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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
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

    val previewView = remember { PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER } }
    val imageCapture = remember {
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()
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
                val preview = Preview.Builder().build()
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

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (hasPermission) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            GuideOverlay(modifier = Modifier.fillMaxSize())

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Centralize o recipiente no quadro", color = Color.White)
                Text(
                    "Câmera a 90°, boa luz, fundo branco",
                    color = Color.White.copy(alpha = 0.75f),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                errorText?.let { Text(it, color = Color(0xFFFF8A80), textAlign = TextAlign.Center) }

                Surface(
                    modifier = Modifier
                        .size(76.dp)
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
                    shape = CircleShape,
                    color = if (capturing) Color.Gray else Color.White,
                ) {}

                OutlinedButton(onClick = onClose) { Text("Cancelar", color = Color.White) }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Precisamos da permissão da câmera para tirar a foto da amostra.",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Permitir câmera") }
                OutlinedButton(onClick = onClose, modifier = Modifier.padding(top = 8.dp)) {
                    Text("Voltar", color = Color.White)
                }
            }
        }
    }
}

/** Escurece a imagem fora de um quadro central, para o usuário alinhar o recipiente. */
@Composable
private fun GuideOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val side = minOf(size.width, size.height) * 0.88f
        val left = (size.width - side) / 2f
        val top = (size.height - side) / 2f - size.height * 0.03f
        val radius = CornerRadius(32.dp.toPx())

        val path = Path().apply {
            fillType = PathFillType.EvenOdd
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(RoundRect(Rect(left, top, left + side, top + side), radius))
        }
        drawPath(path, Color.Black.copy(alpha = 0.55f))
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(left, top),
            size = Size(side, side),
            cornerRadius = radius,
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