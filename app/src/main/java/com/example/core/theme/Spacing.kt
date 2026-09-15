package com.example.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val paddingDefault: Dp = 16.dp,
    val cornerRadius: Dp = 12.dp,

    // Corner Radius Tiers
    val cornerMicro: Dp = 6.dp,
    val cornerSmall: Dp = 10.dp,
    val cornerMedium: Dp = 14.dp,
    val cornerLarge: Dp = 20.dp,
    val cornerExtraLarge: Dp = 24.dp,

    // Icon Size Tiers
    val iconXS: Dp = 12.dp,
    val iconSM: Dp = 16.dp,
    val iconMD: Dp = 20.dp,
    val iconLG: Dp = 24.dp,
    val iconXL: Dp = 32.dp,
    val iconHero: Dp = 48.dp
)

val LocalSpacing = compositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current
