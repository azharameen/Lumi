package com.example.domain.agent

import com.example.domain.repository.AgentStateRepository
import com.example.domain.memory.SemanticMemoryEngine
import com.example.data.remote.OnDeviceGemmaEngine
import com.example.domain.agent.nodes.*
import com.example.domain.tools.AgentToolDispatcher

object LumiAgentGraph {

    /**
     * Builds and configures the DAG Agent State Machine graph for Lumi.
     */
    fun create(
        agentStateRepository: AgentStateRepository,
        semanticMemoryEngine: SemanticMemoryEngine,
        toolDispatcher: AgentToolDispatcher,
        onDeviceGemmaEngine: OnDeviceGemmaEngine? = null,
        onStreamToken: (suspend (String) -> Unit)? = null,
        toolRetriever: com.example.domain.tools.ToolRetriever? = null,
        onAgentStreamEvent: (suspend (AgentStreamEvent) -> Unit)? = null
    ): AgentStateMachine {
        val stateMachine = AgentStateMachine(agentStateRepository)

        // 1. Register Nodes
        stateMachine
            .registerNode(StartNode())
            .registerNode(IntentRoutingNode(onDeviceGemmaEngine))
            .registerNode(MemoryRetrievalNode(semanticMemoryEngine))
            .registerNode(PlanningNode(onDeviceGemmaEngine))
            .registerNode(ReasoningNode(onDeviceGemmaEngine, onStreamToken, toolRetriever))
            .registerNode(ToolExecutionNode(toolDispatcher, onAgentStreamEvent))
            .registerNode(ReflexionNode())
            .registerNode(FinalSynthesisNode())

        // 2. Define Transition Edges
        stateMachine
            .addParallelEdge("START") { listOf("INTENT_ROUTING", "MEMORY_RETRIEVAL") }
            .addEdge("MEMORY_RETRIEVAL") { "PLANNING" }
            .addEdge("PLANNING") { "REASONING" }
            .addEdge("REASONING") { state ->
                if (state.pendingToolName != null || state.pendingToolCalls.isNotEmpty()) {
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

        return stateMachine
    }
}
