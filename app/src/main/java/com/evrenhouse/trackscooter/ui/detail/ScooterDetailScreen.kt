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
                                val csv = Exporter.buildHistoryCsv(state.log, state.maintenance)
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
        )
    }
}


