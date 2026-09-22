package com.evrenhouse.trackscooter.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.LocalThemeToggle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.CompactDropdown
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.FilledAction
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.OutlinedAction
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.SectionCard
import com.evrenhouse.trackscooter.ui.common.SectionTitle
import com.evrenhouse.trackscooter.ui.common.StatCard
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.util.StatusLabels
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: (String) -> Unit,
    onGoScan: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current

    val activeOutlet by viewModel.selectedOutlet.collectAsState()
    var completingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val outletFilteredScooters by remember(state.scooters, activeOutlet) {
        derivedStateOf {
            if (activeOutlet == "all") state.scooters
            else state.scooters.filter { (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == activeOutlet }
        }
    }

    val outletFilteredActivityLog by remember(state.activityLog, outletFilteredScooters, activeOutlet) {
        derivedStateOf {
            if (activeOutlet == "all") state.activityLog
            else {
                val outletScooterIds = outletFilteredScooters.map { it.id }.toSet()
                state.activityLog.filter { outletScooterIds.contains(it.scooterId) }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Dashboard", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Pantau status scooter secara real-time", color = TextMuted, fontSize = 13.sp)
                    }
                    val isDark = LocalThemeIsDark.current
                    val toggleTheme = LocalThemeToggle.current

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Surface)
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                            .clickable { toggleTheme() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isDark) "Beralih ke Mode Terang" else "Beralih ke Mode Gelap",
                            tint = if (isDark) Warning else Accent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                OutletDropdown(
                    selectedOutletId = activeOutlet,
                    onOutletSelected = { viewModel.setSelectedOutlet(it) },
                    getOutletCount = { outletId ->
                        if (outletId == "all") state.scooters.size
                        else state.scooters.count { (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == outletId }
                    }
                )
            }
        }

        when {
            state.error != null && state.scooters.isEmpty() -> {
                item {
                    ErrorState(message = state.error ?: "", onRetry = { viewModel.refresh() })
                }
            }
            state.loading && state.scooters.isEmpty() -> {
                item {
                    LoadingState("Memuat data scooter...")
                }
            }
            else -> {
                // Realtime sync warning banner
                state.error?.let { err ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Red.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                                .border(1.dp, Red.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sinkronisasi Realtime Terganggu", color = Red, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text(err, color = Red, fontSize = 11.sp)
                            }
                            FilledAction(text = "Coba Lagi", onClick = { viewModel.refresh() }, color = Red)
                        }
                    }
                }

                // ── Ringkasan Per Outlet (Saat Semua Outlet Aktif 1:1 Web) ──
                if (activeOutlet == "all") {
                    item {
                        OutletSummaryCards(
                            scooters = state.scooters,
                            onSelectOutlet = { viewModel.setSelectedOutlet(it) }
                        )
                    }
                }

                // ── 4 Unified Stat Cards (1:1 dengan Web App) ──
                item {
                    val ready = outletFilteredScooters.count { it.status == ScooterStatus.AVAILABLE }
                    val maintLuar = outletFilteredScooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location == "luar" }
                    val maintOutlet = outletFilteredScooters.count { it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location != "luar" }
                    val total = ready + maintLuar + maintOutlet

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                valueColor = androidx.compose.ui.graphics.Color(0xFFA855F7),
                                iconBg = androidx.compose.ui.graphics.Color(0xFFA855F7).copy(alpha = 0.12f),
                                iconColor = androidx.compose.ui.graphics.Color(0xFFA855F7),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                // ── Log Recent (Tabel Sesi Sewa Hari Ini 1:1 Web) ──
                item {
                    RecentLogTableCard(
                        activityLog = outletFilteredActivityLog,
                        scooters = outletFilteredScooters,
                        onSelect = onOpenDetail
                    )
                }

                // ── Ringkasan per Jenis (6 Tipe Armada 1:1 Web) ──
                item {
                    TypeSummaryCard(outletFilteredScooters)
                }

                // ── Tabel Maintenance Aktif (1:1 Web) ──
                val filteredRecords = state.maintenanceRecords.filter { rec ->
                    if (activeOutlet == "all") true
                    else {
                        val bike = state.scooters.find { it.id == rec.scooterId }
                        val cur = bike?.currentOutlet ?: Outlets.getHomeOutletForType(rec.scooterType)
                        cur == activeOutlet
                    }
                }
                if (filteredRecords.isNotEmpty()) {
                    item {
                        MaintenanceTable(
                            records = filteredRecords,
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
                        )
                    }
                }
            }
        }
    }
}
