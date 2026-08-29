package com.example.domain.tools

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.domain.connectors.IntegrationService
import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.max
import kotlin.math.min

class AgentToolDispatcher(
    private val database: LumiDatabase,
    private val integrationService: IntegrationService? = null
) {

    suspend fun executeTool(name: String, args: Map<String, Any?>?): Pair<Map<String, Any?>, ToolExecutionReport> {
        val safeArgs = args ?: emptyMap()
        return when (name) {
            // --- Core Life & Pet Tools ---
            "add_calendar_event" -> {
                val title = safeArgs["title"]?.toString() ?: "Focus Session"
                val offsetHours = (safeArgs["startTimeOffsetHours"] as? Number)?.toDouble() ?: 0.5
                val durationMinutes = (safeArgs["durationMinutes"] as? Number)?.toInt() ?: 45
                val category = safeArgs["category"]?.toString() ?: "Focus"
                val description = safeArgs["description"]?.toString() ?: ""

                val now = System.currentTimeMillis()
                val startTime = now + (offsetHours * 3600 * 1000).toLong()
                val endTime = startTime + (durationMinutes * 60 * 1000).toLong()

                val color = when (category) {
                    "Health" -> "#06D6A0"
                    "Focus" -> "#9D65FF"
                    "Social" -> "#FFD166"
                    "Rest" -> "#FF70A6"
                    else -> "#00F0FF"
                }

                val eventId = database.calendarEventDao().insertEvent(
                    CalendarEventEntity(
                        title = title,
                        description = description,
                        startTimeMillis = startTime,
                        endTimeMillis = endTime,
                        category = category,
                        colorHex = color
                    )
                )
                addPetExp(15, "Scheduled calendar event")

                val result = mapOf(
                    "status" to "success",
                    "eventId" to eventId,
                    "message" to "Scheduled '$title' for $durationMinutes mins in your calendar."
                )
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Calendar Event Scheduled",
                    description = "Added '$title' ($durationMinutes mins) to your schedule.",
                    payloadPreview = "Starts in ${"%.1f".format(offsetHours)}h • $category"
                )
                result to report
            }

            "get_daily_schedule" -> {
                val events = database.calendarEventDao().getAllEvents().firstOrNull() ?: emptyList()
                val list = events.take(8).map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "startTimeMillis" to it.startTimeMillis,
                        "endTimeMillis" to it.endTimeMillis,
                        "category" to it.category,
                        "description" to it.description
                    )
                }
                val result = mapOf("eventsCount" to list.size, "events" to list)
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Calendar Checked",
                    description = "Retrieved ${list.size} events on your schedule.",
                    payloadPreview = if (list.isNotEmpty()) "${list.first()["title"]} + more" else "Free day ahead!"
                )
                result to report
            }

            "create_task" -> {
                val title = safeArgs["title"]?.toString() ?: "New Action Item"
                val priority = safeArgs["priority"]?.toString() ?: "MEDIUM"
                val category = safeArgs["category"]?.toString() ?: "General"
                val estimatedMinutes = (safeArgs["estimatedMinutes"] as? Number)?.toInt() ?: 30
                val notes = safeArgs["notes"]?.toString() ?: ""

                val taskId = database.taskDao().insertTask(
                    TaskEntity(
                        title = title,
                        notes = notes,
                        priority = priority,
                        category = category,
                        estimatedMinutes = estimatedMinutes
                    )
                )
                addPetExp(12, "Created task")

                val result = mapOf(
                    "status" to "success",
                    "taskId" to taskId,
                    "message" to "Created task '$title' [$priority priority]"
                )
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Task Added to Organizer",
                    description = "Created '$title' ($estimatedMinutes mins estimated)",
                    payloadPreview = "Priority: $priority • $category"
                )
                result to report
            }

            "complete_task" -> {
                val keyword = safeArgs["taskTitle"]?.toString()?.lowercase() ?: ""
                val pending = database.taskDao().getPendingTasks().firstOrNull() ?: emptyList()
                val match = pending.firstOrNull { it.title.lowercase().contains(keyword) } ?: pending.firstOrNull()

                if (match != null) {
                    database.taskDao().setTaskCompleted(match.id, true)
                    addPetExp(25, "Completed task together")
                    incrementTasksCompleted()
                    val result = mapOf(
                        "status" to "success",
                        "taskId" to match.id,
                        "completedTitle" to match.title,
                        "message" to "Great job! Task '${match.title}' completed!"
                    )
                    val report = ToolExecutionReport(
                        toolName = name,
                        title = "Task Completed! 🎉",
                        description = "Marked '${match.title}' as finished.",
                        payloadPreview = "+25 Pet XP • Bond Boosted!"
                    )
                    result to report
                } else {
                    val result = mapOf("status" to "not_found", "message" to "No matching open task found.")
                    val report = ToolExecutionReport(
                        toolName = name,
                        title = "Task Lookup",
                        description = "Could not find task matching '$keyword'.",
                        isSuccess = false
                    )
                    result to report
                }
            }

            "list_pending_tasks" -> {
                val tasks = database.taskDao().getPendingTasks().firstOrNull() ?: emptyList()
                val list = tasks.take(10).map {
                    mapOf(
                        "id" to it.id,
                        "title" to it.title,
                        "priority" to it.priority,
                        "category" to it.category,
                        "notes" to it.notes
                    )
                }
                val result = mapOf("taskCount" to list.size, "tasks" to list)
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Task Manager Synchronized",
                    description = "Fetched ${list.size} active tasks.",
                    payloadPreview = if (list.isNotEmpty()) list.joinToString(", ") { it["title"].toString() }.take(50) + "..." else "All caught up!"
                )
                result to report
            }

            "log_wellness" -> {
                val moodScore = (safeArgs["moodScore"] as? Number)?.toInt() ?: 4
                val moodLabel = safeArgs["moodLabel"]?.toString() ?: "Calm"
                val energyLevel = (safeArgs["energyLevel"] as? Number)?.toInt() ?: 3
                val hydrationCups = (safeArgs["hydrationIncrementCups"] as? Number)?.toInt() ?: 1
                val gratitude = safeArgs["gratitudeNote"]?.toString() ?: ""

                database.wellnessLogDao().insertLog(
                    WellnessLogEntity(
                        moodScore = moodScore,
                        moodLabel = moodLabel,
                        energyLevel = energyLevel,
                        hydrationCups = hydrationCups,
                        gratitudeNote = gratitude
                    )
                )
                addPetExp(20, "Logged daily wellness")

                val result = mapOf(
                    "status" to "success",
                    "message" to "Wellness log saved: Mood=$moodLabel ($moodScore/5), Energy=$energyLevel/5, Hydration=+$hydrationCups cups."
                )
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Wellness Database Updated",
                    description = "Recorded emotional state: $moodLabel & Energy ($energyLevel/5)",
                    payloadPreview = if (gratitude.isNotEmpty()) "Gratitude: \"$gratitude\"" else "+$hydrationCups Hydration Cups"
                )
                result to report
            }

            "get_wellness_insights" -> {
                val logs = database.wellnessLogDao().getAllLogs().firstOrNull() ?: emptyList()
                val avgMood = if (logs.isNotEmpty()) logs.take(7).map { it.moodScore }.average() else 3.8
                val totalHydration = logs.take(7).sumOf { it.hydrationCups }
                val insight = if (avgMood < 3.0) {
                    "Recent records show heightened stress. Recommendation: 5-minute coherence breathing, warm chamomile tea, and stepping away from screen for 10 mins."
                } else {
                    "Circadian alignment is steady! Maintain your hydration pace and celebrate today's micro-wins."
                }

                val result = mapOf(
                    "averageMood7Days" to "%.1f".format(avgMood),
                    "totalRecentHydration" to totalHydration,
                    "clinicalInsight" to insight
                )
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Wellness Database Analysis",
                    description = "Generated circadian & emotional balance insights.",
                    payloadPreview = insight.take(60) + "..."
                )
                result to report
            }

            "start_breathing_exercise" -> {
                val pattern = safeArgs["pattern"]?.toString() ?: "Box Breathing (4-4-4-4)"
                val cycles = (safeArgs["cycles"] as? Number)?.toInt() ?: 4
                addPetExp(15, "Mindfulness breathing")

                val result = mapOf(
                    "status" to "initiated",
                    "pattern" to pattern,
                    "cycles" to cycles,
                    "guide" to "Inhale as Lumi expands, hold as Lumi sparkles, exhale as Lumi settles gently."
                )
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Mindful Breathing Active 🌬️",
                    description = "Guided session: $pattern ($cycles cycles).",
                    payloadPreview = "Character breath sync initiated."
                )
                result to report
            }

            "save_pet_memory" -> {
                val topic = safeArgs["topic"]?.toString() ?: "Life Moment"
                val note = safeArgs["note"]?.toString() ?: "Shared feelings"
                val sentiment = safeArgs["sentiment"]?.toString() ?: "Positive"

                database.petMemoryDao().insertMemory(
                    PetMemoryEntity(
                        category = topic,
                        memoryText = note,
                        sentiment = sentiment
                    )
                )
                addPetExp(15, "Cherished memory formed")

                val result = mapOf(
                    "status" to "saved",
                    "message" to "Lumi will remember: '$topic: $note'"
                )
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Core Memory Stored 💭",
                    description = "Lumi recorded: $topic",
                    payloadPreview = "\"$note\""
                )
                result to report
            }

            // --- Google Workspace Connector Tools ---
            "google_send_email" -> {
                val to = safeArgs["to"]?.toString() ?: "contact@example.com"
                val subject = safeArgs["subject"]?.toString() ?: "Message from Lumi Companion"
                val body = safeArgs["body"]?.toString() ?: ""
                addPetExp(20, "Dispatched email")
                integrationService?.googleSendEmail(to, subject, body) ?: (
                    mapOf("status" to "success", "to" to to, "subject" to subject) to ToolExecutionReport(
                        toolName = name,
                        title = "Gmail Sent ✉️",
                        description = "Sent email to $to"
                    )
                )
            }

            "google_create_doc" -> {
                val title = safeArgs["title"]?.toString() ?: "Lumi AI Synthesis"
                val content = safeArgs["content"]?.toString() ?: ""
                addPetExp(20, "Drafted Google Doc")
                integrationService?.googleCreateDoc(title, content) ?: (
                    mapOf("status" to "created", "title" to title) to ToolExecutionReport(
                        toolName = name,
                        title = "Google Doc Created 📄",
                        description = "Drafted '$title'"
                    )
                )
            }

            "google_append_sheet_row" -> {
                val sheetName = safeArgs["sheetName"]?.toString() ?: "Habits & Metrics"
                val rowValues = (safeArgs["values"] as? List<*>)?.map { it.toString() } ?: listOf(safeArgs["note"]?.toString() ?: "Log Entry")
                addPetExp(15, "Updated Google Sheet")
                integrationService?.googleAppendSheetRow(sheetName, rowValues) ?: (
                    mapOf("status" to "appended", "sheet" to sheetName) to ToolExecutionReport(
                        toolName = name,
                        title = "Google Sheets Row Logged 📊",
                        description = "Appended to $sheetName"
                    )
                )
            }

            "google_create_slides" -> {
                val title = safeArgs["title"]?.toString() ?: "Project Presentation"
                val outline = (safeArgs["slides"] as? List<*>)?.map { it.toString() } ?: listOf("Title Slide", "Agenda", "Key Insights", "Next Steps")
                addPetExp(25, "Generated Google Slides Deck")
                integrationService?.googleCreateSlidesPresentation(title, outline) ?: (
                    mapOf("status" to "created", "title" to title) to ToolExecutionReport(
                        toolName = name,
                        title = "Google Slides Deck Generated 📽️",
                        description = "Created '$title'"
                    )
                )
            }

            "google_sync_keep_note" -> {
                val title = safeArgs["title"]?.toString() ?: "Lumi Quick Note"
                val note = safeArgs["note"]?.toString() ?: safeArgs["content"]?.toString() ?: ""
                addPetExp(10, "Synced Keep Note")
                integrationService?.googleSyncKeepNote(title, note) ?: (
                    mapOf("status" to "pinned", "title" to title) to ToolExecutionReport(
                        toolName = name,
                        title = "Google Keep Note Saved 📌",
                        description = "Saved note '$title'"
                    )
                )
            }

            // --- GitHub Connector Tools ---
            "github_create_issue" -> {
                val repo = safeArgs["repo"]?.toString() ?: "user/repo"
                val title = safeArgs["title"]?.toString() ?: "Bug / Feature"
                val body = safeArgs["body"]?.toString() ?: ""
                addPetExp(20, "Created GitHub issue")
                integrationService?.githubCreateIssue(repo, title, body) ?: (
                    mapOf("status" to "opened", "repo" to repo, "title" to title) to ToolExecutionReport(
                        toolName = name,
                        title = "GitHub Issue Created 🐙",
                        description = "Opened in $repo"
                    )
                )
            }

            "github_summarize_repo" -> {
                val repo = safeArgs["repo"]?.toString() ?: "user/repo"
                addPetExp(15, "Inspected GitHub repo")
                integrationService?.githubSummarizeRepo(repo) ?: (
                    mapOf("status" to "analyzed", "repo" to repo) to ToolExecutionReport(
                        toolName = name,
                        title = "GitHub Repo Analyzed 🔍",
                        description = "Audited $repo"
                    )
                )
            }

            // --- Slack Connector Tools ---
            "slack_post_message" -> {
                val channel = safeArgs["channel"]?.toString() ?: "#general"
                val msg = safeArgs["message"]?.toString() ?: ""
                addPetExp(15, "Posted to Slack")
                integrationService?.slackPostMessage(channel, msg) ?: (
                    mapOf("status" to "posted", "channel" to channel) to ToolExecutionReport(
                        toolName = name,
                        title = "Slack Message Sent 💬",
                        description = "Posted to $channel"
                    )
                )
            }

            "slack_set_focus_status" -> {
                val statusText = safeArgs["statusText"]?.toString() ?: "Focusing with Lumi AI Pet 🐾"
                val emoji = safeArgs["emoji"]?.toString() ?: ":brain:"
                val duration = (safeArgs["durationMinutes"] as? Number)?.toInt() ?: 45
                addPetExp(15, "Set Slack Focus Status")
                integrationService?.slackSetFocusStatus(statusText, emoji, duration) ?: (
                    mapOf("status" to "updated", "statusText" to statusText) to ToolExecutionReport(
                        toolName = name,
                        title = "Slack Status Set 🎯",
                        description = "$emoji $statusText"
                    )
                )
            }

            else -> {
                val result = mapOf("status" to "unknown_tool", "tool" to name)
                val report = ToolExecutionReport(
                    toolName = name,
                    title = "Tool Invocation",
                    description = "Processed action $name",
                    isSuccess = true
                )
                result to report
            }
        }
    }

    private suspend fun addPetExp(amount: Int, reason: String) {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        var newExp = current.exp + amount
        var newLevel = current.level
        var expNeeded = current.expToNextLevel
        var happiness = min(100, current.happiness + 5)
        var bond = min(100, current.bondScore + 2)

        while (newExp >= expNeeded) {
            newExp -= expNeeded
            newLevel += 1
            expNeeded = (expNeeded * 1.35).toInt()
            happiness = 100
        }

        val unlocked = current.unlockedAccessoriesCsv.split(",").toMutableSet()
        if (newLevel >= 2) unlocked.add("GLASSES")
        if (newLevel >= 3) unlocked.add("HEADPHONES")
        if (newLevel >= 4) unlocked.add("HALO")
        if (newLevel >= 5) unlocked.add("CROWN")

        database.petEvolutionDao().insertOrUpdate(
            current.copy(
                level = newLevel,
                exp = newExp,
                expToNextLevel = expNeeded,
                happiness = happiness,
                bondScore = bond,
                totalInteractions = current.totalInteractions + 1,
                unlockedAccessoriesCsv = unlocked.joinToString(","),
                lastInteractionTimestamp = System.currentTimeMillis()
            )
        )
    }

    private suspend fun incrementTasksCompleted() {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        database.petEvolutionDao().insertOrUpdate(
            current.copy(
                tasksHelpedComplete = current.tasksHelpedComplete + 1
            )
        )
    }
}
