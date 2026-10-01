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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.Preview
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
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
            val scootersById = scooters.associateBy { it.id.uppercase() }
            val sessionList = mutableListOf<RentalSessionSimple>()

            perUnit.forEach { (scooterId, logs) ->
                val bike = scootersById[scooterId.uppercase()] ?: return@forEach
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

    val isDark = LocalThemeIsDark.current
    val readyTextColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
    val inUseTextColor = if (isDark) Color(0xFF818CF8) else Color(0xFF3730A3)
    val kendalaTextColor = if (isDark) Color(0xFFFBBF24) else Color(0xFF8A5300)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        // Card Header: Seluruh header bisa di-klik untuk buka/tutup rincian dengan chevron
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = catTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    // Teks hitungan biasa (bukan pill tombol)
                    Text(
                        text = "· $catTotal unit",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                // Ikon Chevron buka/tutup (hapus tombol More)
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Tutup rincian kategori" else "Buka rincian kategori",
                        tint = TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Bar proporsi bersegmen (ready hijau, disewa biru, kendala amber)
            if (catTotal > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                ) {
                    if (catReady > 0) {
                        Box(
                            modifier = Modifier
                                .weight(catReady.toFloat())
                                .fillMaxHeight()
                                .background(Green),
                        )
                    }
                    if (catInUse > 0) {
                        Box(
                            modifier = Modifier
                                .weight(catInUse.toFloat())
                                .fillMaxHeight()
                                .background(Accent),
                        )
                    }
                    if (catKendala > 0) {
                        Box(
                            modifier = Modifier
                                .weight(catKendala.toFloat())
                                .fillMaxHeight()
                                .background(Warning),
                        )
                    }
                }
            }

            // Teks hitungan di bawah bar dengan rasio kontras tinggi
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("$catReady ready", color = readyTextColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                Text("·", color = Border)
                Text("$catInUse disewa", color = inUseTextColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                if (catKendala > 0) {
                    Text("·", color = Border)
                    Text("$catKendala kendala", color = kendalaTextColor, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Sub-types list only when expanded
        if (isExpanded) {
            HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (relevantTypes.isEmpty()) {
                    Text(
                        text = "Tidak ada unit di kategori ini.",
                        color = TextMuted,
                        fontSize = 11.sp,
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
                                .padding(vertical = 4.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    TypeBadge(type)
                                    Text(
                                        text = TypeLabels.of(type),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                Text("${group.size} unit", color = TextSubtle, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("$available ready", color = readyTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Text("·", color = TextSubtle)
                                Text("$inUse disewa", color = inUseTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                if (kendala > 0) {
                                    Text("·", color = TextSubtle)
                                    Text("$kendala kendala", color = kendalaTextColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        if (idx < relevantTypes.size - 1) {
                            HorizontalDivider(color = Border.copy(alpha = 0.4f), thickness = 0.5.dp)
                        }
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
    // Filter hanya armada yang masih dalam perbaikan ("repair") dan urutkan paling lama lebih dulu
    val activeRepairs = remember(records) {
        records
            .filter { it.status == "repair" }
            .sortedBy { it.startedAt }
    }
    var showAll by rememberSaveable { mutableStateOf(false) }
    val displayedRecords = if (showAll) activeRepairs else activeRepairs.take(5)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        // Header Antrian Perbaikan (tanpa pill Maintenance berlebih & gap rapat)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Perbaikan Berjalan (${activeRepairs.size})",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)

        if (activeRepairs.isEmpty()) {
            Text(
                "Tidak ada armada dalam antrian perbaikan.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(24.dp),
            )
        } else {
            displayedRecords.forEach { rec ->
                RepairQueueRow(rec, onComplete, completingId == rec.id)
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = Border.copy(alpha = 0.5f),
                    thickness = 0.5.dp,
                )
            }

            if (activeRepairs.size > 5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAll = !showAll }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (showAll) "Tampilkan lebih sedikit" else "Lihat semua (${activeRepairs.size})",
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun RepairQueueRow(
    rec: MaintenanceRecord,
    onComplete: (MaintenanceRecord) -> Unit,
    completing: Boolean,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val locLabel = if (rec.location == "outlet") {
        "Di Outlet"
    } else if (!rec.locationDetail.isNullOrBlank()) {
        "Luar (${rec.locationDetail})"
    } else {
        "Luar Outlet"
    }
    val noteText = rec.issue?.takeIf { it.isNotBlank() && it != "-" }
        ?: rec.note?.takeIf { it.isNotBlank() && it != "-" }
    val durationText = DateUtils.timeAgo(rec.startedAt)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        // Kolom Kiri 2 Baris:
        // Baris 1: ID (onSurface, monospace) dan lokasi
        // Baris 2: Catatan (maxLines = 1, ellipsis, sembunyikan jika kosong) dan durasi relatif
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            // Baris 1
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = rec.scooterId,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                Text("·", color = Border)
                Text(
                    text = locLabel,
                    color = if (rec.location == "outlet") TextMuted else Red,
                    fontSize = 11.sp,
                    fontWeight = if (rec.location == "outlet") FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Baris 2
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = TextSubtle,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = durationText,
                    color = TextSubtle,
                    fontSize = 10.5.sp,
                )
                if (!noteText.isNullOrBlank()) {
                    Text("·", color = Border)
                    Text(
                        text = noteText,
                        color = TextMuted,
                        fontSize = 10.5.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }
        }

        // Tombol Selesai Kompak dengan min 48dp touch target
        Box(contentAlignment = Alignment.Center) {
            Button(
                onClick = { onComplete(rec) },
                enabled = !completing,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFFD97706) else Color(0xFF8A5300),
                    contentColor = Color.White,
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 44.dp),
            ) {
                if (completing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Tandai perbaikan selesai", modifier = Modifier.size(13.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text("Selesai", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
fun KpiCard(
    label: String,
    value: Int,
    icon: @Composable (Color) -> Unit,
    accentColor: Color,
    valueTextColor: Color,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Column(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .then(clickModifier)
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Icon kecil
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            icon(accentColor)
        }

        // Angka besar (28sp bold monospace) + Satu label (tanpa subjudul bertele-tele)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = value.toString(),
                color = valueTextColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 4 kartu statistik armada padat (Ready, Disewa, Maint Outlet, Maint Luar).
 * Total armada di chip outlet = ready + inUse + maintOutlet + maintLuar (100% konsisten).
 */
@Composable
fun FleetStatCards(
    scooters: List<Scooter>,
    onCardClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val ready = scooters.count { it.status == ScooterStatus.AVAILABLE }
    val inUse = scooters.count { it.status == ScooterStatus.IN_USE }
    val maintOutlet = scooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location != "luar" }
    val maintLuar = scooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location == "luar" }

    val readyTextColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
    val inUseTextColor = if (isDark) Color(0xFF818CF8) else Color(0xFF3730A3)
    val amberTextColor = if (isDark) Color(0xFFFBBF24) else Color(0xFF8A5300)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KpiCard(
                label = "Unit Ready",
                value = ready,
                icon = { tint -> Icon(Icons.Filled.CheckCircle, null, Modifier.size(16.dp), tint = tint) },
                accentColor = Green,
                valueTextColor = readyTextColor,
                onClick = { onCardClick?.invoke(ScooterStatus.AVAILABLE) },
                modifier = Modifier.weight(1f),
            )
            KpiCard(
                label = "Unit Disewa",
                value = inUse,
                icon = { tint -> Icon(Icons.Filled.ElectricScooter, null, Modifier.size(16.dp), tint = tint) },
                accentColor = Accent,
                valueTextColor = inUseTextColor,
                onClick = { onCardClick?.invoke(ScooterStatus.IN_USE) },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            KpiCard(
                label = "Perbaikan di Outlet",
                value = maintOutlet,
                icon = { tint -> Icon(Icons.Filled.Build, null, Modifier.size(16.dp), tint = tint) },
                accentColor = Warning,
                valueTextColor = amberTextColor,
                onClick = { onCardClick?.invoke(ScooterStatus.MAINTENANCE) },
                modifier = Modifier.weight(1f),
            )
            KpiCard(
                label = "Perbaikan Luar Outlet",
                value = maintLuar,
                icon = { tint -> Icon(Icons.Filled.Construction, null, Modifier.size(16.dp), tint = tint) },
                accentColor = Warning,
                valueTextColor = amberTextColor,
                onClick = { onCardClick?.invoke(ScooterStatus.MAINTENANCE) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Tabel perbaikan berjalan + dialog konfirmasi selesaikan.
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
                title = "Selesaikan Perbaikan?",
                message = "Tandai unit ${rec.scooterId} selesai diperbaiki? Unit akan kembali berstatus Ready.",
                confirmText = "Ya, Selesai",
                cancelText = "Batal",
                onConfirm = {
                    scope.launch {
                        completingId = rec.id
                        runCatching { viewModel.completeMaintenance(rec.id) }
                            .onSuccess {
                                viewModel.refresh()
                                sweetAlert.showSuccess("Perbaikan unit ${rec.scooterId} selesai")
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

// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "Fleet Stat Cards", showBackground = true)
@Composable
private fun FleetStatCardsPreview() {
    val sampleScooters = listOf(
        Scooter("SB-01", "sb", ScooterStatus.AVAILABLE, currentOutlet = "utara"),
        Scooter("SB-02", "sb", ScooterStatus.IN_USE, currentOutlet = "utara"),
        Scooter("FZ-05", "fz", ScooterStatus.MAINTENANCE, currentOutlet = "utara", activeMaintenance = com.evrenhouse.trackscooter.data.ActiveMaintenance("m1", "outlet", null, "baterai", null, "repair", "2026-09-25T10:00:00Z")),
        Scooter("EX-10", "ex", ScooterStatus.MAINTENANCE, currentOutlet = "utara", activeMaintenance = com.evrenhouse.trackscooter.data.ActiveMaintenance("m2", "luar", "Bengkel Pak Budi", "rem", null, "repair", "2026-09-20T10:00:00Z")),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            FleetStatCards(scooters = sampleScooters)
        }
    }
}

@Preview(name = "Repair Queue Row", showBackground = true)
@Composable
private fun RepairQueueRowPreview() {
    val sampleRecord = MaintenanceRecord(
        id = "rec-1",
        scooterId = "SD-57",
        location = "luar",
        locationDetail = "Bengkel Maju",
        issue = "Rem blong dan setelan patah",
        status = "repair",
        startedAt = "2026-09-26T08:00:00Z",
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            RepairQueueRow(rec = sampleRecord, onComplete = {}, completing = false)
        }
    }
}
