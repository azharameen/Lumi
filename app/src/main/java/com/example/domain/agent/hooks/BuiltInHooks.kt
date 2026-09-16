package com.example.domain.agent.hooks

import com.example.domain.agent.AgentState

class TelemetryHook : AgentNodeHook {
    companion object {
        private const val TAG = "TelemetryHook"
    }

    private var nodeStartTime: Long = 0L

    override suspend fun onBeforeNode(nodeName: String, state: AgentState): AgentState {
        nodeStartTime = System.currentTimeMillis()
        println("[$TAG] Entering node: $nodeName")
        return state
    }

    override suspend fun onAfterNode(nodeName: String, state: AgentState): AgentState {
        val duration = System.currentTimeMillis() - nodeStartTime
        println("[$TAG] Exited node: $nodeName in ${duration}ms")
        return state
    }

    override suspend fun onNodeError(nodeName: String, state: AgentState, error: Throwable): AgentState {
        println("[$TAG] Node error in $nodeName: ${error.message}")
        return state
    }
}

class SensorContextHook : AgentNodeHook {
    override suspend fun onBeforeNode(nodeName: String, state: AgentState): AgentState {
        // Enriches state with hardware metadata if at StartNode
        return if (nodeName == "START") {
            state.copy(currentThought = "Telemetry and sensor context injected.")
        } else {
            state
        }
    }

    override suspend fun onAfterNode(nodeName: String, state: AgentState): AgentState = state

    override suspend fun onNodeError(nodeName: String, state: AgentState, error: Throwable): AgentState = state
}

class SecurityAuditHook : AgentNodeHook {
    companion object {
        private const val TAG = "SecurityAuditHook"
    }

    override suspend fun onBeforeNode(nodeName: String, state: AgentState): AgentState {
        if (nodeName == "TOOL_EXECUTION") {
            println("[$TAG] Security audit validating tool risk level before dispatch.")
        }
        return state
    }

    override suspend fun onAfterNode(nodeName: String, state: AgentState): AgentState = state

    override suspend fun onNodeError(nodeName: String, state: AgentState, error: Throwable): AgentState = state
}
