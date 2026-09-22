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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.Outlet
import com.evrenhouse.trackscooter.util.Outlets

fun getOutletIcon(id: String): ImageVector = when (id) {
    "all" -> Icons.Filled.Store
    "utara-motor" -> Icons.Filled.ElectricScooter
    else -> Icons.Filled.LocationOn
}

@Composable
fun OutletDropdown(
    selectedOutletId: String,
    onOutletSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    outlets: List<Outlet> = Outlets.ALL_OUTLETS,
    getOutletCount: ((String) -> Int)? = null,
    labelPrefix: String = "Outlet:"
) {
    var expanded by remember { mutableStateOf(false) }
    val currentOutlet = remember(selectedOutletId, outlets) {
        outlets.find { it.id == selectedOutletId } ?: outlets.firstOrNull() ?: Outlets.ALL_OUTLETS[0]
    }
    val currentCount = getOutletCount?.invoke(currentOutlet.id)

    Box(modifier = modifier) {
        // Trigger Button
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Surface)
                .border(1.dp, if (expanded) Accent else Border, RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = getOutletIcon(currentOutlet.id),
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(15.dp)
            )

            if (labelPrefix.isNotBlank()) {
                Text(
                    text = labelPrefix,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Text(
                text = currentOutlet.label,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            if (currentCount != null) {
                Box(
                    modifier = Modifier
                        .background(Surface3, RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$currentCount",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        // Dropdown Menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Surface, RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .width(220.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = "PILIH LOKASI OUTLET",
                        color = TextSubtle,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                outlets.forEach { outlet ->
                    val isSelected = outlet.id == selectedOutletId
                    val count = getOutletCount?.invoke(outlet.id)

                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = getOutletIcon(outlet.id),
                                        contentDescription = null,
                                        tint = if (isSelected) Accent else TextMuted,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = outlet.label,
                                        color = if (isSelected) Accent else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (count != null) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (isSelected) Accent else Surface3,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "$count",
                                                color = if (isSelected) androidx.compose.ui.graphics.Color.White else TextMuted,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = Accent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        },
                        onClick = {
                            expanded = false
                            onOutletSelected(outlet.id)
                        }
                    )
                }
            }
        }
    }
}
