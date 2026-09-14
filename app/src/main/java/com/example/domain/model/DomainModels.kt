package com.example.domain.model

data class Task(
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDate: Long? = null,
    val priority: String = "MEDIUM",
    val isCompleted: Boolean = false,
    val category: String = "General",
    val estimatedMinutes: Int = 30,
    val createdAt: Long
)

data class CalendarEvent(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val location: String = "",
    val isAllDay: Boolean = false,
    val category: String = "Routine",
    val colorHex: String = "#00F0FF",
    val reminderMinutesBefore: Int = 15,
    val createdAt: Long
)

data class ChatMessage(
    val id: Long = 0,
    val timestamp: Long,
    val sender: String,
    val content: String,
    val petEmotion: String = "HAPPY",
    val toolUsedName: String? = null,
    val toolResultJson: String? = null,
    val imageBase64OrUri: String? = null
)

data class WellnessLog(
    val id: Long = 0,
    val timestamp: Long,
    val moodScore: Int = 3,
    val moodLabel: String = "Balanced",
    val energyLevel: Int = 3,
    val hydrationCups: Int = 0,
    val sleepHours: Float = 7.0f,
    val gratitudeNote: String = "",
    val stressLevel: Int = 2,
    val breathingMinutesCompleted: Int = 0
)

data class GoalPlan(
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String = "Productivity",
    val targetDate: String = "",
    val status: String = "ACTIVE",
    val totalSteps: Int = 0,
    val completedSteps: Int = 0,
    val createdAt: Long,
    val isAiGenerated: Boolean = true,
    val tagsJson: String = "[]"
)

data class GoalMilestone(
    val id: Long = 0,
    val goalId: Long,
    val phaseNumber: Int = 1,
    val phaseTitle: String,
    val stepTitle: String,
    val stepDescription: String,
    val suggestedTool: String = "NONE",
    val isCompleted: Boolean = false,
    val executionOutput: String = "",
    val scheduledDate: String = ""
)

data class PetMemory(
    val id: Long = 0,
    val timestamp: Long,
    val category: String = "Emotion",
    val memoryText: String,
    val sentiment: String = "Positive",
    val emotionalImpact: Int = 3,
    val isPinned: Boolean = false,
    val embeddingBlob: ByteArray? = null
)

data class UserFact(
    val id: String,
    val factKey: String,
    val factValue: String,
    val isPinned: Boolean,
    val createdAt: Long,
    val embeddingBlob: ByteArray? = null
)

data class AiExecutionLog(
    val id: Long = 0,
    val taskCategory: String,
    val engineType: String,
    val modelName: String,
    val promptPreview: String,
    val responsePreview: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val estimatedCostUsd: Double,
    val startTimeMillis: Long,
    val finishTimeMillis: Long,
    val durationMs: Long,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null,
    val isOffline: Boolean = false,
    val hardwareTarget: String = "GPU (OpenCL/Vulkan)",
    val routingReason: String = "",
    val fallbackTriggered: Boolean = false
)
