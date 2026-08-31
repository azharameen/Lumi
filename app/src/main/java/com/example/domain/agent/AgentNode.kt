package com.example.domain.agent

/**
 * Interface representing a distinct node/step in the agent's DAG state machine.
 */
interface AgentNode {
    /**
     * Unique identifier for this node in the graph (e.g., "INTENT_ROUTING", "REASONING", "TOOL_EXECUTION").
     */
    val name: String

    /**
     * Executes the node's task based on current [state] and returns an updated immutable [AgentState].
     */
    suspend fun execute(state: AgentState): AgentState
}
