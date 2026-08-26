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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.StatusChip
import com.evrenhouse.trackscooter.ui.common.TypeBadge
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
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Exporter
import com.evrenhouse.trackscooter.util.DeviceConditionHelper
import com.evrenhouse.trackscooter.util.TypeLabels
import com.evrenhouse.trackscooter.util.showToast
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedDate by remember { mutableStateOf<LocalDate?>(null) } // null = live today
    var statusFilter by remember { mutableStateOf("all") }
    var typeFilter by remember { mutableStateOf("all") }
    var showStatusPanel by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }

    val today = DateUtils.today()

    // Unique dates from the log, newest first
    val availableDates = remember(state.activityLog) {
        state.activityLog.mapNotNull { DateUtils.toLocalDate(it.timestamp) }.distinct().sortedDescending()
    }

    val activeDate = selectedDate ?: today
    val isLiveView = selectedDate == null || activeDate == today

    val logForDate = state.activityLog.filter { DateUtils.toLocalDate(it.timestamp) == activeDate }
    val checkoutCount = logForDate.count { it.action == ActionLabels.CHECKOUT }
    val returnCount = logForDate.count { it.action == ActionLabels.RETURN }

    val filteredScooters = state.scooters.filter { s ->
        (statusFilter == "all" || s.status == statusFilter) &&
            (typeFilter == "all" || s.type == typeFilter)
    }

    val currentIdx = availableDates.indexOfFirst { it == activeDate }
    val canGoPrev = currentIdx in 0 until availableDates.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (isLiveView) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(BlueLive, CircleShape),
                )
            } else {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
            }
            Text(if (isLiveView) "Live Monitor Lapangan" else "Riwayat Harian", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            if (isLiveView) "Pemantauan status dan log aktivitas secara real-time"
            else "Menampilkan riwayat aktivitas — ${DateUtils.formatWeekdayFull(activeDate)}",
            color = TextMuted,
            fontSize = 13.sp,
        )

        // Date navigator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(8.dp),
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

            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item {
                    DatePill(
                        label = "Hari Ini (Live)",
                        active = isLiveView,
                        live = true,
                        onClick = { selectedDate = null },
                    )
                }
                items(availableDates.filter { it != today }.take(7)) { d ->
                    DatePill(
                        label = DateUtils.formatPill(d),
                        active = selectedDate == d,
                        onClick = { selectedDate = d },
                    )
                }
            }

            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Pilih tanggal", tint = Accent, modifier = Modifier.size(18.dp))
            }

            IconButton(onClick = {
                if (currentIdx > 0) selectedDate = availableDates[currentIdx - 1] else selectedDate = null
            }, enabled = !isLiveView) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Berikutnya", tint = TextMuted, modifier = Modifier.size(18.dp))
            }

            // Excel export
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
                Text(if (exporting) "Membuat..." else "Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Daily stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatMini("Total Transaksi", logForDate.size, TextPrimary, Icons.Filled.BarChart, Accent, Modifier.weight(1f))
            StatMini("Keluar (Sewa)", checkoutCount, Red, Icons.Filled.ArrowUpward, Red, Modifier.weight(1f))
            StatMini("Masuk (Kembali)", returnCount, Green, Icons.Filled.ArrowDownward, Green, Modifier.weight(1f))
        }

        // Status panel toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = { showStatusPanel = !showStatusPanel },
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
            ) {
                Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (showStatusPanel) "Sembunyikan Status Unit" else "Tampilkan Status Unit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(4.dp))
                Icon(if (showStatusPanel) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }

        when {
            state.error != null && state.scooters.isEmpty() -> {
                ErrorState(message = state.error ?: "", onRetry = { viewModel.refresh() })
            }
            state.loading && state.scooters.isEmpty() -> {
                LoadingState("Memuat Data Lokal...")
            }
            else -> {
                if (showStatusPanel) {
                    if (isLiveView) {
                        // Filter tabs + grid
                        MonitorFilterPanel(
                            scooters = state.scooters,
                            statusFilter = statusFilter,
                            onStatusFilter = { statusFilter = it },
                            typeFilter = typeFilter,
                            onTypeFilter = { typeFilter = it },
                        )
                        if (filteredScooters.isEmpty()) {
                            Text(
                                "Tidak ada unit scooter yang cocok dengan filter aktif.",
                                color = TextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Surface, RoundedCornerShape(14.dp))
                                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                                    .padding(28.dp),
                            )
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 150.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                userScrollEnabled = false,
                            ) {
                                items(filteredScooters, key = { it.id }) { scooter ->
                                    ScooterCard(scooter = scooter, onClick = { onOpenDetail(scooter.id) })
                                }
                            }
                        }
                    } else {
                        // Historical per-scooter summary
                        HistoricalSummary(logForDate)
                    }
                }

                // Activity feed (filtered by date)
                ActivityFeedPanel(logForDate, isLiveView)
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
private fun StatMini(label: String, value: Int, valueColor: androidx.compose.ui.graphics.Color, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(15.dp))
        }
        Column {
            Text(value.toString(), color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 18.sp)
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

