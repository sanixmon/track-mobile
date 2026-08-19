package com.evrenhouse.trackscooter.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.data.SaveDeviceConditionRequest
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.StatusChip
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.ui.manage.SimpleDropdown
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.DeviceConditionHelper
import com.evrenhouse.trackscooter.util.DeviceFields
import com.evrenhouse.trackscooter.util.Exporter
import com.evrenhouse.trackscooter.util.FieldTone
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import com.evrenhouse.trackscooter.util.showToast
import kotlinx.coroutines.launch

@Composable
fun ScooterDetailScreen(
    scooterId: String,
    onBack: () -> Unit,
    viewModel: ScooterDetailViewModel = viewModel(factory = AppViewModelFactory(repository())),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Local editable condition (mirrors web ScooterDetailModal)
    var condition by remember { mutableStateOf(emptyMap<String, String>()) }
    var monitorDetail by remember { mutableStateOf("") }
    var edited by remember { mutableStateOf(false) }
    var savedSnapshot by remember { mutableStateOf<String?>(null) }
    var confirmComplete by remember { mutableStateOf(false) }

    fun currentSnapshot(): String = (condition + ("monitorDetail" to monitorDetail)).toString()

    // Seed condition from the loaded scooter (once)
    LaunchedEffect(state.scooter?.id) {
        state.scooter?.let { s ->
            if (savedSnapshot == null) {
                val dc = s.deviceCondition
                condition = mapOf(
                    "setelan" to (dc?.setelan ?: "ada"),
                    "lampu" to (dc?.lampu ?: "nyala"),
                    "baterai" to (dc?.baterai ?: "normal"),
                    "monitor" to (dc?.monitor ?: "normal"),
                    "rem" to (dc?.rem ?: "normal"),
                    "ban" to (dc?.ban ?: "aman"),
                )
                monitorDetail = dc?.monitorDetail ?: ""
                savedSnapshot = currentSnapshot()
            }
        }
    }

    LaunchedEffect(state.toast) {
        state.toast?.let {
            context.showToast(it)
            viewModel.consumeToast()
        }
    }

    val hasCondition = state.scooter?.deviceCondition != null
    val isDirty = edited || currentSnapshot() != savedSnapshot

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = TextMuted)
                }
                Column {
                    Text(state.scooter?.id ?: scooterId, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(TypeLabels.of(state.scooter?.type), color = TextMuted, fontSize = 11.sp)
                }
            }
            state.scooter?.let { StatusChip(it.status) }
        }

        when {
            state.loading && state.scooter == null -> LoadingState("Memuat detail unit...")
            state.scooter == null -> {
                Text(
                    "Unit tidak ditemukan.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            else -> {
                val scooter = state.scooter!!

                // Live timer + active maintenance badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface2.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    com.evrenhouse.trackscooter.ui.common.LiveTimer(scooter.status, scooter.lastUpdated)
                    if (scooter.activeMaintenance != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .background(Warning.copy(alpha = 0.12f), RoundedCornerShape(50))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Icon(Icons.Filled.Construction, contentDescription = null, tint = Warning, modifier = Modifier.size(11.dp))
                            Text("Dalam Perbaikan", color = Warning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Device condition editor
                ConditionEditor(
                    condition = condition,
                    onFieldChange = { key, value ->
                        edited = true
                        condition = condition + (key to value)
                    },
                    monitorDetail = monitorDetail,
                    onMonitorDetailChange = {
                        edited = true
                        monitorDetail = it
                    },
                    isDirty = isDirty,
                    hasCondition = hasCondition,
                    saving = state.saving,
                    onMarkAllNormal = {
                        condition = mapOf(
                            "setelan" to "ada", "lampu" to "nyala", "baterai" to "normal",
                            "monitor" to "normal", "rem" to "normal", "ban" to "aman",
                        )
                        monitorDetail = ""
                        edited = true
                    },
                    onSave = {
                        viewModel.saveCondition(
                            SaveDeviceConditionRequest(
                                setelan = condition["setelan"],
                                lampu = condition["lampu"],
                                baterai = condition["baterai"],
                                monitor = condition["monitor"],
                                rem = condition["rem"],
                                ban = condition["ban"],
                                monitorDetail = if (condition["monitor"] == "lain") monitorDetail else null,
                            )
                        )
                        edited = false
                        savedSnapshot = currentSnapshot()
                    },
                )

                // Active maintenance
                scooter.activeMaintenance?.let { am ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Warning.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                            .border(1.dp, Warning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Perbaikan Berjalan", color = Warning, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "${if (am.location == "outlet") "Di Outlet" else "Keluar / Di Luar"}${if (!am.issue.isNullOrBlank()) " · ${am.issue}" else ""}",
                                color = TextMuted,
                                fontSize = 11.sp,
                            )
                            if (!am.note.isNullOrBlank()) {
                                Text(am.note, color = TextMuted, fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                        }
                        Button(
                            onClick = { confirmComplete = true },
                            enabled = !state.completing,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Warning),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            if (state.completing) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                            Spacer(Modifier.width(4.dp))
                            Text("Selesai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // History
                HistorySection(
                    log = state.log,
                    maintenance = state.maintenance,
                    onExport = {
                        scope.launch {
                            runCatching {
                                val csv = buildHistoryCsv(state.log, state.maintenance)
                                val filename = "Riwayat-${scooter.id}-${DateUtils.localDateKey(DateUtils.today())}.csv"
                                Exporter.saveToDownloads(context, filename, csv)
                            }
                                .onSuccess { context.showToast("Excel diunduh") }
                                .onFailure { context.showToast(it.message ?: "Gagal export", long = true) }
                        }
                    },
                )
            }
        }
    }

    if (confirmComplete) {
        AlertDialog(
            onDismissRequest = { confirmComplete = false },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextMuted,
            title = { Text("Selesaikan Maintenance?", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Tandai perbaikan unit ${scooterId} selesai? Unit akan kembali tersedia.", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    state.scooter?.activeMaintenance?.id?.let { viewModel.completeMaintenance(it) }
                    confirmComplete = false
                }) { Text("Ya, Selesai", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            },
            dismissButton = {
                TextButton(onClick = { confirmComplete = false }) { Text("Batal", color = TextMuted, fontSize = 12.sp) }
            },
        )
    }
}

// ── Condition editor ───────────────────────────────────────
@Composable
private fun ConditionEditor(
    condition: Map<String, String>,
    onFieldChange: (String, String) -> Unit,
    monitorDetail: String,
    onMonitorDetailChange: (String) -> Unit,
    isDirty: Boolean,
    hasCondition: Boolean,
    saving: Boolean,
    onMarkAllNormal: () -> Unit,
    onSave: () -> Unit,
) {
    val dc = condition
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("KONDISI PERANGKAT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                if (isDirty && !saving) {
                    Text(
                        "Belum disimpan",
                        color = Warning,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Warning.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onMarkAllNormal,
                    enabled = !saving,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Semua Normal", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onSave,
                    enabled = !saving && isDirty,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    if (saving) {
                        CircularProgressIndicator(modifier = Modifier.size(11.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(11.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(if (saving) "Menyimpan..." else "Simpan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Text("Pilih nilai pada tiap kolom, lalu tekan Simpan", color = TextSubtle, fontSize = 10.sp)

        // 2-column grid
        DeviceFields.ALL.chunked(2).forEach { rowFields ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowFields.forEach { field ->
                    FieldEditor(
                        field = field,
                        value = dc[field.key] ?: "",
                        hasCondition = hasCondition,
                        onChange = { onFieldChange(field.key, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowFields.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        // Monitor detail input
        if (dc["monitor"] == "lain") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Warning.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                    .border(1.dp, Warning.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(14.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = monitorDetail,
                    onValueChange = onMonitorDetailChange,
                    placeholder = { Text("Ketik jenis error lainnya...", color = TextSubtle, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun FieldEditor(
    field: com.evrenhouse.trackscooter.util.DeviceField,
    value: String,
    hasCondition: Boolean,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tone = DeviceConditionHelper.fieldTone(field.key, value, hasCondition)
    val toneColor = when (tone) {
        FieldTone.BAD -> Red
        FieldTone.WARN -> Warning
        else -> TextPrimary
    }

    Column(
        modifier = modifier
            .background(Surface2.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(field.label, color = TextMuted, fontSize = 10.sp)
        SimpleDropdown(
            label = field.options.firstOrNull { it.first == value }?.second ?: "Belum dicek",
            options = if (value.isEmpty()) {
                listOf("" to "Belum dicek") + field.options
            } else {
                field.options
            },
            selected = value,
            onSelect = onChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = when (tone) {
                FieldTone.BAD -> "⚠ ${field.options.firstOrNull { it.first == value }?.second ?: ""}"
                FieldTone.WARN -> "⚠ ${field.options.firstOrNull { it.first == value }?.second ?: ""}"
                else -> ""
            },
            color = toneColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── History ────────────────────────────────────────────────
@Composable
private fun HistorySection(
    log: List<com.evrenhouse.trackscooter.data.ActivityLogEntry>,
    maintenance: List<com.evrenhouse.trackscooter.data.MaintenanceRecord>,
    onExport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("RIWAYAT UNIT (${log.size})", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            TextButton(onClick = onExport, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Filled.FileDownload, contentDescription = null, tint = Green, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Export Excel", color = Green, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (log.isEmpty() && maintenance.isEmpty()) {
            Text(
                "Belum ada riwayat untuk unit ini.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(20.dp),
            )
        } else {
            log.forEach { entry ->
                val isCheckout = entry.action == ActionLabels.CHECKOUT
                HistoryRow(
                    icon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = if (isCheckout) Red else Green, modifier = Modifier.size(13.dp)) },
                    title = if (isCheckout) "Keluar (Sewa)" else "Masuk (Kembali)",
                    subtitle = DateUtils.formatFull(entry.timestamp),
                    iconColor = if (isCheckout) Red else Green,
                )
            }
            maintenance.forEach { m ->
                val isDone = m.status == "done"
                HistoryRow(
                    icon = { Icon(Icons.Filled.Construction, contentDescription = null, tint = if (isDone) Green else Warning, modifier = Modifier.size(13.dp)) },
                    title = "Maintenance · ${m.issue ?: "Perbaikan"}",
                    subtitle = "${if (m.location == "outlet") "Di Outlet" else "Keluar / Di Luar"} · ${DateUtils.formatFull(m.startedAt)}" +
                        (if (isDone && m.resolvedAt != null) " → Selesai ${DateUtils.formatFull(m.resolvedAt)}" else ""),
                    iconColor = if (isDone) Green else Warning,
                    badge = if (isDone) "Selesai" to Green else "Repair" to Warning,
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    iconColor: Color,
    badge: Pair<String, Color>? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                badge?.let { (text, color) ->
                    Text(
                        text,
                        color = color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
            Text(subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}

// ── History CSV export ─────────────────────────────────────
private fun buildHistoryCsv(
    log: List<com.evrenhouse.trackscooter.data.ActivityLogEntry>,
    maintenance: List<com.evrenhouse.trackscooter.data.MaintenanceRecord>,
): String {
    val header = listOf("Tanggal", "Aksi", "Jenis")
    val historyRows = log.map { e ->
        listOf(
            DateUtils.formatFull(e.timestamp),
            if (e.action == "checkout") "Keluar (Sewa)" else "Masuk (Kembali)",
            TypeLabels.of(e.scooterType),
        )
    }
    val historyCsv = Exporter.toCsv(header, historyRows.ifEmpty { listOf(listOf("-", "Belum ada aktivitas", "-")) })

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
    val mCsv = Exporter.toCsv(mHeader, mRows.ifEmpty { listOf(listOf("-", "-", "-", "-", "-", "-")) })

    return "=== RIWAYAT UNIT ===\n$historyCsv\n=== RIWAYAT MAINTENANCE ===\n$mCsv"
}
