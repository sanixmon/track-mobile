package com.evrenhouse.trackscooter.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import com.evrenhouse.trackscooter.util.Outlets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.SimpleDropdown
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.common.statusColor
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.util.ScooterColors
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.StatusOrder
import com.evrenhouse.trackscooter.util.TypeLabels

private val CircleShapeCompat = RoundedCornerShape(50)

@Composable
fun UnitStatusChip(
    status: String,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val color = statusColor(status)

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.subtle)
                .border(1.dp, color.color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(modifier = Modifier.size(6.dp).background(color.color, CircleShape))
            Text(
                text = StatusLabels.of(status),
                style = MaterialTheme.typography.labelMedium,
                color = color.text, // WCAG > 4.5:1 (amber gelap #8A5300 di light mode)
                maxLines = 1,
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = "Pilih status unit",
                tint = color.text,
                modifier = Modifier.size(14.dp),
            )
        }

        if (expanded) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Surface, RoundedCornerShape(10.dp))
                    .border(1.dp, Border, RoundedCornerShape(10.dp)),
            ) {
            listOf(
                ScooterStatus.AVAILABLE to "Unit Ready",
                ScooterStatus.IN_USE to "Unit Diluar",
                ScooterStatus.MAINTENANCE to "Unit Kendala",
            ).forEach { (value, label) ->
                val optColor = statusColor(value)
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(optColor.color, CircleShape))
                            Text(label, fontSize = 12.sp, color = if (value == status) Accent else TextPrimary)
                        }
                    },
                    onClick = {
                        expanded = false
                        if (value != status) {
                            onStatusChange(value)
                        }
                    },
                )
            }
        }
        }
    }
}

/**
 * Row unit padat 2 baris (tinggi ~58dp, minimal 5 row muat di layar 6" tanpa scroll).
 */
