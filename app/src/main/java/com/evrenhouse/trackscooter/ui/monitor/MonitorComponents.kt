package com.evrenhouse.trackscooter.ui.monitor

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.AccentSubtle
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.ScooterColors
import java.time.Duration
import java.time.LocalDateTime

enum class MonitorTab(val label: String, val icon: ImageVector) {
    LIVE_SESSION("Sesi Berjalan", Icons.Filled.DirectionsBike),
    ACTIVITY("Aktivitas Terbaru", Icons.Filled.History),
}

/** ── Header with Outlet Selector & slim real-time indicator ── */
@Composable
fun LivePulseHeader(
    isLiveConnected: Boolean,
    isReconnecting: Boolean,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val isDisconnected = !isLiveConnected && !isReconnecting

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
            Text(
                text = "Monitor",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )

            // Indikator status real-time ramping
            if (isDisconnected) {
                // Warning pill merah jika koneksi terputus
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .background(Red.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                        .border(1.dp, Red.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Box(modifier = Modifier.size(6.dp).background(Red, CircleShape))
                    Text(
                        text = "Koneksi terputus",
                        color = Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            } else {
                // Titik hijau/amber kecil + teks tanpa pill tebal
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isLiveConnected) Green else Warning, CircleShape),
                    )
                    Text(
                        text = if (isReconnecting) "Menghubungkan..." else "Terhubung real-time",
                        color = if (isReconnecting) (if (isDark) Color(0xFFFBBF24) else Color(0xFF8A5300))
                        else (if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
    }
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
                                else Surface3,
                                CircleShape,
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = count.toString(),
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

/** ── Live Session Card: Padat ~115dp, ID netral onSurface, durasi besar di kanan, badge biru Disewa ── */
@Composable
fun LiveSessionCard(
    scooter: Scooter,
    nowMillis: Long,
    onClick: (() -> Unit)? = null,
    onTroubleSwap: ((Scooter) -> Unit)? = null,
    onReturn: ((Scooter) -> Unit)? = null,
    isProcessing: Boolean = false,
    isPendingOffline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val dt = DateUtils.parse(scooter.lastUpdated)
    val startMillis = dt?.atZone(DateUtils.WIB)?.toInstant()?.toEpochMilli() ?: nowMillis
    val elapsedSecs = ((nowMillis - startMillis) / 1000).coerceAtLeast(0)
    val durationText = DateUtils.formatDuration(elapsedSecs)
    val keluarTime = if (dt != null) DateUtils.formatTime(dt) else "-"

    val isDark = LocalThemeIsDark.current
    val nameColor = remember(scooter.type, scooter.id, scooter.currentOutlet, isDark) {
        ScooterColors.getScooterNameColor(scooter.type, scooter.id, scooter.currentOutlet, isDark)
    }
    val blueTextColor = if (isDark) Color(0xFF818CF8) else Color(0xFF1E40AF)
    val blueBgColor = if (isDark) Accent.copy(alpha = 0.18f) else Color(0xFFEFF6FF)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(com.evrenhouse.trackscooter.ui.theme.CardCornerRadius))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(com.evrenhouse.trackscooter.ui.theme.CardCornerRadius))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Baris 1: ID di kiri (onSurface, monospace), durasi sewa besar di kanan
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
                    color = nameColor,
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                )
                // Badge Disewa (Biru, bukan amber)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(blueBgColor)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "Disewa",
                        color = blueTextColor,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            // Durasi sewa besar di kanan (merah jika >= 1 jam, 1:1 web app)
            val isOverHour = elapsedSecs >= 3600
            Text(
                text = durationText,
                color = if (isOverHour) com.evrenhouse.trackscooter.ui.theme.Red else Accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }

        // Baris 2 & 3: Keluar jam di kiri & Tombol Tukar Unit kompak di kanan
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.AccessTime,
                    contentDescription = null,
                    tint = TextSubtle,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "Keluar $keluarTime",
                    color = TextSubtle,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (isPendingOffline) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(com.evrenhouse.trackscooter.ui.theme.Warning.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null,
                                tint = com.evrenhouse.trackscooter.ui.theme.Warning,
                                modifier = Modifier.size(11.dp),
                            )
                            Text(
                                text = "Menunggu Sinyal",
                                color = com.evrenhouse.trackscooter.ui.theme.Warning,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                if (onReturn != null) {
                    Button(
                        onClick = { onReturn(scooter) },
                        enabled = !isProcessing,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF065F46) else Color(0xFFD1FAE5),
                            contentColor = if (isDark) Color(0xFF34D399) else Color(0xFF047857),
                            disabledContainerColor = Surface2,
                            disabledContentColor = TextMuted,
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp),
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(13.dp),
                                strokeWidth = 2.dp,
                                color = TextMuted,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Kembalikan unit sewa",
                                modifier = Modifier.size(13.dp),
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (isProcessing) "Proses..." else "Kembalikan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                if (onTroubleSwap != null) {
                    OutlinedButton(
                        onClick = { onTroubleSwap(scooter) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Border),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SwapHoriz,
                            contentDescription = "Tukar unit sewa",
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Tukar Unit",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

/** ── Empty State Sesi Berjalan ── */
@Composable
fun LiveSessionEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Surface2, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.DirectionsBike,
                contentDescription = null,
                tint = TextSubtle,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = "Tidak ada sesi berjalan",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Semua armada sedang ready atau dalam perbaikan.",
            color = TextMuted,
            fontSize = 11.5.sp,
        )
    }
}

