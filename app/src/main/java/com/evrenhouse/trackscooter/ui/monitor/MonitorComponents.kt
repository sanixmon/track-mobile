package com.evrenhouse.trackscooter.ui.monitor

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.SouthWest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.evrenhouse.trackscooter.data.ScooterStatus
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
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.TypeLabels
import java.time.Duration
import java.time.LocalDateTime

enum class MonitorTab(val label: String, val icon: ImageVector) {
    LIVE_SESSION("Sesi Berjalan", Icons.Filled.DirectionsBike),
    ACTIVITY("Aktivitas Terbaru", Icons.Filled.History),
}

/** ── Header with smart live pulse (1:1 with web MonitorPage.jsx) ── */
@Composable
fun LivePulseHeader(
    isLiveConnected: Boolean,
    isReconnecting: Boolean,
    outletName: String? = null,
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

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Pemantauan",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            // Live status badge (1:1 with web)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(
                        color = when {
                            isReconnecting -> Warning.copy(alpha = 0.12f)
                            isLiveConnected -> Green.copy(alpha = 0.12f)
                            else -> Red.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(20.dp)
                    )
                    .border(
                        1.dp,
                        when {
                            isReconnecting -> Warning.copy(alpha = 0.3f)
                            isLiveConnected -> Green.copy(alpha = 0.3f)
                            else -> Red.copy(alpha = 0.3f)
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = when {
                                isReconnecting -> Warning.copy(alpha = pulseAlpha)
                                isLiveConnected -> Green.copy(alpha = pulseAlpha)
                                else -> Red.copy(alpha = pulseAlpha)
                            },
                            shape = CircleShape,
                        )
                )
                Text(
                    text = when {
                        isReconnecting -> "Menghubungkan..."
                        isLiveConnected -> "Terhubung real-time"
                        else -> "Koneksi terputus"
                    },
                    color = when {
                        isReconnecting -> Warning
                        isLiveConnected -> Green
                        else -> Red
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = if (outletName.isNullOrBlank() || outletName == "Semua Outlet") {
                "Pantau armada yang sedang digunakan pelanggan secara real-time."
            } else {
                "Pantau armada $outletName secara real-time."
            },
            color = TextMuted,
            fontSize = 12.sp,
        )
    }
}

/** ── Tab Selector (1:1 with web MonitorPage.jsx) ── */
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
                MonitorTab.ACTIVITY -> recentCount
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
                if (count > 0) {
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.25f)
                                else if (tab == MonitorTab.LIVE_SESSION) Warning.copy(alpha = 0.2f)
                                else Surface3,
                                CircleShape,
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = count.toString(),
                            color = if (isSelected) Color.White
                            else if (tab == MonitorTab.LIVE_SESSION) Warning
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

