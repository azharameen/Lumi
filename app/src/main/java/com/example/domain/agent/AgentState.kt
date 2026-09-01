package com.example.domain.agent


import com.example.data.remote.GeminiContent
import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport

/**
 * Lifecycle status of the Agent State Machine.
 */
enum class AgentStatus {
    IDLE,
    RUNNING,
    WAITING_FOR_HITL, // Suspended for Human-In-The-Loop approval
    COMPLETED,
    FAILED
}

/**
 * Represents the immutable context passed along nodes in the state machine graph.
 */
data class AgentState(
    val id: String = java.util.UUID.randomUUID().toString(),
    val userQuery: String,
    val history: List<Pair<String, String>> = emptyList(),
    val imageAttachment: ByteArray? = null,
    
    // Planning & Execution
    val currentNodeName: String = "INTENT_ROUTING",
    val status: AgentStatus = AgentStatus.RUNNING,
    val selectedSkillName: String? = null,
    val isLocalExecution: Boolean = false,
    
    // Turn contents for LLM
    val contentsList: List<GeminiContent> = emptyList(),
    
    // Memories retrieved
    val retrievedContext: String = "",
    
    // Tools & Retries
    val pendingToolName: String? = null,
    val pendingToolArgs: Map<String, Any?>? = null,
    val hitlRequired: Boolean = false,
    val executedToolReports: List<ToolExecutionReport> = emptyList(),
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val stepCount: Int = 0,
    val maxSteps: Int = 10,
    val lastError: String? = null,
    
    // Final output
    val finalResponseText: String? = null,
    val inferredEmotion: PetEmotion = PetEmotion.HAPPY
)
