package com.evrenhouse.trackscooter.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.CardCornerRadius
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.ui.theme.bounceClickable
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors
import com.evrenhouse.trackscooter.util.StatusLabels

/**
 * Universal Unit Card.
 * High legibility, minimal text, iconic status badges, 24dp rounded corners.
 */
@Composable
fun UnitCard(
    scooter: Scooter,
    onClick: (Scooter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val typeStyle = ScooterColors.STYLES[scooter.type.lowercase()]
    val outletLabel = Outlets.shortLabelOf(scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type))

    val (statusLabel, statusColor, statusBg) = when (scooter.status) {
        ScooterStatus.AVAILABLE -> Triple(
            "Ready",
            Green,
            if (isDark) Green.copy(alpha = 0.15f) else Color(0xFFDCFCE7),
        )
        ScooterStatus.IN_USE -> Triple(
            "Sewa",
            Accent,
            if (isDark) Accent.copy(alpha = 0.18f) else Color(0xFFDBEAFE),
        )
        ScooterStatus.MAINTENANCE -> Triple(
            if (scooter.activeMaintenance?.location == "luar") "Maint Luar" else "Maint Outlet",
            if (scooter.activeMaintenance?.location == "luar") Warning else Red,
            if (isDark) {
                if (scooter.activeMaintenance?.location == "luar") Warning.copy(alpha = 0.18f) else Red.copy(alpha = 0.18f)
            } else {
                if (scooter.activeMaintenance?.location == "luar") Color(0xFFFEF3C7) else Color(0xFFFEE2E2)
            },
        )
        else -> Triple(
            scooter.status,
            TextMuted,
            if (isDark) Surface2 else Color(0xFFF1F5F9),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 4.dp,
                shape = RoundedCornerShape(CardCornerRadius),
                spotColor = Color(0x14000000),
                ambientColor = Color(0x0A000000),
            )
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(CardCornerRadius))
            .bounceClickable { onClick(scooter) }
            .padding(16.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                // Left: ID & Type Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                typeStyle?.accent?.copy(alpha = if (isDark) 0.2f else 0.12f)
                                    ?: (if (isDark) Surface2 else Color(0xFFF1F5F9))
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricScooter,
                            contentDescription = null,
                            tint = typeStyle?.accent ?: Accent,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = scooter.id,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = scooter.type.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = typeStyle?.accent ?: TextMuted,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "•",
                                fontSize = 10.sp,
                                color = TextMuted,
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(11.dp),
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = outletLabel,
                                    fontSize = 11.sp,
                                    color = TextMuted,
                                )
                            }
                        }
                    }
                }

                // Right: Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusBg)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                        )
                    }
                }
            }

            // Note or Warning Indicator (if maintenance active)
            if (scooter.status == ScooterStatus.MAINTENANCE && !scooter.activeMaintenance?.issue.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDark) Surface2 else Color(0xFFF8FAFC))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (scooter.activeMaintenance?.location == "luar") Warning else Red,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = scooter.activeMaintenance?.issue ?: "Dalam perbaikan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
