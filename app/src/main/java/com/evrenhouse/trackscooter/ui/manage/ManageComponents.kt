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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.SimpleDropdown
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.common.statusColor
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.util.ScooterColors
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.StatusOrder
import com.evrenhouse.trackscooter.util.TypeLabels

private val CircleShapeCompat = RoundedCornerShape(50)

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
    onTroubleSwap: ((Scooter) -> Unit)? = null,
    onEditScooter: ((Scooter) -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        // Toolbar
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("DAFTAR UNIT (${scooters.size})", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

            OutlinedTextField(
                value = search,
                onValueChange = onSearch,
                placeholder = { Text("Cari ID...", color = TextSubtle, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(16.dp)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SimpleDropdown(
                    label = if (filterStatus == "all") "Semua Status" else StatusLabels.of(filterStatus),
                    options = listOf(
                        "all" to "Semua Status",
                        "available" to "Unit Ready",
                        "in-use" to "Unit Diluar",
                        "maintenance" to "Unit Kendala",
                    ),
                    selected = filterStatus,
                    onSelect = onFilterStatus,
                    modifier = Modifier.weight(1f),
                )
                SimpleDropdown(
                    label = if (filterType == "all") "Semua Jenis" else TypeLabels.of(filterType),
                    options = listOf("all" to "Semua Jenis", "sd" to "Standar (SD)", "sj" to "Jumbo (SJ)"),
                    selected = filterType,
                    onSelect = onFilterType,
                    modifier = Modifier.weight(1f),
                )
            }
            SimpleDropdown(
                label = sortLabel(sortBy),
                options = listOf(
                    "id-asc" to "Urutkan: ID (A-Z)",
                    "id-desc" to "Urutkan: ID (Z-A)",
                    "today-checkout" to "Urutkan: Keluar Hari Ini (Terbanyak)",
                    "status" to "Urutkan: Status",
                    "type" to "Urutkan: Jenis",
                ),
                selected = sortBy,
                onSelect = onSortBy,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (scooters.isEmpty()) {
            Text("Tidak ada scooter ditemukan.", color = TextMuted, fontSize = 12.sp, modifier = Modifier.padding(28.dp))
        } else {
            scooters.forEach { scooter ->
                ScooterRow(
                    scooter = scooter,
                    todayCount = getTodayCount(scooter.id),
                    onOpenDetail = { onOpenDetail(scooter.id) },
                    onStatusChange = { onStatusChange(scooter, it) },
                    onDelete = { onDelete(scooter) },
                    onDownloadQr = { onDownloadQr(scooter) },
                    onTroubleSwap = onTroubleSwap,
                    onEditScooter = { onEditScooter?.invoke(scooter) },
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Border),
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
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenDetail)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val isDark = LocalThemeIsDark.current
                val nameColor = ScooterColors.getScooterNameColor(scooter.type, scooter.id, scooter.currentOutlet, isDark)
                Text(
                    text = scooter.id,
                    color = nameColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                )
                TypeBadge(scooter.type, id = scooter.id, outlet = scooter.currentOutlet)

                // Current Outlet Badge (Interactive)
                val currentOutletId = scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type)
                val outletLabel = Outlets.shortLabelOf(currentOutletId)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Surface2)
                        .border(1.dp, Border, RoundedCornerShape(6.dp))
                        .clickable { onEditScooter?.invoke() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    val outletColor = ScooterColors.getOutletColor(currentOutletId)
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = outletColor, modifier = Modifier.size(11.dp))
                    Text(
                        text = outletLabel,
                        color = TextPrimary,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = { onEditScooter?.invoke() }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Edit, contentDescription = "Atur Outlet & Unit", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDownloadQr, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.QrCode, contentDescription = "Unduh QR", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                StatusPillButton(
                    status = scooter.status,
                    onStatusChange = onStatusChange,
                )
                if (scooter.status == ScooterStatus.IN_USE && onTroubleSwap != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .background(Warning.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, Warning.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onTroubleSwap(scooter) }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    ) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(13.dp))
                        Text("Tukar", color = Warning, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(
                "${todayCount}x keluar",
                color = if (todayCount > 0) Accent else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (todayCount > 0) FontWeight.SemiBold else FontWeight.Normal,
            )
        }

        if (scooter.status == ScooterStatus.MAINTENANCE && !scooter.maintenanceNote.isNullOrBlank()) {
            Text(
                "Catatan: ${scooter.maintenanceNote}",
                color = TextMuted,
                fontSize = 11.sp,
                fontStyle = FontStyle.Italic,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun StatusChangeDialog(
    scooter: Scooter,
    newStatus: String,
    onDismiss: () -> Unit,
    onConfirm: (location: String, locationDetail: String?, issue: String, note: String?) -> Unit,
) {
    var location by remember { mutableStateOf("outlet") }
    var locationDetail by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf(scooter.maintenanceNote ?: "") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val title = when (newStatus) {
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
                        label = if (location == "outlet") "Di Outlet" else "Keluar / Luar",
                        options = listOf("outlet" to "Di Outlet", "luar" to "Keluar / Luar"),
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
                onConfirm(location, locationDetail.trim().ifBlank { null }, issue.trim(), note.trim().ifBlank { null })
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
    var selectedOutlet by remember { mutableStateOf(initialOutlet) }
    var selectedStatus by remember { mutableStateOf(scooter.status) }
    var location by remember { mutableStateOf("outlet") }
    var locationDetail by remember { mutableStateOf("") }
    var issue by remember { mutableStateOf(scooter.maintenanceNote ?: "") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        Text("Pindahkan pangkalan atau atur unit", color = TextMuted, fontSize = 12.sp)
                    }
                }

                IconButton(onClick = onDismiss, enabled = !submitting, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            // 1. Lokasi Outlet Saat Ini (Current Outlet)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("LOKASI OUTLET PANGKALAN", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

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
                        label = if (location == "outlet") "Di Outlet" else "Keluar / Luar",
                        options = listOf("outlet" to "Di Outlet", "luar" to "Keluar / Luar"),
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
                            onConfirm(selectedOutlet, selectedStatus, location, locationDetail.trim().ifBlank { null }, issue.trim(), note.trim().ifBlank { null })
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
    "id-desc" -> compareByDescending<Scooter> { it.id.numericPart() }.thenByDescending { it.id }
    "today-checkout" -> compareByDescending<Scooter> { todayCount(it.id) }
    "status" -> compareBy<Scooter> { StatusOrder.ALL[it.status] ?: 99 }.thenBy { it.id.numericPart() }
    "type" -> compareBy<Scooter> { it.type }.thenBy { it.id.numericPart() }
    else -> compareBy<Scooter> { it.id.numericPart() }.thenBy { it.id }
}

fun String.numericPart(): Int = this.filter(Char::isDigit).toIntOrNull() ?: 0

@Composable
fun StatusPillButton(
    status: String,
    onStatusChange: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val color = statusColor(status)

    Box {
        Row(
            modifier = Modifier
                .height(30.dp)
                .background(color.subtle, RoundedCornerShape(8.dp))
                .border(1.dp, color.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(modifier = Modifier.size(6.dp).background(color.color, CircleShapeCompat))
            Text(
                text = StatusLabels.of(status),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = color.color,
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = color.color,
                modifier = Modifier.size(14.dp),
            )
        }

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
                            Box(modifier = Modifier.size(6.dp).background(optColor.color, CircleShapeCompat))
                            Text(label, fontSize = 12.sp, color = if (value == status) Accent else TextPrimary)
                        }
                    },
                    onClick = {
                        onStatusChange(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