data class StandbyUnitInfo(
    val id: String,
    val breakText: String,
    val breakSeconds: Long,
    val isReady: Boolean,
    val remainingText: String? = null,
)

/** Satu sesi sewa lengkap: checkout (keluar) → return (masuk/selesai) atau masih berjalan */
data class RentalTripSession(
    val id: String,
    val scooterId: String,
    val scooterType: String,
    val keluarDt: LocalDateTime,
    val masukDt: LocalDateTime?,
    val keluarTimeStr: String,
    val masukTimeStr: String?,
    val keluarFullTimeStr: String,
    val masukFullTimeStr: String?,
    val durasiSecs: Long,
    val isOngoing: Boolean,
    val lastEventDt: LocalDateTime,
)

object RentalTripUtils {
    fun buildSessions(
        activityLog: List<ActivityLogEntry>,
        scooters: List<Scooter>?,
        todayStr: String = DateUtils.localDateKey(DateUtils.today()),
    ): List<RentalTripSession> {
        val allowedIds = scooters?.map { it.id }?.toSet() ?: emptySet()
        val perUnit = mutableMapOf<String, MutableList<Pair<ActivityLogEntry, LocalDateTime>>>()
        for (l in activityLog) {
            if (allowedIds.isNotEmpty() && !allowedIds.contains(l.scooterId)) continue
            if (DateUtils.dateKey(l.timestamp) != todayStr) continue
            val dt = DateUtils.parse(l.timestamp) ?: continue
            perUnit.getOrPut(l.scooterId) { mutableListOf() }.add(l to dt)
        }

        val sessions = mutableListOf<RentalTripSession>()
        val nowWib = LocalDateTime.now(DateUtils.WIB)

        for ((scooterId, logs) in perUnit) {
            logs.sortBy { it.second }
            val scooterType = logs.firstOrNull()?.first?.scooterType ?: ""
            var pendingCheckout: Pair<ActivityLogEntry, LocalDateTime>? = null

            for ((entry, dt) in logs) {
                when (entry.action) {
                    "checkout" -> {
                        if (pendingCheckout != null) {
                            val durasi = Duration.between(pendingCheckout.second, dt).seconds.coerceAtLeast(0)
                            sessions.add(
                                RentalTripSession(
                                    id = pendingCheckout.first.id,
                                    scooterId = scooterId,
                                    scooterType = scooterType,
                                    keluarDt = pendingCheckout.second,
                                    masukDt = null,
                                    keluarTimeStr = DateUtils.formatTime(pendingCheckout.second),
                                    masukTimeStr = null,
                                    keluarFullTimeStr = DateUtils.formatTimeSec(pendingCheckout.second),
                                    masukFullTimeStr = null,
                                    durasiSecs = durasi,
                                    isOngoing = true,
                                    lastEventDt = pendingCheckout.second,
                                )
                            )
                        }
                        pendingCheckout = entry to dt
                    }
                    "return" -> {
                        if (pendingCheckout != null) {
                            val keluarTime = pendingCheckout.second
                            val durasi = Duration.between(keluarTime, dt).seconds.coerceAtLeast(0)
                            sessions.add(
                                RentalTripSession(
                                    id = "${pendingCheckout.first.id}_${entry.id}",
                                    scooterId = scooterId,
                                    scooterType = scooterType,
                                    keluarDt = keluarTime,
                                    masukDt = dt,
                                    keluarTimeStr = DateUtils.formatTime(keluarTime),
                                    masukTimeStr = DateUtils.formatTime(dt),
                                    keluarFullTimeStr = DateUtils.formatTimeSec(keluarTime),
                                    masukFullTimeStr = DateUtils.formatTimeSec(dt),
                                    durasiSecs = durasi,
                                    isOngoing = false,
                                    lastEventDt = dt,
                                )
                            )
                            pendingCheckout = null
                        } else {
                            sessions.add(
                                RentalTripSession(
                                    id = entry.id,
                                    scooterId = scooterId,
                                    scooterType = scooterType,
                                    keluarDt = dt,
                                    masukDt = dt,
                                    keluarTimeStr = DateUtils.formatTime(dt),
                                    masukTimeStr = DateUtils.formatTime(dt),
                                    keluarFullTimeStr = DateUtils.formatTimeSec(dt),
                                    masukFullTimeStr = DateUtils.formatTimeSec(dt),
                                    durasiSecs = 0L,
                                    isOngoing = false,
                                    lastEventDt = dt,
                                )
                            )
                        }
                    }
                }
            }

            if (pendingCheckout != null) {
                val durasi = Duration.between(pendingCheckout.second, nowWib).seconds.coerceAtLeast(0)
                sessions.add(
                    RentalTripSession(
                        id = pendingCheckout.first.id,
                        scooterId = scooterId,
                        scooterType = scooterType,
                        keluarDt = pendingCheckout.second,
                        masukDt = null,
                        keluarTimeStr = DateUtils.formatTime(pendingCheckout.second),
                        masukTimeStr = null,
                        keluarFullTimeStr = DateUtils.formatTimeSec(pendingCheckout.second),
                        masukFullTimeStr = null,
                        durasiSecs = durasi,
                        isOngoing = true,
                        lastEventDt = pendingCheckout.second,
                    )
                )
            }
        }

        sessions.sortByDescending { it.lastEventDt }
        return sessions
    }
}


