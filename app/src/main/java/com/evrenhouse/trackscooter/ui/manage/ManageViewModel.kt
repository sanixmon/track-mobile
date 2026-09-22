package com.evrenhouse.trackscooter.ui.manage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.UpdateScooterRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ManageUiState(
    val busy: Boolean = false,
    val error: String? = null,
    val toast: String? = null,
)

class ManageViewModel(
    private val repository: ScooterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ManageUiState())
    val state: StateFlow<ManageUiState> = _state.asStateFlow()

    suspend fun addScooter(
        id: String?,
        type: String,
        currentOutlet: String? = null,
        ownership: String? = "outlet"
    ): Boolean =
        runCatching { repository.addScooter(id, type, currentOutlet, ownership) }.isSuccess
    suspend fun deleteScooter(id: String): Boolean =
        runCatching { repository.deleteScooter(id) }.isSuccess

    suspend fun updateStatus(id: String, request: UpdateScooterRequest): Boolean =
        runCatching { repository.updateScooter(id, request) }.isSuccess

    suspend fun downloadBackupBytes(): ByteArray? =
        runCatching { repository.downloadBackup().bytes() }.getOrNull()

    fun setError(msg: String?) {
        _state.value = _state.value.copy(error = msg)
    }

    fun setBusy(busy: Boolean) {
        _state.value = _state.value.copy(busy = busy)
    }

    fun setToast(msg: String?) {
        _state.value = _state.value.copy(toast = msg)
    }

    fun consumeToast() {
        _state.value = _state.value.copy(toast = null)
    }
}
