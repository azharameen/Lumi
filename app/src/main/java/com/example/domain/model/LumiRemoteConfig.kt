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
    val aiCreativityTemperature: Float = 0.75f,
    val proactiveNudgeIntervalHours: Int = 3,
    val enableVoiceAmbientMode: Boolean = true,
    val specialEventBannerText: String = "",
    val defaultCloudModelName: String = "gemini-2.5-flash",
    val enablePetAccessories: Boolean = true,
    val enableLocalGemmaFallback: Boolean = true,
    val maxAgentExecutionSteps: Int = 10,
    val hitlDefaultSecurityMode: String = "SMART_RISK",
    val vectorEmbeddingSimilarityThreshold: Float = 0.55f,
    val voiceSpeechRate: Float = 1.02f,
    val voicePitch: Float = 1.15f,
    val enableOnboardingModelDownload: Boolean = true,
    val enableGeminiNanoAiCore: Boolean = true,
    val enableMcpClient: Boolean = false,
    val enableGoogleWorkspace: Boolean = false,
    val enableSlackIntegration: Boolean = false,
    val enableGithubIntegration: Boolean = true,
    val enablePurchaseButtons: Boolean = false
)
