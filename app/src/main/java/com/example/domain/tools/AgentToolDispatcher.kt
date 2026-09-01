package com.example.domain.tools

import com.example.data.device.HealthConnectManager
import com.example.data.local.LumiDatabase
import com.example.domain.model.ToolExecutionReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enterprise agent tool dispatcher.
 * Uses structured schemas and typed arguments parsing, eliminating fragile regex parsing.
 * Now refactored to use ToolRegistry pattern.
 */
class AgentToolDispatcher(
    private val database: LumiDatabase
) {

    /**
     * Executes the requested tool by looking it up in the ToolRegistry.
     */
    suspend fun executeTool(
        toolName: String,
        args: Map<String, Any?>?
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val registry = ToolRegistry.getInstance()
        val tool = registry.getTool(toolName)

        if (tool == null) {
            return@withContext handleUnknownTool(toolName)
        }

        // 1. Structured Argument Validation
        val validationError = validateArgs(tool, args)
        if (validationError != null) {
            return@withContext handleToolError(toolName, Exception("Validation Error: $validationError"))
        }

        val result = try {
            val executionResult = tool.execute(args ?: emptyMap())
            
            val report = ToolExecutionReport(
                toolName = tool.id,
                title = tool.displayName,
                description = executionResult.resultText,
                payloadPreview = executionResult.payload.toString().take(100),
                isSuccess = executionResult.success
            )
            
            executionResult.payload to report
        } catch (e: Exception) {
            handleToolError(toolName, e)
        }

        // Reward Pet Evolution on successful tool usage
        rewardPetProgression()

        result
    }

    private fun validateArgs(tool: LumiTool, args: Map<String, Any?>?): String? {
        val params = args ?: emptyMap()
        for (expected in tool.parameters) {
            if (expected.required && !params.containsKey(expected.name)) {
                return "Missing required parameter: ${expected.name}"
            }
            // Basic type validation can be added here
        }
        return null
    }

    private fun handleUnknownTool(toolName: String): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to "Tool '$toolName' not recognized.")
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Tool Execution Warning ⚠️",
            description = "Tool '$toolName' is not registered in dispatcher",
            payloadPreview = "Dispatcher bypassed"
        )
        return output to report
    }

    private fun handleToolError(toolName: String, e: Exception): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to (e.localizedMessage ?: "Unknown tool execution failure"))
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Tool Execution Error ⚠️",
            description = "Failed to run $toolName: ${e.localizedMessage ?: "Invalid parameters"}",
            payloadPreview = "Schema parsing or execution exception"
        )
        return output to report
    }

    private suspend fun rewardPetProgression() {
        try {
            val evolution = database.petEvolutionDao().getPetEvolutionDirect()
            if (evolution != null) {
                var newExp = evolution.exp + 20
                var newLevel = evolution.level
                var expNeeded = evolution.expToNextLevel
                var newCoins = evolution.coins + 25
                var newGems = evolution.gems

                while (newExp >= expNeeded) {
                    newExp -= expNeeded
                    newLevel += 1
                    expNeeded = (expNeeded * 1.3).toInt()
                    newCoins += 50
                    newGems += 5
                }

                database.petEvolutionDao().insertOrUpdate(
                    evolution.copy(
                        exp = newExp,
                        level = newLevel,
                        expToNextLevel = expNeeded,
                        coins = newCoins,
                        gems = newGems,
                        happiness = (evolution.happiness + 5).coerceAtMost(100)
                    )
                )
            }
        } catch (_: Exception) {}
    }
}
