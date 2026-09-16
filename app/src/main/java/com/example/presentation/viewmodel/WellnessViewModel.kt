package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.domain.model.PetMemory
import com.example.domain.model.WellnessLog
import com.example.domain.repository.PetMemoryRepository
import com.example.domain.repository.WellnessRepository
import com.example.domain.service.AnalyticsService
import com.example.domain.usecase.device.HandleUserInteractionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WellnessViewModel(
    val wellnessRepository: WellnessRepository,
    val memoryRepository: PetMemoryRepository,
    val handleUserInteractionUseCase: HandleUserInteractionUseCase,
    private val analytics: AnalyticsService? = null
) : ViewModel() {

    val pagedWellnessLogs = wellnessRepository.pagedWellnessLogs.cachedIn(viewModelScope)

    val allWellnessLogs: StateFlow<List<WellnessLog>> = wellnessRepository.allWellnessLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allMemories: StateFlow<List<PetMemory>> = memoryRepository.allMemories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun logWellness(moodScore: Int, moodLabel: String, energyLevel: Int, hydrationCups: Int, gratitude: String) {
        viewModelScope.launch {
            analytics?.logWellnessSession(exerciseType = "mood_checkin_$moodLabel", durationSeconds = 60)
            wellnessRepository.logWellness(moodScore, moodLabel, energyLevel, hydrationCups, gratitude)
        }
    }

    fun incrementHydration(logId: Long) {
        viewModelScope.launch {
            analytics?.logWellnessSession(exerciseType = "hydration_increment", durationSeconds = 5)
            wellnessRepository.incrementHydration(logId)
        }
    }
}
