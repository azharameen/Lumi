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

data class McpServerConfig(
    val serverId: String,
    val name: String,
    val endpointUrl: String,
    val isEnabled: Boolean = true
)

class McpClientEngine(
    private val jsonRpcClient: McpJsonRpcClient = McpJsonRpcClient()
) {
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
            val remoteTools = jsonRpcClient.listTools(server.endpointUrl)
            if (remoteTools.isNotEmpty()) {
                for (remoteTool in remoteTools) {
                    val tool = DynamicMcpTool(
                        serverId = server.serverId,
                        serverName = server.name,
                        endpointUrl = server.endpointUrl,
                        toolName = remoteTool.name,
                        toolDesc = remoteTool.description,
                        jsonRpcClient = jsonRpcClient
                    )
                    registry.registerTool(tool)
                }
            } else {
                val bridgeTool = McpBridgeTool(
                    serverId = server.serverId,
                    serverName = server.name,
                    endpointUrl = server.endpointUrl,
                    jsonRpcClient = jsonRpcClient
                )
                registry.registerTool(bridgeTool)
            }
        }
    }
}

class DynamicMcpTool(
    val serverId: String,
    val serverName: String,
    val endpointUrl: String,
    val toolName: String,
    val toolDesc: String,
    private val jsonRpcClient: McpJsonRpcClient
) : LumiTool {
    override val id: String = "mcp_${serverId}_$toolName"
    override val displayName: String = "MCP: $toolName ⚡"
    override val description: String = toolDesc
    override val category: ToolCategory = ToolCategory.CONNECTORS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.HIGH
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("arguments", "object", "Tool arguments payload", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult = withContext(Dispatchers.IO) {
        val resultText = jsonRpcClient.callTool(endpointUrl, toolName, params)
        ToolExecutionResult(
            success = !resultText.contains("error", ignoreCase = true),
            resultText = resultText,
            payload = mapOf("serverId" to serverId, "tool" to toolName)
        )
    }
}

class McpBridgeTool(
    val serverId: String,
    val serverName: String,
    val endpointUrl: String,
    private val jsonRpcClient: McpJsonRpcClient
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
        val responseText = jsonRpcClient.callTool(endpointUrl, action, params)
        ToolExecutionResult(
            success = true,
            resultText = "Executed MCP $action on $serverName: $responseText",
            payload = mapOf("serverId" to serverId, "action" to action)
        )
    }
}
