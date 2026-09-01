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
            val fact = FactKnowledgeEntity(
                predicate = factKey,
                objectValue = factValue,
                lastUpdatedMillis = System.currentTimeMillis()
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
            petEvolution = petEvolution,
            tasks = emptyList(),
            events = emptyList(),
            wellnessLogs = emptyList()
        )
        briefing.audioScript
    }
}
