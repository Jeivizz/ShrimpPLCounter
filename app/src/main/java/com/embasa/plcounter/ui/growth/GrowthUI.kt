package com.embasa.plcounter.ui.growth

import androidx.compose.ui.graphics.Color
import com.embasa.plcounter.domain.growth.AlertKind
import com.embasa.plcounter.domain.growth.AlertSeverity
import com.embasa.plcounter.domain.growth.GrowthAlert
import com.embasa.plcounter.domain.growth.GrowthStatus
import com.embasa.plcounter.ui.theme.Alvorada
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Coral
import com.embasa.plcounter.ui.theme.TextoSuave
import com.embasa.plcounter.ui.theme.VerdeMar
import java.time.LocalDate

fun statusLabel(status: GrowthStatus?): String = when (status) {
    GrowthStatus.Below -> "Abaixo do esperado"
    GrowthStatus.Within -> "Dentro da faixa"
    GrowthStatus.Above -> "Acima do esperado"
    null -> "Sem referência"
}

fun statusColor(status: GrowthStatus?): Color = when (status) {
    GrowthStatus.Below -> Coral
    GrowthStatus.Within -> VerdeMar
    GrowthStatus.Above -> Cobalto
    null -> TextoSuave
}

fun todayEpochDay(): Long = LocalDate.now().toEpochDay()

fun severityLabel(severity: AlertSeverity): String = when (severity) {
    AlertSeverity.Critical -> "Alerta"
    AlertSeverity.Attention -> "Atenção"
    AlertSeverity.Info -> "Aviso"
}

fun severityColor(severity: AlertSeverity): Color = when (severity) {
    AlertSeverity.Critical -> Coral
    AlertSeverity.Attention -> Alvorada
    AlertSeverity.Info -> Cobalto
}

class AlertText(val title: String, val body: String)

private fun plural(n: Int, one: String, many: String) = if (n == 1) "$n $one" else "$n $many"

fun alertText(alert: GrowthAlert): AlertText = when (alert.kind) {
    AlertKind.BelowRange -> AlertText(
        "Crescimento abaixo do esperado",
        "${plural(alert.count, "pesagem seguida ficou", "pesagens seguidas ficaram")} abaixo da faixa da semana. " +
                "Crescimento menor que o normal pode estar ligado a problemas no viveiro (como qualidade da água " +
                "ou alimentação) ou à saúde dos animais. Confira os parâmetros do viveiro e acione o responsável " +
                "técnico. O app não faz diagnóstico.",
    )
    AlertKind.AboveRange -> AlertText(
        "Crescimento acima do esperado",
        "${plural(alert.count, "pesagem seguida ficou", "pesagens seguidas ficaram")} acima da faixa. " +
                "Confirme se os animais pesados representam o lote e se a curva de referência se aplica a este cultivo.",
    )
    AlertKind.SlowGrowth -> AlertText(
        "Crescimento desacelerado",
        "Nas últimas ${plural(alert.count, "semana", "semanas")} o ganho de peso foi bem menor que o esperado, " +
                "mesmo que o peso ainda esteja dentro da faixa. Vale conferir o viveiro e o manejo.",
    )
    AlertKind.OverdueWeighing -> AlertText(
        "Pesagem atrasada",
        "Faz ${alert.count} dias sem pesagem. Pesagens semanais ajudam a perceber problemas cedo.",
    )
}