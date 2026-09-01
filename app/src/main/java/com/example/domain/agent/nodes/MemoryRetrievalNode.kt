package com.example.domain.agent.nodes

import com.example.data.local.LumiDatabase
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.memory.SemanticMemoryEngine

class MemoryRetrievalNode(
    private val database: LumiDatabase
) : AgentNode {
    override val name: String = "MEMORY_RETRIEVAL"

    private val semanticMemoryEngine = SemanticMemoryEngine(database)

    override suspend fun execute(state: AgentState): AgentState {
        return try {
            val relevantContext = semanticMemoryEngine.retrieveRelevantContext(state.userQuery)
            state.copy(
                retrievedContext = relevantContext,
                currentThought = if (relevantContext.isNotBlank()) "Retrieved relevant user context." else "No relevant context found."
            )
        } catch (e: Exception) {
            state.copy(retrievedContext = "")
        }
    }
}
