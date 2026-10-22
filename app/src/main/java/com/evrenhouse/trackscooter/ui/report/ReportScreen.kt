package com.evrenhouse.trackscooter.ui.report

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.CardCornerRadius
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.InputCornerRadius
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.bounceClickable
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Modern Superapp Daily Rental Report Screen.
 * Physical Attendance/Checklist is completely removed per domain decision.
 * 100% focused on daily rental session analytics and activity feed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: ((String) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    val activeOutlet by viewModel.selectedOutlet.collectAsState()
    val isDark = LocalThemeIsDark.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val todayStr = remember { dateFormat.format(Date()) }

    var selectedDate by rememberSaveable { mutableStateOf(todayStr) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isRefreshing by remember { mutableStateOf(false) }

    // Outlet-filtered scooters & IDs
    val outletScooterIds = remember(state.scooters, activeOutlet) {
        if (activeOutlet == "all") {
            state.scooters.map { it.id }.toSet()
        } else {
            state.scooters
                .filter { (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == activeOutlet }
                .map { it.id }
                .toSet()
        }
    }

    // Filter logs for selected date & outlet
    val filteredLogs = remember(state.activityLog, selectedDate, outletScooterIds, activeOutlet) {
        state.activityLog
            .filter { entry ->
                val entryDate = entry.timestamp?.take(10) ?: ""
                val matchesDate = entryDate == selectedDate
                val matchesOutlet = if (activeOutlet == "all") true else outletScooterIds.contains(entry.scooterId)
                matchesDate && matchesOutlet
            }
            .sortedByDescending { it.timestamp }
    }

    // Calculate daily analytics
    val totalSessions = remember(filteredLogs) {
        filteredLogs.count { it.action == "checkout" }
    }

    val completedReturns = remember(filteredLogs) {
        filteredLogs.count { it.action == "return" }
    }

    val displayLogs = remember(filteredLogs, searchQuery) {
        if (searchQuery.isBlank()) filteredLogs
        else filteredLogs.filter {
            it.scooterId.contains(searchQuery.trim(), ignoreCase = true) ||
                it.scooterType.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.refresh()
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Date Navigator Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 0.dp else 4.dp,
                            shape = RoundedCornerShape(CardCornerRadius),
                            spotColor = Color(0x14000000),
                            ambientColor = Color(0x0A000000),
                        )
                        .clip(RoundedCornerShape(CardCornerRadius))
                        .background(Surface)
                        .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(CardCornerRadius))
                        .padding(16.dp),
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Accent.copy(alpha = if (isDark) 0.25f else 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = Accent,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Tanggal Laporan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted,
                                    )
                                    Text(
                                        text = if (selectedDate == todayStr) "Hari Ini ($selectedDate)" else selectedDate,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                    )
                                }
                            }

                            // Calendar Picker Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Surface2 else Color(0xFFF1F5F9))
                                    .bounceClickable {
                                        val cal = Calendar.getInstance()
                                        DatePickerDialog(
                                            context,
                                            { _, y, m, d ->
                                                selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                                            },
                                            cal.get(Calendar.YEAR),
                                            cal.get(Calendar.MONTH),
                                            cal.get(Calendar.DAY_OF_MONTH),
                                        ).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = "Ganti Tanggal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Accent,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick date selector chips
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DateChip(
                                label = "Hari Ini",
                                isSelected = selectedDate == todayStr,
                                onClick = { selectedDate = todayStr },
                            )

                            val yesterdayStr = remember {
                                val cal = Calendar.getInstance()
                                cal.add(Calendar.DAY_OF_YEAR, -1)
                                dateFormat.format(cal.time)
                            }
                            DateChip(
                                label = "Kemarin",
                                isSelected = selectedDate == yesterdayStr,
                                onClick = { selectedDate = yesterdayStr },
                            )
                        }
                    }
                }
            }

            // 2. Daily Analytics Hero (Total Sesi & Selesai)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    MetricBox(
                        title = "Total Sewa",
                        count = totalSessions,
                        unit = "Sesi Keluar",
                        color = Accent,
                        modifier = Modifier.weight(1f),
                    )

                    MetricBox(
                        title = "Selesai Kembali",
                        count = completedReturns,
                        unit = "Unit Masuk",
                        color = Green,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // 3. Search Bar & Section Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Aktivitas Sewa ($selectedDate)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isDark) Surface2 else Color(0xFFE2E8F0))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "${displayLogs.size} Aksi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextMuted,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Cari riwayat ID unit...", fontSize = 13.sp, color = TextMuted)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(InputCornerRadius),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Border.copy(alpha = 0.7f),
                        ),
                        singleLine = true,
                    )
                }
            }

            // 4. Session Logs Feed
            if (displayLogs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada riwayat untuk '$searchQuery'" else "Tidak ada aktivitas sewa pada $selectedDate",
                            color = TextMuted,
                            fontSize = 13.sp,
                        )
                    }
                }
            } else {
                items(displayLogs, key = { it.id }) { log ->
                    SessionLogCard(
                        entry = log,
                        onClick = { onOpenDetail?.invoke(log.scooterId) },
                    )
                }
            }

            // Spacer for floating bottom bar clearance
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun DateChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val isDark = LocalThemeIsDark.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) Accent else (if (isDark) Surface2 else Color(0xFFF1F5F9))
            )
            .bounceClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextMuted,
        )
    }
}

@Composable
private fun MetricBox(
    title: String,
    count: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(CardCornerRadius),
                spotColor = Color(0x14000000),
                ambientColor = Color(0x0A000000),
            )
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(CardCornerRadius))
            .padding(16.dp),
    ) {
        Column {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$count",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = color,
                letterSpacing = (-0.5).sp,
            )
            Text(unit, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextMuted)
        }
    }
}

@Composable
private fun SessionLogCard(
    entry: ActivityLogEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val isCheckout = entry.action == "checkout"
    val typeStyle = ScooterColors.STYLES[entry.scooterType.lowercase()]

    val actionColor = if (isCheckout) Accent else Green
    val actionText = if (isCheckout) "Keluar (Sewa)" else "Kembali"
    val timeFormatted = entry.timestamp?.let { DateUtils.parse(it)?.let { dt -> DateUtils.formatTime(dt) } } ?: "-"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
            .bounceClickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(actionColor.copy(alpha = if (isDark) 0.25f else 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isCheckout) Icons.Default.DirectionsBike else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = actionColor,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.scooterId,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    typeStyle?.accent?.copy(alpha = if (isDark) 0.2f else 0.12f)
                                        ?: (if (isDark) Surface2 else Color(0xFFF1F5F9))
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = entry.scooterType.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeStyle?.accent ?: TextMuted,
                            )
                        }
                    }
                    Text(
                        text = "$actionText • $timeFormatted",
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(actionColor.copy(alpha = if (isDark) 0.18f else 0.10f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    text = actionText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = actionColor,
                )
            }
        }
    }
}
