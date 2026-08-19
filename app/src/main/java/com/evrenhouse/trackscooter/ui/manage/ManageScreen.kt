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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

    var idInput by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("sd") }
    var search by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("all") }
    var filterType by remember { mutableStateOf("all") }
    var sortBy by remember { mutableStateOf("id-asc") }

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

    val filtered = data.scooters
        .filter { s ->
            s.id.contains(search, ignoreCase = true) &&
                (filterStatus == "all" || s.status == filterStatus) &&
                (filterType == "all" || s.type == filterType)
        }
        .sortedWith(compareScooters(sortBy, ::todayCheckoutCount))

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

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ManageActionButton(
                text = "Export Kondisi",
                icon = { tint -> Icon(Icons.Filled.GridView, null, Modifier.size(14.dp), tint = tint) },
                enabled = data.scooters.isNotEmpty() && !busyAction,
                onClick = {
                    scope.launch {
                        busyAction = true
                        runCatching {
                            val csv = buildConditionsCsv(data.scooters)
                            Exporter.saveToDownloads(context, "Kondisi-Scooter-${todayKey}.csv", csv)
                        }
                            .onSuccess { context.showToast("File CSV berhasil diunduh") }
                            .onFailure { context.showToast(it.toUserMessage(), long = true) }
                        busyAction = false
                    }
                },
            )
            ManageActionButton(
                text = "Unduh Semua QR",
                icon = { tint -> Icon(Icons.Filled.FolderZip, null, Modifier.size(14.dp), tint = tint) },
                enabled = data.scooters.isNotEmpty() && !busyAction,
                onClick = {
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
            )
            ManageActionButton(
                text = "Backup DB",
                icon = { tint -> Icon(Icons.Filled.CloudDownload, null, Modifier.size(14.dp), tint = tint) },
                enabled = !busyAction,
                onClick = {
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
            )
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

// ── Add form ───────────────────────────────────────────────
@Composable
private fun AddScooterForm(
    idInput: String,
    onIdInput: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    error: String?,
    submitting: Boolean,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("TAMBAH UNIT BARU", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

        error?.let {
            Text(it, color = Red, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ID Scooter (Opsional)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${type.uppercase()}-", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { onIdInput(it.filter(Char::isDigit)) },
                    placeholder = { Text("099 (Auto jika kosong)", color = TextSubtle, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Jenis Scooter", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            SimpleDropdown(
                label = TypeLabels.of(type),
                options = listOf("sd" to TypeLabels.of("sd"), "sj" to TypeLabels.of("sj")),
                selected = type,
                onSelect = onTypeChange,
            )
        }

        Button(
            onClick = onSubmit,
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
        ) {
            if (submitting) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(if (submitting) "Memproses..." else "Tambah Scooter", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Scooter list ───────────────────────────────────────────
@Composable
private fun ScooterList(
    scooters: List<Scooter>,
    search: String,
    onSearch: (String) -> Unit,
    filterStatus: String,
    onFilterStatus: (String) -> Unit,
    filterType: String,
    onFilterType: (String) -> Unit,
    sortBy: String,
    onSortBy: (String) -> Unit,
    getTodayCount: (String) -> Int,
    onOpenDetail: (String) -> Unit,
    onStatusChange: (Scooter, String) -> Unit,
    onDelete: (Scooter) -> Unit,
    onDownloadQr: (Scooter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        // Toolbar
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("DAFTAR UNIT (${scooters.size})", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

            OutlinedTextField(
                value = search,
                onValueChange = onSearch,
                placeholder = { Text("Cari ID...", color = TextSubtle, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SimpleDropdown(
                    label = if (filterStatus == "all") "Semua Status" else StatusLabels.of(filterStatus),
                    options = listOf(
                        "all" to "Semua Status",
                        "available" to "Tersedia",
                        "in-use" to "Online",
                        "rusak" to "Offline / Rusak",
                        "maintenance" to "Maintenance",
                    ),
                    selected = filterStatus,
                    onSelect = onFilterStatus,
                    modifier = Modifier.weight(1f),
                )
                SimpleDropdown(
                    label = if (filterType == "all") "Semua Jenis" else TypeLabels.of(filterType),
                    options = listOf("all" to "Semua Jenis", "sd" to "Standar (SD)", "sj" to "Jumbo (SJ)"),
                    selected = filterType,
                    onSelect = onFilterType,
                    modifier = Modifier.weight(1f),
                )
            }
            SimpleDropdown(
                label = sortLabel(sortBy),
                options = listOf(
                    "id-asc" to "Urutkan: ID (A-Z)",
                    "id-desc" to "Urutkan: ID (Z-A)",
                    "today-checkout" to "Urutkan: Keluar Hari Ini (Terbanyak)",
                    "status" to "Urutkan: Status",
                    "type" to "Urutkan: Jenis",
                ),
                selected = sortBy,
                onSelect = onSortBy,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (scooters.isEmpty()) {
            Text("Tidak ada scooter ditemukan.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(28.dp))
        } else {
            scooters.forEach { scooter ->
                ScooterRow(
                    scooter = scooter,
                    todayCount = getTodayCount(scooter.id),
                    onOpenDetail = { onOpenDetail(scooter.id) },
                    onStatusChange = { onStatusChange(scooter, it) },
                    onDelete = { onDelete(scooter) },
                    onDownloadQr = { onDownloadQr(scooter) },
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Border),
                )
            }
        }
    }
}

@Composable
private fun ScooterRow(
    scooter: Scooter,
    todayCount: Int,
    onOpenDetail: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDownloadQr: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(scooter.id, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                TypeBadge(scooter.type)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onDownloadQr, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.QrCode, contentDescription = "Unduh QR", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (scooter.status == ScooterStatus.IN_USE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(Accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .border(1.dp, Accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .background(Accent, CircleShapeCompat),
                    )
                    Text("Online", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            } else {
                SimpleDropdown(
                    label = StatusLabels.of(scooter.status),
                    options = listOf(
                        "available" to "Tersedia",
                        "rusak" to "Offline / Rusak",
                        "maintenance" to "Maintenance",
                    ),
                    selected = scooter.status,
                    onSelect = onStatusChange,
                )
            }
            Text(
                "${todayCount}x keluar",
                color = if (todayCount > 0) Accent else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (todayCount > 0) FontWeight.SemiBold else FontWeight.Normal,
            )
        }

        if ((scooter.status == ScooterStatus.MAINTENANCE || scooter.status == ScooterStatus.RUSAK) && !scooter.maintenanceNote.isNullOrBlank()) {
            Text(
                "Catatan: ${scooter.maintenanceNote}",
                color = TextMuted,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val CircleShapeCompat = RoundedCornerShape(50)

// ── Status change dialog ───────────────────────────────────
@Composable
private fun StatusChangeDialog(
    scooter: Scooter,
    newStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (location: String, issue: String, note: String) -> Unit,
) {
    var location by remember { mutableStateOf("outlet") }
    var issue by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val title = when (newStatus) {
        ScooterStatus.MAINTENANCE -> "Mulai Maintenance"
        ScooterStatus.RUSAK -> "Catatan Kerusakan"
        else -> "Ubah Status"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        titleContentColor = TextPrimary,
        textContentColor = TextMuted,
        title = { Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (newStatus == ScooterStatus.MAINTENANCE) {
                    Text("Catat lokasi dan kendala untuk unit ${scooter.id} agar tim perbaikan dapat bertindak.", fontSize = 13.sp)
                    SimpleDropdown(
                        label = if (location == "outlet") "Di Outlet" else "Keluar / Luar",
                        options = listOf("outlet" to "Di Outlet", "luar" to "Keluar / Luar"),
                        selected = location,
                        onSelect = { location = it },
                    )
                    OutlinedTextField(
                        value = issue,
                        onValueChange = { issue = it },
                        label = { Text("Kendala", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: Tidak menyala", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan (opsional)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                } else if (newStatus == ScooterStatus.RUSAK) {
                    Text("Masukkan catatan kerusakan untuk unit ${scooter.id} (opsional):", fontSize = 13.sp)
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Contoh: Tidak menyala, baterai drop", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text("Ubah status unit ${scooter.id} menjadi ${StatusLabels.of(newStatus)}?", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(location, issue.trim(), note.trim()) }) {
                Text("Simpan", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted, fontSize = 12.sp) }
        },
    )
}

// ── Small helpers ──────────────────────────────────────────
@Composable
private fun androidx.compose.foundation.layout.RowScope.ManageActionButton(
    text: String,
    icon: @Composable (Color) -> Unit,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        icon(Accent)
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodySmall,
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(
                    text = { Text(text, fontSize = 12.sp) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun sortLabel(sortBy: String): String = when (sortBy) {
    "id-asc" -> "Urutkan: ID (A-Z)"
    "id-desc" -> "Urutkan: ID (Z-A)"
    "today-checkout" -> "Urutkan: Keluar Hari Ini (Terbanyak)"
    "status" -> "Urutkan: Status"
    "type" -> "Urutkan: Jenis"
    else -> "Urutkan: ID (A-Z)"
}

private fun compareScooters(sortBy: String, todayCount: (String) -> Int): Comparator<Scooter> = when (sortBy) {
    "id-desc" -> compareByDescending<Scooter> { it.id.numericPart() }.thenByDescending { it.id }
    "today-checkout" -> compareByDescending<Scooter> { todayCount(it.id) }
    "status" -> compareBy<Scooter> { com.evrenhouse.trackscooter.util.StatusOrder.ALL[it.status] ?: 99 }.thenBy { it.id.numericPart() }
    "type" -> compareBy<Scooter> { it.type }.thenBy { it.id.numericPart() }
    else -> compareBy<Scooter> { it.id.numericPart() }.thenBy { it.id }
}

private fun String.numericPart(): Int = this.filter(Char::isDigit).toIntOrNull() ?: 0

private fun buildConditionsCsv(scooters: List<Scooter>): String {
    val header = listOf(
        "ID Unit", "Jenis", "Status", "Kondisi Unit", "Spakbor", "Lampu", "Baterai",
        "Jenis Error", "Rem", "Ban", "Dicek",
    )
    val rows = scooters
        .sortedBy { it.id.numericPart() }
        .map { s ->
            val dc = s.deviceCondition
            val monitorLabel = if (dc?.monitor == "lain") {
                dc.monitorDetail ?: "Lain-lain"
            } else {
                com.evrenhouse.trackscooter.util.DeviceLabels.monitor[dc?.monitor] ?: dc?.monitor ?: "Belum dicek"
            }
            listOf(
                s.id,
                TypeLabels.of(s.type),
                StatusLabels.of(s.status),
                DeviceConditionHelper.summarize(dc),
                com.evrenhouse.trackscooter.util.DeviceLabels.setelan[dc?.setelan] ?: dc?.setelan ?: "Belum dicek",
                com.evrenhouse.trackscooter.util.DeviceLabels.lampu[dc?.lampu] ?: dc?.lampu ?: "Belum dicek",
                com.evrenhouse.trackscooter.util.DeviceLabels.baterai[dc?.baterai] ?: dc?.baterai ?: "Belum dicek",
                if (dc?.monitor != null) monitorLabel else "Belum dicek",
                com.evrenhouse.trackscooter.util.DeviceLabels.rem[dc?.rem] ?: dc?.rem ?: "Belum dicek",
                com.evrenhouse.trackscooter.util.DeviceLabels.ban[dc?.ban] ?: dc?.ban ?: "Belum dicek",
                dc?.updatedAt?.let { DateUtils.formatFull(it) } ?: "-",
            )
        }
    return Exporter.toCsv(header, rows)
}

private fun android.graphics.Bitmap.toPngBytes(): ByteArray {
    val baos = java.io.ByteArrayOutputStream()
    compress(android.graphics.Bitmap.CompressFormat.PNG, 100, baos)
    return baos.toByteArray()
}
