package com.evrenhouse.trackscooter.data
import com.evrenhouse.trackscooter.BuildConfig
import com.evrenhouse.trackscooter.util.UpdatePolicy
import com.evrenhouse.trackscooter.util.VersionUtils

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import retrofit2.HttpException
import retrofit2.Response
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException

sealed interface LocalDataUpdate {
    data class Toggled(val response: ToggleResponse) : LocalDataUpdate
    data object DataMutated : LocalDataUpdate
}

/**
 * Single gateway to the TrackScooter backend. Translates low-level HTTP
 * failures into friendly Indonesian messages, mirroring the web app's
 * storage.js behaviour.
 */
class ScooterRepository(
    private val api: ApiService = ApiClient.service,
    private val githubApi: GitHubApiService = ApiClient.githubService,
) {

    private val _localUpdates = MutableSharedFlow<LocalDataUpdate>(extraBufferCapacity = 16)
    val localUpdates: SharedFlow<LocalDataUpdate> = _localUpdates.asSharedFlow()

    fun notifyScooterToggled(response: ToggleResponse) {
        _localUpdates.tryEmit(LocalDataUpdate.Toggled(response))
    }

    fun notifyDataMutated() {
        _localUpdates.tryEmit(LocalDataUpdate.DataMutated)
    }

    /** Real-time SSE stream of backend database change events */
    fun observeEvents(): Flow<String> = callbackFlow {
        val request = Request.Builder()
            .url("${ApiClient.baseUrl}/api/events")
            .header("Accept", "text/event-stream")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                Log.d("ScooterRepo", "Connected to TrackScooter SSE stream")
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                trySend(type ?: "message")
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                Log.w("ScooterRepo", "SSE stream disconnect/failure: ${t?.message}")
                close(t ?: IOException("SSE disconnected"))
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d("ScooterRepo", "SSE stream closed")
                close()
            }
        }

        val factory = EventSources.createFactory(ApiClient.sseClient)
        val eventSource = factory.newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }

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

    suspend fun addScooter(
        id: String?,
        type: String,
        currentOutlet: String? = null,
    ): Scooter = withContext(Dispatchers.IO) {
        api.addScooter(
            AddScooterRequest(
                id = id?.takeIf { it.isNotBlank() },
                type = type,
                currentOutlet = currentOutlet,
            )
        )
    }

    suspend fun getDailyAttendance(date: String? = null): List<AttendanceRecord> = withContext(Dispatchers.IO) {
        api.getDailyAttendance(date)
    }

    suspend fun recordDailyAttendance(
        scooterId: String,
        date: String? = null,
        note: String? = null,
        outlet: String? = null
    ): AttendanceResponse = withContext(Dispatchers.IO) {
        api.recordDailyAttendance(AttendanceRequest(scooterId, date, note, outlet))
    }

    suspend fun markAllDailyAttendance(
        date: String? = null,
        types: List<String>? = null,
        outlet: String? = null
    ): SimpleSuccessResponse = withContext(Dispatchers.IO) {
        api.markAllDailyAttendance(MarkAllAttendanceRequest(date, types, outlet))
    }

    suspend fun resetDailyAttendance(
        date: String? = null,
        types: List<String>? = null,
        outlet: String? = null
    ): SimpleSuccessResponse = withContext(Dispatchers.IO) {
        api.resetDailyAttendance(ResetAttendanceRequest(date, types, outlet))
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

    suspend fun toggleScooter(id: String, forceMaintenance: Boolean = false): ToggleResponse =
        withContext(Dispatchers.IO) { api.toggleScooter(id, ToggleRequest(forceMaintenance)) }


    suspend fun checkoutScooter(id: String): ToggleResponse =
        withContext(Dispatchers.IO) { api.checkoutScooter(id) }

    suspend fun returnScooter(id: String): ToggleResponse =
        withContext(Dispatchers.IO) { api.returnScooter(id) }

    suspend fun getScooterTechnicalHistory(
        id: String,
        startDate: String? = null,
        endDate: String? = null
    ): ScooterTechnicalHistoryResponse =
        withContext(Dispatchers.IO) { api.getScooterTechnicalHistory(id, startDate, endDate) }
    suspend fun swapScooter(
        id: String,
        replacementId: String,
        note: String,
        issue: String? = null,
        markBroken: Boolean = false
    ): SwapScooterResponse = withContext(Dispatchers.IO) {
        api.swapScooter(id, SwapScooterRequest(replacementId, note, issue, markBroken))
    }
    suspend fun downloadBackup(): ResponseBody = withContext(Dispatchers.IO) { api.downloadBackup() }

    suspend fun checkAppUpdate(): AppUpdateInfo = withContext(Dispatchers.IO) {
        val currentCode = BuildConfig.VERSION_CODE
        val currentName = BuildConfig.VERSION_NAME
        // 1. Server utama (lengkap: minVersion, force, changelog).
        runCatching {
            val res = api.getAppVersion()
            val targetCode = res.resolvedVersionCode
            val targetName = res.resolvedVersionName
            val isCodeHigher = targetCode > currentCode
            val isNameHigher = targetCode == 0 && VersionUtils.isVersionHigher(targetName, currentName)
            val isUpdateAvailable = isCodeHigher || isNameHigher
            // minVersionCode selama ini diabaikan: user jauh tertinggal tidak pernah dipaksa update.
            val forceUpdate = UpdatePolicy.isForceUpdate(
                currentCode = currentCode,
                minVersionCode = res.resolvedMinVersionCode,
                forceFlag = res.resolvedForceUpdate,
            )
            val downloadUrl = res.resolvedDownloadUrl.ifBlank { FALLBACK_DOWNLOAD_URL }
            AppUpdateInfo(
                isUpdateAvailable = isUpdateAvailable,
                latestVersionName = targetName.ifBlank { "v$targetCode" },
                latestVersionCode = targetCode,
                currentVersionName = currentName,
                currentVersionCode = currentCode,
                downloadUrl = downloadUrl,
                forceUpdate = forceUpdate,
                title = res.title,
                changelog = res.changelog,
            )
        }.onSuccess { return@withContext it }
            .onFailure { Log.w("ScooterRepository", "Primary update check failed, trying GitHub", it) }
        // 2. Fallback repo rilis khusus (tanpa minVersion/force/changelog).
        runCatching {
            val rel = githubApi.getLatestRelease()
            val remoteName = VersionUtils.parseReleaseTag(rel.tagName)
            val apkUrl = rel.assets.firstOrNull { it.name == "track-scooter.apk" }?.downloadUrl
            AppUpdateInfo(
                isUpdateAvailable = VersionUtils.isVersionHigher(remoteName, currentName),
                latestVersionName = remoteName.ifBlank { rel.tagName },
                latestVersionCode = 0,
                currentVersionName = currentName,
                currentVersionCode = currentCode,
                downloadUrl = apkUrl.ifBlank { FALLBACK_DOWNLOAD_URL },
                forceUpdate = false,
            )
        }.onSuccess { return@withContext it }
            .onFailure { Log.w("ScooterRepository", "GitHub update check failed", it) }
        // 3. Dua-duanya gagal.
        AppUpdateInfo(
            isUpdateAvailable = false,
            currentVersionName = currentName,
            currentVersionCode = currentCode,
            errorMessage = "Gagal memeriksa pembaruan dari server dan GitHub.",
        )
    }

    companion object {
        const val FALLBACK_DOWNLOAD_URL =
            "https://github.com/sanixmon/track-releases/releases/latest/download/track-scooter.apk"
    }
    }
}

