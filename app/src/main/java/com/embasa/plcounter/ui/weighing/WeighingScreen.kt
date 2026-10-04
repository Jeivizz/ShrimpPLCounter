package com.embasa.plcounter.ui.weighing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.domain.model.Agreement
import com.embasa.plcounter.domain.model.LotEstimate
import com.embasa.plcounter.ui.UiState
import com.embasa.plcounter.ui.components.IAquaHeader
import com.embasa.plcounter.ui.theme.IAquaBlue
import com.embasa.plcounter.ui.theme.IAquaChip
import com.embasa.plcounter.ui.theme.IAquaTipCard
import com.embasa.plcounter.ui.theme.IAquaTitleBlue
import java.text.NumberFormat
import java.util.Locale

private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

private fun decimals(value: Double, digits: Int): String = String.format(PT_BR, "%.${digits}f", value)

private fun formatWeight(grams: Double): String =
    if (grams >= 1000.0) "${decimals(grams / 1000.0, 2)} kg" else "${decimals(grams, 1)} g"

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

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IAquaHeader()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Estimativa por Pesagem",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IAquaTipCard),
                border = BorderStroke(1.dp, IAquaChip),
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Método de referência para conferir a contagem por imagem.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IAquaTitleBlue,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        "Pese as PLs de cada amostra, escorrendo a água sempre do mesmo jeito (inclusive " +
                                "ao pesar o total). Água retida nas PLs aumenta o peso. Use uma balança de " +
                                "precisão (0,001 g).",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // 1. Peso de cada amostra
            SectionCard(title = "1. Peso das amostras") {
                estimate.sampleCounts.forEachIndexed { i, count ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Amostra ${i + 1}", fontWeight = FontWeight.SemiBold)
                            Text(
                                "$count PLs contadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        OutlinedTextField(
                            value = state.sampleWeightTexts[i],
                            onValueChange = { onSampleWeightChange(i, it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("PESO") },
                            suffix = { Text("g") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp),
                        )
                    }
                }
            }

            // 2. Peso por PL e peso esperado do lote
            if (weight != null) {
                SectionCard(title = "2. Peso estimado") {
                    Text("PESO MÉDIO DE UMA PL", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${decimals(weight.weightPerPlG * 1000.0, 2)} mg",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IAquaBlue,
                    )
                    weight.coefficientOfVariation?.let { cv ->
                        Text(
                            text = "Variação entre amostras: " + String.format(PT_BR, "%.1f%%", cv * 100),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "PESO ESPERADO DO LOTE (${NumberFormat.getIntegerInstance(PT_BR).format(estimate.estimatedTotal)} PLs)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatWeight(weight.expectedTotalWeightG),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IAquaBlue,
                    )
                }

                // 3. Conferência com o peso real do total
                SectionCard(title = "3. Conferir com o peso do total (opcional)") {
                    Text(
                        "Pese todas as PLs do lote e informe abaixo para comparar com a estimativa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedTextField(
                        value = state.totalWeightText,
                        onValueChange = onTotalWeightChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("PESO TOTAL MEDIDO") },
                        suffix = { Text("kg") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                    )

                    if (comparison != null) {
                        val (label, color) = when (comparison.agreement) {
                            Agreement.GOOD -> "Boa concordância" to IAquaBlue
                            Agreement.MODERATE -> "Concordância moderada" to MaterialTheme.colorScheme.onSurface
                            Agreement.LOW -> "Baixa concordância" to MaterialTheme.colorScheme.error
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Contagem por imagem")
                            Text(
                                NumberFormat.getIntegerInstance(PT_BR).format(estimate.estimatedTotal),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Contagem por peso")
                            Text(
                                NumberFormat.getIntegerInstance(PT_BR).format(comparison.countByWeight),
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Diferença")
                            Text(
                                String.format(PT_BR, "%+.1f%%", comparison.deviation * 100),
                                fontWeight = FontWeight.Bold,
                                color = color,
                            )
                        }
                        Text(label, color = color, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (comparison.deviation >= 0) {
                                "O peso do lote indica mais PLs do que a contagem por imagem."
                            } else {
                                "O peso do lote indica menos PLs do que a contagem por imagem."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Text(
                    "Informe o peso das três amostras para calcular o peso de uma PL.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) { Text("Voltar ao Resultado", fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            content()
        }
    }
}