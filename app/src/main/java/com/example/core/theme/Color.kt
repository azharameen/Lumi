package com.example.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// LUMI BRAND ACCENTS & NEON GLOWS
// =========================================================================
val LumiCyan = Color(0xFF00F0FF)
val LumiCyanGlow = Color(0x3300F0FF)
val LumiCyanDark = Color(0xFF00B4D8)

val LumiViolet = Color(0xFF9D65FF)
val LumiVioletGlow = Color(0x339D65FF)
val LumiVioletDark = Color(0xFF7033E0)

val LumiPink = Color(0xFFFF70A6)
val LumiPinkGlow = Color(0x33FF70A6)
val LumiPinkDark = Color(0xFFE0407A)

val LumiGold = Color(0xFFFFD166)
val LumiGoldGlow = Color(0x33FFD166)
val LumiYellow = Color(0xFFFFD166)

val LumiMint = Color(0xFF06D6A0)
val LumiMintGlow = Color(0x3306D6A0)
val LumiGreen = Color(0xFF06D6A0)

val LumiCoral = Color(0xFFFF5964)
val LumiCoralGlow = Color(0x33FF5964)

// =========================================================================
// DARK LUXURY ETHEREAL CANVAS & GLASS SURFACES
// =========================================================================
val ObsidianDark = Color(0xFF0C0A17)
val SlateDark = Color(0xFF141124)
val SurfaceDark = Color(0xFF1D1833)
val SurfaceDarkVariant = Color(0xFF272145)
val SurfaceHighlight = Color(0xFF352D5C)
val SurfaceGlass = Color(0xE61A162E)
val SurfaceGlassLight = Color(0xCC251F42)

// =========================================================================
// TEXT & LABELS
// =========================================================================
val TextPrimary = Color(0xFFF6F5FF)
val TextSecondary = Color(0xFFA5A1C8)
val TextTertiary = Color(0xFF726D99)
val TextMuted = Color(0xFF4E4973)

// =========================================================================
// LIGHT THEME PALETTES (FALLBACK)
// =========================================================================
val SurfaceLight = Color(0xFFF7F5FC)
val SurfaceLightVariant = Color(0xFFECE7F7)
val TextPrimaryLight = Color(0xFF18132B)
val TextSecondaryLight = Color(0xFF5D5778)

// =========================================================================
// REUSABLE BRAND GRADIENTS & BORDER BRUSHES
// =========================================================================
val LumiPrimaryGradient = Brush.horizontalGradient(
    listOf(LumiCyan, LumiViolet)
)

val LumiCompanionAuraGradient = Brush.radialGradient(
    colors = listOf(LumiCyan.copy(alpha = 0.25f), Color.Transparent)
)

val LumiGlassCardBorder = Brush.linearGradient(
    listOf(
        SurfaceHighlight.copy(alpha = 0.8f),
        SurfaceDarkVariant.copy(alpha = 0.3f),
        SurfaceHighlight.copy(alpha = 0.6f)
    )
)

val LumiCyanBorderGlow = Brush.linearGradient(
    listOf(
        LumiCyan.copy(alpha = 0.8f),
        LumiViolet.copy(alpha = 0.4f),
        LumiCyan.copy(alpha = 0.6f)
    )
)

val LumiGoldBorderGlow = Brush.linearGradient(
    listOf(
        LumiGold.copy(alpha = 0.8f),
        LumiCoral.copy(alpha = 0.4f),
        LumiGold.copy(alpha = 0.6f)
    )
)

val LumiPinkBorderGlow = Brush.linearGradient(
    listOf(
        LumiPink.copy(alpha = 0.8f),
        LumiViolet.copy(alpha = 0.4f),
        LumiPink.copy(alpha = 0.6f)
    )
)

val LumiMintBorderGlow = Brush.linearGradient(
    listOf(
        LumiMint.copy(alpha = 0.8f),
        LumiCyan.copy(alpha = 0.4f),
        LumiMint.copy(alpha = 0.6f)
    )
)
