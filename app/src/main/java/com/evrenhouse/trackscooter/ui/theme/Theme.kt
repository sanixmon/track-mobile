package com.evrenhouse.trackscooter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    background = Bg,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    outline = Border,
    outlineVariant = Border2,
)

@Composable
fun TrackScooterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content,
    )
}
