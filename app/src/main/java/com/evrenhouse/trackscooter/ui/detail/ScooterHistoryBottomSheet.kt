package com.evrenhouse.trackscooter.ui.detail

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.TechnicalActivity
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
import com.evrenhouse.trackscooter.util.DeviceLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScooterHistoryBottomSheet(
    scooter: Scooter,
    activities: List<TechnicalActivity>,
    isLoading: Boolean,
    selectedPreset: String,
    selectedCategory: String,
    onSelectPreset: (preset: String, customStart: String, customEnd: String) -> Unit,
    onSelectCategory: (category: String) -> Unit,
    onExportXlsx: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    val cal = Calendar.getInstance()
    val showDatePicker = { onDateSelected: (String) -> Unit ->
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(formatted)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Surface2, RoundedCornerShape(10.dp))
                            .border(1.dp, Border, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Schedule, contentDescription = null, tint = Accent, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Riwayat Aktivitas", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(Surface2, RoundedCornerShape(6.dp))
                                    .border(1.dp, Border, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(scooter.id, color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Text(
                            "${TypeLabels.of(scooter.type)} · Riwayat pemakaian & catatan servis",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Tutup", tint = TextMuted, modifier = Modifier.size(18.dp))
                }
            }

            // ── 1. Snapshot Kondisi Fisik Terkini ──
            val dc = scooter.deviceCondition
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "SNAPSHOT KONDISI FISIK TERKINI",
                    color = TextSubtle,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ConditionItem("Spakbor", DeviceLabels.setelan[dc?.setelan] ?: dc?.setelan ?: "-", modifier = Modifier.weight(1f))
                    ConditionItem("Lampu", DeviceLabels.lampu[dc?.lampu] ?: dc?.lampu ?: "-", modifier = Modifier.weight(1f))
                    ConditionItem(
                        "Baterai",
                        DeviceLabels.baterai[dc?.baterai] ?: dc?.baterai ?: "-",
                        highlight = dc?.baterai == "drop",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val monitorText = if (dc?.monitor == "lain") (dc.monitorDetail ?: "Lain-lain") else (DeviceLabels.monitor[dc?.monitor] ?: dc?.monitor ?: "-")
                    ConditionItem("Monitor", monitorText, highlight = dc?.monitor != null && dc.monitor != "normal", modifier = Modifier.weight(1f))
                    ConditionItem("Rem", DeviceLabels.rem[dc?.rem] ?: dc?.rem ?: "-", highlight = dc?.rem == "rusak", modifier = Modifier.weight(1f))
                    ConditionItem("Ban", DeviceLabels.ban[dc?.ban] ?: dc?.ban ?: "-", highlight = dc?.ban == "botak", modifier = Modifier.weight(1f))
                }
            }

            // ── 2. Preset Filter Chips + Export Button ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        "all" to "Semua",
                        "today" to "Hari Ini",
                        "7d" to "7 Hari",
                        "30d" to "30 Hari",
                        "month" to "Bulan Ini",
                        "custom" to "Kustom",
                    ).forEach { (id, label) ->
                        val isSelected = selectedPreset == id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Accent else Surface2)
                                .clickable {
                                    if (id == "custom") {
                                        showDatePicker { start ->
                                            showDatePicker { end ->
                                                onSelectPreset("custom", start, end)
                                            }
                                        }
                                    } else {
                                        onSelectPreset(id, "", "")
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                label,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.width(8.dp))

                Button(
                    onClick = onExportXlsx,
                    enabled = !isLoading,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Icon(Icons.Filled.FileDownload, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Excel", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ── 3. Category Filter Pills ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        "all" to "Semua",
                        "usage" to "Pemakaian",
                        "maintenance" to "Perbaikan",
                    ).forEach { (catId, label) ->
                        val isSelected = selectedCategory == catId
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isSelected) Surface3 else Color.Transparent)
                                .border(1.dp, if (isSelected) Border else Color.Transparent, CircleShape)
                                .clickable { onSelectCategory(catId) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                label,
                                color = if (isSelected) TextPrimary else TextMuted,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Text(
                    "${activities.size} aktivitas",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            // ── 4. Activities Timeline List ──
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp))
                }
            } else if (activities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Surface2.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada riwayat aktivitas pada periode ini.", color = TextMuted, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(activities, key = { idx, a -> a.id.ifBlank { "act_$idx" } }) { idx, actItem ->
                        val isUsage = actItem.type == "usage"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Surface2.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .border(1.dp, Border, RoundedCornerShape(10.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (isUsage) Accent.copy(alpha = 0.15f) else Warning.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isUsage) Icons.Filled.Schedule else Icons.Filled.Construction,
                                    contentDescription = null,
                                    tint = if (isUsage) Accent else Warning,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(if (isUsage) Accent.copy(alpha = 0.12f) else Warning.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            if (isUsage) "Pemakaian" else "Perbaikan",
                                            color = if (isUsage) Accent else Warning,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        actItem.durationText ?: "-",
                                        color = TextPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    "${DateUtils.formatFull(actItem.startTime)} → ${actItem.endTime?.let { DateUtils.formatFull(it) } ?: "-"}",
                                    color = TextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )

                                val detailText = if (isUsage) (actItem.note ?: "Pemakaian operasional") else (actItem.detail ?: actItem.issue ?: "Perbaikan Teknis")
                                Text(
                                    detailText,
                                    color = TextPrimary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConditionItem(label: String, value: String, highlight: Boolean = false, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 2.dp)) {
        Text(label, color = TextMuted, fontSize = 9.sp)
        Text(
            value,
            color = if (highlight) Red else TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
