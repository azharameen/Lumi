package com.example.domain.agent.nodes

import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiPerformanceManager
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.remote.*
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.skill.SkillRegistry
import org.koin.core.context.GlobalContext

class ReasoningNode(
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null
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
    """.trimIndent()

    override suspend fun execute(state: AgentState): AgentState {
        val dynamicTemp = (remoteConfigManager?.config?.value?.aiCreativityTemperature ?: 0.75).toFloat()
        val apiKey = GeminiClient.getApiKey()

        val activeSkill = SkillRegistry.getSkill(state.selectedSkillName)
        val filteredTools = activeSkill.tools

        val systemInstructionText = buildString {
            append(baseSystemPrompt)
            if (activeSkill.systemPromptExtension.isNotBlank()) {
                append("\n\nActive Skill Focus (${activeSkill.displayName}):\n")
                append(activeSkill.systemPromptExtension)
            }
            if (state.retrievedContext.isNotBlank()) {
                append("\n\nUser Context & Memories:\n")
                append(state.retrievedContext)
            }
        }

        // 1. Local-First Reasoning Strategy (Gemma 2B / Phi-2)
        if (onDeviceGemmaEngine?.isModelReady() == true && shouldExecuteLocally(state)) {
            try {
                val localResult = onDeviceGemmaEngine.executeOnDeviceTurn(state.userQuery, state.history)
                return state.copy(
                    finalResponseText = localResult.responseText,
                    inferredEmotion = localResult.inferredEmotion,
                    executedToolReports = state.executedToolReports + localResult.toolReports,
                    pendingToolName = null,
                    pendingToolArgs = null,
                    currentThought = "Lumi reasoned locally and generated a response."
                )
            } catch (e: Exception) {
                crashlyticsManager?.logBreadcrumb("ReasoningNode", "Local reasoning failed, falling back: ${e.message}")
            }
        }

        // 2. Cloud Gemini Strategy
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
                val firstPart = candidate?.parts?.firstOrNull()

                if (firstPart?.functionCall != null) {
                    val funcCall = firstPart.functionCall
                    
                    val updatedContents = state.contentsList.toMutableList().apply {
                        add(
                            GeminiContent(
                                role = "model",
                                parts = listOf(GeminiPart(functionCall = funcCall))
                            )
                        )
                    }

                    return state.copy(
                        contentsList = updatedContents,
                        pendingToolName = funcCall.name,
                        pendingToolArgs = funcCall.args,
                        lastError = null,
                        currentThought = "Decided to execute tool: ${funcCall.name}"
                    )
                } else {
                    val responseText = firstPart?.text ?: "I'm right here beside you, friend! ✨"
                    return state.copy(
                        finalResponseText = responseText,
                        pendingToolName = null,
                        pendingToolArgs = null,
                        currentThought = "Generated final response via Cloud Gemini."
                    )
                }
            } catch (e: Exception) {
                crashlyticsManager?.logBreadcrumb("ReasoningNode", "Gemini REST execution failed, falling back to Firebase AI: ${e.message}")
            }
        }

        // 2. Default Zero-Key Execution: Firebase AI Logic SDK with App Check / Play Integrity
        return try {
            val responseText = firebaseAiEngine.generateChatResponse(
                prompt = state.userQuery,
                history = state.history,
                image = state.imageAttachment,
                systemPrompt = systemInstructionText,
                temperature = dynamicTemp
            )

            state.copy(
                finalResponseText = responseText,
                pendingToolName = null,
                pendingToolArgs = null,
                lastError = null,
                currentThought = "Generated response via Firebase AI."
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
        val query = state.userQuery.lowercase()
        if (state.imageAttachment != null) return false
        if (query.contains("analyze") || query.contains("explain") || query.length > 300) return false
        return true
    }
}
