package com.example

import com.example.domain.model.BloubShape
import com.example.domain.model.BloubSkinColor
import com.example.domain.model.PetAccessory
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PetCompanionLogicTest {

    @Test
    fun `test default pet status initialization`() {
        val status = PetStatus()

        assertEquals("Lumi", status.name)
        assertEquals(1, status.level)
        assertEquals(0, status.exp)
        assertEquals(100, status.expToNextLevel)
        assertEquals(150, status.coins)
        assertEquals(20, status.gems)
        assertEquals(PetEmotion.HAPPY, status.currentEmotion)
        assertEquals(BloubShape.SPHERE, status.bloubShape)
        assertEquals(BloubSkinColor.ELECTRIC_CYAN, status.bloubSkinColor)
        assertEquals("NONE", status.activeAccessory)
    }

    @Test
    fun `test pet accessories catalog pricing and descriptions`() {
        val crown = PetAccessory.GOLD_CROWN
        assertEquals("Starlight Crown", crown.displayName)
        assertEquals(250, crown.coinCost)
        assertEquals(10, crown.gemCost)

        val headphones = PetAccessory.HEADPHONES
        assertEquals(150, headphones.coinCost)
        assertEquals(5, headphones.gemCost)

        val sprout = PetAccessory.SPROUT
        assertEquals(0, sprout.coinCost)
        assertEquals(0, sprout.gemCost)
    }

    @Test
    fun `test pet emotions glow and display values`() {
        for (emotion in PetEmotion.entries) {
            assertNotNull(emotion.displayName)
            assertTrue(emotion.displayName.isNotBlank())
            assertTrue(emotion.glowHex.startsWith("#"))
        }
    }

    @Test
    fun `test pet status copy modification and unlocked catalog checks`() {
        val initial = PetStatus()
        val evolved = initial.copy(
            level = 5,
            exp = 40,
            expToNextLevel = 500,
            coins = 600,
            gems = 45,
            currentEmotion = PetEmotion.ENERGETIC,
            activeAccessory = PetAccessory.GOLD_CROWN.id,
            bloubSkinColor = BloubSkinColor.SUNSHINE_GOLD
        )

        assertEquals(5, evolved.level)
        assertEquals(PetEmotion.ENERGETIC, evolved.currentEmotion)
        assertEquals("CROWN", evolved.activeAccessory)
        assertEquals(BloubSkinColor.SUNSHINE_GOLD, evolved.bloubSkinColor)
        assertTrue(evolved.unlockedAccessoriesCsv.contains("SPROUT"))
        assertFalse(evolved.isSpeaking)
    }
}
