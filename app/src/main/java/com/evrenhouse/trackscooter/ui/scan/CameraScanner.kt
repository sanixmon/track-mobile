package com.evrenhouse.trackscooter.ui.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import com.evrenhouse.trackscooter.ui.common.FilledAction
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraScanner"

@Composable
fun CameraScanner(
    isProcessing: Boolean = false,
    onScan: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var denied by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        denied = !granted
    }

    if (!hasPermission) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Surface, RoundedCornerShape(16.dp))
                .border(1.dp, Border, RoundedCornerShape(16.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.QrCodeScanner,
                contentDescription = null,
                tint = TextSubtle,
                modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                if (denied) "Izin kamera ditolak. Aktifkan izin kamera di Pengaturan." else "Butuh izin kamera untuk memindai QR code scooter.",
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )
            Spacer(Modifier.height(16.dp))
            FilledAction(
                text = if (denied) "Buka Pengaturan" else "Izinkan Kamera",
                onClick = {
                    if (denied) {
                        context.startActivity(
                            android.content.Intent(
                                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                android.net.Uri.fromParts("package", context.packageName, null),
                            )
                        )
                    } else {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
            )
        }
        return
    }

    var liveDetectedBarcode by remember { mutableStateOf<String?>(null) }
    var lastDetectionTime by remember { mutableStateOf(0L) }
    var isCameraBound by remember { mutableStateOf(false) }

    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val analysisExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    // Auto-clear stale detection if QR leaves frame for > 2 seconds
    LaunchedEffect(lastDetectionTime) {
        if (lastDetectionTime > 0) {
            delay(2000)
            if (System.currentTimeMillis() - lastDetectionTime >= 1900) {
                liveDetectedBarcode = null
            }
        }
    }

    LaunchedEffect(previewView) {
        val scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        val cameraProvider = try {
            ProcessCameraProvider.getInstance(context).await()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal init camera provider", e)
            onError("Gagal menginisialisasi kamera.")
            return@LaunchedEffect
        }

        val preview = Preview.Builder().build()
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        analysis.setAnalyzer(analysisExecutor) { imageProxy ->
            processImageProxy(imageProxy, scanner) { barcode ->
                val cleaned = barcode.trim()
                if (cleaned.isNotEmpty()) {
                    val isNew = liveDetectedBarcode != cleaned
                    liveDetectedBarcode = cleaned
                    lastDetectionTime = System.currentTimeMillis()
                    if (isNew) {
                        triggerLightVibration(context)
                    }
                }
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
            )
            preview.setSurfaceProvider(previewView.surfaceProvider)
            isCameraBound = true
        } catch (e: Exception) {
            Log.e(TAG, "Gagal bind kamera", e)
            onError("Tidak dapat mengakses kamera.")
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Camera Viewfinder Box (Locked Aspect Ratio to eliminate layout shift)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Surface2)
                .border(1.5.dp, if (liveDetectedBarcode != null) Green else Border, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
            )

            if (!isCameraBound) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(color = Accent, modifier = Modifier.size(28.dp), strokeWidth = 2.5.dp)
                    Text("Menyiapkan kamera...", color = TextMuted, fontSize = 12.sp)
                }
            }

            // Viewfinder Reticle Frame Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize(0.72f)
                    .border(1.5.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
            )

            // Live Detected Badge at Bottom of Viewfinder
            AnimatedVisibility(
                visible = liveDetectedBarcode != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .background(Color(0xEE0A0A0A), RoundedCornerShape(20.dp))
                        .border(1.dp, Green.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(modifier = Modifier.size(8.dp).background(Green, CircleShape))
                    Text(
                        "QR Terdeteksi: ${liveDetectedBarcode.orEmpty()}",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }

            // Scanning Prompt when no QR in view
            AnimatedVisibility(
                visible = liveDetectedBarcode == null && isCameraBound,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .background(Color(0xCC000000), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        "Arahkan kamera ke QR code unit",
                        color = TextMuted,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        // Explicit Manual Trigger Button to Prevent Accidental Scans
        Button(
            onClick = {
                liveDetectedBarcode?.let { code ->
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onScan(code)
                }
            },
            enabled = liveDetectedBarcode != null && !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Accent,
                contentColor = Color.White,
                disabledContainerColor = Surface2,
                disabledContentColor = TextSubtle,
            ),
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
                Spacer(Modifier.width(8.dp))
                Text("Memproses...", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            } else if (liveDetectedBarcode != null) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Pindai Sekarang: ${liveDetectedBarcode.orEmpty()}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
            } else {
                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Arahkan ke QR untuk Pindai", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private fun triggerLightVibration(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(30)
        }
    } catch (_: Exception) {
        // Safe fallback if vibration permission not granted
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    onBarcode: (String) -> Unit,
) {
    val mediaImage = imageProxy.image
    if (mediaImage == null) {
        imageProxy.close()
        return
    }

    val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

    scanner.process(input)
        .addOnSuccessListener { barcodes ->
            barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }?.let { barcode ->
                onBarcode(barcode.rawValue!!)
            }
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}
