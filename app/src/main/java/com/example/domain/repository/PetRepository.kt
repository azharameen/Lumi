package com.example.domain.repository

import com.example.data.local.entity.PetEvolutionEntity
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.model.PetAccessory
import com.example.domain.model.BloubShape
import com.example.domain.model.BloubSkinColor
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    val petStatus: Flow<PetStatus>
    val petEvolution: Flow<PetEvolutionEntity?>
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
    
    suspend fun setPetEmotion(emotion: PetEmotion)
    suspend fun setSpeaking(isSpeaking: Boolean)
    suspend fun setListening(isListening: Boolean)
    suspend fun setThinking(isThinking: Boolean)

    suspend fun earnCoinsAndExp(coins: Int, exp: Int, reason: String = "")
    suspend fun earnGems(gems: Int, reason: String = "")
    suspend fun buyAccessory(accessory: PetAccessory): Boolean
    suspend fun equipAccessory(accessoryId: String)
    suspend fun updatePetName(name: String)
    suspend fun setBloubShape(shape: BloubShape)
    suspend fun setBloubSkinColor(skinColor: BloubSkinColor)
}
