package com.example.domain.agent.hooks

import com.example.domain.agent.AgentState

/**
 * Interface for agent node lifecycle hooks.
 * Executed before/after each node step in the LumiAgentGraph DAG state machine.
 */
interface AgentNodeHook {
    suspend fun onBeforeNode(nodeName: String, state: AgentState): AgentState
    suspend fun onAfterNode(nodeName: String, state: AgentState): AgentState
    suspend fun onNodeError(nodeName: String, state: AgentState, error: Throwable): AgentState
}
