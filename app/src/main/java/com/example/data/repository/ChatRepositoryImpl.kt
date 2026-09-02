package com.example.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.remote.AiRoutingMode
import com.example.data.remote.HybridAiEngine
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.model.PetEmotion
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class ChatRepositoryImpl(
    private val database: LumiDatabase,
    private val hybridAiEngine: HybridAiEngine,
    private val petRepository: PetRepository
) : ChatRepository {

    private val _agentThoughts = MutableStateFlow<String?>(null)
    override val agentThoughts: Flow<String?> = _agentThoughts.asStateFlow()

    override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getAllMessages()

    override val pagedChatMessages: Flow<PagingData<ChatMessageEntity>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }
    ).flow

    override val aiExecutionLogs: Flow<List<AiExecutionLogEntity>> = database.aiExecutionLogDao().getAllLogs()
    
    override val aiRoutingMode: Flow<AiRoutingMode> = hybridAiEngine.routingMode

    override val pendingHitlActions: Flow<List<HitlPendingAction>> = hybridAiEngine.hitlApprovalManager.pendingActions

    override suspend fun sendMessage(userText: String, image: ByteArray?): ChatMessageEntity = withContext(Dispatchers.IO) {
        // Save user message
        val userEntity = ChatMessageEntity(
            sender = "USER",
            content = userText,
            imageBase64OrUri = if (image != null) "IMAGE_ATTACHED" else null
        )
        database.chatMessageDao().insertMessage(userEntity)

        petRepository.setThinking(true)
        petRepository.setPetEmotion(PetEmotion.THINKING)
        petRepository.setSpeechBubbleText("Thinking...")

        val recentEntities = database.chatMessageDao().getRecentMessagesDirect()
        val historyTurns = recentEntities.reversed().map { it.sender to it.content }

        val agentResult = try {
            hybridAiEngine.executeUserTurn(
                userMessage = userText,
                recentHistory = historyTurns,
                imageAttachment = image,
                onThought = { thought -> _agentThoughts.value = thought }
            )
        } catch (e: Exception) {
            petRepository.setThinking(false)
            throw e
        } finally {
            _agentThoughts.value = null
        }

        petRepository.setThinking(false)
        petRepository.setPetEmotion(agentResult.inferredEmotion)
        petRepository.setSpeechBubbleText(agentResult.responseText)

        val toolName = agentResult.toolReports.firstOrNull()?.toolName
        val toolResult = agentResult.toolReports.firstOrNull()?.description

        val aiEntity = ChatMessageEntity(
            sender = "LUMI",
            content = agentResult.responseText,
            petEmotion = agentResult.inferredEmotion.name,
            toolUsedName = toolName,
            toolResultJson = toolResult
        )
        database.chatMessageDao().insertMessage(aiEntity)

        aiEntity
    }

    override fun setAiRoutingMode(mode: AiRoutingMode) {
        hybridAiEngine.setRoutingMode(mode)
    }

    override suspend fun clearAiAnalyticsLogs() {
        database.aiExecutionLogDao().clearAllLogs()
    }

    override suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        database.chatMessageDao().clearHistory()
    }

    override suspend fun deleteMessage(id: Long) = withContext(Dispatchers.IO) {
        database.chatMessageDao().deleteMessageById(id)
    }

    override suspend fun benchmarkOnDeviceGemma(): Pair<String, Long> {
        val result = hybridAiEngine.onDeviceGemmaEngine.benchmarkOnDeviceGemma()
        // Log benchmark invocation
        val now = System.currentTimeMillis()
        val log = AiExecutionLogEntity(
            taskCategory = "On-Device Benchmark",
            engineType = "ON_DEVICE_GEMMA",
            modelName = "gemma-2b-it-int4",
            promptPreview = "[Benchmark] Warmup & token throughput test",
            responsePreview = result.first,
            promptTokens = 16,
            completionTokens = 32,
            totalTokens = 48,
            estimatedCostUsd = 0.0,
            startTimeMillis = now - result.second,
            finishTimeMillis = now,
            durationMs = result.second,
            isSuccess = true,
            isOffline = true,
            hardwareTarget = "GPU OpenCL / NPU"
        )
        database.aiExecutionLogDao().insertLog(log)
        return result
    }

    override suspend fun resolveHitlAction(stateId: String, approved: Boolean): String? {
        val resultState = hybridAiEngine.hitlApprovalManager.resolveAction(stateId, approved)
        if (resultState != null) {
            val responseText = resultState.finalResponseText ?: resultState.executedToolReports.lastOrNull()?.description ?: "Action completed."
            petRepository.setPetEmotion(resultState.inferredEmotion)
            petRepository.setSpeechBubbleText(responseText)

            val toolName = resultState.executedToolReports.firstOrNull()?.toolName
            val toolDesc = resultState.executedToolReports.firstOrNull()?.description

            val aiEntity = ChatMessageEntity(
                sender = "LUMI",
                content = responseText,
                petEmotion = resultState.inferredEmotion.name,
                toolUsedName = toolName,
                toolResultJson = toolDesc
            )
            database.chatMessageDao().insertMessage(aiEntity)
            return responseText
        }
        return null
    }
}
