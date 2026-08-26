package com.evrenhouse.trackscooter.ui.monitor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.BlueLive
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.TypeLabels

@Composable
fun MonitorFilterPanel(
    scooters: List<Scooter>,
    statusFilter: String,
    onStatusFilter: (String) -> Unit,
    typeFilter: String,
    onTypeFilter: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("FILTER STATUS", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonitorFilterTab("Semua Status", "all", statusFilter, onStatusFilter, count = scooters.size)
                MonitorFilterTab("Tersedia", "available", statusFilter, onStatusFilter, count = scooters.count { it.status == "available" })
                MonitorFilterTab("Online", "in-use", statusFilter, onStatusFilter, count = scooters.count { it.status == "in-use" })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonitorFilterTab("Rusak", "rusak", statusFilter, onStatusFilter, count = scooters.count { it.status == "rusak" })
                MonitorFilterTab("Maintenance", "maintenance", statusFilter, onStatusFilter, count = scooters.count { it.status == "maintenance" })
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("FILTER JENIS", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MonitorFilterTab("Semua Jenis", "all", typeFilter, onTypeFilter)
                MonitorFilterTab("Standar (SD)", "sd", typeFilter, onTypeFilter, count = scooters.count { it.type == "sd" })
                MonitorFilterTab("Jumbo (SJ)", "sj", typeFilter, onTypeFilter, count = scooters.count { it.type == "sj" })
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
                Text(count.toString(), color = if (selected == value) Color.White else TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HistoricalSummary(logForDate: List<ActivityLogEntry>) {
    val perUnit = logForDate.groupBy { it.scooterId }
        .map { (id, entries) ->
            Triple(id, entries.first().scooterType, entries.count { it.action == ActionLabels.CHECKOUT } to entries.count { it.action == ActionLabels.RETURN })
        }
        .sortedBy { it.first }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("RINGKASAN PER UNIT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Text("${logForDate.size} transaksi tercatat", color = TextMuted, fontSize = 11.sp)
        }
        if (logForDate.isEmpty()) {
            Text("Tidak ada aktivitas pada tanggal ini.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(24.dp))
        } else {
            perUnit.forEach { (id, type, counts) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(id, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        TypeBadge(type)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("↗ ${counts.first}x", color = Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("↙ ${counts.second}x", color = Green, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
            }
        }
    }
}

@Composable
fun ActivityFeedPanel(logForDate: List<ActivityLogEntry>, isLiveView: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isLiveView) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(BlueLive, CircleShape),
                )
            } else {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
            }
            Column {
                Text(if (isLiveView) "AKTIVITAS TERKINI" else "RIWAYAT AKTIVITAS", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                Text("${logForDate.size} transaksi keluar/masuk", color = TextMuted, fontSize = 11.sp)
            }
        }
        if (logForDate.isEmpty()) {
            Text(
                if (isLiveView) "Belum ada aktivitas hari ini." else "Tidak ada aktivitas pada tanggal ini.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(28.dp),
            )
        } else {
            logForDate.forEach { entry ->
                val isCheckout = entry.action == ActionLabels.CHECKOUT
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(if (isCheckout) Red.copy(alpha = 0.12f) else Green.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (isCheckout) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                            contentDescription = null,
                            tint = if (isCheckout) Red else Green,
                            modifier = Modifier.size(15.dp),
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(entry.scooterId, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(DateUtils.formatTimeSec(entry.timestamp), color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Text(
                            "Unit ${TypeLabels.of(entry.scooterType)} ${if (isCheckout) "disewa (checkout)" else "dikembalikan (return)"}",
                            color = TextMuted,
                            fontSize = 11.sp,
                        )
                        if (isLiveView) {
                            Text(DateUtils.timeAgo(entry.timestamp), color = TextSubtle, fontSize = 10.sp)
                        }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
            }
        }
    }
}
