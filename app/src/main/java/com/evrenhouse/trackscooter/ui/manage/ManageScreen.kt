package com.evrenhouse.trackscooter.ui.manage
import com.evrenhouse.trackscooter.BuildConfig

import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material.icons.filled.SystemUpdate
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import com.evrenhouse.trackscooter.util.toPngBytes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.tooling.preview.Preview
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import com.evrenhouse.trackscooter.ui.theme.Bg
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.UpdateScooterRequest
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.ManageSkeleton
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.util.Outlets
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
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.MIME_XLSX
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

@OptIn(ExperimentalFoundationApi::class)
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

    val todayKey = remember { DateUtils.localDateKey(DateUtils.today()) }
    val todayCheckoutCounts by remember(data.activityLog, todayKey) {
        derivedStateOf {
            val counts = HashMap<String, Int>(data.scooters.size.coerceAtLeast(16))
            for (entry in data.activityLog) {
                if (entry.action == "checkout" && DateUtils.dateKey(entry.timestamp) == todayKey) {
                    val cur = counts[entry.scooterId] ?: 0
                    counts[entry.scooterId] = cur + 1
                }
            }
            counts
        }
    }
    val getTodayCheckoutCount: (String) -> Int = remember(todayCheckoutCounts) {
        { id -> todayCheckoutCounts[id] ?: 0 }
    }

    val filtered by remember(data.scooters, search, filterStatus, filterType, sortBy, activeOutlet) {
        derivedStateOf {
            data.scooters
                .filter { s ->
                    val matchesOutlet = if (search.isNotBlank()) true else (activeOutlet == "all" || (s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)) == activeOutlet)
                    val matchesSearch = s.id.contains(search, ignoreCase = true)
                    val matchesStatus = filterStatus == "all" || s.status == filterStatus
                    val matchesType = filterType == "all" || s.type == filterType
                    matchesOutlet && matchesSearch && matchesStatus && matchesType
                }
                .sortedWith(compareScooters(sortBy, getTodayCheckoutCount))
        }
    }
    val lazyListState = rememberLazyListState()
    var isFabVisible by remember { mutableStateOf(true) }
    var lastFirstVisibleItemIndex by remember { mutableIntStateOf(0) }
    var lastFirstVisibleItemScrollOffset by remember { mutableIntStateOf(0) }

    // Windowed Pagination: batasi render awal 10 item agar scroll super ringan (60-120fps),
    // bertambah +10 item saat mendekati akhir daftar. Pencarian tetap menyaring seluruh data armada.
    var displayLimit by rememberSaveable(search, filterStatus, filterType, sortBy, activeOutlet) {
        mutableIntStateOf(10)
    }
    val displayedScooters by remember(filtered, displayLimit) {
        derivedStateOf {
            filtered.take(displayLimit)
        }
    }

    // Auto-load 10 item berikutnya saat pengguna scroll mendekati ujung daftar
    LaunchedEffect(lazyListState, filtered.size) {
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 2
        }.collect { nearEnd ->
            if (nearEnd && displayLimit < filtered.size) {
                displayLimit = (displayLimit + 10).coerceAtMost(filtered.size)
            }
        }
    }
    // Otomatis sembunyikan FAB saat scroll turun, tampilkan saat scroll naik atau di paling atas
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex to lazyListState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            if (index == 0 && offset < 40) {
                isFabVisible = true
            } else {
                val isScrollingDown = index > lastFirstVisibleItemIndex || (index == lastFirstVisibleItemIndex && offset > lastFirstVisibleItemScrollOffset + 15)
                val isScrollingUp = index < lastFirstVisibleItemIndex || (index == lastFirstVisibleItemIndex && offset < lastFirstVisibleItemScrollOffset - 15)
                if (isScrollingDown) {
                    isFabVisible = false
                } else if (isScrollingUp) {
                    isFabVisible = true
                }
            }
            lastFirstVisibleItemIndex = index
            lastFirstVisibleItemScrollOffset = offset
        }
    }

    var dataMenuExpanded by remember { mutableStateOf(false) }

    val availableCount = remember(data.scooters) { data.scooters.count { it.status == ScooterStatus.AVAILABLE } }
    val inUseCount = remember(data.scooters) { data.scooters.count { it.status == ScooterStatus.IN_USE } }
    val maintenanceCount = remember(data.scooters) { data.scooters.count { it.status == ScooterStatus.MAINTENANCE } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Header: Judul (jumlah unit hanya tampil sekali "170 unit total") & Outlet Dropdown + Aksi Data
            item {
                Column(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("Kelola", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "· ${data.scooters.size} unit total",
                                color = TextSubtle,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutletDropdown(
                            selectedOutletId = activeOutlet,
                            onOutletSelected = { viewModel.setSelectedOutlet(it) },
                            getOutletCount = null, // Hilangkan duplikasi angka, angka total hanya di FilterChip "Semua"
                            modifier = Modifier.weight(1f),
                        )

                        // Compact Data Actions Dropdown
                        Box {
                            OutlinedButton(
                                onClick = { dataMenuExpanded = !dataMenuExpanded },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Surface2,
                                    contentColor = TextPrimary,
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 9.dp),
                            ) {
                                if (busyAction) {
                                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Memproses...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                } else {
                                    Icon(Icons.Filled.Download, contentDescription = "Menu Aksi Data", tint = Accent, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(6.dp))
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
                                            Text("Unduh laporan kondisi (.xlsx)", fontSize = 10.sp, color = TextMuted)
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Filled.GridView, null, tint = Accent, modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        dataMenuExpanded = false
                                        scope.launch {
                                            busyAction = true
                                            runCatching {
                                                val bytes = Exporter.buildConditionsXlsx(data.scooters)
                                                val filename = "Kondisi-Scooter-${todayKey}.xlsx"
                                                Exporter.saveBytesToDownloads(context, filename, bytes, MIME_XLSX)
                                            }
                                                .onSuccess { sweetAlert.showSuccess("File XLSX berhasil diunduh ($it)") }
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
                                                val items = data.scooters.map {
                                                    Triple(it.id, it.type, it.currentOutlet ?: Outlets.getHomeOutletForType(it.type))
                                                }
                                                QrZip.zipAllQrs(context, items)
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

                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text("Cek Pembaruan", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                            Text("Versi v${BuildConfig.VERSION_NAME}", fontSize = 10.sp, color = TextMuted)
                                        }
                                    },
                                    leadingIcon = { Icon(Icons.Filled.SystemUpdate, null, tint = Accent, modifier = Modifier.size(16.dp)) },
                                    onClick = {
                                        dataMenuExpanded = false
                                        viewModel.checkForAppUpdate(forceRecheck = true) { info ->
                                            if (!info.isUpdateAvailable) {
                                                if (info.errorMessage != null) {
                                                    sweetAlert.showError("Gagal memeriksa pembaruan: ${info.errorMessage}")
                                                } else {
                                                    sweetAlert.showSuccess("Aplikasi sudah versi terbaru (v${info.currentVersionName})")
                                                }
                                            }
                                        }
                                    },
                                    enabled = !busyAction,
                                )
                            }
                        }
                    }
                }
            }
            // 2. STICKY HEADER: Search bar ~48dp, tombol filter & urutkan, dan quick status filter chips
            stickyHeader {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ManageSearchBarSection(
                        search = search,
                        onSearch = { search = it },
                        filterStatus = filterStatus,
                        onFilterStatus = { filterStatus = it },
                        filterType = filterType,
                        onFilterType = { filterType = it },
                        sortBy = sortBy,
                        onSortBy = { sortBy = it },
                        totalCount = data.scooters.size,
                        readyCount = availableCount,
                        inUseCount = inUseCount,
                        maintenanceCount = maintenanceCount,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    )
                }
            }

            // 3. DAFTAR BARIS UNIT ATAU SKELETON LOADING (Prioritaskan skeleton loading agar tidak salah tampil error)
            when {
                data.loading && data.scooters.isEmpty() -> {
                    item {
                        ManageSkeleton()
                    }
                }
                data.error != null && data.scooters.isEmpty() -> {
                    item {
                        ErrorState(message = data.error ?: "", onRetry = { viewModel.refresh() })
                    }
                }
                filtered.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("Tidak ada unit scooter yang cocok.", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
                else -> {
                    items(
                        items = displayedScooters,
                        key = { it.id },
                    ) { scooter ->
                        UnitRow(
                            scooter = scooter,
                            todayCount = getTodayCheckoutCount(scooter.id),
                            activeOutlet = activeOutlet,
                            onOpenDetail = { onOpenDetail(scooter.id) },
                            onStatusChange = { newStatus ->
                                statusDialog = StatusDialogData(scooter, newStatus)
                            },
                            onDelete = {
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
                            onDownloadQr = {
                                scope.launch {
                                    runCatching {
                                        val outlet = scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type)
                                        val bmp = QrUtils.generate(scooter.id, outlet, 400)
                                        val png = bmp.toPngBytes()
                                        val filename = "QR-${scooter.id}-${scooter.type.uppercase()}.png"
                                        Exporter.saveBytesToDownloads(context, filename, png, "image/png")
                                    }
                                        .onSuccess { sweetAlert.showSuccess("QR ${scooter.id} diunduh") }
                                        .onFailure { sweetAlert.showError(it.toUserMessage()) }
                                }
                            },
                            onTroubleSwap = { troubleScooter = it },
                            onEditScooter = { editingScooter = scooter },
                            onEditMaintenance = {
                                statusDialog = StatusDialogData(
                                    it,
                                    ScooterStatus.MAINTENANCE,
                                    "Edit Kendala Unit ${it.id}",
                                )
                            },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Border.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                        )
                    }

                    // Footer indikator jika masih ada sisa item yang belum dimuat
                    if (displayLimit < filtered.size) {
                        item(key = "load_more_footer") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { displayLimit = (displayLimit + 10).coerceAtMost(filtered.size) }
                                    .padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                CircularProgressIndicator(
                                    color = Accent,
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                )
                                Text(
                                    text = "Menampilkan ${displayedScooters.size} dari ${filtered.size} unit...",
                                    color = TextSubtle,
                                    fontSize = 11.5.sp,
                                )
                            }
                        }
                    }
                }
            }

            // 4. App Version Info Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "TrackScooter Mobile v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        // Extended Floating Action Button Tambah Unit di pojok kanan bawah
        // Sembunyi sepenuhnya saat scroll turun, tampil saat scroll naik atau di posisi paling atas
        AnimatedVisibility(
            visible = isFabVisible,
            enter = fadeIn(animationSpec = tween(150)) + scaleIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150)) + scaleOut(animationSpec = tween(150)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 76.dp, end = 16.dp),
        ) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Tambah Unit Baru", modifier = Modifier.size(20.dp)) },
                text = { Text("Tambah Unit", fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                containerColor = Accent,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
            )
        }
    }

    // Status change dialog (OPERASIONAL) vs Edit Scooter dialog (MANAJEMEN) di bawah.
    statusDialog?.let { d ->
        StatusChangeDialog(
            scooter = d.scooter,
            newStatus = d.newStatus,
            dialogTitle = d.title,
            onDismiss = { statusDialog = null },
            onConfirm = { location, locationDetail, issue, note ->
                scope.launch {
                    val request = when (d.newStatus) {
                        ScooterStatus.MAINTENANCE -> UpdateScooterRequest(
                            status = ScooterStatus.MAINTENANCE,
                            location = location,
                            // "" agar terkirim (explicitNulls=false omit null) dan server clear saat outlet.
                            locationDetail = locationDetail ?: "",
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
                        if (d.title != null) {
                            sweetAlert.showSuccess("Catatan kendala unit ${d.scooter.id} berhasil diperbarui")
                        } else {
                            sweetAlert.showSuccess("Status ${d.scooter.id} diperbarui")
                        }
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
                        // "" agar terkirim dan server clear saat outlet (explicitNulls=false omit null).
                        locationDetail = if (status == ScooterStatus.MAINTENANCE) (locationDetail ?: "") else null,
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

data class StatusDialogData(val scooter: Scooter, val newStatus: String, val title: String? = null)


// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "Manage Content - Light", showBackground = true)
@Composable
private fun ManageContentLightPreview() {
    val sampleScooters = listOf(
        Scooter("SB-01", "sb", ScooterStatus.AVAILABLE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T10:00:00Z"),
        Scooter("SB-02", "sb", ScooterStatus.IN_USE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T09:30:00Z"),
        Scooter("FZ-05", "fz", ScooterStatus.MAINTENANCE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T08:15:00Z", maintenanceNote = "Rem belakang berdecit"),
        Scooter("EX-10", "ex", ScooterStatus.AVAILABLE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T07:45:00Z"),
        Scooter("SD-03", "sd", ScooterStatus.AVAILABLE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T07:00:00Z"),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.fillMaxSize().background(Bg)) {
            ScooterList(
                scooters = sampleScooters,
                search = "",
                onSearch = {},
                filterStatus = "all",
                onFilterStatus = {},
                filterType = "all",
                onFilterType = {},
                sortBy = "id-asc",
                onSortBy = {},
                getTodayCount = { 2 },
                onOpenDetail = {},
                onStatusChange = { _, _ -> },
                onDelete = {},
                onDownloadQr = {},
            )
        }
    }
}

@Preview(name = "Manage Content - Dark", showBackground = true)
@Composable
private fun ManageContentDarkPreview() {
    val sampleScooters = listOf(
        Scooter("SB-01", "sb", ScooterStatus.AVAILABLE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T10:00:00Z"),
        Scooter("SB-02", "sb", ScooterStatus.IN_USE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T09:30:00Z"),
        Scooter("FZ-05", "fz", ScooterStatus.MAINTENANCE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T08:15:00Z", maintenanceNote = "Baterai drop saat tanjakan"),
        Scooter("EX-10", "ex", ScooterStatus.AVAILABLE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T07:45:00Z"),
        Scooter("SD-03", "sd", ScooterStatus.AVAILABLE, currentOutlet = "utara-motor", lastUpdated = "2026-10-01T07:00:00Z"),
    )
    TrackScooterTheme(isDark = true) {
        Box(modifier = Modifier.fillMaxSize().background(Bg)) {
            ScooterList(
                scooters = sampleScooters,
                search = "",
                onSearch = {},
                filterStatus = "all",
                onFilterStatus = {},
                filterType = "all",
                onFilterType = {},
                sortBy = "id-asc",
                onSortBy = {},
                getTodayCount = { 2 },
                onOpenDetail = {},
                onStatusChange = { _, _ -> },
                onDelete = {},
                onDownloadQr = {},
            )
        }
    }
}

