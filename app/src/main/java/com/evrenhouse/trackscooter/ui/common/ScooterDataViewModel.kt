package com.evrenhouse.trackscooter.ui.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.LocalDataUpdate
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.UpdateScooterRequest
import com.evrenhouse.trackscooter.data.toUserMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import java.time.Instant

data class ScooterDataUiState(
    val scooters: List<Scooter> = emptyList(),
    val activityLog: List<ActivityLogEntry> = emptyList(),
    val maintenanceRecords: List<MaintenanceRecord> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val isLiveConnected: Boolean = false,
    val isReconnecting: Boolean = false,
    val error: String? = null,
)

/**
 * Shared data holder for Dashboard / Monitor / Manage. Fetches everything the
 * app needs with real-time SSE streaming (zero-delay) and silent background
 * polling fallback every 30s.
 */
class ScooterDataViewModel(
    private val repository: ScooterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScooterDataUiState())
    val state: StateFlow<ScooterDataUiState> = _state.asStateFlow()

    private var pollingJob: Job? = null
    private var streamJob: Job? = null
    private var localUpdatesJob: Job? = null

    init {
        refresh()
        observeLocalUpdates()
        observeStream()
        startPolling()
    }

    /** Complete a repair record then refresh. Throws on failure. */
    suspend fun completeMaintenance(recordId: String) {
        repository.completeMaintenance(recordId)
        repository.notifyDataMutated()
    }

    /** Handle trouble/battery drop on the road: return old unit, mark maintenance (luar), optional swap checkout. */
    suspend fun handleTroubleSwap(
        scooterId: String,
        mode: String,
        replacementId: String?,
        structuredIssue: String,
        locationNote: String,
    ) {
        // 1. Return old scooter to balance activity log
        val returnRes = repository.toggleScooter(scooterId)
        if (returnRes.success) {
            repository.notifyScooterToggled(returnRes)
        }

        // 2. Mark old scooter as maintenance in field
        repository.updateScooter(
            scooterId,
            UpdateScooterRequest(
                status = ScooterStatus.MAINTENANCE,
                location = "luar",
                issue = structuredIssue,
                note = locationNote.ifBlank { null },
                maintenanceNote = structuredIssue,
            ),
        )

        // 3. If mode == "swap", checkout replacement scooter
        if (mode == "swap" && !replacementId.isNullOrBlank()) {
            val checkoutRes = repository.toggleScooter(replacementId)
            if (checkoutRes.success) {
                repository.notifyScooterToggled(checkoutRes)
            }
        }

        repository.notifyDataMutated()
        refresh()
    }

    fun refresh(silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent && _state.value.scooters.isNotEmpty()) {
                _state.value = _state.value.copy(refreshing = true)
            }
            runCatching { repository.fetchAll() }
                .onSuccess { data ->
                    _state.value = _state.value.copy(
                        scooters = data.scooters,
                        activityLog = data.activityLog,
                        maintenanceRecords = data.maintenanceRecords,
                        loading = false,
                        refreshing = false,
                        error = null,
                    )
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(
                        loading = false,
                        refreshing = false,
                        error = if (_state.value.scooters.isEmpty()) err.toUserMessage() else _state.value.error,
                    )
                }
        }
    }

    private fun observeLocalUpdates() {
        localUpdatesJob?.cancel()
        localUpdatesJob = viewModelScope.launch {
            repository.localUpdates.collect { update ->
                when (update) {
                    is LocalDataUpdate.Toggled -> {
                        val res = update.response
                        val updatedScooter = res.scooter
                        val action = res.action
                        if (updatedScooter != null) {
                            val currentScooters = _state.value.scooters.toMutableList()
                            val idx = currentScooters.indexOfFirst { it.id == updatedScooter.id }
                            if (idx != -1) {
                                currentScooters[idx] = updatedScooter
                            } else {
                                currentScooters.add(updatedScooter)
                            }
                            val currentLogs = _state.value.activityLog.toMutableList()
                            if (action != null) {
                                currentLogs.add(
                                    0,
                                    ActivityLogEntry(
                                        id = "local-${System.currentTimeMillis()}",
                                        scooterId = updatedScooter.id,
                                        scooterType = updatedScooter.type,
                                        action = action,
                                        timestamp = Instant.now().toString(),
                                    )
                                )
                            }
                            _state.value = _state.value.copy(
                                scooters = currentScooters,
                                activityLog = currentLogs,
                            )
                        }
                        refresh(silent = true)
                    }
                    is LocalDataUpdate.DataMutated -> {
                        refresh(silent = true)
                    }
                }
            }
        }
    }

    private fun observeStream() {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            repository.observeEvents()
                .onStart {
                    _state.value = _state.value.copy(isLiveConnected = true, isReconnecting = false)
                }
                .retryWhen { cause, attempt ->
                    _state.value = _state.value.copy(isLiveConnected = false, isReconnecting = true)
                    val backoff = (1000L * (1 shl attempt.coerceAtMost(4).toInt())).coerceAtMost(10_000L)
                    Log.w("ScooterDataVM", "SSE disconnect (attempt $attempt), reconnecting in ${backoff}ms...", cause)
                    delay(backoff)
                    refresh(silent = true)
                    true
                }
                .catch { err ->
                    Log.w("ScooterDataVM", "SSE stream exception: ${err.message}")
                    _state.value = _state.value.copy(isLiveConnected = false, isReconnecting = false)
                }
                .collect { _ ->
                    _state.value = _state.value.copy(isLiveConnected = true, isReconnecting = false)
                    refresh(silent = true)
                }
        }
    }

    private fun startPolling() {
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(30_000)
                refresh(silent = true)
            }
        }
    }

    override fun onCleared() {
        localUpdatesJob?.cancel()
        streamJob?.cancel()
        pollingJob?.cancel()
        super.onCleared()
    }
}
