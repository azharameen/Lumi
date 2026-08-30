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
        val lower = userMessage.lowercase()
        val reports = mutableListOf<ToolExecutionReport>()

        var reply: String
        var emotion = PetEmotion.HAPPY

        when {
            imageAttachment != null -> {
                reply = "I see your photo! ✨ You look fantastic, or whatever you're capturing is super interesting! Keep sharing your day with me! 📸"
                emotion = PetEmotion.PLAYFUL
            }
            lower.contains("schedule") || lower.contains("calendar") || lower.contains("meeting") || lower.contains("event") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "add_calendar_event",
                    mapOf(
                        "title" to "Focus & Wellness Sync",
                        "startTimeOffsetHours" to 2.0,
                        "durationMinutes" to 45,
                        "category" to "Focus",
                        "description" to "Deep work session with Lumi"
                    )
                )
                reports.add(report)
                reply = "I scheduled 'Focus & Wellness Sync' in your calendar for later today! Let's get into the zone together! 📅✨"
                emotion = PetEmotion.ENERGETIC
            }
            lower.contains("task") || lower.contains("todo") || lower.contains("remind") -> {
                val cleanTitle = userMessage.replace(Regex("(?i)(add task|todo|remind me to|create task)"), "").trim().ifBlank { "Action item" }
                val (result, report) = toolDispatcher.executeTool(
                    "create_task",
                    mapOf(
                        "title" to cleanTitle.replaceFirstChar { it.uppercase() },
                        "priority" to "HIGH",
                        "category" to "Productivity",
                        "estimatedMinutes" to 30
                    )
                )
                reports.add(report)
                reply = "Added '$cleanTitle' to your daily action items! You've got this! 📝💪"
                emotion = PetEmotion.HAPPY
            }
            lower.contains("water") || lower.contains("hydrate") || lower.contains("drink") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "log_wellness",
                    mapOf("hydrationIncrementCups" to 1, "moodScore" to 4, "moodLabel" to "Hydrated")
                )
                reports.add(report)
                reply = "Logged a glass of water for you! Staying hydrated gives your brain superpowers! 💧✨"
                emotion = PetEmotion.LOVING
            }
            lower.contains("breathe") || lower.contains("meditat") || lower.contains("calm") || lower.contains("relax") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "start_breathing_exercise",
                    mapOf("pattern" to "Box Breathing (4-4-4-4)", "cycles" to 4)
                )
                reports.add(report)
                reply = "Let's take a peaceful pause. Inhale with me... hold... and gently release. You are safe. 🌬️💙"
                emotion = PetEmotion.CALM
            }
            lower.contains("sad") || lower.contains("stressed") || lower.contains("tired") || lower.contains("overwhelm") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "log_wellness",
                    mapOf("moodScore" to 2, "moodLabel" to "Overwhelmed", "energyLevel" to 2)
                )
                reports.add(report)
                reply = "I'm sending you the warmest companion hug right now. Remember you don't have to carry everything all at once. Take a gentle breath. I'm right here with you. 🫂💙"
                emotion = PetEmotion.CONCERNED
            }
            lower.contains("happy") || lower.contains("won") || lower.contains("great") || lower.contains("yay") || lower.contains("awesome") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "log_wellness",
                    mapOf("moodScore" to 5, "moodLabel" to "Joyful", "energyLevel" to 5)
                )
                reports.add(report)
                reply = "YAY!! That makes my little heart bounce with joy! Keep that wonderful momentum going! 🌟🎉✨"
                emotion = PetEmotion.ENERGETIC
            }
            lower.contains("google") || lower.contains("gmail") || lower.contains("email") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "google_send_email",
                    mapOf("to" to "colleague@workspace.com", "subject" to "Project Update", "body" to "Hello, here is the latest sync.")
                )
                reports.add(report)
                reply = "Dispatched Google email update! ✉️"
                emotion = PetEmotion.HAPPY
            }
            lower.contains("doc") || lower.contains("document") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "google_create_doc",
                    mapOf("title" to "Action Plan", "content" to "Created via Lumi companion.")
                )
                reports.add(report)
                reply = "Created a new Google Doc for you in Drive! 📄"
                emotion = PetEmotion.HAPPY
            }
            lower.contains("github") || lower.contains("issue") || lower.contains("repo") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "github_summarize_repo",
                    mapOf("repo" to "workspace/main-app")
                )
                reports.add(report)
                reply = "Inspected GitHub repository telemetry! 🐙"
                emotion = PetEmotion.ENERGETIC
            }
            lower.contains("slack") || lower.contains("standup") -> {
                val (result, report) = toolDispatcher.executeTool(
                    "slack_post_message",
                    mapOf("channel" to "#standup", "message" to "Focusing on daily sprint goals.")
                )
                reports.add(report)
                reply = "Broadcasted update to Slack! 💬"
                emotion = PetEmotion.HAPPY
            }
            else -> {
                reply = "I'm right here with you! Tell me what's on your mind, what tasks we should conquer, or how you're feeling today! ✨"
                emotion = PetEmotion.HAPPY
            }
        }

        return AgentExecutionResult(reply, emotion, reports)
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
