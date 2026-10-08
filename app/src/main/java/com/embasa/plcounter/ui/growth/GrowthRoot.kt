package com.embasa.plcounter.ui.growth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun GrowthRoot(growthVm: GrowthViewModel, onNewLot: () -> Unit) {
    val state by growthVm.state.collectAsState()

    AnimatedContent(
        targetState = state.selectedId,
        transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
        label = "crescimento",
    ) { selectedId ->
        val lot = state.lots.firstOrNull { it.id == selectedId }
        if (lot == null) {
            GrowthHomeScreen(
                state = state,
                reference = growthVm.reference,
                alertConfig = growthVm.alertConfig,
                onOpen = growthVm::select,
                onNewLot = onNewLot,
            )
        } else {
            GrowthDetailScreen(
                lot = lot,
                reference = growthVm.reference,
                alertConfig = growthVm.alertConfig,
                onAddWeighIn = { day, weight -> growthVm.addWeighIn(lot.id, day, weight) },
                onRemoveWeighIn = { growthVm.removeWeighIn(lot.id, it) },
                onDeleteLot = { growthVm.deleteLot(lot.id) },
                onBack = { growthVm.select(null) },
            )
        }
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = growthVm::dismissMessage,
            title = { Text("Atenção") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = growthVm::dismissMessage) { Text("OK") } },
        )
    }
}