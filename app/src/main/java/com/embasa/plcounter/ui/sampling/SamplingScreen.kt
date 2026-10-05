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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.ui.SAMPLE_COUNT
import com.embasa.plcounter.ui.UiState
import com.embasa.plcounter.ui.components.AnnotatedImage
import com.embasa.plcounter.ui.components.AppTopBar
import com.embasa.plcounter.ui.components.CaptureTips
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.ImageViewerDialog
import com.embasa.plcounter.ui.components.PrimaryButton
import com.embasa.plcounter.ui.components.SecondaryButton
import com.embasa.plcounter.ui.components.StepTrack
import com.embasa.plcounter.ui.theme.Alvorada
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Coral
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave

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
        AppTopBar(
            trailing = {
                Text(
                    text = "Amostra ${index + 1} de $SAMPLE_COUNT",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextoSuave,
                )
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            StepTrack(
                total = SAMPLE_COUNT,
                current = index,
                done = state.slots.map { it.result != null },
                modifier = Modifier.padding(horizontal = 4.dp),
            )

            CaptureTips()

            // Área de captura: o resultado aparece sobre a própria foto.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(if (preview != null) Color.Black else MaterialTheme.colorScheme.surfaceVariant)
                    .then(if (preview == null) Modifier.dashedBorder(Cobalto.copy(alpha = 0.35f)) else Modifier)
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier.size(84.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            IAquaIcon(IconKind.Camera, tint = Cobalto, iconSize = 38.dp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Foto da amostra ${index + 1}",
                            style = MaterialTheme.typography.titleMedium,
                            color = Mare,
                        )
                        Text(
                            text = "Toque aqui ou use os botões abaixo",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave,
                        )
                    }
                }

                // Resultado: a contagem em destaque, em cima da foto.
                slot.result?.let { result ->
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(Alvorada))
                        Spacer(Modifier.size(10.dp))
                        Text(
                            text = "${result.count}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Mare,
                        )
                        Spacer(Modifier.size(6.dp))
                        Text("PLs", style = MaterialTheme.typography.labelLarge, color = TextoSuave)
                    }
                    Text(
                        text = "Toque para ampliar",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.55f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }

                if (slot.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (counted || preview != null) "Contando…" else "Abrindo foto…",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                        )
                    }
                }
            }

            slot.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Coral)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton(
                    text = "Galeria",
                    onClick = onPickGallery,
                    enabled = !slot.isLoading,
                    modifier = Modifier.weight(1f),
                    icon = IconKind.Gallery,
                )
                SecondaryButton(
                    text = "Câmera",
                    onClick = onOpenCamera,
                    enabled = !slot.isLoading,
                    modifier = Modifier.weight(1f),
                    icon = IconKind.Camera,
                )
            }

            PrimaryButton(
                text = when {
                    !counted -> "Contar amostra"
                    isLast -> "Ver resultado"
                    else -> "Próxima amostra"
                },
                onClick = { if (counted) onNext() else onCount() },
                enabled = !slot.isLoading && (counted || preview != null),
                modifier = Modifier.padding(bottom = 14.dp),
            )
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

private fun Modifier.dashedBorder(color: Color, radius: Float = 28f): Modifier = drawBehind {
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(radius.dp.toPx()),
        style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
        ),
    )
}