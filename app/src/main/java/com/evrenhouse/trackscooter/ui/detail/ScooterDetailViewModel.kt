package com.evrenhouse.trackscooter.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.SaveDeviceConditionRequest
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.UpdateScooterRequest
import com.evrenhouse.trackscooter.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailUiState(
    val scooterId: String = "",
    val scooter: Scooter? = null,
    val allScooters: List<Scooter> = emptyList(),
    val log: List<ActivityLogEntry> = emptyList(),
    val maintenance: List<MaintenanceRecord> = emptyList(),
    val loading: Boolean = true,
    val error: String? = null,
    val saving: Boolean = false,
    val completing: Boolean = false,
    val toast: String? = null,
)

class ScooterDetailViewModel(
    private val repository: ScooterRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val scooterId: String = savedStateHandle.get<String>("scooterId") ?: ""

    private val _state = MutableStateFlow(DetailUiState(scooterId = scooterId))
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            runCatching {
                val scooters = repository.getScooters()
                val log = repository.getActivityLog()
                val maintenance = repository.getMaintenanceRecords()
                Triple(scooters, log, maintenance)
            }
                .onSuccess { (scooters, log, maintenance) ->
                    _state.value = _state.value.copy(
                        scooter = scooters.firstOrNull { it.id == scooterId },
                        allScooters = scooters,
                        log = log.filter { it.scooterId == scooterId }.take(15),
                        maintenance = maintenance.filter { it.scooterId == scooterId }.take(15),
                        loading = false,
                    )
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(loading = false, error = err.toUserMessage())
                }
        }
    }

    suspend fun handleTroubleSwap(
        mode: String,
        replacementId: String?,
        structuredIssue: String,
        locationNote: String,
    ): Boolean = runCatching {
        val returnRes = repository.toggleScooter(scooterId)
        if (returnRes.success) {
            repository.notifyScooterToggled(returnRes)
        }

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

        if (mode == "swap" && !replacementId.isNullOrBlank()) {
            val checkoutRes = repository.toggleScooter(replacementId)
            if (checkoutRes.success) {
                repository.notifyScooterToggled(checkoutRes)
            }
        }

        repository.notifyDataMutated()
        refresh()
        true
    }.getOrDefault(false)

    fun saveCondition(condition: SaveDeviceConditionRequest) {
        if (_state.value.saving) return
        _state.value = _state.value.copy(saving = true)
        viewModelScope.launch {
            runCatching { repository.saveDeviceCondition(scooterId, condition) }
                .onSuccess {
                    _state.value = _state.value.copy(saving = false, toast = "Kondisi perangkat disimpan")
                    refresh()
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(saving = false, toast = err.toUserMessage())
                }
        }
    }

    fun completeMaintenance(recordId: String) {
        if (_state.value.completing) return
        _state.value = _state.value.copy(completing = true)
        viewModelScope.launch {
            runCatching { repository.completeMaintenance(recordId) }
                .onSuccess {
                    _state.value = _state.value.copy(completing = false, toast = "Maintenance selesai")
                    refresh()
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(completing = false, toast = err.toUserMessage())
                }
        }
    }

    fun consumeToast() {
        _state.value = _state.value.copy(toast = null)
    }
}
