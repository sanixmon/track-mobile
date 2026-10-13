package com.evrenhouse.trackscooter.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.ToggleResponse
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.SweetAlertType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.HttpException
import java.util.concurrent.atomic.AtomicLong

data class ScanUiState(
    val scanning: Boolean = true,
    val lastScannedId: String = "",
    val lastScannedAt: Long = 0L,
    val busy: Boolean = false,
    val pendingScooter: Scooter? = null,
    val pendingBreakText: String? = null,
    val toast: String? = null,
    val toastType: SweetAlertType = SweetAlertType.INFO,
    val toastId: Long = 0L,
)

class ScanViewModel(
    private val repository: ScooterRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    private val toastIdGen = AtomicLong(0L)
    private var scanJob: Job? = null
    private var confirmJob: Job? = null

    fun consumeToast() {
        _state.value = _state.value.copy(toast = null)
    }

    fun dismissConfirmation() {
        confirmJob?.cancel()
        confirmJob = null
        _state.value = _state.value.copy(
            busy = false,
            pendingScooter = null,
            pendingBreakText = null,
            scanning = true,
        )
    }

    /** Handle a scanned id: pause scanning, lookup scooter, open confirmation dialog. */
    fun onScanned(id: String) {
        val s = _state.value
        if (!s.scanning || s.busy || s.pendingScooter != null) return

        val now = System.currentTimeMillis()
        if (s.lastScannedId == id && now - s.lastScannedAt < 2500) return

        val cleanId = id.trim().uppercase()
        if (cleanId.isBlank()) return

        _state.value = s.copy(scanning = false, busy = true, lastScannedId = id, lastScannedAt = now)

        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            try {
                val scooter = runCatching {
                    // Cek data cache di repository terlebih dahulu agar instan dan tahan error 502
                    repository.getCachedScooter(cleanId) ?: withTimeout(8_000L) {
                        val scooters = repository.getScooters()
                        scooters.find { it.id.equals(cleanId, ignoreCase = true) }
                    }
                }.recoverCatching { err ->
                    if (err is CancellationException && err !is kotlinx.coroutines.TimeoutCancellationException) {
                        throw err
                    }
                    repository.getCachedScooter(cleanId) ?: throw err
                }.getOrThrow()

                if (scooter != null) {
                    _state.value = _state.value.copy(
                        busy = false,
                        pendingScooter = scooter,
                        pendingBreakText = null,
                    )
                } else {
                    _state.value = _state.value.copy(
                        busy = false,
                        scanning = true,
                        toast = "Unit $cleanId tidak ditemukan.",
                        toastType = SweetAlertType.ERROR,
                        toastId = toastIdGen.incrementAndGet(),
                    )
                }
            } catch (e: CancellationException) {
                if (e is kotlinx.coroutines.TimeoutCancellationException) {
                    _state.value = _state.value.copy(
                        busy = false,
                        scanning = true,
                        toast = e.toUserMessage(),
                        toastType = SweetAlertType.ERROR,
                        toastId = toastIdGen.incrementAndGet(),
                    )
                } else {
                    _state.value = _state.value.copy(busy = false, scanning = true)
                    throw e
                }
            } catch (err: Throwable) {
                _state.value = _state.value.copy(
                    busy = false,
                    scanning = true,
                    toast = err.toUserMessage(),
                    toastType = SweetAlertType.ERROR,
                    toastId = toastIdGen.incrementAndGet(),
                )
            }
        }
    }

    /** Execute the toggle action confirmed by the user in the dialog. */
    fun confirmScan() {
        val current = _state.value
        val scooter = current.pendingScooter ?: return
        if (current.busy || confirmJob?.isActive == true) return

        val isReturn = scooter.status == ScooterStatus.IN_USE
        val isMaintenance = scooter.status == ScooterStatus.MAINTENANCE

        _state.value = current.copy(busy = true)
        confirmJob = viewModelScope.launch {
            try {
                val res = withTimeout(8_000L) {
                    runCatching {
                        if (isReturn) {
                            repository.returnScooterIdempotent(scooter.id)
                        } else {
                            repository.checkoutScooterIdempotent(scooter.id, forceMaintenance = isMaintenance)
                        }
                    }.recoverCatching { err ->
                        if (err is HttpException && err.code() in 502..504) {
                            delay(350)
                            if (isReturn) {
                                repository.returnScooterIdempotent(scooter.id)
                            } else {
                                repository.checkoutScooterIdempotent(scooter.id, forceMaintenance = isMaintenance)
                            }
                        } else {
                            throw err
                        }
                    }.getOrThrow()
                }

                val isSuccess = res.success
                _state.value = _state.value.copy(
                    busy = false,
                    pendingScooter = null,
                    pendingBreakText = null,
                    scanning = true,
                    toast = resultMessage(res, scooter.id, isReturn),
                    toastType = if (isSuccess) SweetAlertType.SUCCESS else SweetAlertType.ERROR,
                    toastId = toastIdGen.incrementAndGet(),
                )
            } catch (e: CancellationException) {
                if (e is kotlinx.coroutines.TimeoutCancellationException) {
                    handleConfirmFailure(scooter, isReturn, e)
                } else {
                    // Cancelled manually (e.g. user tapped Batal / Close)
                    _state.value = _state.value.copy(
                        busy = false,
                        pendingScooter = null,
                        pendingBreakText = null,
                        scanning = true,
                    )
                    throw e
                }
            } catch (err: Throwable) {
                handleConfirmFailure(scooter, isReturn, err)
            } finally {
                confirmJob = null
                if (_state.value.busy) {
                    _state.value = _state.value.copy(
                        busy = false,
                        pendingScooter = null,
                        pendingBreakText = null,
                        scanning = true,
                    )
                }
            }
        }
    }

    private suspend fun handleConfirmFailure(
        scooter: Scooter,
        isReturn: Boolean,
        err: Throwable,
    ) {
        if (isReturn) {
            val enqueued = withTimeoutOrNull(2_000L) {
                repository.returnQueue.enqueue(scooter.id, scooter.currentOutlet)
            } ?: false
            if (enqueued) {
                val optimisticScooter = scooter.copy(status = ScooterStatus.AVAILABLE)
                repository.notifyScooterToggled(
                    ToggleResponse(
                        success = true,
                        action = "return",
                        message = "Pengembalian unit ${scooter.id} masuk antrian offline.",
                        scooter = optimisticScooter,
                    )
                )
                _state.value = _state.value.copy(
                    busy = false,
                    pendingScooter = null,
                    pendingBreakText = null,
                    scanning = true,
                    toast = "Sinyal lemah: Pengembalian unit ${scooter.id} disimpan ke antrian offline dan akan dikirim otomatis saat online.",
                    toastType = SweetAlertType.WARNING,
                    toastId = toastIdGen.incrementAndGet(),
                )
                return
            }
        }
        val actionLabel = if (isReturn) "mengembalikan" else "menyewakan"
        _state.value = _state.value.copy(
            busy = false,
            pendingScooter = null,
            pendingBreakText = null,
            scanning = true,
            toast = "Gagal $actionLabel unit ${scooter.id}: ${err.toUserMessage()}",
            toastType = SweetAlertType.ERROR,
            toastId = toastIdGen.incrementAndGet(),
        )
    }

    private fun resultMessage(res: ToggleResponse, fallbackId: String, isReturn: Boolean): String {
        if (!res.message.isNullOrBlank()) {
            return res.message
        }
        val unitId = res.scooter?.id ?: fallbackId
        return if (res.success) {
            val isCheckout = res.action == "checkout" || (!isReturn && res.action == null)
            "Unit $unitId berhasil ${if (isCheckout) "disewakan" else "dikembalikan"}."
        } else {
            "Gagal memproses unit $unitId."
        }
    }
}
