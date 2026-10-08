package com.embasa.plcounter.ui.growth

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.domain.growth.GrowthAlertConfig
import com.embasa.plcounter.domain.growth.GrowthAlerts
import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthReference
import com.embasa.plcounter.domain.growth.WeighIn
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.PrimaryButton
import com.embasa.plcounter.ui.components.StatusPill
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Coral
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave

@Composable
fun GrowthDetailScreen(
    lot: GrowthLot,
    reference: GrowthReference,
    alertConfig: GrowthAlertConfig,
    onAddWeighIn: (epochDay: Long, weightG: Double) -> Unit,
    onRemoveWeighIn: (WeighIn) -> Unit,
    onDeleteLot: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)

    var showAdd by remember { mutableStateOf(false) }
    var removing by remember { mutableStateOf<WeighIn?>(null) }
    var deletingLot by remember { mutableStateOf(false) }

    val model = remember(lot, reference) { GrowthChartModel.build(lot, reference) }
    // Janela visível: começa enquadrando os dados e volta a eles quando os dados mudam.
    var viewport by remember(model.fit) { mutableStateOf(model.fit) }
    val today = todayEpochDay()
    val weekNow = lot.weekOf(today)
    val latest = lot.latest()
    val latestEval = latest?.let { reference.evaluate(lot.weekOf(it.epochDay), it.weightG) }
    val expectedNow = reference.rangeAt(weekNow)
    val alerts = remember(lot, reference, alertConfig, today) { GrowthAlerts.evaluate(lot, reference, alertConfig, today) }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Voltar
            Row(
                modifier = Modifier.clip(RoundedCornerShape(50)).clickable(onClick = onBack).padding(end = 12.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IAquaIcon(IconKind.Chevron, tint = Cobalto, iconSize = 24.dp, modifier = Modifier.graphicsLayer { rotationZ = 90f })
                Text("Lotes", style = MaterialTheme.typography.labelLarge, color = Cobalto)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(lot.name, style = MaterialTheme.typography.headlineMedium, color = Mare)
                Text(
                    "Início em ${Fmt.date(lot.startEpochDay)} • semana ${Math.floorDiv(today - lot.startEpochDay, 7L)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextoSuave,
                )
            }

            AlertBanner(alerts)

            // Leituras
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Readout(
                    label = "Última pesagem",
                    value = latest?.let { Fmt.weightG(it.weightG) } ?: "—",
                    modifier = Modifier.weight(1f),
                ) {
                    if (latest != null) StatusPill(statusLabel(latestEval?.status), statusColor(latestEval?.status))
                }
                Readout(
                    label = "Esperado nesta semana",
                    value = expectedNow?.let { "${Fmt.weightG(it.minG)} – ${Fmt.weightG(it.maxG)}" } ?: "—",
                    modifier = Modifier.weight(1f),
                    valueSmall = true,
                ) {
                    if (expectedNow == null) {
                        Text("Fora da curva de referência", style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                    }
                }
            }

            // Gráfico
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color.White).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Peso × semanas", style = MaterialTheme.typography.titleSmall, color = Mare)
                    ChartZoomControls(
                        onZoomIn = { viewport = model.constrain(viewport.zoomed(1.5, viewport.xCenter, viewport.yCenter)) },
                        onZoomOut = { viewport = model.constrain(viewport.zoomed(1 / 1.5, viewport.xCenter, viewport.yCenter)) },
                        onFit = { viewport = model.fit },
                        fitEnabled = viewport != model.fit,
                    )
                }
                GrowthChart(model = model, viewport = viewport, onViewportChange = { viewport = it })
                GrowthLegend()
                Text(
                    text = "Pince para ampliar • arraste para mover • toque duplo para ajustar",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextoSuave,
                )
                Text(
                    text = "Referência: ${reference.name ?: "curva do arquivo growth_reference.csv"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextoSuave,
                )
            }

            // Pesagens
            Column {
                Text("Pesagens", style = MaterialTheme.typography.titleMedium, color = Mare)
                Spacer(Modifier.height(4.dp))
                if (lot.baselineWeightG != null) {
                    HistoryRow(
                        title = "Início (semana 0)",
                        subtitle = "Peso de uma PL, da estimativa",
                        weight = Fmt.weightG(lot.baselineWeightG),
                        pill = null,
                        onDelete = null,
                    )
                    HorizontalDivider(color = Linha)
                }
                val entries = lot.sortedWeighIns()
                if (entries.isEmpty()) {
                    Text(
                        "Nenhuma pesagem registrada. Toque em \"Registrar pesagem\" a cada semana.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextoSuave,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
                entries.forEachIndexed { i, entry ->
                    val week = lot.weekOf(entry.epochDay)
                    val eval = reference.evaluate(week, entry.weightG)
                    HistoryRow(
                        title = Fmt.date(entry.epochDay),
                        subtitle = "Semana ${Math.floor(week).toInt()}" +
                                (eval?.let { " • ${Fmt.percent(it.deviationFromTypical, signed = true)} do típico" } ?: ""),
                        weight = Fmt.weightG(entry.weightG),
                        pill = statusLabel(eval?.status) to statusColor(eval?.status),
                        onDelete = { removing = entry },
                    )
                    if (i < entries.lastIndex) HorizontalDivider(color = Linha)
                }
            }

            TextButton(onClick = { deletingLot = true }) {
                IAquaIcon(IconKind.Trash, tint = Coral, iconSize = 20.dp)
                Spacer(Modifier.width(8.dp))
                Text("Excluir lote", color = Coral)
            }
        }

        PrimaryButton(
            text = "Registrar pesagem",
            onClick = { showAdd = true },
            icon = IconKind.Plus,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
        )
    }

    if (showAdd) {
        WeighInDialog(
            lot = lot,
            onConfirm = { day, weight -> onAddWeighIn(day, weight); showAdd = false },
            onDismiss = { showAdd = false },
        )
    }
    removing?.let { entry ->
        ConfirmDialog(
            title = "Excluir pesagem?",
            message = "A pesagem de ${Fmt.date(entry.epochDay)} (${Fmt.weightG(entry.weightG)}) será removida.",
            confirmLabel = "Excluir",
            onConfirm = { onRemoveWeighIn(entry); removing = null },
            onDismiss = { removing = null },
        )
    }
    if (deletingLot) {
        ConfirmDialog(
            title = "Excluir lote?",
            message = "\"${lot.name}\" e todas as suas pesagens serão apagados deste aparelho.",
            confirmLabel = "Excluir lote",
            onConfirm = { deletingLot = false; onDeleteLot() },
            onDismiss = { deletingLot = false },
        )
    }
}

@Composable
private fun Readout(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueSmall: Boolean = false,
    extra: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(20.dp)).background(Color.White).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
        Text(
            text = value,
            style = if (valueSmall) MaterialTheme.typography.titleSmall else MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (valueSmall) Mare else Cobalto,
        )
        extra()
    }
}

@Composable
private fun HistoryRow(
    title: String,
    subtitle: String,
    weight: String,
    pill: Pair<String, Color>?,
    onDelete: (() -> Unit)?,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Mare)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
            if (pill != null) StatusPill(pill.first, pill.second)
        }
        Text(weight, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Mare)
        if (onDelete != null) {
            Box(
                modifier = Modifier.padding(start = 8.dp).size(40.dp).clip(RoundedCornerShape(50)).clickable(onClick = onDelete),
                contentAlignment = Alignment.Center,
            ) { IAquaIcon(IconKind.Trash, tint = TextoSuave, iconSize = 20.dp) }
        } else {
            Spacer(Modifier.width(48.dp))
        }
    }
}