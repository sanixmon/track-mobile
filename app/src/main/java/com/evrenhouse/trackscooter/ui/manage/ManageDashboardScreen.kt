package com.evrenhouse.trackscooter.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.ui.common.ErrorState
import com.evrenhouse.trackscooter.ui.common.LoadingState
import com.evrenhouse.trackscooter.ui.common.DashboardSkeleton
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.dashboard.FleetStatCards
import com.evrenhouse.trackscooter.ui.dashboard.MaintenanceSection
import com.evrenhouse.trackscooter.ui.dashboard.OutletSummaryCards
import com.evrenhouse.trackscooter.ui.dashboard.TypeSummaryCard
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.Outlets

/**
 * Dashboard khusus ranah Manajemen: ringkasan armada (per outlet, statistik,
 * per jenis) + tabel maintenance. Tanpa log sesi harian (itu ranah Operasional
 * di DashboardScreen). Berbagi komponen + perilaku dengan dashboard operasional.
 */
@Composable
fun ManageDashboardScreen(viewModel: ScooterDataViewModel) {
    val state by viewModel.state.collectAsState()
    val activeOutlet by viewModel.selectedOutlet.collectAsState()

    val outletFilteredScooters by remember(state.scooters, activeOutlet) {
        derivedStateOf {
            if (activeOutlet == "all") state.scooters
            else state.scooters.filter { (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == activeOutlet }
        }
    }

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

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Dashboard", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Surface2, RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Accent, CircleShape),
                        )
                        Text(
                            "Mode Manajemen",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

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
                if (activeOutlet == "all") {
                    item {
                        OutletSummaryCards(
                            scooters = state.scooters,
                            onSelectOutlet = { viewModel.setSelectedOutlet(it) }
                        )
                    }
                }

                item {
                    FleetStatCards(scooters = outletFilteredScooters)
                }

                item {
                    TypeSummaryCard(outletFilteredScooters)
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
