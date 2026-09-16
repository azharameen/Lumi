package com.example.domain.usecase.device

import com.example.domain.briefing.DailyBriefing
import com.example.domain.model.AudioPerceptionState
import com.example.domain.repository.AudioPerceptionRepository
import kotlinx.coroutines.flow.StateFlow

class ManageAudioPerceptionUseCase(
    private val repository: AudioPerceptionRepository
) {
    val state: StateFlow<AudioPerceptionState> = repository.audioPerceptionState

    fun startVoiceListening() = repository.startVoiceListening()
    fun stopVoiceListening() = repository.stopVoiceListening()
    fun startAudioReactiveMode() = repository.startAudioReactiveMode()
    fun stopAudioReactiveMode() = repository.stopAudioReactiveMode()
    fun playBriefingAudio(briefing: DailyBriefing) = repository.playBriefingAudio(briefing)
    fun stopBriefingAudio() = repository.stopBriefingAudio()
}
