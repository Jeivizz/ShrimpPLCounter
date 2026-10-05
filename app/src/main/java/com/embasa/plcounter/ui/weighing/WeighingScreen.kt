package com.embasa.plcounter.ui.weighing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.domain.model.Agreement
import com.embasa.plcounter.domain.model.LotEstimate
import com.embasa.plcounter.ui.UiState
import com.embasa.plcounter.ui.components.AppTopBar
import com.embasa.plcounter.ui.components.Disclosure
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.NumberField
import com.embasa.plcounter.ui.components.PrimaryButton
import com.embasa.plcounter.ui.components.ReadoutField
import com.embasa.plcounter.ui.components.StatusPill
import com.embasa.plcounter.ui.theme.Alvorada
import com.embasa.plcounter.ui.theme.AlvoradaTint
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Coral
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave
import com.embasa.plcounter.ui.theme.VerdeMar

private fun formatWeight(grams: Double): String =
    if (grams >= 1000.0) "${Fmt.fixed(grams / 1000.0, 2)} kg" else "${Fmt.fixed(grams, 1)} g"

@Composable
fun WeighingScreen(
    state: UiState,
    estimate: LotEstimate,
    onSampleWeightChange: (Int, String) -> Unit,
    onTotalWeightChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    val weight = state.weightEstimate
    val comparison = state.weightComparison
    val estimatedText = remember(estimate) { Fmt.int(estimate.estimatedTotal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
    ) {
        AppTopBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Conferência por pesagem", style = MaterialTheme.typography.headlineMedium, color = Mare)
                Text(
                    "Um método de referência para avaliar a confiança da contagem por imagem.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave,
                )
            }

            Disclosure(
                title = "Como pesar",
                container = AlvoradaTint,
                borderColor = null,
                startExpanded = true,
                leading = { IAquaIcon(IconKind.Scale, tint = Alvorada, iconSize = 24.dp) },
            ) {
                Text(
                    "Escorra a água sempre do mesmo jeito, nas amostras e no lote: a água retida " +
                            "nas PLs soma peso. Use balança de precisão (0,001 g).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mare,
                )
            }

            // Peso de cada amostra
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Peso de cada amostra", style = MaterialTheme.typography.titleMedium, color = Mare)
                estimate.sampleCounts.forEachIndexed { i, count ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Amostra ${i + 1}", style = MaterialTheme.typography.titleSmall, color = Mare)
                            Text("$count PLs contadas", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                        }
                        NumberField(
                            value = state.sampleWeightTexts[i],
                            onValueChange = { onSampleWeightChange(i, it) },
                            unit = "g",
                            modifier = Modifier.width(152.dp),
                        )
                    }
                    if (i < estimate.sampleCounts.lastIndex) HorizontalDivider(color = Linha)
                }
            }

            if (weight != null) {
                // Leitura: o que a pesagem das amostras diz sobre o lote.
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("O que a pesagem indica", style = MaterialTheme.typography.titleMedium, color = Mare)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Readout("Peso de uma PL", "${Fmt.fixed(weight.weightPerPlG * 1000.0, 2)} mg", Modifier.weight(1f))
                        Readout("Peso esperado do lote", formatWeight(weight.expectedTotalWeightG), Modifier.weight(1f))
                    }
                    weight.coefficientOfVariation?.let { cv ->
                        Text(
                            text = "Variação do peso entre as amostras: ${Fmt.percent(cv)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextoSuave,
                        )
                    }
                }

                // Conferência com o peso real do lote
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Conferir com o peso do lote", style = MaterialTheme.typography.titleMedium, color = Mare)
                    Text(
                        "Opcional: pese todas as PLs do lote e informe para comparar com a estimativa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave,
                    )
                    ReadoutField(
                        label = "Peso total medido",
                        value = state.totalWeightText,
                        onValueChange = onTotalWeightChange,
                        unit = "kg",
                        icon = IconKind.Scale,
                    )

                    if (comparison != null) {
                        val (label, color) = when (comparison.agreement) {
                            Agreement.GOOD -> "Boa concordância" to VerdeMar
                            Agreement.MODERATE -> "Concordância moderada" to Alvorada
                            Agreement.LOW -> "Baixa concordância" to Coral
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CompareRow("Contagem por imagem", estimatedText)
                            CompareRow("Contagem por peso", Fmt.int(comparison.countByWeight))
                            HorizontalDivider(color = Linha)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = Fmt.percent(comparison.deviation, signed = true),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Mare,
                                )
                                StatusPill(text = label, dotColor = color)
                            }
                            Text(
                                text = if (comparison.deviation >= 0) {
                                    "O peso do lote indica mais PLs do que a contagem por imagem."
                                } else {
                                    "O peso do lote indica menos PLs do que a contagem por imagem."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextoSuave,
                            )
                        }
                    }
                }
            } else {
                Text(
                    "Informe o peso das três amostras para calcular o peso de uma PL.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave,
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        PrimaryButton(
            text = "Voltar ao resultado",
            onClick = onBack,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp).navigationBarsPadding(),
        )
    }
}

@Composable
private fun Readout(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 24.sp),
            fontWeight = FontWeight.ExtraBold,
            color = Cobalto,
            maxLines = 1,
        )
    }
}

@Composable
private fun CompareRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
        Text(value, style = MaterialTheme.typography.titleSmall, color = Mare)
    }
}