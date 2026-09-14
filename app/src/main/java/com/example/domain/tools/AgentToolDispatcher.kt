package com.example.domain.tools

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.domain.model.ToolExecutionReport
import com.example.domain.repository.PetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.context.GlobalContext
import java.util.Locale

class AgentToolDispatcher(
    private val petRepository: PetRepository,
    private val context: Context? = null
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

        // Check required OS permissions before execution
        val activeContext = context ?: getKoinContext()
        val requiredPermissions = getRequiredPermissions(tool.id)
        if (activeContext != null && requiredPermissions.isNotEmpty()) {
            val missingPermission = requiredPermissions.find { perm ->
                ContextCompat.checkSelfPermission(activeContext, perm) != PackageManager.PERMISSION_GRANTED
            }
            if (missingPermission != null) {
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

    private fun getKoinContext(): Context? {
        return try {
            GlobalContext.getOrNull()?.getOrNull<Context>()
        } catch (_: Exception) {
            null
        }
    }

    private fun getRequiredPermissions(toolId: String): List<String> {
        val id = toolId.lowercase(Locale.ROOT)
        return when {
            id.contains("location") || id.contains("gps") -> listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            id.contains("camera") || id.contains("photo") -> listOf(Manifest.permission.CAMERA)
            id.contains("record") || id.contains("audio") || id.contains("mic") -> listOf(Manifest.permission.RECORD_AUDIO)
            id.contains("sms") -> listOf(Manifest.permission.SEND_SMS)
            id.contains("contacts") -> listOf(Manifest.permission.READ_CONTACTS)
            id.contains("calendar") -> listOf(Manifest.permission.READ_CALENDAR)
            else -> emptyList()
        }
    }

    private fun handlePermissionError(toolName: String, detail: String): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to "Permission denied for tool '$toolName'. Missing required permission: $detail")
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Permission Error ⚠️",
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
            title = "Tool Execution Warning ⚠️",
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
            title = "Tool Execution Error ⚠️",
            description = "Failed to run $toolName: ${e.localizedMessage ?: "Invalid parameters"}",
            payloadPreview = "Schema parsing or execution exception",
            isSuccess = false
        )
        return output to report
    }
}
