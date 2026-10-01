package com.evrenhouse.trackscooter.ui.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.clickable
import com.evrenhouse.trackscooter.ui.manage.StatusChangeDialog
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.data.SaveDeviceConditionRequest
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.LiveTimer
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.DetailSkeleton
import com.evrenhouse.trackscooter.ui.common.StatusChip
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.ui.common.repository
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
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.util.ScooterColors
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.DeviceConditionHelper
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.util.DeviceFields
import com.evrenhouse.trackscooter.util.MIME_XLSX
import com.evrenhouse.trackscooter.util.Exporter
import com.evrenhouse.trackscooter.util.FieldTone
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import kotlinx.coroutines.launch

@Composable
fun ScooterDetailScreen(
    scooterId: String,
    onBack: () -> Unit,
    viewModel: ScooterDetailViewModel = viewModel(
        key = "detail_$scooterId",
        factory = AppViewModelFactory(repository())
    ),
) {
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showCompleteConfirm by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(scooterId) {
        viewModel.loadScooter(scooterId)
    }

    // Local editable condition (mirrors web ScooterDetailModal)
    var condition by remember { mutableStateOf(emptyMap<String, String>()) }
    var monitorDetail by remember { mutableStateOf("") }
    var edited by remember { mutableStateOf(false) }
    var savedSnapshot by remember { mutableStateOf<String?>(null) }
    var showTroubleDialog by remember { mutableStateOf(false) }
    var showOutletDialog by remember { mutableStateOf(false) }
    var statusMenuOpen by remember { mutableStateOf(false) }
    var statusChangeTarget by remember { mutableStateOf<String?>(null) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showEditMaintenance by remember { mutableStateOf(false) }

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
            if (it.contains("berhasil", ignoreCase = true) || it.contains("selesai", ignoreCase = true) || it.contains("sukses", ignoreCase = true)) {
                sweetAlert.showSuccess(it)
            } else {
                sweetAlert.showInfo(it)
            }
            viewModel.consumeToast()
        }
    }

    val hasCondition by remember {
        derivedStateOf { state.scooter?.deviceCondition != null }
    }
    val isDirty by remember {
        derivedStateOf { edited || currentSnapshot() != savedSnapshot }
    }
    BackHandler(enabled = isDirty) {
        showDiscardDialog = true
    }

    Box(modifier = Modifier.fillMaxSize().background(Surface)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── 3. TOP APP BAR: Back, ID (onSurface, Monospace), Outlet subtitle, Menu ⋮ ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    IconButton(
                        onClick = { if (isDirty) showDiscardDialog = true else onBack() },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke daftar unit", tint = TextPrimary)
                    }

                    val scooter = state.scooter
                    val idText = scooter?.id ?: scooterId
                    val currentOutletId = scooter?.currentOutlet ?: Outlets.getHomeOutletForType(scooter?.type ?: "sd")
                    val isDark = LocalThemeIsDark.current
                    val idColor = remember(scooter?.type, scooter?.id, scooter?.currentOutlet, isDark) {
                        ScooterColors.getScooterNameColor(scooter?.type, scooter?.id, scooter?.currentOutlet, isDark)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = idText,
                            color = idColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                        )
                        // Outlet sebagai subtitle satu baris tanpa awalan "Outlet:"
                        Text(
                            text = Outlets.labelOf(currentOutletId),
                            color = TextMuted,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Menu Titik Tiga (⋮) di app bar untuk Ganti Outlet & Tukar Unit
                var appBarMenuOpen by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { appBarMenuOpen = true }, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu opsi detail unit", tint = TextMuted)
                    }

                    DropdownMenu(
                        expanded = appBarMenuOpen,
                        onDismissRequest = { appBarMenuOpen = false },
                        modifier = Modifier
                            .background(Surface, RoundedCornerShape(12.dp))
                            .border(1.dp, Border, RoundedCornerShape(12.dp)),
                    ) {
                        DropdownMenuItem(
                            text = { Text("Pindahkan Outlet", fontSize = 12.sp, color = TextPrimary) },
                            leadingIcon = { Icon(Icons.Filled.LocationOn, null, tint = Accent, modifier = Modifier.size(16.dp)) },
                            onClick = {
                                appBarMenuOpen = false
                                showOutletDialog = true
                            },
                        )
                        if (state.scooter?.status == ScooterStatus.IN_USE) {
                            DropdownMenuItem(
                                text = { Text("Trouble / Tukar Unit", fontSize = 12.sp, color = Warning) },
                                leadingIcon = { Icon(Icons.Filled.WarningAmber, null, tint = Warning, modifier = Modifier.size(16.dp)) },
                                onClick = {
                                    appBarMenuOpen = false
                                    showTroubleDialog = true
                                },
                            )
                        }
                    }
                }
            }

            // ── Status Chip di bawah judul & Tanggal diperbarui absolut ──
            state.scooter?.let { s ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box {
                        StatusChip(
                            status = s.status,
                            modifier = Modifier.clickable { statusMenuOpen = true }
                        )
                        DropdownMenu(
                            expanded = statusMenuOpen,
                            onDismissRequest = { statusMenuOpen = false },
                            modifier = Modifier
                                .background(Surface, RoundedCornerShape(10.dp))
                                .border(1.dp, Border, RoundedCornerShape(10.dp))
                        ) {
                            listOf(
                                ScooterStatus.AVAILABLE to "Unit Ready",
                                ScooterStatus.IN_USE to "Unit Diluar",
                                ScooterStatus.MAINTENANCE to "Unit Kendala",
                            ).forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            label,
                                            color = if (s.status == value) Accent else TextPrimary,
                                            fontWeight = if (s.status == value) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    onClick = {
                                        statusMenuOpen = false
                                        if (value != s.status) {
                                            statusChangeTarget = value
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Tanggal kondisi diperbarui relatif + tanggal absolut
                    val timeAgo = DateUtils.timeAgo(s.lastUpdated)
                    val absTime = DateUtils.formatContextualTime(s.lastUpdated)
                    Text(
                        text = "Kondisi diperbarui $timeAgo ($absTime)",
                        color = TextSubtle,
                        fontSize = 10.5.sp,
                    )
                }
            }

            when {
                state.loading && state.scooter == null -> DetailSkeleton()
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

                    // ── 1. Blok Status dan Aksi Perbaikan Terpadu (di bawah header, di atas form) ──
                    UnitStatusBlock(
                        scooter = scooter,
                        onEditMaintenance = { showEditMaintenance = true },
                        onCompleteMaintenance = { showCompleteConfirm = true },
                        completing = state.completing,
                    )

                    // ── 4. Form Kondisi Perangkat Kompak ──
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
                        modifier = Modifier.fillMaxWidth(),
                    )

                    // ── 6. Laporan Riwayat ──
                    HistorySection(
                        log = state.log,
                        maintenance = state.maintenance,
                        onOpenFullHistory = {
                            viewModel.loadTechnicalHistory()
                            showHistorySheet = true
                        },
                        onExport = {
                            scope.launch {
                                runCatching {
                                    val bytes = Exporter.buildHistoryXlsx(scooter, state.log, state.maintenance)
                                    val filename = "Riwayat-${scooter.id}-${DateUtils.localDateKey(DateUtils.today())}.xlsx"
                                    Exporter.saveBytesToDownloads(context, filename, bytes, MIME_XLSX)
                                }
                                    .onSuccess { sweetAlert.showSuccess("File XLSX berhasil diunduh ($it)") }
                                    .onFailure { sweetAlert.showError(it.message ?: "Gagal export") }
                            }
                        },
                    )
                }
            }
        }

        // ── 5. Sticky Bottom Bar Simpan & Batal (Hanya muncul saat dirty) ──
        SaveBar(
            isDirty = isDirty,
            saving = state.saving,
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
            onCancel = {
                state.scooter?.deviceCondition?.let { dc ->
                    condition = mapOf(
                        "setelan" to (dc.setelan ?: "ada"),
                        "lampu" to (dc.lampu ?: "nyala"),
                        "baterai" to (dc.baterai ?: "normal"),
                        "monitor" to (dc.monitor ?: "normal"),
                        "rem" to (dc.rem ?: "normal"),
                        "ban" to (dc.ban ?: "aman"),
                    )
                    monitorDetail = dc.monitorDetail ?: ""
                }
                edited = false
            },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    // Dialog konfirmasi "Selesai Maintenance"
    if (showCompleteConfirm && state.scooter != null) {
        AlertDialog(
            onDismissRequest = { showCompleteConfirm = false },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextMuted,
            title = { Text("Selesaikan Maintenance?", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Tandai perbaikan unit $scooterId selesai? Unit akan kembali berstatus Ready.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showCompleteConfirm = false
                        state.scooter?.activeMaintenance?.id?.let { viewModel.completeMaintenance(it) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text("Ya, Selesai", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteConfirm = false }) {
                    Text("Batal")
                }
            },
        )
    }

    // Dialog konfirmasi "Buang Perubahan" jika back ditekan saat dirty
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextMuted,
            title = { Text("Buang Perubahan?", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Perubahan kondisi perangkat belum disimpan. Yakin ingin keluar tanpa menyimpan?", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                ) {
                    Text("Buang", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Batal")
                }
            },
        )
    }

    if (showTroubleDialog && state.scooter != null) {
        TroubleSwapDialog(
            scooter = state.scooter!!,
            availableScooters = state.allScooters.filter { it.status == ScooterStatus.AVAILABLE },
            onDismiss = { showTroubleDialog = false },
            onConfirm = { replacementId, note, issue, markBroken ->
                scope.launch {
                    val ok = viewModel.swapScooter(
                        scooterId = state.scooter!!.id,
                        replacementId = replacementId,
                        note = note,
                        issue = issue,
                        markBroken = markBroken
                    )
                    showTroubleDialog = false
                    if (ok) {
                        sweetAlert.showSuccess("Unit ${state.scooter?.id} berhasil ditukar ke $replacementId")
                        viewModel.refresh()
                    } else {
                        sweetAlert.showError("Gagal menukar unit.")
                    }
                }
            },
        )
    }

    if (showOutletDialog && state.scooter != null) {
        val s = state.scooter!!
        val currentOutletId = s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)
        AlertDialog(
            onDismissRequest = { showOutletDialog = false },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            title = { Text("Pindahkan Outlet", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pilih lokasi outlet untuk unit ${s.id}:", color = TextMuted, fontSize = 12.sp)
                    Outlets.OPERATIONAL.forEach { o ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (o.id == currentOutletId) Accent.copy(alpha = 0.12f) else Surface2)
                                .clickable {
                                    viewModel.updateOutlet(o.id)
                                    showOutletDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                o.label,
                                color = if (o.id == currentOutletId) Accent else TextPrimary,
                                fontWeight = if (o.id == currentOutletId) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            if (o.id == currentOutletId) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showOutletDialog = false }) { Text("Batal", color = TextMuted) }
            }
        )
    }

    statusChangeTarget?.let { targetStatus ->
        state.scooter?.let { s ->
            StatusChangeDialog(
                scooter = s,
                newStatus = targetStatus,
                onDismiss = { statusChangeTarget = null },
                onConfirm = { location, locationDetail, issue, note ->
                    // "" agar terkirim dan server clear saat outlet (explicitNulls=false omit null).
                    val detailToSend = if (targetStatus == ScooterStatus.MAINTENANCE) (locationDetail ?: "") else locationDetail
                    viewModel.updateStatus(targetStatus, location, detailToSend, issue, note)
                    statusChangeTarget = null
                }
            )
        }
    }

    if (showEditMaintenance && state.scooter != null) {
        StatusChangeDialog(
            scooter = state.scooter!!,
            newStatus = ScooterStatus.MAINTENANCE,
            dialogTitle = "Edit Kendala Unit ${state.scooter!!.id}",
            onDismiss = { showEditMaintenance = false },
            onConfirm = { location, locationDetail, issue, note ->
                val detailToSend = locationDetail ?: ""
                viewModel.updateStatus(ScooterStatus.MAINTENANCE, location, detailToSend, issue, note)
                showEditMaintenance = false
            }
        )
    }

    if (showHistorySheet && state.scooter != null) {
        val filteredActivities = when (state.historyCategoryFilter) {
            "usage" -> state.technicalActivities.filter { it.type == "usage" }
            "maintenance" -> state.technicalActivities.filter { it.type == "maintenance" }
            else -> state.technicalActivities
        }

        ScooterHistoryBottomSheet(
            scooter = state.scooter!!,
            activities = filteredActivities,
            isLoading = state.historyLoading,
            selectedPreset = state.historyDatePreset,
            selectedCategory = state.historyCategoryFilter,
            onSelectPreset = { preset, start, end ->
                viewModel.setHistoryDatePreset(preset, start, end)
            },
            onSelectCategory = { cat ->
                viewModel.setHistoryCategoryFilter(cat)
            },
            onExportXlsx = {
                scope.launch {
                    runCatching {
                        val bytes = Exporter.buildTechnicalHistoryXlsx(
                            scooter = state.scooter!!,
                            activities = filteredActivities,
                            dateRangeLabel = state.historyDatePreset
                        )
                        val periodClean = state.historyDatePreset.replace("\\s+".toRegex(), "-")
                        val filename = "Laporan-Aktivitas-${state.scooter!!.id}-$periodClean.xlsx"
                        Exporter.saveBytesToDownloads(context, filename, bytes, MIME_XLSX)
                    }
                        .onSuccess { sweetAlert.showSuccess("File XLSX berhasil diunduh ($it)") }
                        .onFailure { sweetAlert.showError(it.message ?: "Gagal export") }
                }
            },
            onDismiss = { showHistorySheet = false }
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "Detail Screen - Maintenance Active (Light)", showBackground = true)
@Composable
private fun DetailScreenMaintenanceLightPreview() {
    val sampleScooter = Scooter(
        id = "SB-53",
        type = "sb",
        status = ScooterStatus.MAINTENANCE,
        currentOutlet = "utara",
        lastUpdated = "2026-09-25T10:00:00Z",
        maintenanceNote = "e2",
        activeMaintenance = com.evrenhouse.trackscooter.data.ActiveMaintenance(
            id = "m-1",
            location = "outlet",
            issue = "e2",
            status = "repair",
            startedAt = "2026-09-25T10:00:00Z",
        ),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.fillMaxSize().background(Surface)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                UnitStatusBlock(
                    scooter = sampleScooter,
                    onEditMaintenance = {},
                    onCompleteMaintenance = {},
                )
                ConditionEditor(
                    condition = mapOf("setelan" to "ada", "lampu" to "nyala", "baterai" to "normal", "rem" to "normal", "ban" to "aman", "monitor" to "normal"),
                    onFieldChange = { _, _ -> },
                    monitorDetail = "",
                    onMonitorDetailChange = {},
                )
            }
        }
    }
}

