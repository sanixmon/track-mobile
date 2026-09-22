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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.ui.detail.ScooterDetailDialog
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
    var detailScooterId by remember { mutableStateOf<String?>(null) }
    var sortOrder by remember { mutableStateOf("newest") } // "newest" | "oldest"

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
                    outletName = if (globalOutlet == "all") null else Outlets.labelOf(globalOutlet),
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
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingState("Memuat data pemantauan...")
                    }
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
                                            LiveSessionEmptyState(modifier = Modifier.padding(top = 16.dp))
                                        }
                                    } else {
                                        item {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "SESI SEWA BERJALAN (${inUseScooters.size} UNIT)",
                                                    color = TextSubtle,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp,
                                                )

                                                if (inUseScooters.size > 1) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .clickable {
                                                                sortOrder = if (sortOrder == "newest") "oldest" else "newest"
                                                            }
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Filled.SwapVert,
                                                            contentDescription = null,
                                                            tint = TextMuted,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = if (sortOrder == "newest") "Terbaru" else "Terlama",
                                                            color = TextMuted,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        items(
                                            items = inUseScooters,
                                            key = { "live_${it.id}" },
                                        ) { scooter ->
                                            LiveSessionCard(
                                                scooter = scooter,
                                                nowMillis = nowMillis,
                                                onClick = {
                                                    detailScooterId = scooter.id
                                                    onOpenDetail?.invoke(scooter.id)
                                                },
                                                onTroubleSwap = { troubleScooter = it },
                                            )
                                        }
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
                                            onOpenDetail = { id ->
                                                detailScooterId = id
                                                onOpenDetail?.invoke(id)
                                            }
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

    // ── Scooter Detail Dialog ──
    detailScooterId?.let { id ->
        ScooterDetailDialog(
            scooterId = id,
            onDismiss = { detailScooterId = null }
        )
    }
}
