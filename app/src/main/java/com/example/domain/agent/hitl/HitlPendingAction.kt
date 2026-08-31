package com.example.domain.agent.hitl

import com.example.domain.agent.AgentState

/**
 * Represents a high-risk tool call intercepted by the agent framework,
 * awaiting explicit Human-In-The-Loop approval before execution.
 */
data class HitlPendingAction(
    val stateId: String,
    val toolName: String,
    val actionTitle: String,
    val actionDescription: String,
    val payloadPreview: String,
    val state: AgentState,
    val timestampMillis: Long = System.currentTimeMillis()
)
