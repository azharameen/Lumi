package com.example.domain.model

/**
 * Pure Domain Model for Todo Tasks (decoupled from Room DB TaskEntity).
 */
data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val category: String,
    val createdAt: Long,
    val dueDate: Long? = null,
    val priority: String = "NORMAL"
)

/**
 * Pure Domain Model for Chat Messages (decoupled from Room DB ChatMessageEntity).
 */
data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "PET"
    val text: String,
    val timestamp: Long,
    val emotion: String? = null,
    val isPending: Boolean = false
)

/**
 * Pure Domain Model for Calendar Events (decoupled from Room DB CalendarEventEntity).
 */
data class CalendarEvent(
    val id: String,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val location: String? = null,
    val description: String? = null,
    val isAllDay: Boolean = false
)

/**
 * Pure Domain Model for Stored Personal Facts.
 */
data class UserFact(
    val id: String,
    val factKey: String,
    val factValue: String,
    val isPinned: Boolean,
    val createdAt: Long
)