@Composable
fun UnitRow(
    scooter: Scooter,
    todayCount: Int,
    onOpenDetail: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDownloadQr: () -> Unit,
    activeOutlet: String = "all",
    onTroubleSwap: ((Scooter) -> Unit)? = null,
    onEditScooter: (() -> Unit)? = null,
    onEditMaintenance: ((Scooter) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var overflowMenuExpanded by remember { mutableStateOf(false) }
    val isDark = LocalThemeIsDark.current
    val isMaintenance = scooter.status == ScooterStatus.MAINTENANCE
    val am = scooter.activeMaintenance
    val isLuar = am?.location == "luar"
    val locDetail = am?.locationDetail?.takeIf { it.isNotBlank() }
    val issueText = am?.issue?.takeIf { it.isNotBlank() } ?: scooter.maintenanceNote

    // Hilangkan badge 'SB' bila hanya mengulang prefix ID
    val isRepeatingPrefix = scooter.id.startsWith(scooter.type, ignoreCase = true)
    val showTypeBadge = !isRepeatingPrefix
    // Hilangkan chip lokasi per row kecuali filter outlet sedang "Semua outlet"
    val showLocationChip = activeOutlet == "all"
    val currentOutletId = scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type)
    val outletLabel = Outlets.shortLabelOf(currentOutletId)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onEditScooter?.invoke() ?: onOpenDetail() })
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        // ── BARIS 1: ID, (Badge), (Lokasi) di kiri  ⟷  Chip Status mepet dengan Tombol Titik 3 di kanan ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // ID dengan onSurface netral untuk semua jenis (tanpa warna merah/hijau)
                Text(
                    text = scooter.id,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                )

                // Lokasi outlet (hanya jika filter "Semua outlet")
                if (showLocationChip) {
                    val outletColor = ScooterColors.getOutletColor(currentOutletId)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Surface2)
                            .border(0.5.dp, Border, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.5.dp),
                    ) {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = outletColor, modifier = Modifier.size(10.dp))
                        Text(
                            text = outletLabel,
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                        )
                    }
                }

                // Badge jenis armada hanya bila tidak mengulang prefix ID
                if (showTypeBadge) {
                    TypeBadge(scooter.type, id = scooter.id, outlet = scooter.currentOutlet)
                }
            }

            // Aksi Kanan: Chip Status mepet dengan Menu Titik 3 (bersih tanpa tombol tukar & tanpa unduh QR luar)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Chip Status Interaktif dengan WCAG contrast 4.5:1
                UnitStatusChip(
                    status = scooter.status,
                    onStatusChange = onStatusChange,
                )

                // Overflow Menu Titik 3 (Unduh QR, Edit, Detail, Hapus di dalam menu)
                Box {
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = 36.dp, minHeight = 36.dp)
                            .clip(CircleShape)
                            .clickable { overflowMenuExpanded = true }
                            .padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "Opsi lainnya untuk unit ${scooter.id}",
                            tint = TextMuted,
                            modifier = Modifier.size(17.dp),
                        )
                    }

                    if (overflowMenuExpanded) {
                        DropdownMenu(
                            expanded = overflowMenuExpanded,
                            onDismissRequest = { overflowMenuExpanded = false },
                            modifier = Modifier
                                .background(Surface, RoundedCornerShape(12.dp))
                                .border(1.dp, Border, RoundedCornerShape(12.dp)),
                        ) {
                            DropdownMenuItem(
                                text = { Text("Atur Outlet & Unit", fontSize = 12.sp, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Filled.Edit, null, tint = TextMuted, modifier = Modifier.size(15.dp)) },
                                onClick = {
                                    overflowMenuExpanded = false
                                    onEditScooter?.invoke()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Unduh QR Code", fontSize = 12.sp, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Filled.QrCode, null, tint = TextMuted, modifier = Modifier.size(15.dp)) },
                                onClick = {
                                    overflowMenuExpanded = false
                                    onDownloadQr()
                                },
                            )
                            if (isMaintenance) {
                                val editMaintenanceAction = onEditMaintenance ?: onEditScooter?.let { edit -> { _: Scooter -> edit() } }
                                if (editMaintenanceAction != null) {
                                    DropdownMenuItem(
                                        text = { Text("Edit Kendala & Catatan", fontSize = 12.sp, color = TextPrimary) },
                                        leadingIcon = { Icon(Icons.Filled.Schedule, null, tint = Warning, modifier = Modifier.size(15.dp)) },
                                        onClick = {
                                            overflowMenuExpanded = false
                                            editMaintenanceAction(scooter)
                                        },
                                    )
                                }
                            }
                            DropdownMenuItem(
                                text = { Text("Detail Unit", fontSize = 12.sp, color = TextPrimary) },
                                leadingIcon = { Icon(Icons.Filled.Tune, null, tint = TextMuted, modifier = Modifier.size(15.dp)) },
                                onClick = {
                                    overflowMenuExpanded = false
                                    onOpenDetail()
                                },
                            )
                            HorizontalDivider(color = Border, thickness = 0.5.dp)
                            DropdownMenuItem(
                                text = { Text("Hapus Unit", fontSize = 12.sp, color = Red, fontWeight = FontWeight.SemiBold) },
                                leadingIcon = { Icon(Icons.Filled.Delete, null, tint = Red, modifier = Modifier.size(15.dp)) },
                                onClick = {
                                    overflowMenuExpanded = false
                                    onDelete()
                                },
                            )
                        }
                    }
                }
            }
        }

        // ── BARIS 2: Timestamp, Catatan inline (1 baris + ellipsis) & pensil edit, "Keluar: Nx" ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Ikon jam + timestamp kontekstual
                val timestampRaw = if (isMaintenance) (am?.startedAt ?: scooter.lastUpdated) else scooter.lastUpdated
                val timestampFormatted = DateUtils.formatContextualTime(timestampRaw)
                val timeColor = if (isMaintenance) statusColor(ScooterStatus.MAINTENANCE).text else TextSubtle

                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = timeColor,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = timestampFormatted,
                    color = timeColor,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )

                // Indikator lokasi perbaikan luar/outlet
                if (isMaintenance) {
                    Text("·", color = Border, fontSize = 10.sp)
                    Text(
                        text = if (isLuar) (if (locDetail != null) "Luar ($locDetail)" else "Luar") else "Di Outlet",
                        color = if (isLuar) Red else TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = if (isLuar) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                    )
                }

                // Catatan inline 1 baris bersih tanpa tombol pensil yang mengotori tampilan
                if (!issueText.isNullOrBlank()) {
                    Text("·", color = Border, fontSize = 10.sp)
                    Text(
                        text = issueText,
                        color = TextMuted,
                        fontSize = 10.5.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                }
            }

            // Hitungan keluar hari ini: "Keluar: Nx" (disembunyikan bila 0x)
            if (todayCount > 0) {
                Text(
                    text = "Keluar: ${todayCount}x",
                    color = Accent,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }
    }
}

