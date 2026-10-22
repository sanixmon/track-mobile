package com.evrenhouse.trackscooter.ui.scan

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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.BottomSheetCornerRadius
import com.evrenhouse.trackscooter.ui.theme.ButtonCornerRadius
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors

/**
 * Slide-to-Confirm / Tap-to-Confirm Bottom Sheet for QR Scan Result.
 * High legibility, thumb-friendly 54dp button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanConfirmBottomSheet(
    scooter: Scooter,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    submitting: Boolean = false,
    breakText: String? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val isDark = LocalThemeIsDark.current
    val isAvailable = scooter.status == ScooterStatus.AVAILABLE
    val isInUse = scooter.status == ScooterStatus.IN_USE
    val isMaintenance = scooter.status == ScooterStatus.MAINTENANCE

    val typeStyle = ScooterColors.STYLES[scooter.type.lowercase()]
    val outletLabel = Outlets.labelOf(scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type))

    val actionTitle = when {
        isAvailable -> "Konfirmasi Sewa Unit"
        isInUse -> "Konfirmasi Pengembalian"
        else -> "Unit Dalam Maintenance"
    }

    val actionColor = when {
        isAvailable -> Accent
        isInUse -> Green
        else -> Warning
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
        shape = RoundedCornerShape(topStart = BottomSheetCornerRadius, topEnd = BottomSheetCornerRadius),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Border.copy(alpha = 0.8f)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            // Header: Action type & Unit ID
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(actionColor.copy(alpha = if (isDark) 0.25f else 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = when {
                                isAvailable -> Icons.Default.ArrowUpward
                                isInUse -> Icons.Default.ArrowDownward
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = actionColor,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = actionTitle,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = actionColor,
                        )
                        Text(
                            text = scooter.id,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp,
                        )
                    }
                }

                // Type Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            typeStyle?.accent?.copy(alpha = if (isDark) 0.25f else 0.12f)
                                ?: (if (isDark) Surface2 else Color(0xFFF1F5F9))
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = scooter.type.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = typeStyle?.accent ?: TextPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Unit Details Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isDark) Surface2 else Color(0xFFF8FAFC))
                    .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Lokasi / Outlet", fontSize = 12.sp, color = TextMuted)
                    Text(outletLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Status Saat Ini", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = when {
                            isAvailable -> "Ready di Outlet"
                            isInUse -> "Sedang Berjalan di Luar"
                            else -> "Maintenance (${scooter.activeMaintenance?.location ?: "Outlet"})"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = actionColor,
                    )
                }

                if (!breakText.isNullOrBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Jeda Istirahat", fontSize = 12.sp, color = Warning)
                        Text(breakText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Warning)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (!isMaintenance) {
                    Button(
                        onClick = onConfirm,
                        enabled = !submitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(ButtonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = actionColor),
                    ) {
                        if (submitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAvailable) "Konfirmasi Mulai Sewa" else "Konfirmasi Pengembalian",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(ButtonCornerRadius),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Border)
                    ),
                ) {
                    Text("Batal", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                }
            }
        }
    }
}
