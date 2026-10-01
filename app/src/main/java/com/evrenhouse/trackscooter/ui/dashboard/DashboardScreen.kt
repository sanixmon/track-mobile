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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import com.evrenhouse.trackscooter.ui.common.CompactDropdown
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.FilledAction
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.DashboardSkeleton
import com.evrenhouse.trackscooter.ui.common.OutlinedAction
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.SectionCard
import com.evrenhouse.trackscooter.ui.common.SectionTitle
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.StatusLabels

@Composable
fun DashboardScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: (String) -> Unit,
    onGoScan: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val activeOutlet by viewModel.selectedOutlet.collectAsState()

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
                    Text("Dashboard", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
                    DashboardSkeleton()
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
                    FleetStatCards(scooters = outletFilteredScooters)
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

                // ── Tabel Perbaikan Berjalan ──
                val activeRepairs = state.maintenanceRecords.filter { rec ->
                    val isRepair = rec.status == "repair"
                    val matchesOutlet = if (activeOutlet == "all") true
                    else {
                        val bike = state.scooters.find { it.id == rec.scooterId }
                        val cur = bike?.currentOutlet ?: Outlets.getHomeOutletForType(rec.scooterType ?: bike?.type ?: "sd")
                        cur == activeOutlet
                    }
                    isRepair && matchesOutlet
                }
                if (activeRepairs.isNotEmpty()) {
                    item {
                        MaintenanceSection(
                            viewModel = viewModel,
                            records = activeRepairs,
                        )
                    }
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "Dashboard Screen - Light", showBackground = true)
@Composable
private fun DashboardScreenLightPreview() {
    val sampleScooters = listOf(
        Scooter("SB-01", "sb", ScooterStatus.AVAILABLE, currentOutlet = "utara"),
        Scooter("SB-02", "sb", ScooterStatus.IN_USE, currentOutlet = "utara"),
        Scooter("FZ-05", "fz", ScooterStatus.MAINTENANCE, currentOutlet = "utara"),
    )
    TrackScooterTheme(isDark = false) {
        Box(modifier = Modifier.fillMaxSize().background(Surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FleetStatCards(scooters = sampleScooters)
                TypeSummaryCard(scooters = sampleScooters)
            }
        }
    }
}

@Preview(name = "Dashboard Screen - Dark", showBackground = true)
@Composable
private fun DashboardScreenDarkPreview() {
    val sampleScooters = listOf(
        Scooter("SB-01", "sb", ScooterStatus.AVAILABLE, currentOutlet = "utara"),
        Scooter("SB-02", "sb", ScooterStatus.IN_USE, currentOutlet = "utara"),
        Scooter("FZ-05", "fz", ScooterStatus.MAINTENANCE, currentOutlet = "utara"),
    )
    TrackScooterTheme(isDark = true) {
        Box(modifier = Modifier.fillMaxSize().background(Surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FleetStatCards(scooters = sampleScooters)
                TypeSummaryCard(scooters = sampleScooters)
            }
        }
    }
}
