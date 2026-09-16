package com.example.domain.mcp

import com.example.domain.tools.LumiTool
import com.example.domain.tools.ToolCategory
import com.example.domain.tools.ToolExecutionResult
import com.example.domain.tools.ToolGroupRepository
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
    val isEnabled: Boolean = true,
    /** Use Streamable HTTP transport (SSE) instead of plain JSON-RPC. */
    val useStreamableHttp: Boolean = false,
    /** Tool category override for discovered tools. */
    val toolCategory: ToolCategory = ToolCategory.CONNECTORS,
    /** Description for the dynamic group. */
    val description: String = "Remote MCP server"
)

class McpClientEngine(
    private val jsonRpcClient: McpJsonRpcClient = McpJsonRpcClient(),
    private val toolGroupRepository: ToolGroupRepository? = null
) {
    companion object {
        private const val TAG = "McpClientEngine"
    }

    private val activeServers = mutableListOf<McpServerConfig>()

    private val streamableClients = mutableMapOf<String, McpStreamableHttpClient>()

    fun registerServer(config: McpServerConfig) {
        activeServers.add(config)
        println("[$TAG] Registered MCP server: ${config.name} at ${config.endpointUrl} (streamable=${config.useStreamableHttp})")
    }

    /**
     * Discovers tools from all registered MCP servers, registers them in the ToolRegistry,
     * and registers each MCP server as a dynamic tool group for enable/disable management.
     */
    suspend fun discoverAndBridgeTools(registry: ToolRegistry = ToolRegistry.getInstance()) = withContext(Dispatchers.IO) {
        for (server in activeServers.filter { it.isEnabled }) {
            try {
                // Register the MCP server as a dynamic group (idempotent)
                val groupId = "mcp_${server.serverId}"
                toolGroupRepository?.registerDynamicGroup(
                    groupId = groupId,
                    displayName = "${server.name} (MCP)",
                    description = server.description,
                    category = server.toolCategory,
                    endpointUrl = server.endpointUrl
                )

                val remoteTools = if (server.useStreamableHttp) {
                    discoverViaStreamableHttp(server)
                } else {
                    jsonRpcClient.listTools(server.endpointUrl)
                }

                if (remoteTools.isNotEmpty()) {
                    for (remoteTool in remoteTools) {
                        val tool = if (server.useStreamableHttp) {
                            StreamableMcpTool(
                                serverId = server.serverId,
                                serverName = server.name,
                                endpointUrl = server.endpointUrl,
                                toolName = remoteTool.name,
                                toolDesc = remoteTool.description,
                                parametersJson = remoteTool.parametersJson,
                                toolCategory = server.toolCategory
                            )
                        } else {
                            DynamicMcpTool(
                                serverId = server.serverId,
                                serverName = server.name,
                                endpointUrl = server.endpointUrl,
                                toolName = remoteTool.name,
                                toolDesc = remoteTool.description,
                                jsonRpcClient = jsonRpcClient
                            )
                        }
                        registry.registerTool(tool)
                        // Track this tool under the server's dynamic group
                        toolGroupRepository?.registerToolInGroup(groupId, tool.id, tool.displayName)
                    }
                    println("[$TAG] Discovered ${remoteTools.size} tools from ${server.name} → group '$groupId'")
                } else {
                    val bridgeTool = McpBridgeTool(
                        serverId = server.serverId,
                        serverName = server.name,
                        endpointUrl = server.endpointUrl,
                        jsonRpcClient = jsonRpcClient
                    )
                    registry.registerTool(bridgeTool)
                    toolGroupRepository?.registerToolInGroup(groupId, bridgeTool.id, bridgeTool.displayName)
                    println("[$TAG] No tools from ${server.name}, registered bridge tool in group '$groupId'")
                }
            } catch (e: Exception) {
                println("[$TAG] Error discovering tools from ${server.name}: ${e.message}")
            }
        }
    }

    private suspend fun discoverViaStreamableHttp(server: McpServerConfig): List<McpRemoteTool> {
        val client = streamableClients.getOrPut(server.endpointUrl) {
            McpStreamableHttpClient(server.endpointUrl)
        }
        return client.listTools()
    }

    fun shutdown() {
        streamableClients.values.forEach { it.shutdown() }
        streamableClients.clear()
    }
}

class StreamableMcpTool(
    val serverId: String,
    val serverName: String,
    val endpointUrl: String,
    val toolName: String,
    val toolDesc: String,
    val parametersJson: String,
    toolCategory: ToolCategory = ToolCategory.CONNECTORS
) : LumiTool {
    override val id: String = "mcp_${serverId}_$toolName"
    override val displayName: String = "MCP: $toolName"
    override val description: String = toolDesc
    override val category: ToolCategory = toolCategory
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.MEDIUM

    override val parameters: List<ToolParameter> by lazy {
        parseParametersFromJson(parametersJson)
    }

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult = withContext(Dispatchers.IO) {
        val client = McpStreamableHttpClient(endpointUrl)
        try {
            val resultText = client.callTool(toolName, params)
            ToolExecutionResult(
                success = !resultText.contains("error", ignoreCase = true),
                resultText = resultText,
                payload = mapOf("serverId" to serverId, "tool" to toolName)
            )
        } catch (e: Exception) {
            ToolExecutionResult(
                success = false,
                resultText = "MCP tool execution failed: ${e.message}",
                errorDetails = e.message
            )
        } finally {
            client.shutdown()
        }
    }

    private fun parseParametersFromJson(jsonStr: String): List<ToolParameter> {
        return try {
            val schema = JSONObject(jsonStr)
            val props = schema.optJSONObject("properties") ?: return emptyList()
            val required = schema.optJSONArray("required")
            val requiredSet = (0 until (required?.length() ?: 0)).mapNotNull { i ->
                required?.optString(i)
            }.toSet()

            props.keys().asSequence().map { key ->
                val propObj = props.optJSONObject(key)
                ToolParameter(
                    name = key,
                    type = propObj?.optString("type", "string") ?: "string",
                    description = propObj?.optString("description", "") ?: "",
                    required = key in requiredSet
                )
            }.toList()
        } catch (_: Exception) {
            listOf(ToolParameter("arguments", "object", "Tool arguments payload", required = false))
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
    override val displayName: String = "MCP: $toolName"
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
    override val displayName: String = "MCP: $serverName"
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
