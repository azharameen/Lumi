package com.example.domain.tools

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.domain.connectors.IntegrationService
import com.example.domain.model.PetEmotion
import com.example.domain.model.ToolExecutionReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Enterprise agent tool dispatcher.
 * Uses structured schemas and typed arguments parsing, eliminating fragile regex parsing.
 */
class AgentToolDispatcher(
    private val database: LumiDatabase,
    private val integrationService: IntegrationService
) {

    /**
     * Executes the requested tool with structured typing and returns response data and UI execution report.
     */
    suspend fun executeTool(
        toolName: String,
        args: Map<String, Any?>?
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val result = try {
            when (toolName) {
                // 1. Core Life & Calendar Tools
                "add_calendar_event" -> handleAddCalendarEvent(AgentToolSchemas.AddCalendarEventArgs.fromMap(args))
                "get_daily_schedule" -> handleGetDailySchedule()
                "create_task" -> handleCreateTask(AgentToolSchemas.CreateTaskArgs.fromMap(args))
                "complete_task" -> handleCompleteTask(AgentToolSchemas.CompleteTaskArgs.fromMap(args))
                "list_pending_tasks" -> handleListPendingTasks()
                "log_wellness" -> handleLogWellness(AgentToolSchemas.LogWellnessArgs.fromMap(args))
                "get_wellness_insights" -> handleGetWellnessInsights()
                "start_breathing_exercise" -> handleStartBreathing(AgentToolSchemas.StartBreathingArgs.fromMap(args))
                "save_pet_memory" -> handleSavePetMemory(AgentToolSchemas.SavePetMemoryArgs.fromMap(args))

                // 2. Google Workspace Integrations
                "google_send_email" -> {
                    val typed = AgentToolSchemas.GoogleSendEmailArgs.fromMap(args)
                    integrationService.googleSendEmail(typed.to, typed.subject, typed.body)
                }
                "google_create_doc" -> {
                    val typed = AgentToolSchemas.GoogleCreateDocArgs.fromMap(args)
                    integrationService.googleCreateDoc(typed.title, typed.content, typed.folder)
                }
                "google_append_sheet_row" -> {
                    val typed = AgentToolSchemas.GoogleAppendSheetRowArgs.fromMap(args)
                    integrationService.googleAppendSheetRow(typed.sheetName, typed.values)
                }
                "google_create_slides" -> {
                    val typed = AgentToolSchemas.GoogleCreateSlidesArgs.fromMap(args)
                    integrationService.googleCreateSlidesPresentation(typed.title, typed.slides)
                }
                "google_sync_keep_note" -> {
                    val typed = AgentToolSchemas.GoogleSyncKeepNoteArgs.fromMap(args)
                    integrationService.googleSyncKeepNote(typed.title, typed.note, typed.colorTag)
                }

                // 3. GitHub Integrations
                "github_create_issue" -> {
                    val typed = AgentToolSchemas.GithubCreateIssueArgs.fromMap(args)
                    integrationService.githubCreateIssue(typed.repo, typed.title, typed.body, typed.labels)
                }
                "github_summarize_repo" -> {
                    val typed = AgentToolSchemas.GithubSummarizeRepoArgs.fromMap(args)
                    integrationService.githubSummarizeRepo(typed.repo)
                }

                // 4. Slack Integrations
                "slack_post_message" -> {
                    val typed = AgentToolSchemas.SlackPostMessageArgs.fromMap(args)
                    integrationService.slackPostMessage(typed.channel, typed.message)
                }
                "slack_set_focus_status" -> {
                    val typed = AgentToolSchemas.SlackSetFocusStatusArgs.fromMap(args)
                    integrationService.slackSetFocusStatus(typed.statusText, typed.emoji, typed.durationMinutes)
                }

                else -> handleUnknownTool(toolName)
            }
        } catch (e: Exception) {
            handleToolError(toolName, e)
        }

        // Reward Pet Evolution on successful tool usage
        rewardPetProgression()

        result
    }

    private suspend fun handleAddCalendarEvent(args: AgentToolSchemas.AddCalendarEventArgs): Pair<Map<String, Any?>, ToolExecutionReport> {
        val now = System.currentTimeMillis()
        val startMillis = now + (args.startTimeOffsetHours * 3600 * 1000).toLong()
        val endMillis = startMillis + (args.durationMinutes * 60 * 1000).toLong()

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeDisplay = "${timeFormat.format(Date(startMillis))} - ${timeFormat.format(Date(endMillis))}"

        val event = CalendarEventEntity(
            title = args.title,
            description = args.description,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            category = args.category
        )
        val id = database.calendarEventDao().insertEvent(event)

        val output = mapOf(
            "status" to "success",
            "eventId" to id,
            "title" to args.title,
            "time" to timeDisplay,
            "category" to args.category
        )
        val report = ToolExecutionReport(
            toolName = "add_calendar_event",
            title = "Scheduled Calendar Block 📅",
            description = "Added '${args.title}' for $timeDisplay",
            payloadPreview = "$timeDisplay • ${args.category}"
        )
        return output to report
    }

    private suspend fun handleGetDailySchedule(): Pair<Map<String, Any?>, ToolExecutionReport> {
        val events = database.calendarEventDao().getAllEventsDirect()
        val formatted = events.map { mapOf("id" to it.id, "title" to it.title, "category" to it.category) }
        val output = mapOf("status" to "success", "eventCount" to events.size, "events" to formatted)
        val report = ToolExecutionReport(
            toolName = "get_daily_schedule",
            title = "Calendar Schedule Retrieved 📋",
            description = "Analyzed ${events.size} scheduled blocks for today",
            payloadPreview = if (events.isEmpty()) "No scheduled events" else events.joinToString(", ") { it.title }
        )
        return output to report
    }

    private suspend fun handleCreateTask(args: AgentToolSchemas.CreateTaskArgs): Pair<Map<String, Any?>, ToolExecutionReport> {
        val task = TaskEntity(
            title = args.title,
            priority = args.priority.uppercase(),
            category = args.category,
            estimatedMinutes = args.estimatedMinutes,
            notes = args.notes,
            isCompleted = false
        )
        val id = database.taskDao().insertTask(task)

        val output = mapOf(
            "status" to "success",
            "taskId" to id,
            "title" to args.title,
            "priority" to args.priority,
            "category" to args.category,
            "estimatedMinutes" to args.estimatedMinutes
        )
        val report = ToolExecutionReport(
            toolName = "create_task",
            title = "New Task Created 🎯",
            description = "Added '${args.title}' (${args.estimatedMinutes}m • ${args.priority})",
            payloadPreview = "Category: ${args.category} | Priority: ${args.priority}"
        )
        return output to report
    }

    private suspend fun handleCompleteTask(args: AgentToolSchemas.CompleteTaskArgs): Pair<Map<String, Any?>, ToolExecutionReport> {
        val tasks = database.taskDao().getAllTasksDirect()
        val match = tasks.find { it.title.contains(args.taskTitle, ignoreCase = true) && !it.isCompleted }
        return if (match != null) {
            database.taskDao().updateTask(match.copy(isCompleted = true))
            val output = mapOf("status" to "success", "completedTask" to match.title, "taskId" to match.id)
            val report = ToolExecutionReport(
                toolName = "complete_task",
                title = "Task Completed 🎉",
                description = "Marked '${match.title}' as finished",
                payloadPreview = "Great job finishing this action item!"
            )
            output to report
        } else {
            val output = mapOf("status" to "not_found", "message" to "No open task matching '${args.taskTitle}'")
            val report = ToolExecutionReport(
                toolName = "complete_task",
                title = "Task Search 🔍",
                description = "Could not locate open task matching '${args.taskTitle}'",
                payloadPreview = "Searched active task manager entries"
            )
            output to report
        }
    }

    private suspend fun handleListPendingTasks(): Pair<Map<String, Any?>, ToolExecutionReport> {
        val openTasks = database.taskDao().getAllTasksDirect().filter { !it.isCompleted }
        val output = mapOf("status" to "success", "openTaskCount" to openTasks.size, "tasks" to openTasks.map { it.title })
        val report = ToolExecutionReport(
            toolName = "list_pending_tasks",
            title = "Task Priority Review 📝",
            description = "Identified ${openTasks.size} open action items",
            payloadPreview = if (openTasks.isEmpty()) "All caught up!" else openTasks.take(3).joinToString(", ") { it.title }
        )
        return output to report
    }

    private suspend fun handleLogWellness(args: AgentToolSchemas.LogWellnessArgs): Pair<Map<String, Any?>, ToolExecutionReport> {
        val log = WellnessLogEntity(
            timestamp = System.currentTimeMillis(),
            moodScore = args.moodScore,
            moodLabel = args.moodLabel,
            energyLevel = args.energyLevel,
            hydrationCups = args.hydrationIncrementCups,
            gratitudeNote = args.gratitudeNote
        )
        val id = database.wellnessLogDao().insertLog(log)
        val output = mapOf("status" to "logged", "logId" to id, "moodScore" to args.moodScore, "mood" to args.moodLabel)
        val report = ToolExecutionReport(
            toolName = "log_wellness",
            title = "Wellness Checkpoint Saved 🌱",
            description = "Logged mood: ${args.moodLabel} (${args.moodScore}/5) • Energy: ${args.energyLevel}/5",
            payloadPreview = if (args.gratitudeNote.isNotBlank()) "Gratitude: ${args.gratitudeNote}" else "Hydration: +${args.hydrationIncrementCups} cups"
        )
        return output to report
    }

    private fun handleGetWellnessInsights(): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf(
            "status" to "success",
            "circadianWindow" to "Peak focus window: 9:30 AM - 12:00 PM",
            "hydrationTarget" to "6-8 cups daily (current: on track)",
            "stressReliefTip" to "Taking 3 deep diaphragmatic breaths reduces cortisol by up to 23%."
        )
        val report = ToolExecutionReport(
            toolName = "get_wellness_insights",
            title = "Circadian & Focus Insights 💡",
            description = "Retrieved biophilic focus tips",
            payloadPreview = "Peak focus window: 9:30 AM - 12:00 PM"
        )
        return output to report
    }

    private fun handleStartBreathing(args: AgentToolSchemas.StartBreathingArgs): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "initiated", "pattern" to args.pattern, "cycles" to args.cycles)
        val report = ToolExecutionReport(
            toolName = "start_breathing_exercise",
            title = "Mindful Breathing Session 🌬️",
            description = "Started ${args.pattern} (${args.cycles} cycles)",
            payloadPreview = "Inhale • Hold • Exhale • Rest"
        )
        return output to report
    }

    private suspend fun handleSavePetMemory(args: AgentToolSchemas.SavePetMemoryArgs): Pair<Map<String, Any?>, ToolExecutionReport> {
        val memory = PetMemoryEntity(
            category = args.topic,
            memoryText = args.note,
            sentiment = args.sentiment,
            timestamp = System.currentTimeMillis()
        )
        val id = database.petMemoryDao().insertMemory(memory)
        val output = mapOf("status" to "saved_memory", "memoryId" to id, "topic" to args.topic)
        val report = ToolExecutionReport(
            toolName = "save_pet_memory",
            title = "Lumi Core Memory Formed 💖",
            description = "Remembered: '${args.topic}'",
            payloadPreview = args.note.take(60)
        )
        return output to report
    }

    private fun handleUnknownTool(toolName: String): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to "Tool '$toolName' not recognized.")
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Tool Execution Warning ⚠️",
            description = "Tool '$toolName' is not registered in dispatcher",
            payloadPreview = "Dispatcher bypassed"
        )
        return output to report
    }

    private fun handleToolError(toolName: String, e: Exception): Pair<Map<String, Any?>, ToolExecutionReport> {
        val output = mapOf("status" to "error", "message" to (e.localizedMessage ?: "Unknown tool execution failure"))
        val report = ToolExecutionReport(
            toolName = toolName,
            title = "Tool Execution Error ⚠️",
            description = "Failed to run $toolName: ${e.localizedMessage ?: "Invalid parameters"}",
            payloadPreview = "Schema parsing or execution exception"
        )
        return output to report
    }

    private suspend fun rewardPetProgression() {
        try {
            val evolution = database.petEvolutionDao().getPetEvolutionDirect()
            if (evolution != null) {
                database.petEvolutionDao().insertOrUpdate(
                    evolution.copy(
                        exp = evolution.exp + 15,
                        happiness = (evolution.happiness + 5).coerceAtMost(100)
                    )
                )
            }
        } catch (_: Exception) {}
    }
}
