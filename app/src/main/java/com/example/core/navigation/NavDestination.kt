package com.example.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiViolet

/**
 * Modern 5-tab architecture for Lumi Agent Pet & Personal Operating System:
 * 0: Companion (3D Pet, Wardrobe, Evolution, Telemetry, Overlay)
 * 1: Assistant (Gemini LLM Brain, Multimodal Vision, Voice, Tools)
 * 2: Life Hub (Productivity, Schedule, Tasks, Goal Swarms, Focus Sound)
 * 3: Wellness (Vitality Check-In, Hydration, Breathing, Biometric Vault)
 * 4: Account (User Info & Persona, Connectors, LLM Settings, Privacy & Security)
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
    object LifeHub : NavDestination(2, "Productivity", Icons.Default.Dashboard, LumiGold, "nav_life_hub")
    object Wellness : NavDestination(3, "Wellness", Icons.Default.SelfImprovement, LumiPink, "nav_wellness")
    object Account : NavDestination(4, "Account", Icons.Default.AccountCircle, LumiMint, "nav_account")

    companion object {
        val allDestinations: List<NavDestination> = listOf(
            PetCompanion, Assistant, LifeHub, Wellness, Account
        )
    }
}

