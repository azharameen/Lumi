package com.example.data.tools

import com.example.data.local.dao.ToolFtsDao
import com.example.data.local.entity.ToolFtsEntity
import com.example.domain.memory.WordEmbeddingSimilarity
import com.example.domain.tools.LumiTool
import com.example.domain.tools.ToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stage 1 Fast Local Indexer & Search Engine.
 * Uses MediaPipe dense embeddings for semantic matching of tools.
 */
class FastToolIndex(
    private val toolFtsDao: ToolFtsDao,
    private val toolRegistry: ToolRegistry = ToolRegistry.getInstance()
) {
    /**
     * Rebuilds the FTS index from all currently registered LumiTools in ToolRegistry.
     * Still keeps FTS for broad sync, but semantic search is preferred.
     */
    suspend fun syncIndexFromRegistry() = withContext(Dispatchers.IO) {
        val allTools = toolRegistry.getAllTools()
        toolFtsDao.clearIndex()
        
        val entities = allTools.mapIndexed { index, tool ->
            val keywords = tool.parameters.joinToString(" ") { "${it.name} ${it.description}" }
            ToolFtsEntity(
                rowid = index + 1,
                toolId = tool.id,
                displayName = tool.displayName,
                description = tool.description,
                category = tool.category.name,
                keywords = keywords
            )
        }

        if (entities.isNotEmpty()) {
            toolFtsDao.insertAll(entities)
        }
    }

    /**
     * Search indexed tools and return top N matching LumiTools semantically.
     */
    suspend fun searchTools(query: String, topK: Int = 3): List<LumiTool> = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext emptyList()
        }

        val allTools = toolRegistry.getAllTools()
        
        // Use dense vector embeddings to rank tools instead of keyword matching
        val scoredTools = allTools.map { tool ->
            val toolContent = "${tool.displayName} ${tool.description}"
            val score = WordEmbeddingSimilarity.calculateSimilarity(query, toolContent)
            tool to score
        }.sortedByDescending { it.second }
        
        // Only return tools that have a reasonable semantic match
        scoredTools.filter { it.second > 0.02f }
            .take(topK)
            .map { it.first }
    }
}
