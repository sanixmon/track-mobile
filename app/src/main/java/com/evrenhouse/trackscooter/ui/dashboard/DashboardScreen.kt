package com.evrenhouse.trackscooter.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TroubleSwapDialog
import com.evrenhouse.trackscooter.ui.common.UnitActionBottomSheet
import com.evrenhouse.trackscooter.ui.common.UnitCard
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.InputCornerRadius
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.bounceClickable
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: (String) -> Unit,
    onGoScan: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val activeOutlet by viewModel.selectedOutlet.collectAsState()
    val isDark = LocalThemeIsDark.current
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()

    var selectedFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedScooterForAction by remember { mutableStateOf<Scooter?>(null) }
    var swapScooter by remember { mutableStateOf<Scooter?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Outlet-filtered scooters
    val outletFilteredScooters by remember(state.scooters, activeOutlet) {
        derivedStateOf {
            if (activeOutlet == "all") state.scooters
            else state.scooters.filter {
                (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == activeOutlet
            }
        }
    }

    // Fleet stats calculation
    val stats = remember(outletFilteredScooters) {
        val total = outletFilteredScooters.size
        val ready = outletFilteredScooters.count { it.status == ScooterStatus.AVAILABLE }
        val inUse = outletFilteredScooters.count { it.status == ScooterStatus.IN_USE }
        val maintLuar = outletFilteredScooters.count {
            it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location == "luar"
        }
        val maintOutlet = outletFilteredScooters.count {
            it.status == ScooterStatus.MAINTENANCE && it.activeMaintenance?.location != "luar"
        }
        FleetStats(
            total = total,
            ready = ready,
            inUse = inUse,
            maintLuar = maintLuar,
            maintOutlet = maintOutlet,
        )
    }

    // Filtered scooter list for inline display
    val displayedScooters by remember(outletFilteredScooters, selectedFilter, searchQuery) {
        derivedStateOf {
            outletFilteredScooters.filter { scooter ->
                val matchesFilter = when (selectedFilter) {
                    "available" -> scooter.status == ScooterStatus.AVAILABLE
                    "in-use" -> scooter.status == ScooterStatus.IN_USE
                    "maint-luar" -> scooter.status == ScooterStatus.MAINTENANCE && scooter.activeMaintenance?.location == "luar"
                    "maint-outlet" -> scooter.status == ScooterStatus.MAINTENANCE && scooter.activeMaintenance?.location != "luar"
                    else -> true
                }
                val matchesSearch = if (searchQuery.isBlank()) true else {
                    scooter.id.contains(searchQuery.trim(), ignoreCase = true) ||
                        scooter.type.contains(searchQuery.trim(), ignoreCase = true)
                }
                matchesFilter && matchesSearch
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.refresh()
                isRefreshing = false
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 1. Interactive Fleet Gauge Card
            item {
                InteractiveFleetGauge(
                    stats = stats,
                    selectedFilter = selectedFilter,
                    onSelectFilter = { selectedFilter = it },
                )
            }

            // 2. Type breakdown horizontal pill row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val types = listOf("sd", "sj", "sb", "sjb", "sm", "sjm")
                    types.forEach { typeKey ->
                        val count = outletFilteredScooters.count { it.type.lowercase() == typeKey }
                        if (count > 0 || activeOutlet == "all") {
                            val style = ScooterColors.STYLES[typeKey]
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        style?.accent?.copy(alpha = if (isDark) 0.18f else 0.10f)
                                            ?: (if (isDark) Surface2 else Color(0xFFF1F5F9))
                                    )
                                    .border(
                                        1.dp,
                                        style?.accent?.copy(alpha = 0.4f) ?: Border.copy(alpha = 0.5f),
                                        RoundedCornerShape(12.dp),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = typeKey.uppercase(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = style?.accent ?: TextPrimary,
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$count",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextPrimary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Search & Section Header
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (selectedFilter) {
                                    "available" -> "Armada Ready"
                                    "in-use" -> "Armada Disewa"
                                    "maint-luar" -> "Maintenance Luar"
                                    "maint-outlet" -> "Maintenance Outlet"
                                    else -> "Semua Unit"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isDark) Surface2 else Color(0xFFE2E8F0))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                            ) {
                                Text(
                                    text = "${displayedScooters.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextMuted,
                                )
                            }
                        }

                        if (selectedFilter != null) {
                            Text(
                                text = "Reset Filter",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Accent,
                                modifier = Modifier.bounceClickable { selectedFilter = null },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Cari ID skuter (misal: 01)...", fontSize = 13.sp, color = TextMuted)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(InputCornerRadius),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Border.copy(alpha = 0.7f),
                        ),
                        singleLine = true,
                    )
                }
            }

            // 4. Inline Unit List
            if (displayedScooters.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Unit '$searchQuery' tidak ditemukan" else "Tidak ada unit dalam status ini",
                            color = TextMuted,
                            fontSize = 13.sp,
                        )
                    }
                }
            } else {
                items(displayedScooters, key = { it.id }) { scooter ->
                    UnitCard(
                        scooter = scooter,
                        onClick = { selectedScooterForAction = it },
                    )
                }
            }

            // Spacer for floating bottom bar clearance
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Unit Action Bottom Sheet
    selectedScooterForAction?.let { scooter ->
        UnitActionBottomSheet(
            scooter = scooter,
            onDismissRequest = { selectedScooterForAction = null },
            onDetailClick = { id ->
                selectedScooterForAction = null
                onOpenDetail(id)
            },
            onStartRentClick = {
                selectedScooterForAction = null
                onGoScan()
            },
            onReturnClick = { id ->
                selectedScooterForAction = null
                onGoScan()
            },
            onSwapClick = {
                selectedScooterForAction = null
                swapScooter = scooter
            },
            onResolveMaintenanceClick = { id ->
                selectedScooterForAction = null
                val maintId = scooter.activeMaintenance?.id
                if (maintId != null) {
                    scope.launch {
                        runCatching {
                            viewModel.completeMaintenance(maintId)
                        }.onSuccess {
                            sweetAlert.showSuccess("Sukses", "Maintenance unit $id berhasil diselesaikan")
                            viewModel.refresh()
                        }.onFailure { err ->
                            sweetAlert.showError("Gagal", err.toUserMessage())
                        }
                    }
                }
            },
        )
    }

    // Trouble swap dialog
    swapScooter?.let { scooter ->
        val availableScooters = remember(outletFilteredScooters) {
            outletFilteredScooters.filter { it.status == ScooterStatus.AVAILABLE }
        }
        var isSubmittingSwap by remember { mutableStateOf(false) }

        TroubleSwapDialog(
            scooter = scooter,
            availableScooters = availableScooters,
            onDismiss = { swapScooter = null },
            submitting = isSubmittingSwap,
            onConfirm = { replacementId, note, issue, markBroken ->
                scope.launch {
                    isSubmittingSwap = true
                    val success = viewModel.swapScooter(
                        scooterId = scooter.id,
                        replacementId = replacementId,
                        note = note,
                        issue = issue,
                        markBroken = markBroken,
                    )
                    isSubmittingSwap = false
                    if (success) {
                        sweetAlert.showSuccess("Sukses", "Tukar unit ${scooter.id} -> $replacementId berhasil")
                        swapScooter = null
                        viewModel.refresh()
                    } else {
                        sweetAlert.showError("Gagal", "Gagal menukar unit $replacementId")
                    }
                }
            },
        )
    }
}
