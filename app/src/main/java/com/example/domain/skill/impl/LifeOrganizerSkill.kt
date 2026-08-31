package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

class LifeOrganizerSkill : AgentSkill {
    override val id: String = "LIFE_ORGANIZER"
    override val displayName: String = "Life & Task Organizer"
    override val description: String = "Manages daily calendar blocks, to-do task items, routines, and memories."
    override val systemPromptExtension: String = """
        Focus: Active scheduling, time blocking, and task management.
        Guidelines: Always offer actionable scheduling when user expresses time management concerns or busyness.
    """.trimIndent()

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "add_calendar_event",
                    description = "Schedule a new event, meeting, routine, or focus block in the user's daily calendar timeline.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Title of the event"),
                            "startTimeOffsetHours" to GeminiPropertySchema(type = "NUMBER", description = "Hours from right now when this event starts (e.g. 1.5, 3.0, or 0 for immediate)"),
                            "durationMinutes" to GeminiPropertySchema(type = "INTEGER", description = "Duration of the event in minutes (e.g. 30, 45, 60)"),
                            "category" to GeminiPropertySchema(type = "STRING", description = "Category: Work, Health, Focus, Social, Rest", enum = listOf("Work", "Health", "Focus", "Social", "Rest")),
                            "description" to GeminiPropertySchema(type = "STRING", description = "Details or preparation notes for the event")
                        ),
                        required = listOf("title", "durationMinutes")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "get_daily_schedule",
                    description = "Retrieve all scheduled calendar events and routines for today to analyze availability or conflicts.",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "create_task",
                    description = "Add a new action item or to-do task to the user's task manager with priority and smart time estimate.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Clear actionable task title"),
                            "priority" to GeminiPropertySchema(type = "STRING", description = "Priority level", enum = listOf("LOW", "MEDIUM", "HIGH", "URGENT")),
                            "category" to GeminiPropertySchema(type = "STRING", description = "Category", enum = listOf("General", "Work", "Wellness", "Personal", "Study")),
                            "estimatedMinutes" to GeminiPropertySchema(type = "INTEGER", description = "Estimated duration in minutes (e.g. 15, 30, 60)"),
                            "notes" to GeminiPropertySchema(type = "STRING", description = "Optional notes or broken-down micro-steps")
                        ),
                        required = listOf("title")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "complete_task",
                    description = "Mark a task in the task manager as finished.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "taskTitle" to GeminiPropertySchema(type = "STRING", description = "Title or keyword of the task to mark completed")
                        ),
                        required = listOf("taskTitle")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "list_pending_tasks",
                    description = "Fetch all open, pending tasks to help the user prioritize their day.",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "save_pet_memory",
                    description = "Store a meaningful insight, user preference, emotion, or life goal into Lumi's long-term memory archive so Lumi remembers and evolves.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "topic" to GeminiPropertySchema(type = "STRING", description = "Memory category or theme (e.g. Work Stress, Favorite Song, Life Dream, Friend Birthday)"),
                            "note" to GeminiPropertySchema(type = "STRING", description = "Summary of what user shared"),
                            "sentiment" to GeminiPropertySchema(type = "STRING", description = "Sentiment: Positive, Neutral, Gentle Support")
                        ),
                        required = listOf("topic", "note")
                    )
                )
            )
        )
    )
}
