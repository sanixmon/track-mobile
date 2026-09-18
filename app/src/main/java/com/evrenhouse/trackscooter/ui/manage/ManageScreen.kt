package com.evrenhouse.trackscooter.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.evrenhouse.trackscooter.util.toPngBytes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.UpdateScooterRequest
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Exporter
import com.evrenhouse.trackscooter.util.DeviceConditionHelper
import com.evrenhouse.trackscooter.util.QrZip
import com.evrenhouse.trackscooter.util.QrUtils
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import com.evrenhouse.trackscooter.util.showToast
import kotlinx.coroutines.launch

@Composable
fun ManageScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: (String) -> Unit,
    manageViewModel: ManageViewModel = viewModel(factory = AppViewModelFactory(repository())),
) {
    val data by viewModel.state.collectAsState()
    val mv by manageViewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var idInput by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf("sd") }
    var search by rememberSaveable { mutableStateOf("") }
    var filterStatus by rememberSaveable { mutableStateOf("all") }
    var filterType by rememberSaveable { mutableStateOf("all") }
    var sortBy by rememberSaveable { mutableStateOf("id-asc") }

    var confirmDelete by remember { mutableStateOf<Scooter?>(null) }
    var statusDialog by remember { mutableStateOf<StatusDialogData?>(null) }
    var busyAction by remember { mutableStateOf(false) }

    LaunchedEffect(mv.toast) {
        mv.toast?.let {
            context.showToast(it)
            manageViewModel.consumeToast()
        }
    }

    val todayKey = DateUtils.localDateKey(DateUtils.today())
    fun todayCheckoutCount(id: String): Int =
        data.activityLog.count { it.scooterId == id && it.action == "checkout" && DateUtils.dateKey(it.timestamp) == todayKey }

    val filtered by remember {
        derivedStateOf {
            data.scooters
                .filter { s ->
                    s.id.contains(search, ignoreCase = true) &&
                        (filterStatus == "all" || s.status == filterStatus) &&
                        (filterType == "all" || s.type == filterType)
                }
                .sortedWith(compareScooters(sortBy, ::todayCheckoutCount))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Column {
            Text("Kelola Scooter", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Tambah unit scooter baru, ubah status unit, dan unduh QR code untuk operasional", color = TextMuted, fontSize = 13.sp)
        }

        var dataMenuExpanded by remember { mutableStateOf(false) }

        // Compact Data Actions Dropdown
        Box {
            OutlinedButton(
                onClick = { dataMenuExpanded = !dataMenuExpanded },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Surface,
                    contentColor = TextPrimary,
                ),
            ) {
                if (busyAction) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Memproses...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Filled.Download, contentDescription = null, tint = Accent, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Aksi Data", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            DropdownMenu(
                expanded = dataMenuExpanded,
                onDismissRequest = { dataMenuExpanded = false },
                modifier = Modifier
                    .background(Surface, RoundedCornerShape(12.dp))
                    .border(1.dp, Border, RoundedCornerShape(12.dp)),
            ) {
                DropdownMenuItem(
                    text = {
                        Column {
                            Text("Export Kondisi Unit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Unduh laporan kondisi (.csv)", fontSize = 10.sp, color = TextMuted)
                        }
                    },
                    leadingIcon = { Icon(Icons.Filled.GridView, null, tint = Accent, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        dataMenuExpanded = false
                        scope.launch {
                            busyAction = true
                            runCatching {
                                val csv = Exporter.buildConditionsCsv(data.scooters)
                                Exporter.saveToDownloads(context, "Kondisi-Scooter-${todayKey}.csv", csv)
                            }
                                .onSuccess { context.showToast("File CSV berhasil diunduh") }
                                .onFailure { context.showToast(it.toUserMessage(), long = true) }
                            busyAction = false
                        }
                    },
                    enabled = data.scooters.isNotEmpty() && !busyAction,
                )

                DropdownMenuItem(
                    text = {
                        Column {
                            Text("Unduh Semua QR", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Arsip semua kode QR (.zip)", fontSize = 10.sp, color = TextMuted)
                        }
                    },
                    leadingIcon = { Icon(Icons.Filled.FolderZip, null, tint = Accent, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        dataMenuExpanded = false
                        scope.launch {
                            busyAction = true
                            runCatching {
                                QrZip.zipAllQrs(context, data.scooters.map { it.id to it.type })
                            }
                                .onSuccess { context.showToast("Semua QR Code diunduh ($it)") }
                                .onFailure { context.showToast(it.toUserMessage(), long = true) }
                            busyAction = false
                        }
                    },
                    enabled = data.scooters.isNotEmpty() && !busyAction,
                )

                DropdownMenuItem(
                    text = {
                        Column {
                            Text("Backup Basis Data", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Cadangan database (.db)", fontSize = 10.sp, color = TextMuted)
                        }
                    },
                    leadingIcon = { Icon(Icons.Filled.CloudDownload, null, tint = Accent, modifier = Modifier.size(16.dp)) },
                    onClick = {
                        dataMenuExpanded = false
                        scope.launch {
                            busyAction = true
                            runCatching {
                                val bytes = manageViewModel.downloadBackupBytes()
                                    ?: throw IllegalStateException("Gagal mengunduh backup.")
                                val filename = "trackscooter_backup_${todayKey}.db"
                                Exporter.saveBytesToDownloads(context, filename, bytes, "application/octet-stream")
                            }
                                .onSuccess { context.showToast("Backup DB Berhasil") }
                                .onFailure { context.showToast(it.toUserMessage(), long = true) }
                            busyAction = false
                        }
                    },
                    enabled = !busyAction,
                )
            }
        }

        when {
            data.error != null && data.scooters.isEmpty() -> {
                ErrorState(message = data.error ?: "", onRetry = { viewModel.refresh() })
            }
            data.loading && data.scooters.isEmpty() -> {
                LoadingState("Memuat data scooter...")
            }
            else -> {
                // Add form
                AddScooterForm(
                    idInput = idInput,
                    onIdInput = { idInput = it },
                    type = type,
                    onTypeChange = { type = it },
                    error = mv.error,
                    submitting = mv.busy,
                    onSubmit = {
                        scope.launch {
                            manageViewModel.setBusy(true)
                            val ok = manageViewModel.addScooter(idInput, type)
                            manageViewModel.setBusy(false)
                            if (ok) {
                                idInput = ""
                                type = "sd"
                                viewModel.refresh()
                                context.showToast("Scooter berhasil ditambahkan")
                            } else {
                                manageViewModel.setError("Gagal menambahkan scooter. Periksa koneksi atau ID sudah terdaftar.")
                            }
                        }
                    },
                )

                // List
                ScooterList(
                    scooters = filtered,
                    search = search,
                    onSearch = { search = it },
                    filterStatus = filterStatus,
                    onFilterStatus = { filterStatus = it },
                    filterType = filterType,
                    onFilterType = { filterType = it },
                    sortBy = sortBy,
                    onSortBy = { sortBy = it },
                    getTodayCount = ::todayCheckoutCount,
                    onOpenDetail = onOpenDetail,
                    onStatusChange = { scooter, newStatus ->
                        statusDialog = StatusDialogData(scooter, newStatus)
                    },
                    onDelete = { confirmDelete = it },
                    onDownloadQr = { scooter ->
                        scope.launch {
                            runCatching {
                                val bmp = QrUtils.generate(scooter.id, 400)
                                val png = bmp.toPngBytes()
                                val filename = "QR-${scooter.id}-${scooter.type.uppercase()}.png"
                                Exporter.saveBytesToDownloads(context, filename, png, "image/png")
                            }
                                .onSuccess { context.showToast("QR ${scooter.id} diunduh") }
                                .onFailure { context.showToast(it.toUserMessage(), long = true) }
                        }
                    },
                )
            }
        }
    }

    // Delete confirmation
    confirmDelete?.let { scooter ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = Surface,
            titleContentColor = TextPrimary,
            textContentColor = TextMuted,
            icon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Red, modifier = Modifier.size(24.dp)) },
            title = { Text("Hapus Unit Scooter?", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus scooter ${scooter.id}? Tindakan ini tidak dapat dibatalkan.", fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        val ok = manageViewModel.deleteScooter(scooter.id)
                        confirmDelete = null
                        if (ok) {
                            viewModel.refresh()
                            context.showToast("Unit ${scooter.id} dihapus")
                        } else {
                            context.showToast("Gagal menghapus unit.", long = true)
                        }
                    }
                }) { Text("Ya, Hapus Unit", color = Red, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("Batal", color = TextMuted, fontSize = 12.sp) }
            },
        )
    }

    // Status change dialog
    statusDialog?.let { d ->
        StatusChangeDialog(
            scooter = d.scooter,
            newStatus = d.newStatus,
            onDismiss = { statusDialog = null },
            onConfirm = { location, issue, note ->
                scope.launch {
                    val request = when (d.newStatus) {
                        ScooterStatus.MAINTENANCE -> UpdateScooterRequest(
                            status = ScooterStatus.MAINTENANCE,
                            location = location,
                            issue = issue,
                            note = note,
                            maintenanceNote = issue,
                        )
                        ScooterStatus.RUSAK -> UpdateScooterRequest(
                            status = ScooterStatus.RUSAK,
                            maintenanceNote = note,
                        )
                        else -> UpdateScooterRequest(status = d.newStatus, maintenanceNote = null)
                    }
                    val ok = manageViewModel.updateStatus(d.scooter.id, request)
                    statusDialog = null
                    if (ok) {
                        viewModel.refresh()
                        context.showToast("Status ${d.scooter.id} diperbarui")
                    } else {
                        context.showToast("Gagal mengubah status.", long = true)
                    }
                }
            },
        )
    }
}

data class StatusDialogData(val scooter: Scooter, val newStatus: String)


