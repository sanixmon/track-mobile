package com.evrenhouse.trackscooter.ui.common

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.AppUpdateInfo
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
import com.evrenhouse.trackscooter.TrackScooterApp
import com.evrenhouse.trackscooter.util.OutletPrefs
import com.evrenhouse.trackscooter.util.UpdatePolicy
import com.evrenhouse.trackscooter.util.UpdatePrefs
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

    private val _selectedOutlet = MutableStateFlow(
        runCatching { OutletPrefs.getSelectedOutlet(TrackScooterApp.instance) }.getOrDefault("all")
    )
    val selectedOutlet: StateFlow<String> = _selectedOutlet.asStateFlow()

    fun setSelectedOutlet(outletId: String) {
        _selectedOutlet.value = outletId
        runCatching { OutletPrefs.setSelectedOutlet(TrackScooterApp.instance, outletId) }
    }

    private val _appUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val appUpdate: StateFlow<AppUpdateInfo?> = _appUpdate.asStateFlow()

    private val _isCheckingUpdate = MutableStateFlow(false)
    val isCheckingUpdate: StateFlow<Boolean> = _isCheckingUpdate.asStateFlow()

    fun checkForAppUpdate(forceRecheck: Boolean = false, onResult: ((AppUpdateInfo) -> Unit)? = null) {
        // Throttle: resume beruntun tidak perlu cek ulang terus.
        val now = System.currentTimeMillis()
        if (!forceRecheck && now - lastUpdateCheckAt < UpdatePolicy.RESUME_THROTTLE_MS) {
            return
        }
        lastUpdateCheckAt = now
        viewModelScope.launch {
            _isCheckingUpdate.value = true
            val info = repository.checkAppUpdate()
            _isCheckingUpdate.value = false
            if (info.isUpdateAvailable && shouldRemindUpdate(info)) {
                _appUpdate.value = info
            }
            onResult?.invoke(info)
        }
    }

    private var lastUpdateCheckAt: Long = 0L

    /**
     * User yang menekan "Nanti Saja" tetap diingatkan lagi: sekali sehari
     * atau segera jika ada versi lebih baru. Force update selalu tampil.
     */
    private fun shouldRemindUpdate(info: AppUpdateInfo): Boolean {
        if (info.forceUpdate) return true
        return runCatching {
            val ctx = TrackScooterApp.instance
            UpdatePolicy.shouldShowUpdate(
                forceUpdate = false,
                latestCode = info.latestVersionCode,
                dismissedCode = UpdatePrefs.getDismissedVersionCode(ctx),
                dismissedAt = UpdatePrefs.getDismissedAt(ctx),
                nowMillis = System.currentTimeMillis(),
            )
        }.getOrDefault(true)
    }

    fun dismissUpdateDialog() {
        val current = _appUpdate.value
        // Force update tidak bisa di-dismiss permanen; dialognya pun non-dismissible.
        if (current != null && !current.forceUpdate) {
            runCatching {
                UpdatePrefs.setDismissed(
                    TrackScooterApp.instance,
                    current.latestVersionCode,
                    System.currentTimeMillis(),
                )
            }
        }
        _appUpdate.value = null
    }

    private var pollingJob: Job? = null
    private var streamJob: Job? = null
    private var localUpdatesJob: Job? = null
    private var updateCheckJob: Job? = null

    init {
        refresh()
        observeLocalUpdates()
        observeStream()
        startPolling()
        checkForAppUpdate()
        startUpdateRecheck()
    }

    /** Complete a repair record then refresh. Throws on failure. */
    suspend fun completeMaintenance(recordId: String) {
        repository.completeMaintenance(recordId)
        repository.notifyDataMutated()
    }

    /** Execute atomic swap on backend inheriting rental start time (1:1 with web). */
    suspend fun swapScooter(
        scooterId: String,
        replacementId: String,
        note: String,
        issue: String? = null,
        markBroken: Boolean = false
    ): Boolean = runCatching {
        val res = repository.swapScooter(scooterId, replacementId, note, issue, markBroken)
        if (res.success) {
            repository.notifyDataMutated()
            refresh(silent = true)
        }
        res.success
    }.getOrDefault(false)

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

    /**
     * Cek update berkala agar user versi lama yang sempat offline/gagal
     * saat cek pertama, atau yang men-dismiss, tetap dapat notif.
     */
    private fun startUpdateRecheck() {
        updateCheckJob?.cancel()
        updateCheckJob = viewModelScope.launch {
            while (true) {
                delay(UpdatePolicy.RECHECK_INTERVAL_MS)
                // Jangan timpa dialog yang sedang tampil.
                if (_appUpdate.value == null) {
                    checkForAppUpdate()
                }
            }
        }
    }

    override fun onCleared() {
        localUpdatesJob?.cancel()
        streamJob?.cancel()
        pollingJob?.cancel()
        updateCheckJob?.cancel()
        super.onCleared()
    }
}
