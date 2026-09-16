package com.example.domain.tools

import com.example.data.tools.FastToolIndex

/**
 * Stage 1 Fast Local Retriever orchestrator.
 * Intercepts user query, triggers FastToolIndex, and returns candidates for Stage 2 LLM execution.
 * Filters out disabled tools via [ToolGroupRepository].
 */
class ToolRetriever(
    private val fastToolIndex: FastToolIndex,
    private val toolRegistry: ToolRegistry = ToolRegistry.getInstance(),
    private val toolGroupRepository: ToolGroupRepository? = null
) {

    /**
     * Re-indexes all registered tools into the SQLite FTS engine.
     */
    suspend fun initializeIndex() {
        fastToolIndex.syncIndexFromRegistry()
    }

    /**
     * Fast retrieval for 1,000+ tool ecosystem.
     * Evaluates query in ~3ms and returns top N matching tools that are currently enabled.
     */
    suspend fun getRelevantTools(userQuery: String, maxTools: Int = 3): List<LumiTool> {
        if (toolRegistry.getToolCount() == 0) {
            return emptyList()
        }

        // Query Stage 1 FTS Index
        val matchedTools = fastToolIndex.searchTools(userQuery, maxTools)

        // Filter out disabled tools
        return matchedTools.filter { tool ->
            toolGroupRepository?.isToolActive(tool.id) ?: true
        }
    }

    /**
     * Returns all active (enabled) tools, optionally filtered by group.
     * Used when the AI engine needs the full tool list for function declarations.
     */
    suspend fun getActiveTools(groupId: String? = null): List<LumiTool> {
        val allTools = if (groupId != null) {
            toolRegistry.getToolsByGroup(groupId)
        } else {
            toolRegistry.getAllTools()
        }
        return allTools.filter { tool ->
            toolGroupRepository?.isToolActive(tool.id) ?: true
        }
    }
}
