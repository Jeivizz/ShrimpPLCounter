package com.embasa.plcounter.ui.growth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.embasa.plcounter.R
import com.embasa.plcounter.data.local.FileGrowthRepository
import com.embasa.plcounter.domain.growth.GrowthAlertConfig
import com.embasa.plcounter.domain.growth.GrowthLot
import com.embasa.plcounter.domain.growth.GrowthReference
import com.embasa.plcounter.domain.growth.GrowthRepository
import com.embasa.plcounter.domain.growth.WeighIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

data class GrowthUiState(
    val loading: Boolean = true,
    val lots: List<GrowthLot> = emptyList(),
    val selectedId: String? = null,
    val message: String? = null,
) {
    val selected: GrowthLot? get() = lots.firstOrNull { it.id == selectedId }
}

class GrowthViewModel(
    app: Application,
    private val repository: GrowthRepository,
    val reference: GrowthReference,
    val alertConfig: GrowthAlertConfig = GrowthAlertConfig(),
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(GrowthUiState())
    val state: StateFlow<GrowthUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val lots = runCatching { repository.load() }.getOrDefault(emptyList())
            _state.update { it.copy(loading = false, lots = lots) }
        }
    }

    fun select(id: String?) = _state.update { it.copy(selectedId = id) }

    fun dismissMessage() = _state.update { it.copy(message = null) }

    fun createLot(name: String, startEpochDay: Long, baselineWeightG: Double?): String {
        val lot = GrowthLot(
            id = UUID.randomUUID().toString(),
            name = name.trim().ifEmpty { "Lote" },
            startEpochDay = startEpochDay,
            baselineWeightG = baselineWeightG,
        )
        mutate { it + lot }
        return lot.id
    }

    fun addWeighIn(lotId: String, epochDay: Long, weightG: Double) =
        mutateLot(lotId) { it.withWeighIn(WeighIn(epochDay, weightG)) }

    fun removeWeighIn(lotId: String, weighIn: WeighIn) =
        mutateLot(lotId) { it.withoutWeighIn(weighIn) }

    fun deleteLot(id: String) {
        mutate { lots -> lots.filterNot { it.id == id } }
        _state.update { if (it.selectedId == id) it.copy(selectedId = null) else it }
    }

    private fun mutateLot(id: String, transform: (GrowthLot) -> GrowthLot) =
        mutate { lots -> lots.map { if (it.id == id) transform(it) else it } }

    private fun mutate(transform: (List<GrowthLot>) -> List<GrowthLot>) {
        var snapshot: List<GrowthLot> = emptyList()
        _state.update { s ->
            snapshot = transform(s.lots)
            s.copy(lots = snapshot)
        }
        val toSave = snapshot
        viewModelScope.launch {
            runCatching { repository.save(toSave) }.onFailure {
                _state.update { s -> s.copy(message = "Não foi possível salvar os dados neste aparelho.") }
            }
        }
    }
}

class GrowthViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Curva de referência: arquivo editável em res/raw; se faltar ou estiver inválido, usa o exemplo.
        val text = runCatching {
            app.resources.openRawResource(R.raw.growth_reference).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrNull()
        val reference = text?.let { runCatching { GrowthReference.parseCsv(it) }.getOrNull() } ?: GrowthReference.example()
        val alertConfig = text?.let { runCatching { GrowthAlertConfig.parse(it) }.getOrNull() } ?: GrowthAlertConfig()
        val repository = FileGrowthRepository(File(app.filesDir, "growth_lots.json"))
        return GrowthViewModel(app, repository, reference, alertConfig) as T
    }
}