package com.evrenhouse.trackscooter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalThemeIsDark = compositionLocalOf { true }
val LocalThemeToggle = compositionLocalOf<() -> Unit> { {} }

private val DarkColors = darkColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = AccentSubtle,
    onPrimaryContainer = Accent,
    secondary = Green,
    onSecondary = Color.White,
    secondaryContainer = GreenSubtle,
    onSecondaryContainer = Green,
    tertiary = Warning,
    onTertiary = Color.White,
    tertiaryContainer = WarningSubtle,
    onTertiaryContainer = Warning,
    error = Red,
    onError = Color.White,
    errorContainer = RedSubtle,
    onErrorContainer = Red,
    background = DarkPalette.bg,
    onBackground = DarkPalette.textPrimary,
    surface = DarkPalette.surface,
    onSurface = DarkPalette.textPrimary,
    surfaceVariant = DarkPalette.surface2,
    onSurfaceVariant = DarkPalette.textMuted,
    outline = DarkPalette.border,
    outlineVariant = DarkPalette.border2,
)

private val LightColors = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    primaryContainer = AccentSubtle,
    onPrimaryContainer = Accent,
    secondary = Green,
    onSecondary = Color.White,
    secondaryContainer = GreenSubtle,
    onSecondaryContainer = Green,
    tertiary = Warning,
    onTertiary = Color.White,
    tertiaryContainer = WarningSubtle,
    onTertiaryContainer = Warning,
    error = Red,
    onError = Color.White,
    errorContainer = RedSubtle,
    onErrorContainer = Red,
    background = LightPalette.bg,
    onBackground = LightPalette.textPrimary,
    surface = LightPalette.surface,
    onSurface = LightPalette.textPrimary,
    surfaceVariant = LightPalette.surface2,
    onSurfaceVariant = LightPalette.textMuted,
    outline = LightPalette.border,
    outlineVariant = LightPalette.border2,
)

@Composable
fun TrackScooterTheme(
    isDark: Boolean = LocalThemeIsDark.current,
    content: @Composable () -> Unit,
) {
    val palette = if (isDark) DarkPalette else LightPalette
    val colorScheme = if (isDark) DarkColors else LightColors

    CompositionLocalProvider(
        LocalAppColors provides palette,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
