package com.evrenhouse.trackscooter.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.input.pointer.pointerInput
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.TextSubtle
import com.evrenhouse.trackscooter.util.ModePrefs
import kotlinx.coroutines.delay

/**
 * Full-screen 2-second splash screen transition overlay between
 * Operasional and Manajemen modes.
 */
@Composable
fun ModeTransitionSplash(
    targetMode: String,
    onSwitch: () -> Unit = {},
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isManajemen = targetMode == ModePrefs.MANAJEMEN

    var progressTarget by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
        label = "splash_progress",
    )

    // Pulse animation for icon glow
    val infiniteTransition = rememberInfiniteTransition(label = "splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )
    var splashVisible by remember { mutableStateOf(true) }

    LaunchedEffect(targetMode) {
        progressTarget = 1f
        // Pindahkan mode dan navigasi di detik awal saat layar tertutup penuh oleh splash (600ms)
        // agar layar tujuan punya waktu cukup untuk render dan stabil di belakang splash screen
        delay(600L)
        onSwitch()
        // Pertahankan splash screen tetap menutup penuh selama sisa 1400ms agar tidak ada komponen glitchy
        delay(1400L)
        splashVisible = false
        // Berikan waktu fade out halus sebelum overlay dilepas
        delay(300L)
        onFinished()
    }

    AnimatedVisibility(
        visible = splashVisible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Intercept all touches during the 2-second transition
                }
                .background(Surface),
            contentAlignment = Alignment.Center,
        ) {
        // Decorative background radial gradient
        Box(
            modifier = Modifier
                .size(340.dp)
                .scale(pulseScale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            if (isManajemen) Accent.copy(alpha = 0.22f) else Accent.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            // Icon Badge with pulse
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Surface2)
                    .border(1.5.dp, Accent.copy(alpha = 0.5f), RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isManajemen) Icons.Filled.Inventory2 else Icons.Filled.ElectricScooter,
                    contentDescription = null,
                    tint = Accent,
                    modifier = Modifier.size(46.dp),
                )
            }

            Spacer(Modifier.height(28.dp))

            // Mode Label Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Accent.copy(alpha = 0.12f))
                    .border(1.dp, Accent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = if (isManajemen) Icons.Filled.Tune else Icons.Filled.Home,
                        contentDescription = null,
                        tint = Accent,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = if (isManajemen) "Mode Manajemen" else "Mode Operasional",
                        color = Accent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Main Title
            Text(
                text = if (isManajemen) "Beralih ke Mode Manajemen..." else "Beralih ke Mode Operasional...",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )

            Spacer(Modifier.height(6.dp))

            // Subtitle
            Text(
                text = if (isManajemen) {
                    "Menyiapkan modul kelola armada, riwayat, dan analitik"
                } else {
                    "Menyiapkan modul pemindai lapangan, live monitor, dan laporan"
                },
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )

            Spacer(Modifier.height(36.dp))

            // 2-second Progress Bar
            Column(
                modifier = Modifier.width(220.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Accent,
                    trackColor = Surface2,
                    strokeCap = StrokeCap.Round,
                )

                Text(
                    text = "Memuat antarmuka...",
                    color = TextSubtle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
    }
}
