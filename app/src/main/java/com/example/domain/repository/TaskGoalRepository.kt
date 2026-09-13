package com.example.domain.repository

import com.example.domain.model.CalendarEvent
import com.example.domain.model.GoalMilestone
import com.example.domain.model.GoalPlan
import com.example.domain.model.Task
import com.example.domain.planner.DecomposedGoalResult
import kotlinx.coroutines.flow.Flow

interface TaskGoalRepository {
    val allTasks: Flow<List<Task>>
    val allCalendarEvents: Flow<List<CalendarEvent>>
    val allGoalPlans: Flow<List<GoalPlan>>
    
    fun getMilestonesForGoal(goalId: Long): Flow<List<GoalMilestone>>

    suspend fun addTask(
        title: String,
        priority: String = "MEDIUM",
        category: String = "General",
        estimatedMinutes: Int = 30,
        notes: String = ""
    ): Long
    
    suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean)
    suspend fun deleteTask(task: Task)
    
    suspend fun addCalendarEvent(event: CalendarEvent): Long
    suspend fun deleteCalendarEvent(eventId: Long)
    
    suspend fun decomposeGoal(
        title: String, 
        description: String, 
        category: String, 
        targetDate: String
    ): DecomposedGoalResult
    
    suspend fun executeMilestoneTool(milestoneId: Long, goalId: Long): String
    suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean)
    suspend fun deleteGoal(goalId: Long)
}
