package com.example.domain.model

/**
 * Domain model representing dynamic personalization and AI parameters
 * fetched via Firebase Remote Config.
 */
data class LumiRemoteConfig(
    val welcomeGreeting: String = "Ready for a mindful, productive day with Lumi? ✨",
    val companionTipOfTheDay: String = "Taking a 2-minute conscious breath reset revitalizes neural focus and balance.",
    val seasonalThemeEnabled: Boolean = false,
    val seasonalThemeName: String = "Obsidian Neon",
    val aiCreativityTemperature: Float = 0.7f,
    val proactiveNudgeIntervalHours: Int = 3,
    val enableVoiceAmbientMode: Boolean = true,
    val specialEventBannerText: String = ""
)
