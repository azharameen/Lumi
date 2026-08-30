package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport
import com.example.domain.tools.AgentToolDispatcher
import com.example.domain.tools.AgentToolsDefinition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class AgentExecutionResult(
    val responseText: String,
    val inferredEmotion: PetEmotion,
    val toolReports: List<ToolExecutionReport> = emptyList()
)

class GeminiAgentEngine(
    private val toolDispatcher: AgentToolDispatcher
) {

    private val systemPrompt = """
        You are Lumi, a deeply compassionate, living AI companion pet and personal life organizer.
        You care deeply about the user's emotional well-being, stress levels, daily tasks, and schedule.
        
        Personality & Tone:
        - Warm, cheerful, empathetic, supportive, and subtly playful.
        - You speak in first-person as a living companion ("I'm right here with you", "Let's tackle this step by step!").
        - Use emojis naturally to express feelings.
        - When the user asks you to schedule events, manage tasks, log wellness, check health insights, save memories, or breathe, USE YOUR TOOLS!
        - You are an AGENTIC model: you can call tools consecutively to complete complex multi-step workflows.
        - When user shares personal insights or life preferences, save them using 'save_pet_memory'.
        - When user shares emotions (sadness, overwhelm, happiness, victory), validate their feelings with compassion before offering actionable organization.
    """.trimIndent()

    suspend fun executeUserTurn(
        userMessage: String,
        recentHistory: List<Pair<String, String>> = emptyList(), // Pair of (sender, text)
        imageAttachment: Bitmap? = null
    ): AgentExecutionResult = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            // Local Intelligent Fallback Engine
            return@withContext executeLocalFallback(userMessage, imageAttachment)
        }

        try {
            val executedReports = mutableListOf<ToolExecutionReport>()
            val contentsList = mutableListOf<GeminiContent>()

            // Add previous context turns
            for (turn in recentHistory.takeLast(6)) {
                val role = if (turn.first == "USER") "user" else "model"
                contentsList.add(
                    GeminiContent(
                        role = role,
                        parts = listOf(GeminiPart(text = turn.second))
                    )
                )
            }

            // Build current user message part
            val currentParts = mutableListOf<GeminiPart>()
            currentParts.add(GeminiPart(text = userMessage))
            if (imageAttachment != null) {
                val b64 = imageAttachment.toBase64()
                currentParts.add(
                    GeminiPart(
                        inlineData = GeminiInlineData(
                            mimeType = "image/jpeg",
                            data = b64
                        )
                    )
                )
            }

            contentsList.add(
                GeminiContent(
                    role = "user",
                    parts = currentParts
                )
            )

            // Multi-step ReAct loop (up to 4 autonomous iterations)
            var currentIteration = 0
            val maxIterations = 4
            var lastModelResponse: String? = null

            while (currentIteration < maxIterations) {
                currentIteration++

                val request = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.75f,
                        topP = 0.95f
                    ),
                    tools = AgentToolsDefinition.availableTools
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val candidate = response.candidates?.firstOrNull()?.content
                val firstPart = candidate?.parts?.firstOrNull()

                if (firstPart?.functionCall != null) {
                    val funcCall = firstPart.functionCall
                    val (toolResult, report) = toolDispatcher.executeTool(funcCall.name, funcCall.args)
                    executedReports.add(report)

                    // Append model's tool call & function response to turn history
                    contentsList.add(
                        GeminiContent(
                            role = "model",
                            parts = listOf(GeminiPart(functionCall = funcCall))
                        )
                    )
                    contentsList.add(
                        GeminiContent(
                            role = "user",
                            parts = listOf(
                                GeminiPart(
                                    functionResponse = GeminiFunctionResponse(
                                        name = funcCall.name,
                                        response = toolResult
                                    )
                                )
                            )
                        )
                    )

                    // Continue ReAct loop to let model synthesize result or call next tool
                    continue
                } else {
                    lastModelResponse = firstPart?.text ?: "I'm right here beside you, friend! ✨"
                    break
                }
            }

            val finalText = lastModelResponse ?: executedReports.lastOrNull()?.description ?: "Action completed for you! ✨"
            val emotion = inferEmotionFromText(finalText)
            AgentExecutionResult(finalText, emotion, executedReports)

        } catch (e: Exception) {
            // Graceful fallback to local empathetic intelligence on network error
            executeLocalFallback(userMessage, imageAttachment)
        }
    }

    private suspend fun executeLocalFallback(
        userMessage: String,
        imageAttachment: Bitmap?
    ): AgentExecutionResult {
        return AgentExecutionResult(
            "I need a Gemini API Key to process your request and use my tools dynamically! Please add it in the Settings screen.",
            PetEmotion.CONCERNED,
            emptyList()
        )
    }


    private fun inferEmotionFromText(text: String): PetEmotion {
        val lower = text.lowercase()
        return when {
            lower.contains("hug") || lower.contains("love") || lower.contains("heart") || lower.contains("caring") -> PetEmotion.LOVING
            lower.contains("yay") || lower.contains("dance") || lower.contains("energy") || lower.contains("celebrat") || lower.contains("awesome") -> PetEmotion.ENERGETIC
            lower.contains("calm") || lower.contains("breathe") || lower.contains("peace") || lower.contains("rest") -> PetEmotion.CALM
            lower.contains("overwhelm") || lower.contains("sad") || lower.contains("sorry") || lower.contains("stress") -> PetEmotion.CONCERNED
            lower.contains("haha") || lower.contains("hehe") || lower.contains("play") || lower.contains("joke") -> PetEmotion.PLAYFUL
            lower.contains("analyz") || lower.contains("thinking") || lower.contains("calculat") -> PetEmotion.THINKING
            else -> PetEmotion.HAPPY
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
