package com.example

import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStateSerializer
import com.example.domain.agent.AgentStatus
import com.example.domain.agent.PendingToolCall
import com.example.domain.agent.nodes.ReflexionNode
import com.example.domain.memory.VectorEmbeddingUtils
import com.example.domain.memory.WorkingMemory
import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport
import com.example.domain.tools.LumiTool
import com.example.domain.tools.ToolCategory
import com.example.domain.tools.ToolExecutionResult
import com.example.domain.tools.ToolParameter
import com.example.domain.tools.ToolParameterValidator
import com.example.domain.tools.ToolRiskLevel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AgentCorePhase1Test {

    @Test
    fun workingMemory_prunesByTurnCount() {
        val memory = WorkingMemory(maxTurns = 3, maxTokens = 1000)
        memory.addTurn("user", "Hello 1")
        memory.addTurn("model", "Hi 1")
        memory.addTurn("user", "Hello 2")
        memory.addTurn("model", "Hi 2")

        val turns = memory.getTurns()
        assertEquals(3, turns.size)
        assertEquals("Hi 1", turns[0].second)
        assertEquals("Hello 2", turns[1].second)
        assertEquals("Hi 2", turns[2].second)
    }

    @Test
    fun workingMemory_preservesPinnedInstructionsWhenPruning() {
        val memory = WorkingMemory(maxTurns = 3, maxTokens = 1000)
        // Add pinned system context
        memory.addTurn("system", "You are Lumi, an empathetic companion.", isPinned = true)
        memory.addTurn("user", "Hello 1")
        memory.addTurn("model", "Hi 1")
        memory.addTurn("user", "Hello 2")
        memory.addTurn("model", "Hi 2")

        val turns = memory.getFullTurns()
        // Total turns must be capped at 3
        assertEquals(3, turns.size)
        // First turn must be the pinned system turn, not evicted!
        assertEquals("system", turns[0].speaker)
        assertTrue(turns[0].isPinned)
        assertEquals("Hello 2", turns[1].text)
        assertEquals("Hi 2", turns[2].text)
    }

    @Test
    fun workingMemory_prunesByTokenBudget() {
        // Very small token budget to force token-based eviction
        val memory = WorkingMemory(maxTurns = 10, maxTokens = 15)
        memory.addTurn("user", "Short message")
        memory.addTurn("model", "This is a much longer response that consumes many tokens")

        // Should evict earlier turn to stay within budget
        assertTrue(memory.calculateTotalTokens() <= 20)
    }

    @Test
    fun vectorEmbeddingUtils_roundTripAndCosineSimilarity() {
        val originalFloats = floatArrayOf(1.0f, 0.5f, -0.5f, 0.0f, 0.8f)
        val bytes = VectorEmbeddingUtils.floatArrayToByteArray(originalFloats)
        val deserializedFloats = VectorEmbeddingUtils.byteArrayToFloatArray(bytes)

        assertEquals(originalFloats.size, deserializedFloats.size)
        for (i in originalFloats.indices) {
            assertEquals(originalFloats[i], deserializedFloats[i], 0.0001f)
        }

        // Test identical vectors have cosine similarity of 1.0
        val simIdentical = VectorEmbeddingUtils.cosineSimilarity(originalFloats, deserializedFloats)
        assertEquals(1.0f, simIdentical, 0.0001f)

        // Test orthogonal vectors have cosine similarity of 0.0
        val vecA = floatArrayOf(1f, 0f)
        val vecB = floatArrayOf(0f, 1f)
        val simOrthogonal = VectorEmbeddingUtils.cosineSimilarity(vecA, vecB)
        assertEquals(0.0f, simOrthogonal, 0.0001f)
    }

    @Test
    fun agentStateSerializer_serializesAndDeserializesCorrectly() {
        val state = AgentState(
            id = "test-state-123",
            userQuery = "What's on my calendar and can you turn on flashlight?",
            history = listOf("user" to "Hello", "model" to "Hi!"),
            selectedModelId = "gemini-2.5-flash",
            currentThought = "Analyzing tools",
            currentNodeName = "TOOL_EXECUTION",
            status = AgentStatus.WAITING_FOR_HITL,
            selectedSkillName = "general",
            isLocalExecution = false,
            retrievedContext = "User prefers morning meetings",
            pendingToolName = "system_toggle_flashlight",
            pendingToolArgs = mapOf("state" to true),
            pendingToolCalls = listOf(
                PendingToolCall(id = "call-1", toolName = "system_toggle_flashlight", args = mapOf("state" to true)),
                PendingToolCall(id = "call-2", toolName = "calendar_get_events", args = mapOf("date" to "today"))
            ),
            hitlRequired = true,
            executedToolReports = listOf(
                ToolExecutionReport(
                    toolName = "calendar_get_events",
                    title = "Get Calendar Events",
                    description = "Found 2 events",
                    isSuccess = true,
                    payloadPreview = "Events list"
                )
            ),
            retryCount = 1,
            maxRetries = 3,
            stepCount = 2,
            maxSteps = 10,
            lastError = null,
            finalResponseText = null,
            inferredEmotion = PetEmotion.THINKING
        )

        val json = AgentStateSerializer.serialize(state)
        assertTrue(json.isNotBlank())
        assertTrue(json.contains("test-state-123"))
        assertTrue(json.contains("system_toggle_flashlight"))

        val restored = AgentStateSerializer.deserialize(json)
        assertNotNull(restored)
        assertEquals(state.id, restored!!.id)
        assertEquals(state.userQuery, restored.userQuery)
        assertEquals(state.status, restored.status)
        assertEquals(2, restored.pendingToolCalls.size)
        assertEquals("system_toggle_flashlight", restored.pendingToolCalls[0].toolName)
        assertEquals("calendar_get_events", restored.pendingToolCalls[1].toolName)
        assertEquals(1, restored.executedToolReports.size)
        assertEquals("Get Calendar Events", restored.executedToolReports[0].title)
        assertEquals(PetEmotion.THINKING, restored.inferredEmotion)
    }

    @Test
    fun factKnowledgeEntity_temporalDecayCalculatesAccurately() {
        val now = System.currentTimeMillis()
        val thirtyDaysAgo = now - (30L * 24L * 60L * 60L * 1000L)

        // Pinned fact should not decay
        val pinnedFact = com.example.data.local.entity.FactKnowledgeEntity(
            predicate = "diet",
            objectValue = "Vegetarian",
            confidence = 0.95f,
            isPinned = true,
            lastUpdatedMillis = thirtyDaysAgo,
            accessCount = 1
        )
        assertEquals(0.95f, pinnedFact.effectiveConfidence(now), 0.001f)

        // Unpinned fact should decay by ~half-life over 30 days (+ access bonus)
        val unpinnedFact = com.example.data.local.entity.FactKnowledgeEntity(
            predicate = "temporary_note",
            objectValue = "Buy milk",
            confidence = 0.80f,
            isPinned = false,
            lastUpdatedMillis = thirtyDaysAgo,
            accessCount = 1
        )
        // 0.80 * 0.5 + (1 * 0.02) = 0.42
        val decayed = unpinnedFact.effectiveConfidence(now)
        assertTrue("Decayed score should be approximately 0.42 but was $decayed", decayed in 0.41f..0.43f)
    }

    @Test
    fun agentStreamEvent_sealedHierarchyTypes() {
        val tokenEvent = com.example.domain.agent.AgentStreamEvent.ThoughtToken("Analyzing intent")
        val executingEvent = com.example.domain.agent.AgentStreamEvent.ToolExecuting("system_toggle_flashlight", mapOf("state" to true))
        val completedEvent = com.example.domain.agent.AgentStreamEvent.ToolCompleted("system_toggle_flashlight", "Flashlight turned on", true)
        val chunkEvent = com.example.domain.agent.AgentStreamEvent.ResponseChunk("I've enabled the flashlight!")
        val statusEvent = com.example.domain.agent.AgentStreamEvent.StatusChanged(AgentStatus.COMPLETED)

        assertTrue(tokenEvent is com.example.domain.agent.AgentStreamEvent)
        assertTrue(executingEvent is com.example.domain.agent.AgentStreamEvent)
        assertTrue(completedEvent is com.example.domain.agent.AgentStreamEvent)
        assertTrue(chunkEvent is com.example.domain.agent.AgentStreamEvent)
        assertTrue(statusEvent is com.example.domain.agent.AgentStreamEvent)
    }

    @Test
    fun toolParameterValidator_resolvesAliasesAndTypes() {
        val fakeTool = object : com.example.domain.tools.LumiTool {
            override val id = "system_set_quick_timer"
            override val displayName = "Timer"
            override val description = "Timer"
            override val category = com.example.domain.tools.ToolCategory.UTILITY
            override val riskLevel = com.example.domain.tools.ToolRiskLevel.LOW
            override val parameters = listOf(
                com.example.domain.tools.ToolParameter("seconds", "number", "Duration in seconds", required = true),
                com.example.domain.tools.ToolParameter("label", "string", "Timer label", required = false)
            )
            override suspend fun execute(params: Map<String, Any?>) =
                com.example.domain.tools.ToolExecutionResult(true, "OK")
        }

        // 1. Validated directly
        val res1 = com.example.domain.tools.ToolParameterValidator.validate(fakeTool, mapOf("seconds" to 120))
        assertTrue(res1.isValid)
        assertEquals(120.0, res1.validatedParams["seconds"] as Double, 0.001)

        // 2. Small LLM passes alias 'duration' instead of 'seconds'
        val res2 = com.example.domain.tools.ToolParameterValidator.validate(fakeTool, mapOf("duration" to "300"))
        assertTrue("Expected alias 'duration' to resolve to 'seconds'", res2.isValid)
        assertEquals(300.0, res2.validatedParams["seconds"] as Double, 0.001)

        // 3. Small LLM passes alias 'minutes' instead of 'seconds'
        val res3 = com.example.domain.tools.ToolParameterValidator.validate(fakeTool, mapOf("minutes" to 5))
        assertTrue("Expected alias 'minutes' to resolve to 'seconds'", res3.isValid)
        assertEquals(5.0, res3.validatedParams["seconds"] as Double, 0.001)
    }

    @Test
    fun reflexionNode_abortsOnDuplicateFailureOrMaxReflections() = runBlocking {
        val node = ReflexionNode()

        // 1. Initial state with error
        val state1 = AgentState(
            userQuery = "Run tool",
            currentNodeName = "REFLEXION",
            pendingToolName = "my_tool",
            pendingToolArgs = mapOf("a" to 1),
            lastError = "Connection refused"
        )
        val res1 = node.execute(state1)
        assertEquals(1, res1.reflectionCount)
        assertEquals("REFLEXION", res1.currentNodeName)

        // 2. Exact same failure signature again
        val state2 = res1.copy(
            pendingToolName = "my_tool",
            pendingToolArgs = mapOf("a" to 1),
            lastError = "Connection refused"
        )
        val res2 = node.execute(state2)
        assertEquals("FINAL_SYNTHESIS", res2.currentNodeName)
        assertTrue(res2.finalResponseText!!.contains("Aborting reflection loop"))

        // 3. Max reflections limit >= 2
        val stateMax = AgentState(
            userQuery = "Run tool",
            reflectionCount = 2,
            pendingToolName = "my_tool",
            lastError = "Another error"
        )
        val resMax = node.execute(stateMax)
        assertEquals("FINAL_SYNTHESIS", resMax.currentNodeName)
    }

    @Test
    fun workingMemory_atomicToolPairPruning() {
        val memory = WorkingMemory(maxTurns = 3, maxTokens = 1000)
        memory.addTurn("user", "Help me search")
        memory.addTurn("assistant_tool_call", "Tool Call: search(q='lumi')", isToolCall = true)
        memory.addTurn("tool_result", "Tool Result: found 5 items", isToolResult = true)
        memory.addTurn("model", "I found 5 items for you.")

        // Should prune oldest turns to stay <= maxTurns (3)
        val turns = memory.getFullTurns()
        assertTrue(turns.size <= 3)
        val hasCall = turns.any { it.isToolCall }
        val hasResult = turns.any { it.isToolResult }
        assertEquals(hasCall, hasResult)
    }

    @Test
    fun toolParameterValidator_coercesStringifiedNumbersAndBooleans() {
        val fakeTool = object : LumiTool {
            override val id = "test_coercion_tool"
            override val displayName = "Test Coercion"
            override val description = "Test"
            override val category = ToolCategory.UTILITY
            override val riskLevel = ToolRiskLevel.LOW
            override val parameters = listOf(
                ToolParameter("volume", "integer", "Volume level", required = true),
                ToolParameter("enabled", "boolean", "Is enabled", required = true)
            )
            override suspend fun execute(params: Map<String, Any?>) =
                ToolExecutionResult(true, "OK")
        }

        val res = ToolParameterValidator.validate(
            fakeTool,
            mapOf("volume" to "80", "enabled" to "true")
        )
        assertTrue(res.isValid)
        assertEquals(80L, res.validatedParams["volume"])
        assertEquals(true, res.validatedParams["enabled"])
    }

    @Test
    fun vectorEmbeddingUtils_handlesZeroAndSmallDenominatorsWithoutNaN() {
        val zeroVec = floatArrayOf(0f, 0f, 0f)
        val smallVec = floatArrayOf(1e-8f, 1e-8f, 1e-8f)
        val normVec = floatArrayOf(1f, 2f, 3f)

        val simZero = VectorEmbeddingUtils.cosineSimilarity(zeroVec, normVec)
        assertEquals(0.0f, simZero, 0.00001f)
        assertFalse(simZero.isNaN())

        val simSmall = VectorEmbeddingUtils.cosineSimilarity(smallVec, smallVec)
        assertEquals(0.0f, simSmall, 0.00001f)
        assertFalse(simSmall.isNaN())
    }
}


