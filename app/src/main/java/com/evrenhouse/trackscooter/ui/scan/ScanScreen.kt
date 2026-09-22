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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.OutlinedAction
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun ScanScreen(viewModel: ScanViewModel = viewModel(factory = AppViewModelFactory(repository()))) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current

    var mode by rememberSaveable { mutableStateOf<String?>(null) } // null | "camera" | "image"
    var manualValue by rememberSaveable { mutableStateOf("") }
    var showManual by rememberSaveable { mutableStateOf(false) }
    var decodingImage by remember { mutableStateOf(false) }

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
        // Header
        Column {
            Text("Scan QR Scooter", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Pindai QR code untuk toggle status scooter", color = TextMuted, fontSize = 13.sp)
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

        // Manual input — always visible below
        if (!showManual) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManual = true }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Tag, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "Masukkan ID scooter manual",
                    color = TextSubtle,
                    fontSize = 12.sp,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("ID Scooter", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = manualValue,
                        onValueChange = { manualValue = it.uppercase() },
                        placeholder = { Text("SD-1", color = TextSubtle, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    )
                    androidx.compose.material3.Button(
                        onClick = {
                            if (manualValue.isNotBlank()) {
                                viewModel.onScanned(manualValue.trim())
                                manualValue = ""
                            }
                        },
                        enabled = manualValue.isNotBlank() && state.scanning && !state.busy,
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Accent),
                    ) {
                        Text("OK", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
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
            .background(Surface, RoundedCornerShape(14.dp))
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
