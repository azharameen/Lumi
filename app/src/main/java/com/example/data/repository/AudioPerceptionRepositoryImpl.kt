package com.example.data.repository

import com.example.data.device.AudioHeadsetManager
import com.example.data.device.RealtimeAudioReactiveEngine
import com.example.data.device.VoiceEngine
import com.example.domain.briefing.DailyBriefing
import com.example.domain.model.AudioPerceptionState
import com.example.domain.repository.AudioPerceptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class AudioPerceptionRepositoryImpl(
    private val voiceEngine: VoiceEngine,
    private val audioReactiveEngine: RealtimeAudioReactiveEngine,
    headsetManager: AudioHeadsetManager,
    scope: CoroutineScope
) : AudioPerceptionRepository {

    override val audioPerceptionState: StateFlow<AudioPerceptionState> = combine(
        headsetManager.headsetStatus,
        voiceEngine.isListening
    ) { headset, isListening ->
        AudioPerceptionState(
            headset = headset,
            isListening = isListening
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AudioPerceptionState()
    )

    override fun startVoiceListening() {
        voiceEngine.startListening { _ -> }
    }

    override fun stopVoiceListening() {
        voiceEngine.stopListening()
    }

    override fun startAudioReactiveMode() {
        audioReactiveEngine.startListening()
    }

    override fun stopAudioReactiveMode() {
        audioReactiveEngine.stopListening()
    }

    override fun playBriefingAudio(briefing: DailyBriefing) {
        voiceEngine.speak(briefing.audioScript)
    }

    override fun stopBriefingAudio() {
        voiceEngine.stopSpeaking()
    }
}
