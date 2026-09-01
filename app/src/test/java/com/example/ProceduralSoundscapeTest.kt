package com.example

import com.example.data.device.SoundscapeState
import com.example.data.device.SoundscapeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProceduralSoundscapeTest {

    @Test
    fun testSoundscapeTypesHaveValidMetadata() {
        SoundscapeType.entries.forEach { type ->
            assertTrue("Title should not be blank for $type", type.title.isNotBlank())
            assertTrue("Description should not be blank for $type", type.description.isNotBlank())
            assertTrue("Icon emoji should not be blank for $type", type.iconEmoji.isNotBlank())
        }
    }

    @Test
    fun testSoundscapeDefaultState() {
        val state = SoundscapeState()
        assertFalse(state.isPlaying)
        assertEquals(SoundscapeType.BINAURAL_FOCUS, state.activeType)
        assertEquals(0.65f, state.volume, 0.001f)
        assertEquals(1500, state.remainingSeconds)
        assertEquals(1500, state.totalSeconds)
        assertFalse(state.isTimerActive)
    }

    @Test
    fun testSoundscapeStateTransitions() {
        val initial = SoundscapeState()
        val playing = initial.copy(isPlaying = true, activeType = SoundscapeType.RAIN_ON_LEAVES)
        
        assertTrue(playing.isPlaying)
        assertEquals(SoundscapeType.RAIN_ON_LEAVES, playing.activeType)
        
        val timerStarted = playing.copy(isTimerActive = true, remainingSeconds = 1200, totalSeconds = 1200)
        assertTrue(timerStarted.isTimerActive)
        assertEquals(1200, timerStarted.remainingSeconds)
    }
}
