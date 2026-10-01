package com.evrenhouse.trackscooter.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ActivityLogEntry
import com.evrenhouse.trackscooter.data.MaintenanceRecord
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.LiveTimer
import com.evrenhouse.trackscooter.ui.common.StatusChip
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.TrackScooterTheme
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.DateUtils

/**
 * 1. Blok Status dan Aksi Perbaikan Terpadu di bagian atas layar.
 * Menghilangkan perulangan status 3x dan menyatukan keterangan perbaikan + tombol Edit & Selesai.
 */
@Composable
fun UnitStatusBlock(
    scooter: Scooter,
    onEditMaintenance: () -> Unit,
    onCompleteMaintenance: () -> Unit,
    completing: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val isMaintenance = scooter.status == ScooterStatus.MAINTENANCE
    val am = scooter.activeMaintenance
    val isLuar = am?.location == "luar"
    val locDetail = am?.locationDetail?.takeIf { it.isNotBlank() }
    val issueText = am?.issue?.takeIf { it.isNotBlank() } ?: scooter.maintenanceNote
    val amberDarkText = if (isDark) Color(0xFFFBBF24) else Color(0xFF8A5300)
    val amberBg = if (isDark) Warning.copy(alpha = 0.15f) else Color(0xFFFFF1DC)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isMaintenance) amberBg else Surface2)
            .border(1.dp, if (isMaintenance) amberDarkText.copy(alpha = 0.35f) else Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Status Row & Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusChip(status = scooter.status)
                if (isMaintenance) {
                    val locLabel = if (isLuar) (if (locDetail != null) "Luar · $locDetail" else "Luar Outlet") else "Di Outlet"
                    val desc = if (!issueText.isNullOrBlank()) "$locLabel · $issueText" else locLabel
                    Text(
                        text = desc,
                        color = amberDarkText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (!isMaintenance) {
                LiveTimer(scooter.status, scooter.lastUpdated)
            }
        }

        // Action Buttons Row (Edit & Selesai) jika dalam perbaikan
        if (isMaintenance && am != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Tombol Edit (Outlined)
                OutlinedButton(
                    onClick = onEditMaintenance,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, amberDarkText),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = amberDarkText),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = "Edit data kendala", modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Tombol Selesai (Button filled amber gelap dengan teks putih > 5.2:1 contrast)
                Button(
                    onClick = onCompleteMaintenance,
                    enabled = !completing,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFFD97706) else Color(0xFF8A5300),
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                ) {
                    if (completing) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 4. Form Kondisi Perangkat (Compact single container, < 60% tinggi sebelumnya).
 */
@Composable
fun ConditionEditor(
    condition: Map<String, String>,
    onFieldChange: (String, String) -> Unit,
    monitorDetail: String,
    onMonitorDetailChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dc = condition

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Form Header (tanpa tombol simpan di sini, tombol pindah ke bottom bar sticky)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(Accent.copy(alpha = 0.15f), RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Tune, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
            }
            Text(
                "KONDISI PERANGKAT",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
            )
        }

        Spacer(Modifier.height(4.dp))

        // 1. Spakbor (Ada / Tidak)
        ConditionTwoOptionRow(
            label = "Spakbor",
            options = listOf("ada" to "Ada", "tidak" to "Tidak"),
            selected = dc["setelan"] ?: "ada",
            onSelect = { onFieldChange("setelan", it) },
            badValue = "tidak",
        )

        HorizontalDivider(color = Border.copy(alpha = 0.4f), thickness = 0.5.dp)

        // 2. Lampu (Nyala / Tidak)
        ConditionTwoOptionRow(
            label = "Lampu",
            options = listOf("nyala" to "Nyala", "tidak" to "Tidak"),
            selected = dc["lampu"] ?: "nyala",
            onSelect = { onFieldChange("lampu", it) },
            badValue = "tidak",
        )

        HorizontalDivider(color = Border.copy(alpha = 0.4f), thickness = 0.5.dp)

        // 3. Baterai (Normal / Drop)
        ConditionTwoOptionRow(
            label = "Baterai",
            options = listOf("normal" to "Normal", "drop" to "Drop"),
            selected = dc["baterai"] ?: "normal",
            onSelect = { onFieldChange("baterai", it) },
            badValue = "drop",
        )

        HorizontalDivider(color = Border.copy(alpha = 0.5f), thickness = 0.5.dp)

        // 4. Rem (Normal / Rusak)
        ConditionTwoOptionRow(
            label = "Rem",
            options = listOf("normal" to "Normal", "rusak" to "Rusak"),
            selected = dc["rem"] ?: "normal",
            onSelect = { onFieldChange("rem", it) },
            badValue = "rusak",
        )

        HorizontalDivider(color = Border.copy(alpha = 0.4f), thickness = 0.5.dp)

        // 5. Ban (Aman / Tipis / Botak - Terurut dari baik ke buruk)
        ConditionBanRow(
            selected = dc["ban"] ?: "aman",
            onSelect = { onFieldChange("ban", it) },
        )

        HorizontalDivider(color = Border.copy(alpha = 0.4f), thickness = 0.5.dp)

        // 6. Jenis Error (FlowRow of FilterChip, single choice)
        ConditionErrorTypeSection(
            selected = dc["monitor"] ?: "normal",
            onSelect = { onFieldChange("monitor", it) },
            monitorDetail = monitorDetail,
            onMonitorDetailChange = onMonitorDetailChange,
        )
    }
}

