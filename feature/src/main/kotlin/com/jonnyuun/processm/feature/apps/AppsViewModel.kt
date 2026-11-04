package com.jonnyuun.processm.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jonnyuun.processm.core.model.AppSummary
import com.jonnyuun.processm.core.system.ProcessSnapshot
import com.jonnyuun.processm.domain.AppsRepository
import com.jonnyuun.processm.domain.ProcessRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AppsUiState(
    val apps: List<AppSummary> = emptyList(),
    val query: String = "",
    val running: List<ProcessSnapshot> = emptyList(),
    val runningQuery: String = "",
)

class AppsViewModel(
    private val appsRepository: AppsRepository,
    private val processRepository: ProcessRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AppsUiState())
    val state: StateFlow<AppsUiState> = _state.asStateFlow()

    private var appsJob: Job? = null
    private var processesJob: Job? = null

    init {
        loadApps()
        loadProcesses()
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        loadApps()
    }

    fun onRunningQueryChanged(query: String) {
        _state.update { it.copy(runningQuery = query) }
        loadProcesses()
    }

    private fun loadApps() {
        appsJob?.cancel()
        appsJob = viewModelScope.launch {
            while (isActive) {
                val query = _state.value.query
                val all = appsRepository.listApps()
                val filtered = if (query.isBlank()) all else {
                    all.filter { app ->
                        app.label.lowercase().contains(query.lowercase()) ||
                            app.packageName.lowercase().contains(query.lowercase())
                    }
                }
                _state.update { it.copy(apps = filtered) }
                break
            }
        }
    }

    private fun loadProcesses() {
        processesJob?.cancel()
        processesJob = viewModelScope.launch {
            while (isActive) {
                val query = _state.value.runningQuery
                val all = processRepository.runningProcesses()
                val filtered = if (query.isBlank()) all else {
                    all.filter { p ->
                        (p.packageName ?: p.processName).lowercase().contains(query.lowercase()) ||
                            p.processName.lowercase().contains(query.lowercase())
                    }
                }
                _state.update { it.copy(running = filtered) }
                // Re-poll cada intervalo corto para mostrar uso de RAM en tiempo real.
                val delayMs = 2_000L
                if (isActive) {
                    kotlinx.coroutines.delay(delayMs)
                }
            }
        }
    }

    companion object {
        fun factory(appsRepository: AppsRepository, processRepository: ProcessRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { AppsViewModel(appsRepository, processRepository) }
            }
    }
}
EOF
echo done