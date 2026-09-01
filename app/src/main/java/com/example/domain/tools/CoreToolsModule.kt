package com.example.domain.tools

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.domain.connectors.IntegrationService
import java.text.SimpleDateFormat
import java.util.*

object CoreToolsModule {

    fun register(
        database: LumiDatabase,
        integrationService: IntegrationService,
        registry: ToolRegistry = ToolRegistry.getInstance()
    ) {
        registry.registerTools(listOf(
            AddCalendarEventTool(database),
            GetDailyScheduleTool(database),
            CreateTaskTool(database),
            CompleteTaskTool(database),
            ListPendingTasksTool(database),
            LogWellnessTool(database),
            GoogleSendEmailTool(integrationService)
        ))
    }
}

class AddCalendarEventTool(private val database: LumiDatabase) : LumiTool {
    override val id: String = "add_calendar_event"
    override val displayName: String = "Schedule Calendar Block 📅"
    override val description: String = "Adds a new event to the user's calendar"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.MEDIUM
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("title", "string", "Title of the event"),
        ToolParameter("startTimeOffsetHours", "number", "Hours from now to start"),
        ToolParameter("durationMinutes", "number", "Duration in minutes"),
        ToolParameter("category", "string", "Event category (e.g. Work, Personal)"),
        ToolParameter("description", "string", "Optional description", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val title = params["title"] as? String ?: return ToolExecutionResult(false, "Missing title")
        val offset = (params["startTimeOffsetHours"] as? Number)?.toDouble() ?: 0.0
        val duration = (params["durationMinutes"] as? Number)?.toInt() ?: 30
        val cat = params["category"] as? String ?: "General"
        val desc = params["description"] as? String ?: ""

        val now = System.currentTimeMillis()
        val startMillis = now + (offset * 3600 * 1000).toLong()
        val endMillis = startMillis + (duration * 60 * 1000).toLong()

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeDisplay = "${timeFormat.format(Date(startMillis))} - ${timeFormat.format(Date(endMillis))}"

        val event = CalendarEventEntity(
            title = title,
            description = desc,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            category = cat
        )
        val id = database.calendarEventDao().insertEvent(event)

        return ToolExecutionResult(
            success = true,
            resultText = "Added '$title' for $timeDisplay",
            payload = mapOf("eventId" to id, "time" to timeDisplay)
        )
    }
}

class CreateTaskTool(private val database: LumiDatabase) : LumiTool {
    override val id: String = "create_task"
    override val displayName: String = "New Task Created 🎯"
    override val description: String = "Creates a new task in the productivity manager"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("title", "string", "Task title"),
        ToolParameter("priority", "string", "Priority (LOW, MEDIUM, HIGH)"),
        ToolParameter("category", "string", "Task category"),
        ToolParameter("estimatedMinutes", "number", "Estimated time to complete"),
        ToolParameter("notes", "string", "Optional notes", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val title = params["title"] as? String ?: return ToolExecutionResult(false, "Missing title")
        val priority = params["priority"] as? String ?: "MEDIUM"
        val cat = params["category"] as? String ?: "General"
        val mins = (params["estimatedMinutes"] as? Number)?.toInt() ?: 30
        val notes = params["notes"] as? String ?: ""

        val task = TaskEntity(
            title = title,
            priority = priority.uppercase(),
            category = cat,
            estimatedMinutes = mins,
            notes = notes,
            isCompleted = false
        )
        val id = database.taskDao().insertTask(task)

        return ToolExecutionResult(
            success = true,
            resultText = "Added '$title' ($mins m • $priority)",
            payload = mapOf("taskId" to id)
        )
    }
}

class GetDailyScheduleTool(private val database: LumiDatabase) : LumiTool {
    override val id: String = "get_daily_schedule"
    override val displayName: String = "Calendar Schedule Retrieved 📋"
    override val description: String = "Retrieves today's scheduled events"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val events = database.calendarEventDao().getAllEventsDirect()
        val formatted = events.map { mapOf("id" to it.id, "title" to it.title, "category" to it.category) }
        return ToolExecutionResult(
            success = true,
            resultText = "Analyzed ${events.size} scheduled blocks for today",
            payload = mapOf("events" to formatted)
        )
    }
}

