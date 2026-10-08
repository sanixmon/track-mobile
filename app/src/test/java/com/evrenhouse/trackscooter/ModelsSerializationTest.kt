package com.evrenhouse.trackscooter

import com.evrenhouse.trackscooter.data.ActiveMaintenance
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.AppVersionResponse
import com.evrenhouse.trackscooter.data.AttendanceRecord
import com.evrenhouse.trackscooter.data.AttendanceResponse
import com.evrenhouse.trackscooter.data.DeviceCondition
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.SwapScooterResponse
import com.evrenhouse.trackscooter.data.ToggleResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelsSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testScooterDeserialization_withCompleteFields() {
        val payload = """
            {
                "id": "SD-1",
                "type": "sd",
                "status": "available",
                "ownership": "outlet",
                "current_outlet": "utara",
                "maintenance_note": null,
                "last_updated": "2026-10-08T09:00:00.000Z",
                "device_condition": {
                    "setelan": "ada",
                    "lampu": "nyala",
                    "baterai": "normal",
                    "monitor": "normal",
                    "rem": "normal",
                    "ban": "aman",
                    "monitor_detail": null,
                    "updated_at": "2026-10-08T08:30:00.000Z"
                },
                "active_maintenance": null
            }
        """.trimIndent()

        val scooter = json.decodeFromString<Scooter>(payload)
        assertEquals("SD-1", scooter.id)
        assertEquals("sd", scooter.type)
        assertEquals("available", scooter.status)
        assertEquals("utara", scooter.currentOutlet)
        assertNotNull(scooter.deviceCondition)
        assertEquals("aman", scooter.deviceCondition?.ban)
        assertEquals("normal", scooter.deviceCondition?.baterai)
        assertFalse(scooter.isErrorMonitored)
        assertNull(scooter.activeMaintenance)
    }

    @Test
    fun testScooterDeserialization_withActiveMaintenance() {
        val payload = """
            {
                "id": "SJ-2",
                "type": "sj",
                "status": "maintenance",
                "ownership": "outlet",
                "current_outlet": "utara",
                "maintenance_note": "Ganti ban",
                "last_updated": "2026-10-08T09:10:00.000Z",
                "active_maintenance": {
                    "id": "mnt-123",
                    "location": "outlet",
                    "location_detail": null,
                    "issue": "Ban bocor",
                    "note": "Menunggu teknisi",
                    "status": "repair",
                    "started_at": "2026-10-08T09:05:00.000Z"
                }
            }
        """.trimIndent()

        val scooter = json.decodeFromString<Scooter>(payload)
        assertEquals("SJ-2", scooter.id)
        assertEquals(ScooterStatus.MAINTENANCE, scooter.status)
        assertNotNull(scooter.activeMaintenance)
        assertEquals("mnt-123", scooter.activeMaintenance?.id)
        assertEquals("outlet", scooter.activeMaintenance?.location)
        assertEquals("Ban bocor", scooter.activeMaintenance?.issue)
        assertEquals("repair", scooter.activeMaintenance?.status)
    }

    @Test
    fun testActivityLogDeserialization() {
        val payload = """
            {
                "id": "log-456",
                "scooter_id": "SD-1",
                "scooter_type": "sd",
                "action": "checkout",
                "timestamp": "2026-10-08T09:30:00.000Z"
            }
        """.trimIndent()

        val entry = json.decodeFromString<ActivityLogEntry>(payload)
        assertEquals("log-456", entry.id)
        assertEquals("SD-1", entry.scooterId)
        assertEquals("sd", entry.scooterType)
        assertEquals("checkout", entry.action)
    }

    @Test
    fun testAttendanceRecordDeserialization() {
        val payload = """
            {
                "id": "att-789",
                "date": "2026-10-08",
                "scooter_id": "SB-10",
                "scooter_type": "sb",
                "scanned_at": "2026-10-08T08:00:00.000Z",
                "note": "Hadir fisik di outlet barat",
                "outlet": "barat",
                "scooter_status": "available"
            }
        """.trimIndent()

        val record = json.decodeFromString<AttendanceRecord>(payload)
        assertEquals("att-789", record.id)
        assertEquals("2026-10-08", record.date)
        assertEquals("SB-10", record.scooterId)
        assertEquals("barat", record.outlet)
    }

    @Test
    fun testToggleResponseDeserialization() {
        val payload = """
            {
                "success": true,
                "action": "checkout",
                "scooter": {
                    "id": "SM-5",
                    "type": "sm",
                    "status": "in-use",
                    "current_outlet": "utara-motor",
                    "last_updated": "2026-10-08T10:00:00.000Z"
                }
            }
        """.trimIndent()

        val response = json.decodeFromString<ToggleResponse>(payload)
        assertTrue(response.success)
        assertEquals("checkout", response.action)
        assertEquals("SM-5", response.scooter?.id)
        assertEquals(ScooterStatus.IN_USE, response.scooter?.status)
    }

    @Test
    fun testAppVersionResponseDeserialization_snakeAndCamel() {
        val camelPayload = """
            {
                "versionCode": 29,
                "versionName": "2.8.0",
                "downloadUrl": "https://example.com/app.apk",
                "minVersionCode": 19,
                "forceUpdate": false,
                "title": "Pembaruan",
                "changelog": "Perbaikan bug"
            }
        """.trimIndent()

        val camelRes = json.decodeFromString<AppVersionResponse>(camelPayload)
        assertEquals(29, camelRes.resolvedVersionCode)
        assertEquals("2.8.0", camelRes.resolvedVersionName)
        assertEquals("https://example.com/app.apk", camelRes.resolvedDownloadUrl)
        assertFalse(camelRes.resolvedForceUpdate)

        val snakePayload = """
            {
                "version_code": 30,
                "version_name": "2.8.1",
                "download_url": "https://example.com/latest.apk",
                "min_version_code": 25,
                "force_update": true
            }
        """.trimIndent()

        val snakeRes = json.decodeFromString<AppVersionResponse>(snakePayload)
        assertEquals(30, snakeRes.resolvedVersionCode)
        assertEquals("2.8.1", snakeRes.resolvedVersionName)
        assertTrue(snakeRes.resolvedForceUpdate)
    }
}
