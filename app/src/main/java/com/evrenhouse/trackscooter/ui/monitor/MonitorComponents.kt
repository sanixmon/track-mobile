package com.evrenhouse.trackscooter.ui.monitor

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ButtonDefaults
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
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
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
    selectedOutletId: String,
    onOutletSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val isDisconnected = !isLiveConnected && !isReconnecting

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        // Chip outlet sama persis dengan Dashboard/Kelola (karena semua angka di layar ini milik outlet terpilih)
        OutletDropdown(
            selectedOutletId = selectedOutletId,
            onOutletSelected = onOutletSelected,
            getOutletCount = null,
            modifier = Modifier.fillMaxWidth(),
        )
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
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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

            // Durasi sewa besar di kanan
            Text(
                text = durationText,
                color = Accent,
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

/** Satu perjalanan sewa: keluar → masuk (atau → jalan) */
data class TripChip(
    val id: String,
    val keluarTime: String,     // "HH:mm"
    val masukTime: String?,     // "HH:mm" atau null jika masih berjalan
    val masihKeluar: Boolean,
    val durasiSecs: Long,       // 0 jika masih berjalan
)

/** Satu baris unit: semua trip hari ini dirangkum */
data class UnitLogRow(
    val scooterId: String,
    val scooterType: String,
    val trips: List<TripChip>,
    val masihKeluar: Boolean,   // true jika trip terakhir belum kembali
    val totalDurasiSecs: Long,
    val lastEventDt: LocalDateTime,
)

/** ── Section Unit Ready (Pindah ke bawah daftar Sesi Berjalan) ── */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadyUnitsSection(
    readyUnits: List<StandbyUnitInfo>,
    onOpenDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
            Text(
                text = "Tidak ada unit ready saat ini.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            // Tampilkan 5 unit yang paling lama menganggur
            readyUnits.take(5).forEach { u ->
                ReadyUnitRow(
                    unit = u,
                    onClick = { onOpenDetail?.invoke(u.id) },
                )
                HorizontalDivider(color = Border.copy(alpha = 0.35f), thickness = 0.5.dp)
            }

            if (readyUnits.size > 5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSheet = true }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Lihat semua (${readyUnits.size})",
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = Surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "Daftar Unit Ready (${readyUnits.size})",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Diurutkan dari unit yang paling lama menganggur",
                    color = TextMuted,
                    fontSize = 11.5.sp,
                )
                HorizontalDivider(color = Border, thickness = 0.5.dp)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(readyUnits, key = { it.id }) { u ->
                        ReadyUnitRow(
                            unit = u,
                            onClick = {
                                showSheet = false
                                onOpenDetail?.invoke(u.id)
                            },
                        )
                        HorizontalDivider(color = Border.copy(alpha = 0.35f), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun ReadyUnitRow(
    unit: StandbyUnitInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val nameColor = remember(unit.id, isDark) {
        ScooterColors.getScooterNameColor(null, unit.id, null, isDark)
    }
    val greenTextColor = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = unit.id,
                color = nameColor,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isDark) Green.copy(alpha = 0.15f) else Color(0xFFDCFCE7))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = "Ready",
                    color = greenTextColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Text(
            text = "Menganggur ${unit.breakText}",
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

/** ── Activity Feed Panel — format per unit, chip keluar→masuk (1:1 web) ── */
@Composable
fun ActivityFeedPanel(
    activityLog: List<ActivityLogEntry>,
    scooters: List<Scooter>,
    onOpenDetail: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val today = remember { DateUtils.today() }
    val todayStr = remember(today) { DateUtils.localDateKey(today) }

    // Group logs per unit → list TripChip (1:1 web groupSessionsByUnit)
    val unitRows = remember(activityLog, todayStr, scooters) {
        val allowedIds = scooters.map { it.id }.toSet()

        // Collect per-unit, today only
        val perUnit = mutableMapOf<String, MutableList<Pair<ActivityLogEntry, LocalDateTime>>>()
        for (l in activityLog) {
            if (allowedIds.isNotEmpty() && !allowedIds.contains(l.scooterId)) continue
            if (DateUtils.dateKey(l.timestamp) != todayStr) continue
            val dt = DateUtils.parse(l.timestamp) ?: continue
            perUnit.getOrPut(l.scooterId) { mutableListOf() }.add(l to dt)
        }

        val rows = mutableListOf<UnitLogRow>()
        for ((scooterId, logs) in perUnit) {
            logs.sortBy { it.second }
            val scooterType = logs.firstOrNull()?.first?.scooterType ?: ""

            val trips = mutableListOf<TripChip>()
            var pendingCheckout: Pair<ActivityLogEntry, LocalDateTime>? = null
            var totalDurasiSecs = 0L

            for ((entry, dt) in logs) {
                when (entry.action) {
                    "checkout" -> {
                        pendingCheckout = entry to dt
                    }
                    "return" -> {
                        val keluarTime = pendingCheckout?.second?.let { DateUtils.formatTime(it) } ?: "-"
                        val masukTime = DateUtils.formatTime(dt)
                        val durasi = if (pendingCheckout != null)
                            Duration.between(pendingCheckout!!.second, dt).seconds.coerceAtLeast(0)
                        else 0L
                        totalDurasiSecs += durasi
                        trips.add(TripChip(
                            id = entry.id,
                            keluarTime = keluarTime,
                            masukTime = masukTime,
                            masihKeluar = false,
                            durasiSecs = durasi,
                        ))
                        pendingCheckout = null
                    }
                }
            }
            // Trip yang masih berjalan (checkout tanpa return)
            val stillOut = pendingCheckout
            if (stillOut != null) {
                trips.add(TripChip(
                    id = stillOut.first.id,
                    keluarTime = DateUtils.formatTime(stillOut.second),
                    masukTime = null,
                    masihKeluar = true,
                    durasiSecs = 0L,
                ))
            }

            if (trips.isEmpty()) continue
            val masihKeluar = trips.last().masihKeluar
            val lastDt = logs.maxOf { it.second }
            rows.add(UnitLogRow(
                scooterId = scooterId,
                scooterType = scooterType,
                trips = trips,
                masihKeluar = masihKeluar,
                totalDurasiSecs = totalDurasiSecs,
                lastEventDt = lastDt,
            ))
        }
        // Berjalan dulu, lalu urut terbaru
        rows.sortWith(compareByDescending<UnitLogRow> { it.masihKeluar }.thenByDescending { it.lastEventDt })
    }

    // Filter: semua | keluar (masih berjalan) | kembali (sudah selesai)
    var selectedFilter by rememberSaveable { mutableStateOf("all") }
    val filteredRows = remember(unitRows, selectedFilter) {
        when (selectedFilter) {
            "keluar"  -> unitRows.filter { it.masihKeluar }
            "kembali" -> unitRows.filter { !it.masihKeluar }
            else      -> unitRows
        }
    }

    val isDark = LocalThemeIsDark.current
    val amberText = if (isDark) Warning else Color(0xFFB45309)
    val greenText = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // ── Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "AKTIVITAS TERBARU HARI INI",
                color = TextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
            Text(
                text = "${unitRows.size} aktivitas",
                color = TextMuted,
                fontSize = 11.sp,
            )
        }

        // ── Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ActivityFilterChip("Semua", unitRows.size,        selectedFilter == "all",     { selectedFilter = "all" })
            ActivityFilterChip("Berjalan", unitRows.count { it.masihKeluar },  selectedFilter == "keluar",   { selectedFilter = "keluar"  }, activeColor = amberText)
            ActivityFilterChip("Selesai",  unitRows.count { !it.masihKeluar }, selectedFilter == "kembali",  { selectedFilter = "kembali" }, activeColor = greenText)
        }

        HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)

        if (filteredRows.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Tidak ada aktivitas untuk filter ini.",
                    color = TextMuted, fontSize = 12.sp,
                )
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                filteredRows.forEach { row ->
                    val nameColor = remember(row.scooterType, row.scooterId, isDark) {
                        ScooterColors.getScooterNameColor(row.scooterType, row.scooterId, null, isDark)
                    }
                    val statusColor = if (row.masihKeluar) amberText else greenText
                    val statusLabel = if (row.masihKeluar) "Berjalan" else "Selesai"
                    val totalLabel = if (row.totalDurasiSecs > 0)
                        DateUtils.formatDuration(row.totalDurasiSecs) else null
                    val tripCount = row.trips.size

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenDetail?.invoke(row.scooterId) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Kiri: ID + badge trip count + chips keluar→masuk
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            // Baris 1: ID + badge "2x keluar"
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = row.scooterId,
                                    color = nameColor,
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                                // Badge jumlah trip
                                Box(
                                    modifier = Modifier
                                        .background(AccentSubtle, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.5.dp),
                                ) {
                                    Text(
                                        text = "${tripCount}x keluar",
                                        color = Accent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                // Badge status (Berjalan / Selesai)
                                Box(
                                    modifier = Modifier
                                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.5.dp),
                                ) {
                                    Text(
                                        text = statusLabel,
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            // Baris 2: chips trip  HH:mm→HH:mm  •  HH:mm→jalan
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                row.trips.forEachIndexed { idx, trip ->
                                    if (idx > 0) {
                                        Text("•", color = TextSubtle, fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold)
                                    }
                                    // Chip: "HH:mm→HH:mm" atau "HH:mm→jalan"
                                    val chipText = "${trip.keluarTime}→${if (trip.masihKeluar) "jalan" else (trip.masukTime ?: "-")}"
                                    val chipBg   = if (trip.masihKeluar) amberText.copy(alpha = 0.10f) else Surface2
                                    val chipBorder = if (trip.masihKeluar) amberText.copy(alpha = 0.30f) else Border
                                    val chipText2 = if (trip.masihKeluar) amberText else TextSubtle
                                    Box(
                                        modifier = Modifier
                                            .background(chipBg, RoundedCornerShape(5.dp))
                                            .border(0.5.dp, chipBorder, RoundedCornerShape(5.dp))
                                            .padding(horizontal = 7.dp, vertical = 3.dp),
                                    ) {
                                        Text(
                                            text = chipText,
                                            color = chipText2,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    }
                                }
                            }
                        }

                        // Kanan: total durasi
                        if (totalLabel != null) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .background(Surface2, RoundedCornerShape(6.dp))
                                    .border(0.5.dp, Border, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = totalLabel,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Border.copy(alpha = 0.35f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun ActivityFilterChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = Accent,
) {
    val isDark = LocalThemeIsDark.current
    val bg = if (selected) activeColor.copy(alpha = if (isDark) 0.18f else 0.12f) else Surface2
    val border = if (selected) activeColor else Border
    val text = if (selected) activeColor else TextMuted

    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = label,
            color = text,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .background(if (selected) activeColor.copy(alpha = 0.2f) else Surface3, RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp),
        ) {
            Text(
                text = "$count",
                color = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
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
        StandbyUnitInfo("EX-04", "59 mnt", 3540L, true),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            ReadyUnitsSection(readyUnits = sampleReady)
        }
    }
}
