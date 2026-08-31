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
        id = id,
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
        id = id,
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
        id = id,
        sender = sender,
        text = text,
        timestamp = timestamp,
        emotion = emotion,
        isPending = isPending
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        sender = sender,
        text = text,
        timestamp = timestamp,
        emotion = emotion,
        isPending = isPending
    )
}

fun CalendarEventEntity.toDomain(): CalendarEvent {
    return CalendarEvent(
        id = id,
        title = title,
        startTime = startTime,
        endTime = endTime,
        location = location,
        description = description,
        isAllDay = isAllDay
    )
}

fun CalendarEvent.toEntity(): CalendarEventEntity {
    return CalendarEventEntity(
        id = id,
        title = title,
        startTime = startTime,
        endTime = endTime,
        location = location,
        description = description,
        isAllDay = isAllDay
    )
}

fun FactKnowledgeEntity.toDomain(): UserFact {
    return UserFact(
        id = id,
        factKey = factKey,
        factValue = factValue,
        isPinned = isPinned,
        createdAt = createdAt
    )
}

fun UserFact.toEntity(): FactKnowledgeEntity {
    return FactKnowledgeEntity(
        id = id,
        factKey = factKey,
        factValue = factValue,
        isPinned = isPinned,
        createdAt = createdAt
    )
}
