package com.embasa.plcounter.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.embasa.plcounter.camera.ImagePreparer
import com.embasa.plcounter.data.local.LocalCountingRepository
import com.embasa.plcounter.domain.CountingRepository
import com.embasa.plcounter.domain.EstimateCalculator
import com.embasa.plcounter.domain.model.BatchConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class BatchViewModel(
    app: Application,
    private val repository: CountingRepository,
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    // Bytes JPEG de cada amostra: ficam fora do UiState para não inflar o estado.
    private val images = arrayOfNulls<ImagePreparer.Prepared>(SAMPLE_COUNT)

    // ---------- Splash / Config ----------

    fun onSplashFinished() {
        if (_state.value.screen == Screen.Splash) {
            _state.update { it.copy(screen = Screen.Config) }
        }
    }

    fun onSampleVolumeChange(text: String) =
        _state.update { it.copy(sampleVolumeText = text, configError = null) }

    fun onTotalVolumeChange(text: String) =
        _state.update { it.copy(totalVolumeText = text, configError = null) }

    fun startSampling() {
        val s = _state.value
        val sample = s.sampleVolumeText.toDecimalOrNull()
        val total = s.totalVolumeText.toDecimalOrNull()
        if (sample == null || sample <= 0 || total == null || total <= 0) {
            _state.update { it.copy(configError = "Informe volumes válidos, maiores que zero.") }
            return
        }
        images.fill(null)
        _state.update {
            it.copy(
                config = BatchConfig(sampleVolumeMl = sample, totalVolumeLiters = total),
                screen = Screen.Sampling,
                slots = List(SAMPLE_COUNT) { SampleSlot() },
                currentIndex = 0,
                estimate = null,
                configError = null,
            )
        }
    }

    // ---------- Amostragem ----------

    fun onImagePicked(uri: Uri) {
        val index = _state.value.currentIndex
        // Se a foto veio da câmera, volta para a tela de amostragem.
        _state.update { if (it.screen == Screen.Camera) it.copy(screen = Screen.Sampling) else it }
        updateSlot(index) { SampleSlot(isLoading = true) }
        viewModelScope.launch {
            try {
                val prepared = withContext(Dispatchers.IO) {
                    val p = ImagePreparer.prepare(getApplication<Application>().contentResolver, uri)
                    // Arquivo temporário da câmera: não precisamos mais dele.
                    if (uri.scheme == "file") uri.path?.let { File(it).delete() }
                    p
                }
                images[index] = prepared
                updateSlot(index) { SampleSlot(preview = prepared.bitmap) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateSlot(index) { SampleSlot(error = "Não foi possível abrir a imagem.") }
            }
        }
    }

    fun openCamera() = _state.update { it.copy(screen = Screen.Camera) }

    fun closeCamera() = _state.update { it.copy(screen = Screen.Sampling) }

    fun countCurrent() {
        val index = _state.value.currentIndex
        val prepared = images[index] ?: return
        if (_state.value.slots[index].isLoading) return

        updateSlot(index) { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.countSample(prepared.jpegBytes, "amostra_${index + 1}.jpg")
                .onSuccess { result ->
                    updateSlot(index) { it.copy(isLoading = false, result = result) }
                }
                .onFailure { e ->
                    updateSlot(index) {
                        it.copy(isLoading = false, error = e.message ?: "Erro inesperado.")
                    }
                }
        }
    }

    fun nextOrFinish() {
        val s = _state.value
        if (s.slots[s.currentIndex].result == null) return
        if (s.currentIndex < SAMPLE_COUNT - 1) {
            _state.update { it.copy(currentIndex = it.currentIndex + 1) }
        } else {
            finish()
        }
    }

    fun backToConfig() = _state.update { it.copy(screen = Screen.Config) }

    // ---------- Resultado ----------

    private fun finish() {
        val s = _state.value
        val config = s.config ?: return
        val counts = s.slots.mapNotNull { it.result?.count }
        if (counts.isEmpty()) return
        _state.update {
            it.copy(estimate = EstimateCalculator.estimate(config, counts), screen = Screen.Result)
        }
    }

    fun newBatch() {
        images.fill(null)
        _state.update {
            UiState(
                screen = Screen.Config,
                sampleVolumeText = it.sampleVolumeText,
                totalVolumeText = it.totalVolumeText,
            )
        }
    }

    // ---------- helpers ----------

    private fun updateSlot(index: Int, transform: (SampleSlot) -> SampleSlot) {
        _state.update { s ->
            s.copy(slots = s.slots.mapIndexed { i, slot -> if (i == index) transform(slot) else slot })
        }
    }

    /** Aceita vírgula ou ponto como separador decimal. */
    private fun String.toDecimalOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()
}

class BatchViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        BatchViewModel(app, LocalCountingRepository()) as T
}