package com.evrenhouse.trackscooter.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.ui.common.LiveTimer
import com.evrenhouse.trackscooter.ui.common.StatusChip
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
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.DeviceConditionHelper
import com.evrenhouse.trackscooter.util.TypeLabels

// ── Scooter card (dashboard grid + monitor panel) ──────────
@Composable
fun ScooterCard(scooter: Scooter, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = scooter.id,
                    color = Accent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                Text(
                    text = TypeLabels.of(scooter.type),
                    color = TextMuted,
                    fontSize = 11.sp,
                )
            }
            if (onClick != null) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Detail", tint = TextSubtle, modifier = Modifier.size(16.dp))
            } else {
                TypeBadge(scooter.type)
            }
        }

        StatusChip(scooter.status)

        val issues = DeviceConditionHelper.buildIssueList(scooter.deviceCondition)
        if (issues.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                issues.take(3).forEach { issue ->
                    Text(
                        text = issue.text,
                        color = if (issue.tone == com.evrenhouse.trackscooter.util.FieldTone.WARN) Warning else Red,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        if ((scooter.status == "maintenance" || scooter.status == "rusak") && !scooter.maintenanceNote.isNullOrBlank()) {
            Text(
                text = "Catatan: ${scooter.maintenanceNote}",
                color = TextMuted,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        LiveTimer(scooter.status, scooter.lastUpdated)
    }
}

// ── Type summary ───────────────────────────────────────────
@Composable
fun TypeSummaryCard(scooters: List<Scooter>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Text(
            text = "RINGKASAN PER JENIS",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(16.dp),
        )
        listOf("sd", "sj").forEach { type ->
            val group = scooters.filter { it.type == type }
            val available = group.count { it.status == "available" }
            val inUse = group.count { it.status == "in-use" }
            val rusak = group.count { it.status == "rusak" }
            val maint = group.count { it.status == "maintenance" }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TypeBadge(type)
                    Text(type, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("$available tersedia", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text("·", color = Border)
                    Text("$inUse online", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    if (rusak > 0) {
                        Text("·", color = Border)
                        Text("$rusak rusak", color = Red, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    if (maint > 0) {
                        Text("·", color = Border)
                        Text("$maint dirawat", color = Warning, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            if (type != "sj") {
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

// ── Activity feed ──────────────────────────────────────────
@Composable
fun ActivityFeedCard(log: List<ActivityLogEntry>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Text(
            text = "AKTIVITAS TERBARU",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(16.dp),
        )
        if (log.isEmpty()) {
            Text(
                "Belum ada aktivitas.",
                color = TextMuted,
                fontSize = 13.sp,
                modifier = Modifier.padding(24.dp),
            )
        } else {
            Column {
                log.take(15).forEach { entry ->
                    FeedRow(entry)
                }
            }
        }
    }
}

@Composable
private fun FeedRow(entry: ActivityLogEntry) {
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
                .size(30.dp)
                .background(if (isCheckout) Red.copy(alpha = 0.12f) else Green.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (isCheckout) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = if (isCheckout) Red else Green,
                modifier = Modifier.size(14.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.scooterId,
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = "${TypeLabels.of(entry.scooterType)} · ${DateUtils.timeAgo(entry.timestamp)}",
                color = TextMuted,
                fontSize = 10.sp,
                maxLines = 1,
            )
        }
        Text(
            text = if (isCheckout) "Keluar" else "Masuk",
            color = if (isCheckout) Red else Green,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ── Activity history table (search + filter + pagination) ──
data class HistoryFilters(
    val search: String = "",
    val action: String = "all",
    val page: Int = 1,
)

@Composable
fun HistoryTable(
    log: List<ActivityLogEntry>,
    filters: HistoryFilters,
    onFilters: (HistoryFilters) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemsPerPage = 8
    val filtered = log.filter { entry ->
        val matchesSearch = entry.scooterId.contains(filters.search, ignoreCase = true)
        val matchesAction = filters.action == "all" || entry.action == filters.action
        matchesSearch && matchesAction
    }
    val totalPages = ((filtered.size + itemsPerPage - 1) / itemsPerPage).coerceAtLeast(1)
    val safePage = filters.page.coerceIn(1, totalPages)
    val paged = filtered.drop((safePage - 1) * itemsPerPage).take(itemsPerPage)

    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        // Header + filters
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "RIWAYAT AKTIVITAS LENGKAP",
                color = TextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Text("Total ${filtered.size} riwayat ditemukan", color = TextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = filters.search,
                    onValueChange = { onFilters(filters.copy(search = it, page = 1)) },
                    placeholder = { Text("Cari ID...", color = TextSubtle, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                FilterDropdown(
                    label = if (filters.action == "all") "Semua" else if (filters.action == "checkout") "Keluar" else "Masuk",
                    options = listOf("all" to "Semua", "checkout" to "Keluar", "return" to "Masuk"),
                    selected = filters.action,
                    onSelect = { onFilters(filters.copy(action = it, page = 1)) },
                )
            }
        }

        // Rows
        if (paged.isEmpty()) {
            Text(
                "Tidak ada data riwayat yang cocok.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(24.dp),
            )
        } else {
            Column {
                paged.forEach { entry ->
                    HistoryRow(entry)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Border),
                    )
                }
            }
        }

        // Pagination
        if (totalPages > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedPill(
                    text = "Sebelumnya",
                    enabled = safePage > 1,
                    onClick = { onFilters(filters.copy(page = safePage - 1)) },
                )
                Text("Halaman $safePage dari $totalPages", color = TextMuted, fontSize = 11.sp)
                OutlinedPill(
                    text = "Berikutnya",
                    enabled = safePage < totalPages,
                    onClick = { onFilters(filters.copy(page = safePage + 1)) },
                )
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: ActivityLogEntry) {
    val isCheckout = entry.action == ActionLabels.CHECKOUT
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.scooterId,
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = TypeLabels.of(entry.scooterType),
                color = TextMuted,
                fontSize = 10.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                if (isCheckout) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = if (isCheckout) Red else Green,
                modifier = Modifier.size(11.dp),
            )
            Text(
                text = if (isCheckout) "Dipakai" else "Tersedia",
                color = if (isCheckout) Red else Green,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(11.dp))
                Text(
                    text = DateUtils.formatFullSec(entry.timestamp),
                    color = TextMuted,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

// ── Maintenance tracking table ─────────────────────────────
@Composable
fun MaintenanceTable(
    records: List<MaintenanceRecord>,
    onComplete: (MaintenanceRecord) -> Unit,
    completingId: String?,
    modifier: Modifier = Modifier,
) {
    val repair = records.count { it.status == "repair" }
    val done = records.count { it.status == "done" }

    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "STATUS MAINTENANCE",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )
                Text("$repair dalam perbaikan · $done selesai", color = TextMuted, fontSize = 11.sp)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(Warning.copy(alpha = 0.12f), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Icon(Icons.Filled.Construction, contentDescription = null, tint = Warning, modifier = Modifier.size(12.dp))
                Text("Maintenance", color = Warning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (records.isEmpty()) {
            Text(
                "Belum ada catatan maintenance.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(24.dp),
            )
        } else {
            records.take(20).forEach { rec ->
                MaintenanceRow(rec, onComplete, completingId == rec.id)
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
private fun MaintenanceRow(rec: MaintenanceRecord, onComplete: (MaintenanceRecord) -> Unit, completing: Boolean) {
    val isRepair = rec.status == "repair"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = rec.scooterId,
                color = Accent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(11.dp))
                Text(
                    text = if (rec.location == "outlet") "Di Outlet" else "Keluar / Luar",
                    color = TextMuted,
                    fontSize = 11.sp,
                )
            }
            if (!rec.issue.isNullOrBlank()) {
                Text(
                    text = rec.issue,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = DateUtils.formatFull(rec.startedAt),
                color = TextSubtle,
                fontSize = 10.sp,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(
                        if (isRepair) Warning.copy(alpha = 0.12f) else Green.copy(alpha = 0.12f),
                        RoundedCornerShape(6.dp),
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Icon(
                    if (isRepair) Icons.Filled.Construction else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = if (isRepair) Warning else Green,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = if (isRepair) "Repair" else "Selesai",
                    color = if (isRepair) Warning else Green,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            if (isRepair) {
                androidx.compose.material3.OutlinedButton(
                    onClick = { onComplete(rec) },
                    enabled = !completing,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Warning),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Warning),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    if (completing) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            color = Warning,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("Selesai", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ── Small helpers ──────────────────────────────────────────
@Composable
fun OutlinedPill(text: String, enabled: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
    ) {
        Text(text, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    androidx.compose.material3.ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        androidx.compose.material3.OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable)
                .width(110.dp),
            shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodySmall,
        )
        androidx.compose.material3.DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { (value, text) ->
                androidx.compose.material3.DropdownMenuItem(
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
