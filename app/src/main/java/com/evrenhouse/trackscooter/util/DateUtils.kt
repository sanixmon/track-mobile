package com.evrenhouse.trackscooter.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateUtils {

    val WIB: ZoneId = ZoneId.of("Asia/Jakarta")

    private val dayMonthYearTime = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    private val dayMonthYearTimeSec = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss", Locale("id", "ID"))
    private val timeHm = DateTimeFormatter.ofPattern("HH:mm", Locale("id", "ID"))
    private val timeHms = DateTimeFormatter.ofPattern("HH:mm:ss", Locale("id", "ID"))
    private val dateKeyFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val dayMonthYear = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("id", "ID"))
    private val dayMonthTime = DateTimeFormatter.ofPattern("dd MMM, HH:mm", Locale("id", "ID"))
    private val weekdayFull = DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", Locale("id", "ID"))
    private val shortPill = DateTimeFormatter.ofPattern("EEE dd/MM", Locale("id", "ID"))

    /** Parse an ISO-8601 timestamp to a LocalDateTime in WIB. Returns null on failure. */
    fun parse(ts: String?): java.time.LocalDateTime? {
        if (ts.isNullOrBlank()) return null
        return runCatching {
            Instant.parse(ts).atZone(WIB).toLocalDateTime()
        }.getOrElse {
            // fallback for non-ISO strings
            runCatching {
                java.time.LocalDateTime.parse(ts)
            }.getOrNull()
        }
    }

    fun formatFull(ts: String?): String {
        val dt = parse(ts) ?: return "-"
        return dayMonthYearTime.format(dt)
    }

    /** Web parity: "dd MMM, HH:mm" untuk baris timestamp maintenance (ManageComponents web). */
    fun formatDayMonthTime(ts: String?): String {
        val dt = parse(ts) ?: return "Dalam perbaikan"
        return dayMonthTime.format(dt)
    }

    fun formatFullSec(ts: String?): String {
        val dt = parse(ts) ?: return "-"
        return dayMonthYearTimeSec.format(dt)
    }

    fun formatTime(ts: String?): String {
        val dt = parse(ts) ?: return "-"
        return timeHm.format(dt)
    }

    fun formatTimeSec(ts: String?): String {
        val dt = parse(ts) ?: return "-"
        return timeHms.format(dt)
    }

    fun formatDateFull(ts: String?): String {
        val dt = parse(ts) ?: return "-"
        return dayMonthYear.format(dt)
    }

    fun formatWeekdayFull(ts: String?): String {
        val dt = parse(ts) ?: return "-"
        return weekdayFull.format(dt)
    }

    fun formatWeekdayFull(date: java.time.LocalDate): String = weekdayFull.format(date)

    fun formatTime(dt: java.time.LocalDateTime): String = timeHm.format(dt)

    fun formatPill(date: java.time.LocalDate): String = shortPill.format(date)

    fun dateKey(ts: String?): String? {
        val dt = parse(ts) ?: return null
        return dateKeyFmt.format(dt)
    }

    fun localDateKey(date: java.time.LocalDate): String = dateKeyFmt.format(date)

    fun today(): java.time.LocalDate = LocalDate.now(WIB)

    fun toLocalDate(ts: String?): java.time.LocalDate? = parse(ts)?.toLocalDate()


    fun formatDuration(totalSecs: Long): String {
        val safeSecs = maxOf(0L, totalSecs)
        val hrs = safeSecs / 3600
        val mins = (safeSecs % 3600) / 60
        return if (hrs > 0) "${hrs}j ${mins}m" else "${mins} mnt"
    }

    fun toEpochMilli(dt: java.time.LocalDateTime): Long =
        dt.atZone(WIB).toInstant().toEpochMilli()
    /** Relative "x menit lalu" style, Indonesian. */
    fun timeAgo(ts: String?): String {
        val dt = parse(ts) ?: return ""
        val now = java.time.LocalDateTime.now(WIB)
        val diff = java.time.Duration.between(dt, now)
        val minutes = diff.toMinutes()
        return when {
            minutes < 1 -> "baru saja"
            minutes < 60 -> "$minutes menit lalu"
            minutes < 1440 -> "${minutes / 60} jam lalu"
            else -> "${minutes / 1440} hari lalu"
        }
    }

    /** Legacy formatter for legacy code paths (kept for safety). */
    fun legacy(ts: String?): String {
        if (ts.isNullOrBlank()) return "-"
        return runCatching {
            val fmt = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            fmt.format(Date.from(Instant.parse(ts)))
        }.getOrDefault("-")
    }
}
