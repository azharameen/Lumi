package com.example.domain.usecase.pet

import com.example.domain.model.PetEmotion
import com.example.domain.repository.PetRepository
import com.example.domain.service.AnalyticsService
import com.example.domain.service.DeviceSensorsService

class PetInteractionUseCase(
    private val petRepository: PetRepository,
    private val sensorsService: DeviceSensorsService,
    private val analyticsService: AnalyticsService?
) {
    suspend fun petTheAnimal() {
        petRepository.petTheAnimal()
        sensorsService.vibratePurr()
        analyticsService?.logPetInteraction("pet", "HAPPY", 100)
    }

    suspend fun feedPet(foodName: String) {
        petRepository.feedPet(foodName)
        sensorsService.vibrateCelebration()
        analyticsService?.logPetInteraction("feed", "HAPPY", 100)
    }

    suspend fun playWithPet() {
        petRepository.playWithPet()
        sensorsService.vibrateTap()
        analyticsService?.logPetInteraction("play", "PLAYFUL", 100)
    }

    suspend fun handleShakeGesture() {
        sensorsService.vibratePurr()
        petRepository.setPetEmotion(PetEmotion.PLAYFUL)
        analyticsService?.logPetInteraction("shake_play", "PLAYFUL", 100)
    }
}
