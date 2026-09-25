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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.OutletDropdown
import com.evrenhouse.trackscooter.ui.common.OutlinedAction
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.TypeBadge
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.ScooterColors
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

enum class ScanSubTab(val label: String, val icon: ImageVector) {
    SCAN("Scan", Icons.Filled.QrCodeScanner),
    BY_ID("By ID", Icons.Filled.Tag),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScanScreen(
    dataViewModel: ScooterDataViewModel = viewModel(factory = AppViewModelFactory(repository())),
    viewModel: ScanViewModel = viewModel(factory = AppViewModelFactory(repository())),
) {
    val state by viewModel.state.collectAsState()
    val dataState by dataViewModel.state.collectAsState()
    val globalOutlet by dataViewModel.selectedOutlet.collectAsState()

    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current

    var selectedSubTab by rememberSaveable { mutableStateOf(ScanSubTab.SCAN) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var decodingImage by remember { mutableStateOf(false) }

    // Ready scooters (ScooterStatus.AVAILABLE) filtered by global outlet
    val outletFilteredReadyScooters by remember(dataState.scooters, globalOutlet) {
        derivedStateOf {
            val list = dataState.scooters.filter { s ->
                val isReady = s.status == ScooterStatus.AVAILABLE
                val matchesOutlet = if (globalOutlet == "all") true
                else (s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)) == globalOutlet
                isReady && matchesOutlet
            }
            list.sortedWith(
                compareBy<Scooter> { it.id.filter { ch -> !ch.isDigit() } }
                    .thenBy { it.id.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
            )
        }
    }

    // Ready scooters filtered by search query
    val displayReadyScooters by remember(outletFilteredReadyScooters, searchQuery) {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                outletFilteredReadyScooters
            } else {
                outletFilteredReadyScooters.filter {
                    it.id.contains(searchQuery.trim(), ignoreCase = true)
                }
            }
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
        // ── Header ──
        Column {
            Text("Scan QR Scooter", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                text = if (selectedSubTab == ScanSubTab.SCAN) "Pindai QR code scooter untuk toggle status sewa"
                else "Pilih unit ready berdasarkan ID & outlet",
                color = TextMuted,
                fontSize = 12.sp,
            )
        }

        // ── 2 Sub Tab Selector (Sub Tab 1: Scan, Sub Tab 2: By ID) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Surface2, RoundedCornerShape(12.dp))
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            ScanSubTab.entries.forEach { tab ->
                val isSelected = selectedSubTab == tab
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) Accent else Color.Transparent)
                        .clickable { selectedSubTab = tab }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else TextMuted,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = tab.label,
                        color = if (isSelected) Color.White else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    )
                    if (tab == ScanSubTab.BY_ID) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Color.White.copy(alpha = 0.25f)
                                    else Green.copy(alpha = 0.2f),
                                    CircleShape,
                                )
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = outletFilteredReadyScooters.size.toString(),
                                color = if (isSelected) Color.White else Green,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════
        // SUB TAB 1: SCAN (LANGSUNG OPEN KAMERA)
        // ══════════════════════════════════════════════════════════
        if (selectedSubTab == ScanSubTab.SCAN) {
            CameraScanner(
                isProcessing = state.busy || !state.scanning,
                onScan = { viewModel.onScanned(it) },
                onError = { sweetAlert.showError(it) },
            )

            if (decodingImage) {
                Text("Mendekode QR dari gambar...", color = TextMuted, fontSize = 12.sp)
            }

            // Quick alternative: Gallery upload
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedAction(
                    text = "Upload QR dari Galeri",
                    icon = Icons.Filled.Image,
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    color = TextMuted,
                )
            }
        }

        // ══════════════════════════════════════════════════════════
        // SUB TAB 2: BY ID (LIST UNIT READY AFFECTED BY OUTLET FILTER)
        // ══════════════════════════════════════════════════════════
        if (selectedSubTab == ScanSubTab.BY_ID) {
            // Outlet Dropdown Filter
            OutletDropdown(
                selectedOutletId = globalOutlet,
                onOutletSelected = { dataViewModel.setSelectedOutlet(it) },
                getOutletCount = { outletId ->
                    if (outletId == "all") {
                        dataState.scooters.count { it.status == ScooterStatus.AVAILABLE }
                    } else {
                        dataState.scooters.count {
                            (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == outletId &&
                            it.status == ScooterStatus.AVAILABLE
                        }
                    }
                },
                labelPrefix = "Filter Outlet:",
            )

            // Search by Unit ID
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it.uppercase() },
                placeholder = { Text("Cari nomor ID unit (cth: SD-01)...", color = TextSubtle, fontSize = 12.sp) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                ),
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                ),
            )

            // Header Ready Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Green, CircleShape),
                    )
                    Text(
                        text = "UNIT READY (${displayReadyScooters.size})",
                        color = TextSubtle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                    )
                }

                Text(
                    text = "Ketuk unit untuk proses sewa",
                    color = TextMuted,
                    fontSize = 11.sp,
                )
            }

            // List / FlowRow of Ready Scooters
            if (displayReadyScooters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface)
                        .border(1.dp, Border, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = TextSubtle,
                            modifier = Modifier.size(28.dp),
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada unit ready dengan ID \"$searchQuery\""
                            else if (globalOutlet == "all") "Tidak ada unit ready saat ini."
                            else "Tidak ada unit ready di ${Outlets.labelOf(globalOutlet)}.",
                            color = TextMuted,
                            fontSize = 12.sp,
                        )
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val isDark = LocalThemeIsDark.current
                    displayReadyScooters.forEach { s ->
                        val isSelected = searchQuery.equals(s.id, ignoreCase = true)
                        val nameColor = if (isSelected) Accent else ScooterColors.getScooterNameColor(s.type, s.id, s.currentOutlet, isDark)
                        val outletName = Outlets.shortLabelOf(s.currentOutlet ?: Outlets.getHomeOutletForType(s.type))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Accent.copy(alpha = 0.2f) else Surface)
                                .border(1.dp, if (isSelected) Accent else Border, RoundedCornerShape(10.dp))
                                .clickable(enabled = state.scanning && !state.busy) {
                                    viewModel.onScanned(s.id)
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Text(
                                        text = s.id,
                                        color = nameColor,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                    )
                                    TypeBadge(type = s.type)
                                }
                                Spacer(Modifier.height(3.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Green, CircleShape),
                                    )
                                    Text(
                                        text = outletName,
                                        color = TextSubtle,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Submit button if manual input typed
            if (searchQuery.isNotBlank()) {
                Button(
                    onClick = {
                        viewModel.onScanned(searchQuery.trim())
                        searchQuery = ""
                    },
                    enabled = state.scanning && !state.busy,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                ) {
                    Text("Proses Unit $searchQuery", fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
            onDismiss = { viewModel.dismissConfirmation() },
        )
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