@Composable
fun ScooterRow(
    scooter: Scooter,
    todayCount: Int,
    onOpenDetail: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onDownloadQr: () -> Unit,
    onTroubleSwap: ((Scooter) -> Unit)? = null,
    onEditScooter: (() -> Unit)? = null,
    onEditMaintenance: ((Scooter) -> Unit)? = null,
) {
    UnitRow(
        scooter = scooter,
        todayCount = todayCount,
        onOpenDetail = onOpenDetail,
        onStatusChange = onStatusChange,
        onDelete = onDelete,
        onDownloadQr = onDownloadQr,
        onTroubleSwap = onTroubleSwap,
        onEditScooter = onEditScooter,
        onEditMaintenance = onEditMaintenance,
    )
}

/**
 * Filter status cepat (Semua, Ready, Diluar, Kendala) di bawah search bar.
 */
@Composable
fun ManageFilterChips(
    selectedStatus: String,
    totalCount: Int,
    readyCount: Int,
    inUseCount: Int,
    maintenanceCount: Int,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StatusFilterPill(
            label = "Semua",
            count = totalCount,
            selected = selectedStatus == "all",
            onClick = { onStatusSelected("all") },
        )
        StatusFilterPill(
            label = "Ready",
            count = readyCount,
            selected = selectedStatus == ScooterStatus.AVAILABLE,
            onClick = { onStatusSelected(ScooterStatus.AVAILABLE) },
            activeColor = Green,
        )
        StatusFilterPill(
            label = "Diluar",
            count = inUseCount,
            selected = selectedStatus == ScooterStatus.IN_USE,
            onClick = { onStatusSelected(ScooterStatus.IN_USE) },
            activeColor = Accent,
        )
        StatusFilterPill(
            label = "Kendala",
            count = maintenanceCount,
            selected = selectedStatus == ScooterStatus.MAINTENANCE,
            onClick = { onStatusSelected(ScooterStatus.MAINTENANCE) },
            activeColor = Warning,
        )
    }
}

