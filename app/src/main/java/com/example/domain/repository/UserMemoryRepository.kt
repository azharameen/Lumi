package com.example.domain.repository

import com.example.domain.account.UserProfileData
import com.example.domain.model.ChatMessage
import com.example.domain.model.UserFact
import kotlinx.coroutines.flow.Flow

interface UserMemoryRepository {
    val userProfile: Flow<UserProfileData>
    val userFacts: Flow<List<UserFact>>
    val chatMessages: Flow<List<ChatMessage>>

    suspend fun updateUserProfile(profile: UserProfileData)
    suspend fun addUserFact(factKey: String, factValue: String, isPinned: Boolean = false)
    suspend fun removeUserFact(factId: String)
    suspend fun togglePinFact(factId: String)
    suspend fun saveChatMessage(message: ChatMessage)
    suspend fun clearChatHistory()
    suspend fun generateMorningBriefing(): String
}
