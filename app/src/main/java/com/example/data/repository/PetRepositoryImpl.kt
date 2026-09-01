package com.example.data.repository

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.PetEvolutionEntity
import com.example.domain.model.*
import com.example.domain.repository.PetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.min

class PetRepositoryImpl(
    private val database: LumiDatabase,
    private val scope: CoroutineScope
) : PetRepository {

    private val _currentEmotion = MutableStateFlow(PetEmotion.HAPPY)
    private val _isSpeaking = MutableStateFlow(false)
    private val _isListening = MutableStateFlow(false)
    private val _isThinking = MutableStateFlow(false)
    private val _speechBubbleText = MutableStateFlow<String?>("Hi! I'm Lumi, your personal AI friend ✨")
    private val _isOverlayActive = MutableStateFlow(false)

    override val currentEmotion: Flow<PetEmotion> = _currentEmotion.asStateFlow()
    override val isSpeaking: Flow<Boolean> = _isSpeaking.asStateFlow()
    override val isListening: Flow<Boolean> = _isListening.asStateFlow()
    override val isThinking: Flow<Boolean> = _isThinking.asStateFlow()
    override val speechBubbleText: Flow<String?> = _speechBubbleText.asStateFlow()
    override val isOverlayActive: Flow<Boolean> = _isOverlayActive.asStateFlow()

    override val petEvolution: Flow<PetEvolutionEntity?> = database.petEvolutionDao().getPetEvolution()

    override val petStatus: Flow<PetStatus> = combine(
        combine(petEvolution, _currentEmotion, _isSpeaking) { evo, emotion, speaking ->
            Triple(evo, emotion, speaking)
        },
        combine(_isListening, _isThinking, _speechBubbleText) { listening, thinking, bubbleText ->
            Triple(listening, thinking, bubbleText)
        }
    ) { (evo, emotion, speaking), (listening, thinking, bubbleText) ->
        val entity = evo ?: PetEvolutionEntity()
        val shape = try { BloubShape.valueOf(entity.bloubShape) } catch (e: Exception) { BloubShape.SPHERE }
        val skinColor = try { BloubSkinColor.valueOf(entity.bloubSkinColor) } catch (e: Exception) { BloubSkinColor.ELECTRIC_CYAN }

        PetStatus(
            name = entity.name,
            level = entity.level,
            exp = entity.exp,
            expToNextLevel = entity.expToNextLevel,
            coins = entity.coins,
            gems = entity.gems,
            streakDays = entity.streakDays,
            bondScore = entity.bondScore,
            happiness = entity.happiness,
            energy = entity.energy,
            personalityTrait = entity.personalityTrait,
            activeAccessory = entity.activeAccessory,
            currentEmotion = emotion,
            bloubShape = shape,
            bloubSkinColor = skinColor,
            unlockedAccessoriesCsv = entity.unlockedAccessoriesCsv,
            unlockedSkinsCsv = entity.unlockedSkinsCsv,
            unlockedShapesCsv = entity.unlockedShapesCsv,
            isSpeaking = speaking,
            isListening = listening,
            isThinking = thinking,
            speechBubbleText = bubbleText,
            daysTogether = entity.daysTogether,
            totalInteractions = entity.totalInteractions
        )
    }

    override fun setOverlayActive(active: Boolean) {
        _isOverlayActive.value = active
    }

    override suspend fun setSpeechBubbleText(text: String?) {
        _speechBubbleText.value = text
    }

    override suspend fun petTheAnimal() {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        val newHappiness = min(100, current.happiness + 8)
        val newBond = min(100, current.bondScore + 3)
        var newExp = current.exp + 10
        var newLevel = current.level
        var expNeeded = current.expToNextLevel
        var newCoins = current.coins + 5
        var newGems = current.gems

        if (newExp >= expNeeded) {
            newExp -= expNeeded
            newLevel += 1
            expNeeded = (expNeeded * 1.3).toInt()
            newCoins += 50
            newGems += 5
        }

        database.petEvolutionDao().insertOrUpdate(
            current.copy(
                happiness = newHappiness,
                bondScore = newBond,
                exp = newExp,
                level = newLevel,
                expToNextLevel = expNeeded,
                coins = newCoins,
                gems = newGems,
                totalInteractions = current.totalInteractions + 1
            )
        )

        _currentEmotion.value = PetEmotion.LOVING
        _speechBubbleText.value = "*Purrrrr* So warm!" // Simplified for now, will use UseCase for AI reaction
    }

    override suspend fun feedPet(foodName: String) {
        earnCoinsAndExp(5, 10, "Fed pet $foodName")
    }

    override suspend fun playWithPet() {
        earnCoinsAndExp(10, 20, "Played with pet")
    }

    override suspend fun setPetEmotion(emotion: PetEmotion) {
        _currentEmotion.value = emotion
    }

    override suspend fun setSpeaking(isSpeaking: Boolean) {
        _isSpeaking.value = isSpeaking
    }

    override suspend fun setListening(isListening: Boolean) {
        _isListening.value = isListening
    }

    override suspend fun setThinking(isThinking: Boolean) {
        _isThinking.value = isThinking
    }

    override suspend fun earnCoinsAndExp(coins: Int, exp: Int, reason: String) {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        var newExp = current.exp + exp
        var newLevel = current.level
        var expNeeded = current.expToNextLevel
        var newCoins = current.coins + coins
        var newGems = current.gems

        val leveledUp = newExp >= expNeeded
        while (newExp >= expNeeded) {
            newExp -= expNeeded
            newLevel += 1
            expNeeded = (expNeeded * 1.3).toInt()
            newCoins += 50
            newGems += 5
        }

        database.petEvolutionDao().updateProgression(
            exp = newExp,
            level = newLevel,
            expToNextLevel = expNeeded,
            coins = newCoins,
            gems = newGems
        )

        if (leveledUp) {
            _currentEmotion.value = PetEmotion.ENERGETIC
            _speechBubbleText.value = "LEVEL UP! We are now Level $newLevel! +50 Coins 🪙 +5 Gems 💎"
        } else if (coins > 0 || exp > 0) {
            _speechBubbleText.value = "+$coins 🪙 +$exp XP ${if (reason.isNotBlank()) "for $reason" else ""}"
        }
    }

    override suspend fun earnGems(gems: Int, reason: String) {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        val newGems = current.gems + gems
        database.petEvolutionDao().updateCurrencies(current.coins, newGems)
        _currentEmotion.value = PetEmotion.HAPPY
        _speechBubbleText.value = "+$gems Starlight Gems! 💎"
    }

    override suspend fun buyAccessory(accessory: PetAccessory): Boolean {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        if (current.coins < accessory.coinCost || current.gems < accessory.gemCost) {
            return false
        }
        val unlockedList = current.unlockedAccessoriesCsv.split(",").toMutableSet()
        unlockedList.add(accessory.id)
        val newCsv = unlockedList.joinToString(",")
        val newCoins = current.coins - accessory.coinCost
        val newGems = current.gems - accessory.gemCost

        database.petEvolutionDao().insertOrUpdate(
            current.copy(
                coins = newCoins,
                gems = newGems,
                unlockedAccessoriesCsv = newCsv,
                activeAccessory = accessory.id
            )
        )
        _currentEmotion.value = PetEmotion.PLAYFUL
        _speechBubbleText.value = "Equipped ${accessory.displayName}! ${accessory.iconEmoji} How stylish!"
        return true
    }

    override suspend fun equipAccessory(accessoryId: String) {
        database.petEvolutionDao().setActiveAccessory(accessoryId)
        _currentEmotion.value = PetEmotion.HAPPY
        _speechBubbleText.value = "Changed look! ✨ Looking sharp!"
    }

    override suspend fun updatePetName(name: String) {
        database.petEvolutionDao().updatePetName(name)
        _speechBubbleText.value = "My new name is $name! I love it!"
    }

    override suspend fun setBloubShape(shape: BloubShape) {
        database.petEvolutionDao().setBloubShape(shape.name)
        _currentEmotion.value = PetEmotion.HAPPY
        _speechBubbleText.value = "Transformed into ${shape.displayName}! ${shape.iconEmoji} How do I look?"
    }

    override suspend fun setBloubSkinColor(skinColor: BloubSkinColor) {
        database.petEvolutionDao().setBloubSkinColor(skinColor.name)
        _currentEmotion.value = PetEmotion.ENERGETIC
        _speechBubbleText.value = "Ooh, new ${skinColor.displayName} clay skin! ✨ Glowing!"
    }
}
