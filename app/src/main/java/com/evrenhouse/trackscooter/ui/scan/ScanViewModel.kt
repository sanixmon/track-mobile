package com.evrenhouse.trackscooter.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.ToggleResponse
import com.evrenhouse.trackscooter.data.toUserMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ScanUiState(
    val scanning: Boolean = true,
    val lastScannedId: String = "",
    val lastScannedAt: Long = 0L,
    val busy: Boolean = false,
    val confirmation: ToggleResponse? = null,
    val toast: String? = null,
)

class ScanViewModel(
    private val repository: ScooterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    fun consumeToast() {
        _state.value = _state.value.copy(toast = null)
    }

    fun consumeConfirmation() {
        _state.value = _state.value.copy(confirmation = null, scanning = true)
    }

    /** Handle a scanned id: debounce 3s, toggle, and possibly ask for confirmation. */
    fun onScanned(id: String) {
        val s = _state.value
        if (!s.scanning || s.busy) return

        val now = System.currentTimeMillis()
        if (s.lastScannedId == id && now - s.lastScannedAt < 3000) return

        _state.value = s.copy(scanning = false, busy = true, lastScannedId = id, lastScannedAt = now)

        viewModelScope.launch {
            runCatching { repository.toggleScooter(id) }
                .onSuccess { res ->
                    if (res.requiresConfirmation) {
                        _state.value = _state.value.copy(busy = false, confirmation = res)
                    } else {
                        _state.value = _state.value.copy(
                            busy = false,
                            toast = resultMessage(res),
                        )
                    }
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(
                        busy = false,
                        scanning = true,
                        toast = err.toUserMessage(),
                    )
                }
        }
    }

    /** User confirmed renting a maintenance/rusak unit. */
    fun forceToggle(id: String) {
        _state.value = _state.value.copy(busy = true, confirmation = null)
        viewModelScope.launch {
            runCatching { repository.toggleScooter(id, forceMaintenance = true) }
                .onSuccess { res ->
                    _state.value = _state.value.copy(busy = false, toast = resultMessage(res))
                }
                .onFailure { err ->
                    _state.value = _state.value.copy(busy = false, scanning = true, toast = err.toUserMessage())
                }
        }
    }

    private fun resultMessage(res: ToggleResponse): String {
        val unit = res.scooter?.id?.let { "Unit $it " } ?: ""
        return if (res.success) {
            val isCheckout = res.action == "checkout"
            "${unit}${if (isCheckout) "Disewakan" else "Dikembalikan"}"
        } else {
            res.message ?: "Gagal"
        }
    }
}
