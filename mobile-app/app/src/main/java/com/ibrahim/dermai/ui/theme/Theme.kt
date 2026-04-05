package com.ibrahim.dermai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * DermAI özel açık tema renk şeması.
 * Teal/cyan sağlık teması.
 */
private val LightColorScheme = lightColorScheme(
    primary = DermPrimary,
    onPrimary = DermOnPrimary,
    primaryContainer = DermPrimaryContainer,
    onPrimaryContainer = DermOnPrimaryContainer,

    secondary = DermSecondary,
    onSecondary = DermOnSecondary,
    secondaryContainer = DermSecondaryContainer,
    onSecondaryContainer = DermOnSecondaryContainer,

    tertiary = DermTertiary,
    onTertiary = DermOnTertiary,
    tertiaryContainer = DermTertiaryContainer,
    onTertiaryContainer = DermOnTertiaryContainer,

    background = DermBackground,
    onBackground = DermOnBackground,
    surface = DermSurface,
    onSurface = DermOnSurface,
    surfaceVariant = DermSurfaceVariant,
    onSurfaceVariant = DermOnSurfaceVariant,

    error = DermError,
    onError = DermOnError,
    errorContainer = DermErrorContainer,
    onErrorContainer = DermOnErrorContainer,

    outline = DermOutline,
    outlineVariant = DermOutlineVariant
)

/**
 * DermAI özel koyu tema renk şeması.
 */
private val DarkColorScheme = darkColorScheme(
    primary = DermPrimaryDarkMode,
    onPrimary = DermOnPrimaryDarkMode,
    primaryContainer = DermPrimaryContainerDark,
    onPrimaryContainer = DermOnPrimaryContainerDark,

    secondary = DermSecondaryDarkMode,
    onSecondary = DermOnSecondaryDarkMode,
    secondaryContainer = DermSecondaryContainerDark,
    onSecondaryContainer = DermOnSecondaryContainerDark,

    tertiary = DermTertiaryDarkMode,
    onTertiary = DermOnTertiaryDarkMode,
    tertiaryContainer = DermTertiaryContainerDark,
    onTertiaryContainer = DermOnTertiaryContainerDark,

    background = DermBackgroundDark,
    onBackground = DermOnBackgroundDark,
    surface = DermSurfaceDark,
    onSurface = DermOnSurfaceDark,
    surfaceVariant = DermSurfaceVariantDark,
    onSurfaceVariant = DermOnSurfaceVariantDark,

    error = DermErrorDark,
    onError = DermOnErrorDark,
    errorContainer = DermErrorContainerDark,
    onErrorContainer = DermOnErrorContainerDark,

    outline = DermOutlineDark,
    outlineVariant = DermOutlineVariantDark
)

/**
 * DermAI ana tema composable fonksiyonu.
 * Dynamic color kullanılmıyor – tutarlı marka kimliği sağlanır.
 */
@Composable
fun DermAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Status bar rengini tema ile uyumlu yap
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}