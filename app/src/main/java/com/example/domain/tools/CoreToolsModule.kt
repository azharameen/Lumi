package com.example.domain.tools

import com.example.domain.connectors.IntegrationService
import com.example.domain.model.CalendarEvent
import com.example.domain.repository.TaskGoalRepository
import com.example.domain.repository.WellnessRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddCalendarEventTool(private val taskGoalRepository: TaskGoalRepository) : LumiTool {
    override val id: String = "add_calendar_event"
    override val displayName: String = "Calendar Block Scheduled 📅"
    override val description: String = "Adds a time-blocked event to the schedule"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("title", "string", "Event title"),
        ToolParameter("startTimeOffsetHours", "number", "Hours from now (e.g. 1.5)"),
        ToolParameter("durationMinutes", "number", "Duration in minutes"),
        ToolParameter("category", "string", "Event category (Focus, Meeting, etc)"),
        ToolParameter("description", "string", "Event details", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val title = params["title"] as? String ?: return ToolExecutionResult(false, "Missing title")
        val offset = (params["startTimeOffsetHours"] as? Number)?.toDouble() ?: 0.0
        val duration = (params["durationMinutes"] as? Number)?.toInt() ?: 60
        val cat = params["category"] as? String ?: "General"
        val desc = params["description"] as? String ?: ""

        val startMillis = System.currentTimeMillis() + (offset * 3600000).toLong()
        val endMillis = startMillis + (duration * 60000).toLong()

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val timeDisplay = "${timeFormat.format(Date(startMillis))} - ${timeFormat.format(Date(endMillis))}"

        val event = CalendarEvent(
            id = 0,
            title = title,
            description = desc,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            category = cat,
            createdAt = System.currentTimeMillis()
        )
        val id = taskGoalRepository.addCalendarEvent(event)
        
        return ToolExecutionResult(
            success = true,
            resultText = "Added '$title' for $timeDisplay",
            payload = mapOf("eventId" to id, "time" to timeDisplay)
        )
    }
}

class CreateTaskTool(private val taskGoalRepository: TaskGoalRepository) : LumiTool {
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

        val id = taskGoalRepository.addTask(
            title = title,
            priority = priority.uppercase(Locale.ROOT),
            category = cat,
            estimatedMinutes = mins,
            notes = notes
        )
        
        return ToolExecutionResult(
            success = true,
            resultText = "Added '$title' ($mins m • $priority)",
            payload = mapOf("taskId" to id)
        )
    }
}

class GetDailyScheduleTool(private val taskGoalRepository: TaskGoalRepository) : LumiTool {
    override val id: String = "get_daily_schedule"
    override val displayName: String = "Calendar Schedule Retrieved 📋"
    override val description: String = "Retrieves today's scheduled events"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val events = taskGoalRepository.getAllEventsSync()
        val formatted = events.map { mapOf("id" to it.id, "title" to it.title, "category" to it.category) }
        
        return ToolExecutionResult(
            success = true,
            resultText = "Analyzed ${events.size} scheduled blocks for today",
            payload = mapOf("events" to formatted)
        )
    }
}

class CompleteTaskTool(private val taskGoalRepository: TaskGoalRepository) : LumiTool {
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
        val tasks = taskGoalRepository.getAllTasksSync()
        val match = tasks.find { it.title.contains(title, ignoreCase = true) && !it.isCompleted }
        
        return if (match != null) {
            taskGoalRepository.updateTask(match.copy(isCompleted = true))
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

class ListPendingTasksTool(private val taskGoalRepository: TaskGoalRepository) : LumiTool {
    override val id: String = "list_pending_tasks"
    override val displayName: String = "Task Priority Review 📝"
    override val description: String = "Lists all currently pending tasks"
    override val category: ToolCategory = ToolCategory.CALENDAR
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.LOW
    override val parameters: List<ToolParameter> = emptyList()

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val openTasks = taskGoalRepository.getAllTasksSync().filter { !it.isCompleted }
        return ToolExecutionResult(
            success = true,
            resultText = "Identified ${openTasks.size} open action items",
            payload = mapOf("tasks" to openTasks.map { it.title })
        )
    }
}

class LogWellnessTool(private val wellnessRepository: WellnessRepository) : LumiTool {
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

        wellnessRepository.logWellness(mood, label, energy, hydration, gratitude)
        
        return ToolExecutionResult(
            success = true,
            resultText = "Logged mood: $label ($mood/5) • Energy: $energy/5",
            payload = mapOf("moodLabel" to label)
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

object CoreToolsModule {
    fun register(
        taskGoalRepository: TaskGoalRepository,
        wellnessRepository: WellnessRepository,
        integrationService: IntegrationService,
        registry: ToolRegistry = ToolRegistry.getInstance()
    ) {
        registry.registerTools(listOf(
            AddCalendarEventTool(taskGoalRepository),
            CreateTaskTool(taskGoalRepository),
            GetDailyScheduleTool(taskGoalRepository),
            CompleteTaskTool(taskGoalRepository),
            ListPendingTasksTool(taskGoalRepository),
            LogWellnessTool(wellnessRepository),
            GoogleSendEmailTool(integrationService)
        ))
    }
}
