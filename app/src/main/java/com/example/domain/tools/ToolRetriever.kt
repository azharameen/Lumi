package com.example.domain.tools

import com.example.data.tools.FastToolIndex

/**
 * Stage 1 Fast Local Retriever orchestrator.
 * Intercepts user query, triggers FastToolIndex, and returns candidates for Stage 2 LLM execution.
 */
class ToolRetriever(
    private val fastToolIndex: FastToolIndex,
    private val toolRegistry: ToolRegistry = ToolRegistry.getInstance()
) {

    /**
     * Re-indexes all registered tools into the SQLite FTS engine.
     */
    suspend fun initializeIndex() {
        fastToolIndex.syncIndexFromRegistry()
    }

    /**
     * Fast retrieval for 1,000+ tool ecosystem.
     * Evaluates query in ~3ms and returns top N matching tools.
     */
    suspend fun getRelevantTools(userQuery: String, maxTools: Int = 3): List<LumiTool> {
        if (toolRegistry.getToolCount() == 0) {
            return emptyList()
        }
        
        // Query Stage 1 FTS Index
        val matchedTools = fastToolIndex.searchTools(userQuery, maxTools)
        return matchedTools
    }
}
