package com.evrenhouse.trackscooter.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Ukuran referensi navbar mengambang
private val NavigationBarMaxWidth = 420.dp
private val NavigationBarHorizontalPadding = 20.dp
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
    monitorBadgeCount: Int = 0,
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
                val badge = if (item.route == Routes.MONITOR) monitorBadgeCount else 0
                FloatingNavItem(
                    item = item,
                    selected = selected,
                    isScan = isScan,
                    badgeCount = badge,
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
    badgeCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected && !isScan) 1.06f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "nav_scale",
    )

    val animatedPillColor by animateColorAsState(
        targetValue = when {
            isScan -> Accent
            selected -> Accent.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        animationSpec = tween(180),
        label = "nav_pill_color",
    )

    val animatedContentColor by animateColorAsState(
        targetValue = when {
            isScan -> Color.White
            selected -> Accent
            else -> TextMuted
        },
        animationSpec = tween(180),
        label = "nav_content_color",
    )

    val labelColor by animateColorAsState(
        targetValue = when {
            isScan && selected -> Accent
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
        if (isScan) {
            // Aksi Tengah Scan: Tombol bulat terintegrasi (berbeda jelas dari tab lain)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Accent)
                    .border(1.5.dp, Color.White.copy(alpha = if (selected) 0.6f else 0.2f), androidx.compose.foundation.shape.CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = "Pindai QR unit",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        } else {
            // Tab Navigasi Standar
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(animatedPillColor)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = "Navigasi ke ${item.label}",
                        tint = animatedContentColor,
                        modifier = Modifier.size(19.dp),
                    )
                    if (badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 8.dp, y = (-5).dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444))
                                .padding(horizontal = 4.dp, vertical = 0.5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (badgeCount > 99) "99+" else "$badgeCount",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        // Label dengan font size sama persis (10.sp) di seluruh item agar baseline selalu sejajar
        Text(
            text = item.label,
            color = labelColor,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}
