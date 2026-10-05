package com.embasa.plcounter.ui.result

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.domain.model.BatchConfig
import com.embasa.plcounter.domain.model.LotEstimate
import com.embasa.plcounter.ui.SampleSlot
import com.embasa.plcounter.ui.components.AnnotatedImage
import com.embasa.plcounter.ui.components.AppTopBar
import com.embasa.plcounter.ui.components.Disclosure
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.ImageViewerDialog
import com.embasa.plcounter.ui.components.PrimaryButton
import com.embasa.plcounter.ui.components.SecondaryButton
import com.embasa.plcounter.ui.components.StatusPill
import com.embasa.plcounter.ui.components.WaveFill
import com.embasa.plcounter.ui.theme.Alvorada
import com.embasa.plcounter.ui.theme.AlvoradaTint
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.Raso
import com.embasa.plcounter.ui.theme.TextoSuave
import com.embasa.plcounter.ui.theme.VerdeMar
import kotlin.math.roundToLong

@Composable
fun ResultScreen(
    estimate: LotEstimate,
    config: BatchConfig?,
    slots: List<SampleSlot>,
    onWeighing: () -> Unit,
    onNewBatch: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    var viewingIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            AppTopBar()

            Column(modifier = Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Resultado do lote", style = MaterialTheme.typography.headlineMedium, color = Mare)
                Text(
                    text = "${estimate.sampleCounts.size} amostras analisadas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave,
                )
            }

            Spacer(Modifier.height(20.dp))
            WaterHero(estimate, config)

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                // Amostras: lista simples, uma divisória entre linhas (sem caixa por item).
                Column {
                    Text("Amostras", style = MaterialTheme.typography.titleMedium, color = Mare)
                    Spacer(Modifier.height(6.dp))
                    estimate.sampleCounts.forEachIndexed { i, count ->
                        val preview = slots.getOrNull(i)?.preview
                        SampleRow(
                            index = i,
                            count = count,
                            slot = slots.getOrNull(i),
                            onClick = { if (preview != null) viewingIndex = i },
                        )
                        if (i < estimate.sampleCounts.lastIndex) HorizontalDivider(color = Linha)
                    }
                }

                if (estimate.highVariability) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(AlvoradaTint)
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        IAquaIcon(IconKind.Info, tint = Alvorada, iconSize = 22.dp)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "As amostras variam bastante entre si. Misture bem o recipiente " +
                                    "e considere refazer a amostragem para uma estimativa mais confiável.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Mare,
                        )
                    }
                }

                if (config != null) {
                    CalculationDisclosure(config = config, estimate = estimate)
                }
            }
        }

        // Ações fixas no rodapé: sempre ao alcance do polegar.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SecondaryButton(
                text = "Conferir por pesagem",
                onClick = onWeighing,
                modifier = Modifier.fillMaxWidth(),
                icon = IconKind.Scale,
            )
            PrimaryButton(text = "Nova análise", onClick = onNewBatch)
        }
    }

    val viewing = viewingIndex
    if (viewing != null) {
        val slot = slots.getOrNull(viewing)
        val bitmap = slot?.preview
        if (bitmap != null) {
            ImageViewerDialog(
                bitmap = bitmap,
                result = slot?.result,
                title = "Amostra ${viewing + 1} — ${slot?.result?.count ?: 0} PLs",
                onDismiss = { viewingIndex = null },
            )
        }
    }
}

/**
 * A contagem "dentro da água": a superfície ondulada vem do logo (sol sobre o mar) e é o
 * único bloco colorido da tela; o resto fica quieto para o número ser a primeira coisa lida.
 */
@Composable
private fun WaterHero(estimate: LotEstimate, config: BatchConfig?) {
    val water = remember {
        Brush.verticalGradient(listOf(Color(0xFF2C6FD1), Cobalto, Color(0xFF0A1F6B)))
    }
    val total = remember(estimate) { Fmt.int(estimate.estimatedTotal) }
    val numberSize = when {
        total.length <= 8 -> 56.sp
        total.length <= 10 -> 46.sp
        else -> 38.sp
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 36.dp, bottomEnd = 36.dp)),
    ) {
        WaveFill(brush = water, modifier = Modifier.matchParentSize(), amplitude = 10.dp, wavelength = 240.dp)
        WaveFill(
            color = Color.White.copy(alpha = 0.08f),
            modifier = Modifier.matchParentSize().padding(top = 26.dp),
            amplitude = 8.dp,
            wavelength = 180.dp,
            phase = { 2.2f },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, bottom = 30.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (config != null) {
                    "Estimativa para ${Fmt.number(config.totalVolumeLiters)} L"
                } else {
                    "Estimativa do lote"
                },
                style = MaterialTheme.typography.titleSmall,
                color = Color.White.copy(alpha = 0.8f),
            )
            Text(
                text = total,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = numberSize),
                color = Color.White,
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "pós-larvas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f),
            )

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusPill(text = "Média ${Fmt.fixed(estimate.meanCount, 1)}", dotColor = Raso, onDark = true)
                estimate.coefficientOfVariation?.let { cv ->
                    StatusPill(
                        text = "Variação ${Fmt.percent(cv)}",
                        dotColor = if (estimate.highVariability) Alvorada else VerdeMar,
                        onDark = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun SampleRow(index: Int, count: Int, slot: SampleSlot?, onClick: () -> Unit) {
    val preview = slot?.preview
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = preview != null, onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black),
        ) {
            if (preview != null) {
                AnnotatedImage(
                    bitmap = preview,
                    result = slot?.result,
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 1.dp,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Amostra ${index + 1}", style = MaterialTheme.typography.titleSmall, color = Mare)
            if (preview != null) {
                Text("Toque para conferir", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
            }
        }
        Text("$count", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Mare)
        Spacer(Modifier.width(6.dp))
        Text("PLs", style = MaterialTheme.typography.labelLarge, color = TextoSuave)
    }
}

@Composable
private fun CalculationDisclosure(config: BatchConfig, estimate: LotEstimate) {
    val rows = remember(config, estimate) {
        val shownMean = Math.round(estimate.meanCount * 100) / 100.0

        val sign = if ((shownMean * config.scaleFactor).roundToLong() == estimate.estimatedTotal) "=" else "≈"
        listOf(
            "Volume da amostra" to "${Fmt.number(config.sampleVolumeMl)} ml",
            "Volume total" to "${Fmt.number(config.totalVolumeLiters)} L",
            "Amostras analisadas" to "${estimate.sampleCounts.size}",
            "Fator de extrapolação" to "× ${Fmt.number(config.scaleFactor)}",
        ) to Pair(
            "${Fmt.number(shownMean)} PLs (média) × ${Fmt.number(config.scaleFactor)} $sign ${Fmt.int(estimate.estimatedTotal)} PLs",
            "Fator = ${Fmt.number(config.totalVolumeLiters)} L × 1.000 ÷ ${Fmt.number(config.sampleVolumeMl)} ml",
        )
    }
    val (params, calc) = rows

    Disclosure(
        title = "Como chegamos a esse número",
        summary = "Volumes, fator e conta",
    ) {
        params.forEach { (label, value) ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                Text(value, style = MaterialTheme.typography.titleSmall, color = Mare)
            }
        }
        HorizontalDivider(color = Linha)
        Text(calc.first, style = MaterialTheme.typography.titleSmall, color = Mare)
        Text(calc.second, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
    }
}