package com.evrenhouse.trackscooter.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.ScooterStatus
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
import com.evrenhouse.trackscooter.util.StatusLabels
import kotlinx.coroutines.delay

// ── Status colors ──────────────────────────────────────────
data class StatusColor(val color: Color, val subtle: Color)

@Composable
fun statusColor(status: String?): StatusColor = when (status) {
    ScooterStatus.AVAILABLE -> StatusColor(Green, Green.copy(alpha = 0.12f))
    ScooterStatus.IN_USE -> StatusColor(Accent, Accent.copy(alpha = 0.12f))
    ScooterStatus.RUSAK -> StatusColor(Red, Red.copy(alpha = 0.12f))
    ScooterStatus.MAINTENANCE -> StatusColor(Warning, Warning.copy(alpha = 0.12f))
    else -> StatusColor(TextMuted, Surface3)
}

@Composable
fun StatusChip(status: String?, modifier: Modifier = Modifier, pulse: Boolean = false) {
    val c = statusColor(status)
    Row(
        modifier = modifier
            .background(c.subtle, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(c.color, CircleShape),
        )
        Text(
            text = StatusLabels.of(status),
            color = c.color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp,
        )
    }
}

@Composable
fun TypeBadge(type: String?, modifier: Modifier = Modifier) {
    val isSd = type == "sd"
    Row(
        modifier = modifier
            .background(if (isSd) Accent.copy(alpha = 0.15f) else Surface3, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = type?.uppercase() ?: "-",
            color = if (isSd) Accent else TextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}

// ── Section card ───────────────────────────────────────────
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp)),
    ) {
        if (header != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2.copy(alpha = 0.4f), RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                header()
            }
        }
        content()
    }
}

@Composable
fun SectionTitle(text: String, subtitle: String? = null) {
    Column {
        Text(
            text = text,
            color = TextSubtle,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
        )
        if (subtitle != null) {
            Spacer(Modifier.height(2.dp))
            Text(text = subtitle, color = TextMuted, fontSize = 11.sp)
        }
    }
}

// ── Live timer (mirrors web LiveTimer) ─────────────────────
@Composable
fun LiveTimer(status: String?, lastUpdated: String?) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(status, lastUpdated) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    if (status == ScooterStatus.IN_USE) {
        val ts = DateUtils.parse(lastUpdated)?.let {
            java.time.Duration.between(it, java.time.LocalDateTime.now(DateUtils.WIB))
        }
        val totalSecs = ts?.seconds?.coerceAtLeast(0) ?: 0
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        val formatted = "%02d:%02d:%02d".format(hrs, mins, secs)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Red, CircleShape),
            )
            Text(
                text = "Durasi: $formatted",
                color = Red,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            )
        }
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.AccessTime, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(12.dp))
            Text(
                text = "Update: ${DateUtils.timeAgo(lastUpdated)}",
                color = TextSubtle,
                fontSize = 11.sp,
            )
        }
    }
}

// ── State panels ───────────────────────────────────────────
@Composable
fun LoadingState(text: String = "Memuat data...") {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CircularProgressIndicator(color = Accent, strokeWidth = 3.dp)
        Text(text, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(16.dp))
            .border(1.dp, Red.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Filled.CloudOff, contentDescription = null, tint = Red, modifier = Modifier.size(28.dp))
        Text("Koneksi Basis Data Gagal", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text(message, color = TextMuted, fontSize = 12.sp)
        OutlinedAction(text = "Coba Hubungkan Kembali", onClick = onRetry, color = Accent)
    }
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = TextMuted, fontSize = 12.sp)
    }
}

// ── Buttons ────────────────────────────────────────────────
@Composable
fun OutlinedAction(text: String, onClick: () -> Unit, color: Color = Accent, enabled: Boolean = true) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = color,
        ),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun FilledAction(
    text: String,
    onClick: () -> Unit,
    color: Color = Accent,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            icon?.invoke()
            Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Stat card ──────────────────────────────────────────────
@Composable
fun StatCard(
    label: String,
    sub: String,
    value: Int,
    icon: @Composable (Color) -> Unit,
    valueColor: Color,
    iconBg: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Surface, RoundedCornerShape(14.dp))
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                icon(iconColor)
            }
            Text(
                text = value.toString(),
                color = valueColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = sub,
            color = TextMuted,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Compact Dropdown Pill (replaces bulky 56dp OutlinedTextField) ──
@Composable
fun CompactDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .height(34.dp)
                .background(Surface2, RoundedCornerShape(8.dp))
                .border(1.dp, Border, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            leadingIcon?.invoke()
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Surface, RoundedCornerShape(10.dp))
                .border(1.dp, Border, RoundedCornerShape(10.dp)),
        ) {
            options.forEach { (value, text) ->
                val isSelected = value == selected
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Accent else TextPrimary,
                        )
                    },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}

// Keep SimpleDropdown alias pointing to CompactDropdown for smooth migration
@Composable
fun SimpleDropdown(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    CompactDropdown(
        label = label,
        options = options,
        selected = selected,
        onSelect = onSelect,
        modifier = modifier,
    )
}

// ── Segmented Pill Group for Fast Single-Touch Selection ──
@Composable
fun SegmentedPillGroup(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = Accent,
) {
    Row(
        modifier = modifier
            .background(Surface2, RoundedCornerShape(8.dp))
            .border(1.dp, Border, RoundedCornerShape(8.dp))
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (value, label) ->
            val isSelected = value == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) activeColor else Color.Transparent)
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

