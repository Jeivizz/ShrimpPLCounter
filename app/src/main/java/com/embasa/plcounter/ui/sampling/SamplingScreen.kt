package com.embasa.plcounter.ui.sampling

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.ui.SAMPLE_COUNT
import com.embasa.plcounter.ui.UiState
import com.embasa.plcounter.ui.components.AnnotatedImage
import com.embasa.plcounter.ui.components.IAquaHeader
import com.embasa.plcounter.ui.components.ImageViewerDialog
import com.embasa.plcounter.ui.components.TipsCard
import com.embasa.plcounter.ui.theme.IAquaBlue
import com.embasa.plcounter.ui.theme.IAquaTrackInactive

@Composable
fun SamplingScreen(
    state: UiState,
    onPickGallery: () -> Unit,
    onOpenCamera: () -> Unit,
    onCount: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val index = state.currentIndex
    val slot = state.slots[index]
    val isLast = index == SAMPLE_COUNT - 1
    val counted = slot.result != null
    val preview = slot.preview

    var viewing by remember(index) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IAquaHeader()

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TipsCard()

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Processo de Amostragem",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        text = "Amostra ${index + 1} de $SAMPLE_COUNT",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(SAMPLE_COUNT) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i <= index) IAquaBlue else IAquaTrackInactive),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .dashedBorder(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f))
                    .clickable(enabled = !slot.isLoading) {
                        if (preview != null && counted) viewing = true else onPickGallery()
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (preview != null) {
                    AnnotatedImage(
                        bitmap = preview,
                        result = slot.result,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = Color.White, modifier = Modifier.size(88.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("\uD83D\uDCF7", fontSize = 36.sp)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Capturar Amostra ${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                slot.result?.let { result ->
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = IAquaBlue,
                    ) {
                        Text(
                            text = "${result.count} PLs",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp),
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.55f),
                    ) {
                        Text(
                            text = "Toque para ampliar e conferir",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                if (slot.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                }
            }

            slot.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onPickGallery,
                    enabled = !slot.isLoading,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Galeria") }

                OutlinedButton(
                    onClick = onOpenCamera,
                    enabled = !slot.isLoading,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                ) { Text("Câmera") }
            }

            Button(
                onClick = { if (counted) onNext() else onCount() },
                enabled = !slot.isLoading && (counted || preview != null),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = when {
                        !counted -> "Contar Amostra"
                        isLast -> "Ver Resultado"
                        else -> "Próxima Amostra"
                    },
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    if (viewing && preview != null) {
        ImageViewerDialog(
            bitmap = preview,
            result = slot.result,
            title = "Amostra ${index + 1} — ${slot.result?.count ?: 0} PLs",
            onDismiss = { viewing = false },
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    radius: Dp = 24.dp,
    strokeWidth: Dp = 1.5.dp,
): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
        ),
    )
}