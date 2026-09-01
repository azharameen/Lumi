package com.example.domain.agent.nodes

import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState

class ReflexionNode : AgentNode {
    override val name: String = "REFLEXION"

    override suspend fun execute(state: AgentState): AgentState {
        val errorMsg = state.lastError ?: "Tool call returned an error."

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
            currentThought = "Analyzing error and attempting self-correction: $errorMsg"
        )
    }
}
