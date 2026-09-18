package com.evrenhouse.trackscooter.ui.monitor

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.dashboard.ScooterCard
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.BlueLive
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Exporter
import com.evrenhouse.trackscooter.util.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

private val NullableLocalDateSaver = Saver<LocalDate?, String>(
    save = { it?.toString() ?: "" },
    restore = { if (it.isEmpty()) null else LocalDate.parse(it) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(MonitorTab.LIVE_SESSION) }
    var selectedDate by rememberSaveable(stateSaver = NullableLocalDateSaver) { mutableStateOf<LocalDate?>(null) } // null = live today
    var statusFilter by rememberSaveable { mutableStateOf("all") }
    var typeFilter by rememberSaveable { mutableStateOf("all") }
    var showDatePicker by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }

    // Ticker to live-update rental duration without seconds every 5 seconds
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            nowMillis = System.currentTimeMillis()
        }
    }

    val today = remember { DateUtils.today() }

    // Unique dates from the log, newest first
    val availableDates by remember {
        derivedStateOf {
            state.activityLog.mapNotNull { DateUtils.toLocalDate(it.timestamp) }.distinct().sortedDescending()
        }
    }

    val activeDate = selectedDate ?: today
    val isLiveView = selectedDate == null || activeDate == today

    val logForDate by remember {
        derivedStateOf {
            state.activityLog.filter { DateUtils.toLocalDate(it.timestamp) == activeDate }
        }
    }
    val checkoutCount by remember {
        derivedStateOf {
            logForDate.count { it.action == ActionLabels.CHECKOUT }
        }
    }
    val returnCount by remember {
        derivedStateOf {
            logForDate.count { it.action == ActionLabels.RETURN }
        }
    }

    // In-use scooters for Live Session tab
    val inUseScooters by remember {
        derivedStateOf {
            state.scooters.filter { it.status == "in-use" }
        }
    }

    val filteredScooters by remember {
        derivedStateOf {
            state.scooters.filter { s ->
                (statusFilter == "all" || s.status == statusFilter) &&
                    (typeFilter == "all" || s.type == typeFilter)
            }
        }
    }

    val currentIdx by remember {
        derivedStateOf { availableDates.indexOfFirst { it == activeDate } }
    }
    val canGoPrev by remember {
        derivedStateOf { currentIdx in 0 until availableDates.size - 1 }
    }

    val displayDates by remember {
        derivedStateOf {
            availableDates.filter { it != today }.take(7)
        }
    }

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Top Header with Live Pulse ──
            item {
                LivePulseHeader(
                    isLiveView = isLiveView,
                    isLiveConnected = state.isLiveConnected,
                    isReconnecting = state.isReconnecting,
                    activeDate = activeDate,
                )
            }

            // ── 3-Tab Selector ──
            item {
                MonitorTabSelector(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                    liveCount = inUseScooters.size,
                    recentCount = logForDate.size,
                )
            }

            when {
                state.error != null && state.scooters.isEmpty() -> {
                    item {
                        ErrorState(message = state.error ?: "", onRetry = { viewModel.refresh() })
                    }
                }
                state.loading && state.scooters.isEmpty() -> {
                    item {
                        LoadingState("Memuat data monitor...")
                    }
                }
                else -> {
                    // ── Tab 1: LIVE SESSION ──
                    if (selectedTab == MonitorTab.LIVE_SESSION) {
                        if (inUseScooters.isEmpty()) {
                            item {
                                LiveSessionEmptyState()
                            }
                        } else {
                            item {
                                Text(
                                    text = "SESI AKTIF BERJALAN (${inUseScooters.size} UNIT)",
                                    color = TextSubtle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                )
                            }
                            items(
                                items = inUseScooters,
                                key = { "live_${it.id}" },
                            ) { scooter ->
                                LiveSessionCard(
                                    scooter = scooter,
                                    nowMillis = nowMillis,
                                    onClick = { onOpenDetail(scooter.id) },
                                )
                            }
                        }
                    }

                    // ── Tab 2: RECENT (Aktivitas Terkini) ──
                    if (selectedTab == MonitorTab.RECENT) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = if (isLiveView) "AKTIVITAS TERKINI HARI INI" else "RIWAYAT AKTIVITAS",
                                    color = TextSubtle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.1.sp,
                                )
                                Text(
                                    text = "${logForDate.size} transaksi",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                )
                            }
                        }

                        if (logForDate.isEmpty()) {
                            item {
                                Text(
                                    text = if (isLiveView) "Belum ada aktivitas hari ini." else "Tidak ada aktivitas pada tanggal ini.",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Surface, RoundedCornerShape(14.dp))
                                        .border(1.dp, Border, RoundedCornerShape(14.dp))
                                        .padding(28.dp),
                                )
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Surface, RoundedCornerShape(14.dp))
                                        .border(1.dp, Border, RoundedCornerShape(14.dp)),
                                ) {
                                    Column {
                                        logForDate.forEachIndexed { index, entry ->
                                            ActivityItemRow(entry = entry, isLiveView = isLiveView)
                                            if (index < logForDate.size - 1) {
                                                Box(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .height(1.dp)
                                                        .background(Border)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Tab 3: SUMMARY (Date Picker, Total Transaksi, Status Unit) ──
                    if (selectedTab == MonitorTab.SUMMARY) {
                        // Date Navigator
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Surface, RoundedCornerShape(14.dp))
                                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                // Row 1: Horizontal scrollable date pills
                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    item(key = "date_live") {
                                        DatePill(
                                            label = "Hari Ini (Live)",
                                            active = isLiveView,
                                            live = true,
                                            onClick = { selectedDate = null },
                                        )
                                    }
                                    items(
                                        items = displayDates,
                                        key = { it.toString() },
                                    ) { d ->
                                        DatePill(
                                            label = DateUtils.formatPill(d),
                                            active = selectedDate == d,
                                            onClick = { selectedDate = d },
                                        )
                                    }
                                }

                                // Row 2: Controls & Export Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        IconButton(onClick = {
                                            when {
                                                currentIdx == -1 && availableDates.isNotEmpty() -> selectedDate = availableDates.first()
                                                canGoPrev -> selectedDate = availableDates[currentIdx + 1]
                                            }
                                        }, enabled = currentIdx == -1 || canGoPrev) {
                                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Sebelumnya", tint = TextMuted, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(Icons.Filled.CalendarMonth, contentDescription = "Pilih tanggal", tint = Accent, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(onClick = {
                                            if (currentIdx > 0) selectedDate = availableDates[currentIdx - 1] else selectedDate = null
                                        }, enabled = !isLiveView) {
                                            Icon(Icons.Filled.ChevronRight, contentDescription = "Berikutnya", tint = TextMuted, modifier = Modifier.size(18.dp))
                                        }
                                    }

                                    // Excel / CSV Export
                                    androidx.compose.material3.Button(
                                        onClick = {
                                            scope.launch {
                                                exporting = true
                                                runCatching {
                                                    val filename = "Laporan-Harian-${DateUtils.localDateKey(activeDate)}.csv"
                                                    Exporter.saveToDownloads(context, filename, Exporter.buildDailyReportCsv(activeDate, state.activityLog, state.scooters))
                                                }
                                                    .onSuccess { context.showToast("Laporan Excel diunduh ($it)") }
                                                    .onFailure { context.showToast(it.toUserMessage(), long = true) }
                                                exporting = false
                                            }
                                        },
                                        enabled = !exporting,
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    ) {
                                        if (exporting) {
                                            androidx.compose.material3.CircularProgressIndicator(modifier = Modifier.size(12.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                                        } else {
                                            Icon(Icons.Filled.FileDownload, contentDescription = null, modifier = Modifier.size(13.dp))
                                        }
                                        Spacer(Modifier.width(4.dp))
                                        Text(if (exporting) "Membuat..." else "Export", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Daily Stats KPI Strip
                        item {
                            DailyKpiStrip(
                                total = logForDate.size,
                                checkoutCount = checkoutCount,
                                returnCount = returnCount,
                            )
                        }

                        // Status Unit Section
                        if (isLiveView) {
                            item {
                                MonitorFilterPanel(
                                    scooters = state.scooters,
                                    statusFilter = statusFilter,
                                    onStatusFilter = { statusFilter = it },
                                    typeFilter = typeFilter,
                                    onTypeFilter = { typeFilter = it },
                                )
                            }

                            if (filteredScooters.isEmpty()) {
                                item {
                                    Text(
                                        text = "Tidak ada unit scooter yang cocok dengan filter aktif.",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Surface, RoundedCornerShape(14.dp))
                                            .border(1.dp, Border, RoundedCornerShape(14.dp))
                                            .padding(28.dp),
                                    )
                                }
                            } else {
                                items(
                                    items = filteredScooters.chunked(2),
                                    key = { chunk -> chunk.joinToString { it.id } },
                                ) { row ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        row.forEach { scooter ->
                                            ScooterCard(
                                                scooter = scooter,
                                                onClick = { onOpenDetail(scooter.id) },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                        if (row.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        } else {
                            // Historical summary table for past date
                            item {
                                HistoricalSummary(logForDate)
                            }
                        }
                    }
                }
            }
        }
    }

    // Native date picker dialog
    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = activeDate.atStartOfDay(DateUtils.WIB).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val d = java.time.Instant.ofEpochMilli(millis).atZone(DateUtils.WIB).toLocalDate()
                        selectedDate = if (d == today) null else d
                    }
                    showDatePicker = false
                }) { Text("Pilih", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal", color = TextMuted) }
            },
        ) {
            DatePicker(state = pickerState, showModeToggle = false)
        }
    }
}

@Composable
private fun DatePill(label: String, active: Boolean, onClick: () -> Unit, live: Boolean = false) {
    Row(
        modifier = Modifier
            .background(if (active) Accent.copy(alpha = 0.15f) else Surface3, RoundedCornerShape(8.dp))
            .border(1.dp, if (active) Accent else Border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (live) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (active) BlueLive else TextSubtle, CircleShape),
            )
        }
        Text(
            text = label,
            color = if (active) Accent else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun DailyKpiStrip(
    total: Int,
    checkoutCount: Int,
    returnCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(12.dp))
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Column 1: Total
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = total.toString(),
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Total Transaksi",
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Divider 1
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(26.dp)
                .background(Border),
        )

        // Column 2: Keluar (Sewa)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = Red, modifier = Modifier.size(13.dp))
                Text(
                    text = checkoutCount.toString(),
                    color = Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "Keluar (Sewa)",
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Divider 2
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(26.dp)
                .background(Border),
        )

        // Column 3: Masuk (Kembali)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = Green, modifier = Modifier.size(13.dp))
                Text(
                    text = returnCount.toString(),
                    color = Green,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "Masuk (Kembali)",
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
