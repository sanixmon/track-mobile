package com.evrenhouse.trackscooter.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

object Exporter {

    private fun escapeCsv(value: Any?): String {
        val s = value?.toString() ?: "-"
        return if (s.contains(',') || s.contains('"') || s.contains('\n')) {
            "\"${s.replace("\"", "\"\"")}\""
        } else {
            s
        }
    }

    fun toCsv(header: List<String>, rows: List<List<Any?>>): String = buildString {
        append(header.joinToString(",") { escapeCsv(it) })
        append("\n")
        rows.forEach { row ->
            append(row.joinToString(",") { escapeCsv(it) })
            append("\n")
        }
    }

    /** Build complete history and maintenance CSV for a single scooter unit. */
    fun buildHistoryCsv(
        log: List<ActivityLogEntry>,
        maintenance: List<MaintenanceRecord>,
    ): String {
        val header = listOf("Tanggal", "Aksi", "Jenis")
        val historyRows = log.map { e ->
            listOf(
                DateUtils.formatFull(e.timestamp),
                if (e.action == "checkout") "Keluar (Sewa)" else "Masuk (Kembali)",
                TypeLabels.of(e.scooterType),
            )
        }
        val historyCsv = toCsv(header, historyRows.ifEmpty { listOf(listOf("-", "Belum ada aktivitas", "-")) })

        val mHeader = listOf("Mulai", "Lokasi", "Kendala", "Catatan", "Status", "Selesai")
        val mRows = maintenance.map { m ->
            listOf(
                DateUtils.formatFull(m.startedAt),
                if (m.location == "outlet") "Di Outlet" else "Keluar / Luar",
                m.issue ?: "-",
                m.note ?: "-",
                if (m.status == "done") "Selesai" else "Dalam Perbaikan",
                m.resolvedAt?.let { DateUtils.formatFull(it) } ?: "-",
            )
        }
        val mCsv = toCsv(mHeader, mRows.ifEmpty { listOf(listOf("-", "-", "-", "-", "-", "-")) })

        return "=== RIWAYAT UNIT ===\n$historyCsv\n=== RIWAYAT MAINTENANCE ===\n$mCsv"
    }

    /** Build complete fleet device condition report CSV. */
    fun buildConditionsCsv(scooters: List<Scooter>): String {
        val header = listOf(
            "ID Unit", "Jenis", "Status", "Kondisi Unit", "Spakbor", "Lampu", "Baterai",
            "Jenis Error", "Rem", "Ban", "Dicek",
        )
        val rows = scooters
            .sortedBy { it.id.filter(Char::isDigit).toIntOrNull() ?: 0 }
            .map { s ->
                val dc = s.deviceCondition
                val monitorLabel = if (dc?.monitor == "lain") {
                    dc.monitorDetail ?: "Lain-lain"
                } else {
                    DeviceLabels.monitor[dc?.monitor] ?: dc?.monitor ?: "Belum dicek"
                }
                listOf(
                    s.id,
                    TypeLabels.of(s.type),
                    StatusLabels.of(s.status),
                    DeviceConditionHelper.summarize(dc),
                    DeviceLabels.setelan[dc?.setelan] ?: dc?.setelan ?: "Belum dicek",
                    DeviceLabels.lampu[dc?.lampu] ?: dc?.lampu ?: "Belum dicek",
                    DeviceLabels.baterai[dc?.baterai] ?: dc?.baterai ?: "Belum dicek",
                    if (dc?.monitor != null) monitorLabel else "Belum dicek",
                    DeviceLabels.rem[dc?.rem] ?: dc?.rem ?: "Belum dicek",
                    DeviceLabels.ban[dc?.ban] ?: dc?.ban ?: "Belum dicek",
                    dc?.updatedAt?.let { DateUtils.formatFull(it) } ?: "-",
                )
            }
        return toCsv(header, rows)
    }

    /** Build daily usage report CSV for all units on a given date. */
    fun buildDailyReportCsv(date: LocalDate, activityLog: List<ActivityLogEntry>, scooters: List<Scooter>): String {
        val typeByUnit = scooters.associate { it.id to it.type }
        val conditionByUnit = scooters.associate { it.id to it.deviceCondition }

        val perUnit = activityLog.groupBy { it.scooterId }
        val sessions = mutableListOf<Triple<String, LocalDateTime?, LocalDateTime?>>()

        for ((scooterId, logs) in perUnit) {
            val sorted = logs
                .mapNotNull { l -> DateUtils.parse(l.timestamp)?.let { l to it } }
                .sortedBy { it.second }

            var open: Pair<String, LocalDateTime>? = null
            for ((l, dt) in sorted) {
                if (l.action == ActionLabels.CHECKOUT) {
                    if (open != null && open.second.toLocalDate() == date) {
                        sessions.add(Triple(scooterId, open.second, null))
                    }
                    open = l.action to dt
                } else if (l.action == ActionLabels.RETURN && open != null) {
                    if (open.second.toLocalDate() == date) {
                        sessions.add(Triple(scooterId, open.second, dt))
                    }
                    open = null
                }
            }
            if (open != null && open.second.toLocalDate() == date) {
                sessions.add(Triple(scooterId, open.second, null))
            }
        }

        sessions.sortBy { it.second }

        val rows = sessions.mapIndexed { i, s ->
            val totalMs = if (s.third != null) {
                Duration.between(s.second, s.third).toMillis()
            } else {
                Duration.between(s.second, LocalDateTime.now(DateUtils.WIB)).toMillis().coerceAtLeast(0)
            }
            val hours = Math.round(totalMs / 3600000.0 * 100.0) / 100.0
            listOf(
                i + 1,
                s.first,
                typeByUnit[s.first]?.let { TypeLabels.of(it) } ?: "-",
                DeviceConditionHelper.summarize(conditionByUnit[s.first]),
                s.second?.let { DateUtils.formatTime(it) } ?: "-",
                s.third?.let { DateUtils.formatTime(it) } ?: "Belum kembali",
                hours,
            )
        }

        val header = listOf("No", "Unit", "Jenis", "Kondisi Unit", "Out (Keluar)", "In (Masuk)", "Jumlah Jam")
        return toCsv(header, rows.ifEmpty { listOf(listOf(1, "Tidak ada aktivitas", "-", "-", "-", "-", 0)) })
    }

    /** Write a text file into public Downloads; returns the display filename. */
    suspend fun saveToDownloads(context: Context, filename: String, content: String): String =
        saveBytesToDownloads(context, filename, content.toByteArray(), "text/csv")

    /** Write raw bytes (e.g. PNG, ZIP, .db) into public Downloads. */
    suspend fun saveBytesToDownloads(context: Context, filename: String, bytes: ByteArray, mime: String = "application/octet-stream"): String =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("Gagal membuat file di Downloads.")
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IllegalStateException("Gagal menulis file.")
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { it.write(bytes) }
            }
            filename
        }
}
