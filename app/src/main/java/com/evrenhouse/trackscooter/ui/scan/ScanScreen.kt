package com.evrenhouse.trackscooter.ui.scan
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.tooling.preview.Preview
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.TypeLabels
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.OutlinedAction
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.SweetAlertType
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.monitor.RentalTripUtils
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors

enum class ScanSubTab(val label: String, val icon: ImageVector) {
    SCAN("Scan", Icons.Filled.QrCodeScanner),
    BY_ID("By ID", Icons.Filled.Tag),
}

@OptIn(ExperimentalLayoutApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    dataViewModel: ScooterDataViewModel = viewModel(factory = AppViewModelFactory(repository())),
    viewModel: ScanViewModel = viewModel(factory = AppViewModelFactory(repository())),
    onBack: (() -> Unit)? = null,
) {
    val state by viewModel.state.collectAsState()
    val dataState by dataViewModel.state.collectAsState()
    val globalOutlet by dataViewModel.selectedOutlet.collectAsState()

    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { ScanSubTab.entries.size }
    val currentSubTab by remember { derivedStateOf { ScanSubTab.entries[pagerState.currentPage] } }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var byIdStatusFilter by rememberSaveable { mutableStateOf("available") } // "available" (Ready) | "in_use" (Disewa)

    // Scooters filtered by global outlet
    val outletFilteredScooters by remember(dataState.scooters, globalOutlet) {
        derivedStateOf {
            val list = if (globalOutlet == "all") {
                dataState.scooters
            } else {
                dataState.scooters.filter {
                    (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == globalOutlet
                }
            }
            list.sortedWith(
                compareBy<Scooter> { it.id.filter { ch -> !ch.isDigit() } }
                    .thenBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
            )
        }
    }

    val readyScooters by remember(outletFilteredScooters) {
        derivedStateOf {
            outletFilteredScooters.filter { it.status == ScooterStatus.AVAILABLE }
        }
    }

    val inUseScooters by remember(outletFilteredScooters) {
        derivedStateOf {
            outletFilteredScooters.filter { it.status == ScooterStatus.IN_USE }
        }
    }

    // List displayed in By ID based on status filter & search query
    val displayScooters by remember(outletFilteredScooters, readyScooters, inUseScooters, byIdStatusFilter, searchQuery) {
        derivedStateOf {
            val q = searchQuery.trim()
            if (q.isBlank()) {
                if (byIdStatusFilter == "available") readyScooters else inUseScooters
            } else {
                // Saat mencari ID, cari dari seluruh armada outlet aktif (ready maupun in_use)
                outletFilteredScooters.filter {
                    it.id.contains(q, ignoreCase = true)
                }
            }
        }
    }


    // Alert events
    LaunchedEffect(state.toastId, state.toast) {
        state.toast?.let { msg ->
            when (state.toastType) {
                SweetAlertType.SUCCESS -> sweetAlert.showSuccess(msg)
                SweetAlertType.ERROR -> sweetAlert.showError(msg)
                SweetAlertType.WARNING -> sweetAlert.showWarning(msg)
                else -> sweetAlert.showInfo(msg)
            }
            viewModel.consumeToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        BackHandler(enabled = onBack != null) {
            onBack?.invoke()
        }

        // ── 2 Sub Tab Selector (Sub Tab 1: Scan, Sub Tab 2: By ID) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface2, RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ScanSubTab.entries.forEach { tab ->
                val isSelected = currentSubTab == tab
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Accent else Color.Transparent)
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(tab.ordinal)
                            }
                        }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (tab == ScanSubTab.BY_ID) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.25f)
                                    else Green.copy(alpha = 0.2f),
                                    CircleShape,
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = readyScooters.size.toString(),
                                color = if (isSelected) Color.White else Green,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // ── Swipeable Pager for Sub Tabs ──
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) { page ->
            when (ScanSubTab.entries[page]) {
                ScanSubTab.SCAN -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        ScanRecentLogCard(
                            activityLog = dataState.activityLog,
                            scooters = outletFilteredScooters,
                            onSelect = { viewModel.onScanned(it) },
                        )

                        CameraScanner(
                            isProcessing = state.busy || !state.scanning,
                            onScan = { viewModel.onScanned(it) },
                            onError = { sweetAlert.showError(it) },
                        )
                    }
                }

                ScanSubTab.BY_ID -> {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Search by Unit ID
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it.uppercase() },
                            placeholder = { Text("Cari nomor ID unit (cth: SD-01)...", color = TextSubtle, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                        Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                            ),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Accent,
                                unfocusedBorderColor = Border,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                            ),
                        )

                        // Toggle Status (Ready vs Disewa) saat tidak sedang mengetik pencarian
                        if (searchQuery.isBlank()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Surface2, RoundedCornerShape(10.dp))
                                    .border(1.dp, Border, RoundedCornerShape(10.dp))
                                    .padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                // Tab 1: Unit Ready (Sewa)
                                val isReadyTab = byIdStatusFilter == "available"
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isReadyTab) Accent else Color.Transparent)
                                        .clickable { byIdStatusFilter = "available" }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (isReadyTab) Color.White else Green, CircleShape),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Ready (${readyScooters.size})",
                                        color = if (isReadyTab) Color.White else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isReadyTab) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }

                                // Tab 2: Unit Disewa (Pengembalian)
                                val isInUseTab = byIdStatusFilter == "in_use"
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isInUseTab) Accent else Color.Transparent)
                                        .clickable { byIdStatusFilter = "in_use" }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (isInUseTab) Color.White else Color(0xFF38BDF8), CircleShape),
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Disewa (${inUseScooters.size})",
                                        color = if (isInUseTab) Color.White else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isInUseTab) FontWeight.Bold else FontWeight.Medium,
                                    )
                                }
                            }
                        }

                        // Header Summary & Guidance
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val titleText = when {
                                searchQuery.isNotBlank() -> "HASIL PENCARIAN (${displayScooters.size})"
                                byIdStatusFilter == "available" -> "UNIT READY (${readyScooters.size})"
                                else -> "UNIT SEDANG DISEWA (${inUseScooters.size})"
                            }
                            val subtitleText = when {
                                searchQuery.isNotBlank() -> "Ketuk unit untuk proses"
                                byIdStatusFilter == "available" -> "Ketuk unit untuk proses sewa"
                                else -> "Ketuk unit untuk pengembalian"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            if (searchQuery.isNotBlank()) Accent
                                            else if (byIdStatusFilter == "available") Green
                                            else Color(0xFF38BDF8),
                                            CircleShape,
                                        ),
                                )
                                Text(
                                    text = titleText,
                                    color = TextSubtle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                )
                            }

                            Text(
                                text = subtitleText,
                                color = TextMuted,
                                fontSize = 11.sp,
                            )
                        }

                        // List / Uniform 3-Column Grid of Scooters
                        if (displayScooters.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Surface)
                                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = TextSubtle,
                                        modifier = Modifier.size(28.dp),
                                    )
                                    val emptyMsg = when {
                                        searchQuery.isNotBlank() -> "Tidak ada unit dengan ID \"$searchQuery\"."
                                        byIdStatusFilter == "available" -> if (globalOutlet == "all") "Tidak ada unit ready saat ini." else "Tidak ada unit ready di ${Outlets.labelOf(globalOutlet)}."
                                        else -> if (globalOutlet == "all") "Tidak ada unit yang sedang disewa." else "Tidak ada unit yang sedang disewa dari ${Outlets.labelOf(globalOutlet)}."
                                    }
                                    Text(
                                        text = emptyMsg,
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val isDark = LocalThemeIsDark.current
                                val rows = displayScooters.chunked(3)
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        rowItems.forEach { s ->
                                            val isSelected = searchQuery.equals(s.id, ignoreCase = true)
                                            val nameColor = if (isSelected) Accent else ScooterColors.getScooterNameColor(s.type, s.id, s.currentOutlet, isDark)
                                            val outletName = Outlets.shortLabelOf(s.currentOutlet ?: Outlets.getHomeOutletForType(s.type))
                                            val isAvailable = s.status == ScooterStatus.AVAILABLE

                                            Column(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (isSelected) Accent.copy(alpha = 0.2f) else Surface)
                                                    .border(1.dp, if (isSelected) Accent else Border, RoundedCornerShape(10.dp))
                                                    .clickable(enabled = state.scanning && !state.busy) {
                                                        viewModel.onScanned(s.id)
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 9.dp),
                                            ) {
                                                Text(
                                                    text = s.id,
                                                    color = nameColor,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    maxLines = 1,
                                                )
                                                Spacer(Modifier.height(3.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(
                                                                if (isAvailable) Green else Color(0xFF38BDF8),
                                                                CircleShape,
                                                            ),
                                                    )
                                                    Text(
                                                        text = if (isAvailable) outletName else "Disewa",
                                                        color = TextSubtle,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        maxLines = 1,
                                                    )
                                                }
                                            }
                                        }
                                        if (rowItems.size < 3) {
                                            repeat(3 - rowItems.size) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Submit button if manual input typed
                        if (searchQuery.isNotBlank()) {
                            Button(
                                onClick = {
                                    viewModel.onScanned(searchQuery.trim())
                                    searchQuery = ""
                                },
                                enabled = state.scanning && !state.busy,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                            ) {
                                Text("Proses Unit $searchQuery", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // Universal Scan Confirmation Bottom Sheet
    state.pendingScooter?.let { scooter ->
        ScanConfirmBottomSheet(
            scooter = scooter,
            breakText = state.pendingBreakText,
            submitting = state.busy,
            onConfirm = { viewModel.confirmScan() },
            onDismiss = { viewModel.dismissConfirmation() },
        )
    }
}

@Composable
fun ScanRecentLogCard(
    activityLog: List<ActivityLogEntry>,
    scooters: List<Scooter>? = null,
    onSelect: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val today = remember { DateUtils.today() }
    val todayStr = remember(today) { DateUtils.localDateKey(today) }

    val allSessions = remember(activityLog, todayStr, scooters) {
        RentalTripUtils.buildSessions(activityLog, scooters, todayStr)
    }

    var visibleCount by rememberSaveable { mutableStateOf(4) }
    val currentLimit = visibleCount.coerceAtLeast(4)
    val displayedSessions = allSessions.take(currentLimit)
    val hasMore = allSessions.size > displayedSessions.size
    val canReset = displayedSessions.size > 4 && !hasMore

    val isDark = LocalThemeIsDark.current
    val amberColor = if (isDark) Warning else Color(0xFFD97706)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        if (allSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Belum ada aktivitas sewa hari ini.",
                    color = TextMuted,
                    fontSize = 12.sp,
                )
            }
        } else {
            displayedSessions.forEachIndexed { index, session ->
                if (index > 0) {
                    HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)
                }

                val nameColor = remember(session.scooterType, session.scooterId, isDark) {
                    ScooterColors.getScooterNameColor(session.scooterType, session.scooterId, null, isDark)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { if (onSelect != null) it.clickable { onSelect(session.scooterId) } else it }
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Nomor Unit
                    Text(
                        text = session.scooterId,
                        color = nameColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    )

                    // Jam Mulai → Jam Selesai
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = session.keluarTimeStr,
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                        )
                        Text(
                            text = "→",
                            color = TextSubtle,
                            fontSize = 12.sp,
                        )
                        if (session.isOngoing) {
                            Text(
                                text = "jalan",
                                color = amberColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                        } else {
                            Text(
                                text = session.masukTimeStr ?: "-",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }
            }

            // Tombol More (+4) / Tutup kembali (4)
            if (hasMore || canReset) {
                HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)

                val remaining = allSessions.size - displayedSessions.size
                val nextChunk = remaining.coerceAtMost(4)
                val buttonText = if (hasMore) {
                    "Muat lebih banyak (+$nextChunk)"
                } else {
                    "Tutup kembali (4)"
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (hasMore) {
                                visibleCount += 4
                            } else {
                                visibleCount = 4
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = buttonText,
                        color = Accent,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Preview(name = "Scan Recent Log Card", showBackground = true)
@Composable
private fun ScanRecentLogCardPreview() {
    val sampleLogs = listOf(
        ActivityLogEntry("1", "SD-02", "sd", "checkout", "2026-10-23T14:15:00Z"),
        ActivityLogEntry("2", "SD-02", "sd", "return", "2026-10-23T15:00:00Z"),
        ActivityLogEntry("3", "FZ-05", "fz", "checkout", "2026-10-23T15:15:00Z"),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.padding(16.dp)) {
            ScanRecentLogCard(activityLog = sampleLogs)
        }
    }
}
