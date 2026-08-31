package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.model.BloubShape
import com.example.domain.model.BloubSkinColor
import com.example.domain.repository.LumiRepository
import com.example.data.device.SensorsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(
    val repository: LumiRepository,
    val sensorsManager: SensorsManager
) : ViewModel() {
                val petStatus = repository.petStatus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetStatus())
    val petEvolution = repository.petEvolution.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        sensorsManager.startListening(
            onShake = {
                viewModelScope.launch {
                    sensorsManager.vibratePurr()
                    repository.setPetEmotion(PetEmotion.PLAYFUL)
                }
            }
        )
    }

    fun onPetTouched() {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.setPetEmotion(PetEmotion.HAPPY)
        }
    }

    fun onPetPetted() {
        viewModelScope.launch {
            sensorsManager.vibratePurr()
            repository.setPetEmotion(PetEmotion.HAPPY)
        }
    }

    fun setBloubShape(shape: BloubShape) { viewModelScope.launch { repository.setBloubShape(shape) } }
    fun setBloubSkinColor(skinColor: BloubSkinColor) { viewModelScope.launch { repository.setBloubSkinColor(skinColor) } }
    fun buyAccessory(accessory: com.example.domain.model.PetAccessory, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.buyAccessory(accessory)
            if (success) {
                sensorsManager.vibratePurr()
            }
            onResult(success)
        }
    }
    fun equipAccessory(accessoryId: String) {
        viewModelScope.launch {
            repository.equipAccessory(accessoryId)
            sensorsManager.vibrateTap()
        }
    }
    fun updatePetName(name: String) { viewModelScope.launch { repository.updatePetName(name) } }
    fun feedPet() {
        viewModelScope.launch {
            repository.setPetEmotion(PetEmotion.ENERGETIC)
            repository.earnCoinsAndExp(coins = 10, exp = 15, reason = "Feeding Lumi")
            sensorsManager.vibratePurr()
        }
    }
    fun dancePet() {
        viewModelScope.launch {
            repository.setPetEmotion(PetEmotion.PLAYFUL)
            repository.earnCoinsAndExp(coins = 10, exp = 15, reason = "Dancing with Lumi")
            sensorsManager.vibrateTap()
        }
    }
    fun pokePet() { viewModelScope.launch { repository.setPetEmotion(PetEmotion.CONCERNED) } }

    fun togglePetSleep() {
        viewModelScope.launch {
            if (petStatus.value.currentEmotion == PetEmotion.SLEEPY) {
                repository.setPetEmotion(PetEmotion.HAPPY)
            } else {
                repository.setPetEmotion(PetEmotion.SLEEPY)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorsManager.stopListening()
    }
}


