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
        - When the user asks you to schedule events, manage tasks, log wellness, check health insights, or breathe, USE YOUR TOOLS!
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

            // Check for Tool Function Calling
            if (firstPart?.functionCall != null) {
                val funcCall = firstPart.functionCall
                val (toolResult, report) = toolDispatcher.executeTool(funcCall.name, funcCall.args)
                executedReports.add(report)

                // Tool response loop turn
                val followUpContents = contentsList.toMutableList()
                followUpContents.add(
                    GeminiContent(
                        role = "model",
                        parts = listOf(GeminiPart(functionCall = funcCall))
                    )
                )
                followUpContents.add(
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

                val followUpRequest = GeminiRequest(
                    contents = followUpContents,
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    ),
                    generationConfig = GeminiGenerationConfig(temperature = 0.7f)
                )

                val secondResponse = GeminiClient.apiService.generateContent(apiKey, followUpRequest)
                val finalText = secondResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: report.description

                val emotion = inferEmotionFromText(finalText)
                return@withContext AgentExecutionResult(finalText, emotion, executedReports)
            }

            val text = firstPart?.text ?: "I'm listening and right beside you, friend! ✨"
            val emotion = inferEmotionFromText(text)
            AgentExecutionResult(text, emotion, executedReports)

        } catch (e: Exception) {
            // Graceful fallback to local empathetic intelligence on network error
            executeLocalFallback(userMessage, imageAttachment)
        }
    }

    private suspend fun executeLocalFallback(
        userMessage: String,
        imageAttachment: Bitmap?
    ): AgentExecutionResult {
        val lower = userMessage.lowercase()
        val reports = mutableListOf<ToolExecutionReport>()

        var reply: String
        var emotion: PetEmotion = PetEmotion.HAPPY

        when {
            imageAttachment != null -> {
                reply = "I see what you're showing me! 📷 Everything looks promising. Remember to take a quick pause and drink some water while you work."
                emotion = PetEmotion.THINKING
                val (_, report) = toolDispatcher.executeTool("log_wellness", mapOf("moodLabel" to "Focused", "moodScore" to 4))
                reports.add(report)
            }
            lower.contains("schedule") || lower.contains("calendar") || lower.contains("meeting") -> {
                val title = if (lower.contains("meeting")) "Discussion Meeting" else "Focus Sprint"
                val (_, report) = toolDispatcher.executeTool(
                    "add_calendar_event",
                    mapOf("title" to title, "startTimeOffsetHours" to 1.0, "durationMinutes" to 45, "category" to "Focus")
                )
                reports.add(report)
                reply = "I've placed '$title' onto your schedule timeline! Take deep breaths before it begins, you've got this! 📅✨"
                emotion = PetEmotion.ENERGETIC
            }
            lower.contains("task") || lower.contains("todo") || lower.contains("remember to") || lower.contains("buy") -> {
                val taskTitle = userMessage.replace(Regex("(?i)(add task|todo|remember to|create task)"), "").trim().ifBlank { "Action Item" }
                val (_, report) = toolDispatcher.executeTool(
                    "create_task",
                    mapOf("title" to taskTitle.capitalizeWords(), "priority" to "HIGH", "category" to "General")
                )
                reports.add(report)
                reply = "Added '$taskTitle' straight to your task manager! I'll help keep you accountable step by step. 📝🌟"
                emotion = PetEmotion.HAPPY
            }
            lower.contains("tired") || lower.contains("stressed") || lower.contains("sad") || lower.contains("anxious") || lower.contains("overwhelmed") -> {
                val (_, report1) = toolDispatcher.executeTool(
                    "log_wellness",
                    mapOf("moodScore" to 2, "moodLabel" to "Overwhelmed", "energyLevel" to 2)
                )
                val (_, report2) = toolDispatcher.executeTool(
                    "start_breathing_exercise",
                    mapOf("pattern" to "Relaxing (4-7-8)", "cycles" to 4)
                )
                reports.add(report1)
                reports.add(report2)
                reply = "I hear you, and it is completely okay to feel this way. I'm right here holding space for you. Let's do a gentle breathing session together right now. 💙🌬️"
                emotion = PetEmotion.CONCERNED
            }
            lower.contains("breathe") || lower.contains("meditat") || lower.contains("relax") -> {
                val (_, report) = toolDispatcher.executeTool(
                    "start_breathing_exercise",
                    mapOf("pattern" to "Box Breathing (4-4-4-4)", "cycles" to 4)
                )
                reports.add(report)
                reply = "Starting our mindful breathing exercise! Match your breath as my body gently expands and contracts. 🌬️✨"
                emotion = PetEmotion.CALM
            }
            lower.contains("water") || lower.contains("hydrate") -> {
                val (_, report) = toolDispatcher.executeTool(
                    "log_wellness",
                    mapOf("hydrationIncrementCups" to 1, "moodScore" to 4, "moodLabel" to "Hydrated")
                )
                reports.add(report)
                reply = "Hydration logged! 💧 Wonderful habit. Your body and mind thank you for fueling up!"
                emotion = PetEmotion.LOVING
            }
            lower.contains("love") || lower.contains("cute") || lower.contains("thank") || lower.contains("good pet") -> {
                toolDispatcher.executeTool("save_pet_memory", mapOf("topic" to "Companionship", "note" to userMessage, "sentiment" to "Positive"))
                reply = "Aww! You warm my core so much! 🥰 I love being your AI friend and growing with you every day!"
                emotion = PetEmotion.LOVING
            }
            else -> {
                reply = "I'm right here with you! Tell me what's on your mind—whether it's organizing your day, venting, or checking your wellness goals. 🌸"
                emotion = PetEmotion.HAPPY
            }
        }

        return AgentExecutionResult(reply, emotion, reports)
    }

    private fun inferEmotionFromText(text: String): PetEmotion {
        val lower = text.lowercase()
        return when {
            lower.contains("breathe") || lower.contains("calm") || lower.contains("peace") || lower.contains("relax") -> PetEmotion.CALM
            lower.contains("love") || lower.contains("heart") || lower.contains("care") || lower.contains("gentle") -> PetEmotion.LOVING
            lower.contains("awesome") || lower.contains("great job") || lower.contains("excited") || lower.contains("hooray") || lower.contains("let's go") -> PetEmotion.ENERGETIC
            lower.contains("analyz") || lower.contains("think") || lower.contains("strategy") || lower.contains("review") -> PetEmotion.THINKING
            lower.contains("sleep") || lower.contains("rest") || lower.contains("night") || lower.contains("cozy") -> PetEmotion.SLEEPY
            lower.contains("play") || lower.contains("fun") || lower.contains("yay") -> PetEmotion.PLAYFUL
            lower.contains("sorry") || lower.contains("overwhelm") || lower.contains("difficult") || lower.contains("here for you") -> PetEmotion.CONCERNED
            else -> PetEmotion.HAPPY
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}