@Composable
private fun ConditionTwoOptionRow(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    badValue: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Surface2)
                .border(1.dp, Border, RoundedCornerShape(8.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEach { (value, optLabel) ->
                val isSelected = value == selected
                val isBad = value == badValue
                val activeBg = if (isSelected) {
                    if (isBad) Red else Accent
                } else Color.Transparent

                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 64.dp, minHeight = 34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(activeBg)
                        .clickable { onSelect(value) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = optLabel,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConditionBanRow(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Terurut dari baik ke buruk: Aman, Tipis, Botak
    val options = listOf("aman" to "Aman", "tipis" to "Tipis", "botak" to "Botak")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Ban",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Surface2)
                .border(1.dp, Border, RoundedCornerShape(8.dp))
                .padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEach { (value, optLabel) ->
                val isSelected = value == selected
                val activeBg = if (isSelected) {
                    when (value) {
                        "botak" -> Red
                        "tipis" -> Warning
                        else -> Accent
                    }
                } else Color.Transparent

                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 52.dp, minHeight = 34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(activeBg)
                        .clickable { onSelect(value) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = optLabel,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextMuted,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConditionErrorTypeSection(
    selected: String,
    onSelect: (String) -> Unit,
    monitorDetail: String,
    onMonitorDetailChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val errorOptions = listOf(
        "normal" to "Normal",
        "e2" to "E2",
        "e4" to "E4",
        "e16" to "E16",
        "e6" to "E6",
        "lain" to "Lain Lain",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Jenis Error",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            errorOptions.forEach { (code, label) ->
                val isSelected = selected == code
                val isProblem = code != "normal"
                val activeBg = if (isSelected) {
                    if (isProblem) (if (isDark) Red.copy(alpha = 0.25f) else Color(0xFFFEE2E2))
                    else (if (isDark) Accent.copy(alpha = 0.25f) else Color(0xFFEFF6FF))
                } else Surface2
                val activeBorder = if (isSelected) {
                    if (isProblem) Red else Accent
                } else Border
                val activeText = if (isSelected) {
                    if (isProblem) Red else Accent
                } else TextMuted

                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeBg)
                        .border(1.dp, activeBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelect(code) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isProblem) Red else Accent, CircleShape),
                        )
                    }
                    Text(
                        text = label,
                        color = activeText,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                }
            }
        }

        // Keterangan Masalah jika opsi 'Lain Lain' dipilih
        if (selected == "lain") {
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

/**
 * 5. Sticky Bottom Bar Simpan & Batal (Hanya muncul saat ada perubahan/dirty).
 */
@Composable
fun SaveBar(
    isDirty: Boolean,
    saving: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = isDirty,
        enter = fadeIn(animationSpec = tween(150)) + slideInVertically { it },
        exit = fadeOut(animationSpec = tween(150)) + slideOutVertically { it },
        modifier = modifier,
    ) {
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 8.dp,
            color = Surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                ) {
                    Text("Batal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onSave,
                    enabled = !saving,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 44.dp),
                ) {
                    if (saving) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        Spacer(Modifier.width(6.dp))
                        Text("Menyimpan...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * 6. Laporan Riwayat (Judul onSurface, Export TextButton Accent, padding simetris, badge Perbaikan kontras).
 */
@Composable
fun HistorySection(
    log: List<ActivityLogEntry>,
    maintenance: List<MaintenanceRecord>,
    onOpenFullHistory: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Header riwayat (padding atas-bawah simetris)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = TextMuted, modifier = Modifier.size(15.dp))
                Text(
                    "Laporan Riwayat",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            TextButton(
                onClick = onExport,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Icon(Icons.Filled.FileDownload, contentDescription = "Unduh laporan riwayat", tint = Accent, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Export", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        if (log.isEmpty() && maintenance.isEmpty()) {
            Text(
                "Belum ada riwayat untuk unit ini.",
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFullHistory() }
                    .padding(vertical = 12.dp),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenFullHistory() },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                log.take(3).forEach { entry ->
                    val isCheckout = entry.action == "checkout"
                    HistoryRow(
                        icon = { Icon(Icons.Filled.Schedule, contentDescription = null, tint = if (isCheckout) Red else Green, modifier = Modifier.size(13.dp)) },
                        title = if (isCheckout) "Keluar (Sewa)" else "Masuk (Kembali)",
                        subtitle = DateUtils.formatFull(entry.timestamp),
                        iconColor = if (isCheckout) Red else Green,
                    )
                }
                maintenance.take(3).forEach { m ->
                    val isDone = m.status == "done"
                    val locLabel = if (m.location == "outlet") "Di Outlet" else if (!m.locationDetail.isNullOrBlank()) "Luar · ${m.locationDetail}" else "Luar Outlet"
                    HistoryRow(
                        icon = { Icon(Icons.Filled.Construction, contentDescription = null, tint = if (isDone) Green else Warning, modifier = Modifier.size(13.dp)) },
                        title = "Perbaikan · ${m.issue ?: "Kendala"}",
                        subtitle = "$locLabel · ${DateUtils.formatFull(m.startedAt)}" +
                            (if (isDone && m.resolvedAt != null) " → Selesai ${DateUtils.formatFull(m.resolvedAt)}" else ""),
                        iconColor = if (isDone) Green else Warning,
                        badge = if (isDone) "Selesai" to Green else "Perbaikan" to Warning,
                    )
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
    val isDark = LocalThemeIsDark.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface2.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
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
                    val badgeTextColor = if (text == "Perbaikan" || color == Warning) {
                        if (isDark) Color(0xFFFBBF24) else Color(0xFF8A5300)
                    } else {
                        color
                    }
                    val badgeBgColor = if (text == "Perbaikan" || color == Warning) {
                        if (isDark) Warning.copy(alpha = 0.18f) else Color(0xFFFFF1DC)
                    } else {
                        color.copy(alpha = 0.12f)
                    }
                    Text(
                        text = text,
                        color = badgeTextColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(badgeBgColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
            Text(subtitle, color = TextMuted, fontSize = 10.sp)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────

@Preview(name = "UnitStatusBlock - Maintenance", showBackground = true)
@Composable
private fun UnitStatusBlockMaintenancePreview() {
    TrackScooterTheme(isDark = false) {
        UnitStatusBlock(
            scooter = Scooter(
                id = "SB-53",
                type = "sb",
                status = ScooterStatus.MAINTENANCE,
                currentOutlet = "utara",
                activeMaintenance = com.evrenhouse.trackscooter.data.ActiveMaintenance(
                    id = "m-1",
                    location = "outlet",
                    issue = "e2",
                    status = "repair",
                    startedAt = "2026-09-25T10:00:00Z",
                ),
            ),
            onEditMaintenance = {},
            onCompleteMaintenance = {},
        )
    }
}

@Preview(name = "UnitStatusBlock - Ready", showBackground = true)
@Composable
private fun UnitStatusBlockReadyPreview() {
    TrackScooterTheme(isDark = false) {
        UnitStatusBlock(
            scooter = Scooter(
                id = "SB-01",
                type = "sb",
                status = ScooterStatus.AVAILABLE,
                currentOutlet = "utara",
            ),
            onEditMaintenance = {},
            onCompleteMaintenance = {},
        )
    }
}
