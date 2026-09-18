package com.evrenhouse.trackscooter.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.TypeLabels
import java.time.Duration
import java.time.LocalDateTime

private val PRESET_ISSUES = listOf(
    "Baterai Drop / Habis",
    "Ban Bocor / Kempes",
    "Rem Rusak",
    "Lampu Mati",
    "Mesin Mati",
    "Lainnya",
)

@Composable
fun TroubleSwapDialog(
    scooter: Scooter,
    availableScooters: List<Scooter>,
    onDismiss: () -> Unit,
    onConfirm: (mode: String, replacementId: String?, structuredIssue: String, locationNote: String) -> Unit,
) {
    var mode by remember { mutableStateOf("swap") } // "swap" | "evacuate"
    var selectedIssue by remember { mutableStateOf(PRESET_ISSUES[0]) }
    var customIssue by remember { mutableStateOf("") }
    var locationNote by remember { mutableStateOf("") }

    // Prioritize available scooters with matching type
    val sortedReplacements = remember(availableScooters, scooter.type) {
        availableScooters.sortedWith(
            compareByDescending<Scooter> { it.type == scooter.type }
                .thenBy { it.id }
        )
    }

    var selectedReplacementId by remember {
        mutableStateOf(sortedReplacements.firstOrNull()?.id ?: "")
    }

    // Elapsed duration string
    val elapsedText = remember(scooter.lastUpdated) {
        val dt = DateUtils.parse(scooter.lastUpdated)
        if (dt != null) {
            val totalSecs = Duration.between(dt, LocalDateTime.now(DateUtils.WIB)).seconds.coerceAtLeast(0)
            val hrs = totalSecs / 3600
            val mins = (totalSecs % 3600) / 60
            if (hrs > 0) "${hrs}j ${mins}m" else "${mins} mnt"
        } else {
            "-"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Surface,
        titleContentColor = TextPrimary,
        textContentColor = TextMuted,
        shape = RoundedCornerShape(16.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Warning.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Warning, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Trouble & Tukar Unit", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Penanganan kendala scooter di jalan", fontSize = 11.sp, color = TextMuted)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = TextSubtle, modifier = Modifier.size(16.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Unit info banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface2, RoundedCornerShape(10.dp))
                        .border(1.dp, Border, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(scooter.id, color = Accent, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        TypeBadge(scooter.type)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(Red, CircleShape))
                        Text("Durasi: $elapsedText", color = Red, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Mode Selector Tabs
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("PILIHAN TINDAKAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 1.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface2, RoundedCornerShape(10.dp))
                            .border(1.dp, Border, RoundedCornerShape(10.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val isSwap = mode == "swap"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSwap) Accent.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (isSwap) Accent else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { mode = "swap" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = if (isSwap) Accent else TextMuted, modifier = Modifier.size(15.dp))
                                Text("Tukar Unit (Swap)", color = if (isSwap) Accent else TextMuted, fontSize = 11.sp, fontWeight = if (isSwap) FontWeight.Bold else FontWeight.Medium)
                            }
                        }

                        val isEvacuate = mode == "evacuate"
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isEvacuate) Warning.copy(alpha = 0.15f) else Color.Transparent)
                                .border(1.dp, if (isEvacuate) Warning else Color.Transparent, RoundedCornerShape(8.dp))
                                .clickable { mode = "evacuate" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = if (isEvacuate) Warning else TextMuted, modifier = Modifier.size(14.dp))
                                Text("Akhiri & Evakuasi", color = if (isEvacuate) Warning else TextMuted, fontSize = 11.sp, fontWeight = if (isEvacuate) FontWeight.Bold else FontWeight.Medium)
                            }
                        }
                    }
                }

                // Issue Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("JENIS KENDALA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 1.sp)
                    SimpleDropdown(
                        label = selectedIssue,
                        options = PRESET_ISSUES.map { it to it },
                        selected = selectedIssue,
                        onSelect = { selectedIssue = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (selectedIssue == "Lainnya") {
                        OutlinedTextField(
                            value = customIssue,
                            onValueChange = { customIssue = it },
                            placeholder = { Text("Tuliskan kendala spesifik...", color = TextSubtle, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            textStyle = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                // Location Note
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("LOKASI PENJEMPUTAN / CATATAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSubtle, letterSpacing = 1.sp)
                    OutlinedTextField(
                        value = locationNote,
                        onValueChange = { locationNote = it },
                        placeholder = { Text("Contoh: Jl. Sudirman depan Cafe X", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodySmall,
                    )
                }

                // Replacement scooter selector (only if mode == "swap")
                if (mode == "swap") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("PILIH UNIT PENGGANTI", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Accent, letterSpacing = 1.sp)
                            Text("${sortedReplacements.size} unit tersedia", fontSize = 10.sp, color = TextMuted)
                        }

                        if (sortedReplacements.isEmpty()) {
                            Text(
                                "Tidak ada unit berstatus Tersedia saat ini. Silakan pilih opsi 'Akhiri & Evakuasi'.",
                                color = Red,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                            )
                        } else {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            val currentChoice = sortedReplacements.firstOrNull { it.id == selectedReplacementId }
                                ?: sortedReplacements.first()

                            Box(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Surface3, RoundedCornerShape(10.dp))
                                        .border(1.dp, Border, RoundedCornerShape(10.dp))
                                        .clickable { dropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(currentChoice.id, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                        TypeBadge(currentChoice.type)
                                        if (currentChoice.type == scooter.type) {
                                            Text("(Tipe Sama)", color = Green, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = TextMuted)
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier
                                        .background(Surface, RoundedCornerShape(10.dp))
                                        .border(1.dp, Border, RoundedCornerShape(10.dp)),
                                ) {
                                    sortedReplacements.forEach { candidate ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                ) {
                                                    Text(candidate.id, color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    TypeBadge(candidate.type)
                                                    if (candidate.type == scooter.type) {
                                                        Text("(Tipe Sama)", color = Green, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedReplacementId = candidate.id
                                                dropdownExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canSubmit = mode != "swap" || selectedReplacementId.isNotBlank()
            Button(
                onClick = {
                    val issueTitle = if (selectedIssue == "Lainnya") {
                        customIssue.trim().ifBlank { "Kendala lain" }
                    } else {
                        selectedIssue
                    }
                    val locText = if (locationNote.isNotBlank()) "di ${locationNote.trim()}" else "di jalan"
                    val structuredIssue = if (mode == "swap") {
                        "[TUKAR -> $selectedReplacementId] $issueTitle $locText (Durasi: $elapsedText)"
                    } else {
                        "[EVAKUASI] $issueTitle $locText (Durasi: $elapsedText)"
                    }
                    onConfirm(mode, if (mode == "swap") selectedReplacementId else null, structuredIssue, locationNote.trim())
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = if (mode == "swap") Accent else Warning),
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(
                    if (mode == "swap") "Tukar Unit" else "Catat Evakuasi",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextMuted, fontSize = 12.sp)
            }
        },
    )
}
