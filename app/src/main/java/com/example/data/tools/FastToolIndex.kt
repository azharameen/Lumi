package com.example.data.tools

import com.example.data.local.dao.ToolFtsDao
import com.example.data.local.entity.ToolFtsEntity
import com.example.domain.tools.LumiTool
import com.example.domain.tools.ToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Stage 1 Fast Local Indexer & Search Engine.
 * Leverages SQLite FTS5 / BM25 inside LumiDatabase to query 1,000+ registered tools in ~2ms.
 */
class FastToolIndex(
    private val toolFtsDao: ToolFtsDao,
    private val toolRegistry: ToolRegistry = ToolRegistry.getInstance()
) {

    /**
     * Rebuilds the FTS index from all currently registered LumiTools in ToolRegistry.
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
     * Search 1,000+ indexed tools and return top N matching LumiTools.
     * Completes in ~2-5ms.
     */
    suspend fun searchTools(query: String, topK: Int = 3): List<LumiTool> = withContext(Dispatchers.IO) {
        val sanitizedQuery = sanitizeFtsQuery(query)
        if (sanitizedQuery.isBlank()) {
            return@withContext emptyList()
        }

        try {
            val matchingIds = toolFtsDao.searchMatchingToolIds(sanitizedQuery, topK)
            matchingIds.mapNotNull { id -> toolRegistry.getTool(id) }
        } catch (e: Exception) {
            // Fallback: Simple keyword scanning if FTS syntax error occurs
            fallbackKeywordSearch(query, topK)
        }
    }

    private fun sanitizeFtsQuery(query: String): String {
        // Extract words and format for SQLite FTS5 wildcard search (word*)
        val words = query.replace(Regex("[^a-zA-Z0-9 ]"), " ")
            .split(" ")
            .filter { it.length > 2 }
        if (words.isEmpty()) return ""
        return words.joinToString(" OR ") { "$it*" }
    }

    private fun fallbackKeywordSearch(query: String, topK: Int): List<LumiTool> {
        val lowerQuery = query.lowercase()
        return toolRegistry.getAllTools()
            .filter { tool ->
                tool.displayName.lowercase().contains(lowerQuery) ||
                tool.description.lowercase().contains(lowerQuery) ||
                tool.id.lowercase().contains(lowerQuery)
            }
            .take(topK)
    }
}