class CompleteTaskTool(private val database: LumiDatabase) : LumiTool {
    override val id: String = "complete_task"
    override val displayName: String = "Task Completed 🎉"
    override val description: String = "Marks a task as completed by title"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("taskTitle", "string", "Title of the task to complete")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val title = params["taskTitle"] as? String ?: return ToolExecutionResult(false, "Missing task title")
        val tasks = database.taskDao().getAllTasksDirect()
        val match = tasks.find { it.title.contains(title, ignoreCase = true) && !it.isCompleted }
        return if (match != null) {
            database.taskDao().updateTask(match.copy(isCompleted = true))
            ToolExecutionResult(
                success = true, 
                resultText = "Marked '${match.title}' as finished", 
                payload = mapOf("taskId" to match.id)
            )
        } else {
            ToolExecutionResult(false, "No open task matching '$title'")
        }
    }
}

class ListPendingTasksTool(private val database: LumiDatabase) : LumiTool {
    override val id: String = "list_pending_tasks"
    override val displayName: String = "Task Priority Review 📝"
    override val description: String = "Lists all currently pending tasks"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val openTasks = database.taskDao().getAllTasksDirect().filter { !it.isCompleted }
        return ToolExecutionResult(
            success = true,
            resultText = "Identified ${openTasks.size} open action items",
            payload = mapOf("tasks" to openTasks.map { it.title })
        )
    }
}

class LogWellnessTool(private val database: LumiDatabase) : LumiTool {
    override val id: String = "log_wellness"
    override val displayName: String = "Wellness Checkpoint Saved 🌱"
    override val description: String = "Logs mood, energy, and hydration"
    override val category: ToolCategory = ToolCategory.HEALTH
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("moodScore", "number", "Mood score 1-5"),
        ToolParameter("moodLabel", "string", "Mood description"),
        ToolParameter("energyLevel", "number", "Energy level 1-5"),
        ToolParameter("hydrationIncrementCups", "number", "Cups of water added"),
        ToolParameter("gratitudeNote", "string", "Optional gratitude note", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val mood = (params["moodScore"] as? Number)?.toInt() ?: 3
        val label = params["moodLabel"] as? String ?: "Neutral"
        val energy = (params["energyLevel"] as? Number)?.toInt() ?: 3
        val hydration = (params["hydrationIncrementCups"] as? Number)?.toInt() ?: 0
        val gratitude = params["gratitudeNote"] as? String ?: ""

        val log = WellnessLogEntity(
            timestamp = System.currentTimeMillis(),
            moodScore = mood,
            moodLabel = label,
            energyLevel = energy,
            hydrationCups = hydration,
            gratitudeNote = gratitude
        )
        val id = database.wellnessLogDao().insertLog(log)
        return ToolExecutionResult(
            success = true,
            resultText = "Logged mood: $label ($mood/5) • Energy: $energy/5",
            payload = mapOf("logId" to id)
        )
    }
}

class GoogleSendEmailTool(private val integrationService: IntegrationService) : LumiTool {
    override val id: String = "google_send_email"
    override val displayName: String = "Send Email 📧"
    override val description: String = "Sends an email via Google Workspace"
    override val category: ToolCategory = ToolCategory.CONNECTORS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.HIGH
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("to", "string", "Recipient email address"),
        ToolParameter("subject", "string", "Email subject"),
        ToolParameter("body", "string", "Email body")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val to = params["to"] as? String ?: return ToolExecutionResult(false, "Missing recipient")
        val subject = params["subject"] as? String ?: ""
        val body = params["body"] as? String ?: ""

        integrationService.googleSendEmail(to, subject, body)

        return ToolExecutionResult(
            success = true,
            resultText = "Email sent to $to",
            payload = mapOf("recipient" to to)
        )
    }
}
