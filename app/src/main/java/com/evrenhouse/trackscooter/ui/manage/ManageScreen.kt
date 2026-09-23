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
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.util.Outlets
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
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
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
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()

    val activeOutlet by viewModel.selectedOutlet.collectAsState()
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var search by rememberSaveable { mutableStateOf("") }
    var filterStatus by rememberSaveable { mutableStateOf("all") }
    var filterType by rememberSaveable { mutableStateOf("all") }
    var sortBy by rememberSaveable { mutableStateOf("id-asc") }

    var statusDialog by remember { mutableStateOf<StatusDialogData?>(null) }
    var troubleScooter by remember { mutableStateOf<Scooter?>(null) }
    var busyAction by remember { mutableStateOf(false) }
    var editingScooter by remember { mutableStateOf<Scooter?>(null) }

    LaunchedEffect(mv.toast) {
        mv.toast?.let {
            if (it.contains("berhasil", ignoreCase = true) || it.contains("sukses", ignoreCase = true)) {
                sweetAlert.showSuccess(it)
            } else {
                sweetAlert.showInfo(it)
            }
            manageViewModel.consumeToast()
        }
    }

    val todayKey = DateUtils.localDateKey(DateUtils.today())
    fun todayCheckoutCount(id: String): Int =
        data.activityLog.count { it.scooterId == id && it.action == "checkout" && DateUtils.dateKey(it.timestamp) == todayKey }

    val filtered by remember(data.scooters, search, filterStatus, filterType, sortBy, activeOutlet) {
        derivedStateOf {
            data.scooters
                .filter { s ->
                    val matchesOutlet = activeOutlet == "all" || (s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)) == activeOutlet
                    val matchesSearch = s.id.contains(search, ignoreCase = true)
                    val matchesStatus = filterStatus == "all" || s.status == filterStatus
                    val matchesType = filterType == "all" || s.type == filterType
                    matchesOutlet && matchesSearch && matchesStatus && matchesType
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tambah Unit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

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
                                    .onSuccess { sweetAlert.showSuccess("File CSV berhasil diunduh") }
                                    .onFailure { sweetAlert.showError(it.toUserMessage()) }
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
                                    .onSuccess { sweetAlert.showSuccess("Semua QR Code diunduh ($it)") }
                                    .onFailure { sweetAlert.showError(it.toUserMessage()) }
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
                                    .onSuccess { sweetAlert.showSuccess("Backup DB Berhasil") }
                                    .onFailure { sweetAlert.showError(it.toUserMessage()) }
                                busyAction = false
                            }
                        },
                        enabled = !busyAction,
                    )
                }
            }
        }

        // Outlet Dropdown Filter
        OutletDropdown(
            selectedOutletId = activeOutlet,
            onOutletSelected = { viewModel.setSelectedOutlet(it) },
            getOutletCount = { outletId ->
                if (outletId == "all") data.scooters.size
                else data.scooters.count { (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == outletId }
            }
        )

        when {
            data.error != null && data.scooters.isEmpty() -> {
                ErrorState(message = data.error ?: "", onRetry = { viewModel.refresh() })
            }
            data.loading && data.scooters.isEmpty() -> {
                LoadingState("Memuat data scooter...")
            }
            else -> {

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
                    onDelete = { scooter ->
                        sweetAlert.showConfirm(
                            title = "Hapus Unit Scooter?",
                            message = "Apakah Anda yakin ingin menghapus scooter ${scooter.id}? Tindakan ini tidak dapat dibatalkan.",
                            confirmText = "Ya, Hapus Unit",
                            cancelText = "Batal",
                            isDanger = true,
                            onConfirm = {
                                scope.launch {
                                    val ok = manageViewModel.deleteScooter(scooter.id)
                                    if (ok) {
                                        viewModel.refresh()
                                        sweetAlert.showSuccess("Unit ${scooter.id} dihapus")
                                    } else {
                                        sweetAlert.showError("Gagal menghapus unit.")
                                    }
                                }
                            },
                        )
                    },
                    onDownloadQr = { scooter ->
                        scope.launch {
                            runCatching {
                                val bmp = QrUtils.generate(scooter.id, 400)
                                val png = bmp.toPngBytes()
                                val filename = "QR-${scooter.id}-${scooter.type.uppercase()}.png"
                                Exporter.saveBytesToDownloads(context, filename, png, "image/png")
                            }
                                .onSuccess { sweetAlert.showSuccess("QR ${scooter.id} diunduh") }
                                .onFailure { sweetAlert.showError(it.toUserMessage()) }
                        }
                    },
                    onTroubleSwap = { troubleScooter = it },
                    onEditScooter = { editingScooter = it },
                )
            }
        }
    }

    // Status change dialog
    statusDialog?.let { d ->
        StatusChangeDialog(
            scooter = d.scooter,
            newStatus = d.newStatus,
            onDismiss = { statusDialog = null },
            onConfirm = { location, locationDetail, issue, note ->
                scope.launch {
                    val request = when (d.newStatus) {
                        ScooterStatus.MAINTENANCE -> UpdateScooterRequest(
                            status = ScooterStatus.MAINTENANCE,
                            location = location,
                            locationDetail = locationDetail,
                            issue = issue,
                            note = note,
                            maintenanceNote = issue,
                        )
                        else -> UpdateScooterRequest(status = d.newStatus, maintenanceNote = null)
                    }
                    val ok = manageViewModel.updateStatus(d.scooter.id, request)
                    statusDialog = null
                    if (ok) {
                        viewModel.refresh()
                        sweetAlert.showSuccess("Status ${d.scooter.id} diperbarui")
                    } else {
                        sweetAlert.showError("Gagal mengubah status.")
                    }
                }
            },
        )
    }

    // Edit Scooter & Outlet Dialog
    editingScooter?.let { scooter ->
        EditScooterDialog(
            scooter = scooter,
            onDismiss = { editingScooter = null },
            onConfirm = { currentOutlet, status, location, locationDetail, issue, note ->
                scope.launch {
                    val req = UpdateScooterRequest(
                        status = status,
                        currentOutlet = currentOutlet,
                        maintenanceNote = if (status == ScooterStatus.MAINTENANCE) (issue ?: note) else null,
                        location = if (status == ScooterStatus.MAINTENANCE) location else null,
                        locationDetail = if (status == ScooterStatus.MAINTENANCE && location == "luar") locationDetail else null,
                        issue = if (status == ScooterStatus.MAINTENANCE) issue else null,
                        note = note,
                    )
                    val ok = manageViewModel.updateStatus(scooter.id, req)
                    if (ok) {
                        editingScooter = null
                        viewModel.refresh()
                        sweetAlert.showSuccess("Pengaturan unit ${scooter.id} berhasil disimpan")
                    } else {
                        sweetAlert.showError("Gagal memperbarui unit ${scooter.id}.")
                    }
                }
            }
        )
    }

    // Trouble / Tukar Dialog
    troubleScooter?.let { scooter ->
        TroubleSwapDialog(
            scooter = scooter,
            availableScooters = data.scooters.filter { it.status == ScooterStatus.AVAILABLE },
            onDismiss = { troubleScooter = null },
            onConfirm = { replacementId, note, issue, markBroken ->
                scope.launch {
                    val ok = viewModel.swapScooter(
                        scooterId = scooter.id,
                        replacementId = replacementId,
                        note = note,
                        issue = issue,
                        markBroken = markBroken
                    )
                    if (ok) {
                        troubleScooter = null
                        sweetAlert.showSuccess("Unit ${scooter.id} berhasil ditukar ke $replacementId")
                    } else {
                        sweetAlert.showError("Gagal menukar unit.")
                    }
                }
            },
        )
    }
    if (showAddDialog) {
        AddScooterDialog(
            scooters = data.scooters,
            onDismiss = { showAddDialog = false },
            onSubmit = { id, newType, newOutlet, addAnother ->
                scope.launch {
                    manageViewModel.setBusy(true)
                    val ok = manageViewModel.addScooter(id, newType, newOutlet)
                    manageViewModel.setBusy(false)
                    if (ok) {
                        viewModel.refresh()
                        sweetAlert.showSuccess("Unit ${id ?: "baru"} berhasil ditambahkan")
                        if (!addAnother) {
                            showAddDialog = false
                        }
                    } else {
                        sweetAlert.showError("Gagal menambahkan unit. ID mungkin sudah terdaftar.")
                    }
                }
            },
            submitting = mv.busy,
            initialOutlet = if (activeOutlet != "all") activeOutlet else "utara"
        )
    }
}

data class StatusDialogData(val scooter: Scooter, val newStatus: String)


