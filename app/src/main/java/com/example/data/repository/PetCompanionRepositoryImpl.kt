package com.example.data.repository

import com.example.data.device.ProceduralSoundscapeEngine
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.LumiRepository
import com.example.domain.repository.PetCompanionRepository
import kotlinx.coroutines.flow.Flow

class PetCompanionRepositoryImpl(
    private val lumiRepository: LumiRepository,
    private val soundscapeEngine: ProceduralSoundscapeEngine? = null
) : PetCompanionRepository {

    override val petStatus: Flow<PetStatus> = lumiRepository.petStatus
    override val currentEmotion: Flow<PetEmotion> = lumiRepository.currentEmotion
    override val isSpeaking: Flow<Boolean> = lumiRepository.isSpeaking
    override val isListening: Flow<Boolean> = lumiRepository.isListening
    override val isThinking: Flow<Boolean> = lumiRepository.isThinking
    override val speechBubbleText: Flow<String?> = lumiRepository.speechBubbleText
    override val isOverlayActive: Flow<Boolean> = lumiRepository.isOverlayActive

    override fun setOverlayActive(active: Boolean) {
        lumiRepository.setOverlayActive(active)
    }

    override suspend fun setSpeechBubbleText(text: String?) {
        lumiRepository.setSpeechBubbleText(text)
    }

    override suspend fun petTheAnimal() {
        lumiRepository.petTheAnimal()
    }

    override suspend fun feedPet(foodName: String) {
        lumiRepository.feedPet(foodName)
    }

    override suspend fun playWithPet() {
        lumiRepository.playWithPet()
    }

    override suspend fun triggerSoundscape(soundType: String) {
        soundscapeEngine?.triggerSoundscape(soundType)
    }

    override suspend fun stopSoundscape() {
        soundscapeEngine?.stopSoundscape()
    }
}
