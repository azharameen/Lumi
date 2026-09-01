package com.example.domain.repository

import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.TaskEntity
import com.example.domain.planner.DecomposedGoalResult
import kotlinx.coroutines.flow.Flow

interface TaskGoalRepository {
    val allTasks: Flow<List<TaskEntity>>
    val allCalendarEvents: Flow<List<CalendarEventEntity>>
    val allGoalPlans: Flow<List<GoalPlanEntity>>
    
    fun getMilestonesForGoal(goalId: Long): Flow<List<GoalMilestoneEntity>>

    suspend fun addTask(
        title: String,
        priority: String = "MEDIUM",
        category: String = "General",
        estimatedMinutes: Int = 30,
        notes: String = ""
    ): Long
    
    suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean)
    suspend fun deleteTask(task: TaskEntity)
    
    suspend fun addCalendarEvent(event: CalendarEventEntity): Long
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
