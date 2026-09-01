package com.example.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
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
import com.example.domain.repository.PetRepository

class PetViewModel(
    val petRepository: PetRepository,
    val sensorsManager: SensorsManager,
    private val analytics: LumiAnalyticsManager? = null,
    private val crashlytics: LumiCrashlyticsManager? = null
) : ViewModel() {
    val petStatus = petRepository.petStatus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetStatus())
    val petEvolution = petRepository.petEvolution.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        sensorsManager.startListening(
            onShake = {
                viewModelScope.launch {
                    sensorsManager.vibratePurr()
                    petRepository.setPetEmotion(PetEmotion.PLAYFUL)
                    analytics?.logPetInteraction("shake_play", "PLAYFUL", petStatus.value.happiness)
                    crashlytics?.log("Pet emotion changed to PLAYFUL via shake gesture")
                }
            }
        )
    }

    fun onPetTouched() {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            petRepository.setPetEmotion(PetEmotion.HAPPY)
            analytics?.logPetInteraction("touch", "HAPPY", petStatus.value.happiness)
        }
    }

    fun onPetPetted() {
        viewModelScope.launch {
            sensorsManager.vibratePurr()
            petRepository.setPetEmotion(PetEmotion.HAPPY)
            analytics?.logPetInteraction("pet", "HAPPY", petStatus.value.happiness)
        }
    }

    fun setBloubShape(shape: BloubShape) { viewModelScope.launch { petRepository.setBloubShape(shape) } }
    fun setBloubSkinColor(skinColor: BloubSkinColor) { viewModelScope.launch { petRepository.setBloubSkinColor(skinColor) } }
    fun buyAccessory(accessory: com.example.domain.model.PetAccessory, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = petRepository.buyAccessory(accessory)
            if (success) {
                sensorsManager.vibratePurr()
                analytics?.logPetInteraction("buy_accessory", petStatus.value.currentEmotion.name, petStatus.value.happiness)
            }
            onResult(success)
        }
    }
    fun equipAccessory(accessoryId: String) {
        viewModelScope.launch {
            petRepository.equipAccessory(accessoryId)
            sensorsManager.vibrateTap()
            analytics?.logPetInteraction("equip_accessory", petStatus.value.currentEmotion.name, petStatus.value.happiness)
        }
    }
    fun updatePetName(name: String) { viewModelScope.launch { petRepository.updatePetName(name) } }
    fun feedPet() {
        viewModelScope.launch {
            petRepository.setPetEmotion(PetEmotion.ENERGETIC)
            petRepository.earnCoinsAndExp(coins = 10, exp = 15, reason = "Feeding Lumi")
            sensorsManager.vibratePurr()
            analytics?.logPetInteraction("feed", "ENERGETIC", petStatus.value.happiness)
        }
    }
    fun dancePet() {
        viewModelScope.launch {
            petRepository.setPetEmotion(PetEmotion.PLAYFUL)
            petRepository.earnCoinsAndExp(coins = 10, exp = 15, reason = "Dancing with Lumi")
            sensorsManager.vibrateTap()
            analytics?.logPetInteraction("dance", "PLAYFUL", petStatus.value.happiness)
        }
    }
    fun pokePet() { viewModelScope.launch { petRepository.setPetEmotion(PetEmotion.CONCERNED) } }

    fun togglePetSleep() {
        viewModelScope.launch {
            if (petStatus.value.currentEmotion == PetEmotion.SLEEPY) {
                petRepository.setPetEmotion(PetEmotion.HAPPY)
                analytics?.logPetInteraction("wake_up", "HAPPY", petStatus.value.happiness)
            } else {
                petRepository.setPetEmotion(PetEmotion.SLEEPY)
                analytics?.logPetInteraction("sleep", "SLEEPY", petStatus.value.happiness)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorsManager.stopListening()
    }
}



