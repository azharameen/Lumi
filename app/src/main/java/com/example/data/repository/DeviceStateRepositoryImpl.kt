package com.example.data.repository

import com.example.data.device.ProceduralSoundscapeEngine
import com.example.data.device.SoundscapeState
import com.example.data.device.SoundscapeType
import com.example.domain.repository.DeviceStateRepository
import kotlinx.coroutines.flow.StateFlow

class DeviceStateRepositoryImpl(
    private val soundscapeEngine: ProceduralSoundscapeEngine
) : DeviceStateRepository {
    override val soundscapeState: StateFlow<SoundscapeState> get() = soundscapeEngine.state
    override fun startSoundscape(type: SoundscapeType) = soundscapeEngine.startSoundscape(type)
    override fun stopSoundscape() = soundscapeEngine.stopSoundscape()
    override fun setSoundscapeVolume(volume: Float) = soundscapeEngine.setVolume(volume)
    override fun startFocusTimerWithSoundscape(minutes: Int) = soundscapeEngine.startFocusTimer(minutes)
    override fun stopFocusTimerWithSoundscape() = soundscapeEngine.stopFocusTimer()
}
