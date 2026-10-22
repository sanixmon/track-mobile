package com.evrenhouse.trackscooter.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── TrackScooter Modern Electric Indigo & Tonal Superapp Palette ──
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

// Dark Theme: Sleek Charcoal Slate (gentle on eyes, high contrast)
val DarkPalette = AppColors(
    bg = Color(0xFF0B0F17),
    surface = Color(0xFF141C2B),
    surface2 = Color(0xFF1E293B),
    surface3 = Color(0xFF283548),
    border = Color(0xFF232F42),
    border2 = Color(0xFF334155),
    textPrimary = Color(0xFFF8FAFC),
    textMuted = Color(0xFF94A3B8),
    textSubtle = Color(0xFF64748B),
    isDark = true,
)

// Light Theme: Clean Minimalist Mobility Slate
val LightPalette = AppColors(
    bg = Color(0xFFF6F8FB),
    surface = Color(0xFFFFFFFF),
    surface2 = Color(0xFFF1F5F9),
    surface3 = Color(0xFFE2E8F0),
    border = Color(0xFFE2E8F0),
    border2 = Color(0xFFCBD5E1),
    textPrimary = Color(0xFF0F172A),
    textMuted = Color(0xFF475569),
    textSubtle = Color(0xFF94A3B8),
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

// ── Static Branding & Status Accents (Modern Electric Indigo / High-Legibility) ──
val Accent = Color(0xFF2563EB)         // Electric Indigo / Royal Blue
val AccentHover = Color(0xFF1D4ED8)
val AccentSubtle = Color(0x1F2563EB)   // 12% opacity soft container tint
val AccentLight = Color(0xFF3B82F6)

val Green = Color(0xFF10B981)          // Emerald Green
val GreenSubtle = Color(0x1F10B981)

val Red = Color(0xFFEF4444)            // Rose Red
val RedSubtle = Color(0x1FEF4444)

val Warning = Color(0xFFF59E0B)        // Amber Warning
val WarningSubtle = Color(0x1FF59E0B)
val Yellow = Warning

val BlueLive = Color(0xFF3B82F6)
val Purple = Color(0xFF8B5CF6)
val PurpleSubtle = Color(0x1F8B5CF6)
