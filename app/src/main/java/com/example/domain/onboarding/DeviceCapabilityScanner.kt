package com.example.domain.onboarding

data class DeviceProfile(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val freeStorageBytes: Long,
    val isLowRamDevice: Boolean,
    val apiLevel: Int,
    val isAiCoreAvailable: Boolean
)

interface DeviceCapabilityScanner {
    fun scanDevice(): DeviceProfile
}
