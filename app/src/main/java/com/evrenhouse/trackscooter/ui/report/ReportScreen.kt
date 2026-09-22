package com.evrenhouse.trackscooter.ui.report

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle2
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.AttendanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TypeBadge
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
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class RentalSessionItem(
    val no: Int,
    val scooterId: String,
    val type: String,
    val startTs: Date,
    val endTs: Date?,
    val durationText: String,
    val inProgress: Boolean
)

@Composable
fun ReportScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: ((String) -> Unit)? = null
) {
    val data by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { dateFormat.format(Date()) }
    val yesterdayStr = remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        dateFormat.format(cal.time)
    }

    var selectedDate by rememberSaveable { mutableStateOf(todayStr) }
    var selectedOutlet by rememberSaveable { mutableStateOf("all") }
    var attendanceRecords by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }
    var loadingAttendance by remember { mutableStateOf(false) }
    var attendanceFilter by rememberSaveable { mutableStateOf("all") } // "all" | "unattended" | "attended"
    var attendanceSearch by rememberSaveable { mutableStateOf("") }

    // Load attendance for selected date
    fun loadAttendanceData() {
        scope.launch {
            loadingAttendance = true
            runCatching { viewModel.repository.getDailyAttendance(selectedDate) }
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

    // Section 1: Calculate Rental Sessions for selected date
    val sessionItems by remember(data.activityLog, data.scooters, selectedDate, selectedOutlet) {
        derivedStateOf {
            val perUnit = mutableMapOf<String, MutableList<ActivityLogEntry>>()
            data.activityLog.forEach { e ->
                perUnit.getOrPut(e.scooterId) { mutableListOf() }.add(e)
            }

            val result = mutableListOf<RentalSessionItem>()
            var counter = 1

            perUnit.forEach { (scooterId, logs) ->
                val bike = data.scooters.find { it.id == scooterId }
                val bikeOutlet = bike?.currentOutlet ?: Outlets.getHomeOutletForType(bike?.type ?: "sd")
                if (selectedOutlet != "all" && bikeOutlet != selectedOutlet) return@forEach

                val sortedLogs = logs.mapNotNull {
                    val dt = DateUtils.parse(it.timestamp)
                    if (dt != null) it to dt else null
                }.sortedBy { it.second }

                var openCheckout: Pair<ActivityLogEntry, Date>? = null
                for (entry in sortedLogs) {
                    val (log, dt) = entry
                    val logDateKey = dateFormat.format(dt)

                    if (log.action == "checkout") {
                        openCheckout = log to dt
                    } else if (log.action == "return" && openCheckout != null) {
                        val (_, startDt) = openCheckout
                        if (dateFormat.format(startDt) == selectedDate) {
                            val diffSecs = (dt.time - startDt.time) / 1000
                            result.add(
                                RentalSessionItem(
                                    no = counter++,
                                    scooterId = scooterId,
                                    type = bike?.type ?: "sd",
                                    startTs = startDt,
                                    endTs = dt,
                                    durationText = DateUtils.formatDuration(diffSecs),
                                    inProgress = false
                                )
                            )
                        }
                        openCheckout = null
                    }
                }

                // Check active ongoing checkout today
                if (openCheckout != null) {
                    val (_, startDt) = openCheckout
                    if (dateFormat.format(startDt) == selectedDate) {
                        val diffSecs = (System.currentTimeMillis() - startDt.time) / 1000
                        result.add(
                            RentalSessionItem(
                                no = counter++,
                                scooterId = scooterId,
                                type = bike?.type ?: "sd",
                                startTs = startDt,
                                endTs = null,
                                durationText = DateUtils.formatDuration(diffSecs),
                                inProgress = true
                            )
                        )
                    }
                }
            }

            result.sortedByDescending { it.startTs }
        }
    }

    // Section 2: Filtered Attendance Units
    val filteredAttendanceUnits by remember(expectedInOutlet, attendanceSearch, attendanceFilter, attendanceMap) {
        derivedStateOf {
            expectedInOutlet
                .filter { s ->
                    val matchSearch = attendanceSearch.isBlank() || s.id.contains(attendanceSearch, ignoreCase = true)
                    val matchFilter = when (attendanceFilter) {
                        "attended" -> attendanceMap.containsKey(s.id)
                        "unattended" -> !attendanceMap.containsKey(s.id)
                        else -> true
                    }
                    matchSearch && matchFilter
                }
                .sortedWith(compareBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 9999 })
        }
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale("id", "ID")) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Header & Filter Controls ──
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text("Laporan", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Rekapitulasi perputaran sesi sewa dan verifikasi fisik armada", color = TextMuted, fontSize = 12.sp)
                }

                // Controls Row: Quick Dates + Calendar Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedDate == todayStr) Accent else Surface)
                                .border(1.dp, if (selectedDate == todayStr) Accent else Border, RoundedCornerShape(8.dp))
                                .clickable { selectedDate = todayStr }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Hari Ini",
                                color = if (selectedDate == todayStr) Color.White else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedDate == yesterdayStr) Accent else Surface)
                                .border(1.dp, if (selectedDate == yesterdayStr) Accent else Border, RoundedCornerShape(8.dp))
                                .clickable { selectedDate = yesterdayStr }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Kemarin",
                                color = if (selectedDate == yesterdayStr) Color.White else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Calendar Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                            .clickable {
                                val cal = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        val picked = Calendar.getInstance().apply {
                                            set(year, month, day)
                                        }
                                        selectedDate = dateFormat.format(picked.time)
                                    },
                                    cal.get(Calendar.YEAR),
                                    cal.get(Calendar.MONTH),
                                    cal.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, null, tint = Accent, modifier = Modifier.size(14.dp))
                        Text(selectedDate, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                // Global Outlet Dropdown Filter
                OutletDropdown(
                    selectedOutletId = selectedOutlet,
                    onOutletSelected = { selectedOutlet = it },
                    labelPrefix = "Filter Outlet:"
                )
            }
        }

        // ── 4 KPI Status Cards ──
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatCard(
                        title = "Unit Ready",
                        value = "${outletScooters.count { it.status == ScooterStatus.AVAILABLE }}",
                        sub = "Siap Sewa",
                        color = Green,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "Unit Diluar",
                        value = "${outletScooters.count { it.status == ScooterStatus.IN_USE }}",
                        sub = "Sedang Sewa",
                        color = Accent,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatCard(
                        title = "Unit Kendala",
                        value = "${outletScooters.count { it.status == ScooterStatus.MAINTENANCE || it.status == ScooterStatus.RUSAK }}",
                        sub = "Perbaikan",
                        color = Warning,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatCard(
                        title = "Progres Absen",
                        value = "$progressPercent%",
                        sub = "$attendedCount/${expectedInOutlet.size} Unit",
                        color = if (progressPercent == 100) Green else Accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Section 1: Rekapitulasi Sesi Sewa ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.dp, Color.Transparent)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Timer, null, tint = Accent, modifier = Modifier.size(16.dp))
                        Text("Rekapitulasi Sesi Sewa", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(
                        modifier = Modifier
                            .background(Surface3, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("${sessionItems.size} Sesi", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (sessionItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.ElectricScooter, null, tint = TextSubtle, modifier = Modifier.size(28.dp))
                            Text("Tidak ada sesi sewa pada tanggal ini", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sessionItems.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Surface2)
                                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.scooterId,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    TypeBadge(type = item.type)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(Icons.Filled.ArrowUpward, null, tint = Accent, modifier = Modifier.size(11.dp))
                                        Text(timeFormat.format(item.startTs), color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        if (item.endTs != null) {
                                            Text(" - ", color = TextSubtle, fontSize = 11.sp)
                                            Icon(Icons.Filled.ArrowDownward, null, tint = Green, modifier = Modifier.size(11.dp))
                                            Text(timeFormat.format(item.endTs), color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.durationText,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (item.inProgress) Warning.copy(alpha = 0.15f) else Green.copy(alpha = 0.15f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (item.inProgress) "Berjalan" else "Selesai",
                                            color = if (item.inProgress) Warning else Green,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Section 2: Checklist Kehadiran Fisik Outlet ──
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(16.dp))
            ) {
                // Title & Mass Actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                .background(Green.copy(alpha = 0.12f))
                                .border(1.dp, Green.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable {
                                    sweetAlert.showConfirm(
                                        title = "Tandai Semua Hadir?",
                                        message = "Seluruh unit di outlet akan ditandai hadir pada $selectedDate.",
                                        confirmText = "Ya, Tandai",
                                        cancelText = "Batal",
                                        isDanger = false,
                                        onConfirm = {
                                            scope.launch {
                                                runCatching {
                                                    viewModel.repository.markAllDailyAttendance(
                                                        date = selectedDate,
                                                        outlet = if (selectedOutlet == "all") null else selectedOutlet
                                                    )
                                                }.onSuccess {
                                                    sweetAlert.showSuccess(it.message ?: "Semua hadir")
                                                    loadAttendanceData()
                                                }
                                            }
                                        }
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.DoneAll, null, tint = Green, modifier = Modifier.size(13.dp))
                                Text("Semua Hadir", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

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
                                                    viewModel.repository.resetDailyAttendance(
                                                        date = selectedDate,
                                                        outlet = if (selectedOutlet == "all") null else selectedOutlet
                                                    )
                                                }.onSuccess {
                                                    sweetAlert.showSuccess(it.message ?: "Absen direset")
                                                    loadAttendanceData()
                                                }
                                            }
                                        }
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = attendanceSearch,
                        onValueChange = { attendanceSearch = it },
                        placeholder = { Text("Cari ID unit...", color = TextSubtle, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextMuted, modifier = Modifier.size(15.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AttendanceFilterChip(
                            label = "Semua (${expectedInOutlet.size})",
                            selected = attendanceFilter == "all",
                            onClick = { attendanceFilter = "all" },
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceFilterChip(
                            label = "Belum ($unattendedCount)",
                            selected = attendanceFilter == "unattended",
                            onClick = { attendanceFilter = "unattended" },
                            activeColor = Warning,
                            modifier = Modifier.weight(1f)
                        )
                        AttendanceFilterChip(
                            label = "Hadir ($attendedCount)",
                            selected = attendanceFilter == "attended",
                            onClick = { attendanceFilter = "attended" },
                            activeColor = Green,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Unit List
                if (filteredAttendanceUnits.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Tidak ada unit yang cocok", color = TextMuted, fontSize = 12.sp)
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                                viewModel.repository.recordDailyAttendance(
                                                    scooterId = scooter.id,
                                                    date = selectedDate,
                                                    outlet = if (selectedOutlet == "all") null else selectedOutlet
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
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (isAttended) Green.copy(alpha = 0.15f) else Surface3,
                                                RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isAttended) Icons.Filled.CheckCircle2 else Icons.Filled.Store,
                                            contentDescription = null,
                                            tint = if (isAttended) Green else TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = scooter.id,
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            TypeBadge(type = scooter.type)
                                        }
                                        Text(
                                            text = if (isAttended && record?.scannedAt != null) {
                                                val scanDt = DateUtils.parse(record.scannedAt)
                                                if (scanDt != null) "Diabsen: ${timeFormat.format(scanDt)}" else "Sudah diabsen"
                                            } else "Belum diabsen hadir",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isAttended) Green.copy(alpha = 0.15f) else Warning.copy(alpha = 0.15f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isAttended) "Hadir" else "Belum",
                                        color = if (isAttended) Green else Warning,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReportStatCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Baseline
        ) {
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
            Text(sub, color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AttendanceFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Accent
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) activeColor else Surface)
            .border(1.dp, if (selected) activeColor else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
