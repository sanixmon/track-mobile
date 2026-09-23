package com.evrenhouse.trackscooter.ui.detail

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.data.SaveDeviceConditionRequest
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.LiveTimer
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.StatusChip
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.ui.common.TypeBadge
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
import com.evrenhouse.trackscooter.util.Exporter
import com.evrenhouse.trackscooter.util.FieldTone
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import com.evrenhouse.trackscooter.util.Outlets
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
                val isDark = LocalThemeIsDark.current
                val scooter = state.scooter
                val idText = scooter?.id ?: scooterId
                val nameColor = ScooterColors.getScooterNameColor(scooter?.type, idText, scooter?.currentOutlet, isDark)
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(idText, color = nameColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        if (scooter != null) {
                            TypeBadge(scooter.type, id = scooter.id, outlet = scooter.currentOutlet)
                            val currentOutletId = scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type)
                            val outletColor = ScooterColors.getOutletColor(currentOutletId)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showOutletDialog = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("· ${Outlets.labelOf(currentOutletId)}", color = outletColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Ubah Pangkalan", tint = outletColor, modifier = Modifier.size(13.dp))
                            }
                        }
                    }
                    Text(TypeLabels.of(scooter?.type), color = TextMuted, fontSize = 11.sp)
                }
            }
            state.scooter?.let { s ->
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
            }
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
                    LiveTimer(scooter.status, scooter.lastUpdated)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
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
                        if (scooter.status == ScooterStatus.IN_USE) {
                            Button(
                                onClick = { showTroubleDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Warning),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            ) {
                                Icon(Icons.Filled.WarningAmber, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Trouble / Tukar", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
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
                            onClick = {
                                sweetAlert.showConfirm(
                                    title = "Selesaikan Maintenance?",
                                    message = "Tandai perbaikan unit $scooterId selesai? Unit akan kembali tersedia.",
                                    confirmText = "Ya, Selesai",
                                    cancelText = "Batal",
                                    onConfirm = {
                                        state.scooter?.activeMaintenance?.id?.let { viewModel.completeMaintenance(it) }
                                    },
                                )
                            },
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
                                val csv = Exporter.buildHistoryCsv(state.log, state.maintenance)
                                val filename = "Riwayat-${scooter.id}-${DateUtils.localDateKey(DateUtils.today())}.csv"
                                Exporter.saveToDownloads(context, filename, csv)
                            }
                                .onSuccess { sweetAlert.showSuccess("Excel diunduh") }
                                .onFailure { sweetAlert.showError(it.message ?: "Gagal export") }
                        }
                    },
                )
            }
        }
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
            title = { Text("Pindahkan Pangkalan", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Pilih lokasi pangkalan untuk unit ${s.id}:", color = TextMuted, fontSize = 12.sp)
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
                    viewModel.updateStatus(targetStatus, location, locationDetail, issue, note)
                    statusChangeTarget = null
                }
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ScooterDetailDialog(
    scooterId: String,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(20.dp))
        ) {
            ScooterDetailScreen(
                scooterId = scooterId,
                onBack = onDismiss
            )
        }
    }
}


