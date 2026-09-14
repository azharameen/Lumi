package com.example.domain.agent

import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Enterprise JSON Serializer/Deserializer for AgentState using Moshi.
 * Enables zero-loss process-death resumption during Human-In-The-Loop (HITL) gates
 * across both pure JVM unit tests and on-device Android runtime.
 */
object AgentStateSerializer {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stateAdapter = moshi.adapter(SerializableState::class.java)
    private val mapAdapter = moshi.adapter(Map::class.java)

    data class SerializableTurn(val sender: String, val text: String)
    data class SerializableCall(val id: String, val toolName: String, val argsJson: String)
    data class SerializableReport(
        val toolName: String,
        val title: String,
        val description: String,
        val isSuccess: Boolean,
        val payloadPreview: String
    )

    data class SerializableState(
        val id: String,
        val userQuery: String,
        val history: List<SerializableTurn> = emptyList(),
        val selectedModelId: String? = null,
        val currentThought: String? = null,
        val currentNodeName: String = "START",
        val status: String = "RUNNING",
        val selectedSkillName: String? = null,
        val isLocalExecution: Boolean = false,
        val retrievedContext: String = "",
        val pendingToolName: String? = null,
        val pendingToolArgsJson: String? = null,
        val pendingToolCalls: List<SerializableCall> = emptyList(),
        val hitlRequired: Boolean = false,
        val executedToolReports: List<SerializableReport> = emptyList(),
        val retryCount: Int = 0,
        val maxRetries: Int = 3,
        val stepCount: Int = 0,
        val maxSteps: Int = 10,
        val lastError: String? = null,
        val finalResponseText: String? = null,
        val inferredEmotion: String = "HAPPY"
    )

    fun serialize(state: AgentState): String {
        return try {
            val serializable = SerializableState(
                id = state.id,
                userQuery = state.userQuery,
                history = state.history.map { SerializableTurn(it.first, it.second) },
                selectedModelId = state.selectedModelId,
                currentThought = state.currentThought,
                currentNodeName = state.currentNodeName,
                status = state.status.name,
                selectedSkillName = state.selectedSkillName,
                isLocalExecution = state.isLocalExecution,
                retrievedContext = state.retrievedContext,
                pendingToolName = state.pendingToolName,
                pendingToolArgsJson = state.pendingToolArgs?.let { mapAdapter.toJson(it) },
                pendingToolCalls = state.pendingToolCalls.map { call ->
                    SerializableCall(
                        id = call.id,
                        toolName = call.toolName,
                        argsJson = mapAdapter.toJson(call.args)
                    )
                },
                hitlRequired = state.hitlRequired,
                executedToolReports = state.executedToolReports.map {
                    SerializableReport(it.toolName, it.title, it.description, it.isSuccess, it.payloadPreview)
                },
                retryCount = state.retryCount,
                maxRetries = state.maxRetries,
                stepCount = state.stepCount,
                maxSteps = state.maxSteps,
                lastError = state.lastError,
                finalResponseText = state.finalResponseText,
                inferredEmotion = state.inferredEmotion.name
            )
            stateAdapter.toJson(serializable)
        } catch (_: Exception) {
            ""
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun deserialize(jsonString: String): AgentState? {
        if (jsonString.isBlank()) return null
        return try {
            val serializable = stateAdapter.fromJson(jsonString) ?: return null
            val status = try { AgentStatus.valueOf(serializable.status) } catch (_: Exception) { AgentStatus.RUNNING }
            val emotion = try { PetEmotion.valueOf(serializable.inferredEmotion) } catch (_: Exception) { PetEmotion.HAPPY }

            val parsedPendingArgs = serializable.pendingToolArgsJson?.let {
                try { mapAdapter.fromJson(it) as? Map<String, Any?> } catch (_: Exception) { null }
            }

            val parsedCalls = serializable.pendingToolCalls.map { call ->
                val callArgs = try {
                    mapAdapter.fromJson(call.argsJson) as? Map<String, Any?> ?: emptyMap()
                } catch (_: Exception) { emptyMap() }
                PendingToolCall(id = call.id, toolName = call.toolName, args = callArgs)
            }

            AgentState(
                id = serializable.id,
                userQuery = serializable.userQuery,
                history = serializable.history.map { it.sender to it.text },
                selectedModelId = serializable.selectedModelId,
                currentThought = serializable.currentThought,
                currentNodeName = serializable.currentNodeName,
                status = status,
                selectedSkillName = serializable.selectedSkillName,
                isLocalExecution = serializable.isLocalExecution,
                retrievedContext = serializable.retrievedContext,
                pendingToolName = serializable.pendingToolName,
                pendingToolArgs = parsedPendingArgs,
                pendingToolCalls = parsedCalls,
                hitlRequired = serializable.hitlRequired,
                executedToolReports = serializable.executedToolReports.map {
                    ToolExecutionReport(it.toolName, it.title, it.description, it.isSuccess, it.payloadPreview)
                },
                retryCount = serializable.retryCount,
                maxRetries = serializable.maxRetries,
                stepCount = serializable.stepCount,
                maxSteps = serializable.maxSteps,
                lastError = serializable.lastError,
                finalResponseText = serializable.finalResponseText,
                inferredEmotion = emotion
            )
        } catch (_: Exception) {
            null
        }
    }
}
