package com.evrenhouse.trackscooter.ui.dashboard
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.evrenhouse.trackscooter.data.ScooterStatus
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.LiveTimer
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.StatCard
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
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.util.ScooterColors
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.DeviceConditionHelper
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.TypeLabels
import kotlinx.coroutines.launch

@Composable
fun OutletSummaryCards(
    scooters: List<Scooter>,
    onSelectOutlet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Outlets.OPERATIONAL.forEach { outlet ->
            val outletScooters = scooters.filter { (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == outlet.id }
            val ready = outletScooters.count { it.status == ScooterStatus.AVAILABLE }
            val maintLuar = outletScooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location == "luar" }
            val maintOutlet = outletScooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location != "luar" }
            val total = ready + maintLuar + maintOutlet
            val outletColor = ScooterColors.getOutletColor(outlet.id)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .clickable { onSelectOutlet(outlet.id) }
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Surface3, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.evrenhouse.trackscooter.ui.common.getOutletIcon(outlet.id),
                                contentDescription = null,
                                tint = outletColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Column {
                            Text(outlet.label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Berdasarkan lokasi saat ini", color = TextMuted, fontSize = 10.sp)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(outletColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("$total Unit", color = outletColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }

                Box(Modifier.fillMaxWidth().height(1.dp).background(Border))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("$ready", color = Green, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Ready", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("$maintLuar", color = Warning, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Luar", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("$maintOutlet", color = Red, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Outlet", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("$total", color = androidx.compose.ui.graphics.Color(0xFFA855F7), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Total", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class RentalSessionSimple(
    val scooterId: String,
    val type: String,
    val startDt: java.time.LocalDateTime,
    val endDt: java.time.LocalDateTime?,
    val durationText: String,
    val inProgress: Boolean
)

@Composable
fun RecentLogTableCard(
    activityLog: List<ActivityLogEntry>,
    scooters: List<Scooter>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAll by remember { mutableStateOf(false) }
    val todayKey = remember { DateUtils.localDateKey(DateUtils.today()) }

    val sessions by remember(activityLog, scooters, todayKey) {
        derivedStateOf {
            val perUnit = mutableMapOf<String, MutableList<ActivityLogEntry>>()
            activityLog.forEach { e ->
                perUnit.getOrPut(e.scooterId) { mutableListOf() }.add(e)
            }

            val sessionList = mutableListOf<RentalSessionSimple>()

            perUnit.forEach { (scooterId, logs) ->
                val bike = scooters.find { it.id.equals(scooterId, ignoreCase = true) } ?: return@forEach
                val type = bike.type
                val sorted = logs.mapNotNull {
                    val dt = DateUtils.parse(it.timestamp)
                    if (dt != null) it to dt else null
                }.sortedBy { it.second }

                var openCheckout: Pair<ActivityLogEntry, java.time.LocalDateTime>? = null
                for ((log, dt) in sorted) {
                    if (log.action == "checkout") {
                        openCheckout = log to dt
                    } else if (log.action == "return" && openCheckout != null) {
                        val (_, startDt) = openCheckout
                        if (DateUtils.localDateKey(startDt.toLocalDate()) == todayKey) {
                            val diffSecs = java.time.Duration.between(startDt, dt).seconds
                            sessionList.add(
                                RentalSessionSimple(
                                    scooterId = scooterId,
                                    type = type,
                                    startDt = startDt,
                                    endDt = dt,
                                    durationText = DateUtils.formatDuration(diffSecs),
                                    inProgress = false
                                )
                            )
                        }
                        openCheckout = null
                    }
                }

                if (openCheckout != null) {
                    val (_, startDt) = openCheckout
                    if (DateUtils.localDateKey(startDt.toLocalDate()) == todayKey) {
                        val nowDt = java.time.LocalDateTime.now(DateUtils.WIB)
                        val diffSecs = java.time.Duration.between(startDt, nowDt).seconds
                        sessionList.add(
                            RentalSessionSimple(
                                scooterId = scooterId,
                                type = type,
                                startDt = startDt,
                                endDt = null,
                                durationText = DateUtils.formatDuration(diffSecs),
                                inProgress = true
                            )
                        )
                    }
                }
            }

            sessionList.sortedByDescending { it.startDt }
        }
    }

    val displayedSessions = remember(sessions, showAll) {
        if (showAll) sessions else sessions.take(8)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LOG RECENT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            Box(
                modifier = Modifier
                    .background(Surface3, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text("${sessions.count()} sesi", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (sessions.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada sesi sewa hari ini.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            for ((index, session) in displayedSessions.withIndex()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(session.scooterId) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val isDark = LocalThemeIsDark.current
                        val nameColor = ScooterColors.getScooterNameColor(session.type, session.scooterId, isDark = isDark)
                        Text(session.scooterId, color = nameColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, maxLines = 1)
                        val timeRange = if (session.endDt != null) {
                            "${DateUtils.formatTime(session.startDt)} - ${DateUtils.formatTime(session.endDt)}"
                        } else {
                            DateUtils.formatTime(session.startDt)
                        }
                        Text(
                            text = timeRange,
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(session.durationText, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace, maxLines = 1)
                        if (session.inProgress) {
                            Box(
                                modifier = Modifier
                                    .background(Warning.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Disewa", color = Warning, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (index < displayedSessions.count() - 1) {
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Border))
                }
            }

            if (sessions.count() > 8) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAll = !showAll }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (showAll) "Tampilkan Lebih Sedikit" else "Lihat Semua (${sessions.count()} sesi)",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Type summary: 2 Bento Cards (Scooter Dewasa & Scooter Jumbo) ──
@Composable
fun TypeSummaryCard(scooters: List<Scooter>, modifier: Modifier = Modifier) {
    val categories = listOf(
        "Scooter Dewasa" to listOf("sd", "sm", "sb"),
        "Scooter Jumbo" to listOf("sj", "sjm", "sjb")
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        for ((catTitle, catTypes) in categories) {
            CategoryBentoCard(catTitle = catTitle, catTypes = catTypes, scooters = scooters)
        }
    }
}

@Composable
private fun CategoryBentoCard(
    catTitle: String,
    catTypes: List<String>,
    scooters: List<Scooter>,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    val relevantTypes = remember(scooters, catTypes) {
        catTypes.filter { type -> scooters.any { it.type == type } }
    }
    val catTotal = scooters.count { it.type in catTypes }
    val catReady = scooters.count { it.type in catTypes && it.status == "available" }
    val catInUse = scooters.count { it.type in catTypes && it.status == "in-use" }
    val catKendala = scooters.count { it.type in catTypes && it.status == "maintenance" }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        // Card Header with More/Collapse toggle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface3.copy(alpha = 0.5f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .clickable { isExpanded = !isExpanded }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = catTitle,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(Surface, RoundedCornerShape(8.dp))
                            .border(1.dp, Border, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$catTotal unit",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Tombol More / Collapse
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isExpanded) Surface else Accent.copy(alpha = 0.12f))
                            .border(1.dp, if (isExpanded) Border else Accent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Collapse" else "More",
                            color = if (isExpanded) TextMuted else Accent,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Collapse" else "More",
                            tint = if (isExpanded) TextMuted else Accent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("$catReady ready", color = Green, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("·", color = TextSubtle)
                Text("$catInUse disewa", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                if (catKendala > 0) {
                    Text("·", color = TextSubtle)
                    Text("$catKendala kendala", color = Warning, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Sub-types list only when expanded!
        if (isExpanded) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Border)
            )

            if (relevantTypes.isEmpty()) {
                Text(
                    text = "Tidak ada unit di kategori ini.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                for ((idx, type) in relevantTypes.withIndex()) {
                    val group = scooters.filter { it.type == type }
                    val available = group.count { it.status == "available" }
                    val inUse = group.count { it.status == "in-use" }
                    val kendala = group.count { it.status == "maintenance" }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TypeBadge(type)
                                Text(
                                    text = TypeLabels.of(type),
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text("${group.size} unit", color = TextSubtle, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("$available ready", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text("·", color = TextSubtle)
                            Text("$inUse disewa", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            if (kendala > 0) {
                                Text("·", color = TextSubtle)
                                Text("$kendala kendala", color = Warning, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    if (idx < relevantTypes.size - 1) {
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
    var showAll by rememberSaveable { mutableStateOf(false) }
    val displayedRecords = if (showAll) records else records.take(5)

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
            displayedRecords.forEach { rec ->
                MaintenanceRow(rec, onComplete, completingId == rec.id)
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Border),
                )
            }

            if (records.size > 5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAll = !showAll }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (showAll) "Tampilkan lebih sedikit" else "Lihat semua (${records.size})",
                        color = Accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                    text = if (rec.location == "outlet") "Di Outlet" else if (!rec.locationDetail.isNullOrBlank()) "Luar · ${rec.locationDetail}" else "Luar Outlet",
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

/**
 * 4 kartu statistik armada (dipakai Dashboard operasional + Dashboard Manajemen).
 * Diekstrak dari DashboardScreen agar dua ranah tidak menduplikasi hitungan.
 */
@Composable
fun FleetStatCards(scooters: List<Scooter>, modifier: Modifier = Modifier) {
    val ready = scooters.count { it.status == ScooterStatus.AVAILABLE }
    val maintLuar = scooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location == "luar" }
    val maintOutlet = scooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location != "luar" }
    val total = ready + maintLuar + maintOutlet

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                label = "Unit Ready",
                sub = "Siap disewakan",
                value = ready,
                icon = { tint -> Icon(Icons.Filled.CheckCircle, null, Modifier.size(17.dp), tint = tint) },
                valueColor = Green,
                iconBg = Green.copy(alpha = 0.12f),
                iconColor = Green,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Maint. Luar Outlet",
                sub = "Perbaikan luar",
                value = maintLuar,
                icon = { tint -> Icon(Icons.Filled.Construction, null, Modifier.size(17.dp), tint = tint) },
                valueColor = Warning,
                iconBg = Warning.copy(alpha = 0.12f),
                iconColor = Warning,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                label = "Maint. di Outlet",
                sub = "Perbaikan outlet",
                value = maintOutlet,
                icon = { tint -> Icon(Icons.Filled.Build, null, Modifier.size(17.dp), tint = tint) },
                valueColor = Red,
                iconBg = Red.copy(alpha = 0.12f),
                iconColor = Red,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Unit Total",
                sub = "Total armada outlet",
                value = total,
                icon = { tint -> Icon(Icons.Filled.Layers, null, Modifier.size(17.dp), tint = tint) },
                valueColor = Color(0xFFA855F7),
                iconBg = Color(0xFFA855F7).copy(alpha = 0.12f),
                iconColor = Color(0xFFA855F7),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Tabel maintenance + alur selesaikan (dipakai dua dashboard).
 * Diekstrak dari DashboardScreen agar perilaku Selesaikan identik.
 */
@Composable
fun MaintenanceSection(
    viewModel: ScooterDataViewModel,
    records: List<MaintenanceRecord>,
    modifier: Modifier = Modifier,
) {
    var completingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val sweetAlert = LocalSweetAlert.current

    MaintenanceTable(
        records = records,
        completingId = completingId,
        onComplete = { rec ->
            sweetAlert.showConfirm(
                title = "Selesaikan Maintenance?",
                message = "Tandai perbaikan unit ${rec.scooterId} selesai? Unit akan kembali tersedia.",
                confirmText = "Ya, Selesai",
                cancelText = "Batal",
                onConfirm = {
                    scope.launch {
                        completingId = rec.id
                        runCatching { viewModel.completeMaintenance(rec.id) }
                            .onSuccess {
                                viewModel.refresh()
                                sweetAlert.showSuccess("Maintenance unit ${rec.scooterId} selesai")
                            }
                            .onFailure { err -> sweetAlert.showError(err.toUserMessage()) }
                        completingId = null
                    }
                },
            )
        },
        modifier = modifier,
    )
}