/** ── Section Unit Ready (Grid 3 Kolom) ── */
@Composable
fun ReadyUnitsSection(
    readyUnits: List<StandbyUnitInfo>,
    onOpenDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Green, CircleShape),
                )
                Text(
                    text = "Ready (${readyUnits.size})",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)

        if (readyUnits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Tidak ada unit ready saat ini.",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }
        } else {
            val rows = remember(readyUnits) { readyUnits.chunked(3) }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rows.forEach { rowUnits ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        rowUnits.forEach { u ->
                            ReadyUnitGridCard(
                                unit = u,
                                onClick = { onOpenDetail?.invoke(u.id) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        repeat(3 - rowUnits.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

/**
 * Kartu Grid Unit Ready (3 Kolom).
 * Warna kartu dinamis berdasarkan waktu menganggur:
 * - Baru dipakai (< 15 menit): Latar & border Merah (Red)
 * - Siap sewa / istirahat cukup (>= 15 menit): Latar & border Hijau (Green)
 */
@Composable
fun ReadyUnitGridCard(
    unit: StandbyUnitInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val nameColor = remember(unit.id, isDark) {
        ScooterColors.getScooterNameColor(null, unit.id, null, isDark)
    }

    val isCooledDown = unit.breakSeconds >= 900L || unit.isReady
    val cardBg = if (isCooledDown) {
        if (isDark) Green.copy(alpha = 0.14f) else Color(0xFFDCFCE7)
    } else {
        if (isDark) Red.copy(alpha = 0.16f) else Color(0xFFFEE2E2)
    }
    val cardBorder = if (isCooledDown) {
        if (isDark) Green.copy(alpha = 0.40f) else Color(0xFF86EFAC)
    } else {
        if (isDark) Red.copy(alpha = 0.45f) else Color(0xFFFCA5A5)
    }
    val statusTextColor = if (isCooledDown) {
        if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
    } else {
        if (isDark) Red else Color(0xFFB91C1C)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = unit.id,
            color = nameColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
        )

        Text(
            text = if (unit.breakText.isBlank() || unit.breakText == "-") "Ready" else unit.breakText,
            color = statusTextColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
        )
    }
}

/**
 * Kartu Sesi Sewa Accordion (Single-Open).
 * Menampilkan ringkasan unit & waktu pada posisi tertutup, dan timeline mini 2 titik pada posisi terbuka.
 */
@Composable
fun TripSessionCard(
    session: RentalTripSession,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onOpenDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val amberColor = if (isDark) Warning else Color(0xFFD97706)
    val greenColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF16A34A)
    val nameColor = remember(session.scooterType, session.scooterId, isDark) {
        ScooterColors.getScooterNameColor(session.scooterType, session.scooterId, null, isDark)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggleExpand)
            .animateContentSize(animationSpec = tween(220))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Baris Header: Nama Unit + Ringkasan Jam + Badge Status Pill Minimalis + Chevron
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = session.scooterId,
                    color = nameColor,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                )
                val summaryText = if (session.isOngoing) {
                    "${session.keluarTimeStr} → jalan"
                } else {
                    val durasiText = DateUtils.formatDuration(session.durasiSecs)
                    "${session.keluarTimeStr} → ${session.masukTimeStr ?: "-"} • $durasiText"
                }
                Text(
                    text = summaryText,
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val badgeBg = if (session.isOngoing) {
                    amberColor.copy(alpha = if (isDark) 0.18f else 0.12f)
                } else {
                    greenColor.copy(alpha = if (isDark) 0.18f else 0.12f)
                }
                val badgeText = if (session.isOngoing) amberColor else greenColor
                val badgeLabel = if (session.isOngoing) "Berjalan" else "Selesai"

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp),
                ) {
                    Text(
                        text = badgeLabel,
                        color = badgeText,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Tutup rincian" else "Buka rincian",
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Panel Detail Accordion (Expanded State)
        if (isExpanded) {
            HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Timeline Mini 2 Titik Vertikal
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    // Titik 1: Waktu Keluar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(20.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(8.dp)
                                    .background(amberColor, CircleShape),
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height(26.dp)
                                    .background(Border),
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 1.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Waktu Keluar",
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "${session.keluarFullTimeStr} WIB",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }

                    // Titik 2: Waktu Kembali
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(20.dp),
                        ) {
                            if (session.isOngoing) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(8.dp)
                                        .border(1.5.dp, amberColor, CircleShape),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(8.dp)
                                        .background(greenColor, CircleShape),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 0.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Waktu Kembali",
                                color = TextMuted,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            val kembaliText = if (session.isOngoing) "Sedang berjalan" else "${session.masukFullTimeStr ?: "-"} WIB"
                            val kembaliColor = if (session.isOngoing) amberColor else TextPrimary
                            Text(
                                text = kembaliText,
                                color = kembaliColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = if (session.isOngoing) FontFamily.Default else FontFamily.Monospace,
                            )
                        }
                    }
                }

                // Total Durasi
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface2, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Total Durasi",
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    val durasiLabel = if (session.isOngoing) "Sedang berjalan" else DateUtils.formatDuration(session.durasiSecs)
                    Text(
                        text = durasiLabel,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    )
                }

                // Tombol Aksi: Buka Detail Skuter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Accent.copy(alpha = if (isDark) 0.16f else 0.10f))
                        .border(1.dp, Accent.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                        .clickable { onOpenDetail?.invoke(session.scooterId) }
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Buka Detail Skuter ${session.scooterId}",
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/**
 * Activity Feed Panel — Kronologis Sesi Sewa (Paired Trip Session).
 * Bersih tanpa filter/search/header tambahan, diurutkan murni berdasarkan aktivitas terbaru.
 */
@Composable
fun ActivityFeedPanel(
    activityLog: List<ActivityLogEntry>,
    scooters: List<Scooter>,
    onOpenDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val today = remember { DateUtils.today() }
    val todayStr = remember(today) { DateUtils.localDateKey(today) }

    val tripSessions: List<RentalTripSession> = remember(activityLog, todayStr, scooters) {
        RentalTripUtils.buildSessions(activityLog, scooters, todayStr)
    }

    var expandedSessionId by rememberSaveable { mutableStateOf<String?>(null) }

    if (tripSessions.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Belum ada aktivitas sewa hari ini",
                color = TextMuted,
                fontSize = 13.sp,
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(tripSessions, key = { it.id }) { session ->
                TripSessionCard(
                    session = session,
                    isExpanded = expandedSessionId == session.id,
                    onToggleExpand = {
                        expandedSessionId = if (expandedSessionId == session.id) null else session.id
                    },
                    onOpenDetail = onOpenDetail,
                )
            }
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "Live Session Card", showBackground = true)
@Composable
private fun LiveSessionCardPreview() {
    val sampleScooter = Scooter(
        id = "SB-01",
        type = "sb",
        status = "in-use",
        lastUpdated = "2026-10-01T11:28:00Z",
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            LiveSessionCard(
                scooter = sampleScooter,
                nowMillis = System.currentTimeMillis(),
                onTroubleSwap = {},
            )
        }
    }
}

@Preview(name = "Ready Units Section", showBackground = true)
@Composable
private fun ReadyUnitsSectionPreview() {
    val sampleReady = listOf(
        StandbyUnitInfo("SB-02", "7 hari 22 j", 684000L, true),
        StandbyUnitInfo("FZ-10", "1 j 39 mnt", 5940L, true),
        StandbyUnitInfo("EX-04", "5 mnt", 300L, false),
        StandbyUnitInfo("SD-01", "12 mnt", 720L, false),
        StandbyUnitInfo("SM-03", "45 mnt", 2700L, true),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            ReadyUnitsSection(readyUnits = sampleReady)
        }
    }
}

@Preview(name = "Trip Session Card", showBackground = true)
@Composable
private fun TripSessionCardPreview() {
    val sampleSession = RentalTripSession(
        id = "demo-1",
        scooterId = "SD-02",
        scooterType = "sd",
        keluarDt = LocalDateTime.now().minusMinutes(45),
        masukDt = LocalDateTime.now(),
        keluarTimeStr = "14:15",
        masukTimeStr = "15:00",
        keluarFullTimeStr = "14:15:22",
        masukFullTimeStr = "15:00:10",
        durasiSecs = 2700L,
        isOngoing = false,
        lastEventDt = LocalDateTime.now(),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            TripSessionCard(
                session = sampleSession,
                isExpanded = true,
                onToggleExpand = {},
                onOpenDetail = {},
            )
        }
    }
}
