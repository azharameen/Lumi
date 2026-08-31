package com.example.domain.tools

import java.util.concurrent.ConcurrentHashMap

/**
 * Enterprise-grade thread-safe Tool Registry capable of holding 1,000+ LumiTools.
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
