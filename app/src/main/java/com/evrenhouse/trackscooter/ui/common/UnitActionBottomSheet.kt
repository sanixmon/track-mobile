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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.evrenhouse.trackscooter.ui.theme.bounceClickable
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors

/**
 * Thumb-friendly Unit Action Bottom Sheet.
 * Contextual action stack with primary action at the very bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitActionBottomSheet(
    scooter: Scooter,
    onDismissRequest: () -> Unit,
    onDetailClick: (String) -> Unit,
    onStartRentClick: (String) -> Unit = {},
    onReturnClick: (String) -> Unit = {},
    onSwapClick: (String) -> Unit = {},
    onResolveMaintenanceClick: (String) -> Unit = {},
    onMoveToMaintenanceClick: (String) -> Unit = {},
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val isDark = LocalThemeIsDark.current
    val typeStyle = ScooterColors.STYLES[scooter.type.lowercase()]
    val outletLabel = Outlets.labelOf(scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type))

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
            // Unit Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(
                                typeStyle?.accent?.copy(alpha = if (isDark) 0.25f else 0.15f)
                                    ?: Accent.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ElectricScooter,
                            contentDescription = null,
                            tint = typeStyle?.accent ?: Accent,
                            modifier = Modifier.size(26.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = scooter.id,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                        )
                        Text(
                            text = "${scooter.type.uppercase()} • $outletLabel",
                            fontSize = 12.sp,
                            color = TextMuted,
                        )
                    }
                }

                // Status Badge
                val (statusText, statusColor) = when (scooter.status) {
                    ScooterStatus.AVAILABLE -> "Ready" to Green
                    ScooterStatus.IN_USE -> "Sedang Disewa" to Accent
                    ScooterStatus.MAINTENANCE -> "Maintenance" to Warning
                    else -> scooter.status to TextMuted
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusColor.copy(alpha = if (isDark) 0.2f else 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons Contextual Stack (Primary thumb-friendly at the bottom)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (scooter.status) {
                    ScooterStatus.AVAILABLE -> {
                        // Option: Move to maintenance
                        OutlinedButton(
                            onClick = {
                                onDismissRequest()
                                onMoveToMaintenanceClick(scooter.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(ButtonCornerRadius),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Warning,
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Warning.copy(alpha = 0.5f))
                            ),
                        ) {
                            Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Laporkan Maintenance", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        // Primary Action: Start Rent / Scan
                        Button(
                            onClick = {
                                onDismissRequest()
                                onStartRentClick(scooter.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(ButtonCornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mulai Sewa Unit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    ScooterStatus.IN_USE -> {
                        // Option: Swap unit
                        OutlinedButton(
                            onClick = {
                                onDismissRequest()
                                onSwapClick(scooter.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(ButtonCornerRadius),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Accent,
                            ),
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tukar Unit (Trouble)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }

                        // Primary Action: Return Unit
                        Button(
                            onClick = {
                                onDismissRequest()
                                onReturnClick(scooter.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(ButtonCornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                        ) {
                            Icon(Icons.Default.KeyboardReturn, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kembalikan Unit", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    ScooterStatus.MAINTENANCE -> {
                        // Primary Action: Resolve Maintenance
                        Button(
                            onClick = {
                                onDismissRequest()
                                onResolveMaintenanceClick(scooter.id)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(ButtonCornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Selesaikan Perbaikan", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                // Secondary: View full detail & history
                OutlinedButton(
                    onClick = {
                        onDismissRequest()
                        onDetailClick(scooter.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(ButtonCornerRadius),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextMuted,
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Border)
                    ),
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Detail Kondisi & Riwayat", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
        }
    }
}
