package com.example

import com.example.domain.tools.LumiTool
import com.example.domain.tools.ToolCategory
import com.example.domain.tools.ToolExecutionResult
import com.example.domain.tools.ToolParameter
import com.example.domain.tools.ToolRegistry
import com.example.domain.tools.ToolRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ToolRegistryTest {

    private lateinit var registry: ToolRegistry

    private class TestTool(
        override val id: String,
        override val displayName: String,
        override val category: ToolCategory,
        override val description: String,
        override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW,
        override val parameters: List<ToolParameter> = emptyList()
    ) : LumiTool {
        override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
            return ToolExecutionResult(success = true, resultText = "Executed $id")
        }
    }

    @Before
    fun setUp() {
        registry = ToolRegistry.getInstance()
        registry.clear()
    }

    @Test
    fun `test register and retrieve single tool`() {
        val tool = TestTool(
            id = "tool_focus_timer",
            displayName = "Zen Focus Timer",
            category = ToolCategory.UTILITY,
            description = "Starts a mindful Pomodoro session with gentle ambient chimes."
        )

        registry.registerTool(tool)
        assertEquals(1, registry.getToolCount())

        val retrieved = registry.getTool("tool_focus_timer")
        assertNotNull(retrieved)
        assertEquals("Zen Focus Timer", retrieved?.displayName)
        assertEquals(ToolCategory.UTILITY, retrieved?.category)
    }

    @Test
    fun `test register batch tools and unregister`() {
        val tools: List<LumiTool> = listOf(
            TestTool("tool_1", "Log Hydration", ToolCategory.HEALTH, "Logs a glass of water"),
            TestTool("tool_2", "Schedule Event", ToolCategory.CALENDAR, "Creates a calendar event"),
            TestTool("tool_3", "Connect Integration", ToolCategory.CONNECTORS, "Integrates user service")
        )

        registry.registerTools(tools)
        assertEquals(3, registry.getToolCount())

        registry.unregisterTool("tool_2")
        assertEquals(2, registry.getToolCount())
        assertNull(registry.getTool("tool_2"))
        assertNotNull(registry.getTool("tool_1"))
        assertNotNull(registry.getTool("tool_3"))
    }

    @Test
    fun `test clear registry resets count`() {
        registry.registerTool(
            TestTool("tool_temp", "Temporary Tool", ToolCategory.SYSTEM, "Test description")
        )
        assertEquals(1, registry.getToolCount())
        registry.clear()
        assertEquals(0, registry.getToolCount())
    }
}
