package com.evrenhouse.trackscooter.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Soft & Rounded Superapp Shape Tokens ──
val CardCornerRadius = 24.dp
val DialogCornerRadius = 24.dp
val BottomSheetCornerRadius = 28.dp
val FloatingBarCornerRadius = 28.dp
val ButtonCornerRadius = 16.dp
val InputCornerRadius = 16.dp
val ChipCornerRadius = 12.dp
val PillCornerRadius = 999.dp

val SuperappShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(ChipCornerRadius),
    medium = RoundedCornerShape(ButtonCornerRadius),
    large = RoundedCornerShape(CardCornerRadius),
    extraLarge = RoundedCornerShape(BottomSheetCornerRadius),
)
