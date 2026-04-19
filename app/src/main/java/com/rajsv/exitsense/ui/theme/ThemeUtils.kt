package com.rajsv.exitsense.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.rajsv.exitsense.data.model.SettingsDataStore

data class AppColors(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundTertiary: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val divider: Color,
    val accentPrimary: Color,
    val accentSecondary: Color,
    val glassBackground: Color,
    val glassBorder: Color
)

@Composable
fun rememberAppColors(): AppColors {
    val colorScheme = MaterialTheme.colorScheme
    val customColors = ExitSenseTheme.customColors

    return AppColors(
        backgroundPrimary = colorScheme.background,
        backgroundSecondary = colorScheme.surface,
        backgroundTertiary = colorScheme.surfaceVariant,
        cardBackground = colorScheme.primaryContainer,
        cardBorder = customColors.cardBorder,
        textPrimary = colorScheme.onBackground,
        textSecondary = colorScheme.onSurfaceVariant,
        textTertiary = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        divider = colorScheme.outline,
        accentPrimary = colorScheme.primary,
        accentSecondary = colorScheme.secondary,
        glassBackground = customColors.glassBackground,
        glassBorder = customColors.glassBorder
    )
}

@Composable
fun isDarkTheme(): Boolean {
    val context = LocalContext.current
    val isDarkMode by SettingsDataStore.getDarkMode(context).collectAsState(initial = true)
    return isDarkMode
}
