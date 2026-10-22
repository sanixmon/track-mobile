package com.evrenhouse.trackscooter.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.FloatingBarCornerRadius
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.bounceClickable

data class SuperappTabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val PrimaryTabs = listOf(
    SuperappTabItem(Routes.DASHBOARD, "Beranda", Icons.Default.Dashboard),
    SuperappTabItem(Routes.MONITOR, "Monitor", Icons.Default.Monitor),
    SuperappTabItem(Routes.REPORT, "Laporan", Icons.Default.Assessment),
    SuperappTabItem(Routes.MANAGE, "Kelola", Icons.Default.Tune),
)

/**
 * Superapp Floating Pill Navigation Dock with Raised Center Action FAB.
 * 4 tabs + Center Scan Action Button.
 */
@Composable
fun SuperappBottomBar(
    currentDestination: NavDestination?,
    onTabSelected: (String) -> Unit,
    onScanClick: () -> Unit,
    monitorBadgeCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // Floating Pill Dock Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (isDark) 0.dp else 16.dp,
                    shape = RoundedCornerShape(FloatingBarCornerRadius),
                    spotColor = Color(0x26000000),
                    ambientColor = Color(0x1A000000),
                )
                .clip(RoundedCornerShape(FloatingBarCornerRadius))
                .background(if (isDark) Surface2.copy(alpha = 0.95f) else Surface.copy(alpha = 0.96f))
                .border(
                    width = 1.dp,
                    color = Border.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(FloatingBarCornerRadius),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                // Tab 1: Beranda
                NavTabButton(
                    item = PrimaryTabs[0],
                    currentDestination = currentDestination,
                    onSelect = onTabSelected,
                    modifier = Modifier.weight(1f),
                )

                // Tab 2: Monitor
                NavTabButton(
                    item = PrimaryTabs[1],
                    currentDestination = currentDestination,
                    onSelect = onTabSelected,
                    badgeCount = monitorBadgeCount,
                    modifier = Modifier.weight(1f),
                )

                // Center Spacer reserved for Raised FAB
                Spacer(modifier = Modifier.size(56.dp))

                // Tab 3: Laporan
                NavTabButton(
                    item = PrimaryTabs[2],
                    currentDestination = currentDestination,
                    onSelect = onTabSelected,
                    modifier = Modifier.weight(1f),
                )

                // Tab 4: Kelola
                NavTabButton(
                    item = PrimaryTabs[3],
                    currentDestination = currentDestination,
                    onSelect = onTabSelected,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Raised Center Action FAB (Scan QR)
        Box(
            modifier = Modifier
                .offset(y = (-14).dp)
                .size(58.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    spotColor = Accent.copy(alpha = 0.5f),
                    ambientColor = Accent.copy(alpha = 0.25f),
                )
                .clip(CircleShape)
                .background(Accent)
                .border(3.dp, if (isDark) Surface else Color.White, CircleShape)
                .bounceClickable(onClick = onScanClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan QR",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun NavTabButton(
    item: SuperappTabItem,
    currentDestination: NavDestination?,
    onSelect: (String) -> Unit,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

    val iconColor by animateColorAsState(
        targetValue = if (selected) Accent else TextMuted,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "iconColor",
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabScale",
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .bounceClickable { onSelect(item.route) }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
            )

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 6.dp, y = (-4).dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Red),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (badgeCount > 9) "9+" else "$badgeCount",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = item.label,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = iconColor,
        )
    }
}
