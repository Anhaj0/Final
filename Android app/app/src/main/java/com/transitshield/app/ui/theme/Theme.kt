package com.transitshield.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TransitDarkColorScheme = darkColorScheme(
    primary = BlueElectric,
    onPrimary = TextPrimary,
    primaryContainer = BlueDark,
    onPrimaryContainer = BlueLight,
    secondary = BlueLight,
    onSecondary = TextInverse,
    secondaryContainer = BgElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = PurpleInfo,
    onTertiary = TextPrimary,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle,
    error = RedError,
    onError = TextPrimary,
    scrim = Color(0xFF000000)
)

@Composable
fun TransitShieldTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TransitDarkColorScheme,
        typography = Typography,
        content = content
    )
}