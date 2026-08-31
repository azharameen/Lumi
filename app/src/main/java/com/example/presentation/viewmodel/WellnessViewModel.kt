package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class WellnessViewModel(
    val repository: LumiRepository,
    val biometricVault: BiometricVaultManager
) : ViewModel() {

    val pagedWellnessLogs = repository.pagedWellnessLogs.cachedIn(viewModelScope)

    val allWellnessLogs: StateFlow<List<WellnessLogEntity>> = repository.allWellnessLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allMemories: StateFlow<List<PetMemoryEntity>> = repository.allMemories.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun logWellness(moodScore: Int, moodLabel: String, energyLevel: Int, hydrationCups: Int, gratitude: String) {
        viewModelScope.launch { repository.logWellness(moodScore, moodLabel, energyLevel, hydrationCups, gratitude) }
    }

    fun incrementHydration(logId: Long) {
        viewModelScope.launch { repository.incrementHydration(logId) }
    }
}



