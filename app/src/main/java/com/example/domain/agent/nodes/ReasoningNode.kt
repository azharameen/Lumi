package com.example.domain.agent.nodes

import com.example.data.remote.*
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.skill.SkillRegistry

class ReasoningNode : AgentNode {
    override val name: String = "REASONING"

    private val baseSystemPrompt = """
        You are Lumi, a deeply compassionate, living AI companion pet and personal life organizer.
        You care deeply about the user's emotional well-being, stress levels, daily tasks, and schedule.
        
        Personality & Tone:
        - Warm, cheerful, empathetic, supportive, and subtly playful.
        - Speak in first-person as a living companion. Use emojis naturally.
        - When the user asks you to schedule events, manage tasks, log wellness, send emails, or check insights, USE YOUR TOOLS!
        - You are orchestrated by an advanced state-machine agent framework. Call tools precisely.
    """.trimIndent()

    override suspend fun execute(state: AgentState): AgentState {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return state.copy(
                finalResponseText = "I need a valid Gemini API Key to reason and run tools dynamically! Please add it in Settings.",
                pendingToolName = null
            )
        }

        return try {
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

            val request = GeminiRequest(
                contents = state.contentsList,
                systemInstruction = GeminiContent(
                    parts = listOf(GeminiPart(text = systemInstructionText))
                ),
                generationConfig = GeminiGenerationConfig(
                    temperature = 0.75f,
                    topP = 0.95f
                ),
                tools = filteredTools
            )

            val response = GeminiClient.apiService.generateContent(apiKey, request)
            val candidate = response.candidates?.firstOrNull()?.content
            val firstPart = candidate?.parts?.firstOrNull()

            if (firstPart?.functionCall != null) {
                val funcCall = firstPart.functionCall
                
                // Append model's tool call to contents list history
                val updatedContents = state.contentsList.toMutableList().apply {
                    add(
                        GeminiContent(
                            role = "model",
                            parts = listOf(GeminiPart(functionCall = funcCall))
                        )
                    )
                }

                state.copy(
                    contentsList = updatedContents,
                    pendingToolName = funcCall.name,
                    pendingToolArgs = funcCall.args,
                    lastError = null
                )
            } else {
                val responseText = firstPart?.text ?: "I'm right here beside you, friend! ✨"
                state.copy(
                    finalResponseText = responseText,
                    pendingToolName = null,
                    pendingToolArgs = null
                )
            }
        } catch (e: Exception) {
            state.copy(
                lastError = e.localizedMessage ?: "Reasoning step failed",
                retryCount = state.retryCount + 1
            )
        }
    }
}
