package com.evrenhouse.trackscooter.ui.monitor

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.ui.common.StatusChip
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.BlueLive
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
import com.evrenhouse.trackscooter.ui.theme.Yellow
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.TypeLabels
import java.time.LocalDate

enum class MonitorTab(val label: String, val icon: ImageVector) {
    LIVE_SESSION("Live Session", Icons.Filled.Sensors),
    RECENT("Recent", Icons.Filled.History),
    SUMMARY("Summary", Icons.Filled.Analytics),
}

/** ── Tab Selector ── */
@Composable
fun MonitorTabSelector(
    selectedTab: MonitorTab,
    onSelectTab: (MonitorTab) -> Unit,
    liveCount: Int,
    recentCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface2, RoundedCornerShape(12.dp))
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MonitorTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val count = when (tab) {
                MonitorTab.LIVE_SESSION -> liveCount
                MonitorTab.RECENT -> recentCount
                MonitorTab.SUMMARY -> null
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) Accent else Color.Transparent)
                    .clickable { onSelectTab(tab) }
                    .padding(vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = tab.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TextMuted,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = tab.label,
                    color = if (isSelected) Color.White else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
                if (count != null && count > 0) {
                    Spacer(Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.25f)
                                else if (tab == MonitorTab.LIVE_SESSION) Red.copy(alpha = 0.2f)
                                else Surface3,
                                CircleShape,
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = count.toString(),
                            color = if (isSelected) Color.White
                            else if (tab == MonitorTab.LIVE_SESSION) Red
                            else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

/** ── Header with smart live pulse ── */
@Composable
fun LivePulseHeader(
    isLiveView: Boolean,
    isLiveConnected: Boolean,
    isReconnecting: Boolean,
    activeDate: LocalDate,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (isLiveView) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = when {
                            isReconnecting -> Warning.copy(alpha = pulseAlpha)
                            isLiveConnected -> BlueLive.copy(alpha = pulseAlpha)
                            else -> Green.copy(alpha = pulseAlpha)
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            color = when {
                                isReconnecting -> Warning
                                isLiveConnected -> BlueLive
                                else -> Green
                            },
                            shape = CircleShape,
                        ),
                )
            }
        } else {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(18.dp),
            )
        }

        Column {
            Text(
                text = if (isLiveView) {
                    if (isReconnecting) "Live Monitor Lapangan (Menghubungkan...)" else "Live Monitor Lapangan"
                } else {
                    "Arsip: ${DateUtils.formatWeekdayFull(activeDate)}"
                },
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (isLiveView) {
                    if (isLiveConnected) "Terhubung real-time · sinkronisasi instan aktif"
                    else if (isReconnecting) "Koneksi terputus, mencoba menghubungkan ulang..."
                    else "Memantau sesi dan aktivitas secara langsung"
                } else {
                    "Data riwayat transaksi operasional masa lalu"
                },
                color = if (isReconnecting) Warning else TextMuted,
                fontSize = 12.sp,
            )
        }
    }
}

/** ── Live Session Card with Timer (No Seconds) and Progressive Loading Bar ── */
@Composable
fun LiveSessionCard(
    scooter: Scooter,
    nowMillis: Long,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val dt = DateUtils.parse(scooter.lastUpdated)
    val startMillis = dt?.atZone(DateUtils.WIB)?.toInstant()?.toEpochMilli() ?: nowMillis
    val elapsedSecs = ((nowMillis - startMillis) / 1000).coerceAtLeast(0)
    val hrs = elapsedSecs / 3600
    val mins = (elapsedSecs % 3600) / 60
    val totalMins = (elapsedSecs / 60).toInt()

    // Timer text TANPA DETIK
    val timerText = if (hrs > 0) "${hrs}j ${mins}m" else "${mins} mnt"

    // Progressive bar benchmarked to standard 60-min session
    val targetSessionMins = 60f
    val progress = (totalMins / targetSessionMins).coerceIn(0.04f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "barProgress")

    val barColor = when {
        totalMins >= 60 -> Red
        totalMins >= 45 -> Warning
        else -> BlueLive
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Top Row: Scooter ID & Type Badge + Online Chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = scooter.id,
                    color = Accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                TypeBadge(scooter.type)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusChip(scooter.status)
                if (onClick != null) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = TextSubtle,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Middle: Live Timer tanpa detik
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface3, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    Icons.Filled.Timer,
                    contentDescription = null,
                    tint = barColor,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "Durasi Sewa:",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }
            Text(
                text = timerText,
                color = barColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Progressive Loading Bar
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Surface3),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(barColor),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Mulai: ${DateUtils.formatTime(scooter.lastUpdated)} WIB",
                    color = TextSubtle,
                    fontSize = 11.sp,
                )
                Text(
                    text = if (totalMins >= 60) "⚠️ Melebihi 60 mnt" else "$totalMins mnt berjalan",
                    color = if (totalMins >= 60) Red else TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = if (totalMins >= 60) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }
    }
}

