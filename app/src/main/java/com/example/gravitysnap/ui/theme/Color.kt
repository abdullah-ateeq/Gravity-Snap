package com.example.gravitysnap.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Neon Energy Palette
val NeonCyan = Color(0xFF00F0FF)
val NeonAqua = Color(0xFF00D2FF)
val NeonBlue = Color(0xFF0077FE)
val CoreWhite = Color(0xFFFFFFFF)

// Accent Colors
val NeonViolet = Color(0xFF8B5CF6)
val DeepViolet = Color(0xFF6D28D9)
val NeonMagenta = Color(0xFFEC4899)
val NeonAmber = Color(0xFFFBBF24)

// Danger Palette
val HazardRed = Color(0xFFFF0055)
val HazardOrange = Color(0xFFFF6B00)
val HazardCore = Color(0xFFFF85A1)

// Dark Theme Surfaces
val DarkBg = Color(0xFF060913)
val DarkBgElevated = Color(0xFF0C1222)
val DarkCardBg = Color(0xFF111A30)
val DarkCardBorder = Color(0x3300F0FF)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkCorridorFloor = Color(0xFF0A0F1F)
val DarkGridLine = Color(0x1A00F0FF)

// Light Theme Surfaces
val LightBg = Color(0xFFF3F6FB)
val LightBgElevated = Color(0xFFE7ECF5)
val LightCardBg = Color(0xFFFFFFFF)
val LightCardBorder = Color(0x330099AA)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)
val LightCorridorFloor = Color(0xFFE2E8F0)
val LightGridLine = Color(0x14006688)

@Immutable
data class GravitySnapPalette(
    val isDark: Boolean,
    val background: Color,
    val backgroundElevated: Color,
    val cardBackground: Color,
    val cardBorder: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val primaryCyan: Color,
    val primaryAqua: Color,
    val accentViolet: Color,
    val accentMagenta: Color,
    val hazardRed: Color,
    val hazardOrange: Color,
    val corridorSurface: Color,
    val gridLine: Color,
    val floorGlow: Color,
    val ceilingGlow: Color
)

val DarkPalette = GravitySnapPalette(
    isDark = true,
    background = DarkBg,
    backgroundElevated = DarkBgElevated,
    cardBackground = DarkCardBg,
    cardBorder = DarkCardBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    primaryCyan = NeonCyan,
    primaryAqua = NeonAqua,
    accentViolet = NeonViolet,
    accentMagenta = NeonMagenta,
    hazardRed = HazardRed,
    hazardOrange = HazardOrange,
    corridorSurface = DarkCorridorFloor,
    gridLine = DarkGridLine,
    floorGlow = NeonCyan,
    ceilingGlow = NeonCyan
)

val LightPalette = GravitySnapPalette(
    isDark = false,
    background = LightBg,
    backgroundElevated = LightBgElevated,
    cardBackground = LightCardBg,
    cardBorder = LightCardBorder,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    primaryCyan = Color(0xFF0099AA),
    primaryAqua = Color(0xFF007A99),
    accentViolet = Color(0xFF7C3AED),
    accentMagenta = Color(0xFFDB2777),
    hazardRed = Color(0xFFE11D48),
    hazardOrange = Color(0xFFEA580C),
    corridorSurface = LightCorridorFloor,
    gridLine = LightGridLine,
    floorGlow = Color(0xFF0099AA),
    ceilingGlow = Color(0xFF0099AA)
)

val LocalGravitySnapColors = staticCompositionLocalOf { DarkPalette }