@Preview(name = "Detail Screen - Maintenance Active (Dark)", showBackground = true)
@Composable
private fun DetailScreenMaintenanceDarkPreview() {
    val sampleScooter = Scooter(
        id = "SB-53",
        type = "sb",
        status = ScooterStatus.MAINTENANCE,
        currentOutlet = "utara",
        lastUpdated = "2026-09-25T10:00:00Z",
        maintenanceNote = "e2",
        activeMaintenance = com.evrenhouse.trackscooter.data.ActiveMaintenance(
            id = "m-1",
            location = "outlet",
            issue = "e2",
            status = "repair",
            startedAt = "2026-09-25T10:00:00Z",
        ),
    )
    TrackScooterTheme(isDark = true) {
        Box(modifier = Modifier.fillMaxSize().background(Surface)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                UnitStatusBlock(
                    scooter = sampleScooter,
                    onEditMaintenance = {},
                    onCompleteMaintenance = {},
                )
                ConditionEditor(
                    condition = mapOf("setelan" to "ada", "lampu" to "nyala", "baterai" to "normal", "rem" to "normal", "ban" to "aman", "monitor" to "normal"),
                    onFieldChange = { _, _ -> },
                    monitorDetail = "",
                    onMonitorDetailChange = {},
                )
            }
        }
    }
}


