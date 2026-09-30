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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.util.ScooterColors
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.TypeLabels
import java.time.Duration
import java.time.LocalDateTime

private val PRESET_ISSUES = listOf(
    "Baterai Kurang",
    "Ban Kempes",
    "Rem Kurang Pakem",
    "Kecepatan Lemah",
    "Lainnya"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TroubleSwapDialog(
    scooter: Scooter,
    availableScooters: List<Scooter>,
    onDismiss: () -> Unit,
    onConfirm: (replacementId: String, note: String, issue: String?, markBroken: Boolean) -> Unit,
    submitting: Boolean = false
) {
    val isDark = LocalThemeIsDark.current
    val currentRegion = remember(scooter.type) { Outlets.getHomeOutletForType(scooter.type) }

    // Prioritize: 1. Same exact type, 2. Same region, 3. Numeric ID order (1:1 web logic)
    val sortedReplacements = remember(availableScooters, scooter.type, currentRegion) {
        availableScooters.sortedWith(
            compareByDescending<Scooter> { it.type.equals(scooter.type, ignoreCase = true) }
                .thenByDescending { Outlets.getHomeOutletForType(it.type) == currentRegion }
                .thenBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 9999 }
        )
    }

    var selectedReplacementId by remember {
        mutableStateOf(sortedReplacements.firstOrNull()?.id ?: "")
    }

    // Pilihan efektif (paritas web): pertahankan pilihan user bila masih valid
    // saat daftar refresh, sonst jatuh kembali ke kandidat pertama.
    // remember(sortedReplacements) lama me-reset pilihan user tiap refresh.
    val effectiveReplacementId = remember(selectedReplacementId, sortedReplacements) {
        if (sortedReplacements.any { it.id == selectedReplacementId }) selectedReplacementId
        else sortedReplacements.firstOrNull()?.id ?: ""
    }

    var selectedIssue by remember { mutableStateOf<String?>(PRESET_ISSUES[0]) }
    var customIssue by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var markBroken by remember { mutableStateOf(false) }
    var replacementDropdownOpen by remember { mutableStateOf(false) }

    val elapsedText = remember(scooter.lastUpdated) {
        val dt = DateUtils.parse(scooter.lastUpdated)
        if (dt != null) {
            val totalSecs = Duration.between(dt, LocalDateTime.now(DateUtils.WIB)).seconds.coerceAtLeast(0)
            DateUtils.formatDuration(totalSecs)
        } else {
            "-"
        }
    }

    val canSubmit by remember(selectedReplacementId, sortedReplacements, note, submitting) {
        derivedStateOf {
            effectiveReplacementId.isNotBlank() && note.isNotBlank() && !submitting
        }
    }

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
                        Icon(Icons.Filled.SwapHoriz, null, tint = Accent, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text("Tukar Unit Scooter", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Ganti unit pelanggan dengan unit ready lain", color = TextMuted, fontSize = 12.sp)
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !submitting,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Filled.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }

            // Current Unit Banner (Unit Lama)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val oldUnitColor = ScooterColors.getScooterNameColor(scooter.type, scooter.id, scooter.currentOutlet, isDark)
                Column {
                    Text("UNIT LAMA", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(scooter.id, color = oldUnitColor, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("DURASI BERJALAN", color = TextSubtle, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(elapsedText, color = Warning, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Replacement Unit Picker
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("PILIH UNIT PENGGANTI (READY)", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

                if (sortedReplacements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Red.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                            .border(1.dp, Red.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Text("Tidak ada unit ready yang tersedia untuk ditukar.", color = Red, fontSize = 12.sp)
                    }
                } else {
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Surface2)
                                .border(1.dp, Border, RoundedCornerShape(10.dp))
                                .clickable { replacementDropdownOpen = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val selectedBike = sortedReplacements.find { it.id == effectiveReplacementId }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val selColor = if (selectedBike != null) ScooterColors.getScooterNameColor(selectedBike.type, selectedBike.id, selectedBike.currentOutlet, isDark) else TextPrimary
                                Text(
                                    effectiveReplacementId.ifBlank { "Pilih unit..." },
                                    color = selColor,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextMuted)
                        }

                        DropdownMenu(
                            expanded = replacementDropdownOpen,
                            onDismissRequest = { replacementDropdownOpen = false },
                            modifier = Modifier
                                .background(Surface, RoundedCornerShape(10.dp))
                                .border(1.dp, Border, RoundedCornerShape(10.dp))
                        ) {
                            sortedReplacements.forEach { r ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val rColor = ScooterColors.getScooterNameColor(r.type, r.id, r.currentOutlet, isDark)
                                                Text(r.id, color = rColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Text(TypeLabels.of(r.type), color = TextMuted, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedReplacementId = r.id
                                        replacementDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Note (Wajib diisi)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("CATATAN ALASAN TUKAR *", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Text("Wajib diisi", color = Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = { Text("Contoh: Baterai habis di jalan, tukar ke unit ready...", color = TextSubtle, fontSize = 12.sp) },
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

            // Issue Categories
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("KATEGORI KENDALA", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PRESET_ISSUES.take(3).forEach { iss ->
                        val isSelected = selectedIssue == iss
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Accent else Surface2)
                                .border(1.dp, if (isSelected) Accent else Border, RoundedCornerShape(8.dp))
                                .clickable { selectedIssue = if (isSelected) null else iss }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iss,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PRESET_ISSUES.drop(3).forEach { iss ->
                        val isSelected = selectedIssue == iss
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Accent else Surface2)
                                .border(1.dp, if (isSelected) Accent else Border, RoundedCornerShape(8.dp))
                                .clickable { selectedIssue = if (isSelected) null else iss }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = iss,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (selectedIssue == "Lainnya") {
                    OutlinedTextField(
                        value = customIssue,
                        onValueChange = { customIssue = it },
                        placeholder = { Text("Rincian kendala lainnya...", color = TextSubtle, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }

            // Mark broken checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { markBroken = !markBroken }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Checkbox(
                    checked = markBroken,
                    onCheckedChange = { markBroken = it },
                    colors = CheckboxDefaults.colors(checkedColor = Accent)
                )
                Text(
                    text = "Tandai unit lama sebagai kendala / butuh perbaikan",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Action buttons
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
                        val issueText = if (selectedIssue == "Lainnya") {
                            customIssue.ifBlank { "Lainnya" }
                        } else selectedIssue

                        onConfirm(effectiveReplacementId, note.trim(), issueText, markBroken)
                    },
                    enabled = canSubmit,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.4f),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Memproses...", fontSize = 12.sp)
                    } else {
                        Text("Tukar Unit Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
