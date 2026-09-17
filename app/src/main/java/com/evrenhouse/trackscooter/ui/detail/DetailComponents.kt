package com.evrenhouse.trackscooter.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.ui.common.SimpleDropdown
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.ActionLabels
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.DeviceConditionHelper
import com.evrenhouse.trackscooter.util.DeviceField
import com.evrenhouse.trackscooter.util.DeviceFields
import com.evrenhouse.trackscooter.util.FieldTone

@Composable
fun ConditionEditor(
    condition: Map<String, String>,
    onFieldChange: (String, String) -> Unit,
    monitorDetail: String,
    onMonitorDetailChange: (String) -> Unit,
    isDirty: Boolean,
    hasCondition: Boolean,
    saving: Boolean,
    onSave: () -> Unit,
) {
    val dc = condition
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("KONDISI PERANGKAT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
                if (isDirty && !saving) {
                    Text(
                        "Belum disimpan",
                        color = Warning,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(Warning.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    )
                }
            }
            Button(
                onClick = onSave,
                enabled = !saving && isDirty,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(modifier = Modifier.size(11.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(11.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(if (saving) "Menyimpan..." else "Simpan", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // 2-column grid
        DeviceFields.ALL.chunked(2).forEach { rowFields ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowFields.forEach { field ->
                    FieldEditor(
                        field = field,
                        value = dc[field.key] ?: "",
                        hasCondition = hasCondition,
                        onChange = { onFieldChange(field.key, it) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowFields.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        // Monitor detail text field when monitor is 'lain'
        if (dc["monitor"] == "lain") {
            OutlinedTextField(
                value = monitorDetail,
                onValueChange = onMonitorDetailChange,
                label = { Text("Keterangan Masalah Monitor (Wajib)", fontSize = 11.sp) },
                placeholder = { Text("Contoh: Layar bergaris, redup, pecah...", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun FieldEditor(
    field: DeviceField,
    value: String,
    hasCondition: Boolean,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tone = DeviceConditionHelper.fieldTone(field.key, value, hasCondition)
    val toneColor = when (tone) {
        FieldTone.GOOD -> Green
        FieldTone.BAD -> Red
        FieldTone.WARN -> Warning
        FieldTone.NONE -> TextSubtle
    }

    Column(
        modifier = modifier
            .background(Surface2.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(field.label, color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        SimpleDropdown(
            label = field.options.firstOrNull { it.first == value }?.second ?: "Belum dicek",
            options = if (value.isEmpty()) {
                listOf("" to "Belum dicek") + field.options
            } else {
                field.options
            },
            selected = value,
            onSelect = onChange,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun HistorySection(
    log: List<ActivityLogEntry>,
    maintenance: List<MaintenanceRecord>,
    onExport: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("RIWAYAT UNIT (${log.size})", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            TextButton(onClick = onExport, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Filled.FileDownload, contentDescription = null, tint = Green, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Export", color = Green, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (log.isEmpty() && maintenance.isEmpty()) {
            Text(
                "Belum ada riwayat untuk unit ini.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(20.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 58.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    log.forEach { entry ->
                        val isCheckout = entry.action == ActionLabels.CHECKOUT
                        HistoryRow(
                            icon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = if (isCheckout) Red else Green, modifier = Modifier.size(13.dp)) },
                            title = if (isCheckout) "Keluar (Sewa)" else "Masuk (Kembali)",
                            subtitle = DateUtils.formatFull(entry.timestamp),
                            iconColor = if (isCheckout) Red else Green,
                        )
                    }
                    maintenance.forEach { m ->
                        val isDone = m.status == "done"
                        HistoryRow(
                            icon = { Icon(Icons.Filled.Construction, contentDescription = null, tint = if (isDone) Green else Warning, modifier = Modifier.size(13.dp)) },
                            title = "Maintenance · ${m.issue ?: "Perbaikan"}",
                            subtitle = "${if (m.location == "outlet") "Di Outlet" else "Keluar / Di Luar"} · ${DateUtils.formatFull(m.startedAt)}" +
                                (if (isDone && m.resolvedAt != null) " → Selesai ${DateUtils.formatFull(m.resolvedAt)}" else ""),
                            iconColor = if (isDone) Green else Warning,
                            badge = if (isDone) "Selesai" to Green else "Repair" to Warning,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    iconColor: Color,
    badge: Pair<String, Color>? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .border(1.dp, Border, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(iconColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            icon()
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                badge?.let { (text, color) ->
                    Text(
                        text,
                        color = color,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(color.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
            Text(subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}
