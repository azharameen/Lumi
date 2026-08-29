package com.example.domain.briefing

import android.content.Context
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.domain.model.PetStatus
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

    fun generateBriefing(
        type: BriefingType? = null,
        petStatus: PetStatus,
        petEvolution: PetEvolutionEntity?,
        tasks: List<TaskEntity>,
        events: List<CalendarEventEntity>,
        wellnessLogs: List<WellnessLogEntity>,
        locationCity: String? = null
    ): DailyBriefing {
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

        val highlights = mutableListOf<String>()
        val scriptBuilder = StringBuilder()

        val title: String
        val greeting: String
        val motivationalQuote: String
        val focusGoal: String
        val recommendedAction: String
        val recommendedActionType: String

        when (resolvedType) {
            BriefingType.MORNING -> {
                title = "🌅 Morning Intelligence Briefing"
                greeting = "Good morning! $petName is energized and ready for Level $petLevel milestones."
                scriptBuilder.append("Good morning! Here is your Lumi daily intelligence briefing for $dateString. ")

                if (todayEvents.isNotEmpty()) {
                    highlights.add("📅 ${todayEvents.size} calendar event(s) scheduled today.")
                    val firstEvent = todayEvents.first()
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    highlights.add("⏰ Next up: \"${firstEvent.title}\" at ${timeFormat.format(Date(firstEvent.startTimeMillis))}.")
                    scriptBuilder.append("You have ${todayEvents.size} event scheduled today, starting with ${firstEvent.title}. ")
                } else {
                    highlights.add("📅 Clear calendar day ahead — great for uninterrupted deep work.")
                    scriptBuilder.append("Your calendar is wide open today, perfect for deep uninterrupted focus. ")
                }

                if (pendingTasks.isNotEmpty()) {
                    highlights.add("⚡ ${pendingTasks.size} pending task(s) on your radar.")
                    if (highPriorityTasks.isNotEmpty()) {
                        highlights.add("🔥 Top priority: \"${highPriorityTasks.first().title}\".")
                        scriptBuilder.append("You have ${pendingTasks.size} pending tasks, with top priority on ${highPriorityTasks.first().title}. ")
                    } else {
                        scriptBuilder.append("You have ${pendingTasks.size} pending tasks to tackle. ")
                    }
                } else {
                    highlights.add("✨ Zero pending tasks. Inbox zero state!")
                    scriptBuilder.append("Your task queue is completely clear. ")
                }

                highlights.add("💧 Hydration target: 8 glasses (Logged so far: $totalWaterGlasses).")
                scriptBuilder.append("Remember to hydrate early. Let's make today productive and calm!")

                motivationalQuote = "\"Small daily steps compounded over time create extraordinary transformations.\""
                focusGoal = if (highPriorityTasks.isNotEmpty()) "Conquer \"${highPriorityTasks.first().title}\"" else "Maintain momentum and high energy"
                recommendedAction = if (totalWaterGlasses == 0) "Log 1st Glass of Water" else "Review Top Priority Task"
                recommendedActionType = if (totalWaterGlasses == 0) "HYDRATE" else "TASKS"
            }

            BriefingType.AFTERNOON -> {
                title = "☀️ Midday Energy & Progress Check"
                greeting = "Keep going! $petName is cheering you on through the afternoon."
                scriptBuilder.append("Hey there! Here is your midday pulse check. ")

                highlights.add("✅ Completed today: ${completedTasksToday.size} task(s).")
                highlights.add("⏳ Remaining tasks: ${pendingTasks.size}.")
                scriptBuilder.append("You've completed ${completedTasksToday.size} tasks so far with ${pendingTasks.size} remaining. ")

                if (todayEvents.isNotEmpty()) {
                    highlights.add("📆 Remaining afternoon schedule: ${todayEvents.size} block(s).")
                }

                highlights.add("💧 Water tracker: $totalWaterGlasses / 8 glasses logged.")
                scriptBuilder.append("You have logged $totalWaterGlasses glasses of water. Take a deep breath and stay hydrated!")

                motivationalQuote = "\"Focus is a muscle. Take a 2-minute reset to sharpen your mind.\""
                focusGoal = "Power through remaining afternoon blocks"
                recommendedAction = "Take 2-Min Reset Breathing"
                recommendedActionType = "BREATHING"
            }

            BriefingType.EVENING -> {
                title = "🌙 Evening Reflection & Wind-Down"
                greeting = "Great work today! $petName is cozy and resting."
                scriptBuilder.append("Good evening! Time for your daily reflection and wind-down summary. ")

                highlights.add("🎉 Total tasks crushed today: ${completedTasksToday.size}.")
                highlights.add("🌟 Companion bond: $bondScore% (Level $petLevel).")
                scriptBuilder.append("You crushed ${completedTasksToday.size} tasks today. Your Lumi companion bond is at $bondScore percent. ")

                if (pendingTasks.isNotEmpty()) {
                    highlights.add("📋 ${pendingTasks.size} task(s) rolled over smoothly to tomorrow.")
                }

                highlights.add("💧 Final hydration count: $totalWaterGlasses / 8 glasses.")
                scriptBuilder.append("Outstanding effort today. Prepare for a restful night and recharge your energy for tomorrow.")

                motivationalQuote = "\"Rest is not a reward for work; it is a vital part of the creative process.\""
                focusGoal = "Unplug, relax, and restore your mind"
                recommendedAction = "Start 4-7-8 Bedtime Relaxation"
                recommendedActionType = "BREATHING"
            }
        }

        return DailyBriefing(
            type = resolvedType,
            title = title,
            greeting = greeting,
            dateFormatted = dateString,
            highlights = highlights,
            motivationalQuote = motivationalQuote,
            focusGoal = focusGoal,
            recommendedAction = recommendedAction,
            recommendedActionType = recommendedActionType,
            audioScript = scriptBuilder.toString()
        )
    }
}
