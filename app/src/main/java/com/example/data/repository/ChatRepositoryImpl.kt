package com.example.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.remote.AiRoutingMode
import com.example.data.remote.HybridAiEngine
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.model.PetEmotion
import com.example.domain.model.ChatMessage
import com.example.domain.model.AiExecutionLog
import com.example.data.local.mapper.toDomain
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ChatRepositoryImpl(
    private val database: LumiDatabase,
    private val hybridAiEngine: HybridAiEngine,
    private val petRepository: PetRepository
) : ChatRepository {

    private val _agentThoughts = MutableStateFlow<String?>(null)
    override val agentThoughts: Flow<String?> = _agentThoughts.asStateFlow()

    private val _agentStreamEvents = kotlinx.coroutines.flow.MutableSharedFlow<com.example.domain.agent.AgentStreamEvent>(replay = 10)
    override val agentStreamEvents: kotlinx.coroutines.flow.SharedFlow<com.example.domain.agent.AgentStreamEvent> = _agentStreamEvents.asSharedFlow()

    private val _streamingAiMessage = MutableStateFlow<ChatMessage?>(null)
    override val streamingAiMessage: Flow<ChatMessage?> = _streamingAiMessage.asStateFlow()

    override val chatMessages: Flow<List<ChatMessage>> = database.chatMessageDao().getAllMessages().map { list -> list.map { it.toDomain() } }

    override val pagedChatMessages: Flow<PagingData<ChatMessage>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }
    ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

    override val aiExecutionLogs: Flow<List<AiExecutionLog>> = database.aiExecutionLogDao().getAllLogs().map { list -> list.map { it.toDomain() } }
    
    override val aiRoutingMode: Flow<AiRoutingMode> = hybridAiEngine.routingMode

    override val pendingHitlActions: Flow<List<HitlPendingAction>> = hybridAiEngine.hitlApprovalManager.pendingActions

    override suspend fun sendMessage(userText: String, image: ByteArray?, modelId: String?): ChatMessage = withContext(Dispatchers.IO) {
        // Fetch prior history BEFORE inserting current user message to avoid duplicate turns
        val previousEntities = database.chatMessageDao().getRecentMessagesDirect()
        val historyTurns = previousEntities.reversed().map { it.sender to it.content }

        val userEntity = ChatMessageEntity(
            sender = "USER",
            content = userText,
            imageBase64OrUri = if (image != null) "IMAGE_ATTACHED" else null
        )
        database.chatMessageDao().insertMessage(userEntity)

        petRepository.setThinking(true)
        petRepository.setSpeaking(false)
        petRepository.setPetEmotion(PetEmotion.THINKING)
        petRepository.setSpeechBubbleText("Thinking...")

        // Stage initial thinking streaming message placeholder
        _streamingAiMessage.value = ChatMessage(timestamp = System.currentTimeMillis(), 
            id = -999L,
            sender = "LUMI",
            content = "",
            petEmotion = PetEmotion.THINKING.name
        )

        val agentResult = try {
            hybridAiEngine.executeUserTurn(
                userMessage = userText,
                recentHistory = historyTurns,
                imageAttachment = image,
                selectedModelId = modelId,
                onThought = { thought -> 
                    _agentThoughts.value = thought 
                    if (thought != null) {
                        _agentStreamEvents.tryEmit(com.example.domain.agent.AgentStreamEvent.ThoughtToken(thought))
                    }
                },
                onStreamToken = { tokenChunk ->
                    _streamingAiMessage.value = ChatMessage(timestamp = System.currentTimeMillis(), 
                        id = -999L,
                        sender = "LUMI",
                        content = tokenChunk,
                        petEmotion = PetEmotion.HAPPY.name
                    )
                    _agentStreamEvents.tryEmit(com.example.domain.agent.AgentStreamEvent.ResponseChunk(tokenChunk))
                    if (tokenChunk.isNotBlank()) {
                        petRepository.setThinking(false)
                        petRepository.setSpeaking(true)
                    }
                },
                onAgentStreamEvent = { event ->
                    _agentStreamEvents.emit(event)
                    when (event) {
                        is com.example.domain.agent.AgentStreamEvent.ToolExecuting -> {
                            petRepository.setThinking(true)
                            petRepository.setSpeechBubbleText("Using ${event.toolName}...")
                        }
                        is com.example.domain.agent.AgentStreamEvent.ToolCompleted -> {
                            petRepository.setSpeechBubbleText("✓ ${event.toolName}")
                        }
                        is com.example.domain.agent.AgentStreamEvent.StatusChanged -> {
                            if (event.status == com.example.domain.agent.AgentStatus.RUNNING) {
                                petRepository.setThinking(true)
                            }
                        }
                        else -> {}
                    }
                }
            )
        } catch (e: Exception) {
            petRepository.setThinking(false)
            petRepository.setSpeaking(false)
            petRepository.setPetEmotion(PetEmotion.CONCERNED)
            _streamingAiMessage.value = null
            throw e
        } finally {
            _agentThoughts.value = null
        }

        petRepository.setThinking(false)
        petRepository.setSpeaking(false)
        petRepository.setPetEmotion(agentResult.inferredEmotion)
        petRepository.setSpeechBubbleText(agentResult.responseText)

        val toolName = agentResult.toolReports.firstOrNull()?.toolName
        val rawToolResult = agentResult.toolReports.firstOrNull()?.description
        val finalThought = agentResult.finalThought
        
        val bundledResult = org.json.JSONObject().apply {
            if (rawToolResult != null) put("tool_result", rawToolResult)
            if (finalThought != null) put("agent_thought", finalThought)
        }.toString().takeIf { it != "{}" } ?: rawToolResult

        val engineTag = if (agentResult.usedEngine.contains("GEMMA")) "ON_DEVICE_GEMMA" else null
        val finalToolName = when {
            engineTag != null && toolName != null -> "$engineTag:$toolName"
            engineTag != null -> engineTag
            else -> toolName
        }

        val aiEntity = ChatMessageEntity(
            sender = "LUMI",
            content = agentResult.responseText,
            petEmotion = agentResult.inferredEmotion.name,
            toolUsedName = finalToolName,
            toolResultJson = bundledResult
        )
        database.chatMessageDao().insertMessage(aiEntity)
        _streamingAiMessage.value = null

        aiEntity.toDomain()
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
