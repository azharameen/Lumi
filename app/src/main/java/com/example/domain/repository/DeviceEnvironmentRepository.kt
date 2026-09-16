package com.example.domain.repository

import com.example.domain.model.EnvironmentState
import kotlinx.coroutines.flow.StateFlow

interface DeviceEnvironmentRepository {
    val environmentState: StateFlow<EnvironmentState>
}
