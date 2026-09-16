package com.example.domain.tools

import java.util.concurrent.ConcurrentHashMap

/**
 * Enterprise-grade thread-safe Tool Registry capable of holding 1,000+ LumiTools.
 * Supports group-aware queries via [ToolGroupCatalog].
 */
class ToolRegistry private constructor() {

    private val toolsMap = ConcurrentHashMap<String, LumiTool>()

    fun registerTool(tool: LumiTool) {
        toolsMap[tool.id] = tool
    }

    fun registerTools(tools: List<LumiTool>) {
        tools.forEach { registerTool(it) }
    }

    fun unregisterTool(toolId: String) {
        toolsMap.remove(toolId)
    }

    fun getTool(toolId: String): LumiTool? {
        return toolsMap[toolId]
    }

    fun getAllTools(): List<LumiTool> {
        return toolsMap.values.toList()
    }

    fun getToolCount(): Int = toolsMap.size

    fun clear() {
        toolsMap.clear()
    }

    // ── Group-Aware Queries ──────────────────────────────────

    /**
     * Returns all tools belonging to a specific group ID.
     */
    fun getToolsByGroup(groupId: String): List<LumiTool> {
        return toolsMap.values.filter { tool ->
            ToolGroupCatalog.resolveGroupForTool(tool.id)?.groupId == groupId
        }
    }

    /**
     * Returns all tools in a specific category.
     */
    fun getToolsByCategory(category: ToolCategory): List<LumiTool> {
        return toolsMap.values.filter { it.category == category }
    }

    /**
     * Returns tools organized by their resolved group.
     * Tools that don't match any group are placed under "ungrouped".
     */
    fun getGroupedTools(): Map<String, List<LumiTool>> {
        val grouped = LinkedHashMap<String, MutableList<LumiTool>>()
        for (tool in toolsMap.values) {
            val group = ToolGroupCatalog.resolveGroupForTool(tool.id)
            val key = group?.groupId ?: "ungrouped"
            grouped.getOrPut(key) { mutableListOf() }.add(tool)
        }
        return grouped
    }

    /**
     * Returns all group definitions with their resolved tool counts.
     */
    fun getGroupSummary(): List<GroupSummary> {
        val grouped = getGroupedTools()
        return ToolGroupCatalog.groups.map { def ->
            GroupSummary(
                groupId = def.groupId,
                displayName = def.displayName,
                description = def.description,
                category = def.category,
                toolCount = grouped[def.groupId]?.size ?: 0
            )
        }
    }

    /**
     * Summary of a tool group with its resolved tool count.
     */
    data class GroupSummary(
        val groupId: String,
        val displayName: String,
        val description: String,
        val category: ToolCategory,
        val toolCount: Int
    )

    companion object {
        @Volatile
        private var instance: ToolRegistry? = null

        fun getInstance(): ToolRegistry {
            return instance ?: synchronized(this) {
                instance ?: ToolRegistry().also { instance = it }
            }
        }
    }
}
