package com.evrenhouse.trackscooter.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScooterDataUiState(
    val scooters: List<Scooter> = emptyList(),
    val activityLog: List<ActivityLogEntry> = emptyList(),
    val maintenanceRecords: List<MaintenanceRecord> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
)

/**
 * Shared data holder for Dashboard / Monitor / Manage. Fetches everything the
 * app needs and silently polls every 30s (mirrors web useScooterData).
 */
class ScooterDataViewModel(
    private val repository: ScooterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScooterDataUiState())
    val state: StateFlow<ScooterDataUiState> = _state.asStateFlow()

    private var pollingJob: Job? = null

    init {
        refresh()
        startPolling()
    }

    /** Complete a repair record then refresh. Throws on failure. */
    suspend fun completeMaintenance(recordId: String) {
        repository.completeMaintenance(recordId)
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching { repository.fetchAll() }
                .onSuccess { data ->
                    _state.value = ScooterDataUiState(
                        scooters = data.scooters,
                        activityLog = data.activityLog,
                        maintenanceRecords = data.maintenanceRecords,
                        loading = false,
                    )
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(
                        loading = false,
                        error = err.toUserMessage(),
                    )
                }
        }
    }

    private fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(30_000)
                refresh()
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
