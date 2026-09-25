package com.evrenhouse.trackscooter.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Domain constants (mirror of web src/constants.js) ──────
object ScooterStatus {
    const val AVAILABLE = "available"
    const val IN_USE = "in-use"
    const val MAINTENANCE = "maintenance"
}

object ScooterType {
    const val SD = "sd"
    const val SJ = "sj"
    const val SB = "sb"
    const val SJB = "sjb"
    const val SM = "sm"
    const val SJM = "sjm"
}

object DeviceFields {
    const val SETELAN = "setelan"
    const val LAMPU = "lampu"
    const val BATERAI = "baterai"
    const val MONITOR = "monitor"
    const val REM = "rem"
    const val BAN = "ban"
    const val MONITOR_DETAIL = "monitorDetail"
}

// ── API DTOs ───────────────────────────────────────────────
@Serializable
data class DeviceCondition(
    @SerialName("setelan") val setelan: String? = null,
    @SerialName("lampu") val lampu: String? = null,
    @SerialName("baterai") val baterai: String? = null,
    @SerialName("monitor") val monitor: String? = null,
    @SerialName("rem") val rem: String? = null,
    @SerialName("ban") val ban: String? = null,
    @SerialName("monitor_detail") val monitorDetail: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

@Serializable
data class ActiveMaintenance(
    @SerialName("id") val id: String,
    @SerialName("location") val location: String,
    @SerialName("issue") val issue: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("status") val status: String,
    @SerialName("started_at") val startedAt: String,
)

@Serializable
data class Scooter(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("status") val status: String,
    @SerialName("ownership") val ownership: String? = null,
    @SerialName("current_outlet") val currentOutlet: String? = null,
    @SerialName("maintenance_note") val maintenanceNote: String? = null,
    @SerialName("last_updated") val lastUpdated: String? = null,
    @SerialName("device_condition") val deviceCondition: DeviceCondition? = null,
    @SerialName("active_maintenance") val activeMaintenance: ActiveMaintenance? = null,
) {
    val isErrorMonitored: Boolean
        get() = deviceCondition?.monitor != null && deviceCondition.monitor != "normal"
}

@Serializable
data class ActivityLogEntry(
    @SerialName("id") val id: String,
    @SerialName("scooter_id") val scooterId: String,
    @SerialName("scooter_type") val scooterType: String,
    @SerialName("action") val action: String, // "checkout" | "return"
    @SerialName("timestamp") val timestamp: String,
)

@Serializable
data class MaintenanceRecord(
    @SerialName("id") val id: String,
    @SerialName("scooter_id") val scooterId: String,
    @SerialName("scooter_type") val scooterType: String? = null,
    @SerialName("location") val location: String,
    @SerialName("location_detail") val locationDetail: String? = null,
    @SerialName("issue") val issue: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("status") val status: String, // "repair" | "done"
    @SerialName("started_at") val startedAt: String,
    @SerialName("resolved_at") val resolvedAt: String? = null,
)

// ── Request / response bodies ──────────────────────────────
@Serializable
data class AddScooterRequest(
    @SerialName("id") val id: String? = null,
    @SerialName("type") val type: String,
    @SerialName("currentOutlet") val currentOutlet: String? = null,
)

@Serializable
data class UpdateScooterRequest(
    @SerialName("status") val status: String? = null,
    @SerialName("currentOutlet") val currentOutlet: String? = null,
    @SerialName("maintenanceNote") val maintenanceNote: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("locationDetail") val locationDetail: String? = null,
    @SerialName("issue") val issue: String? = null,
    @SerialName("note") val note: String? = null,
)

@Serializable
data class SaveDeviceConditionRequest(
    @SerialName("setelan") val setelan: String?,
    @SerialName("lampu") val lampu: String?,
    @SerialName("baterai") val baterai: String?,
    @SerialName("monitor") val monitor: String?,
    @SerialName("rem") val rem: String?,
    @SerialName("ban") val ban: String?,
    @SerialName("monitorDetail") val monitorDetail: String? = null,
)

@Serializable
data class ToggleRequest(
    @SerialName("forceMaintenance") val forceMaintenance: Boolean = false,
)

@Serializable
data class SwapScooterRequest(
    @SerialName("replacementId") val replacementId: String,
    @SerialName("note") val note: String,
    @SerialName("issue") val issue: String? = null,
    @SerialName("markBroken") val markBroken: Boolean = false,
)

@Serializable
data class SwapScooterResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("message") val message: String? = null,
    @SerialName("oldScooter") val oldScooter: Scooter? = null,
    @SerialName("replacementScooter") val replacementScooter: Scooter? = null,
)

