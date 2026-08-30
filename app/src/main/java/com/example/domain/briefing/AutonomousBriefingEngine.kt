package com.example.domain.briefing

import android.content.Context
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.domain.model.PetStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class BriefingType {
    MORNING,
    AFTERNOON,
    EVENING
}

data class DailyBriefing(
    val type: BriefingType,
    val title: String,
    val greeting: String,
    val dateFormatted: String,
    val highlights: List<String>,
    val motivationalQuote: String,
    val focusGoal: String,
    val recommendedAction: String,
    val recommendedActionType: String, // "BREATHING", "HYDRATE", "TASKS", "SCHEDULE"
    val audioScript: String,
    val generatedAt: Long = System.currentTimeMillis()
)

class AutonomousBriefingEngine(private val context: Context) {

    suspend fun generateBriefing(
        type: BriefingType? = null,
        petStatus: PetStatus,
        petEvolution: PetEvolutionEntity?,
        tasks: List<TaskEntity>,
        events: List<CalendarEventEntity>,
        wellnessLogs: List<WellnessLogEntity>,
        locationCity: String? = null
    ): DailyBriefing = withContext(Dispatchers.IO) {
        val now = Calendar.getInstance()
        val hour = now.get(Calendar.HOUR_OF_DAY)
        val resolvedType = type ?: when {
            hour < 12 -> BriefingType.MORNING
            hour < 18 -> BriefingType.AFTERNOON
            else -> BriefingType.EVENING
        }

        val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        val pendingTasks = tasks.filter { !it.isCompleted }
        val highPriorityTasks = pendingTasks.filter { it.priority.uppercase() == "HIGH" || it.priority.uppercase() == "URGENT" }
        val completedTasksToday = tasks.filter { it.isCompleted }

        val todayEvents = events.filter {
            val eventCal = Calendar.getInstance().apply { timeInMillis = it.startTimeMillis }
            eventCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
                    eventCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)
        }

        val totalWaterGlasses = wellnessLogs.sumOf { it.hydrationCups }
        val petName = petStatus.name
        val petLevel = petEvolution?.level ?: 1
        val bondScore = petEvolution?.bondScore ?: 50

        // Dynamic LLM Synthesis if API available
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are Lumi, a sentient companion AI generating a personalized daily intelligence briefing for the user.
                    Time of day: ${resolvedType.name}
                    Date: $dateString
                    Companion: $petName (Level $petLevel, Bond $bondScore%)
                    Events today: ${todayEvents.map { "${it.title} at ${it.startTimeMillis}" }}
                    Pending Tasks (${pendingTasks.size}): ${pendingTasks.take(5).map { "${it.title} [${it.priority}]" }}
                    Completed Today: ${completedTasksToday.size}
                    Water Logged: $totalWaterGlasses glasses
                    
                    Return ONLY a JSON object:
                    {
                      "title": "Title with emoji",
                      "greeting": "Personalized companion greeting",
                      "highlights": ["highlight 1", "highlight 2", "highlight 3"],
                      "motivationalQuote": "A profound, uplifting quote matching their current workload",
                      "focusGoal": "Clear, single highest-leverage focus objective",
                      "recommendedAction": "Action button text",
                      "recommendedActionType": "BREATHING" or "HYDRATE" or "TASKS" or "SCHEDULE",
                      "audioScript": "Conversational speech script for voice narration"
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(temperature = 0.5f)
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!jsonText.isNullOrBlank()) {
                    val cleanJson = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val jsonObj = JSONObject(cleanJson)

                    val highlightsList = mutableListOf<String>()
                    val highlightsArr = jsonObj.optJSONArray("highlights")
                    if (highlightsArr != null) {
                        for (i in 0 until highlightsArr.length()) {
                            highlightsList.add(highlightsArr.getString(i))
                        }
                    }

                    return@withContext DailyBriefing(
                        type = resolvedType,
                        title = jsonObj.optString("title", "✨ Daily Intelligence Briefing"),
                        greeting = jsonObj.optString("greeting", "Hello from $petName! Ready to make today great!"),
                        dateFormatted = dateString,
                        highlights = if (highlightsList.isNotEmpty()) highlightsList else listOf("Track your tasks and wellness", "Stay focused and hydrated"),
                        motivationalQuote = jsonObj.optString("motivationalQuote", "\"The journey of a thousand miles begins with a single step.\""),
                        focusGoal = jsonObj.optString("focusGoal", "Deep focus on top priority goals"),
                        recommendedAction = jsonObj.optString("recommendedAction", "Start Focus Session"),
                        recommendedActionType = jsonObj.optString("recommendedActionType", "TASKS"),
                        audioScript = jsonObj.optString("audioScript", "Here is your daily update with Lumi.")
                    )
                }
            } catch (e: Exception) {
                return@withContext DailyBriefing(
                    type = resolvedType,
                    title = "⚠️ Briefing Unavailable",
                    greeting = "Please configure your AI API key in Settings.",
                    dateFormatted = dateString,
                    highlights = listOf("Requires AI Engine to synthesize briefing."),
                    motivationalQuote = "Waiting for intelligence uplink...",
                    focusGoal = "Configure API Key",
                    recommendedAction = "Open Settings",
                    recommendedActionType = "TASKS",
                    audioScript = "Please configure your AI API key in Settings."
                )
            }
        }

        // If API key is blank, just return the exact same fallback
        return@withContext DailyBriefing(
            type = resolvedType,
            title = "⚠️ Briefing Unavailable",
            greeting = "Please configure your AI API key in Settings.",
            dateFormatted = dateString,
            highlights = listOf("Requires AI Engine to synthesize briefing."),
            motivationalQuote = "Waiting for intelligence uplink...",
            focusGoal = "Configure API Key",
            recommendedAction = "Open Settings",
            recommendedActionType = "TASKS",
            audioScript = "Please configure your AI API key in Settings."
        )
    }
}
