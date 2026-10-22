package com.evrenhouse.trackscooter.ui.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.CardCornerRadius
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.Red
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.ui.theme.bounceClickable

data class FleetStats(
    val total: Int,
    val ready: Int,
    val inUse: Int,
    val maintLuar: Int,
    val maintOutlet: Int,
)

/**
 * Interactive Fleet Gauge & Status Filters.
 * Minimalist, high legibility, clean.
 * Tapping a filter pill immediately updates the inline list below.
 */
@Composable
fun InteractiveFleetGauge(
    stats: FleetStats,
    selectedFilter: String?, // null = all, "available", "in-use", "maint-luar", "maint-outlet"
    onSelectFilter: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current

    val total = if (stats.total > 0) stats.total.toFloat() else 1f
    val readyAngle = (stats.ready / total) * 360f
    val inUseAngle = (stats.inUse / total) * 360f
    val maintLuarAngle = (stats.maintLuar / total) * 360f
    val maintOutletAngle = (stats.maintOutlet / total) * 360f

    val animReady by animateFloatAsState(targetValue = readyAngle, animationSpec = tween(600), label = "ready")
    val animInUse by animateFloatAsState(targetValue = inUseAngle, animationSpec = tween(600), label = "inUse")
    val animMaintLuar by animateFloatAsState(targetValue = maintLuarAngle, animationSpec = tween(600), label = "maintLuar")
    val animMaintOutlet by animateFloatAsState(targetValue = maintOutletAngle, animationSpec = tween(600), label = "maintOutlet")

    val readyPercentage = if (stats.total > 0) ((stats.ready.toFloat() / stats.total) * 100).toInt() else 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 0.dp else 6.dp,
                shape = RoundedCornerShape(CardCornerRadius),
                spotColor = Color(0x1A000000),
                ambientColor = Color(0x0F000000),
            )
            .clip(RoundedCornerShape(CardCornerRadius))
            .background(Surface)
            .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(CardCornerRadius))
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Gauge Ring & Center Metric
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center,
            ) {
                val trackColor = if (isDark) Surface2 else Color(0xFFF1F5F9)
                Canvas(modifier = Modifier.size(150.dp)) {
                    val strokeWidth = 16.dp.toPx()

                    // Background track
                    drawCircle(
                        color = trackColor,
                        style = Stroke(width = strokeWidth),
                    )

                    var currentStart = -90f

                    if (stats.ready > 0) {
                        drawArc(
                            color = Green,
                            startAngle = currentStart,
                            sweepAngle = animReady.coerceAtLeast(4f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        currentStart += animReady
                    }

                    if (stats.inUse > 0) {
                        drawArc(
                            color = Accent,
                            startAngle = currentStart,
                            sweepAngle = animInUse.coerceAtLeast(4f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        currentStart += animInUse
                    }

                    if (stats.maintLuar > 0) {
                        drawArc(
                            color = Warning,
                            startAngle = currentStart,
                            sweepAngle = animMaintLuar.coerceAtLeast(4f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                        currentStart += animMaintLuar
                    }

                    if (stats.maintOutlet > 0) {
                        drawArc(
                            color = Red,
                            startAngle = currentStart,
                            sweepAngle = animMaintOutlet.coerceAtLeast(4f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        )
                    }
                }

                // Center Metrics
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${stats.ready}",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-1).sp,
                    )
                    Text(
                        text = "READY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Green,
                        letterSpacing = 1.sp,
                    )
                    Text(
                        text = "$readyPercentage% Kesiapan",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Status Filter Chips (Tapping filters inline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusPill(
                    label = "Ready",
                    count = stats.ready,
                    color = Green,
                    isSelected = selectedFilter == "available",
                    onClick = {
                        onSelectFilter(if (selectedFilter == "available") null else "available")
                    },
                    modifier = Modifier.weight(1f),
                )

                StatusPill(
                    label = "Sewa",
                    count = stats.inUse,
                    color = Accent,
                    isSelected = selectedFilter == "in-use",
                    onClick = {
                        onSelectFilter(if (selectedFilter == "in-use") null else "in-use")
                    },
                    modifier = Modifier.weight(1f),
                )

                StatusPill(
                    label = "Luar",
                    count = stats.maintLuar,
                    color = Warning,
                    isSelected = selectedFilter == "maint-luar",
                    onClick = {
                        onSelectFilter(if (selectedFilter == "maint-luar") null else "maint-luar")
                    },
                    modifier = Modifier.weight(1f),
                )

                StatusPill(
                    label = "Outlet",
                    count = stats.maintOutlet,
                    color = Red,
                    isSelected = selectedFilter == "maint-outlet",
                    onClick = {
                        onSelectFilter(if (selectedFilter == "maint-outlet") null else "maint-outlet")
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalThemeIsDark.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) color.copy(alpha = if (isDark) 0.25f else 0.15f)
                else (if (isDark) Surface2 else Color(0xFFF8FAFC))
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) color else Border.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
            )
            .bounceClickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isSelected) color else TextPrimary,
                )
            }
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else TextMuted,
            )
        }
    }
}
