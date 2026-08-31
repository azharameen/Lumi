package com.example.domain.agent.nodes

import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiFunctionResponse
import com.example.data.remote.GeminiPart
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStatus
import com.example.domain.tools.AgentToolDispatcher

class ToolExecutionNode(
    private val toolDispatcher: AgentToolDispatcher
) : AgentNode {
    override val name: String = "TOOL_EXECUTION"

    // High-risk tools requiring Human-In-The-Loop explicit approval
    private val hitlTools = setOf(
        "google_send_email",
        "google_create_doc",
        "github_create_issue",
        "slack_post_message"
    )

    override suspend fun execute(state: AgentState): AgentState {
        val toolName = state.pendingToolName ?: return state.copy(lastError = "No pending tool call found")

        // Check HITL gate
        if (hitlTools.contains(toolName) && !state.hitlRequired && state.executedToolReports.none { it.toolName == toolName }) {
            return state.copy(
                status = AgentStatus.WAITING_FOR_HITL,
                hitlRequired = true
            )
        }

        return try {
            val (toolResult, report) = toolDispatcher.executeTool(toolName, state.pendingToolArgs)

            val updatedReports = state.executedToolReports.toMutableList().apply { add(report) }

            // Append function response turn to Gemini context list
            val updatedContents = state.contentsList.toMutableList().apply {
                add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(
                            GeminiPart(
                                functionResponse = GeminiFunctionResponse(
                                    name = toolName,
                                    response = toolResult
                                )
                            )
                        )
                    )
                )
            }

            state.copy(
                contentsList = updatedContents,
                executedToolReports = updatedReports,
                pendingToolName = null,
                pendingToolArgs = null,
                lastError = if (toolResult["status"] == "error") toolResult["message"] as? String else null
            )
        } catch (e: Exception) {
            state.copy(
                lastError = e.localizedMessage ?: "Tool execution failed",
                retryCount = state.retryCount + 1
            )
        }
    }
}
