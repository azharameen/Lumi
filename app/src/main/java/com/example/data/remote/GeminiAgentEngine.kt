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
 * Replaces legacy ReAct loops with structured DAG execution nodes and HITL governance.
 */
class GeminiAgentEngine(
    private val toolDispatcher: AgentToolDispatcher,
    private val database: LumiDatabase,
    private val hitlApprovalManager: HitlApprovalManager? = null
) {

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(), // Pair of (sender, text)
        imageAttachment: Bitmap? = null
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext executeLocalFallback(userMessage, imageAttachment)
        }

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
                return@withContext AgentExecutionResult(
                    responseText = finalState.lastError ?: "State machine execution encountered an error.",
                    inferredEmotion = PetEmotion.CONCERNED,
                    toolReports = finalState.executedToolReports
                )
            }

            AgentExecutionResult(
                responseText = finalState.finalResponseText ?: "Action completed for you! ✨",
                inferredEmotion = finalState.inferredEmotion,
                toolReports = finalState.executedToolReports
            )

        } catch (e: Exception) {
            executeLocalFallback(userMessage, imageAttachment)
        }
    }

    private fun executeLocalFallback(
        userMessage: String,
        imageAttachment: Bitmap?
    ): AgentExecutionResult {
        return AgentExecutionResult(
            "I need a Gemini API Key to process your request and run tools dynamically! Please add it in the Settings screen.",
            PetEmotion.CONCERNED,
            emptyList()
        )
    }
}
