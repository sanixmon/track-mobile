package com.evrenhouse.trackscooter.ui.scan

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
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors

enum class ScanSubTab(val label: String, val icon: ImageVector) {
    SCAN("Scan", Icons.Filled.QrCodeScanner),
    BY_ID("By ID", Icons.Filled.Tag),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanScreen(
    dataViewModel: ScooterDataViewModel = viewModel(factory = AppViewModelFactory(repository())),
    viewModel: ScanViewModel = viewModel(factory = AppViewModelFactory(repository())),
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
    // Ready scooters (ScooterStatus.AVAILABLE) filtered by global outlet
    val outletFilteredReadyScooters by remember(dataState.scooters, globalOutlet) {
        derivedStateOf {
            val list = dataState.scooters.filter { s ->
                val isReady = s.status == ScooterStatus.AVAILABLE
                val matchesOutlet = if (globalOutlet == "all") true
                else (s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)) == globalOutlet
                isReady && matchesOutlet
            }
            list.sortedWith(
                compareBy<Scooter> { it.id.filter { ch -> !ch.isDigit() } }
                    .thenBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
            )
        }
    }

    // Ready scooters filtered by search query
    val displayReadyScooters by remember(outletFilteredReadyScooters, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                outletFilteredReadyScooters
            } else {
                outletFilteredReadyScooters.filter {
                    it.id.contains(searchQuery.trim(), ignoreCase = true)
                }
            }
        }
    }


    // Alert events
    LaunchedEffect(state.toast) {
        state.toast?.let {
            if (it.contains("berhasil", ignoreCase = true) || it.contains("check", ignoreCase = true) || it.contains("sukses", ignoreCase = true)) {
                sweetAlert.showSuccess(it)
            } else if (it.contains("gagal", ignoreCase = true) || it.contains("tidak", ignoreCase = true) || it.contains("salah", ignoreCase = true)) {
                sweetAlert.showError(it)
            } else {
                sweetAlert.showInfo(it)
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
        // ── Header ──
        Text("Scan", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)

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
                                text = outletFilteredReadyScooters.size.toString(),
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
                        CameraScanner(
                            isProcessing = state.busy || !state.scanning,
                            onScan = { viewModel.onScanned(it) },
                            onError = { sweetAlert.showError(it) },
                        )

                        ScanRecentLogCard(
                            activityLog = dataState.activityLog,
                            onSelect = { viewModel.onScanned(it) },
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

                        // Header Ready Summary
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
                                    text = "UNIT READY (${displayReadyScooters.size})",
                                    color = TextSubtle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                )
                            }

                            Text(
                                text = "Ketuk unit untuk proses sewa",
                                color = TextMuted,
                                fontSize = 11.sp,
                            )
                        }

                        // List / FlowRow of Ready Scooters
                        if (displayReadyScooters.isEmpty()) {
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
                                    Text(
                                        text = if (searchQuery.isNotBlank()) "Tidak ada unit ready dengan ID \"$searchQuery\""
                                        else if (globalOutlet == "all") "Tidak ada unit ready saat ini."
                                        else "Tidak ada unit ready di ${Outlets.labelOf(globalOutlet)}.",
                                        color = TextMuted,
                                        fontSize = 12.sp,
                                    )
                                }
                            }
                        } else {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                val isDark = LocalThemeIsDark.current
                                displayReadyScooters.forEach { s ->
                                    val isSelected = searchQuery.equals(s.id, ignoreCase = true)
                                    val nameColor = if (isSelected) Accent else ScooterColors.getScooterNameColor(s.type, s.id, s.currentOutlet, isDark)
                                    val outletName = Outlets.shortLabelOf(s.currentOutlet ?: Outlets.getHomeOutletForType(s.type))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) Accent.copy(alpha = 0.2f) else Surface)
                                            .border(1.dp, if (isSelected) Accent else Border, RoundedCornerShape(10.dp))
                                            .clickable(enabled = state.scanning && !state.busy) {
                                                viewModel.onScanned(s.id)
                                            }
                                            .padding(horizontal = 12.dp, vertical = 9.dp),
                                    ) {
                                        Column {
                                            Text(
                                                text = s.id,
                                                color = nameColor,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace,
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(Green, CircleShape),
                                                )
                                                Text(
                                                    text = outletName,
                                                    color = TextSubtle,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                )
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

    // Universal Scan Confirmation Modal
    state.pendingScooter?.let { scooter ->
        ScanConfirmDialog(
            scooter = scooter,
            breakText = state.pendingBreakText,
            submitting = state.busy,
            onConfirm = {
                viewModel.confirmScan()
                dataViewModel.refresh(silent = true)
            },
            onDismiss = { viewModel.dismissConfirmation() },
        )
    }
}

@Composable
fun ScanRecentLogCard(
    activityLog: List<ActivityLogEntry>,
    onSelect: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showAll by remember { mutableStateOf(false) }

    val recentLogs = remember(activityLog) {
        activityLog.filter { it.action == "checkout" || it.action == "return" }
            .sortedByDescending { it.timestamp }
    }

    val displayedLogs = if (showAll) recentLogs else recentLogs.take(2)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Filled.History, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                Text("LOG RECENT SEWA & SELESAI", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
            if (recentLogs.size > 2) {
                Text(
                    text = if (showAll) "Tutup" else "Lihat semua (${recentLogs.size})",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showAll = !showAll },
                )
            }
        }

        if (recentLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada riwayat aktivitas sewa.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            displayedLogs.forEachIndexed { index, log ->
                if (index > 0) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
                }
                val isCheckout = log.action == "checkout"
                val timeStr = remember(log.timestamp) {
                    val dt = DateUtils.parse(log.timestamp)
                    if (dt != null) DateUtils.formatTime(dt) else log.timestamp
                }
                val isDark = LocalThemeIsDark.current
                val nameColor = ScooterColors.getScooterNameColor(log.scooterType, log.scooterId, null, isDark)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let { if (onSelect != null) it.clickable { onSelect(log.scooterId) } else it }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Badge Sewa / Selesai
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isCheckout) Accent.copy(alpha = 0.15f) else Green.copy(alpha = 0.15f),
                                    RoundedCornerShape(6.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                        ) {
                            Text(
                                text = if (isCheckout) "Sewa" else "Selesai",
                                color = if (isCheckout) Accent else Green,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        // ID & Jenis
                        Column {
                            Text(
                                text = log.scooterId,
                                color = nameColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                            )
                            if (log.scooterType.isNotBlank()) {
                                Text(
                                    text = TypeLabels.of(log.scooterType),
                                    color = TextMuted,
                                    fontSize = 10.5.sp,
                                 )
                            }
                        }
                    }

                    // Timestamp
                    Text(
                        text = timeStr,
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}
