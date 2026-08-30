package com.example.domain.tools

import org.json.JSONArray
import org.json.JSONObject

/**
 * Type-safe input schema definitions and strict JSON parsers for AI Function & Tool Calls.
 */
object AgentToolSchemas {

    data class AddCalendarEventArgs(
        val title: String = "Focus Session",
        val startTimeOffsetHours: Double = 0.5,
        val durationMinutes: Int = 45,
        val category: String = "Focus",
        val description: String = ""
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): AddCalendarEventArgs {
                if (map == null) return AddCalendarEventArgs()
                return AddCalendarEventArgs(
                    title = map["title"]?.toString() ?: "Focus Session",
                    startTimeOffsetHours = (map["startTimeOffsetHours"] as? Number)?.toDouble() ?: 0.5,
                    durationMinutes = (map["durationMinutes"] as? Number)?.toInt() ?: 45,
                    category = map["category"]?.toString() ?: "Focus",
                    description = map["description"]?.toString() ?: ""
                )
            }
        }
    }

    data class CreateTaskArgs(
        val title: String = "New Action Item",
        val priority: String = "MEDIUM",
        val category: String = "General",
        val estimatedMinutes: Int = 30,
        val notes: String = ""
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): CreateTaskArgs {
                if (map == null) return CreateTaskArgs()
                return CreateTaskArgs(
                    title = map["title"]?.toString() ?: "New Action Item",
                    priority = map["priority"]?.toString()?.uppercase() ?: "MEDIUM",
                    category = map["category"]?.toString() ?: "General",
                    estimatedMinutes = (map["estimatedMinutes"] as? Number)?.toInt() ?: 30,
                    notes = map["notes"]?.toString() ?: ""
                )
            }
        }
    }

    data class CompleteTaskArgs(
        val taskTitle: String = ""
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): CompleteTaskArgs {
                if (map == null) return CompleteTaskArgs()
                return CompleteTaskArgs(
                    taskTitle = map["taskTitle"]?.toString() ?: ""
                )
            }
        }
    }

    data class LogWellnessArgs(
        val moodScore: Int = 4,
        val moodLabel: String = "Calm",
        val energyLevel: Int = 3,
        val hydrationIncrementCups: Int = 1,
        val gratitudeNote: String = ""
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): LogWellnessArgs {
                if (map == null) return LogWellnessArgs()
                return LogWellnessArgs(
                    moodScore = (map["moodScore"] as? Number)?.toInt() ?: 4,
                    moodLabel = map["moodLabel"]?.toString() ?: "Calm",
                    energyLevel = (map["energyLevel"] as? Number)?.toInt() ?: 3,
                    hydrationIncrementCups = (map["hydrationIncrementCups"] as? Number)?.toInt() ?: 1,
                    gratitudeNote = map["gratitudeNote"]?.toString() ?: ""
                )
            }
        }
    }

    data class StartBreathingArgs(
        val pattern: String = "Box Breathing (4-4-4-4)",
        val cycles: Int = 4
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): StartBreathingArgs {
                if (map == null) return StartBreathingArgs()
                return StartBreathingArgs(
                    pattern = map["pattern"]?.toString() ?: "Box Breathing (4-4-4-4)",
                    cycles = (map["cycles"] as? Number)?.toInt() ?: 4
                )
            }
        }
    }

    data class SavePetMemoryArgs(
        val topic: String = "Life Moment",
        val note: String = "",
        val sentiment: String = "Positive"
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): SavePetMemoryArgs {
                if (map == null) return SavePetMemoryArgs()
                return SavePetMemoryArgs(
                    topic = map["topic"]?.toString() ?: "Life Moment",
                    note = map["note"]?.toString() ?: "",
                    sentiment = map["sentiment"]?.toString() ?: "Positive"
                )
            }
        }
    }

    data class GoogleSendEmailArgs(
        val to: String = "",
        val subject: String = "",
        val body: String = ""
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): GoogleSendEmailArgs {
                if (map == null) return GoogleSendEmailArgs()
                return GoogleSendEmailArgs(
                    to = map["to"]?.toString() ?: "",
                    subject = map["subject"]?.toString() ?: "",
                    body = map["body"]?.toString() ?: ""
                )
            }
        }
    }

    data class GoogleCreateDocArgs(
        val title: String = "Lumi AI Synthesis",
        val content: String = "",
        val folder: String = "Lumi AI Notes"
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): GoogleCreateDocArgs {
                if (map == null) return GoogleCreateDocArgs()
                return GoogleCreateDocArgs(
                    title = map["title"]?.toString() ?: "Lumi AI Synthesis",
                    content = map["content"]?.toString() ?: "",
                    folder = map["folder"]?.toString() ?: "Lumi AI Notes"
                )
            }
        }
    }

    data class GoogleAppendSheetRowArgs(
        val sheetName: String = "Habits & Metrics",
        val values: List<String> = emptyList()
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): GoogleAppendSheetRowArgs {
                if (map == null) return GoogleAppendSheetRowArgs()
                val rawValues = map["values"]
                val list = when (rawValues) {
                    is List<*> -> rawValues.mapNotNull { it?.toString() }
                    is JSONArray -> (0 until rawValues.length()).map { rawValues.getString(it) }
                    else -> emptyList()
                }
                return GoogleAppendSheetRowArgs(
                    sheetName = map["sheetName"]?.toString() ?: "Habits & Metrics",
                    values = list
                )
            }
        }
    }

    data class GoogleCreateSlidesArgs(
        val title: String = "Project Presentation",
        val slides: List<String> = emptyList()
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): GoogleCreateSlidesArgs {
                if (map == null) return GoogleCreateSlidesArgs()
                val rawSlides = map["slides"]
                val list = when (rawSlides) {
                    is List<*> -> rawSlides.mapNotNull { it?.toString() }
                    is JSONArray -> (0 until rawSlides.length()).map { rawSlides.getString(it) }
                    else -> emptyList()
                }
                return GoogleCreateSlidesArgs(
                    title = map["title"]?.toString() ?: "Project Presentation",
                    slides = list
                )
            }
        }
    }

    data class GoogleSyncKeepNoteArgs(
        val title: String = "Lumi Quick Note",
        val note: String = "",
        val colorTag: String = "Cyan"
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): GoogleSyncKeepNoteArgs {
                if (map == null) return GoogleSyncKeepNoteArgs()
                return GoogleSyncKeepNoteArgs(
                    title = map["title"]?.toString() ?: "Lumi Quick Note",
                    note = map["note"]?.toString() ?: "",
                    colorTag = map["colorTag"]?.toString() ?: "Cyan"
                )
            }
        }
    }

    data class GithubCreateIssueArgs(
        val repo: String = "",
        val title: String = "",
        val body: String = "",
        val labels: List<String> = listOf("lumi-ai", "enhancement")
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): GithubCreateIssueArgs {
                if (map == null) return GithubCreateIssueArgs()
                val rawLabels = map["labels"]
                val labelsList = when (rawLabels) {
                    is List<*> -> rawLabels.mapNotNull { it?.toString() }
                    is JSONArray -> (0 until rawLabels.length()).map { rawLabels.getString(it) }
                    else -> listOf("lumi-ai", "enhancement")
                }
                return GithubCreateIssueArgs(
                    repo = map["repo"]?.toString() ?: "",
                    title = map["title"]?.toString() ?: "",
                    body = map["body"]?.toString() ?: "",
                    labels = labelsList
                )
            }
        }
    }

    data class GithubSummarizeRepoArgs(
        val repo: String = ""
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): GithubSummarizeRepoArgs {
                if (map == null) return GithubSummarizeRepoArgs()
                return GithubSummarizeRepoArgs(
                    repo = map["repo"]?.toString() ?: ""
                )
            }
        }
    }

    data class SlackPostMessageArgs(
        val channel: String = "#general",
        val message: String = ""
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): SlackPostMessageArgs {
                if (map == null) return SlackPostMessageArgs()
                return SlackPostMessageArgs(
                    channel = map["channel"]?.toString() ?: "#general",
                    message = map["message"]?.toString() ?: ""
                )
            }
        }
    }

    data class SlackSetFocusStatusArgs(
        val statusText: String = "Focusing with Lumi AI Pet 🐾",
        val emoji: String = ":brain:",
        val durationMinutes: Int = 45
    ) {
        companion object {
            fun fromMap(map: Map<String, Any?>?): SlackSetFocusStatusArgs {
                if (map == null) return SlackSetFocusStatusArgs()
                return SlackSetFocusStatusArgs(
                    statusText = map["statusText"]?.toString() ?: "Focusing with Lumi AI Pet 🐾",
                    emoji = map["emoji"]?.toString() ?: ":brain:",
                    durationMinutes = (map["durationMinutes"] as? Number)?.toInt() ?: 45
                )
            }
        }
    }
}
