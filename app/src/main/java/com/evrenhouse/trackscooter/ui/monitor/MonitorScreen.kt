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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import java.time.Duration
import java.time.LocalDateTime
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.MonitorSkeleton
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.DateUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: ((String) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = 0) { MonitorTab.entries.size }
    val currentTab by remember { derivedStateOf { MonitorTab.entries[pagerState.currentPage] } }
    var troubleScooter by remember { mutableStateOf<Scooter?>(null) }
    var returnConfirmScooter by remember { mutableStateOf<Scooter?>(null) }
    val processingReturnIds by viewModel.processingReturnIds.collectAsState()
    val pendingOfflineReturnIds by viewModel.pendingOfflineReturnIds.collectAsState()
    var sortOrder by rememberSaveable { mutableStateOf("oldest") } // Default terlama dulu

    // 5-second ticker keeps rental duration updated (1:1 with web)
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            nowMillis = System.currentTimeMillis()
        }
    }

    val todayStr = remember { DateUtils.localDateKey(DateUtils.today()) }
    val globalOutlet by viewModel.selectedOutlet.collectAsState()

    // Filter scooters by global outlet
    val outletFilteredScooters by remember(state.scooters, globalOutlet) {
        derivedStateOf {
            if (globalOutlet == "all") state.scooters
            else state.scooters.filter {
                (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == globalOutlet
            }
        }
    }

    // Filter activity log by global outlet
    val outletFilteredActivityLog by remember(state.activityLog, outletFilteredScooters, globalOutlet) {
        derivedStateOf {
            if (globalOutlet == "all") state.activityLog
            else {
                val validIds = outletFilteredScooters.map { it.id }.toSet()
                state.activityLog.filter { validIds.contains(it.scooterId) }
            }
        }
    }

    val todayLogsCount by remember(outletFilteredActivityLog, todayStr) {
        derivedStateOf {
            outletFilteredActivityLog.count { DateUtils.dateKey(it.timestamp) == todayStr }
        }
    }

    // In-use scooters scoped to outlet
    val inUseScooters by remember(outletFilteredScooters, sortOrder) {
        derivedStateOf {
            val list = outletFilteredScooters.filter { it.status == ScooterStatus.IN_USE }
            if (sortOrder == "newest") {
                list.sortedByDescending { it.lastUpdated }
            } else {
                list.sortedBy { it.lastUpdated }
            }
        }
    }

    // Unit Ready (terlama menganggur dihitung dari return/update terakhir)
    val readyUnits = remember(outletFilteredScooters, outletFilteredActivityLog) {
        val available = outletFilteredScooters.filter { it.status == ScooterStatus.AVAILABLE }
        val now = LocalDateTime.now(DateUtils.WIB)
        val logsByScooter = outletFilteredActivityLog.groupBy { it.scooterId }

        available.map { s ->
            val logs = (logsByScooter[s.id] ?: emptyList())
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
            } else {
                StandbyUnitInfo(
                    id = s.id,
                    breakText = "-",
                    breakSeconds = 0L,
                    isReady = true,
                    remainingText = null
                )
            }
        }.sortedByDescending { it.breakSeconds }
    }

    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Fixed Top Header & Tab Selector (1:1 web layout) ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                LivePulseHeader(
                    isLiveConnected = state.isLiveConnected,
                    isReconnecting = state.isReconnecting,
                )

                MonitorTabSelector(
                    selectedTab = currentTab,
                    onSelectTab = { tab ->
                        scope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                    },
                    liveCount = inUseScooters.size,
                    recentCount = todayLogsCount,
                )
            }

            when {
                state.error != null && state.scooters.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        ErrorState(message = state.error ?: "", onRetry = { viewModel.refresh() })
                    }
                }
                state.loading && state.scooters.isEmpty() -> {
                    MonitorSkeleton()
                }
                else -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top,
                    ) { page ->
                        when (MonitorTab.entries[page]) {
                            // ══════════════════════════════════════
                            // TAB 1: SESI BERJALAN (1:1 with web)
                            // ══════════════════════════════════════
                            MonitorTab.LIVE_SESSION -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    if (inUseScooters.isEmpty()) {
                                        item {
                                            LiveSessionEmptyState(modifier = Modifier.padding(top = 8.dp))
                                        }
                                    } else {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Text(
                                                    text = "Sesi Berjalan",
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                )

                                                // Kontrol urutan: Terlama (default) & Terbaru
                                                var sortMenuExpanded by remember { mutableStateOf(false) }
                                                Box {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable { sortMenuExpanded = true }
                                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    ) {
                                                        Text(
                                                            text = if (sortOrder == "oldest") "Terlama" else "Terbaru",
                                                            color = TextMuted,
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                        )
                                                        Icon(
                                                            Icons.Filled.KeyboardArrowDown,
                                                            contentDescription = "Ganti urutan sesi",
                                                            tint = TextMuted,
                                                            modifier = Modifier.size(16.dp),
                                                        )
                                                    }

                                                    DropdownMenu(
                                                        expanded = sortMenuExpanded,
                                                        onDismissRequest = { sortMenuExpanded = false },
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Terlama (Prioritas)") },
                                                            onClick = {
                                                                sortOrder = "oldest"
                                                                sortMenuExpanded = false
                                                            },
                                                        )
                                                        DropdownMenuItem(
                                                            text = { Text("Terbaru") },
                                                            onClick = {
                                                                sortOrder = "newest"
                                                                sortMenuExpanded = false
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        items(
                                            items = inUseScooters,
                                            key = { "live_${it.id}" },
                                        ) { scooter ->
                                            val cleanId = scooter.id.trim().uppercase()
                                            LiveSessionCard(
                                                scooter = scooter,
                                                nowMillis = nowMillis,
                                                onClick = { onOpenDetail?.invoke(scooter.id) },
                                                onTroubleSwap = { troubleScooter = it },
                                                onReturn = { returnConfirmScooter = it },
                                                isProcessing = processingReturnIds.contains(cleanId),
                                                isPendingOffline = pendingOfflineReturnIds.contains(cleanId),
                                            )
                                        }
                                    }

                                    // Section Unit Ready dipindahkan ke bawah daftar sesi berjalan di tab ini
                                    item {
                                        ReadyUnitsSection(
                                            readyUnits = readyUnits,
                                            onOpenDetail = onOpenDetail,
                                            modifier = Modifier.padding(top = 8.dp),
                                        )
                                    }
                                }
                            }
                            // ══════════════════════════════════════
                            // TAB 2: AKTIVITAS TERBARU (1:1 with web)
                            // ══════════════════════════════════════
                            MonitorTab.ACTIVITY -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    item {
                                        ActivityFeedPanel(
                                            activityLog = outletFilteredActivityLog,
                                            scooters = outletFilteredScooters,
                                            onOpenDetail = { id -> onOpenDetail?.invoke(id) }
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

    // ── Trouble Swap Dialog (1:1 web backend API call) ──
    troubleScooter?.let { scooter ->
        TroubleSwapDialog(
            scooter = scooter,
            availableScooters = outletFilteredScooters.filter { it.status == ScooterStatus.AVAILABLE },
            onDismiss = { troubleScooter = null },
            onConfirm = { replacementId, note, issue, markBroken ->
                scope.launch {
                    val ok = viewModel.swapScooter(
                        scooterId = scooter.id,
                        replacementId = replacementId,
                        note = note,
                        issue = issue,
                        markBroken = markBroken
                    )
                    if (ok) {
                        troubleScooter = null
                        sweetAlert.showSuccess("Unit ${scooter.id} berhasil ditukar ke $replacementId")
                    } else {
                        sweetAlert.showError("Gagal menukar unit. Pastikan unit pengganti ready.")
                    }
                }
            },
        )
    }

    // ── Dialog Konfirmasi Kembalikan Unit Langsung ──
    returnConfirmScooter?.let { scooter ->
        val dt = DateUtils.parse(scooter.lastUpdated)
        val startMillis = dt?.atZone(DateUtils.WIB)?.toInstant()?.toEpochMilli() ?: nowMillis
        val elapsedSecs = ((nowMillis - startMillis) / 1000).coerceAtLeast(0)
        val durationText = DateUtils.formatDuration(elapsedSecs)
        val keluarTime = if (dt != null) DateUtils.formatTime(dt) else "-"
        val cleanId = scooter.id.trim().uppercase()
        val isProcessing = processingReturnIds.contains(cleanId)

        AlertDialog(
            onDismissRequest = { if (!isProcessing) returnConfirmScooter = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = com.evrenhouse.trackscooter.ui.theme.Green,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text = "Kembalikan Unit ${scooter.id}?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Konfirmasi unit telah kembali secara fisik ke outlet. Durasi sewa akan diselesaikan.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        lineHeight = 18.sp,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Surface2)
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Jam Keluar", fontSize = 10.5.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                                Text(keluarTime, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Durasi Sewa", fontSize = 10.5.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
                                Text(
                                    durationText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (elapsedSecs >= 3600) com.evrenhouse.trackscooter.ui.theme.Red else Accent,
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.returnScooterDirect(scooter.id) { success, msg ->
                            if (success) {
                                sweetAlert.showSuccess(msg)
                            } else {
                                sweetAlert.showError(msg)
                            }
                        }
                        returnConfirmScooter = null
                    },
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = com.evrenhouse.trackscooter.ui.theme.Green,
                        contentColor = Color.White,
                    ),
                ) {
                    Text("Ya, Kembalikan Unit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { returnConfirmScooter = null },
                    enabled = !isProcessing,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text("Batal", fontSize = 13.sp)
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(16.dp),
        )
    }
}


// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "Monitor Screen - Live Session (Light)", showBackground = true)
@Composable
private fun MonitorScreenLightPreview() {
    val sampleScooters = listOf(
        Scooter("SB-01", "sb", ScooterStatus.IN_USE, currentOutlet = "utara", lastUpdated = "2026-10-01T11:28:00Z"),
        Scooter("SB-02", "sb", ScooterStatus.AVAILABLE, currentOutlet = "utara", lastUpdated = "2026-10-01T09:00:00Z"),
        Scooter("FZ-05", "fz", ScooterStatus.AVAILABLE, currentOutlet = "utara", lastUpdated = "2026-10-01T08:30:00Z"),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.fillMaxSize().background(Surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LivePulseHeader(
                    isLiveConnected = true,
                    isReconnecting = false,
                )
                LiveSessionCard(
                    scooter = sampleScooters[0],
                    nowMillis = System.currentTimeMillis(),
                    onTroubleSwap = {},
                )
            }
        }
    }
}

@Preview(name = "Monitor Screen - Live Session (Dark)", showBackground = true)
@Composable
private fun MonitorScreenDarkPreview() {
    val sampleScooters = listOf(
        Scooter("SB-01", "sb", ScooterStatus.IN_USE, currentOutlet = "utara", lastUpdated = "2026-10-01T11:28:00Z"),
        Scooter("SB-02", "sb", ScooterStatus.AVAILABLE, currentOutlet = "utara", lastUpdated = "2026-10-01T09:00:00Z"),
    )
    TrackScooterTheme(isDark = true) {
        Box(modifier = Modifier.fillMaxSize().background(Surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LivePulseHeader(
                    isLiveConnected = true,
                    isReconnecting = false,
                )
                LiveSessionCard(
                    scooter = sampleScooters[0],
                    nowMillis = System.currentTimeMillis(),
                    onTroubleSwap = {},
                )
            }
        }
    }
}
