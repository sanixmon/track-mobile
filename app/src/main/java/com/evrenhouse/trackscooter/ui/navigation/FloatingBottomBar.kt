package com.evrenhouse.trackscooter.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

/**
 * Transparent Floating Navigation Bar
 * Tanpa dock / card background — murni navbar transparan yang melayang di atas background.
 * Ukuran lebih ramping/kompak, dengan tab aktif sedikit lebih besar (animasi spring scale, icon & font lebih besar, highlight aksen lembut).
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
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                FloatingNavItem(
                    item = item,
                    selected = selected,
                    onClick = { onNavigate(item) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FloatingNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Tab aktif sedikit lebih besar (scale 1.09f) dengan animasi spring yang halus
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.09f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "nav_scale",
    )

    val iconSize by animateDpAsState(
        targetValue = if (selected) 20.dp else 17.dp,
        animationSpec = tween(durationMillis = 180),
        label = "nav_icon_size",
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (selected) Accent.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = tween(180),
        label = "nav_bg",
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) Accent else TextMuted,
        animationSpec = tween(180),
        label = "nav_content_color",
    )

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(animatedBgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = animatedContentColor,
                modifier = Modifier.size(iconSize),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                color = animatedContentColor,
                fontSize = if (selected) 10.5.sp else 9.5.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
            )
        }
    }
}
