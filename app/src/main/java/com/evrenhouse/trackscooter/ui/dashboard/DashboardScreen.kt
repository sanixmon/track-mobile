package com.evrenhouse.trackscooter.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Construction
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

    var gridStatus by rememberSaveable { mutableStateOf("all") }
    var gridType by rememberSaveable { mutableStateOf("all") }
    var isScootersExpanded by rememberSaveable { mutableStateOf(false) }
    var historyFilters by remember { mutableStateOf(HistoryFilters()) }
    var completingId by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val filteredScooters by remember(state.scooters, gridStatus, gridType) {
        derivedStateOf {
            state.scooters.filter { s ->
                (gridStatus == "all" || s.status == gridStatus) &&
                    (gridType == "all" || s.type == gridType)
            }
        }
    }

    val displayScooters by remember(filteredScooters, isScootersExpanded) {
        derivedStateOf {
            if (isScootersExpanded) filteredScooters else filteredScooters.take(5)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Dashboard", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Pantau status scooter secara real-time", color = TextMuted, fontSize = 13.sp)
                }
                FilledAction(
                    text = "Scan",
                    onClick = onGoScan,
                    icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(15.dp)) },
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

                // Stats Row 1
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StatCard(
                            label = "Online",
                            sub = "Sedang digunakan",
                            value = state.scooters.count { it.status == ScooterStatus.IN_USE },
                            icon = { tint -> Icon(Icons.Filled.Wifi, null, Modifier.size(17.dp), tint = tint) },
                            valueColor = Accent,
                            iconBg = Accent.copy(alpha = 0.12f),
                            iconColor = Accent,
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Offline",
                            sub = "Rusak di outlet",
                            value = state.scooters.count { it.status == ScooterStatus.RUSAK },
                            icon = { tint -> Icon(Icons.Filled.WifiOff, null, Modifier.size(17.dp), tint = tint) },
                            valueColor = Red,
                            iconBg = Red.copy(alpha = 0.12f),
                            iconColor = Red,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Stats Row 2
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StatCard(
                            label = "Maintenance",
                            sub = "Dalam perbaikan",
                            value = state.scooters.count { it.status == ScooterStatus.MAINTENANCE },
                            icon = { tint -> Icon(Icons.Filled.Construction, null, Modifier.size(17.dp), tint = tint) },
                            valueColor = Warning,
                            iconBg = Warning.copy(alpha = 0.12f),
                            iconColor = Warning,
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            label = "Total Unit",
                            sub = "Seluruh armada",
                            value = state.scooters.size,
                            icon = { tint -> Icon(Icons.Filled.Layers, null, Modifier.size(17.dp), tint = tint) },
                            valueColor = TextPrimary,
                            iconBg = Surface3,
                            iconColor = TextMuted,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // Scooter section header with filters
                item(key = "scooters_filter_header") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "STATUS SCOOTER (${filteredScooters.size})",
                            color = TextSubtle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            CompactDropdown(
                                label = if (gridStatus == "all") "Status" else StatusLabels.of(gridStatus),
                                options = listOf(
                                    "all" to "Semua Status",
                                    "available" to "Tersedia",
                                    "in-use" to "Online",
                                    "rusak" to "Offline / Rusak",
                                    "maintenance" to "Maintenance",
                                ),
                                selected = gridStatus,
                                onSelect = { gridStatus = it },
                            )
                            CompactDropdown(
                                label = if (gridType == "all") "Jenis" else gridType.uppercase(),
                                options = listOf("all" to "Semua Jenis", "sd" to "Standar (SD)", "sj" to "Jumbo (SJ)"),
                                selected = gridType,
                                onSelect = { gridType = it },
                            )
                        }
                    }
                }

                if (filteredScooters.isEmpty()) {
                    item(key = "scooters_empty") {
                        Text(
                            "Tidak ada scooter yang cocok dengan filter status/jenis.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface, RoundedCornerShape(14.dp))
                                .border(1.dp, Border, RoundedCornerShape(14.dp))
                                .padding(28.dp),
                        )
                    }
                } else {
                    items(
                        items = displayScooters,
                        key = { it.id },
                    ) { scooter ->
                        ScooterCard(
                            scooter = scooter,
                            onClick = { onOpenDetail(scooter.id) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    if (filteredScooters.size > 5) {
                        item(key = "scooters_toggle") {
                            OutlinedAction(
                                text = if (isScootersExpanded) "Tampilkan Lebih Sedikit" else "Lihat Selengkapnya (${filteredScooters.size - 5} unit lainnya)",
                                onClick = { isScootersExpanded = !isScootersExpanded },
                                color = Accent,
                            )
                        }
                    }
                }

                // History table
                item {
                    HistoryTable(
                        log = state.activityLog,
                        filters = historyFilters,
                        onFilters = { historyFilters = it },
                    )
                }

                // Sidebar: type summary + activity feed
                item {
                    TypeSummaryCard(state.scooters)
                }

                item {
                    ActivityFeedCard(state.activityLog)
                }

                // Maintenance table
                item {
                    MaintenanceTable(
                        records = state.maintenanceRecords,
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
