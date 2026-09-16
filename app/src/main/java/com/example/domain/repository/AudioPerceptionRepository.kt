package com.example.domain.repository

import com.example.domain.briefing.DailyBriefing
import com.example.domain.model.AudioPerceptionState
import kotlinx.coroutines.flow.StateFlow

interface AudioPerceptionRepository {
    val audioPerceptionState: StateFlow<AudioPerceptionState>
    fun startVoiceListening()
    fun stopVoiceListening()
    fun startAudioReactiveMode()
    fun stopAudioReactiveMode()
    fun playBriefingAudio(briefing: DailyBriefing)
    fun stopBriefingAudio()
}
