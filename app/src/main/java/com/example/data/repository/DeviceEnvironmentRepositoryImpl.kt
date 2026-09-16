package com.example.data.repository

import com.example.data.device.BatteryStatusManager
import com.example.data.device.ContextLocationEngine
import com.example.data.device.NetworkConnectivityEngine
import com.example.data.device.ZenModeManager
import com.example.domain.model.EnvironmentState
import com.example.domain.repository.DeviceEnvironmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DeviceEnvironmentRepositoryImpl(
    batteryManager: BatteryStatusManager,
    networkEngine: NetworkConnectivityEngine,
    zenManager: ZenModeManager,
    locationEngine: ContextLocationEngine,
    scope: CoroutineScope
) : DeviceEnvironmentRepository {

    override val environmentState: StateFlow<EnvironmentState> = combine(
        batteryManager.batteryStatus,
        networkEngine.networkStatus,
        zenManager.zenStatus,
        locationEngine.locationState
    ) { battery, network, zen, location ->
        EnvironmentState(
            battery = battery,
            network = network,
            zen = zen,
            location = location
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = EnvironmentState()
    )
}
