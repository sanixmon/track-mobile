package com.evrenhouse.trackscooter.ui.manage

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterType
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
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.TypeLabels

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScooterDialog(
    scooters: List<Scooter>,
    onDismiss: () -> Unit,
    onSubmit: (id: String?, type: String, outlet: String, addAnother: Boolean) -> Unit,
    submitting: Boolean = false,
    initialType: String = ScooterType.SD,
    initialOutlet: String = "utara"
) {
    var type by remember { mutableStateOf(initialType) }
    var outlet by remember { mutableStateOf(initialOutlet) }
    var numberInput by remember { mutableStateOf("") }
    var typeDropdownOpen by remember { mutableStateOf(false) }
    var outletDropdownOpen by remember { mutableStateOf(false) }

    val prefix = remember(type) { "${type.uppercase()}-" }

    // Dynamic next-number calculation
    val nextNumber by remember(scooters, type) {
        derivedStateOf {
            val sameTypeNumbers = scooters
                .filter { it.type.equals(type, ignoreCase = true) }
                .mapNotNull {
                    val raw = it.id.replace(prefix, "", ignoreCase = true)
                    raw.toIntOrNull()?.takeIf { n -> n > 0 }
                }
            if (sameTypeNumbers.isNotEmpty()) sameTypeNumbers.max() + 1 else 1
        }
    }

    // Nomor 0/tidak valid (paritas temuan web): server akan membuat SD-0,
    // jadi tolak di client seperti duplikat.
    val isInvalidNumber by remember(numberInput) {
        derivedStateOf {
            numberInput.isNotBlank() && (numberInput.trim().toIntOrNull() ?: -1) <= 0
        }
    }

    // Instant real-time duplicate check
    val isDuplicate by remember(numberInput, scooters, type) {
        derivedStateOf {
            if (numberInput.isBlank()) false
            else {
                val checkId = "$prefix${numberInput.trim().toIntOrNull() ?: numberInput.trim()}"
                scooters.any { it.id.equals(checkId, ignoreCase = true) }
            }
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
                            .background(Accent.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ElectricScooter,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Tambah Unit Baru",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daftarkan armada scooter ke outlet",
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

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 1. Jenis Scooter Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Jenis Scooter",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface2)
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                                .clickable { typeDropdownOpen = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = TypeLabels.of(type),
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextMuted)
                        }

                        DropdownMenu(
                            expanded = typeDropdownOpen,
                            onDismissRequest = { typeDropdownOpen = false },
                            modifier = Modifier
                                .background(Surface, RoundedCornerShape(12.dp))
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                        ) {
                            TypeLabels.ALL.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = label,
                                            color = if (type == key) Accent else TextPrimary,
                                            fontWeight = if (type == key) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        type = key
                                        outlet = Outlets.getHomeOutletForType(key)
                                        typeDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Nomor Scooter (Opsional)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nomor Scooter (Opsional)",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (numberInput.isNotBlank() && !isDuplicate) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Filled.CheckCircle, null, tint = Green, modifier = Modifier.size(13.dp))
                                Text("Tersedia", color = Green, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Surface2)
                            .border(1.dp, if (isDuplicate) Red else Border, RoundedCornerShape(12.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Surface3)
                                .padding(horizontal = 12.dp, vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prefix,
                                color = Accent,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedTextField(
                            value = numberInput,
                            onValueChange = { numberInput = it.filter { ch -> ch.isDigit() } },
                            placeholder = {
                                Text("$nextNumber (Otomatis)", color = TextSubtle, fontSize = 12.sp)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (isDuplicate) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = Red, modifier = Modifier.size(12.dp))
                            Text(
                                text = "ID $prefix$numberInput sudah terdaftar di sistem",
                                color = Red,
                                fontSize = 11.sp
                            )
                        }
                    } else if (isInvalidNumber) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = Red, modifier = Modifier.size(12.dp))
                            Text(
                                text = "Nomor unit harus lebih dari 0",
                                color = Red,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        Text(
                            text = "Kosongkan untuk nomor berikutnya ($prefix$nextNumber)",
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    }
                }

                // 3. Lokasi Outlet Penugasan
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Lokasi Outlet Penugasan",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Surface2)
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                                .clickable { outletDropdownOpen = true }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Store, null, tint = Accent, modifier = Modifier.size(15.dp))
                                Text(
                                    text = Outlets.labelOf(outlet),
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextMuted)
                        }

                        DropdownMenu(
                            expanded = outletDropdownOpen,
                            onDismissRequest = { outletDropdownOpen = false },
                            modifier = Modifier
                                .background(Surface, RoundedCornerShape(12.dp))
                                .border(1.dp, Border, RoundedCornerShape(12.dp))
                        ) {
                            Outlets.OPERATIONAL.forEach { o ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = o.label,
                                            color = if (outlet == o.id) Accent else TextPrimary,
                                            fontWeight = if (outlet == o.id) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        outlet = o.id
                                        outletDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                    OutlinedButton(
                        onClick = {
                            val finalId = if (numberInput.isNotBlank()) "$prefix${numberInput.trim()}" else "$prefix$nextNumber"
                            onSubmit(finalId, type, outlet, true)
                            numberInput = ""
                        },
                        enabled = !submitting && !isDuplicate && !isInvalidNumber,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.4f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Accent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Accent)
                    ) {
                        Text("Simpan & Lagi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        val finalId = if (numberInput.isNotBlank()) "$prefix${numberInput.trim()}" else "$prefix$nextNumber"
                        onSubmit(finalId, type, outlet, false)
                    },
                    enabled = !submitting && !isDuplicate && !isInvalidNumber,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent)
                ) {
                    if (submitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Menyimpan...", fontSize = 12.sp)
                    } else {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Simpan Unit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
