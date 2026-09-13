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
            
            // If user is discussing or resuming a known topic frame with a summary, attach it
            val activeTopic = com.example.domain.ai.TopicContextManager.getInstance().getActiveTopic()
            val topicContext = if (activeTopic != null && activeTopic.summary.isNotBlank()) {
                "Active Discussion Topic: ${activeTopic.title}\nTopic Context: ${activeTopic.summary}"
            } else ""

            val combinedContext = when {
                relevantContext.isNotBlank() && topicContext.isNotBlank() -> "$topicContext\n\n$relevantContext"
                topicContext.isNotBlank() -> topicContext
                else -> relevantContext
            }

            state.copy(
                retrievedContext = combinedContext,
                currentThought = if (combinedContext.isNotBlank()) "Retrieved relevant topic & user context." else "No relevant context found."
            )
        } catch (e: Exception) {
            state.copy(retrievedContext = "")
        }
    }
}
