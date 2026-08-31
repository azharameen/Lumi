package com.example.domain.repository

import com.example.domain.model.CalendarEvent
import com.example.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskGoalRepository {
    val tasks: Flow<List<Task>>
    val calendarEvents: Flow<List<CalendarEvent>>

    suspend fun addTask(title: String, category: String = "GENERAL", dueDate: Long? = null)
    suspend fun completeTask(taskId: String)
    suspend fun deleteTask(taskId: String)
    suspend fun addCalendarEvent(title: String, startTime: Long, endTime: Long, location: String? = null)
    suspend fun deleteCalendarEvent(eventId: String)
    suspend fun generateGoalPlan(goalTitle: String): String
}
