package com.evrenhouse.trackscooter.ui.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.AccentSubtle
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Border2
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.GreenSubtle
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.RedSubtle
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.Surface3
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.ui.theme.WarningSubtle
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicLong

enum class SweetAlertType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    CONFIRM
}

data class SweetAlertData(
    val id: Long,
    val type: SweetAlertType,
    val title: String,
    val message: String,
    val confirmText: String = "OK",
    val cancelText: String? = null,
    val isDangerConfirm: Boolean = false,
    val autoDismissMillis: Long? = null,
    val onConfirm: (() -> Unit)? = null,
    val onCancel: (() -> Unit)? = null,
)

class SweetAlertController {
    private val idGen = AtomicLong(0)
    var currentAlert by mutableStateOf<SweetAlertData?>(null)
        private set

    fun showSuccess(
        message: String,
        title: String = "Berhasil",
        autoDismissMillis: Long? = 2500L,
        onConfirm: (() -> Unit)? = null,
    ) {
        currentAlert = SweetAlertData(
            id = idGen.incrementAndGet(),
            type = SweetAlertType.SUCCESS,
            title = title,
            message = message,
            confirmText = "OK",
            autoDismissMillis = autoDismissMillis,
            onConfirm = onConfirm,
        )
    }

    fun showError(
        message: String,
        title: String = "Terjadi Kesalahan",
        onConfirm: (() -> Unit)? = null,
    ) {
        currentAlert = SweetAlertData(
            id = idGen.incrementAndGet(),
            type = SweetAlertType.ERROR,
            title = title,
            message = message,
            confirmText = "Tutup",
            autoDismissMillis = null,
            onConfirm = onConfirm,
        )
    }

    fun showWarning(
        message: String,
        title: String = "Perhatian",
        onConfirm: (() -> Unit)? = null,
    ) {
        currentAlert = SweetAlertData(
            id = idGen.incrementAndGet(),
            type = SweetAlertType.WARNING,
            title = title,
            message = message,
            confirmText = "OK",
            autoDismissMillis = null,
            onConfirm = onConfirm,
        )
    }

    fun showInfo(
        message: String,
        title: String = "Informasi",
        autoDismissMillis: Long? = 2500L,
        onConfirm: (() -> Unit)? = null,
    ) {
        currentAlert = SweetAlertData(
            id = idGen.incrementAndGet(),
            type = SweetAlertType.INFO,
            title = title,
            message = message,
            confirmText = "OK",
            autoDismissMillis = autoDismissMillis,
            onConfirm = onConfirm,
        )
    }

    fun showConfirm(
        title: String = "Konfirmasi",
        message: String,
        confirmText: String = "Ya, Lanjutkan",
        cancelText: String = "Batal",
        isDanger: Boolean = false,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null,
    ) {
        currentAlert = SweetAlertData(
            id = idGen.incrementAndGet(),
            type = SweetAlertType.CONFIRM,
            title = title,
            message = message,
            confirmText = confirmText,
            cancelText = cancelText,
            isDangerConfirm = isDanger,
            autoDismissMillis = null,
            onConfirm = onConfirm,
            onCancel = onCancel,
        )
    }

    fun dismiss() {
        currentAlert = null
    }
}

val LocalSweetAlert = compositionLocalOf<SweetAlertController> {
    error("SweetAlertController not provided. Wrap your UI in SweetAlertProvider.")
}

@Composable
fun rememberSweetAlertController(): SweetAlertController {
    return remember { SweetAlertController() }
}

@Composable
fun SweetAlertProvider(
    controller: SweetAlertController = rememberSweetAlertController(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalSweetAlert provides controller) {
        content()
        SweetAlertHost(controller = controller)
    }
}

@Composable
fun SweetAlertHost(controller: SweetAlertController) {
    val alert = controller.currentAlert ?: return

    SweetAlertDialog(
        data = alert,
        onDismiss = { controller.dismiss() },
    )
}

@Composable
fun SweetAlertDialog(
    data: SweetAlertData,
    onDismiss: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    // Trigger subtle haptic feedback when alert appears
    LaunchedEffect(data.id) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    // Auto-dismiss handler if specified
    LaunchedEffect(data.id, data.autoDismissMillis) {
        val duration = data.autoDismissMillis
        if (duration != null && duration > 0) {
            delay(duration)
            onDismiss()
        }
    }

    // Scale-in spring bounce animation
    val scale = remember(data.id) { Animatable(0.7f) }
    LaunchedEffect(data.id) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .scale(scale.value)
                .padding(horizontal = 28.dp)
                .widthIn(min = 280.dp, max = 360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Surface2)
                .border(1.dp, Border, RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Animated Icon Badge
                SweetAlertIcon(type = data.type, isDanger = data.isDangerConfirm)

                Spacer(modifier = Modifier.height(18.dp))

                // Title
                Text(
                    text = data.title,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message Body
                Text(
                    text = data.message,
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(22.dp))

                // Action Buttons
                if (data.cancelText != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                onDismiss()
                                data.onCancel?.invoke()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Border2),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Surface3,
                                contentColor = TextMuted,
                            ),
                        ) {
                            Text(
                                text = data.cancelText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        val confirmBg = if (data.isDangerConfirm) Red else Accent
                        Button(
                            onClick = {
                                onDismiss()
                                data.onConfirm?.invoke()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = confirmBg,
                                contentColor = Color.White,
                            ),
                        ) {
                            Text(
                                text = data.confirmText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                } else {
                    val buttonBg = when (data.type) {
                        SweetAlertType.SUCCESS -> Green
                        SweetAlertType.ERROR -> Red
                        SweetAlertType.WARNING -> Warning
                        SweetAlertType.INFO, SweetAlertType.CONFIRM -> Accent
                    }
                    Button(
                        onClick = {
                            onDismiss()
                            data.onConfirm?.invoke()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonBg,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text(
                            text = data.confirmText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SweetAlertIcon(type: SweetAlertType, isDanger: Boolean) {
    val (bgColor, borderColor, iconTint, iconVector) = when (type) {
        SweetAlertType.SUCCESS -> Quad(GreenSubtle, Green, Green, Icons.Filled.Check)
        SweetAlertType.ERROR -> Quad(RedSubtle, Red, Red, Icons.Filled.Close)
        SweetAlertType.WARNING -> Quad(WarningSubtle, Warning, Warning, Icons.Filled.PriorityHigh)
        SweetAlertType.INFO -> Quad(AccentSubtle, Accent, Accent, Icons.Filled.Info)
        SweetAlertType.CONFIRM -> {
            if (isDanger) {
                Quad(RedSubtle, Red, Red, Icons.Filled.Close)
            } else {
                Quad(WarningSubtle, Warning, Warning, Icons.Filled.PriorityHigh)
            }
        }
    }

    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.dp, borderColor.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // Inner ring for depth
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(1.5.dp, borderColor.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
