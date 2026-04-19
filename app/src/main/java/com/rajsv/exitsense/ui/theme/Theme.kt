package com.rajsv.exitsense.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Custom colors that aren't part of Material 3's default colorScheme
data class CustomColors(
    val glassBackground: Color,
    val glassBorder: Color,
    val cardBackgroundElevated: Color,
    val cardBorder: Color,
    val textMuted: Color,
    val overlay: Color,
    val statusWarning: Color,
    val statusSuccess: Color,
    val onGradient: Color,
    val shadow: Color
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomColors(
        glassBackground = Color.Unspecified,
        glassBorder = Color.Unspecified,
        cardBackgroundElevated = Color.Unspecified,
        cardBorder = Color.Unspecified,
        textMuted = Color.Unspecified,
        overlay = Color.Unspecified,
        statusWarning = Color.Unspecified,
        statusSuccess = Color.Unspecified,
        onGradient = Color.Unspecified,
        shadow = Color.Unspecified
    )
}

private val DarkCustomColors = CustomColors(
    glassBackground = GlassBackgroundDark,
    glassBorder = GlassBorderDark,
    cardBackgroundElevated = CardBackgroundElevated,
    cardBorder = CardBorder,
    textMuted = TextMuted,
    overlay = OverlayDark,
    statusWarning = StatusWarning,
    statusSuccess = StatusSuccess,
    onGradient = Color.White,
    shadow = Color.Black.copy(alpha = 0.4f)
)

private val LightCustomColors = CustomColors(
    glassBackground = GlassBackgroundLight,
    glassBorder = GlassBorderLight,
    cardBackgroundElevated = LightThemeColors.CardBackgroundElevated,
    cardBorder = LightThemeColors.CardBorder,
    textMuted = LightThemeColors.TextMuted,
    overlay = OverlayLight,
    statusWarning = Color(0xFFD97706), // Darker amber for accessibility in light mode
    statusSuccess = Color(0xFF15803D),  // Darker green for accessibility in light mode
    onGradient = Color.White,
    shadow = Color.Black.copy(alpha = 0.15f)
)

private val ExitSenseDarkColorScheme = darkColorScheme(
    primary = AccentPrimary,
    onPrimary = TextPrimary,
    primaryContainer = CardBackground,
    onPrimaryContainer = TextPrimary,
    secondary = AccentSecondary,
    onSecondary = TextPrimary,
    secondaryContainer = CardBackgroundElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentTertiary,
    onTertiary = TextPrimary,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = BackgroundSecondary,
    onSurface = TextPrimary,
    surfaceVariant = BackgroundTertiary,
    onSurfaceVariant = TextSecondary,
    outline = DividerDark,
    error = StatusError,
    onError = TextPrimary
)

private val ExitSenseLightColorScheme = lightColorScheme(
    primary = AccentPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3ECFF),
    onPrimaryContainer = LightThemeColors.TextPrimary,
    secondary = AccentSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD5F5EE),
    onSecondaryContainer = LightThemeColors.TextPrimary,
    tertiary = AccentTertiary,
    onTertiary = Color.White,
    background = LightThemeColors.BackgroundPrimary,
    onBackground = LightThemeColors.TextPrimary,
    surface = LightThemeColors.BackgroundSecondary,
    onSurface = LightThemeColors.TextPrimary,
    surfaceVariant = LightThemeColors.BackgroundTertiary,
    onSurfaceVariant = LightThemeColors.TextSecondary,
    outline = DividerLight,
    error = Color(0xFFB3261E), // More accessible red for light mode
    onError = Color.White
)

object ExitSenseTheme {
    val customColors: CustomColors
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColors.current
}

@Composable
fun ExitSenseTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ExitSenseDarkColorScheme else ExitSenseLightColorScheme
    val customColors = if (darkTheme) DarkCustomColors else LightCustomColors
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val statusBarColor = colorScheme.background
            window.statusBarColor = statusBarColor.toArgb()
            window.navigationBarColor = statusBarColor.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
