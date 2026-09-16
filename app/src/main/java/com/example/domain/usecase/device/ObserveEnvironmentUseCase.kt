package com.example.domain.usecase.device

import com.example.domain.model.EnvironmentState
import com.example.domain.repository.DeviceEnvironmentRepository
import kotlinx.coroutines.flow.StateFlow

class ObserveEnvironmentUseCase(
    private val repository: DeviceEnvironmentRepository
) {
    operator fun invoke(): StateFlow<EnvironmentState> = repository.environmentState
}