/** ── Live Session Empty State ── */
@Composable
fun LiveSessionEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Green.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = "Tidak Ada Sesi Sewa Aktif",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Semua scooter sedang standby/tersedia. Saat scooter di-scan sewa (checkout), sesi dan timer live akan otomatis muncul di sini.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

/** ── Single Activity Log Item for LazyColumn ── */
@Composable
fun ActivityItemRow(
    entry: ActivityLogEntry,
    isLiveView: Boolean,
    modifier: Modifier = Modifier,
) {
    val isCheckout = entry.action == ActionLabels.CHECKOUT

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    if (isCheckout) Red.copy(alpha = 0.12f) else Green.copy(alpha = 0.12f),
                    RoundedCornerShape(8.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isCheckout) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = if (isCheckout) Red else Green,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = entry.scooterId,
                    color = Accent,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = DateUtils.formatTimeSec(entry.timestamp),
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
            Text(
                text = "Unit ${TypeLabels.of(entry.scooterType)} ${if (isCheckout) "disewa (checkout)" else "dikembalikan (return)"}",
                color = TextMuted,
                fontSize = 11.sp,
            )
            if (isLiveView) {
                Text(
                    text = DateUtils.timeAgo(entry.timestamp),
                    color = TextSubtle,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

/** ── Responsive Status Unit Filter Panel (Horizontal Scrolling Chips) ── */
@Composable
fun MonitorFilterPanel(
    scooters: List<Scooter>,
    statusFilter: String,
    onStatusFilter: (String) -> Unit,
    typeFilter: String,
    onTypeFilter: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Status filters in responsive horizontal scrollable LazyRow
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("FILTER STATUS", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item {
                    MonitorFilterTab("Semua", "all", statusFilter, onStatusFilter, count = scooters.size)
                }
                item {
                    MonitorFilterTab("Tersedia", "available", statusFilter, onStatusFilter, count = scooters.count { it.status == "available" })
                }
                item {
                    MonitorFilterTab("Online", "in-use", statusFilter, onStatusFilter, count = scooters.count { it.status == "in-use" })
                }
                item {
                    MonitorFilterTab("Rusak", "rusak", statusFilter, onStatusFilter, count = scooters.count { it.status == "rusak" })
                }
                item {
                    MonitorFilterTab("Maintenance", "maintenance", statusFilter, onStatusFilter, count = scooters.count { it.status == "maintenance" })
                }
            }
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(Border))

        // Type filters in responsive horizontal scrollable LazyRow
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("FILTER JENIS", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item {
                    MonitorFilterTab("Semua Jenis", "all", typeFilter, onTypeFilter)
                }
                item {
                    MonitorFilterTab("Standar (SD)", "sd", typeFilter, onTypeFilter, count = scooters.count { it.type == "sd" })
                }
                item {
                    MonitorFilterTab("Jumbo (SJ)", "sj", typeFilter, onTypeFilter, count = scooters.count { it.type == "sj" })
                }
            }
        }
    }
}

@Composable
fun MonitorFilterTab(label: String, value: String, selected: String, onSelect: (String) -> Unit, count: Int? = null) {
    Row(
        modifier = Modifier
            .background(if (selected == value) Accent.copy(alpha = 0.15f) else Surface3, RoundedCornerShape(8.dp))
            .border(1.dp, if (selected == value) Accent else Border, RoundedCornerShape(8.dp))
            .clickable { onSelect(value) }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = label,
            color = if (selected == value) Accent else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (count != null) {
            Box(
                modifier = Modifier
                    .background(if (selected == value) Accent else Border, CircleShape)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(
                    text = count.toString(),
                    color = if (selected == value) Color.White else TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** ── Clean Historical Summary Table for Past Dates ── */
@Composable
fun HistoricalSummary(logForDate: List<ActivityLogEntry>, modifier: Modifier = Modifier) {
    val perUnit = logForDate.groupBy { it.scooterId }
        .map { (id, entries) ->
            Triple(id, entries.first().scooterType, entries.count { it.action == ActionLabels.CHECKOUT } to entries.count { it.action == ActionLabels.RETURN })
        }
        .sortedBy { it.first }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("RINGKASAN PER UNIT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("${logForDate.size} transaksi tercatat", color = TextMuted, fontSize = 11.sp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
        if (logForDate.isEmpty()) {
            Text("Tidak ada aktivitas pada tanggal ini.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(24.dp))
        } else {
            perUnit.forEachIndexed { index, (id, type, counts) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(id, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        TypeBadge(type)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Keluar", tint = Red, modifier = Modifier.size(13.dp))
                            Text("${counts.first}x", color = Red, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = "Masuk", tint = Green, modifier = Modifier.size(13.dp))
                            Text("${counts.second}x", color = Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (index < perUnit.size - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
                }
            }
        }
    }
}
