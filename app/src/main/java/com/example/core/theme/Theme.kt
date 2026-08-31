package com.example.core.theme
import androidx.compose.ui.graphics.Color

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = LumiCyan,
    onPrimary = ObsidianDark,
    primaryContainer = SurfaceDarkVariant,
    onPrimaryContainer = LumiCyan,
    secondary = LumiViolet,
    onSecondary = ObsidianDark,
    secondaryContainer = SurfaceHighlight,
    onSecondaryContainer = LumiViolet,
    tertiary = LumiPink,
    onTertiary = ObsidianDark,
    background = ObsidianDark,
    onBackground = TextPrimary,
    surface = SlateDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceHighlight
)

private val LightColorScheme = lightColorScheme(
    primary = LumiViolet,
    onPrimary = TextPrimary,
    primaryContainer = SurfaceLightVariant,
    onPrimaryContainer = LumiViolet,
    secondary = LumiCyan,
    onSecondary = TextPrimaryLight,
    tertiary = LumiPink,
    background = SurfaceLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = SurfaceLightVariant,
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek futuristic dark companion mode
    petColorPrimary: Color? = null,
    petColorSecondary: Color? = null,
    content: @Composable () -> Unit
) {
    val dynamicDark = if (petColorPrimary != null && petColorSecondary != null) {
        DarkColorScheme.copy(
            primary = petColorPrimary,
            onPrimaryContainer = petColorPrimary,
            secondary = petColorSecondary,
            onSecondaryContainer = petColorSecondary,
            tertiary = petColorPrimary
        )
    } else DarkColorScheme

    val colorScheme = if (darkTheme) dynamicDark else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity
            activity?.window?.let { window ->
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
