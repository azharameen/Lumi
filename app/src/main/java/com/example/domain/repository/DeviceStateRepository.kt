package com.example.domain.repository

import com.example.data.device.SoundscapeState
import com.example.data.device.SoundscapeType
import kotlinx.coroutines.flow.StateFlow

interface DeviceStateRepository {
    val soundscapeState: StateFlow<SoundscapeState>
    fun startSoundscape(type: SoundscapeType)
    fun stopSoundscape()
    fun setSoundscapeVolume(volume: Float)
    fun startFocusTimerWithSoundscape(minutes: Int)
    fun stopFocusTimerWithSoundscape()
}
