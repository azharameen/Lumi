package com.example.data.repository

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.TaskEntity
import com.example.domain.planner.AutonomousGoalPlanner
import com.example.domain.planner.DecomposedGoalResult
import com.example.domain.repository.PetRepository
import com.example.domain.repository.TaskGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TaskGoalRepositoryImpl(
    private val database: LumiDatabase,
    private val goalPlanner: AutonomousGoalPlanner,
    private val petRepository: PetRepository
) : TaskGoalRepository {

    override val allTasks: Flow<List<TaskEntity>> = database.taskDao().getAllTasks()
    
    override val allCalendarEvents: Flow<List<CalendarEventEntity>> = database.calendarEventDao().getAllEvents()
    
    override val allGoalPlans: Flow<List<GoalPlanEntity>> = database.goalPlanDao().getAllGoals()

    override fun getMilestonesForGoal(goalId: Long): Flow<List<GoalMilestoneEntity>> = 
        database.goalPlanDao().getMilestonesForGoal(goalId)

    override suspend fun addTask(
        title: String,
        priority: String,
        category: String,
        estimatedMinutes: Int,
        notes: String
    ): Long = withContext(Dispatchers.IO) {
        database.taskDao().insertTask(
            TaskEntity(
                title = title,
                notes = notes,
                priority = priority,
                category = category,
                estimatedMinutes = estimatedMinutes
            )
        )
    }

    override suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        database.taskDao().setTaskCompleted(taskId, isCompleted)
        if (isCompleted) {
            petRepository.earnCoinsAndExp(coins = 25, exp = 20, reason = "Completing Task")
        }
    }

    override suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
        database.taskDao().deleteTask(task)
    }

    override suspend fun addCalendarEvent(event: CalendarEventEntity): Long = withContext(Dispatchers.IO) {
        database.calendarEventDao().insertEvent(event)
    }

    override suspend fun deleteCalendarEvent(eventId: Long) = withContext(Dispatchers.IO) {
        database.calendarEventDao().deleteEventById(eventId)
    }

    override suspend fun decomposeGoal(
        title: String,
        description: String,
        category: String,
        targetDate: String
    ): DecomposedGoalResult {
        return goalPlanner.decomposeAndSaveGoal(title, description, category, targetDate)
    }

    override suspend fun executeMilestoneTool(milestoneId: Long, goalId: Long): String {
        return goalPlanner.executeMilestoneTool(milestoneId, goalId)
    }

    override suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) {
        goalPlanner.toggleMilestone(milestoneId, goalId, isCompleted)
        if (isCompleted) {
            petRepository.earnCoinsAndExp(coins = 35, exp = 30, reason = "Goal Milestone Conquered")
            petRepository.earnGems(gems = 2, reason = "Goal Milestone")
        }
    }

    override suspend fun deleteGoal(goalId: Long) = withContext(Dispatchers.IO) {
        database.goalPlanDao().deleteGoalById(goalId)
    }
}
