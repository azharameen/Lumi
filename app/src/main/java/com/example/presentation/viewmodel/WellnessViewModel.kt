package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.repository.LumiRepository
import com.example.data.device.BiometricVaultManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import com.example.domain.repository.WellnessRepository
import com.example.domain.repository.PetMemoryRepository

class WellnessViewModel(
    val wellnessRepository: WellnessRepository,
    val memoryRepository: PetMemoryRepository,
    val biometricVault: BiometricVaultManager,
    private val analytics: LumiAnalyticsManager? = null
) : ViewModel() {

    val pagedWellnessLogs = wellnessRepository.pagedWellnessLogs.cachedIn(viewModelScope)

    val allWellnessLogs: StateFlow<List<WellnessLogEntity>> = wellnessRepository.allWellnessLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allMemories: StateFlow<List<PetMemoryEntity>> = memoryRepository.allMemories.stateIn(
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



