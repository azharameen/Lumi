package com.example.domain.tools

import com.example.domain.model.ToolExecutionReport
import com.example.domain.repository.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class AgentToolDispatcher(
    private val petRepository: PetRepository,
    private val permissionChecker: ToolPermissionChecker? = null,
    private val toolGroupRepository: ToolGroupRepository? = null
) {
    suspend fun executeTool(
        toolName: String,
        args: Map<String, Any?>?
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val registry = ToolRegistry.getInstance()
        val cleanName = toolName.trim().lowercase(Locale.ROOT).removePrefix("system_").removePrefix("tool_")

        val tool = registry.getTool(toolName)
            ?: registry.getAllTools().find { it.id.equals(toolName, ignoreCase = true) }
            ?: registry.getAllTools().find { it.id.removePrefix("system_").equals(cleanName, ignoreCase = true) }
            ?: registry.getAllTools().find { it.id.contains(cleanName, ignoreCase = true) }
            ?: registry.getAllTools().find { it.displayName.equals(cleanName, ignoreCase = true) }

        if (tool == null) {
            return@withContext handleUnknownTool(toolName)
        }

        // Gate 1: Enable/disable check via ToolGroupRepository
        toolGroupRepository?.let { groupRepo ->
            if (!groupRepo.isToolActive(tool.id)) {
                return@withContext handleDisabledTool(tool)
            }
        }

        // Gate 2: OS permission check
        permissionChecker?.let { checker ->
            val (hasPermission, missingPermission) = checker.hasPermissionForTool(tool.id)
            if (!hasPermission && missingPermission != null) {
                return@withContext handlePermissionError(toolName, missingPermission)
            }
        }

        val validationResult = ToolParameterValidator.validate(tool, args ?: emptyMap())
        if (!validationResult.isValid) {
            return@withContext handleToolError(toolName, Exception("Validation Error: ${validationResult.errorMessage}"))
        }

        val safeParams = validationResult.validatedParams

        val result = try {
            val executionResult = tool.execute(safeParams)

            val report = ToolExecutionReport(
                toolName = tool.id,
                title = tool.displayName,
                description = executionResult.resultText,
                payloadPreview = executionResult.payload.toString().take(100),
                isSuccess = executionResult.success
            )

            executionResult.payload to report
        } catch (e: SecurityException) {
            handlePermissionError(toolName, e.localizedMessage ?: "SecurityException thrown during execution")
        } catch (e: Exception) {
            handleToolError(toolName, e)
        }

        if (result.second.isSuccess) {
            petRepository.earnCoinsAndExp(coins = 25, exp = 20, reason = "Tool Execution: $toolName")
        }

        result
    }

    private fun handleDisabledTool(tool: LumiTool): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf(
            "status" to "disabled",
            "message" to "Tool '${tool.displayName}' is currently disabled. Ask the user to enable it in Settings > Tools & Connectors."
        )
        val report = ToolExecutionReport(
            toolName = tool.id,
            title = "Tool Disabled",
            description = "Tool '${tool.displayName}' has been disabled by the user.",
            payloadPreview = "Disabled by user preference",
            isSuccess = false
        )
        return output to report
    }

    private fun handlePermissionError(toolName: String, detail: String): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to "Permission denied for tool '$toolName'. Missing required permission: $detail")
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Permission Error",
            description = "Failed to run $toolName: Permission denied ($detail)",
            payloadPreview = "SecurityException / Missing permission",
            isSuccess = false
        )
        return output to report
    }

    private fun handleUnknownTool(toolName: String): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to "Tool '$toolName' not recognized.")
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Tool Execution Warning",
            description = "Tool '$toolName' is not registered in dispatcher",
            payloadPreview = "Dispatcher bypassed",
            isSuccess = false
        )
        return output to report
    }

    private fun handleToolError(toolName: String, e: Exception): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to (e.localizedMessage ?: "Unknown tool execution failure"))
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Tool Execution Error",
            description = "Failed to run $toolName: ${e.localizedMessage ?: "Invalid parameters"}",
            payloadPreview = "Schema parsing or execution exception",
            isSuccess = false
        )
        return output to report
    }
}
