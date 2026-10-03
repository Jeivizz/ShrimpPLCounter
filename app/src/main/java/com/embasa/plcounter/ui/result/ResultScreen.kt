package com.embasa.plcounter.ui.result

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.domain.model.LotEstimate
import com.embasa.plcounter.ui.SampleSlot
import com.embasa.plcounter.ui.components.AnnotatedImage
import com.embasa.plcounter.ui.components.IAquaHeader
import com.embasa.plcounter.ui.components.ImageViewerDialog
import com.embasa.plcounter.ui.theme.IAquaBlue
import com.embasa.plcounter.ui.theme.IAquaTipCard
import java.text.NumberFormat
import java.util.Locale

private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

@Composable
fun ResultScreen(
    estimate: LotEstimate,
    slots: List<SampleSlot>,
    onNewBatch: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    var viewingIndex by remember { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        IAquaHeader()

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Resultado do Lote",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "ESTIMATIVA TOTAL DE PL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = NumberFormat.getIntegerInstance(PT_BR).format(estimate.estimatedTotal),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IAquaBlue,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Média por amostra: " + String.format(PT_BR, "%.1f", estimate.meanCount),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    estimate.coefficientOfVariation?.let { cv ->
                        Text(
                            text = "Variação entre amostras: " + String.format(PT_BR, "%.1f%%", cv * 100),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Contagem por amostra", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Toque na miniatura para conferir as marcações.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    estimate.sampleCounts.forEachIndexed { i, count ->
                        val preview = slots.getOrNull(i)?.preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = preview != null) { viewingIndex = i },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            if (preview != null) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black),
                                ) {
                                    AnnotatedImage(
                                        bitmap = preview,
                                        result = slots[i].result,
                                        modifier = Modifier.fillMaxSize(),
                                        strokeWidth = 1.dp,
                                    )
                                }
                            }
                            Text("Amostra ${i + 1}", modifier = Modifier.weight(1f))
                            Text("$count PLs", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (estimate.highVariability) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = IAquaTipCard),
                ) {
                    Text(
                        text = "As amostras variam bastante entre si. Misture bem o recipiente e " +
                                "considere refazer a amostragem para uma estimativa mais confiável.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Button(
                onClick = onNewBatch,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
            ) { Text("Nova Análise", fontWeight = FontWeight.Bold) }

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
            ) { Text("Voltar") }
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