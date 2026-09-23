package com.evrenhouse.trackscooter.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.TechnicalActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime


const val MIME_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
const val MIME_CSV = "text/csv"

data class ExcelSheet(
    val name: String,
    val headers: List<String>,
    val rows: List<List<Any?>>,
    val titleRows: List<String> = emptyList(),
)
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

    fun buildXlsx(sheets: List<ExcelSheet>): ByteArray {
        val bos = ByteArrayOutputStream()
        val zip = ZipOutputStream(bos)

        fun writeEntry(name: String, content: String) {
            val entry = ZipEntry(name)
            zip.putNextEntry(entry)
            zip.write(content.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        // [Content_Types].xml
        val overrides = sheets.indices.joinToString("\n") { i ->
            """  <Override PartName="/xl/worksheets/sheet${i + 1}.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>"""
        }
        writeEntry(
            "[Content_Types].xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
$overrides
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""
        )

        // _rels/.rels
        writeEntry(
            "_rels/.rels",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
        )

        // xl/_rels/workbook.xml.rels
        val wbRelsList = mutableListOf<String>()
        for (i in sheets.indices) {
            wbRelsList.add("""  <Relationship Id="rId${i + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet${i + 1}.xml"/>""")
        }
        val stylesRid = "rId${sheets.size + 1}"
        wbRelsList.add("""  <Relationship Id="$stylesRid" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
        writeEntry(
            "xl/_rels/workbook.xml.rels",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
${wbRelsList.joinToString("\n")}
</Relationships>"""
        )

        // xl/workbook.xml
        val sheetEntries = sheets.mapIndexed { idx, s ->
            val safeName = s.name.replace("[\\\\/?*\\[\\]:]".toRegex(), "").take(31).ifBlank { "Sheet${idx + 1}" }
            """    <sheet name="$safeName" sheetId="${idx + 1}" r:id="rId${idx + 1}"/>"""
        }.joinToString("\n")
        writeEntry(
            "xl/workbook.xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
$sheetEntries
  </sheets>
</workbook>"""
        )

        // xl/styles.xml
        writeEntry(
            "xl/styles.xml",
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="2">
    <font><sz val="11"/><color theme="1"/><name val="Calibri"/><family val="2"/></font>
    <font><b/><sz val="11"/><color theme="1"/><name val="Calibri"/><family val="2"/></font>
  </fonts>
  <fills count="2">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
  </fills>
  <borders count="1">
    <border><left/><right/><top/><bottom/><diagonal/></border>
  </borders>
  <cellStyleXfs count="1">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0"/>
  </cellStyleXfs>
  <cellXfs count="2">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/>
  </cellXfs>
</styleSheet>"""
        )

        fun escapeXml(v: String): String = v
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")

        fun colLetter(index: Int): String {
            var col = index
            var result = ""
            while (col >= 0) {
                result = ((col % 26 + 'A'.code).toChar()) + result
                col = (col / 26) - 1
            }
            return result
        }

        for ((idx, sheet) in sheets.withIndex()) {
            val sb = StringBuilder()
            sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            sb.append("""<sheetData>""")

            var currentRow = 1
            for (title in sheet.titleRows) {
                sb.append("""<row r="$currentRow">""")
                sb.append("""<c r="A$currentRow" s="1" t="inlineStr"><is><t>${escapeXml(title)}</t></is></c>""")
                sb.append("""</row>""")
                currentRow++
            }
            if (sheet.titleRows.isNotEmpty()) {
                currentRow++
            }

            // Header row
            sb.append("""<row r="$currentRow">""")
            for ((cIdx, h) in sheet.headers.withIndex()) {
                val ref = "${colLetter(cIdx)}$currentRow"
                sb.append("""<c r="$ref" s="1" t="inlineStr"><is><t>${escapeXml(h)}</t></is></c>""")
            }
            sb.append("""</row>""")
            currentRow++

            // Data rows
            for (row in sheet.rows) {
                sb.append("""<row r="$currentRow">""")
                for ((cIdx, cell) in row.withIndex()) {
                    val ref = "${colLetter(cIdx)}$currentRow"
                    val cellStr = cell?.toString() ?: "-"
                    sb.append("""<c r="$ref" t="inlineStr"><is><t>${escapeXml(cellStr)}</t></is></c>""")
                }
                sb.append("""</row>""")
                currentRow++
            }
            sb.append("""</sheetData>""")
            sb.append("""</worksheet>""")

            writeEntry("xl/worksheets/sheet${idx + 1}.xml", sb.toString())
        }

        zip.finish()
        zip.flush()
        return bos.toByteArray()
    }

    private fun String.numericId(): Int = this.filter(Char::isDigit).toIntOrNull() ?: 0

    /** Build complete fleet device condition report XLSX. */
    fun buildConditionsXlsx(scooters: List<Scooter>): ByteArray {
        val headers = listOf(
            "ID Unit",
            "Jenis",
            "Lokasi Outlet",
            "Status",
            "Kondisi Unit",
            "Spakbor",
            "Lampu",
            "Baterai",
            "Rem",
            "Ban",
            "Monitor",
            "Detail Monitor",
            "Catatan Maintenance",
            "Pemeriksaan Terakhir"
        )
        val sorted = scooters.sortedWith(compareBy<Scooter> { it.id.numericId() }.thenBy { it.id })
        val rows = sorted.map { s ->
            val c = s.deviceCondition
            val currentOutlet = s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)
            listOf(
                s.id,
                TypeLabels.of(s.type),
                Outlets.labelOf(currentOutlet),
                StatusLabels.of(s.status),
                DeviceConditionHelper.summarize(c),
                DeviceLabels.setelan[c?.setelan] ?: c?.setelan ?: "-",
                DeviceLabels.lampu[c?.lampu] ?: c?.lampu ?: "-",
                DeviceLabels.baterai[c?.baterai] ?: c?.baterai ?: "-",
                DeviceLabels.rem[c?.rem] ?: c?.rem ?: "-",
                DeviceLabels.ban[c?.ban] ?: c?.ban ?: "-",
                DeviceLabels.monitor[c?.monitor] ?: c?.monitor ?: "-",
                c?.monitorDetail ?: "-",
                s.maintenanceNote ?: "-",
                c?.updatedAt?.let { DateUtils.formatFull(it) } ?: "-"
            )
        }
        return buildXlsx(listOf(ExcelSheet("Kondisi Unit", headers, rows)))
    }

    /** Build complete history and maintenance XLSX workbook (2 sheets) for a single scooter unit. */
    fun buildHistoryXlsx(
        scooter: Scooter,
        log: List<ActivityLogEntry>,
        maintenance: List<MaintenanceRecord>,
    ): ByteArray {
        val historyHeaders = listOf("Tanggal", "Aksi", "ID Unit", "Jenis")
        val historyRows = log.map { e ->
            listOf(
                DateUtils.formatFull(e.timestamp),
                if (e.action == "checkout") "Keluar (Sewa)" else "Masuk (Kembali)",
                e.scooterId,
                TypeLabels.of(e.scooterType),
            )
        }

        val mHeaders = listOf("Mulai", "Lokasi", "Lokasi Detail", "Kendala", "Catatan", "Status", "Selesai")
        val mRows = maintenance.map { m ->
            listOf(
                DateUtils.formatFull(m.startedAt),
                if (m.location == "outlet") "Di Outlet" else "Keluar / Luar",
                m.locationDetail ?: "-",
                m.issue ?: "-",
                m.note ?: "-",
                if (m.status == "done") "Selesai" else "Dalam Perbaikan",
                m.resolvedAt?.let { DateUtils.formatFull(it) } ?: "-",
            )
        }

        return buildXlsx(
            listOf(
                ExcelSheet("Riwayat Unit", historyHeaders, historyRows.ifEmpty { listOf(listOf("-", "Belum ada aktivitas", scooter.id, "-")) }),
                ExcelSheet("Riwayat Maintenance", mHeaders, mRows.ifEmpty { listOf(listOf("-", "-", "-", "-", "-", "-", "-")) }),
            )
        )
    }

    /** Build complete technical history XLSX for a single scooter unit. */
    fun buildTechnicalHistoryXlsx(
        scooter: Scooter,
        activities: List<TechnicalActivity>,
        dateRangeLabel: String = "Semua-Waktu",
    ): ByteArray {
        val dc = scooter.deviceCondition
        val monitorLabel = if (dc?.monitor == "lain") {
            dc.monitorDetail ?: "Lain-lain"
        } else {
            DeviceLabels.monitor[dc?.monitor] ?: dc?.monitor ?: "Belum dicek"
        }

        val titleRows = listOf(
            "LAPORAN AKTIVITAS & RIWAYAT TEKNIS UNIT",
            "ID Unit: ${scooter.id} | Jenis: ${TypeLabels.of(scooter.type)} | Status Terkini: ${StatusLabels.of(scooter.status)} | Periode: $dateRangeLabel",
            "Kondisi Fisik: Spakbor: ${DeviceLabels.setelan[dc?.setelan] ?: dc?.setelan ?: "Belum dicek"} | Lampu: ${DeviceLabels.lampu[dc?.lampu] ?: dc?.lampu ?: "Belum dicek"} | Baterai: ${DeviceLabels.baterai[dc?.baterai] ?: dc?.baterai ?: "Belum dicek"} | Monitor: $monitorLabel | Rem: ${DeviceLabels.rem[dc?.rem] ?: dc?.rem ?: "Belum dicek"} | Ban: ${DeviceLabels.ban[dc?.ban] ?: dc?.ban ?: "Belum dicek"}"
        )

        val headers = listOf("No", "Waktu Mulai", "Waktu Selesai", "Durasi", "Tipe Aktivitas", "Keterangan / Detail")
        val dataRows = if (activities.isNotEmpty()) {
            activities.mapIndexed { idx, a ->
                listOf(
                    idx + 1,
                    DateUtils.formatFull(a.startTime),
                    a.endTime?.let { DateUtils.formatFull(it) } ?: "-",
                    a.durationText ?: "-",
                    if (a.type == "usage") "Pemakaian" else "Perbaikan",
                    if (a.type == "usage") (a.note ?: "-") else (a.detail ?: a.issue ?: "-")
                )
            }
        } else {
            listOf(listOf(1, "-", "-", "-", "-", "Belum ada riwayat aktivitas pada periode ini"))
        }

        return buildXlsx(listOf(ExcelSheet("Riwayat Aktivitas", headers, dataRows, titleRows)))
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
