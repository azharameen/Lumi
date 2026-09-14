package com.example.domain.agent.nodes

import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState

class ReflexionNode : AgentNode {
    override val name: String = "REFLEXION"

    override suspend fun execute(state: AgentState): AgentState {
        val errorMsg = state.lastError ?: "Tool call returned an error."
        val toolName = state.pendingToolName
            ?: state.executedToolReports.lastOrNull()?.toolName
            ?: "unknown_tool"
        val argsHash = state.pendingToolArgs?.hashCode()
            ?: state.pendingToolCalls.lastOrNull()?.args?.hashCode()
            ?: 0
        val signature = "$toolName:$argsHash:$errorMsg"

        val isDuplicateFailure = state.failureSignatures.contains(signature)
        val nextReflectionCount = state.reflectionCount + 1

        if (isDuplicateFailure || nextReflectionCount > 2 || state.reflectionCount >= 2) {
            val failureSummary = "Aborting reflection loop: repeating tool failure or max reflections reached ($nextReflectionCount attempts). Tool '$toolName' failed with: '$errorMsg'."
            return state.copy(
                currentNodeName = "FINAL_SYNTHESIS",
                finalResponseText = failureSummary,
                pendingToolName = null,
                pendingToolArgs = null,
                pendingToolCalls = emptyList(),
                reflectionCount = nextReflectionCount,
                failureSignatures = state.failureSignatures + signature,
                currentThought = failureSummary
            )
        }

        // Format a self-correction feedback part into history
        val feedbackContent = GeminiContent(
            role = "user",
            parts = listOf(
                GeminiPart(
                    text = "System Notice: Previous action failed with error: '$errorMsg'. Please analyze why it failed and try a corrected tool call or alternative response."
                )
            )
        )

        val updatedContents = state.contentsList.toMutableList().apply { add(feedbackContent) }

        return state.copy(
            contentsList = updatedContents,
            pendingToolName = null,
            pendingToolArgs = null,
            reflectionCount = nextReflectionCount,
            failureSignatures = state.failureSignatures + signature,
            currentThought = "Analyzing error and attempting self-correction: $errorMsg"
        )
    }
}
