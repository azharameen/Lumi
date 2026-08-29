package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiGold
import com.example.ui.theme.LumiViolet

/**
 * Modern 3-tab architecture for Lumi Agent Pet.
 * 0: Companion (3D Pet, Wardrobe, Evolution, Telemetry, Overlay)
 * 1: Assistant (Gemini LLM Brain, Multimodal Vision, Voice, Tools)
 * 2: Life Hub (Schedule & Agenda, Tasks, Wellness, Biometric Memory Vault)
 */
sealed class NavDestination(
    val tabIndex: Int,
    val title: String,
    val icon: ImageVector,
    val accentColor: Color,
    val testTag: String
) {
    object PetCompanion : NavDestination(0, "Companion", Icons.Default.AutoAwesome, LumiCyan, "nav_companion")
    object Assistant : NavDestination(1, "Assistant", Icons.Default.Psychology, LumiViolet, "nav_assistant")
    object LifeHub : NavDestination(2, "Life Hub", Icons.Default.Dashboard, LumiGold, "nav_life_hub")

    companion object {
        val allDestinations: List<NavDestination> = listOf(
            PetCompanion, Assistant, LifeHub
        )
    }
}

