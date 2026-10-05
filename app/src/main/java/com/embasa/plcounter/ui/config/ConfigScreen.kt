package com.embasa.plcounter.ui.config

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.ui.UiState
import com.embasa.plcounter.ui.components.AppTopBar
import com.embasa.plcounter.ui.components.CaptureTips
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.PrimaryButton
import com.embasa.plcounter.ui.components.ReadoutField
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Coral
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave

private fun String.asDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

@Composable
fun ConfigScreen(
    state: UiState,
    onSampleVolumeChange: (String) -> Unit,
    onTotalVolumeChange: (String) -> Unit,
    onStart: () -> Unit,
) {
    // Mostra ao vivo o que os dois volumes significam para a contagem.
    val factor = remember(state.sampleVolumeText, state.totalVolumeText) {
        val sample = state.sampleVolumeText.asDecimal()
        val total = state.totalVolumeText.asDecimal()
        if (sample != null && total != null && sample > 0 && total > 0) total * 1000.0 / sample else null
    }

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
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nova análise", style = MaterialTheme.typography.headlineMedium, color = Mare)
                Text(
                    "Informe os volumes. O total do lote é estimado pela média de 3 amostras.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave,
                )
            }

            ReadoutField(
                label = "Volume de cada amostra",
                value = state.sampleVolumeText,
                onValueChange = onSampleVolumeChange,
                unit = "ml",
                icon = IconKind.Drop,
            )
            ReadoutField(
                label = "Volume total do lote",
                value = state.totalVolumeText,
                onValueChange = onTotalVolumeChange,
                unit = "L",
                icon = IconKind.Waves,
            )

            // Linha de leitura: o fator que liga amostra e lote.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IAquaIcon(IconKind.Info, tint = Cobalto, iconSize = 22.dp)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (factor != null) {
                        "Cada PL contada na amostra representa ${Fmt.number(factor)} no lote."
                    } else {
                        "Preencha os dois volumes para ver o fator de extrapolação."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mare,
                )
            }

            state.configError?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Coral)
            }

            CaptureTips(startExpanded = true)
            Spacer(Modifier.height(4.dp))
        }

        PrimaryButton(
            text = "Iniciar amostragem",
            onClick = onStart,
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .navigationBarsPadding(),
        )
    }
}