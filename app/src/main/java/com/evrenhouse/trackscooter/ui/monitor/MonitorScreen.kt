package com.evrenhouse.trackscooter.ui.monitor

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.ui.dashboard.ScooterCard
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.BlueLive
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Exporter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: ((String) -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(initialPage = 0) { MonitorTab.entries.size }
    val currentTab by remember { derivedStateOf { MonitorTab.entries[pagerState.currentPage] } }
    var troubleScooter by remember { mutableStateOf<Scooter?>(null) }
    // Ticker to live-update rental duration without seconds every 5 seconds
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            nowMillis = System.currentTimeMillis()
        }
    }

    val todayStr = remember { DateUtils.localDateKey(DateUtils.today()) }

    val logForDate by remember(state.activityLog) {
        derivedStateOf {
            state.activityLog.filter { DateUtils.dateKey(it.timestamp) == todayStr }
        }
    }
    // In-use scooters for Live Session tab
    val inUseScooters by remember {
        derivedStateOf {
            state.scooters.filter { it.status == "in-use" }
        }
    }



    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Fixed Top Header & Tab Selector ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LivePulseHeader(
                    isLiveView = true,
                    isLiveConnected = state.isLiveConnected,
                    isReconnecting = state.isReconnecting,
                    activeDate = DateUtils.today(),
                )
                MonitorTabSelector(
                    selectedTab = currentTab,
                    onSelectTab = { tab ->
                        scope.launch {
                            pagerState.animateScrollToPage(tab.ordinal)
                        }
                    },
                    liveCount = inUseScooters.size,
                    recentCount = logForDate.size,
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
                        LoadingState("Memuat data monitor...")
                    }
                }
                else -> {
                    // ── Horizontal Pager for swipe / scroll between tabs ──
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Top,
                    ) { page ->
                        when (MonitorTab.entries[page]) {
                            // ══════════════════════════════════════
                            // TAB 1: LIVE SESSION
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
                                            Text(
                                                text = "SESI SEWA BERJALAN (${inUseScooters.size} UNIT)",
                                                color = TextSubtle,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp,
                                                modifier = Modifier.padding(bottom = 2.dp),
                                            )
                                        }
                                        items(
                                            items = inUseScooters,
                                            key = { "live_${it.id}" },
                                        ) { scooter ->
                                            LiveSessionCard(
                                                scooter = scooter,
                                                nowMillis = nowMillis,
                                                onClick = onOpenDetail?.let { open -> { open(scooter.id) } },
                                                onTroubleSwap = { troubleScooter = it },
                                            )
                                        }
                                    }
                                }
                            }

                            // ══════════════════════════════════════
                            // TAB 2: RECENT (Aktivitas Terkini)
                            // ══════════════════════════════════════
                            MonitorTab.ACTIVITY -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "AKTIVITAS TERKINI HARI INI",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 1.1.sp,
                                            )
                                            Text(
                                                text = "${logForDate.size} transaksi",
                                                color = TextMuted,
                                                fontSize = 11.sp,
                                            )
                                        }
                                    }

                                    if (logForDate.isEmpty()) {
                                        item {
                                            Text(
                                                text = "Belum ada aktivitas hari ini.",
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Surface, RoundedCornerShape(14.dp))
                                                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                                                    .padding(28.dp),
                                            )
                                        }
                                    } else {
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Surface, RoundedCornerShape(14.dp))
                                                    .border(1.dp, Border, RoundedCornerShape(14.dp)),
                                            ) {
                                                Column {
                                                    logForDate.forEachIndexed { index, entry ->
                                                        ActivityItemRow(entry = entry, isLiveView = true)
                                                        if (index < logForDate.size - 1) {
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
                                }
                            }
                        }
                    }
                }
            }
        }
    troubleScooter?.let { scooter ->
        TroubleSwapDialog(
            scooter = scooter,
            availableScooters = state.scooters.filter { it.status == ScooterStatus.AVAILABLE },
            onDismiss = { troubleScooter = null },
            onConfirm = { mode, replacementId, structuredIssue, locationNote ->
                scope.launch {
                    runCatching {
                        viewModel.handleTroubleSwap(
                            scooterId = scooter.id,
                            mode = mode,
                            replacementId = replacementId,
                            structuredIssue = structuredIssue,
                            locationNote = locationNote,
                        )
                    }.onSuccess {
                        troubleScooter = null
                        sweetAlert.showSuccess(
                            if (mode == "swap") "Unit ${scooter.id} berhasil ditukar ke $replacementId"
                            else "Unit ${scooter.id} dihentikan & dicatat evakuasi"
                        )
                    }.onFailure { err ->
                        sweetAlert.showError("Gagal memproses insiden: ${err.toUserMessage()}")
                    }
                }
            },
        )
    }
}