@Composable
private fun StatusFilterPill(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = Accent,
) {
    val isDark = LocalThemeIsDark.current
    val bg = if (selected) activeColor.copy(alpha = if (isDark) 0.18f else 0.12f) else Surface2
    val border = if (selected) activeColor else Border
    val text = if (selected) {
        if (activeColor == Warning && !isDark) Color(0xFF8A5300) else activeColor
    } else {
        TextMuted
    }

    Row(
        modifier = Modifier
            .defaultMinSize(minHeight = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Text(
            text = label,
            color = text,
            fontSize = 11.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
        Box(
            modifier = Modifier
                .background(if (selected) activeColor.copy(alpha = 0.2f) else Surface3, RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp),
        ) {
            Text(
                text = "$count",
                color = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

/**
 * Toolbar pencarian, tombol filter & urutkan, dan quick status filter chips
 * dengan tinggi search field ~48dp dan jarak vertikal rapat (<= 20% tinggi layar).
 */
@Composable
fun ManageSearchBarSection(
    search: String,
    onSearch: (String) -> Unit,
    filterStatus: String,
    onFilterStatus: (String) -> Unit,
    filterType: String,
    onFilterType: (String) -> Unit,
    sortBy: String,
    onSortBy: (String) -> Unit,
    totalCount: Int,
    readyCount: Int,
    inUseCount: Int,
    maintenanceCount: Int,
    modifier: Modifier = Modifier,
) {
    var showFilterModal by remember { mutableStateOf(false) }
    val activeFilterCount = (if (filterStatus != "all") 1 else 0) +
        (if (filterType != "all") 1 else 0) +
        (if (sortBy != "id-asc") 1 else 0)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Search Bar & Filter Button (tinggi ~48dp, rapat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = onSearch,
                placeholder = { Text("Cari ID unit...", color = TextSubtle, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(16.dp)) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodySmall,
            )

            // Tombol Filter & Urutkan
            Row(
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (activeFilterCount > 0) Accent.copy(alpha = 0.15f) else Surface2)
                    .border(1.dp, if (activeFilterCount > 0) Accent else Border, RoundedCornerShape(10.dp))
                    .clickable { showFilterModal = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Tune,
                    contentDescription = "Filter dan Urutkan",
                    tint = if (activeFilterCount > 0) Accent else TextPrimary,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = if (activeFilterCount > 0) "Filter ($activeFilterCount)" else "Filter & Urutkan",
                    color = if (activeFilterCount > 0) Accent else TextPrimary,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Quick Status Filter Chips
        ManageFilterChips(
            selectedStatus = filterStatus,
            totalCount = totalCount,
            readyCount = readyCount,
            inUseCount = inUseCount,
            maintenanceCount = maintenanceCount,
            onStatusSelected = onFilterStatus,
        )

        // Modal Filter & Urutkan
        if (showFilterModal) {
            ManageFilterDialog(
                currentStatus = filterStatus,
                currentType = filterType,
                currentSort = sortBy,
                onApply = { newStatus, newType, newSort ->
                    onFilterStatus(newStatus)
                    onFilterType(newType)
                    onSortBy(newSort)
                    showFilterModal = false
                },
                onDismiss = { showFilterModal = false },
            )
        }
    }
}

@Composable
fun ScooterList(
    scooters: List<Scooter>,
    search: String,
    onSearch: (String) -> Unit,
    filterStatus: String,
    onFilterStatus: (String) -> Unit,
    filterType: String,
    onFilterType: (String) -> Unit,
    sortBy: String,
    onSortBy: (String) -> Unit,
    getTodayCount: (String) -> Int,
    onOpenDetail: (String) -> Unit,
    onStatusChange: (Scooter, String) -> Unit,
    onDelete: (Scooter) -> Unit,
    onDownloadQr: (Scooter) -> Unit,
    activeOutlet: String = "all",
    onTroubleSwap: ((Scooter) -> Unit)? = null,
    onEditScooter: ((Scooter) -> Unit)? = null,
    onEditMaintenance: ((Scooter) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val availableCount = remember(scooters) { scooters.count { it.status == ScooterStatus.AVAILABLE } }
    val inUseCount = remember(scooters) { scooters.count { it.status == ScooterStatus.IN_USE } }
    val maintenanceCount = remember(scooters) { scooters.count { it.status == ScooterStatus.MAINTENANCE } }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ManageSearchBarSection(
            search = search,
            onSearch = onSearch,
            filterStatus = filterStatus,
            onFilterStatus = onFilterStatus,
            filterType = filterType,
            onFilterType = onFilterType,
            sortBy = sortBy,
            onSortBy = onSortBy,
            totalCount = scooters.size,
            readyCount = availableCount,
            inUseCount = inUseCount,
            maintenanceCount = maintenanceCount,
        )
        // Daftar baris unit langsung di atas background dengan HorizontalDivider (tanpa card ganda)
        if (scooters.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Tidak ada unit scooter yang cocok.", color = TextMuted, fontSize = 12.sp)
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                scooters.forEach { scooter ->
                    key(scooter.id) {
                        UnitRow(
                            scooter = scooter,
                            todayCount = getTodayCount(scooter.id),
                            activeOutlet = activeOutlet,
                            onOpenDetail = { onOpenDetail(scooter.id) },
                            onStatusChange = { onStatusChange(scooter, it) },
                            onDelete = { onDelete(scooter) },
                            onDownloadQr = { onDownloadQr(scooter) },
                            onTroubleSwap = onTroubleSwap,
                            onEditScooter = { onEditScooter?.invoke(scooter) },
                            onEditMaintenance = onEditMaintenance,
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Border.copy(alpha = 0.5f),
                            thickness = 0.5.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChangeDialog(
    scooter: Scooter,
    newStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (location: String, locationDetail: String?, issue: String, note: String?) -> Unit,
    /**
     * Judul kustom untuk memisahkan mode:
     * - null = "Mulai Maintenance" (entry operasional baru via ubah status)
     * - "Edit Kendala ..." = koreksi data lapangan yang sudah berjalan
     *   (mirip web showMaintenanceDialog title Mulai vs Edit).
     */
    dialogTitle: String? = null,
) {
    var location by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.location ?: "outlet") }
    var locationDetail by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.locationDetail ?: "") }
    var issue by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.issue ?: scooter.maintenanceNote ?: "") }
    var note by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.note ?: "") }
    var errorMessage by remember(scooter, newStatus) { mutableStateOf<String?>(null) }

    val title = dialogTitle ?: when (newStatus) {
        ScooterStatus.MAINTENANCE -> "Mulai Maintenance"
        else -> "Ubah Status"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        titleContentColor = TextPrimary,
        textContentColor = TextMuted,
        title = { Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (newStatus == ScooterStatus.MAINTENANCE) {
                    Text("Catat lokasi dan kendala untuk unit ${scooter.id} agar tim perbaikan dapat bertindak.", fontSize = 13.sp)
                    SimpleDropdown(
                        label = if (location == "outlet") "Di Outlet" else "Keluar / Di Luar",
                        options = listOf("outlet" to "Di Outlet", "luar" to "Keluar / Di Luar"),
                        selected = location,
                        onSelect = {
                            location = it
                            errorMessage = null
                        },
                    )
                    if (location == "luar") {
                        OutlinedTextField(
                            value = locationDetail,
                            onValueChange = {
                                locationDetail = it
                                errorMessage = null
                            },
                            label = { Text("Nama Tempat Maintenance *", fontSize = 12.sp) },
                            placeholder = { Text("Contoh: Bengkel Pak Budi, Toko ABC", color = TextSubtle, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                        )
                    }
                    OutlinedTextField(
                        value = issue,
                        onValueChange = {
                            issue = it
                            errorMessage = null
                        },
                        label = { Text("Kendala / Kerusakan *", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: Baterai drop, rem blong", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan Tambahan (opsional)", fontSize = 12.sp) },
                        placeholder = { Text("Detail kondisi, suku cadang, dll.", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text("Ubah status unit ${scooter.id} menjadi ${StatusLabels.of(newStatus)}?", fontSize = 13.sp)
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (newStatus == ScooterStatus.MAINTENANCE) {
                    if (issue.trim().isBlank()) {
                        errorMessage = "Kendala / kerusakan wajib diisi"
                        return@TextButton
                    }
                    if (location == "luar" && locationDetail.trim().isBlank()) {
                        errorMessage = "Nama tempat maintenance wajib diisi jika di luar outlet"
                        return@TextButton
                    }
                }
                // Kirim "" (bukan null) saat outlet agar server clear location_detail.
                // ApiClient explicitNulls=false meng-omit null sehingga ghost detail tidak ke-clear.
                val detailToSend = if (newStatus == ScooterStatus.MAINTENANCE && location == "luar") {
                    locationDetail.trim()
                } else if (newStatus == ScooterStatus.MAINTENANCE) {
                    ""
                } else {
                    null
                }
                onConfirm(location, detailToSend, issue.trim(), note.trim().ifBlank { null })
            }) {
                Text("Simpan", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted, fontSize = 12.sp) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScooterDialog(
    scooter: Scooter,
    onDismiss: () -> Unit,
    onConfirm: (currentOutlet: String, status: String, location: String?, locationDetail: String?, issue: String?, note: String?) -> Unit,
    submitting: Boolean = false,
) {
    val initialOutlet = remember(scooter.currentOutlet, scooter.type) {
        scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type)
    }
    var selectedOutlet by remember(scooter.id, initialOutlet) { mutableStateOf(initialOutlet) }
    var selectedStatus by remember(scooter.id) { mutableStateOf(scooter.status) }
    var location by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.location ?: "outlet") }
    var locationDetail by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.locationDetail ?: "") }
    var issue by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.issue ?: scooter.maintenanceNote ?: "") }
    var note by remember(scooter) { mutableStateOf(scooter.activeMaintenance?.note ?: "") }
    var errorMessage by remember(scooter.id) { mutableStateOf<String?>(null) }

    var outletDropdownOpen by remember { mutableStateOf(false) }
    var statusDropdownOpen by remember { mutableStateOf(false) }
    BasicAlertDialog(
        onDismissRequest = { if (!submitting) onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Tune, null, tint = Accent, modifier = Modifier.size(18.dp))
                    }
                    val isDark = LocalThemeIsDark.current
                    val nameColor = ScooterColors.getScooterNameColor(scooter.type, scooter.id, scooter.currentOutlet, isDark)
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Atur Armada", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(scooter.id, color = nameColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Text("Pindahkan outlet atau atur unit", color = TextMuted, fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onDismiss, enabled = !submitting, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            // 1. Lokasi Outlet Saat Ini (Current Outlet)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("LOKASI OUTLET", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Surface2)
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                            .clickable { outletDropdownOpen = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.LocationOn, null, tint = Accent, modifier = Modifier.size(16.dp))
                            Text(Outlets.labelOf(selectedOutlet), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Filled.ArrowDropDown, null, tint = TextMuted)
                    }

                    DropdownMenu(
                        expanded = outletDropdownOpen,
                        onDismissRequest = { outletDropdownOpen = false },
                        modifier = Modifier
                            .background(Surface, RoundedCornerShape(10.dp))
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                    ) {
                        Outlets.OPERATIONAL.forEach { o ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(o.label, color = if (selectedOutlet == o.id) Accent else TextPrimary, fontWeight = if (selectedOutlet == o.id) FontWeight.Bold else FontWeight.Normal)
                                    }
                                },
                                 onClick = {
                                    selectedOutlet = o.id
                                    outletDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }


            // 3. Status Armada
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("STATUS OPERASIONAL", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

                Box {
                    val statusColor = statusColor(selectedStatus)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Surface2)
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                            .clickable { statusDropdownOpen = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.size(8.dp).background(statusColor.color, CircleShapeCompat))
                            Text(StatusLabels.of(selectedStatus), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Filled.ArrowDropDown, null, tint = TextMuted)
                    }

                    DropdownMenu(
                        expanded = statusDropdownOpen,
                        onDismissRequest = { statusDropdownOpen = false },
                        modifier = Modifier
                            .background(Surface, RoundedCornerShape(10.dp))
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                    ) {
                        listOf(
                            ScooterStatus.AVAILABLE to "Unit Ready",
                            ScooterStatus.IN_USE to "Unit Diluar",
                            ScooterStatus.MAINTENANCE to "Unit Kendala",
                        ).forEach { (value, label) ->
                            val c = statusColor(value)
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(modifier = Modifier.size(6.dp).background(c.color, CircleShapeCompat))
                                        Text(label, color = if (selectedStatus == value) Accent else TextPrimary, fontWeight = if (selectedStatus == value) FontWeight.Bold else FontWeight.Normal)
                                    }
                                },
                                onClick = {
                                    selectedStatus = value
                                    statusDropdownOpen = false
                                }
                            )
                        }
                    }
                }
            }
            if (selectedStatus == ScooterStatus.MAINTENANCE) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("LOKASI PERBAIKAN", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    SimpleDropdown(
                        label = if (location == "outlet") "Di Outlet" else "Keluar / Di Luar",
                        options = listOf("outlet" to "Di Outlet", "luar" to "Keluar / Di Luar"),
                        selected = location,
                        onSelect = {
                            location = it
                            errorMessage = null
                        },
                    )
                }

                if (location == "luar") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("NAMA TEMPAT MAINTENANCE *", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                        OutlinedTextField(
                            value = locationDetail,
                            onValueChange = {
                                locationDetail = it
                                errorMessage = null
                            },
                            placeholder = { Text("Contoh: Bengkel Pak Budi, Toko ABC", color = TextSubtle, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("KENDALA / KERUSAKAN *", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    OutlinedTextField(
                        value = issue,
                        onValueChange = {
                            issue = it
                            errorMessage = null
                        },
                        placeholder = { Text("Contoh: Baterai drop, rem blong", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                }
            }


            // 4. Catatan (Note)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("CATATAN UNIT / KENDALA", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Catatan teknis, mutasi outlet, atau perbaikan...", color = TextSubtle, fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 3,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Border,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                    )
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !submitting,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Text("Batal", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        if (selectedStatus == ScooterStatus.MAINTENANCE) {
                            if (issue.trim().isBlank()) {
                                errorMessage = "Kendala / kerusakan wajib diisi"
                                return@Button
                            }
                            if (location == "luar" && locationDetail.trim().isBlank()) {
                                errorMessage = "Nama tempat maintenance wajib diisi jika di luar outlet"
                                return@Button
                            }
                            // "" untuk outlet agar server clear (explicitNulls=false omit null).
                            val detailToSend = if (location == "luar") locationDetail.trim() else ""
                            onConfirm(selectedOutlet, selectedStatus, location, detailToSend, issue.trim(), note.trim().ifBlank { null })
                        } else {
                            onConfirm(selectedOutlet, selectedStatus, null, null, null, note.trim().ifBlank { null })
                        }
                    },
                    enabled = !submitting,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Menyimpan...", fontSize = 12.sp)
                    } else {
                        Text("Simpan Perubahan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.ManageActionButton(
    text: String,
    icon: @Composable (Color) -> Unit,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
    ) {
        icon(Accent)
        Spacer(Modifier.width(6.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

fun sortLabel(sortBy: String): String = when (sortBy) {
    "id-asc" -> "Urutkan: ID (A-Z)"
    "id-desc" -> "Urutkan: ID (Z-A)"
    "today-checkout" -> "Urutkan: Keluar Hari Ini (Terbanyak)"
    "status" -> "Urutkan: Status"
    "type" -> "Urutkan: Jenis"
    else -> "Urutkan: ID (A-Z)"
}

fun compareScooters(sortBy: String, todayCount: (String) -> Int): Comparator<Scooter> = when (sortBy) {
    "id-desc" -> compareByDescending<Scooter> { it.id.idPrefix() }.thenByDescending { it.id.numericPart() }
    "today-checkout" -> compareByDescending<Scooter> { todayCount(it.id) }
    "status" -> compareBy<Scooter> { StatusOrder.ALL[it.status] ?: 99 }.thenBy { it.id.idPrefix() }.thenBy { it.id.numericPart() }
    "type" -> compareBy<Scooter> { it.type }.thenBy { it.id.idPrefix() }.thenBy { it.id.numericPart() }
    else -> compareBy<Scooter> { it.id.idPrefix() }.thenBy { it.id.numericPart() }
}

fun String.idPrefix(): String = this.filter { !it.isDigit() }
fun String.numericPart(): Int = this.filter(Char::isDigit).toIntOrNull() ?: 0

@Composable
fun StatusPillButton(
    status: String,
    onStatusChange: (String) -> Unit,
) {
    UnitStatusChip(
        status = status,
        onStatusChange = onStatusChange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageFilterDialog(
    currentStatus: String,
    currentType: String,
    currentSort: String,
    onApply: (status: String, type: String, sort: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var tempStatus by remember { mutableStateOf(currentStatus) }
    var tempType by remember { mutableStateOf(currentType) }
    var tempSort by remember { mutableStateOf(currentSort) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    Text("Filter & Urutkan Unit", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            // Section 1: Status
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("STATUS UNIT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                val statusOptions = listOf(
                    "all" to "Semua",
                    ScooterStatus.AVAILABLE to "Ready",
                    ScooterStatus.IN_USE to "Diluar",
                    ScooterStatus.MAINTENANCE to "Kendala",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    statusOptions.forEach { (key, label) ->
                        val selected = tempStatus == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Accent else Surface2)
                                .border(1.dp, if (selected) Accent else Border, RoundedCornerShape(8.dp))
                                .clickable { tempStatus = key }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.White else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }

            // Section 2: Tipe / Jenis Scooter
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("JENIS / TIPE SCOOTER", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                val typeOptions = listOf("all" to "Semua Jenis") + TypeLabels.ALL.map { (k, v) -> k to v }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    typeOptions.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            rowOptions.forEach { (key, label) ->
                                val selected = tempType == key
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) Accent else Surface2)
                                        .border(1.dp, if (selected) Accent else Border, RoundedCornerShape(8.dp))
                                        .clickable { tempType = key }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.CenterStart,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        if (key != "all") {
                                            TypeBadge(key)
                                        }
                                        Text(
                                            text = label,
                                            color = if (selected) Color.White else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                            if (rowOptions.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Section 3: Urutkan Berdasarkan
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("URUTKAN BERDASARKAN", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                val sortOptions = listOf(
                    "id-asc" to "ID Unit (A - Z)",
                    "id-desc" to "ID Unit (Z - A)",
                    "today-checkout" to "Keluar Hari Ini (Terbanyak)",
                    "status" to "Status Unit",
                    "type" to "Jenis Scooter",
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sortOptions.forEach { (key, label) ->
                        val selected = tempSort == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) Accent.copy(alpha = 0.12f) else Surface2)
                                .border(1.dp, if (selected) Accent else Border, RoundedCornerShape(8.dp))
                                .clickable { tempSort = key }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Accent else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(Accent, CircleShape),
                                )
                            }
                        }
                    }
                }
            }

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = {
                        tempStatus = "all"
                        tempType = "all"
                        tempSort = "id-asc"
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted),
                ) {
                    Text("Reset", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        onApply(tempStatus, tempType, tempSort)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text("Terapkan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "UnitRow - Normal Ready", showBackground = true)
@Composable
private fun UnitRowReadyPreview() {
    TrackScooterTheme(isDark = false) {
        UnitRow(
            scooter = Scooter(
                id = "SB-01",
                type = "sb",
                status = ScooterStatus.AVAILABLE,
                currentOutlet = "utara-motor",
                lastUpdated = "2026-10-01T10:00:00Z",
            ),
            todayCount = 3,
            onOpenDetail = {},
            onStatusChange = {},
            onDelete = {},
            onDownloadQr = {},
        )
    }
}

@Preview(name = "UnitRow - In Use", showBackground = true)
@Composable
private fun UnitRowInUsePreview() {
    TrackScooterTheme(isDark = false) {
        UnitRow(
            scooter = Scooter(
                id = "FZ-12",
                type = "fz",
                status = ScooterStatus.IN_USE,
                currentOutlet = "utara-motor",
                lastUpdated = "2026-10-01T09:30:00Z",
            ),
            todayCount = 1,
            onOpenDetail = {},
            onStatusChange = {},
            onDelete = {},
            onDownloadQr = {},
            onTroubleSwap = {},
        )
    }
}

@Preview(name = "UnitRow - Maintenance Long Note", showBackground = true)
@Composable
private fun UnitRowMaintenanceLongNotePreview() {
    TrackScooterTheme(isDark = false) {
        UnitRow(
            scooter = Scooter(
                id = "EX-05",
                type = "ex",
                status = ScooterStatus.MAINTENANCE,
                currentOutlet = "utara-motor",
                lastUpdated = "2026-10-01T08:15:00Z",
                maintenanceNote = "Baterai bocor dan rem belakang berdecit saat turunan tajam",
            ),
            todayCount = 0,
            onOpenDetail = {},
            onStatusChange = {},
            onDelete = {},
            onDownloadQr = {},
            onEditMaintenance = {},
        )
    }
}

