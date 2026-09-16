package com.example.domain.model

import com.example.data.device.BatteryStatus
import com.example.data.device.LocationContext
import com.example.data.device.NetworkStatus
import com.example.data.device.ZenModeStatus

data class EnvironmentState(
    val battery: BatteryStatus = BatteryStatus(),
    val network: NetworkStatus = NetworkStatus(),
    val zen: ZenModeStatus = ZenModeStatus(),
    val location: LocationContext = LocationContext()
)
