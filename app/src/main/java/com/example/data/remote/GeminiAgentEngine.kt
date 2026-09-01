package com.example.data.remote

import android.graphics.Bitmap
import com.example.data.local.LumiDatabase
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
    val toolReports: List<ToolExecutionReport> = emptyList()
)

/**
 * Enterprise Agent Engine executing state machine graphs (LumiAgentGraph).
 * Uses Firebase AI Logic as the zero-key Cloud LLM provider with Play Integrity transport security.
 */
class GeminiAgentEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val database: LumiDatabase,
    private val hitlApprovalManager: HitlApprovalManager? = null
) {
    private val firebaseAiEngine = FirebaseAiCloudEngine.getInstance()

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(),
        imageAttachment: Bitmap? = null
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        try {
            val initialState = AgentState(
                userQuery = userMessage,
                history = recentHistory,
                imageAttachment = imageAttachment
            )

            val stateMachine = LumiAgentGraph.create(database, toolDispatcher)
            var finalState = initialState

            // Execute the DAG state machine via Kotlin Flow
            stateMachine.run(initialState).collect { state ->
                finalState = state
            }

            if (finalState.status == AgentStatus.WAITING_FOR_HITL) {
                hitlApprovalManager?.enqueueHitlAction(finalState)
                val toolName = finalState.pendingToolName ?: "action"
                return@withContext AgentExecutionResult(
                    responseText = "I've staged the `$toolName` action for you! Since this interacts with external services, please review and approve it to execute. 🛡️",
                    inferredEmotion = PetEmotion.THINKING,
                    toolReports = finalState.executedToolReports
                )
            }

            if (finalState.status == AgentStatus.FAILED) {
                // Fallback to direct Firebase AI generation
                val directResponse = firebaseAiEngine.generateChatResponse(
                    prompt = userMessage,
                    history = recentHistory,
                    image = imageAttachment
                )
                return@withContext AgentExecutionResult(
                    responseText = directResponse,
                    inferredEmotion = PetEmotion.HAPPY,
                    toolReports = finalState.executedToolReports
                )
            }

            val finalReply = finalState.finalResponseText
                ?: firebaseAiEngine.generateChatResponse(
                    prompt = userMessage,
                    history = recentHistory,
                    image = imageAttachment
                )

            AgentExecutionResult(
                responseText = finalReply,
                inferredEmotion = finalState.inferredEmotion,
                toolReports = finalState.executedToolReports
            )

        } catch (e: Exception) {
            val fallbackText = firebaseAiEngine.generateChatResponse(
                prompt = userMessage,
                history = recentHistory,
                image = imageAttachment
            )
            AgentExecutionResult(
                responseText = fallbackText,
                inferredEmotion = PetEmotion.HAPPY,
                toolReports = emptyList()
            )
        }
    }
}
