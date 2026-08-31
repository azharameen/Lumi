package com.example.data.local.mapper

import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.FactKnowledgeEntity
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.CalendarEvent
import com.example.domain.model.ChatMessage
import com.example.domain.model.Task
import com.example.domain.model.UserFact

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id.toString(),
        title = title,
        isCompleted = isCompleted,
        category = category,
        createdAt = createdAt,
        dueDate = dueDate,
        priority = priority
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        isCompleted = isCompleted,
        category = category,
        createdAt = createdAt,
        dueDate = dueDate,
        priority = priority
    )
}

fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id.toString(),
        sender = sender,
        text = content,
        timestamp = timestamp,
        emotion = petEmotion,
        isPending = false
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id.toLongOrNull() ?: 0L,
        sender = sender,
        content = text,
        timestamp = timestamp,
        petEmotion = emotion ?: "HAPPY"
    )
}

fun CalendarEventEntity.toDomain(): CalendarEvent {
    return CalendarEvent(
        id = id.toString(),
        title = title,
        startTime = startTimeMillis,
        endTime = endTimeMillis,
        location = location,
        description = description,
        isAllDay = isAllDay
    )
}

fun CalendarEvent.toEntity(): CalendarEventEntity {
    return CalendarEventEntity(
        id = id.toLongOrNull() ?: 0L,
        title = title,
        startTimeMillis = startTime,
        endTimeMillis = endTime,
        location = location ?: "",
        description = description ?: "",
        isAllDay = isAllDay
    )
}

fun FactKnowledgeEntity.toDomain(): UserFact {
    return UserFact(
        id = id.toString(),
        factKey = predicate,
        factValue = objectValue,
        isPinned = false,
        createdAt = lastUpdatedMillis
    )
}

fun UserFact.toEntity(): FactKnowledgeEntity {
    return FactKnowledgeEntity(
        id = id.toLongOrNull() ?: 0L,
        predicate = factKey,
        objectValue = factValue,
        lastUpdatedMillis = createdAt
    )
}
