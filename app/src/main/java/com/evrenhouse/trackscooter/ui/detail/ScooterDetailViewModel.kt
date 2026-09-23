package com.evrenhouse.trackscooter.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.SaveDeviceConditionRequest
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.UpdateScooterRequest
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.TechnicalActivity
import com.evrenhouse.trackscooter.util.DateUtils
import java.time.LocalDate
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
    val technicalActivities: List<TechnicalActivity> = emptyList(),
    val historyLoading: Boolean = false,
    val historyError: String? = null,
    val historyDatePreset: String = "all",
    val historyCategoryFilter: String = "all",
    val historyCustomStart: String = "",
    val historyCustomEnd: String = "",
)

class ScooterDetailViewModel(
    private val repository: ScooterRepository,
    savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    private val initialId: String = savedStateHandle?.get<String>("scooterId") ?: ""

    private val _state = MutableStateFlow(DetailUiState(scooterId = initialId))
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        if (initialId.isNotBlank()) {
            loadScooter(initialId)
        }
    }

    fun loadScooter(id: String) {
        val cleanId = id.trim().uppercase()
        if (cleanId.isBlank()) return

        _state.value = _state.value.copy(scooterId = cleanId, loading = true, error = null)
        viewModelScope.launch {
            runCatching {
                val scooters = repository.getScooters()
                val log = repository.getActivityLog()
                val maintenance = repository.getMaintenanceRecords()
                Triple(scooters, log, maintenance)
            }
                .onSuccess { (scooters, log, maintenance) ->
                    val foundScooter = scooters.firstOrNull { it.id.equals(cleanId, ignoreCase = true) }
                    _state.value = _state.value.copy(
                        scooterId = cleanId,
                        scooter = foundScooter,
                        allScooters = scooters,
                        log = log.filter { it.scooterId.equals(cleanId, ignoreCase = true) }.take(15),
                        maintenance = maintenance.filter { it.scooterId.equals(cleanId, ignoreCase = true) }.take(15),
                        loading = false,
                        error = if (foundScooter == null) "Unit $cleanId tidak ditemukan." else null,
                    )
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(loading = false, error = err.toUserMessage())
                }
        }
    }

    fun refresh() {
        val currentId = _state.value.scooterId
        if (currentId.isNotBlank()) {
            loadScooter(currentId)
        }
    }

    suspend fun swapScooter(
        scooterId: String,
        replacementId: String,
        note: String,
        issue: String? = null,
        markBroken: Boolean = false,
    ): Boolean = runCatching {
        val res = repository.swapScooter(scooterId, replacementId, note, issue, markBroken)
        if (res.success) {
            repository.notifyDataMutated()
            refresh()
        }
        res.success
    }.getOrDefault(false)

    fun saveCondition(condition: SaveDeviceConditionRequest) {
        val currentId = _state.value.scooterId
        if (currentId.isBlank() || _state.value.saving) return
        _state.value = _state.value.copy(saving = true)
        viewModelScope.launch {
            runCatching { repository.saveDeviceCondition(currentId, condition) }
                .onSuccess {
                    _state.value = _state.value.copy(saving = false, toast = "Kondisi perangkat disimpan")
                    loadScooter(currentId)
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(saving = false, toast = err.toUserMessage())
                }
        }
    }

    fun updateOutlet(newOutlet: String) {
        val currentId = _state.value.scooterId
        if (currentId.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.updateScooter(
                    currentId,
                    UpdateScooterRequest(currentOutlet = newOutlet)
                )
            }
                .onSuccess {
                    _state.value = _state.value.copy(toast = "Outlet unit berhasil diubah")
                    repository.notifyDataMutated()
                    loadScooter(currentId)
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(toast = err.toUserMessage())
                }
        }
    }

    fun updateStatus(
        newStatus: String,
        location: String? = null,
        locationDetail: String? = null,
        issue: String? = null,
        note: String? = null,
    ) {
        val currentId = _state.value.scooterId
        if (currentId.isBlank()) return
        viewModelScope.launch {
            val req = UpdateScooterRequest(
                status = newStatus,
                location = location,
                locationDetail = locationDetail,
                issue = issue,
                note = note,
                maintenanceNote = if (newStatus == ScooterStatus.MAINTENANCE) (issue ?: note) else null,
            )
            runCatching { repository.updateScooter(currentId, req) }
                .onSuccess {
                    _state.value = _state.value.copy(toast = "Status unit berhasil diperbarui")
                    repository.notifyDataMutated()
                    loadScooter(currentId)
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(toast = err.toUserMessage())
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


    fun loadTechnicalHistory(startDate: String? = null, endDate: String? = null) {
        val id = _state.value.scooterId.ifBlank { return }
        _state.value = _state.value.copy(historyLoading = true, historyError = null)
        viewModelScope.launch {
            runCatching {
                repository.getScooterTechnicalHistory(id, startDate, endDate)
            }.onSuccess { res ->
                _state.value = _state.value.copy(
                    historyLoading = false,
                    technicalActivities = res.activities,
                    historyError = null
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    historyLoading = false,
                    historyError = err.toUserMessage()
                )
            }
        }
    }

    fun setHistoryDatePreset(preset: String, customStart: String = "", customEnd: String = "") {
        _state.value = _state.value.copy(
            historyDatePreset = preset,
            historyCustomStart = customStart,
            historyCustomEnd = customEnd
        )
        val today = LocalDate.now(DateUtils.WIB)
        val (start, end) = when (preset) {
            "today" -> today.toString() to today.toString()
            "7d" -> today.minusDays(7).toString() to today.toString()
            "30d" -> today.minusDays(30).toString() to today.toString()
            "month" -> today.withDayOfMonth(1).toString() to today.toString()
            "custom" -> (customStart.ifBlank { null }) to (customEnd.ifBlank { null })
            else -> null to null
        }
        loadTechnicalHistory(start, end)
    }

    fun setHistoryCategoryFilter(category: String) {
        _state.value = _state.value.copy(historyCategoryFilter = category)
    }
    fun consumeToast() {
        _state.value = _state.value.copy(toast = null)
    }
}
