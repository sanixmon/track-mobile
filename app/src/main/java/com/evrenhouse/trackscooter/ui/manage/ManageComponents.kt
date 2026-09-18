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
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.StatusOrder
import com.evrenhouse.trackscooter.util.TypeLabels

private val CircleShapeCompat = RoundedCornerShape(50)

@Composable
fun AddScooterForm(
    idInput: String,
    onIdInput: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    error: String?,
    submitting: Boolean,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("TAMBAH UNIT BARU", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)

        error?.let {
            Text(it, color = Red, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("ID Scooter (Opsional)", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${type.uppercase()}-", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { onIdInput(it.filter(Char::isDigit)) },
                    placeholder = { Text("099 (Auto jika kosong)", color = TextSubtle, fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Jenis Scooter", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            SimpleDropdown(
                label = TypeLabels.of(type),
                options = listOf("sd" to TypeLabels.of("sd"), "sj" to TypeLabels.of("sj")),
                selected = type,
                onSelect = onTypeChange,
            )
        }

        Button(
            onClick = onSubmit,
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
        ) {
            if (submitting) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
            }
            Spacer(Modifier.width(6.dp))
            Text(if (submitting) "Memproses..." else "Tambah Scooter", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
    onTroubleSwap: ((Scooter) -> Unit)? = null,
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
                        "available" to "Tersedia",
                        "in-use" to "Online",
                        "rusak" to "Offline / Rusak",
                        "maintenance" to "Maintenance",
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
            Column {
                Text(scooter.id, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                TypeBadge(scooter.type)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
            if (scooter.status == ScooterStatus.IN_USE) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .background(Accent.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .border(1.dp, Accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Box(
                            Modifier
                                .size(6.dp)
                                .background(Accent, CircleShapeCompat),
                        )
                        Text("Online", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    if (onTroubleSwap != null) {
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
            } else {
                StatusPillButton(
                    status = scooter.status,
                    onStatusChange = onStatusChange,
                )
            }
            Text(
                "${todayCount}x keluar",
                color = if (todayCount > 0) Accent else TextMuted,
                fontSize = 11.sp,
                fontWeight = if (todayCount > 0) FontWeight.SemiBold else FontWeight.Normal,
            )
        }

        if ((scooter.status == ScooterStatus.MAINTENANCE || scooter.status == ScooterStatus.RUSAK) && !scooter.maintenanceNote.isNullOrBlank()) {
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
    onConfirm: (location: String, issue: String, note: String) -> Unit,
) {
    var location by remember { mutableStateOf("outlet") }
    var issue by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val title = when (newStatus) {
        ScooterStatus.MAINTENANCE -> "Mulai Maintenance"
        ScooterStatus.RUSAK -> "Catatan Kerusakan"
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
                        onSelect = { location = it },
                    )
                    OutlinedTextField(
                        value = issue,
                        onValueChange = { issue = it },
                        label = { Text("Kendala", fontSize = 12.sp) },
                        placeholder = { Text("Contoh: Tidak menyala", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan (opsional)", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                } else if (newStatus == ScooterStatus.RUSAK) {
                    Text("Masukkan catatan kerusakan untuk unit ${scooter.id} (opsional):", fontSize = 13.sp)
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        placeholder = { Text("Contoh: Tidak menyala, baterai drop", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Text("Ubah status unit ${scooter.id} menjadi ${StatusLabels.of(newStatus)}?", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(location, issue.trim(), note.trim()) }) {
                Text("Simpan", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal", color = TextMuted, fontSize = 12.sp) }
        },
    )
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
                "available" to "Tersedia",
                "rusak" to "Offline / Rusak",
                "maintenance" to "Maintenance",
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

