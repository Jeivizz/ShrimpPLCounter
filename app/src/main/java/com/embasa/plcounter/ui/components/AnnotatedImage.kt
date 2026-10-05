package com.embasa.plcounter.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import com.embasa.plcounter.domain.model.SampleResult
import kotlin.math.max
import kotlin.math.roundToInt

private val MarkerColor = Color(0xFF00E676)

/**
 * Foto com as marcações desenhadas por cima. As coordenadas das detecções vêm do backend
 * na escala da imagem enviada (a mesma do [bitmap]), então basta reescalar para a tela.
 * Desenhar no app (em vez de pedir a imagem anotada) evita baixar outro JPEG e
 * prepara a correção manual (tocar para adicionar/remover).
 */
@Composable
fun AnnotatedImage(
    bitmap: Bitmap,
    result: SampleResult?,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
) {
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Canvas(modifier = modifier) {
        val iw = imageBitmap.width.toFloat()
        val ih = imageBitmap.height.toFloat()
        val scale = minOf(size.width / iw, size.height / ih)
        val dw = iw * scale
        val dh = ih * scale
        val left = (size.width - dw) / 2f
        val top = (size.height - dh) / 2f

        drawImage(
            image = imageBitmap,
            dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
            dstSize = IntSize(dw.roundToInt(), dh.roundToInt()),
        )

        if (result != null && result.imageWidth > 0 && result.imageHeight > 0) {
            val sx = dw / result.imageWidth
            val sy = dh / result.imageHeight
            val pad = 3.dp.toPx()
            val minSide = 12.dp.toPx()
            result.detections.forEach { d ->
                val w = max(d.width * sx + 2 * pad, minSide)
                val h = max(d.height * sy + 2 * pad, minSide)
                drawRect(
                    color = MarkerColor,
                    topLeft = Offset(left + d.centerX * sx - w / 2f, top + d.centerY * sy - h / 2f),
                    size = Size(w, h),
                    style = Stroke(width = strokeWidth.toPx()),
                )
            }
        }
    }
}

/**
 * Tela cheia: fundo todo preto (inclusive atrás das barras do sistema), instruções numa
 * faixa no topo e a imagem ocupando o espaço restante, com zoom (pinça), arrastar e
 * toque duplo para restaurar.
 */
@Composable
fun ImageViewerDialog(
    bitmap: Bitmap,
    result: SampleResult?,
    title: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        // Deixa a janela do diálogo toda preta, sem escurecimento e com ícones claros nas barras.
        val view = LocalView.current
        SideEffect {
            val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
            window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.BLACK))
            window.setDimAmount(0f)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }

        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var area by remember { mutableStateOf(IntSize.Zero) }

        Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

            // Topo: título e instruções
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Pinça para ampliar • arraste para mover • toque duplo para restaurar",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                RoundIconButton(icon = IconKind.Close, onClick = onDismiss)
            }

            // Imagem: só o espaço abaixo do topo; o zoom não passa por cima das instruções.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clipToBounds()
                    .onSizeChanged { area = it },
            ) {
                AnnotatedImage(
                    bitmap = bitmap,
                    result = result,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(onDoubleTap = {
                                scale = 1f
                                offset = Offset.Zero
                            })
                        }
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 8f)
                                val maxX = area.width * (newScale - 1f) / 2f
                                val maxY = area.height * (newScale - 1f) / 2f
                                val raw = offset + pan
                                offset = Offset(raw.x.coerceIn(-maxX, maxX), raw.y.coerceIn(-maxY, maxY))
                                scale = newScale
                            }
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y,
                        ),
                )
            }

            // Reserva o espaço da barra de navegação, também em preto.
            Spacer(modifier = Modifier.fillMaxWidth().navigationBarsPadding())
        }
    }
}