package com.embasa.plcounter.ui.growth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.domain.growth.GrowthAlertConfig
import com.embasa.plcounter.domain.growth.GrowthAlerts
import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthReference
import com.embasa.plcounter.ui.components.AppTopBar
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.PrimaryButton
import com.embasa.plcounter.ui.components.StatusPill
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave

@Composable
fun GrowthHomeScreen(
    state: GrowthUiState,
    reference: GrowthReference,
    alertConfig: GrowthAlertConfig,
    onOpen: (String) -> Unit,
    onNewLot: () -> Unit,
) {
    val today = todayEpochDay()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppTopBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Crescimento", style = MaterialTheme.typography.headlineMedium, color = Mare)
                Text(
                    "Pese um camarão por semana e compare com a curva esperada.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave,
                )
            }
            Spacer(Modifier.height(12.dp))

            if (state.lots.isEmpty() && !state.loading) {
                EmptyState()
            } else {
                state.lots.sortedByDescending { it.startEpochDay }.forEach { lot ->
                    LotRow(lot = lot, reference = reference, alertConfig = alertConfig, today = today, onClick = { onOpen(lot.id) })
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        PrimaryButton(
            text = "Novo lote",
            onClick = onNewLot,
            icon = IconKind.Plus,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(84.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { IAquaIcon(IconKind.Chart, tint = Cobalto, iconSize = 38.dp) }
        Text("Nenhum lote acompanhado", style = MaterialTheme.typography.titleMedium, color = Mare)
        Text(
            "Crie um lote aqui, ou, ao terminar a estimativa por pesagem, toque em " +
                    "\"Acompanhar crescimento deste lote\" para já usar o peso de uma PL como ponto inicial.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextoSuave,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LotRow(lot: GrowthLot, reference: GrowthReference, alertConfig: GrowthAlertConfig, today: Long, onClick: () -> Unit) {
    val latest = lot.latest()
    val evaluation = latest?.let { reference.evaluate(lot.weekOf(it.epochDay), it.weightG) }
    val ageWeeks = Math.floorDiv(today - lot.startEpochDay, 7L)
    val topAlert = GrowthAlerts.evaluate(lot, reference, alertConfig, today).firstOrNull()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(lot.name, style = MaterialTheme.typography.titleMedium, color = Mare, modifier = Modifier.weight(1f, fill = false))
                if (topAlert != null) StatusPill(severityLabel(topAlert.severity), severityColor(topAlert.severity))
            }
            Text(
                "Semana $ageWeeks • início ${Fmt.date(lot.startEpochDay)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextoSuave,
            )
            Spacer(Modifier.height(4.dp))
            if (latest != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(Fmt.weightG(latest.weightG), style = MaterialTheme.typography.titleSmall, color = Mare)
                    StatusPill(statusLabel(evaluation?.status), statusColor(evaluation?.status))
                }
            } else {
                Text("Sem pesagens ainda", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
            }
        }
        Spacer(Modifier.width(8.dp))
        IAquaIcon(
            IconKind.Chevron,
            tint = Linha,
            iconSize = 22.dp,
            modifier = Modifier.graphicsLayer { rotationZ = -90f },
        )
    }
}