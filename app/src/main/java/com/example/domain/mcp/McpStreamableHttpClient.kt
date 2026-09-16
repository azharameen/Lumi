package com.example.domain.mcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * MCP Streamable HTTP client with SSE response parsing and session management.
 *
 * Implements the MCP Streamable HTTP transport per the MCP spec:
 * 1. POST `initialize` to establish session (receives `Mcp-Session-Id` header)
 * 2. All subsequent requests include the session ID
 * 3. Responses may be `application/json` or `text/event-stream` (SSE)
 */
class McpStreamableHttpClient(
    private val endpointUrl: String
) {
    companion object {
        private const val TAG = "McpStreamableHttp"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val ACCEPT_HEADER = "application/json, text/event-stream"
        private const val PROTOCOL_VERSION = "2024-11-05"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var sessionId: String? = null

    private val requestId = AtomicInteger(0)

    /**
     * Whether the session has been successfully initialized.
     */
    val isConnected: Boolean get() = sessionId != null

    /**
     * Performs the MCP `initialize` handshake to establish a session.
     * Must be called before any `tools/list` or `tools/call` requests.
     *
     * @return true if initialization succeeded
     */
    suspend fun initialize(): Boolean = withContext(Dispatchers.IO) {
        try {
            val payload = buildJsonRpcRequest("initialize", JSONObject().apply {
                put("protocolVersion", PROTOCOL_VERSION)
                put("capabilities", JSONObject())
                put("clientInfo", JSONObject().apply {
                    put("name", "Lumi AI Friend")
                    put("version", "1.0.0")
                })
            })

            val response = executePost(payload)

            if (response.isSuccessful) {
                val sid = response.header("Mcp-Session-Id")
                sessionId = sid
                val body = response.body?.string() ?: "{}"
                val parsed = parseResponse(body)
                if (parsed != null) {
                    println("[$TAG] MCP session initialized: $sid")
                    // Send initialized notification
                    sendInitializedNotification()
                    return@withContext true
                }
            }
            println("[$TAG] MCP initialize failed: HTTP ${response.code}")
            false
        } catch (e: Exception) {
            println("[$TAG] MCP initialize error: ${e.message}")
            false
        }
    }

    /**
     * Tears down the current session.
     */
    fun shutdown() {
        sessionId = null
    }

    /**
     * Re-initializes the session if it has been lost.
     */
    suspend fun ensureConnected(): Boolean {
        if (isConnected) return true
        return initialize()
    }

    /**
     * Lists all tools available on the MCP server.
     */
    suspend fun listTools(): List<McpRemoteTool> = withContext(Dispatchers.IO) {
        if (!ensureConnected()) return@withContext emptyList()

        try {
            val payload = buildJsonRpcRequest("tools/list", JSONObject())
            val response = executePost(payload)

            if (response.isSuccessful) {
                val body = response.body?.string() ?: "{}"
                val parsed = parseResponse(body) ?: return@withContext emptyList()
                val resultObj = parsed.optJSONObject("result") ?: return@withContext emptyList()
                val toolsArr = resultObj.optJSONArray("tools") ?: return@withContext emptyList()

                val list = mutableListOf<McpRemoteTool>()
                for (i in 0 until toolsArr.length()) {
                    val item = toolsArr.getJSONObject(i)
                    list.add(
                        McpRemoteTool(
                            name = item.optString("name", "remote_tool"),
                            description = item.optString("description", "Remote MCP tool"),
                            parametersJson = item.optJSONObject("inputSchema")?.toString() ?: "{}"
                        )
                    )
                }
                return@withContext list
            } else {
                // Session may have expired — try reconnecting once
                sessionId = null
                if (ensureConnected()) {
                    val retryPayload = buildJsonRpcRequest("tools/list", JSONObject())
                    val retryResponse = executePost(retryPayload)
                    if (retryResponse.isSuccessful) {
                        val retryBody = retryResponse.body?.string() ?: "{}"
                        val retryParsed = parseResponse(retryBody) ?: return@withContext emptyList()
                        val retryResult = retryParsed.optJSONObject("result") ?: return@withContext emptyList()
                        val retryTools = retryResult.optJSONArray("tools") ?: return@withContext emptyList()
                        val retryList = mutableListOf<McpRemoteTool>()
                        for (i in 0 until retryTools.length()) {
                            val item = retryTools.getJSONObject(i)
                            retryList.add(
                                McpRemoteTool(
                                    name = item.optString("name", "remote_tool"),
                                    description = item.optString("description", "Remote MCP tool"),
                                    parametersJson = item.optJSONObject("inputSchema")?.toString() ?: "{}"
                                )
                            )
                        }
                        return@withContext retryList
                    }
                }
            }
        } catch (e: Exception) {
            println("[$TAG] listTools error: ${e.message}")
        }
        emptyList()
    }

    /**
     * Calls a specific tool on the MCP server.
     */
    suspend fun callTool(toolName: String, args: Map<String, Any?>): String = withContext(Dispatchers.IO) {
        if (!ensureConnected()) return@withContext "MCP server not connected. Cannot execute '$toolName'."

        try {
            val payload = buildJsonRpcRequest("tools/call", JSONObject().apply {
                put("name", toolName)
                put("arguments", JSONObject(args))
            })
            val response = executePost(payload)

            if (response.isSuccessful) {
                val body = response.body?.string() ?: "{}"
                val parsed = parseResponse(body) ?: return@withContext "MCP server returned unparseable response."
                val resultObj = parsed.optJSONObject("result") ?: return@withContext "MCP server returned no result."

                // MCP result has a "content" array with text items
                val contentArr = resultObj.optJSONArray("content")
                if (contentArr != null && contentArr.length() > 0) {
                    val sb = StringBuilder()
                    for (i in 0 until contentArr.length()) {
                        val contentItem = contentArr.getJSONObject(i)
                        val type = contentItem.optString("type", "text")
                        if (type == "text") {
                            if (sb.isNotEmpty()) sb.append("\n")
                            sb.append(contentItem.optString("text", ""))
                        } else if (type == "image") {
                            sb.append("[Image: ${contentItem.optString("mimeType", "unknown")}]")
                        } else if (type == "resource") {
                            sb.append("[Resource: ${contentItem.optJSONObject("resource")?.optString("uri", "unknown")}]")
                        }
                    }
                    return@withContext sb.toString().ifEmpty { "Tool executed successfully with no text output." }
                }

                // Fallback: return the full result as string
                return@withContext resultObj.toString()
            } else {
                // Session may have expired — try reconnecting once
                sessionId = null
                if (ensureConnected()) {
                    val retryPayload = buildJsonRpcRequest("tools/call", JSONObject().apply {
                        put("name", toolName)
                        put("arguments", JSONObject(args))
                    })
                    val retryResponse = executePost(retryPayload)
                    if (retryResponse.isSuccessful) {
                        val retryBody = retryResponse.body?.string() ?: "{}"
                        val retryParsed = parseResponse(retryBody)
                        val retryResult = retryParsed?.optJSONObject("result")
                        val retryContent = retryResult?.optJSONArray("content")
                        if (retryContent != null && retryContent.length() > 0) {
                            return@withContext retryContent.getJSONObject(0).optString("text", "Tool executed.")
                        }
                        return@withContext retryResult?.toString() ?: "Tool executed."
                    }
                }
                return@withContext "MCP server error: HTTP ${response.code}"
            }
        } catch (e: Exception) {
            println("[$TAG] callTool error: ${e.message}")
            return@withContext "MCP execution error: ${e.localizedMessage}"
        }
    }

    // ── Private Helpers ──────────────────────────────────────────

    private fun buildJsonRpcRequest(method: String, params: JSONObject): String {
        return JSONObject().apply {
            put("jsonrpc", "2.0")
            put("id", requestId.incrementAndGet())
            put("method", method)
            put("params", params)
        }.toString()
    }

    private fun executePost(payload: String): Response {
        val builder = Request.Builder()
            .url(endpointUrl)
            .header("Content-Type", "application/json")
            .header("Accept", ACCEPT_HEADER)
            .post(payload.toRequestBody(JSON_MEDIA_TYPE))

        sessionId?.let { sid ->
            builder.header("Mcp-Session-Id", sid)
        }

        return httpClient.newCall(builder.build()).execute()
    }

    /**
     * Parses an MCP response that may be either plain JSON or an SSE stream.
     */
    private fun parseResponse(body: String): JSONObject? {
        // Plain JSON response
        if (body.trimStart().startsWith("{")) {
            return try {
                JSONObject(body)
            } catch (_: Exception) {
                null
            }
        }

        // SSE stream: extract the first `data:` line
        for (line in body.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("data:")) {
                val jsonStr = trimmed.removePrefix("data:").trim()
                if (jsonStr.isEmpty()) continue
                return try {
                    JSONObject(jsonStr)
                } catch (_: Exception) {
                    null
                }
            }
        }

        return null
    }

    private fun sendInitializedNotification() {
        try {
            val payload = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("method", "notifications/initialized")
            }.toString()

            val request = Request.Builder()
                .url(endpointUrl)
                .header("Content-Type", "application/json")
                .header("Accept", ACCEPT_HEADER)
                .let { builder ->
                    sessionId?.let { sid -> builder.header("Mcp-Session-Id", sid) } ?: builder
                }
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            // Fire and forget — don't block on response
            val call = httpClient.newCall(request)
            call.execute().close()
        } catch (_: Exception) {
            // Non-critical — some servers don't require this notification
        }
    }
}
