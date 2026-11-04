package com.jonnyuun.processm.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jonnyuun.processm.core.model.AppSummary
import com.jonnyuun.processm.domain.AppsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AppsUiState(
    val apps: List<AppSummary> = emptyList(),
    val query: String = "",
    val loading: Boolean = false,
)

class AppsViewModel(
    private val repository: AppsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AppsUiState())
    val state: StateFlow<AppsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadApps()
    }

    fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query) }
        loadApps()
    }

    private fun loadApps() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            while (isActive) {
                val query = _state.value.query
                val all = repository.listApps()
                val filtered = if (query.isBlank()) all else {
                    all.filter { app ->
                        app.label.lowercase().contains(query.lowercase()) ||
                            app.packageName.lowercase().contains(query.lowercase())
                    }
                }
                _state.update { it.copy(apps = filtered, loading = false) }
                // No polling: apps change only on explicit refresh or install/uninstall.
                // Keep coroutine alive briefly then exit; reload on query change.
                break
            }
        }
    }

    companion object {
        fun factory(repository: AppsRepository): ViewModelProvider.Factory = viewModelFactory {
            initializer { AppsViewModel(repository) }
        }
    }
}
EOF
echo done