package com.example.data.repository

import com.example.data.device.ProceduralSoundscapeEngine
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.PetRepository
import com.example.domain.repository.PetCompanionRepository
import kotlinx.coroutines.flow.Flow

class PetCompanionRepositoryImpl(
    private val petRepository: PetRepository,
    private val soundscapeEngine: ProceduralSoundscapeEngine? = null
) : PetCompanionRepository {

    override val petStatus: Flow<PetStatus> = petRepository.petStatus
    override val currentEmotion: Flow<PetEmotion> = petRepository.currentEmotion
    override val isSpeaking: Flow<Boolean> = petRepository.isSpeaking
    override val isListening: Flow<Boolean> = petRepository.isListening
    override val isThinking: Flow<Boolean> = petRepository.isThinking
    override val speechBubbleText: Flow<String?> = petRepository.speechBubbleText
    override val isOverlayActive: Flow<Boolean> = petRepository.isOverlayActive

    override fun setOverlayActive(active: Boolean) {
        petRepository.setOverlayActive(active)
    }

    override suspend fun setSpeechBubbleText(text: String?) {
        petRepository.setSpeechBubbleText(text)
    }

    override suspend fun petTheAnimal() {
        petRepository.petTheAnimal()
    }

    override suspend fun feedPet(foodName: String) {
        petRepository.feedPet(foodName)
    }

    override suspend fun playWithPet() {
        petRepository.playWithPet()
    }

    override suspend fun triggerSoundscape(soundType: String) {
        val typeEnum = try { com.example.data.device.SoundscapeType.valueOf(soundType) } catch (e: Exception) { com.example.data.device.SoundscapeType.BINAURAL_FOCUS }
        soundscapeEngine?.startSoundscape(typeEnum)
    }

    override suspend fun stopSoundscape() {
        soundscapeEngine?.stopSoundscape()
    }
}
