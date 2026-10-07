package com.embasa.plcounter.ui.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.ui.components.Fmt
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.components.NumberField
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Coral
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave

private fun String.asDecimal(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Linha que mostra a data e abre o seletor de data do Material. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRow(label: String, epochDay: Long, onChange: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = Mare)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .clickable { open = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IAquaIcon(IconKind.Calendar, tint = Cobalto, iconSize = 22.dp)
            Spacer(Modifier.width(12.dp))
            Text(Fmt.date(epochDay), style = MaterialTheme.typography.titleMedium, color = Mare)
        }
    }

    if (open) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = epochDay * MILLIS_PER_DAY)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { onChange(Math.floorDiv(it, MILLIS_PER_DAY)) }
                    open = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancelar") } },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * Cria um lote. [initialBaselineG] vem preenchido quando o lote nasce da estimativa por pesagem
 * (peso de uma PL calculado); o campo continua editável e pode ficar vazio.
 */
@Composable
fun NewLotDialog(
    initialName: String,
    initialBaselineG: Double?,
    onConfirm: (name: String, startEpochDay: Long, baselineG: Double?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var startDay by remember { mutableStateOf(todayEpochDay()) }
    var baselineMg by remember { mutableStateOf(initialBaselineG?.let { Fmt.fixed(it * 1000.0, 2) } ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF3F7FB),
        shape = RoundedCornerShape(28.dp),
        title = { Text("Novo lote", style = MaterialTheme.typography.titleLarge, color = Mare) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (initialBaselineG != null) {
                    Text(
                        "O peso de uma PL calculado na pesagem vira o ponto inicial (semana 0) do gráfico.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextoSuave,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Nome do lote", style = MaterialTheme.typography.titleSmall, color = Mare)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; error = null },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Cobalto,
                            unfocusedBorderColor = Linha,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                        ),
                    )
                }
                DateRow("Data de início (povoamento)", startDay) { startDay = it; error = null }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Peso de uma PL (opcional)", style = MaterialTheme.typography.titleSmall, color = Mare)
                    NumberField(
                        value = baselineMg,
                        onValueChange = { baselineMg = it; error = null },
                        unit = "mg",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Coral) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val baseline = baselineMg.asDecimal()
                when {
                    baselineMg.isNotBlank() && (baseline == null || baseline <= 0) ->
                        error = "Informe um peso válido em mg, ou deixe em branco."
                    startDay > todayEpochDay() -> error = "A data de início não pode ser no futuro."
                    else -> onConfirm(name, startDay, baseline?.div(1000.0))
                }
            }) { Text("Criar lote") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

/** Registra a pesagem de UM camarão, em gramas. */
@Composable
fun WeighInDialog(
    lot: GrowthLot,
    onConfirm: (epochDay: Long, weightG: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    var weight by remember { mutableStateOf("") }
    var day by remember { mutableStateOf(todayEpochDay()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF3F7FB),
        shape = RoundedCornerShape(28.dp),
        title = { Text("Registrar pesagem", style = MaterialTheme.typography.titleLarge, color = Mare) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Peso de um camarão", style = MaterialTheme.typography.titleSmall, color = Mare)
                    NumberField(
                        value = weight,
                        onValueChange = { weight = it; error = null },
                        unit = "g",
                        modifier = Modifier.fillMaxWidth(),
                        big = true,
                    )
                }
                DateRow("Data da pesagem", day) { day = it; error = null }
                error?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Coral) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = weight.asDecimal()
                when {
                    value == null || value <= 0 -> error = "Informe o peso em gramas (maior que zero)."
                    day < lot.startEpochDay -> error = "A data não pode ser anterior ao início do lote."
                    day > todayEpochDay() -> error = "A data não pode ser no futuro."
                    else -> onConfirm(day, value)
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF3F7FB),
        shape = RoundedCornerShape(28.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = Mare) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = TextoSuave) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel, color = Coral) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}