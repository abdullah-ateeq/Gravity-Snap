package com.example.gravitysnap.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.gravitysnap.data.ThemeMode

private val AppDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = DarkBg,
    primaryContainer = DarkCardBg,
    onPrimaryContainer = NeonCyan,
    secondary = NeonViolet,
    onSecondary = CoreWhite,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkBgElevated,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCardBg,
    onSurfaceVariant = DarkTextSecondary,
    error = HazardRed,
    onError = CoreWhite
)

private val AppLightColorScheme = lightColorScheme(
    primary = LightPalette.primaryCyan,
    onPrimary = CoreWhite,
    primaryContainer = LightCardBg,
    onPrimaryContainer = LightPalette.primaryCyan,
    secondary = LightPalette.accentViolet,
    onSecondary = CoreWhite,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightBgElevated,
    onSurface = LightTextPrimary,
    surfaceVariant = LightCardBg,
    onSurfaceVariant = LightTextSecondary,
    error = HazardRed,
    onError = CoreWhite
)

object GravitySnapTheme {
    val colors: GravitySnapPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalGravitySnapColors.current
}

@Composable
fun GravitySnapTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val palette = if (isDark) DarkPalette else LightPalette
    val colorScheme = if (isDark) AppDarkColorScheme else AppLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = palette.background.toArgb()
            window.navigationBarColor = palette.background.toArgb()
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalGravitySnapColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}