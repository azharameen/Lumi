package com.example.domain.agent.nodes

import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiFunctionResponse
import com.example.data.remote.GeminiPart
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStatus
import com.example.domain.tools.AgentToolDispatcher

import com.example.domain.agent.PendingToolCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

import com.example.domain.agent.AgentStreamEvent

class ToolExecutionNode(
    private val toolDispatcher: AgentToolDispatcher,
    private val onEvent: (suspend (AgentStreamEvent) -> Unit)? = null
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
        val callsToExecute: List<PendingToolCall> = if (state.pendingToolCalls.isNotEmpty()) {
            state.pendingToolCalls
        } else if (state.pendingToolName != null) {
            listOf(PendingToolCall(toolName = state.pendingToolName, args = state.pendingToolArgs ?: emptyMap()))
        } else {
            return state.copy(lastError = "No pending tool call found")
        }

        // Check HITL gate for calls requiring user approval
        val hitlCall = callsToExecute.find { hitlTools.contains(it.toolName) }
        if (hitlCall != null && !state.hitlRequired && state.executedToolReports.none { it.toolName == hitlCall.toolName }) {
            return state.copy(
                status = AgentStatus.WAITING_FOR_HITL,
                hitlRequired = true,
                pendingToolName = hitlCall.toolName,
                pendingToolArgs = hitlCall.args
            )
        }

        return try {
            val executed = coroutineScope {
                callsToExecute.map { call ->
                    async(Dispatchers.IO) {
                        onEvent?.invoke(AgentStreamEvent.ToolExecuting(call.toolName, call.args))
                        val (toolResult, report) = toolDispatcher.executeTool(call.toolName, call.args)
                        onEvent?.invoke(AgentStreamEvent.ToolCompleted(call.toolName, report.description, report.isSuccess))
                        Triple(call, toolResult, report)
                    }
                }.awaitAll()
            }

            val updatedReports = state.executedToolReports.toMutableList()
            val responseParts = mutableListOf<GeminiPart>()
            var lastErr: String? = null

            for ((call, toolResult, report) in executed) {
                updatedReports.add(report)
                responseParts.add(
                    GeminiPart(
                        functionResponse = GeminiFunctionResponse(
                            name = call.toolName,
                            response = toolResult
                        )
                    )
                )
                if (toolResult["status"] == "error") {
                    lastErr = toolResult["message"] as? String
                }
            }

            val updatedContents = state.contentsList.toMutableList().apply {
                add(
                    GeminiContent(
                        role = "user",
                        parts = responseParts
                    )
                )
            }

            state.copy(
                contentsList = updatedContents,
                executedToolReports = updatedReports,
                pendingToolName = null,
                pendingToolArgs = null,
                pendingToolCalls = emptyList(),
                lastError = lastErr
            )
        } catch (e: Exception) {
            state.copy(
                lastError = e.localizedMessage ?: "Tool execution failed",
                retryCount = state.retryCount + 1
            )
        }
    }
}
