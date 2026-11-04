package com.jonnyuun.processm.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jonnyuun.processm.core.model.CpuSnapshot
import com.jonnyuun.processm.core.model.MemorySnapshot
import com.jonnyuun.processm.core.model.Metric
import com.jonnyuun.processm.core.model.StorageSnapshot
import com.jonnyuun.processm.domain.MetricsRepository
import java.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class OverviewUiState(
    val memory: Metric<MemorySnapshot>? = null,
    val cpu: Metric<CpuSnapshot>? = null,
    val storage: Metric<StorageSnapshot>? = null,
    val lastUpdate: Instant? = null,
    val memoryHistory: List<Float> = emptyList(),
    val cpuHistory: List<Float> = emptyList(),
)

class OverviewViewModel(
    private val repository: MetricsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OverviewUiState())
    val state: StateFlow<OverviewUiState> = _state.asStateFlow()

    private var pollJob: Job? = null

    init {
        startPolling()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                val memory = repository.memory()
                val cpu = repository.cpu()
                val storage = repository.storage()

                val memoryFraction = (memory as? Metric.Available<MemorySnapshot>)?.value?.usedFraction?.toFloat()
                val cpuFraction = ((cpu as? Metric.Available<CpuSnapshot>)?.value?.usagePercent)?.div(100.0)?.toFloat()

                _state.update { previous ->
                    previous.copy(
                        memory = memory,
                        cpu = cpu,
                        storage = storage,
                        lastUpdate = Instant.now(),
                        memoryHistory = appendHistory(previous.memoryHistory, memoryFraction),
                        cpuHistory = appendHistory(previous.cpuHistory, cpuFraction),
                    )
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    companion object {
        private const val POLL_INTERVAL_MS = 2_000L
        private const val HISTORY_SIZE = 30

        fun factory(repository: MetricsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { OverviewViewModel(repository) }
        }
    }

    private fun appendHistory(history: List<Float>, value: Float?): List<Float> {
        if (value == null) return history
        return (history + value).takeLast(HISTORY_SIZE)
    }
}
