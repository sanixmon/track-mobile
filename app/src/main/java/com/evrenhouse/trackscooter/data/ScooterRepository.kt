package com.evrenhouse.trackscooter.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Single gateway to the TrackScooter backend. Translates low-level HTTP
 * failures into friendly Indonesian messages, mirroring the web app's
 * storage.js behaviour.
 */
class ScooterRepository(private val api: ApiService = ApiClient.service) {

    /** Fresh snapshot of everything the dashboard needs, fetched in parallel. */
    suspend fun fetchAll(): DashboardData = withContext(Dispatchers.IO) {
        coroutineScope {
            val scooters = async { api.getScooters() }
            val log = async { api.getActivityLog() }
            val maintenance = async { api.getMaintenanceRecords() }
            DashboardData(scooters.await(), log.await(), maintenance.await())
        }
    }

    suspend fun getScooters(): List<Scooter> = withContext(Dispatchers.IO) { api.getScooters() }

    suspend fun getActivityLog(): List<ActivityLogEntry> = withContext(Dispatchers.IO) { api.getActivityLog() }

    suspend fun getMaintenanceRecords(): List<MaintenanceRecord> = withContext(Dispatchers.IO) {
        api.getMaintenanceRecords()
    }

    suspend fun addScooter(id: String?, type: String): Scooter = withContext(Dispatchers.IO) {
        api.addScooter(AddScooterRequest(id = id?.takeIf { it.isNotBlank() }, type = type))
    }

    suspend fun deleteScooter(id: String): Unit = withContext(Dispatchers.IO) {
        api.deleteScooter(id)
    }

    suspend fun updateScooter(id: String, fields: UpdateScooterRequest): Scooter =
        withContext(Dispatchers.IO) { api.updateScooter(id, fields) }

    suspend fun saveDeviceCondition(id: String, condition: SaveDeviceConditionRequest): DeviceCondition =
        withContext(Dispatchers.IO) {
            api.saveDeviceCondition(id, condition).deviceCondition
                ?: throw IllegalStateException("Server tidak mengembalikan kondisi perangkat.")
        }

    suspend fun completeMaintenance(recordId: String): Unit = withContext(Dispatchers.IO) {
        api.completeMaintenance(recordId)
    }

    /**
     * Toggle a scooter between available <-> in-use.
     * Returns the raw response — callers must handle requiresConfirmation.
     */
    suspend fun toggleScooter(id: String, forceMaintenance: Boolean = false): ToggleResponse =
        withContext(Dispatchers.IO) { api.toggleScooter(id, ToggleRequest(forceMaintenance)) }

    suspend fun downloadBackup(): ResponseBody = withContext(Dispatchers.IO) { api.downloadBackup() }
}

data class DashboardData(
    val scooters: List<Scooter>,
    val activityLog: List<ActivityLogEntry>,
    val maintenanceRecords: List<MaintenanceRecord>,
)

/** Convert a thrown exception into a short, user-facing Indonesian message. */
fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Tidak dapat terhubung ke server API. Periksa koneksi internet."
    is HttpException -> {
        val body = errorBodyMessage()
        if (!body.isNullOrBlank()) body else "Permintaan gagal (${code()})."
    }
    else -> message ?: "Terjadi kesalahan. Silakan coba lagi."
}

private fun HttpException.errorBodyMessage(): String? = runCatching {
    val raw = response()?.errorBody()?.string() ?: return null
    Json { ignoreUnknownKeys = true }.decodeFromString<ApiError>(raw).error
}.getOrNull()

/** Helper to read a raw (streamed) response body safely. */
suspend fun Response<ResponseBody>.readBytesOrNull(): ByteArray? = withContext(Dispatchers.IO) {
    runCatching { body()?.bytes() }.getOrNull()
}
