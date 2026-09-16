package com.example.domain.model

import com.example.data.device.AudioHeadsetStatus

data class AudioPerceptionState(
    val headset: AudioHeadsetStatus = AudioHeadsetStatus(),
    val isBriefingSpeaking: Boolean = false,
    val isListening: Boolean = false
)
