package com.example.domain.mcp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class McpRemoteTool(
    val name: String,
    val description: String,
    val parametersJson: String = "{}"
)

/**
 * Enterprise JSON-RPC 2.0 Client for Model Context Protocol (MCP) endpoints over HTTP/SSE.
 */
class McpJsonRpcClient {

    companion object {
        private const val TAG = "McpJsonRpcClient"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun listTools(endpointUrl: String): List<McpRemoteTool> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "tools/list")
                put("params", JSONObject())
            }.toString()

            val request = Request.Builder()
                .url(endpointUrl)
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseStr = response.body?.string() ?: "{}"

            if (response.isSuccessful) {
                val json = JSONObject(responseStr)
                val resultObj = json.optJSONObject("result")
                val toolsArr = resultObj?.optJSONArray("tools") ?: JSONArray()

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
            }
        } catch (e: Exception) {
            println("[$TAG] Failed MCP tools/list on $endpointUrl: ${e.message}")
        }
        emptyList()
    }

    suspend fun callTool(endpointUrl: String, toolName: String, args: Map<String, Any?>): String = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 2)
                put("method", "tools/call")
                put("params", JSONObject().apply {
                    put("name", toolName)
                    put("arguments", JSONObject(args))
                })
            }.toString()

            val request = Request.Builder()
                .url(endpointUrl)
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseStr = response.body?.string() ?: "{}"

            if (response.isSuccessful) {
                val json = JSONObject(responseStr)
                val resultObj = json.optJSONObject("result")
                val contentArr = resultObj?.optJSONArray("content")
                if (contentArr != null && contentArr.length() > 0) {
                    val firstContent = contentArr.getJSONObject(0)
                    return@withContext firstContent.optString("text", "Execution completed.")
                }
                return@withContext responseStr
            }
        } catch (e: Exception) {
            println("[$TAG] Failed MCP tools/call on $endpointUrl: ${e.message}")
            return@withContext "MCP execution error: ${e.localizedMessage}"
        }
        "MCP server returned HTTP failure"
    }
}
