package com.example.data.remote


import com.example.domain.repository.AgentStateRepository
import com.example.domain.memory.SemanticMemoryEngine
import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStatus
import com.example.domain.agent.LumiAgentGraph
import com.example.domain.agent.hitl.HitlApprovalManager
import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport
import com.example.domain.tools.AgentToolDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AgentExecutionResult(
    val responseText: String,
    val inferredEmotion: PetEmotion,
    val toolReports: List<ToolExecutionReport> = emptyList(),
    val finalThought: String? = null
)

/**
 * Enterprise Agent Engine executing state machine graphs (LumiAgentGraph).
 * Uses Firebase AI Logic as the zero-key Cloud LLM provider with Play Integrity transport security.
 */
class GeminiAgentEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val agentStateRepository: AgentStateRepository,
    private val semanticMemoryEngine: SemanticMemoryEngine,
    private val hitlApprovalManager: HitlApprovalManager? = null,
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null,
    private val toolRetriever: com.example.domain.tools.ToolRetriever? = null
) {
    private val firebaseAiEngine = FirebaseAiCloudEngine.getInstance()

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: ByteArray? = null,
        selectedModelId: String? = null,
        onThought: (String?) -> Unit = {},
        onStreamToken: suspend (String) -> Unit = {},
        onAgentStreamEvent: (suspend (com.example.domain.agent.AgentStreamEvent) -> Unit)? = null
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        val cloudModel = if (selectedModelId?.startsWith("gemini") == true) selectedModelId else null
        try {
            val initialState = AgentState(
                userQuery = userMessage,
                history = recentHistory,
                imageAttachment = imageAttachment,
                selectedModelId = selectedModelId
            )

            val wrappedStreamToken: suspend (String) -> Unit = { token ->
                onStreamToken(token)
                onAgentStreamEvent?.invoke(com.example.domain.agent.AgentStreamEvent.ResponseChunk(token))
            }

            val stateMachine = LumiAgentGraph.create(
                agentStateRepository = agentStateRepository,
                semanticMemoryEngine = semanticMemoryEngine,
                toolDispatcher = toolDispatcher,
                onDeviceGemmaEngine = onDeviceGemmaEngine,
                onStreamToken = wrappedStreamToken,
                toolRetriever = toolRetriever,
                onAgentStreamEvent = onAgentStreamEvent
            )
            var finalState = initialState

            // Execute the DAG state machine via Kotlin Flow
            stateMachine.run(initialState).collect { state ->
                finalState = state
                onThought(state.currentThought)
                state.currentThought?.let { t ->
                    onAgentStreamEvent?.invoke(com.example.domain.agent.AgentStreamEvent.ThoughtToken(t))
                }
                onAgentStreamEvent?.invoke(com.example.domain.agent.AgentStreamEvent.StatusChanged(state.status))
            }

            if (finalState.status == AgentStatus.WAITING_FOR_HITL) {
                hitlApprovalManager?.enqueueHitlAction(finalState)
                val toolName = finalState.pendingToolName ?: "action"
                val hitlMsg = "I've staged the `$toolName` action for you! Since this interacts with external services, please review and approve it to execute. 🛡️"
                onStreamToken(hitlMsg)
                return@withContext AgentExecutionResult(
                    responseText = hitlMsg,
                    inferredEmotion = PetEmotion.THINKING,
                    toolReports = finalState.executedToolReports,
                    finalThought = finalState.currentThought
                )
            }

            if (finalState.status == AgentStatus.FAILED) {
                if (finalState.executedToolReports.isNotEmpty()) {
                    val reportText = finalState.executedToolReports.last().description
                    onStreamToken(reportText)
                    return@withContext AgentExecutionResult(
                        responseText = reportText,
                        inferredEmotion = PetEmotion.HAPPY,
                        toolReports = finalState.executedToolReports,
                        finalThought = finalState.currentThought
                    )
                }
                // Fallback to direct Firebase AI generation - local Gemma IDs are never sent to cloud
                val directResponse = firebaseAiEngine.generateChatResponseStream(
                    prompt = userMessage,
                    history = recentHistory,
                    image = imageAttachment,
                    modelName = cloudModel,
                    onChunk = onStreamToken
                )
                return@withContext AgentExecutionResult(
                    responseText = directResponse,
                    inferredEmotion = PetEmotion.HAPPY,
                    toolReports = finalState.executedToolReports,
                    finalThought = finalState.currentThought
                )
            }

            val finalReply = if (finalState.finalResponseText != null) {
                onStreamToken(finalState.finalResponseText!!)
                finalState.finalResponseText!!
            } else if (finalState.executedToolReports.isNotEmpty()) {
                val reportText = finalState.executedToolReports.last().description
                onStreamToken(reportText)
                reportText
            } else {
                firebaseAiEngine.generateChatResponseStream(
                    prompt = userMessage,
                    history = recentHistory,
                    image = imageAttachment,
                    modelName = cloudModel,
                    onChunk = onStreamToken
                )
            }

            AgentExecutionResult(
                responseText = finalReply,
                inferredEmotion = finalState.inferredEmotion,
                toolReports = finalState.executedToolReports,
                finalThought = finalState.currentThought
            )

        } catch (e: Exception) {
            val fallbackText = firebaseAiEngine.generateChatResponseStream(
                prompt = userMessage,
                history = recentHistory,
                image = imageAttachment,
                modelName = cloudModel,
                onChunk = onStreamToken
            )
            AgentExecutionResult(
                responseText = fallbackText,
                inferredEmotion = PetEmotion.HAPPY,
                toolReports = emptyList(),
                finalThought = "Fallback execution due to error."
            )
        }
    }
}
