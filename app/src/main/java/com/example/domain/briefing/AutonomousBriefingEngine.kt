package com.example.domain.briefing

import android.content.Context
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiPerformanceManager
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.data.remote.FirebaseAiCloudEngine
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
import org.koin.core.context.GlobalContext
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

    private val analyticsManager by lazy {
        try {
            GlobalContext.get().get<LumiAnalyticsManager>()
        } catch (_: Exception) {
            LumiAnalyticsManager(context)
        }
    }

    private val crashlyticsManager by lazy {
        try {
            GlobalContext.get().get<LumiCrashlyticsManager>()
        } catch (_: Exception) {
            null
        }
    }

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

        val rcConfig = remoteConfigManager?.config?.value
        val dynamicTip = rcConfig?.companionTipOfTheDay ?: "Take a mindful deep breath whenever you feel overwhelmed."
        val dynamicGreetingPrefix = rcConfig?.welcomeGreeting ?: "Hi! I'm $petName"

        analyticsManager.logScreenView("DailyBriefing_${resolvedType.name}")

        // Dynamic Firebase AI LLM Synthesis (Zero-key cloud intelligence)
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
                Inspirational context: $dynamicTip
                
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

            val jsonText: String = if (performanceManager != null) {
                performanceManager!!.traceAsync<String>(LumiPerformanceManager.TRACE_DAILY_BRIEFING_GEN) {
                    FirebaseAiCloudEngine.getInstance().generateStructuredText(
                        systemInstruction = "You are Lumi's briefing synthesizer. Output strictly valid JSON.",
                        prompt = prompt,
                        temperature = 0.4f
                    )
                }
            } else {
                FirebaseAiCloudEngine.getInstance().generateStructuredText(
                    systemInstruction = "You are Lumi's briefing synthesizer. Output strictly valid JSON.",
                    prompt = prompt,
                    temperature = 0.4f
                )
            }

            if (jsonText.isNotBlank()) {
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
                    greeting = jsonObj.optString("greeting", "$dynamicGreetingPrefix! Ready to make today great!"),
                    dateFormatted = dateString,
                    highlights = if (highlightsList.isNotEmpty()) highlightsList else listOf("Track your tasks and wellness", "Stay focused and hydrated"),
                    motivationalQuote = jsonObj.optString("motivationalQuote", "\"$dynamicTip\""),
                    focusGoal = jsonObj.optString("focusGoal", "Deep focus on top priority goals"),
                    recommendedAction = jsonObj.optString("recommendedAction", "Start Focus Session"),
                    recommendedActionType = jsonObj.optString("recommendedActionType", "TASKS"),
                    audioScript = jsonObj.optString("audioScript", "Here is your daily update with Lumi.")
                )
            }
        } catch (e: Exception) {
            crashlyticsManager?.logBreadcrumb("AutonomousBriefingEngine", "Firebase AI briefing synthesis failed: ${e.message}")
        }

        // Intelligent offline heuristic synthesis with Remote Config fallback
        val defaultHighlights = mutableListOf<String>()
        if (todayEvents.isNotEmpty()) {
            defaultHighlights.add("${todayEvents.size} schedule events planned today")
        } else {
            defaultHighlights.add("Open calendar schedule today")
        }
        if (pendingTasks.isNotEmpty()) {
            defaultHighlights.add("${pendingTasks.size} tasks pending (${highPriorityTasks.size} high priority)")
        } else {
            defaultHighlights.add("All current tasks completed!")
        }
        defaultHighlights.add("Hydration: $totalWaterGlasses glasses logged")

        val timeGreeting = when (resolvedType) {
            BriefingType.MORNING -> "Good morning from $petName!"
            BriefingType.AFTERNOON -> "Good afternoon from $petName!"
            BriefingType.EVENING -> "Good evening from $petName!"
        }

        DailyBriefing(
            type = resolvedType,
            title = when (resolvedType) {
                BriefingType.MORNING -> "🌅 Morning Intelligence Briefing"
                BriefingType.AFTERNOON -> "☀️ Afternoon Pulse"
                BriefingType.EVENING -> "🌙 Evening Wind Down"
            },
            greeting = "$timeGreeting $dynamicGreetingPrefix",
            dateFormatted = dateString,
            highlights = defaultHighlights,
            motivationalQuote = dynamicTip,
            focusGoal = if (highPriorityTasks.isNotEmpty()) "Focus on: ${highPriorityTasks.first().title}" else "Organize upcoming goals and mindful pause",
            recommendedAction = if (totalWaterGlasses < 4) "Log Hydration" else "Start 4-7-8 Breathing",
            recommendedActionType = if (totalWaterGlasses < 4) "HYDRATE" else "BREATHING",
            audioScript = "$timeGreeting Here is your daily summary: You have ${pendingTasks.size} pending tasks and ${todayEvents.size} events on your schedule today. $dynamicTip"
        )
    }
}