@Serializable
data class ToggleResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("requiresConfirmation") val requiresConfirmation: Boolean = false,
    @SerialName("message") val message: String? = null,
    @SerialName("action") val action: String? = null,
    @SerialName("scooter") val scooter: Scooter? = null,
)

@Serializable
data class SaveDeviceConditionResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("scooter") val scooter: Scooter? = null,
    @SerialName("device_condition") val deviceCondition: DeviceCondition? = null,
)

@Serializable
data class CompleteMaintenanceResponse(
    @SerialName("success") val success: Boolean = false,
    @SerialName("record") val record: MaintenanceRecord? = null,
)


@Serializable
data class TechnicalActivity(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String, // "usage" | "maintenance"
    @SerialName("startTime") val startTime: String,
    @SerialName("endTime") val endTime: String? = null,
    @SerialName("durationMinutes") val durationMinutes: Int? = null,
    @SerialName("durationText") val durationText: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("issue") val issue: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("locationDetail") val locationDetail: String? = null,
    @SerialName("detail") val detail: String? = null,
)

@Serializable
data class ScooterTechnicalHistoryResponse(
    @SerialName("scooter") val scooter: Scooter,
    @SerialName("condition") val condition: DeviceCondition? = null,
    @SerialName("activities") val activities: List<TechnicalActivity> = emptyList(),
)
@Serializable
data class ApiError(
    @SerialName("error") val error: String? = null,
)

data class DashboardData(
    val scooters: List<Scooter>,
    val activityLog: List<ActivityLogEntry>,
    val maintenanceRecords: List<MaintenanceRecord>,
)

@Serializable
data class AttendanceRecord(
    @SerialName("id") val id: String,
    @SerialName("date") val date: String,
    @SerialName("scooter_id") val scooterId: String,
    @SerialName("scooter_type") val scooterType: String? = null,
    @SerialName("scanned_at") val scannedAt: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("outlet") val outlet: String? = null,
    @SerialName("scooter_status") val scooterStatus: String? = null,
)

@Serializable
data class AttendanceRequest(
    @SerialName("scooterId") val scooterId: String,
    @SerialName("date") val date: String? = null,
    @SerialName("note") val note: String? = null,
    @SerialName("outlet") val outlet: String? = null,
)

@Serializable
data class AttendanceResponse(
    @SerialName("success") val success: Boolean,
    @SerialName("alreadyRecorded") val alreadyRecorded: Boolean = false,
    @SerialName("record") val record: AttendanceRecord? = null,
    @SerialName("scooter") val scooter: Scooter? = null,
    @SerialName("message") val message: String? = null,
)

@Serializable
data class MarkAllAttendanceRequest(
    @SerialName("date") val date: String? = null,
    @SerialName("types") val types: List<String>? = null,
    @SerialName("outlet") val outlet: String? = null,
)

@Serializable
data class ResetAttendanceRequest(
    @SerialName("date") val date: String? = null,
    @SerialName("types") val types: List<String>? = null,
    @SerialName("outlet") val outlet: String? = null,
)

@Serializable
data class SimpleSuccessResponse(
    @SerialName("success") val success: Boolean = true,
    @SerialName("message") val message: String? = null,
)

@Serializable
data class AppVersionResponse(
    @SerialName("versionCode") val versionCode: Int? = null,
    @SerialName("version_code") val versionCodeSnake: Int? = null,
    @SerialName("versionName") val versionName: String? = null,
    @SerialName("version_name") val versionNameSnake: String? = null,
    @SerialName("downloadUrl") val downloadUrl: String? = null,
    @SerialName("download_url") val downloadUrlSnake: String? = null,
    @SerialName("minVersionCode") val minVersionCode: Int? = null,
    @SerialName("min_version_code") val minVersionCodeSnake: Int? = null,
    @SerialName("forceUpdate") val forceUpdate: Boolean? = null,
    @SerialName("force_update") val forceUpdateSnake: Boolean? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("changelog") val changelog: String? = null,
) {
    val resolvedVersionCode: Int get() = versionCode ?: versionCodeSnake ?: 0
    val resolvedVersionName: String get() = versionName ?: versionNameSnake ?: ""
    val resolvedDownloadUrl: String get() = downloadUrl ?: downloadUrlSnake ?: ""
    val resolvedForceUpdate: Boolean get() = forceUpdate ?: forceUpdateSnake ?: false
}

data class AppUpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersionName: String = "",
    val latestVersionCode: Int = 0,
    val currentVersionName: String = "",
    val currentVersionCode: Int = 0,
    val downloadUrl: String = "",
    val forceUpdate: Boolean = false,
    val title: String? = null,
    val changelog: String? = null,
    val errorMessage: String? = null,
)

