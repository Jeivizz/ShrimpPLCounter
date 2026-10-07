package com.embasa.plcounter.ui.growth

import androidx.compose.ui.graphics.Color
import com.embasa.plcounter.domain.growth.GrowthStatus
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Coral
import com.embasa.plcounter.ui.theme.TextoSuave
import com.embasa.plcounter.ui.theme.VerdeMar
import java.time.LocalDate

/** Situação sempre em cor + texto (nunca só cor). Null = semana fora da curva de referência. */
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