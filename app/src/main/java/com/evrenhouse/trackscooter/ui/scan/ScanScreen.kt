package com.evrenhouse.trackscooter.ui.scan

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.OutlinedAction
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.common.repository
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
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.util.ScooterColors
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.StatusLabels
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanScreen(
    dataViewModel: ScooterDataViewModel = viewModel(factory = AppViewModelFactory(repository())),
    viewModel: ScanViewModel = viewModel(factory = AppViewModelFactory(repository()))
) {
    val state by viewModel.state.collectAsState()
    val dataState by dataViewModel.state.collectAsState()
    val globalOutlet by dataViewModel.selectedOutlet.collectAsState()

    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current

    var mode by rememberSaveable { mutableStateOf<String?>(null) } // null | "camera" | "image"
    var manualValue by rememberSaveable { mutableStateOf("") }
    var showManual by rememberSaveable { mutableStateOf(false) }
    var decodingImage by remember { mutableStateOf(false) }

    // Filter scooters by the global unified outlet selection
    val outletFilteredScooters by remember(dataState.scooters, globalOutlet) {
        derivedStateOf {
            val list = if (globalOutlet == "all") dataState.scooters
            else dataState.scooters.filter {
                (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == globalOutlet
            }
            list.sortedWith(compareBy<Scooter> { it.id.filter { ch -> !ch.isDigit() } }.thenBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 })
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodingImage = true
            decodeImageUri(context, uri) { id ->
                decodingImage = false
                id?.let { viewModel.onScanned(it) }
                    ?: sweetAlert.showError("QR tidak ditemukan pada gambar.")
            }
        }
    }

    // Alert events
    LaunchedEffect(state.toast) {
        state.toast?.let {
            if (it.contains("berhasil", ignoreCase = true) || it.contains("check", ignoreCase = true) || it.contains("sukses", ignoreCase = true)) {
                sweetAlert.showSuccess(it)
            } else if (it.contains("gagal", ignoreCase = true) || it.contains("tidak", ignoreCase = true) || it.contains("salah", ignoreCase = true)) {
                sweetAlert.showError(it)
            } else {
                sweetAlert.showInfo(it)
            }
            viewModel.consumeToast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Header & Unified Global Outlet Picker
        // Header
        Column {
            Text("Scan QR Scooter", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Pindai QR code untuk toggle status sewa", color = TextMuted, fontSize = 12.sp)
        }

        // Mode picker
        if (mode == null) {
            ModeButton(
                icon = { tint -> Icon(Icons.Filled.CameraAlt, null, Modifier.size(17.dp), tint = tint) },
                label = "Gunakan Kamera",
                sub = "Arahkan kamera ke QR code",
                onClick = { mode = "camera" },
            )
            ModeButton(
                icon = { tint -> Icon(Icons.Filled.Image, null, Modifier.size(17.dp), tint = tint) },
                label = "Upload dari Galeri",
                sub = "Pilih gambar QR dari perangkat",
                onClick = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            )
        }

        // Camera scanner
        if (mode == "camera") {
            CameraScanner(
                isProcessing = state.busy || !state.scanning,
                onScan = { viewModel.onScanned(it) },
                onError = { sweetAlert.showError(it) },
            )
            OutlinedAction(text = "Ganti metode scan", onClick = { mode = null }, color = TextMuted)
        }

        if (decodingImage) {
            Text("Mendekode QR dari gambar...", color = TextMuted, fontSize = 12.sp)
        }

        // ── Pilih Cepat Unit (Dipengaruhi oleh Outlet Picker Global) ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.TouchApp, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                    Text("PILIH CEPAT UNIT", color = TextSubtle, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }

                Text(
                    text = if (globalOutlet == "all") "Semua Outlet (${outletFilteredScooters.size} unit)"
                    else "${Outlets.labelOf(globalOutlet)} (${outletFilteredScooters.size} unit)",
                    color = Accent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search / Filter Input
            OutlinedTextField(
                value = manualValue,
                onValueChange = { manualValue = it.uppercase() },
                placeholder = { Text("Cari atau ketik nomor ID...", color = TextSubtle, fontSize = 12.sp) },
                trailingIcon = {
                    if (manualValue.isNotEmpty()) {
                        IconButton(onClick = { manualValue = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                )
            )

            // Matching scooters chips
            // Matching scooters chips: when typing manual search, search all scooters across outlets
            val matchingScooters = remember(dataState.scooters, outletFilteredScooters, manualValue) {
                if (manualValue.isBlank()) {
                    outletFilteredScooters
                } else {
                    dataState.scooters
                        .filter { it.id.contains(manualValue.trim(), ignoreCase = true) }
                        .sortedWith(compareBy<Scooter> { it.id.filter { ch -> !ch.isDigit() } }.thenBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 })
                }
            }

            if (outletFilteredScooters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (globalOutlet == "all") "Tidak ada armada scooter."
                        else "Tidak ada armada scooter terdaftar di ${Outlets.labelOf(globalOutlet)}.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            } else if (matchingScooters.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Ketuk unit untuk proses langsung:", color = TextSubtle, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        matchingScooters.forEach { s ->
                            val isSelected = manualValue.equals(s.id, ignoreCase = true)
                            val statusColor = when (s.status) {
                                ScooterStatus.AVAILABLE -> Green
                                ScooterStatus.IN_USE -> Accent
                                ScooterStatus.MAINTENANCE -> Warning
                                else -> TextMuted
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Accent.copy(alpha = 0.2f) else Surface2)
                                    .border(1.dp, if (isSelected) Accent else Border, RoundedCornerShape(8.dp))
                                    .clickable(enabled = state.scanning && !state.busy) {
                                        viewModel.onScanned(s.id)
                                    }
                                    .padding(horizontal = 9.dp, vertical = 7.dp)
                            ) {
                                val isDark = LocalThemeIsDark.current
                                val nameColor = if (isSelected) Accent else ScooterColors.getScooterNameColor(s.type, s.id, s.currentOutlet, isDark)
                                Text(
                                    text = s.id,
                                    color = nameColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(statusColor, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            // Submit button if manual input typed
            if (manualValue.isNotBlank()) {
                Button(
                    onClick = {
                        viewModel.onScanned(manualValue.trim())
                        manualValue = ""
                    },
                    enabled = state.scanning && !state.busy,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text("Proses Unit $manualValue", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    // Universal Scan Confirmation Modal
    state.pendingScooter?.let { scooter ->
        ScanConfirmDialog(
            scooter = scooter,
            breakText = state.pendingBreakText,
            submitting = state.busy,
            onConfirm = { viewModel.confirmScan() },
            onDismiss = { viewModel.dismissConfirmation() }
        )
    }
}

@Composable
private fun ModeButton(
    icon: @Composable (Color) -> Unit,
    label: String,
    sub: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, Border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Accent.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            icon(Accent)
        }
        Column {
            Text(label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(sub, color = TextMuted, fontSize = 11.sp)
        }
    }
}

/** Decode a QR from a picked image via ML Kit, then invoke callback with the id. */
private fun decodeImageUri(
    context: android.content.Context,
    uri: Uri,
    onResult: (String?) -> Unit,
) {
    val executor = Executors.newSingleThreadExecutor()
    val scanner = BarcodeScanning.getClient()
    executor.execute {
        runCatching {
            val image = InputImage.fromFilePath(context, uri)
            val result = Tasks.await(scanner.process(image))
            result.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue
        }.onSuccess { onResult(it) }
            .onFailure { onResult(null) }
        scanner.close()
        executor.shutdown()
    }
}
