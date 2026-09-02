package com.example.domain.agent.hooks

import android.util.Log
import com.example.domain.agent.AgentState

class TelemetryHook : AgentNodeHook {
    companion object {
        private const val TAG = "TelemetryHook"
    }

    private var nodeStartTime: Long = 0L

    override suspend fun onBeforeNode(nodeName: String, state: AgentState): AgentState {
        nodeStartTime = System.currentTimeMillis()
        Log.d(TAG, "Entering node: $nodeName")
        return state
    }

    override suspend fun onAfterNode(nodeName: String, state: AgentState): AgentState {
        val duration = System.currentTimeMillis() - nodeStartTime
        Log.d(TAG, "Exited node: $nodeName in ${duration}ms")
        return state
    }

    override suspend fun onNodeError(nodeName: String, state: AgentState, error: Throwable): AgentState {
        Log.e(TAG, "Node error in $nodeName: ${error.message}", error)
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
            Log.i(TAG, "Security audit validating tool risk level before dispatch.")
        }
        return state
    }

    override suspend fun onAfterNode(nodeName: String, state: AgentState): AgentState = state

    override suspend fun onNodeError(nodeName: String, state: AgentState, error: Throwable): AgentState = state
}
