package com.example.domain.agent

import com.example.data.local.LumiDatabase
import com.example.data.remote.OnDeviceGemmaEngine
import com.example.domain.agent.nodes.*
import com.example.domain.tools.AgentToolDispatcher

object LumiAgentGraph {

    /**
     * Builds and configures the DAG Agent State Machine graph for Lumi.
     */
    fun create(
        database: LumiDatabase,
        toolDispatcher: AgentToolDispatcher,
        onDeviceGemmaEngine: OnDeviceGemmaEngine? = null
    ): AgentStateMachine {
        val stateMachine = AgentStateMachine(database.agentCheckpointDao())

        // 1. Register Nodes
        stateMachine
            .registerNode(StartNode())
            .registerNode(IntentRoutingNode(onDeviceGemmaEngine))
            .registerNode(MemoryRetrievalNode(database))
            .registerNode(PlanningNode())
            .registerNode(ReasoningNode(onDeviceGemmaEngine))
            .registerNode(ToolExecutionNode(toolDispatcher))
            .registerNode(ReflexionNode())
            .registerNode(FinalSynthesisNode())

        // 2. Define Transition Edges
        stateMachine
            .addParallelEdge("START") { listOf("INTENT_ROUTING", "MEMORY_RETRIEVAL") }
            .addEdge("MEMORY_RETRIEVAL") { "PLANNING" }
            .addEdge("PLANNING") { "REASONING" }
            .addEdge("REASONING") { state ->
                if (state.pendingToolName != null) {
                    "TOOL_EXECUTION"
                } else {
                    "FINAL_SYNTHESIS"
                }
            }
            .addEdge("TOOL_EXECUTION") { state ->
                when {
                    state.status == AgentStatus.WAITING_FOR_HITL -> "TOOL_EXECUTION"
                    state.lastError != null && state.retryCount < state.maxRetries -> "REFLEXION"
                    else -> "REASONING"
                }
            }
            .addEdge("REFLEXION") { "REASONING" }
            .addEdge("FINAL_SYNTHESIS") { "FINAL_SYNTHESIS" }

        return stateMachine
    }
}
