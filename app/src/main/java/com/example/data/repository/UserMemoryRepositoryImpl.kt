package com.example.data.repository

import com.example.data.local.dao.ChatMessageDao
import com.example.data.local.dao.FactKnowledgeDao
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toEntity
import com.example.domain.account.UserProfileData
import com.example.domain.account.UserProfileManager
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
    private val userProfileManager: UserProfileManager,
    private val factKnowledgeDao: FactKnowledgeDao,
    private val chatMessageDao: ChatMessageDao,
    private val briefingEngine: AutonomousBriefingEngine
) : UserMemoryRepository {

    override val userProfile: Flow<UserProfileData> = userProfileManager.profileFlow

    override val userFacts: Flow<List<UserFact>> = factKnowledgeDao.getAllFactsFlow()
        .map { list -> list.map { it.toDomain() } }

    override val chatMessages: Flow<List<ChatMessage>> = chatMessageDao.getRecentMessagesFlow(50)
        .map { list -> list.map { it.toDomain() } }

    override suspend fun updateUserProfile(profile: UserProfileData) = withContext(Dispatchers.IO) {
        userProfileManager.updateProfile(profile)
    }

    override suspend fun addUserFact(factKey: String, factValue: String, isPinned: Boolean) = withContext(Dispatchers.IO) {
        val fact = com.example.data.local.entity.FactKnowledgeEntity(
            factKey = factKey,
            factValue = factValue,
            isPinned = isPinned,
            createdAt = System.currentTimeMillis()
        )
        factKnowledgeDao.insertFact(fact)
    }

    override suspend fun removeUserFact(factId: String) = withContext(Dispatchers.IO) {
        factKnowledgeDao.deleteFactById(factId)
    }

    override suspend fun togglePinFact(factId: String) = withContext(Dispatchers.IO) {
        val fact = factKnowledgeDao.getFactById(factId)
        if (fact != null) {
            factKnowledgeDao.insertFact(fact.copy(isPinned = !fact.isPinned))
        }
    }

    override suspend fun saveChatMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatMessageDao.insertMessage(message.toEntity())
    }

    override suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        chatMessageDao.clearAllMessages()
    }

    override suspend fun generateMorningBriefing(): String = withContext(Dispatchers.IO) {
        val briefing = briefingEngine.generateBriefing(BriefingType.MORNING)
        briefing.summaryText
    }
}