/** ── Live Session Card (1:1 with web LiveSessionCard.jsx) ── */
@Composable
fun LiveSessionCard(
    scooter: Scooter,
    nowMillis: Long,
    onClick: (() -> Unit)? = null,
    onTroubleSwap: ((Scooter) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val dt = DateUtils.parse(scooter.lastUpdated)
    val startMillis = dt?.atZone(DateUtils.WIB)?.toInstant()?.toEpochMilli() ?: nowMillis
    val elapsedSecs = ((nowMillis - startMillis) / 1000).coerceAtLeast(0)
    val isOverHour = elapsedSecs >= 3600
    val durationText = DateUtils.formatDuration(elapsedSecs)
    val keluarTime = if (dt != null) DateUtils.formatTime(dt) else "-"

    val infiniteTransition = rememberInfiniteTransition(label = "ping")
    val pingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pingAlpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Header: Nomer ID & Badge Disewa (1:1 web)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = scooter.id,
                color = nameColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                modifier = Modifier.weight(1f, fill = false),
            )

            // Badge Disewa (Amber with glowing dot)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .background(Warning.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                    .border(1.dp, Warning.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Warning.copy(alpha = pingAlpha), CircleShape)
                )
                Text(
                    text = "Disewa",
                    color = Warning,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Info: Jam Keluar & Durasi Berjalan (1:1 web)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface2, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.AccessTime, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(11.dp))
                    Text(
                        text = "KELUAR",
                        color = TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = keluarTime,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Timer, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(11.dp))
                    Text(
                        text = "DURASI",
                        color = TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = durationText,
                    color = if (isOverHour) Red else Accent,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Action Footer: Tukar Unit Button (1:1 web)
        if (onTroubleSwap != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Surface3)
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                    .clickable { onTroubleSwap(scooter) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tukar Unit",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** ── Live Session Empty State (1:1 with web) ── */
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
            text = "Tidak Ada Sesi Berjalan",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Semua armada saat ini berada di outlet dan siap untuk disewa.",
            color = TextMuted,
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

data class StandbyUnitInfo(
    val id: String,
    val breakText: String,
    val breakSeconds: Long,
    val isReady: Boolean,
    val remainingText: String? = null
)

data class UnifiedLogItem(
    val id: String,
    val action: String, // "checkout" | "return"
    val scooterId: String,
    val scooterType: String,
    val dt: LocalDateTime,
    val waktu: String,
    val durationOrBreak: String?
)

/** ── Activity Feed Panel (1:1 with web ActivityFeedPanel.jsx) ── */
@Composable
fun ActivityFeedPanel(
    activityLog: List<ActivityLogEntry>,
    scooters: List<Scooter>,
    onOpenDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val today = remember { DateUtils.today() }

    // Standby units with break duration (longest resting first)
    val standbyUnits = remember(scooters, activityLog) {
        val available = scooters.filter { it.status == ScooterStatus.AVAILABLE }
        val now = LocalDateTime.now(DateUtils.WIB)

        available.mapNotNull { s ->
            val logs = activityLog.filter { it.scooterId == s.id }
                .mapNotNull { l ->
                    val dt = DateUtils.parse(l.timestamp)
                    if (dt != null) l to dt else null
                }.sortedBy { it.second }

            val lastReturn = logs.lastOrNull { it.first.action == "return" }
            val baseTime = lastReturn?.second ?: DateUtils.parse(s.lastUpdated)

            if (baseTime != null) {
                val diffSecs = Duration.between(baseTime, now).seconds.coerceAtLeast(0)
                val isReady = diffSecs >= 900 // 15 mins
                val remainingSecs = (900 - diffSecs).coerceAtLeast(0)
                StandbyUnitInfo(
                    id = s.id,
                    breakText = DateUtils.formatDuration(diffSecs),
                    breakSeconds = diffSecs,
                    isReady = isReady,
                    remainingText = if (!isReady) DateUtils.formatDuration(remainingSecs) else null
                )
            } else null
        }.sortedByDescending { it.breakSeconds }
    }

    // Unified logs for today
    val unifiedLogs = remember(activityLog, today) {
        val perUnit = mutableMapOf<String, MutableList<Pair<ActivityLogEntry, LocalDateTime>>>()
        for (l in activityLog) {
            val dt = DateUtils.parse(l.timestamp) ?: continue
            perUnit.getOrPut(l.scooterId) { mutableListOf() }.add(l to dt)
        }

        val list = mutableListOf<UnifiedLogItem>()
        val todayStr = DateUtils.localDateKey(today)

        val allowedScooterIds = scooters.map { it.id }.toSet()
        for ((scooterId, logs) in perUnit) {
            if (allowedScooterIds.isNotEmpty() && !allowedScooterIds.contains(scooterId)) continue
            logs.sortBy { it.second }
            var lastReturnDt: LocalDateTime? = null
            var lastCheckoutDt: LocalDateTime? = null

            for ((entry, dt) in logs) {
                val isToday = DateUtils.dateKey(entry.timestamp) == todayStr

                if (entry.action == "checkout") {
                    var jedaText: String? = null
                    if (lastReturnDt != null) {
                        val diff = Duration.between(lastReturnDt, dt).seconds.coerceAtLeast(0)
                        jedaText = "Jeda ${DateUtils.formatDuration(diff)}"
                    }
                    lastCheckoutDt = dt
                    lastReturnDt = null

                    if (isToday) {
                        list.add(
                            UnifiedLogItem(
                                id = entry.id,
                                action = "checkout",
                                scooterId = scooterId,
                                scooterType = entry.scooterType,
                                dt = dt,
                                waktu = DateUtils.formatTime(dt),
                                durationOrBreak = jedaText
                            )
                        )
                    }
                } else if (entry.action == "return") {
                    var durasiText: String? = null
                    if (lastCheckoutDt != null) {
                        val diff = Duration.between(lastCheckoutDt, dt).seconds.coerceAtLeast(0)
                        durasiText = "Durasi ${DateUtils.formatDuration(diff)}"
                    }
                    lastReturnDt = dt
                    lastCheckoutDt = null

                    if (isToday) {
                        list.add(
                            UnifiedLogItem(
                                id = entry.id,
                                action = "return",
                                scooterId = scooterId,
                                scooterType = entry.scooterType,
                                dt = dt,
                                waktu = DateUtils.formatTime(dt),
                                durationOrBreak = durasiText
                            )
                        )
                    }
                }
            }
        }
        list.sortedByDescending { it.dt }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Standby Units with Longest Rest (Ready: Jeda >= 15 mnt) (1:1 with web)
        if (standbyUnits.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface, RoundedCornerShape(14.dp))
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Coffee, contentDescription = null, tint = Green, modifier = Modifier.size(15.dp))
                        Text(
                            text = "UNIT STANDBY",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text("${standbyUnits.size} unit", color = TextMuted, fontSize = 11.sp)
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(standbyUnits, key = { it.id }) { u ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (u.isReady) Green.copy(alpha = 0.1f) else Warning.copy(alpha = 0.1f))
                                .border(1.dp, if (u.isReady) Green.copy(alpha = 0.3f) else Warning.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .let { if (onOpenDetail != null) it.clickable { onOpenDetail(u.id) } else it }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            val isDark = LocalThemeIsDark.current
                            val nameColor = ScooterColors.getScooterNameColor(null, u.id, isDark = isDark)
                            Text(u.id, color = nameColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Text(
                                text = if (u.isReady) "Jeda ${u.breakText}" else "Jeda ${u.breakText} (sisa ${u.remainingText})",
                                color = if (u.isReady) Green else Warning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Unified Activity Feed for Today
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface, RoundedCornerShape(14.dp))
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AKTIVITAS TERBARU HARI INI",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text("${unifiedLogs.size} aktivitas", color = TextMuted, fontSize = 11.sp)
            }

            if (unifiedLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada aktivitas transaksi sewa hari ini.", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    unifiedLogs.forEachIndexed { index, item ->
                        val isCheckout = item.action == "checkout"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface2)
                                .let { if (onOpenDetail != null) it.clickable { onOpenDetail(item.scooterId) } else it }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (isCheckout) Warning.copy(alpha = 0.15f) else Green.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCheckout) Icons.Filled.NorthEast else Icons.Filled.SouthWest,
                                        contentDescription = null,
                                        tint = if (isCheckout) Warning else Green,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val isDark = LocalThemeIsDark.current
                                        val nameColor = ScooterColors.getScooterNameColor(item.scooterType, item.scooterId, isDark = isDark)
                                        Text(item.scooterId, color = nameColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(
                                        text = if (isCheckout) "Keluar sewa" else "Selesai / Kembali",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = item.waktu,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (item.durationOrBreak != null) {
                                    Text(
                                        text = item.durationOrBreak,
                                        color = if (isCheckout) TextMuted else Accent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
