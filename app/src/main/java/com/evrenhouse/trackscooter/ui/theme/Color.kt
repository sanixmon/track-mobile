package com.evrenhouse.trackscooter.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── TrackScooter Dynamic Palette Structure (mirrors web light & dark modes) ──
data class AppColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val surface3: Color,
    val border: Color,
    val border2: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val isDark: Boolean,
)

val DarkPalette = AppColors(
    bg = Color(0xFF000000),
    surface = Color(0xFF0A0A0A),
    surface2 = Color(0xFF121212),
    surface3 = Color(0xFF1A1A1A),
    border = Color(0xFF262626),
    border2 = Color(0xFF333333),
    textPrimary = Color(0xFFEDEDED),
    textMuted = Color(0xFFA1A1AA),
    textSubtle = Color(0xFF71717A),
    isDark = true,
)

val LightPalette = AppColors(
    bg = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF1F5F9),
    surface3 = Color(0xFFE2E8F0),
    border = Color(0xFFCBD5E1),
    border2 = Color(0xFF94A3B8),
    textPrimary = Color(0xFF0F172A),
    textMuted = Color(0xFF334155),
    textSubtle = Color(0xFF475569),
    isDark = false,
)

val LocalAppColors = staticCompositionLocalOf { LightPalette }

val Bg: Color
    @Composable
    get() = LocalAppColors.current.bg

val Surface: Color
    @Composable
    get() = LocalAppColors.current.surface

val Surface2: Color
    @Composable
    get() = LocalAppColors.current.surface2

val Surface3: Color
    @Composable
    get() = LocalAppColors.current.surface3

val Border: Color
    @Composable
    get() = LocalAppColors.current.border

val Border2: Color
    @Composable
    get() = LocalAppColors.current.border2

val TextPrimary: Color
    @Composable
    get() = LocalAppColors.current.textPrimary

val TextMuted: Color
    @Composable
    get() = LocalAppColors.current.textMuted

val TextSubtle: Color
    @Composable
    get() = LocalAppColors.current.textSubtle

// Static branding / status accents
val Accent = Color(0xFF4F6EF7)
val AccentHover = Color(0xFF3D5CE0)
val AccentSubtle = Color(0x264F6EF7) // rgba(79,110,247,0.15)

val Green = Color(0xFF22C55E)
val GreenSubtle = Color(0x2622C55E)

val Red = Color(0xFFEF4444)
val RedSubtle = Color(0x26EF4444)

val Warning = Color(0xFFF59E0B)
val WarningSubtle = Color(0x26F59E0B)
val Yellow = Warning

val BlueLive = Color(0xFF3B82F6)
