package com.evrenhouse.trackscooter.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.TextMuted

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

// Ukuran referensi terinspirasi dari FloatingNavigationToolbar ArchiveTune
private val NavigationBarMaxWidth = 380.dp
private val NavigationBarHorizontalPadding = 12.dp
private val NavigationBarBottomPadding = 8.dp

/**
 * Transparent Floating Navigation Bar (Referensi: ArchiveTune)
 * - Murni transparan tanpa dock card / border luar.
 * - Ukuran lebih kompak dan ramping.
 * - Tab aktif sedikit lebih besar (spring scale 1.08x, icon lebih besar, pill indikator pada icon, font semi-bold).
 */
@Composable
fun FloatingBottomBar(
    items: List<BottomNavItem>,
    currentDestination: NavDestination?,
    onNavigate: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = NavigationBarHorizontalPadding,
                end = NavigationBarHorizontalPadding,
                bottom = NavigationBarBottomPadding,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = NavigationBarMaxWidth)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                val isScan = item.route == Routes.SCAN
                FloatingNavItem(
                    item = item,
                    selected = selected,
                    isScan = isScan,
                    onClick = { onNavigate(item) },
                    modifier = Modifier.weight(if (isScan) 1.5f else 1f),
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: BottomNavItem,
    selected: Boolean,
    isScan: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tab aktif sedikit lebih besar (scale 1.08f) dengan animasi spring halus
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "nav_scale",
    )

    // Tombol scan 1,5x lebih besar dari tombol lain
    val iconSize by animateDpAsState(
        targetValue = if (isScan) {
            if (selected) 30.dp else 26.dp
        } else {
            if (selected) 20.dp else 17.dp
        },
        animationSpec = tween(durationMillis = 180),
        label = "nav_icon_size",
    )

    val animatedPillColor by animateColorAsState(
        targetValue = when {
            isScan && selected -> Accent
            isScan -> Accent.copy(alpha = 0.18f)
            selected -> Accent.copy(alpha = 0.16f)
            else -> Color.Transparent
        },
        animationSpec = tween(180),
        label = "nav_pill_color",
    )

    val animatedContentColor by animateColorAsState(
        targetValue = when {
            isScan && selected -> Color.White
            isScan -> Accent
            selected -> Accent
            else -> TextMuted
        },
        animationSpec = tween(180),
        label = "nav_content_color",
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            isScan -> Accent
            selected -> Accent
            else -> TextMuted
        },
        animationSpec = tween(180),
        label = "nav_label_color",
    )

    Column(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Pill indikator di belakang icon (1,5x lebih besar jika scan)
        Box(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(if (isScan) 16.dp else 12.dp))
                .background(animatedPillColor)
                .padding(
                    horizontal = if (isScan) 18.dp else 12.dp,
                    vertical = if (isScan) 6.dp else 4.dp,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = "Navigasi ke ${item.label}",
                tint = animatedContentColor,
                modifier = Modifier.size(iconSize),
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            color = labelColor,
            fontSize = if (isScan) 11.5.sp else 10.sp,
            fontWeight = if (isScan) {
                if (selected) FontWeight.Bold else FontWeight.SemiBold
            } else {
                if (selected) FontWeight.SemiBold else FontWeight.Normal
            },
            maxLines = 1,
        )
    }
}
