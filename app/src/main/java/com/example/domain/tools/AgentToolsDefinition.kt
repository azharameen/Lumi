package com.example.domain.tools

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper

object AgentToolsDefinition {

    val availableTools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                // Core Life & Calendar Tools
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
                    name = "log_wellness",
                    description = "Record the user's emotional mood score, energy, hydration, or gratitude note in the wellness database.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "moodScore" to GeminiPropertySchema(type = "INTEGER", description = "Mood rating 1 (distressed) to 5 (thriving)"),
                            "moodLabel" to GeminiPropertySchema(type = "STRING", description = "Descriptive word for emotional state (e.g. Joyful, Calm, Overwhelmed, Tired, Grateful)"),
                            "energyLevel" to GeminiPropertySchema(type = "INTEGER", description = "Energy level 1 (drained) to 5 (fully energized)"),
                            "hydrationIncrementCups" to GeminiPropertySchema(type = "INTEGER", description = "Number of water cups drank (e.g. 1, 2)"),
                            "gratitudeNote" to GeminiPropertySchema(type = "STRING", description = "Something the user is thankful for today")
                        )
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "get_wellness_insights",
                    description = "Query the external wellness knowledge base and user history for personalized circadian, hydration, and stress recovery tips.",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "start_breathing_exercise",
                    description = "Trigger a real-time mindful breathing exercise session where Lumi expands/contracts with the user's breath.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "pattern" to GeminiPropertySchema(type = "STRING", description = "Breathing pattern", enum = listOf("Box Breathing (4-4-4-4)", "Relaxing (4-7-8)", "Energy Boost (4-2-4-2)")),
                            "cycles" to GeminiPropertySchema(type = "INTEGER", description = "Number of breath cycles (e.g. 4, 6, 8)")
                        )
                    )
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
                ),

                // Google Workspace Tools
                GeminiFunctionDeclaration(
                    name = "google_send_email",
                    description = "Draft and send an email to a contact via the authenticated Google Workspace / Gmail connector.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "to" to GeminiPropertySchema(type = "STRING", description = "Recipient email address"),
                            "subject" to GeminiPropertySchema(type = "STRING", description = "Email subject line"),
                            "body" to GeminiPropertySchema(type = "STRING", description = "Email body message")
                        ),
                        required = listOf("to", "subject", "body")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_create_doc",
                    description = "Create a rich Google Doc in Google Drive with notes, summaries, or action plans.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Document title"),
                            "content" to GeminiPropertySchema(type = "STRING", description = "Document markdown or text body"),
                            "folder" to GeminiPropertySchema(type = "STRING", description = "Target Drive folder")
                        ),
                        required = listOf("title", "content")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_append_sheet_row",
                    description = "Log structured metrics or habit entries into a Google Sheets spreadsheet.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "sheetName" to GeminiPropertySchema(type = "STRING", description = "Name of target Sheet"),
                            "values" to GeminiPropertySchema(type = "ARRAY", description = "Row data values to append")
                        ),
                        required = listOf("sheetName")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_create_slides",
                    description = "Generate a Google Slides presentation outline and slide deck in Google Drive.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Presentation deck title"),
                            "slides" to GeminiPropertySchema(type = "ARRAY", description = "List of slide topics or bullets")
                        ),
                        required = listOf("title")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_sync_keep_note",
                    description = "Pin and sync a note, grocery list, or brainstorm to Google Keep.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Keep note title"),
                            "note" to GeminiPropertySchema(type = "STRING", description = "Keep note body content"),
                            "colorTag" to GeminiPropertySchema(type = "STRING", description = "Color label (Cyan, Yellow, Pink, Green)")
                        ),
                        required = listOf("title", "note")
                    )
                ),

                // GitHub Connector Tools
                GeminiFunctionDeclaration(
                    name = "github_create_issue",
                    description = "Open a new issue or feature card in a GitHub repository.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "repo" to GeminiPropertySchema(type = "STRING", description = "Repository in owner/repo format"),
                            "title" to GeminiPropertySchema(type = "STRING", description = "Issue title"),
                            "body" to GeminiPropertySchema(type = "STRING", description = "Issue details and specs")
                        ),
                        required = listOf("repo", "title")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "github_summarize_repo",
                    description = "Inspect repository health, open issues, active PRs, and recent commits on GitHub.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "repo" to GeminiPropertySchema(type = "STRING", description = "Repository in owner/repo format")
                        ),
                        required = listOf("repo")
                    )
                ),

                // Slack Connector Tools
                GeminiFunctionDeclaration(
                    name = "slack_post_message",
                    description = "Broadcast a channel update, progress report, or reminder to Slack.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "channel" to GeminiPropertySchema(type = "STRING", description = "Target channel like #general or #standup"),
                            "message" to GeminiPropertySchema(type = "STRING", description = "Message text to broadcast")
                        ),
                        required = listOf("channel", "message")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "slack_set_focus_status",
                    description = "Synchronize Do-Not-Disturb and set user Slack status while focusing with Lumi.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "statusText" to GeminiPropertySchema(type = "STRING", description = "Status message"),
                            "emoji" to GeminiPropertySchema(type = "STRING", description = "Slack emoji tag like :brain: or :paw_prints:"),
                            "durationMinutes" to GeminiPropertySchema(type = "INTEGER", description = "DND duration in minutes")
                        )
                    )
                )
            )
        )
    )
}
