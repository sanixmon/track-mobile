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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.TypeBadge
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
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.StatusLabels
import com.evrenhouse.trackscooter.util.TypeLabels
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanConfirmDialog(
    scooter: Scooter,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    submitting: Boolean = false,
    breakText: String? = null
) {
    val isAvailable = scooter.status == ScooterStatus.AVAILABLE
    val isInUse = scooter.status == ScooterStatus.IN_USE
    val isMaintenance = scooter.status == ScooterStatus.MAINTENANCE || scooter.status == ScooterStatus.RUSAK

    val outletLabel = remember(scooter.currentOutlet, scooter.type) {
        val outletId = scooter.currentOutlet ?: Outlets.getHomeOutletForType(scooter.type)
        Outlets.labelOf(outletId)
    }

    val elapsedDuration = remember(scooter.lastUpdated, scooter.status) {
        if (scooter.status == ScooterStatus.IN_USE && scooter.lastUpdated != null) {
            val dt = DateUtils.parse(scooter.lastUpdated)
            if (dt != null) {
                val millis = DateUtils.toEpochMilli(dt)
                val diffSecs = (System.currentTimeMillis() - millis) / 1000
                DateUtils.formatDuration(diffSecs)
            } else null
        } else null
    }

    val currentTimeStr = remember {
        SimpleDateFormat("HH:mm", Locale("id", "ID")).format(Date())
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                            .size(38.dp)
                            .background(
                                when {
                                    isAvailable -> Accent.copy(alpha = 0.15f)
                                    isInUse -> Green.copy(alpha = 0.15f)
                                    else -> Warning.copy(alpha = 0.15f)
                                },
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isAvailable -> Icons.Filled.ArrowUpward
                                isInUse -> Icons.Filled.ArrowDownward
                                else -> Icons.Filled.WarningAmber
                            },
                            contentDescription = null,
                            tint = when {
                                isAvailable -> Accent
                                isInUse -> Green
                                else -> Warning
                            },
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = when {
                                isAvailable -> "Konfirmasi Sewa"
                                isInUse -> "Konfirmasi Selesai"
                                else -> "Unit Dalam Perbaikan"
                            },
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when {
                                isAvailable -> "Unit akan keluar untuk disewa"
                                isInUse -> "Unit telah selesai dan kembali"
                                else -> "Status unit saat ini kendala/maintenance"
                            },
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    enabled = !submitting,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Tutup",
                        tint = TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Scooter Identity Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Surface2, RoundedCornerShape(14.dp))
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = scooter.id,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        TypeBadge(type = scooter.type)
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                when {
                                    isAvailable -> Green.copy(alpha = 0.15f)
                                    isInUse -> Accent.copy(alpha = 0.15f)
                                    else -> Warning.copy(alpha = 0.15f)
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = StatusLabels.of(scooter.status),
                            color = when {
                                isAvailable -> Green
                                isInUse -> Accent
                                else -> Warning
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Store,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = outletLabel,
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "$currentTimeStr WIB",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Contextual information
            if (isAvailable && !breakText.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Green.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .border(1.dp, Green.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = Green,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Jeda istirahat:", color = TextMuted, fontSize = 12.sp)
                    }
                    Text(
                        text = breakText,
                        color = Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (isInUse && elapsedDuration != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Surface3, RoundedCornerShape(10.dp))
                        .border(1.dp, Border, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Durasi sewa:", color = TextMuted, fontSize = 12.sp)
                    }
                    Text(
                        text = elapsedDuration ?: "-",
                        color = Accent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (isMaintenance) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Warning.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .border(1.dp, Warning.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Catatan Kendala:",
                        color = Warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = scooter.maintenanceNote ?: "Dalam proses perbaikan teknis.",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Apakah Anda yakin ingin tetap menyewakan unit ini?",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            // Buttons
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
                    Text("Batal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onConfirm,
                    enabled = !submitting,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isAvailable -> Accent
                            isInUse -> Green
                            else -> Warning
                        }
                    )
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Proses...", fontSize = 12.sp)
                    } else {
                        Text(
                            text = when {
                                isAvailable -> "Sewa Unit"
                                isInUse -> "Kembalikan"
                                else -> "Tetap Sewa"
                            },
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
