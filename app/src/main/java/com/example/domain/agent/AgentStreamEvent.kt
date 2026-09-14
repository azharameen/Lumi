package com.example.domain.agent

/**
 * Unified stream events emitted during an agentic turn for real-time UI/UX feedback.
 */
sealed interface AgentStreamEvent {
    data class ThoughtToken(val text: String) : AgentStreamEvent
    data class ToolExecuting(val toolName: String, val args: Map<String, Any?>) : AgentStreamEvent
    data class ToolCompleted(val toolName: String, val summary: String, val success: Boolean) : AgentStreamEvent
    data class ResponseChunk(val text: String) : AgentStreamEvent
    data class StatusChanged(val status: AgentStatus) : AgentStreamEvent
}
