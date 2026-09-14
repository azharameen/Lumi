package com.example.domain.repository


import com.example.domain.model.ChatMessage
import com.example.domain.model.AiExecutionLog
import com.example.data.remote.AiRoutingMode
import com.example.domain.agent.hitl.HitlPendingAction
import kotlinx.coroutines.flow.Flow
import androidx.paging.PagingData

interface ChatRepository {
    val chatMessages: Flow<List<ChatMessage>>
    val pagedChatMessages: Flow<PagingData<ChatMessage>>
    val aiExecutionLogs: Flow<List<AiExecutionLog>>
    val aiRoutingMode: Flow<AiRoutingMode>
    val pendingHitlActions: Flow<List<HitlPendingAction>>
    val agentThoughts: Flow<String?>
    val streamingAiMessage: Flow<ChatMessage?>

    suspend fun sendMessage(userText: String, image: ByteArray? = null, modelId: String? = null): ChatMessage
    fun setAiRoutingMode(mode: AiRoutingMode)
    suspend fun clearAiAnalyticsLogs()
    suspend fun clearChatHistory()
    suspend fun deleteMessage(id: Long)
    suspend fun benchmarkOnDeviceGemma(): Pair<String, Long>
    suspend fun resolveHitlAction(stateId: String, approved: Boolean): String?
}
