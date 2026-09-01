package com.example

import com.example.domain.model.LumiRemoteConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LumiRemoteConfigTest {

    @Test
    fun `test default remote config values`() {
        val defaultConfig = LumiRemoteConfig()

        assertEquals("Ready for a mindful, productive day with Lumi? ✨", defaultConfig.welcomeGreeting)
        assertEquals(0.7f, defaultConfig.aiCreativityTemperature)
        assertEquals(3, defaultConfig.proactiveNudgeIntervalHours)
        assertFalse(defaultConfig.seasonalThemeEnabled)
        assertEquals("Obsidian Neon", defaultConfig.seasonalThemeName)
        assertTrue(defaultConfig.enableVoiceAmbientMode)
        assertEquals("", defaultConfig.specialEventBannerText)
    }

    @Test
    fun `test custom remote config copy`() {
        val customConfig = LumiRemoteConfig(
            welcomeGreeting = "Happy Lunar New Year! 🎆",
            seasonalThemeEnabled = true,
            seasonalThemeName = "Celestial Gold",
            aiCreativityTemperature = 0.85f,
            proactiveNudgeIntervalHours = 4,
            specialEventBannerText = "🏮 Lunar Celebration Event Active!"
        )

        assertEquals("Happy Lunar New Year! 🎆", customConfig.welcomeGreeting)
        assertTrue(customConfig.seasonalThemeEnabled)
        assertEquals("Celestial Gold", customConfig.seasonalThemeName)
        assertEquals(0.85f, customConfig.aiCreativityTemperature)
        assertEquals(4, customConfig.proactiveNudgeIntervalHours)
        assertEquals("🏮 Lunar Celebration Event Active!", customConfig.specialEventBannerText)
    }
}
