package com.embasa.plcounter.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.embasa.plcounter.domain.model.SampleResult
import kotlin.math.max
import kotlin.math.roundToInt

private val MarkerColor = Color(0xFF00E676)


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

/** Tela cheia com zoom (pinça), arrastar e toque duplo para voltar ao tamanho original. */
@Composable
fun ImageViewerDialog(
    bitmap: Bitmap,
    result: SampleResult?,
    title: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var box by remember { mutableStateOf(IntSize.Zero) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onSizeChanged { box = it },
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
                            val maxX = box.width * (newScale - 1f) / 2f
                            val maxY = box.height * (newScale - 1f) / 2f
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

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp),
            ) {
                Text(text = title, color = Color.White)
                Text(text = "Pinça para ampliar • toque duplo para restaurar", color = Color.White.copy(alpha = 0.7f))
            }

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(12.dp)
                    .clickable(onClick = onDismiss),
                shape = RoundedCornerShape(50),
                color = Color.White.copy(alpha = 0.2f),
            ) {
                Text(
                    text = "✕",
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }
    }
}