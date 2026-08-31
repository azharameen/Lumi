package com.example.domain.repository

import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import kotlinx.coroutines.flow.Flow

interface PetCompanionRepository {
    val petStatus: Flow<PetStatus>
    val currentEmotion: Flow<PetEmotion>
    val isSpeaking: Flow<Boolean>
    val isListening: Flow<Boolean>
    val isThinking: Flow<Boolean>
    val speechBubbleText: Flow<String?>
    val isOverlayActive: Flow<Boolean>

    fun setOverlayActive(active: Boolean)
    suspend fun setSpeechBubbleText(text: String?)
    suspend fun petTheAnimal()
    suspend fun feedPet(foodName: String)
    suspend fun playWithPet()
    suspend fun triggerSoundscape(soundType: String)
    suspend fun stopSoundscape()
}
