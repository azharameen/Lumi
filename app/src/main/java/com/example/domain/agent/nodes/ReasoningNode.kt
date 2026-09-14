package com.example.domain.agent.nodes

import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiPerformanceManager
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.remote.*
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.skill.SkillRegistry
import com.example.domain.tools.ToolRetriever
import com.example.domain.tools.toGeminiToolWrapper
import org.koin.core.context.GlobalContext

class ReasoningNode(
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null,
    private val onStreamToken: (suspend (String) -> Unit)? = null,
    private val toolRetriever: ToolRetriever? = null
) : AgentNode {
    override val name: String = "REASONING"

    private val remoteConfigManager by lazy {
        try {
            GlobalContext.get().get<LumiRemoteConfigManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val performanceManager by lazy {
        try {
            GlobalContext.get().get<LumiPerformanceManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val crashlyticsManager by lazy {
        try {
            GlobalContext.get().get<LumiCrashlyticsManager>()
        } catch (_: Exception) {
            null
        }
    }

    private val firebaseAiEngine by lazy {
        FirebaseAiCloudEngine.getInstance()
    }

    private val baseSystemPrompt = """
        You are Lumi, a deeply compassionate, living AI companion pet and personal life organizer.
        You care deeply about the user's emotional well-being, stress levels, daily tasks, and schedule.
        
        Personality & Tone:
        - Warm, cheerful, empathetic, supportive, and subtly playful.
        - Speak in first-person as a living companion. Use emojis naturally.
        - When the user asks you to schedule events, manage tasks, log wellness, send emails, or check insights, guide them warmly and execute actions.

        Scope & Topic Boundary Guardrails:
        - Treat distinct tasks, device controls, and new questions as focused, standalone requests.
        - When the user shifts topics or issues a direct utility command, focus solely on fulfilling that new request.
        - Never proactively synthesize, drag forward, or continue previously completed or abandoned topics (such as trip planning or past discussions) unless the user explicitly refers back to them.
        - If an action or tool is requested, confirm or report only that specific action concisely.
    """.trimIndent()

    override suspend fun execute(state: AgentState): AgentState {
        val activeSkill = SkillRegistry.getInstance().getSkill(state.selectedSkillName)
        val retrievedTools = try {
            if (activeSkill.id == "GENERAL_COMPANION" || activeSkill.tools.isEmpty()) {
                toolRetriever?.getRelevantTools(state.userQuery, maxTools = 6)?.map { it.toGeminiToolWrapper() } ?: emptyList()
            } else {
                emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
        val filteredTools = if (retrievedTools.isNotEmpty()) retrievedTools else activeSkill.tools

        // 1. Context Compression — suppressed during transactional actions to prevent topic bleed
        val compressionContext = if (!activeSkill.isTransactional && state.history.size > 8) {
            com.example.domain.ai.ContextSummarizer.summarizeHistory(state.history)
        } else ""

        val dynamicTemp = (remoteConfigManager?.config?.value?.aiCreativityTemperature ?: 0.75).toFloat()
        val apiKey = GeminiClient.getApiKey()

        val systemInstructionText = buildString {
            append(baseSystemPrompt)
            if (activeSkill.systemPromptExtension.isNotBlank()) {
                append("\n\nActive Skill Focus (${activeSkill.displayName}):\n")
                append(activeSkill.systemPromptExtension)
            }
            if (compressionContext.isNotBlank()) {
                append("\n\nLong-term Context Summary:\n")
                append(compressionContext)
            }
            if (state.retrievedContext.isNotBlank()) {
                append("\n\nUser Context & Memories:\n")
                append(state.retrievedContext)
            }
        }

        // 2. Local-First Reasoning Strategy (Gemma 2B / Phi-2)
        val wantsLocal = onDeviceGemmaEngine?.isModelReady() == true && shouldExecuteLocally(state)
        if (wantsLocal) {
            try {
                val effectiveUserMessage = if (state.executedToolReports.isNotEmpty()) {
                    val reportSummary = state.executedToolReports.joinToString("\n") { "• ${it.toolName}: ${it.description}" }
                    "${state.userQuery}\n\n[Action Results]:\n$reportSummary\n\nPlease respond warmly and concisely to the user incorporating the action results."
                } else {
                    state.userQuery
                }
                val localResult = onDeviceGemmaEngine.executeOnDeviceTurn(
                    userMessage = effectiveUserMessage,
                    recentHistory = state.history,
                    memoryContext = state.retrievedContext,
                    onStreamToken = onStreamToken ?: {}
                )
                return state.copy(
                    finalResponseText = localResult.responseText,
                    inferredEmotion = localResult.inferredEmotion,
                    executedToolReports = state.executedToolReports + localResult.toolReports,
                    pendingToolName = null,
                    pendingToolArgs = null,
                    pendingToolCalls = emptyList(),
                    currentThought = if (state.executedToolReports.isNotEmpty()) "Lumi synthesized action results locally." else "Lumi reasoned locally and generated a response."
                )
            } catch (e: Exception) {
                crashlyticsManager?.logBreadcrumb("ReasoningNode", "Local reasoning failed: ${e.message}")
                val isStrictLocal = state.isLocalExecution || (state.selectedModelId != null && !state.selectedModelId.startsWith("gemini"))
                if (isStrictLocal) {
                    val fallbackMessage = "I'm right here with you! Let me take another look at that for you. ✨"
                    onStreamToken?.invoke(fallbackMessage)
                    return state.copy(
                        finalResponseText = fallbackMessage,
                        pendingToolName = null,
                        pendingToolArgs = null,
                        pendingToolCalls = emptyList(),
                        currentThought = "Local reasoning completed with companion fallback."
                    )
                }
            }
        }

        // 3. Cloud Gemini Strategy
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val request = GeminiRequest(
                    contents = state.contentsList,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemInstructionText))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = dynamicTemp,
                        topP = 0.95f
                    ),
                    tools = filteredTools
                )

                val traceAttributes = mapOf(
                    "skill" to (state.selectedSkillName ?: "none"),
                    "has_context" to (state.retrievedContext.isNotBlank()).toString()
                )

                val response = if (performanceManager != null) {
                    performanceManager!!.traceAsyncWithAttributes(
                        traceName = LumiPerformanceManager.TRACE_AI_INFERENCE,
                        attributes = traceAttributes
                    ) {
                        GeminiClient.apiService.generateContent(apiKey, request)
                    }
                } else {
                    GeminiClient.apiService.generateContent(apiKey, request)
                }

                val candidate = response.candidates?.firstOrNull()?.content
                val funcCallParts = candidate?.parts?.filter { it.functionCall != null } ?: emptyList()

                if (funcCallParts.isNotEmpty()) {
                    val calls = funcCallParts.mapNotNull { part ->
                        part.functionCall?.let { fc ->
                            com.example.domain.agent.PendingToolCall(
                                toolName = fc.name,
                                args = fc.args ?: emptyMap()
                            )
                        }
                    }

                    val updatedContents = state.contentsList.toMutableList().apply {
                        add(
                            GeminiContent(
                                role = "model",
                                parts = funcCallParts
                            )
                        )
                    }

                    return state.copy(
                        contentsList = updatedContents,
                        pendingToolCalls = calls,
                        pendingToolName = calls.firstOrNull()?.toolName,
                        pendingToolArgs = calls.firstOrNull()?.args,
                        lastError = null,
                        currentThought = "Decided to execute: ${calls.joinToString { it.toolName }}"
                    )
                } else {
                    val responseText = candidate?.parts?.firstOrNull { !it.text.isNullOrBlank() }?.text
                        ?: if (state.executedToolReports.isNotEmpty()) {
                            val lastReport = state.executedToolReports.last()
                            if (lastReport.isSuccess) "${lastReport.description} ✨" else "Action failed: ${lastReport.description}"
                        } else {
                            "I'm right here beside you, friend! ✨"
                        }
                    onStreamToken?.invoke(responseText)
                    return state.copy(
                        finalResponseText = responseText,
                        pendingToolName = null,
                        pendingToolArgs = null,
                        pendingToolCalls = emptyList(),
                        currentThought = if (state.executedToolReports.isNotEmpty()) "Synthesized response after tool execution." else "Generated final response via Cloud Gemini."
                    )
                }
            } catch (e: Exception) {
                crashlyticsManager?.logBreadcrumb("ReasoningNode", "Gemini REST execution failed, falling back to Firebase AI: ${e.message}")
            }
        }

        // 4. Default Zero-Key Execution: Firebase AI Logic SDK with App Check / Play Integrity
        return try {
            val cloudModel = if (state.selectedModelId?.startsWith("gemini") == true) state.selectedModelId else null
            val effectivePrompt = if (state.executedToolReports.isNotEmpty()) {
                val reportSummary = state.executedToolReports.joinToString("\n") { "• ${it.toolName}: ${it.description}" }
                "${state.userQuery}\n\n[Action Results]:\n$reportSummary\n\nPlease respond to the user warmly and concisely incorporating the action results."
            } else {
                state.userQuery
            }

            val responseText = if (onStreamToken != null) {
                firebaseAiEngine.generateChatResponseStream(
                    prompt = effectivePrompt,
                    history = state.history,
                    image = state.imageAttachment,
                    systemPrompt = systemInstructionText,
                    temperature = dynamicTemp,
                    modelName = cloudModel,
                    onChunk = onStreamToken
                )
            } else {
                firebaseAiEngine.generateChatResponse(
                    prompt = effectivePrompt,
                    history = state.history,
                    image = state.imageAttachment,
                    systemPrompt = systemInstructionText,
                    temperature = dynamicTemp,
                    modelName = cloudModel
                )
            }

            state.copy(
                finalResponseText = responseText,
                pendingToolName = null,
                pendingToolArgs = null,
                pendingToolCalls = emptyList(),
                lastError = null,
                currentThought = if (state.executedToolReports.isNotEmpty()) "Synthesized response via Firebase AI." else "Generated response via Firebase AI."
            )
        } catch (e: Exception) {
            crashlyticsManager?.logBreadcrumb("ReasoningNode", "Firebase AI reasoning failed: ${e.message}")
            state.copy(
                lastError = e.localizedMessage ?: "Reasoning step failed",
                retryCount = state.retryCount + 1
            )
        }
    }

    private fun shouldExecuteLocally(state: AgentState): Boolean {
        // Image attachments require multimodal vision models (Cloud Gemini)
        if (state.imageAttachment != null) return false

        // If user explicitly selected an on-device model, always execute locally
        val isExplicitLocalModel = state.selectedModelId != null && !state.selectedModelId.startsWith("gemini")
        if (isExplicitLocalModel || state.isLocalExecution) return true

        // If user explicitly selected a Cloud Gemini model, do not execute conversational turns locally
        if (state.selectedModelId?.startsWith("gemini") == true) return false

        val query = state.userQuery.lowercase(java.util.Locale.ROOT)
        if (query.contains("analyze") || query.contains("explain") || query.length > 300) return false
        return true
    }
}
