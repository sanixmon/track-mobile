package com.evrenhouse.trackscooter.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.ScooterStatus
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
    val pendingScooter: Scooter? = null,
    val pendingBreakText: String? = null,
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

    fun dismissConfirmation() {
        _state.value = _state.value.copy(pendingScooter = null, pendingBreakText = null, scanning = true)
    }

    /** Handle a scanned id: pause scanning, lookup scooter, open confirmation dialog. */
    fun onScanned(id: String) {
        val s = _state.value
        if (!s.scanning || s.busy || s.pendingScooter != null) return

        val now = System.currentTimeMillis()
        if (s.lastScannedId == id && now - s.lastScannedAt < 3000) return

        val cleanId = id.trim().uppercase()
        _state.value = s.copy(scanning = false, busy = true, lastScannedId = id, lastScannedAt = now)

        viewModelScope.launch {
            runCatching {
                val scooters = repository.getScooters()
                scooters.find { it.id.equals(cleanId, ignoreCase = true) }
            }.onSuccess { scooter ->
                if (scooter != null) {
                    _state.value = _state.value.copy(
                        busy = false,
                        pendingScooter = scooter,
                        pendingBreakText = null
                    )
                } else {
                    _state.value = _state.value.copy(
                        busy = false,
                        scanning = true,
                        toast = "Scooter $cleanId tidak ditemukan."
                    )
                }
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    busy = false,
                    scanning = true,
                    toast = err.toUserMessage()
                )
            }
        }
    }

    /** Execute the toggle action confirmed by the user in the dialog. */
    fun confirmScan() {
        val scooter = _state.value.pendingScooter ?: return
        val isReturn = scooter.status == ScooterStatus.IN_USE

        _state.value = _state.value.copy(busy = true)
        viewModelScope.launch {
            runCatching {
                if (isReturn) {
                    repository.returnScooter(scooter.id)
                } else {
                    repository.checkoutScooter(scooter.id)
                }
            }.onSuccess { res ->
                if (res.success) {
                    repository.notifyScooterToggled(res)
                }
                _state.value = _state.value.copy(
                    busy = false,
                    pendingScooter = null,
                    scanning = true,
                    toast = resultMessage(res)
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(
                    busy = false,
                    pendingScooter = null,
                    scanning = true,
                    toast = err.toUserMessage()
                )
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
