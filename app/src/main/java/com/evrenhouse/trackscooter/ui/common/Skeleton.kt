package com.evrenhouse.trackscooter.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2

/**
 * Creates an animated linear gradient brush for shimmer effects.
 * Works seamlessly in both Light and Dark themes.
 */
@Composable
fun shimmerBrush(
    isDark: Boolean = LocalThemeIsDark.current,
): Brush {
    val shimmerColors = if (isDark) {
        listOf(
            Surface2,
            Surface2.copy(alpha = 0.6f),
            Surface2.copy(alpha = 0.95f),
            Surface2,
        )
    } else {
        listOf(
            Color(0xFFE8EEF5),
            Color(0xFFF1F5F9),
            Color(0xFFFFFFFF),
            Color(0xFFE8EEF5),
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_translate",
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnim - 350f, y = translateAnim - 350f),
        end = Offset(x = translateAnim, y = translateAnim),
    )
}

/**
 * Generic rectangular skeleton element.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp),
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush),
    )
}

/**
 * Single skeleton text line placeholder.
 */
@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 14.dp,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    val boxModifier = modifier
        .height(height)
        .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())

    SkeletonBox(
        modifier = boxModifier,
        shape = shape,
    )
}

/**
 * Dashboard skeleton: Stat Cards, Outlet Summary, Recent Activity Table, Type Breakdown.
 */
@Composable
fun DashboardSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Outlet Dropdown Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
        )

        // 4 Stat Cards Grid (2x2)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    shape = RoundedCornerShape(14.dp),
                )
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    shape = RoundedCornerShape(14.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    shape = RoundedCornerShape(14.dp),
                )
                SkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }

        // Recent Activity Table Card Skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SkeletonLine(width = 120.dp, height = 16.dp)
                SkeletonBox(modifier = Modifier.size(54.dp, 20.dp), shape = RoundedCornerShape(6.dp))
            }

            repeat(4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SkeletonBox(modifier = Modifier.size(32.dp, 16.dp), shape = RoundedCornerShape(4.dp))
                        SkeletonLine(width = 90.dp, height = 12.dp)
                    }
                    SkeletonBox(modifier = Modifier.size(60.dp, 20.dp), shape = RoundedCornerShape(6.dp))
                }
            }
        }

        // Type Summary Card Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            shape = RoundedCornerShape(14.dp),
        )
    }
}

/**
 * Monitor Screen Skeleton: Tabs, Live Sessions, Standby units.
 */
@Composable
fun MonitorSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Tab Selector Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            shape = RoundedCornerShape(10.dp),
        )

        // Standby Units Strip Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
        )

        // Live Session Cards Skeleton
        repeat(3) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface)
                    .border(1.dp, Border, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SkeletonBox(modifier = Modifier.size(48.dp, 20.dp), shape = RoundedCornerShape(6.dp))
                        SkeletonBox(modifier = Modifier.size(40.dp, 20.dp), shape = RoundedCornerShape(6.dp))
                    }
                    SkeletonBox(modifier = Modifier.size(70.dp, 24.dp), shape = RoundedCornerShape(8.dp))
                }
                SkeletonLine(width = 160.dp, height = 12.dp)
            }
        }
    }
}

/**
 * Manage Screen Skeleton: Outlet filter, Search box, Unit cards list.
 */
@Composable
fun ManageSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Search & Filter Toolbar Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(10.dp),
        )

        // Unit Cards List Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SkeletonLine(width = 140.dp, height = 16.dp)
                SkeletonBox(modifier = Modifier.size(72.dp, 26.dp), shape = RoundedCornerShape(8.dp))
            }

            repeat(5) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            SkeletonBox(modifier = Modifier.size(54.dp, 20.dp), shape = RoundedCornerShape(6.dp))
                            SkeletonBox(modifier = Modifier.size(40.dp, 20.dp), shape = RoundedCornerShape(6.dp))
                            SkeletonBox(modifier = Modifier.size(50.dp, 20.dp), shape = RoundedCornerShape(6.dp))
                        }
                        SkeletonBox(modifier = Modifier.size(24.dp, 24.dp), shape = CircleShape)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        SkeletonBox(modifier = Modifier.size(80.dp, 24.dp), shape = RoundedCornerShape(12.dp))
                        SkeletonLine(width = 50.dp, height = 12.dp)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Border),
                    )
                }
            }
        }
    }
}

/**
 * Report Screen Skeleton: Date bar, Summary chips, Attendance rows.
 */
@Composable
fun ReportSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Date & Filter Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(12.dp),
        )

        // Summary Chips Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SkeletonBox(modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(10.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(10.dp))
            SkeletonBox(modifier = Modifier.weight(1f).height(38.dp), shape = RoundedCornerShape(10.dp))
        }

        // Attendance List Skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(5) {
                SkeletonBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                )
            }
        }
    }
}

/**
 * Scooter Detail Screen Skeleton: Info header, timer banner, condition fields.
 */
@Composable
fun DetailSkeleton(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Top Header Skeleton
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SkeletonBox(modifier = Modifier.size(72.dp, 28.dp), shape = RoundedCornerShape(8.dp))
            SkeletonBox(modifier = Modifier.size(80.dp, 24.dp), shape = RoundedCornerShape(12.dp))
        }

        // Timer Banner Skeleton
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            shape = RoundedCornerShape(12.dp),
        )

        // Condition Editor Cards Skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Surface)
                .border(1.dp, Border, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SkeletonLine(width = 160.dp, height = 16.dp)

            repeat(5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonLine(width = 110.dp, height = 14.dp)
                    SkeletonBox(modifier = Modifier.size(46.dp, 24.dp), shape = RoundedCornerShape(12.dp))
                }
            }
        }
    }
}
