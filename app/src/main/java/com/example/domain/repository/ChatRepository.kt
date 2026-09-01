package com.example.domain.repository

import com.example.data.local.entity.AiExecutionLogEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.remote.AiRoutingMode
import com.example.domain.agent.hitl.HitlPendingAction
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface ChatRepository {
    val chatMessages: Flow<List<ChatMessageEntity>>
    val pagedChatMessages: Flow<PagingData<ChatMessageEntity>>
    val aiExecutionLogs: Flow<List<AiExecutionLogEntity>>
    val aiRoutingMode: Flow<AiRoutingMode>
    val pendingHitlActions: Flow<List<HitlPendingAction>>

    suspend fun sendMessage(userText: String, image: ByteArray? = null): ChatMessageEntity
    fun setAiRoutingMode(mode: AiRoutingMode)
    suspend fun clearAiAnalyticsLogs()
    suspend fun benchmarkOnDeviceGemma(): Pair<String, Long>
    suspend fun resolveHitlAction(stateId: String, approved: Boolean): String?
}
