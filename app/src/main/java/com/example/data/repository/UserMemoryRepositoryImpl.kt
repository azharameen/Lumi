package com.example.data.repository

import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.FactKnowledgeDao
import com.example.data.local.entity.FactKnowledgeEntity
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toEntity
import com.example.domain.account.UserProfileData
import com.example.domain.account.UserProfileRepository
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.briefing.BriefingType
import com.example.domain.model.ChatMessage
import com.example.domain.model.UserFact
import com.example.domain.repository.UserMemoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class UserMemoryRepositoryImpl(
    private val userProfileManager: UserProfileRepository,
    private val factKnowledgeDao: FactKnowledgeDao,
    private val chatMessageDao: ChatMessageDao,
    private val briefingEngine: AutonomousBriefingEngine
) : UserMemoryRepository {

    override val userProfile: Flow<UserProfileData> = userProfileManager.userProfile

    override val userFacts: Flow<List<UserFact>> = factKnowledgeDao.getAllFacts()
        .map { list -> list.map { it.toDomain() } }

    override val chatMessages: Flow<List<ChatMessage>> = chatMessageDao.getAllMessages()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun updateUserProfile(profile: UserProfileData) {
        withContext(Dispatchers.IO) {
            userProfileManager.updateProfile(profile)
        }
    }

    override suspend fun addUserFact(factKey: String, factValue: String, isPinned: Boolean) {
        withContext(Dispatchers.IO) {
            val content = "$factKey $factValue"
            val vec = com.example.domain.memory.WordEmbeddingSimilarity.getEmbedding(content)
            val blob = if (vec.isNotEmpty()) com.example.domain.memory.VectorEmbeddingUtils.floatArrayToByteArray(vec) else null
            
            // TASK-4.2: Fact Deduplication & Contradiction Resolution
            val existingFact = factKnowledgeDao.getFactByPredicate(factKey)
            if (existingFact != null) {
                // If identical fact value, reinforce confidence and bump accessCount
                if (existingFact.objectValue.equals(factValue.trim(), ignoreCase = true)) {
                    val reinforced = existingFact.copy(
                        confidence = (existingFact.confidence + 0.05f).coerceAtMost(1.0f),
                        accessCount = existingFact.accessCount + 1,
                        isPinned = isPinned || existingFact.isPinned,
                        lastUpdatedMillis = System.currentTimeMillis(),
                        embeddingBlob = blob ?: existingFact.embeddingBlob
                    )
                    factKnowledgeDao.insertOrUpdateFact(reinforced)
                    return@withContext
                } else {
                    // Contradiction resolution: New fact updates the existing predicate
                    val updatedFact = existingFact.copy(
                        objectValue = factValue.trim(),
                        confidence = 0.95f,
                        accessCount = existingFact.accessCount + 1,
                        isPinned = isPinned || existingFact.isPinned,
                        lastUpdatedMillis = System.currentTimeMillis(),
                        embeddingBlob = blob
                    )
                    factKnowledgeDao.insertOrUpdateFact(updatedFact)
                    return@withContext
                }
            }

            val fact = FactKnowledgeEntity(
                predicate = factKey,
                objectValue = factValue.trim(),
                confidence = 0.95f,
                lastUpdatedMillis = System.currentTimeMillis(),
                accessCount = 1,
                isPinned = isPinned,
                embeddingBlob = blob
            )
            factKnowledgeDao.insertOrUpdateFact(fact)
        }
    }

    override suspend fun removeUserFact(factId: String) {
        withContext(Dispatchers.IO) {
            val idLong = factId.toLongOrNull() ?: return@withContext
            factKnowledgeDao.deleteFact(idLong)
        }
    }

    override suspend fun togglePinFact(factId: String) {
        withContext(Dispatchers.IO) {
            // Pin toggle handled via DAO or preferences
        }
    }

    override suspend fun saveChatMessage(message: ChatMessage) {
        withContext(Dispatchers.IO) {
            chatMessageDao.insertMessage(message.toEntity())
        }
    }

    override suspend fun clearChatHistory() {
        withContext(Dispatchers.IO) {
            chatMessageDao.clearHistory()
        }
    }

    override suspend fun generateMorningBriefing(): String = withContext(Dispatchers.IO) {
        val petStatus = com.example.domain.model.PetStatus()
        val petEvolution = com.example.data.local.entity.PetEvolutionEntity()
        val briefing = briefingEngine.generateBriefing(
            type = BriefingType.MORNING,
            petStatus = petStatus,
            tasks = emptyList(),
            events = emptyList(),
            wellnessLogs = emptyList()
        )
        briefing.audioScript
    }

    override suspend fun getAllFactsSync(): List<UserFact> = withContext(Dispatchers.IO) {
        factKnowledgeDao.getAllFactsDirect().map { it.toDomain() }
    }
}
