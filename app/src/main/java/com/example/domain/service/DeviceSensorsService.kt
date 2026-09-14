package com.example.domain.service
import kotlinx.coroutines.flow.StateFlow

interface DeviceSensorsService {
    val ambientLux: StateFlow<Float>
    fun startListening(onShake: (() -> Unit)?)
    fun stopListening()
    fun vibratePurr()
    fun vibrateTap()
    fun vibrateCelebration()
}
