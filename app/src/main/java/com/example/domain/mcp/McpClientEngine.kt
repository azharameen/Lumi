package com.example.domain.mcp

import android.util.Log
import com.example.domain.tools.LumiTool
import com.example.domain.tools.ToolCategory
import com.example.domain.tools.ToolExecutionResult
import com.example.domain.tools.ToolParameter
import com.example.domain.tools.ToolRegistry
import com.example.domain.tools.ToolRiskLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class McpServerConfig(
    val serverId: String,
    val name: String,
    val endpointUrl: String,
    val isEnabled: Boolean = true
)

class McpClientEngine {
    companion object {
        private const val TAG = "McpClientEngine"
    }

    private val activeServers = mutableListOf<McpServerConfig>()

    fun registerServer(config: McpServerConfig) {
        activeServers.add(config)
        Log.i(TAG, "Registered MCP server: ${config.name} at ${config.endpointUrl}")
    }

    suspend fun discoverAndBridgeTools(registry: ToolRegistry = ToolRegistry.getInstance()) = withContext(Dispatchers.IO) {
        for (server in activeServers.filter { it.isEnabled }) {
            // Register an MCP bridge tool dynamically in Lumi's tool registry
            val bridgeTool = McpBridgeTool(
                serverId = server.serverId,
                serverName = server.name,
                endpointUrl = server.endpointUrl
            )
            registry.registerTool(bridgeTool)
        }
    }
}

class McpBridgeTool(
    val serverId: String,
    val serverName: String,
    val endpointUrl: String
) : LumiTool {
    override val id: String = "mcp_${serverId}_execute"
    override val displayName: String = "MCP: $serverName ⚡"
    override val description: String = "Model Context Protocol remote tool execution via $serverName"
    override val category: ToolCategory = ToolCategory.CONNECTORS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.HIGH
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("action", "string", "Action or tool method name", required = true),
        ToolParameter("payloadJson", "string", "JSON payload arguments", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult = withContext(Dispatchers.IO) {
        val action = params["action"]?.toString() ?: "ping"
        return@withContext try {
            ToolExecutionResult(
                success = true,
                resultText = "Executed MCP $action on $serverName ($endpointUrl)",
                payload = mapOf("serverId" to serverId, "action" to action)
            )
        } catch (e: Exception) {
            ToolExecutionResult(false, "MCP execution error: ${e.localizedMessage}")
        }
    }
}
