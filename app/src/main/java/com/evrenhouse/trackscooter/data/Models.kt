package com.evrenhouse.trackscooter.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Domain constants (mirror of web src/constants.js) ──────
object ScooterStatus {
    const val AVAILABLE = "available"
    const val IN_USE = "in-use"
    const val RUSAK = "rusak"
    const val MAINTENANCE = "maintenance"
}

object ScooterType {
    const val SD = "sd"
    const val SJ = "sj"
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
    @SerialName("scooter_type") val scooterType: String,
    @SerialName("location") val location: String,
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
)

@Serializable
data class UpdateScooterRequest(
    @SerialName("status") val status: String? = null,
    @SerialName("maintenanceNote") val maintenanceNote: String? = null,
    @SerialName("location") val location: String? = null,
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
data class ApiError(
    @SerialName("error") val error: String? = null,
)
