package com.example.data.repository

import com.example.data.local.dao.CalendarEventDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.mapper.toDomain
import com.example.domain.model.CalendarEvent
import com.example.domain.model.Task
import com.example.domain.planner.AutonomousGoalPlanner
import com.example.domain.repository.TaskGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TaskGoalRepositoryImpl(
    private val taskDao: TaskDao,
    private val calendarEventDao: CalendarEventDao,
    private val goalPlanner: AutonomousGoalPlanner? = null
) : TaskGoalRepository {

    override val tasks: Flow<List<Task>> = taskDao.getAllTasks()
        .map { list -> list.map { it.toDomain() } }

    override val calendarEvents: Flow<List<CalendarEvent>> = calendarEventDao.getAllEvents()
        .map { list -> list.map { it.toDomain() } }

    override suspend fun addTask(title: String, category: String, dueDate: Long?) {
        withContext(Dispatchers.IO) {
            val task = TaskEntity(
                title = title,
                category = category,
                dueDate = dueDate,
                createdAt = System.currentTimeMillis()
            )
            taskDao.insertTask(task)
        }
    }

    override suspend fun completeTask(taskId: String) {
        withContext(Dispatchers.IO) {
            val idLong = taskId.toLongOrNull() ?: return@withContext
            taskDao.setTaskCompleted(idLong, true)
        }
    }

    override suspend fun deleteTask(taskId: String) {
        withContext(Dispatchers.IO) {
            val idLong = taskId.toLongOrNull() ?: return@withContext
            val task = taskDao.getTaskById(idLong)
            if (task != null) {
                taskDao.deleteTask(task)
            }
        }
    }

    override suspend fun addCalendarEvent(title: String, startTime: Long, endTime: Long, location: String?) {
        withContext(Dispatchers.IO) {
            val event = CalendarEventEntity(
                title = title,
                startTimeMillis = startTime,
                endTimeMillis = endTime,
                location = location ?: ""
            )
            calendarEventDao.insertEvent(event)
        }
    }

    override suspend fun deleteCalendarEvent(eventId: String) {
        withContext(Dispatchers.IO) {
            val idLong = eventId.toLongOrNull() ?: return@withContext
            calendarEventDao.deleteEventById(idLong)
        }
    }

    override suspend fun generateGoalPlan(goalTitle: String): String = withContext(Dispatchers.IO) {
        val plan = goalPlanner?.decomposeAndSaveGoal(goalTitle, "General")
        plan?.title ?: "Goal plan generated for $goalTitle"
    }
}
