package com.evrenhouse.trackscooter.ui.report

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.AttendanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterRepository
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.ui.scan.CameraScanner
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ReportSubTab(val label: String, val icon: ImageVector) {
    CLOSING("Closing", Icons.Filled.Checklist),
    SCAN("Scan Absen", Icons.Filled.QrCodeScanner),
}

@Composable
fun ReportScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: ((String) -> Unit)? = null,
    repository: ScooterRepository = repository(),
) {
    val data by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { dateFormat.format(Date()) }

    var selectedDate by rememberSaveable { mutableStateOf(todayStr) }
    val selectedOutlet by viewModel.selectedOutlet.collectAsState()
    var attendanceRecords by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var loadingAttendance by remember { mutableStateOf(false) }
    var attendanceFilter by rememberSaveable { mutableStateOf("all") } // "all" | "unattended" | "attended"
    var attendanceSearch by rememberSaveable { mutableStateOf("") }

    val pagerState = rememberPagerState(initialPage = 0) { ReportSubTab.entries.size }
    val currentSubTab by remember { derivedStateOf { ReportSubTab.entries[pagerState.currentPage] } }

    // Load attendance for selected date
    fun loadAttendanceData() {
        scope.launch {
            loadingAttendance = true
            runCatching { repository.getDailyAttendance(selectedDate) }
                .onSuccess { attendanceRecords = it }
                .onFailure { attendanceRecords = emptyList() }
            loadingAttendance = false
        }
    }

    LaunchedEffect(selectedDate) {
        loadAttendanceData()
    }

    // Attendance map
    val attendanceMap by remember(attendanceRecords) {
        derivedStateOf {
            attendanceRecords.associateBy { it.scooterId }
        }
    }

    // Filter scooters by global outlet
    val outletScooters by remember(data.scooters, selectedOutlet) {
        derivedStateOf {
            if (selectedOutlet == "all") data.scooters
            else data.scooters.filter {
                (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == selectedOutlet
            }
        }
    }

    // Units expected in outlet (not in-use)
    val expectedInOutlet by remember(outletScooters) {
        derivedStateOf {
            outletScooters.filter { it.status != ScooterStatus.IN_USE }
        }
    }

    val attendedCount by remember(expectedInOutlet, attendanceMap) {
        derivedStateOf {
            expectedInOutlet.count { attendanceMap.containsKey(it.id) }
        }
    }

    val unattendedCount by remember(expectedInOutlet, attendedCount) {
        derivedStateOf {
            maxOf(0, expectedInOutlet.size - attendedCount)
        }
    }

    val progressPercent by remember(expectedInOutlet, attendedCount) {
        derivedStateOf {
            if (expectedInOutlet.isNotEmpty()) (attendedCount * 100) / expectedInOutlet.size else 100
        }
    }

    // Filtered Attendance Units
    val filteredAttendanceUnits by remember(expectedInOutlet, attendanceSearch, attendanceFilter, attendanceMap) {
        derivedStateOf {
            val baseUnits = if (attendanceSearch.isNotBlank()) data.scooters.filter { it.status != ScooterStatus.IN_USE } else expectedInOutlet
            baseUnits
                .filter { s ->
                    val matchSearch = attendanceSearch.isBlank() || s.id.contains(attendanceSearch, ignoreCase = true)
                    val matchFilter = when (attendanceFilter) {
                        "attended" -> attendanceMap.containsKey(s.id)
                        "unattended" -> !attendanceMap.containsKey(s.id)
                        else -> true
                    }
                    matchSearch && matchFilter
                }
                .sortedWith(compareBy<Scooter> { it.id.filter { ch -> !ch.isDigit() } }.thenBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 })
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // ── Header ──
        Text("Laporan", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        // ── 2 Sub Tab Selector (Closing & Scan Absen) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface2, RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ReportSubTab.entries.forEach { tab ->
                val isSelected = currentSubTab == tab
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Accent else Color.Transparent)
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (tab == ReportSubTab.CLOSING) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.25f)
                                    else if (progressPercent == 100) Green.copy(alpha = 0.2f) else Accent.copy(alpha = 0.2f),
                                    CircleShape,
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = "$attendedCount/${expectedInOutlet.size}",
                                color = if (isSelected) Color.White else if (progressPercent == 100) Green else Accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // ── Swipeable Pager for Sub Tabs ──
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (ReportSubTab.entries[page]) {
                // ══════════════════════════════════════════════════════════
                // SUB TAB 1: CLOSING (CHECKLIST KEHADIRAN OUTLET)
                // ══════════════════════════════════════════════════════════
                ReportSubTab.CLOSING -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        // ── Filter Outlet & Date Picker (Sejajar) ──
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutletDropdown(
                                selectedOutletId = selectedOutlet,
                                onOutletSelected = { viewModel.setSelectedOutlet(it) },
                                labelPrefix = "Filter Outlet:",
                                modifier = Modifier.weight(1f),
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Surface2)
                                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val cal = Calendar.getInstance()
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                val formatted = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                                                selectedDate = formatted
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH),
                                        ).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(Icons.Filled.CalendarMonth, null, tint = Accent, modifier = Modifier.size(16.dp))
                                Text(
                                    text = selectedDate,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }

                        // ── 4 KPI Status Cards ──
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ReportStatCard(
                                    title = "Unit Ready",
                                    value = "${outletScooters.count { it.status == ScooterStatus.AVAILABLE }}",
                                    sub = "Siap Sewa",
                                    color = Green,
                                    modifier = Modifier.weight(1f),
                                )
                                ReportStatCard(
                                    title = "Unit Diluar",
                                    value = "${outletScooters.count { it.status == ScooterStatus.IN_USE }}",
                                    sub = "Sedang Sewa",
                                    color = Accent,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                ReportStatCard(
                                    title = "Unit Kendala",
                                    value = "${outletScooters.count { it.status == ScooterStatus.MAINTENANCE }}",
                                    sub = "Perbaikan",
                                    color = Warning,
                                    modifier = Modifier.weight(1f),
                                )
                                ReportStatCard(
                                    title = "Progres Absen",
                                    value = "$progressPercent%",
                                    sub = "$attendedCount/${expectedInOutlet.size} Unit",
                                    color = if (progressPercent == 100) Green else Accent,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        // ── Checklist Kehadiran Box ──
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Surface)
                                .border(1.dp, Border, RoundedCornerShape(16.dp)),
                        ) {
                        // Title & Reset Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Icon(Icons.Filled.Store, null, tint = Accent, modifier = Modifier.size(16.dp))
                                    Text("Checklist Kehadiran Outlet", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("$attendedCount / ${expectedInOutlet.size} Unit Hadir", color = TextMuted, fontSize = 11.sp)
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Surface3)
                                        .border(1.dp, Border, RoundedCornerShape(8.dp))
                                        .clickable {
                                            sweetAlert.showConfirm(
                                                title = "Reset Absen?",
                                                message = "Catatan kehadiran unit untuk tanggal $selectedDate akan dihapus.",
                                                confirmText = "Ya, Reset",
                                                cancelText = "Batal",
                                                isDanger = true,
                                                onConfirm = {
                                                    scope.launch {
                                                        runCatching {
                                                            repository.resetDailyAttendance(
                                                                date = selectedDate,
                                                                outlet = if (selectedOutlet == "all") null else selectedOutlet,
                                                            )
                                                        }.onSuccess {
                                                            sweetAlert.showSuccess(it.message ?: "Absen direset")
                                                            loadAttendanceData()
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(Icons.Filled.RestartAlt, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                        Text("Reset", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        // Filter & Search bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface2)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            OutlinedTextField(
                                value = attendanceSearch,
                                onValueChange = { attendanceSearch = it.uppercase() },
                                placeholder = { Text("Cari ID unit...", color = TextSubtle, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextMuted, modifier = Modifier.size(15.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                AttendanceFilterChip(
                                    label = "Semua (${expectedInOutlet.size})",
                                    selected = attendanceFilter == "all",
                                    onClick = { attendanceFilter = "all" },
                                    modifier = Modifier.weight(1f),
                                )
                                AttendanceFilterChip(
                                    label = "Belum ($unattendedCount)",
                                    selected = attendanceFilter == "unattended",
                                    onClick = { attendanceFilter = "unattended" },
                                    activeColor = Warning,
                                    modifier = Modifier.weight(1f),
                                )
                                AttendanceFilterChip(
                                    label = "Hadir ($attendedCount)",
                                    selected = attendanceFilter == "attended",
                                    onClick = { attendanceFilter = "attended" },
                                    activeColor = Green,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }

                        // Unit List
                        if (filteredAttendanceUnits.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text("Tidak ada unit yang cocok", color = TextMuted, fontSize = 12.sp)
                            }
                        } else {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                filteredAttendanceUnits.forEach { scooter ->
                                    val record = attendanceMap[scooter.id]
                                    val isAttended = record != null

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isAttended) Green.copy(alpha = 0.05f) else Surface2)
                                            .border(1.dp, if (isAttended) Green.copy(alpha = 0.3f) else Border, RoundedCornerShape(12.dp))
                                            .clickable {
                                                scope.launch {
                                                    runCatching {
                                                        repository.recordDailyAttendance(
                                                            scooterId = scooter.id,
                                                            date = selectedDate,
                                                            outlet = if (selectedOutlet == "all") null else selectedOutlet,
                                                        )
                                                    }.onSuccess {
                                                        sweetAlert.showSuccess(it.message ?: "Unit ${scooter.id} diabsen")
                                                        loadAttendanceData()
                                                        viewModel.refresh()
                                                    }.onFailure {
                                                        sweetAlert.showError(it.toUserMessage())
                                                    }
                                                }
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(
                                                        if (isAttended) Green.copy(alpha = 0.15f) else Surface3,
                                                        RoundedCornerShape(8.dp),
                                                    ),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Icon(
                                                    imageVector = if (isAttended) Icons.Filled.CheckCircle else Icons.Filled.Store,
                                                    contentDescription = null,
                                                    tint = if (isAttended) Green else TextMuted,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            }

                                            Column {
                                                val isDark = LocalThemeIsDark.current
                                                val nameColor = ScooterColors.getScooterNameColor(scooter.type, scooter.id, scooter.currentOutlet, isDark)
                                                Text(
                                                    text = scooter.id,
                                                    color = nameColor,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                )
                                                Text(
                                                    text = if (isAttended && record?.scannedAt != null) {
                                                        val scanDt = DateUtils.parse(record.scannedAt)
                                                        if (scanDt != null) "Diabsen: ${DateUtils.formatTime(scanDt)}" else "Sudah diabsen"
                                                    } else "Belum diabsen hadir",
                                                    color = TextMuted,
                                                    fontSize = 11.sp,
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isAttended) Green.copy(alpha = 0.15f) else Warning.copy(alpha = 0.15f),
                                                    RoundedCornerShape(6.dp),
                                                )
                                                .padding(horizontal = 8.dp, vertical = 3.dp),
                                        ) {
                                            Text(
                                                text = if (isAttended) "Hadir" else "Belum",
                                                color = if (isAttended) Green else Warning,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }

            // ══════════════════════════════════════════════════════════
            // SUB TAB 2: SCAN ABSEN (KAMERA SCANNER ABSENSI SAJA)
            // ══════════════════════════════════════════════════════════
            ReportSubTab.SCAN -> {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    var isScanningAttendance by remember { mutableStateOf(false) }

                    CameraScanner(
                        isProcessing = isScanningAttendance,
                        onScan = { rawCode ->
                            val scannedId = rawCode.trim().uppercase()
                            if (!isScanningAttendance && scannedId.isNotBlank()) {
                                isScanningAttendance = true
                                scope.launch {
                                    runCatching {
                                        repository.recordDailyAttendance(
                                            scooterId = scannedId,
                                            date = selectedDate,
                                            outlet = if (selectedOutlet == "all") null else selectedOutlet,
                                        )
                                    }.onSuccess { res ->
                                        sweetAlert.showSuccess(res.message ?: "Unit $scannedId berhasil diabsen!")
                                        loadAttendanceData()
                                        viewModel.refresh()
                                    }.onFailure { err ->
                                        sweetAlert.showError(err.toUserMessage())
                                    }
                                    isScanningAttendance = false
                                }
                            }
                        },
                        onError = { sweetAlert.showError(it) },
                    )

                    ScanAttendanceRecentLogCard(records = attendanceRecords)
                }
            }
        }
    }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ReportStatCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(sub, color = TextMuted, fontSize = 11.sp)
    }
}

@Composable
private fun AttendanceFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Accent,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) activeColor else Surface3)
            .border(1.dp, if (selected) activeColor else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextMuted,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

@Composable
fun ScanAttendanceRecentLogCard(
    records: List<AttendanceRecord>,
    modifier: Modifier = Modifier,
) {
    var showAll by remember { mutableStateOf(false) }

    val recentRecords = remember(records) {
        records.sortedByDescending { it.scannedAt }
    }

    val displayedRecords = if (showAll) recentRecords else recentRecords.take(2)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Filled.History, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp))
                Text("LOG RECENT ABSEN", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            if (recentRecords.size > 2) {
                Text(
                    text = if (showAll) "Tutup" else "Lihat semua (${recentRecords.size})",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAll = !showAll },
                )
            }
        }

        if (recentRecords.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada unit yang diabsen.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            displayedRecords.forEachIndexed { index, record ->
                if (index > 0) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
                }
                val timeStr = remember(record.scannedAt) {
                    val dt = DateUtils.parse(record.scannedAt)
                    if (dt != null) DateUtils.formatTime(dt) else record.scannedAt
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Green.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = "Hadir",
                                color = Green,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Text(
                            text = record.scooterId,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                        )
                    }

                    Text(
                        text = timeStr,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}
