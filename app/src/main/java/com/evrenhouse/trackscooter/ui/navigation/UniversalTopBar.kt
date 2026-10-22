package com.evrenhouse.trackscooter.ui.navigation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricScooter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.AccentSubtle
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.bounceClickable
import com.evrenhouse.trackscooter.util.Outlets

/**
 * Fixed Universal Header (Top Bar).
 * Minimalist, high legibility, clean.
 * Displays the dynamic active outlet name and a live connection indicator.
 * Clicking the outlet pill triggers [onOutletClick] to open the large bottom sheet.
 */
@Composable
fun UniversalTopBar(
    selectedOutletId: String,
    totalScooterCount: Int = 0,
    onOutletClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current
    val outletLabel = Outlets.ALL_OUTLETS.find { it.id == selectedOutletId }?.label
        ?: Outlets.labelOf(selectedOutletId)

    // Pulse animation for the live sync dot
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Brand Logo & Dynamic Outlet Pill (Tap to change)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(if (isDark) Surface2 else Color(0xFFF1F5F9))
                    .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(999.dp))
                    .bounceClickable(onClick = onOutletClick)
                    .padding(horizontal = 12.dp, vertical = 7.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = outletLabel,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (totalScooterCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentSubtle)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "$totalScooterCount",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Pilih Outlet",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp),
                )
            }

            // Right side: Clean minimal live status pulse
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Green.copy(alpha = if (isDark) 0.15f else 0.12f))
                    .padding(horizontal = 9.dp, vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .graphicsLayer { alpha = pulseAlpha }
                        .clip(CircleShape)
                        .background(Green),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "LIVE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Green,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }
}
