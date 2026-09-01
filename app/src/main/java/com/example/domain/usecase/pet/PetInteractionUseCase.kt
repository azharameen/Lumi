package com.example.domain.usecase.pet

import com.example.domain.model.PetEmotion
import com.example.domain.repository.PetRepository

class PetInteractionUseCase(
    private val petRepository: PetRepository
) {
    suspend fun petTheAnimal() {
        petRepository.petTheAnimal()
    }

    suspend fun feedPet(foodName: String) {
        petRepository.feedPet(foodName)
    }

    suspend fun playWithPet() {
        petRepository.playWithPet()
    }
}
