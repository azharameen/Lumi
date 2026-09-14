package com.example.domain.repository

import com.example.domain.model.CalendarEvent
import com.example.domain.model.GoalMilestone
import com.example.domain.model.GoalPlan
import com.example.domain.model.Task
import com.example.domain.planner.PlannedMilestone
import kotlinx.coroutines.flow.Flow

interface TaskGoalRepository {
    val allTasks: Flow<List<Task>>
    val allCalendarEvents: Flow<List<CalendarEvent>>
    val allGoalPlans: Flow<List<GoalPlan>>
    
    fun getMilestonesForGoal(goalId: Long): Flow<List<GoalMilestone>>
    suspend fun getMilestonesForGoalSync(goalId: Long): List<GoalMilestone>
    suspend fun getMilestoneSync(milestoneId: Long): GoalMilestone?
    
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
    
    suspend fun insertGoalPlan(title: String, description: String, category: String, targetDate: String): Long
    suspend fun insertMilestones(goalId: Long, milestones: List<PlannedMilestone>)
    suspend fun updateMilestone(milestone: GoalMilestone)
    suspend fun updateGoalMetrics(goalId: Long)
    
    suspend fun deleteGoal(goalId: Long)
    
    suspend fun getAllTasksSync(): List<Task>
    suspend fun getAllEventsSync(): List<CalendarEvent>
    suspend fun updateTask(task: Task)
}
