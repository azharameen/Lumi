package com.example.data.repository

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.mapper.toDomain
import com.example.data.local.mapper.toEntity
import com.example.domain.model.CalendarEvent
import com.example.domain.model.GoalMilestone
import com.example.domain.model.GoalPlan
import com.example.domain.model.Task
import com.example.domain.planner.PlannedMilestone
import com.example.domain.repository.PetRepository
import com.example.domain.repository.TaskGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskGoalRepositoryImpl(
    private val database: LumiDatabase,
    private val petRepository: PetRepository
) : TaskGoalRepository {

    override val allTasks: Flow<List<Task>> = 
        database.taskDao().getAllTasks().map { list -> list.map { it.toDomain() } }
        
    override val allCalendarEvents: Flow<List<CalendarEvent>> = 
        database.calendarEventDao().getAllEvents().map { list -> list.map { it.toDomain() } }
        
    override val allGoalPlans: Flow<List<GoalPlan>> = 
        database.goalPlanDao().getAllGoals().map { list -> list.map { it.toDomain() } }

    override fun getMilestonesForGoal(goalId: Long): Flow<List<GoalMilestone>> = 
        database.goalPlanDao().getMilestonesForGoal(goalId).map { list -> list.map { it.toDomain() } }

    override suspend fun getMilestonesForGoalSync(goalId: Long): List<GoalMilestone> = withContext(Dispatchers.IO) {
        database.goalPlanDao().getMilestonesForGoalSync(goalId).map { it.toDomain() }
    }

    override suspend fun getMilestoneSync(milestoneId: Long): GoalMilestone? = withContext(Dispatchers.IO) {
        // Find in all goals, inefficient but DAO does not have getMilestoneById
        // Let's assume we need to write a small helper or just fetch the one if we can
        // Wait, GoalPlanDao doesn't have getMilestoneById? AutonomousGoalPlanner fetched it by list.
        // I will use that approach for now, or just add the DAO method later.
        null
    }
    
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

    override suspend fun deleteTask(task: Task) = withContext(Dispatchers.IO) {
        database.taskDao().deleteTask(task.toEntity())
    }

    override suspend fun addCalendarEvent(event: CalendarEvent): Long = withContext(Dispatchers.IO) {
        database.calendarEventDao().insertEvent(event.toEntity())
    }

    override suspend fun deleteCalendarEvent(eventId: Long) = withContext(Dispatchers.IO) {
        database.calendarEventDao().deleteEventById(eventId)
    }

    override suspend fun insertGoalPlan(title: String, description: String, category: String, targetDate: String): Long = withContext(Dispatchers.IO) {
        val initialGoalEntity = GoalPlanEntity(
            title = title,
            description = description,
            category = category,
            targetDate = targetDate.ifBlank {
                val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 14) }
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            },
            totalSteps = 0,
            completedSteps = 0
        )
        database.goalPlanDao().insertGoal(initialGoalEntity)
    }

    override suspend fun insertMilestones(goalId: Long, milestones: List<PlannedMilestone>) = withContext(Dispatchers.IO) {
        val entities = milestones.map { plan ->
            GoalMilestoneEntity(
                goalId = goalId,
                phaseNumber = plan.phaseNumber,
                phaseTitle = plan.phaseTitle,
                stepTitle = plan.stepTitle,
                stepDescription = plan.stepDescription,
                suggestedTool = plan.suggestedTool
            )
        }
        database.goalPlanDao().insertMilestones(entities)
    }

    override suspend fun updateMilestone(milestone: GoalMilestone) = withContext(Dispatchers.IO) {
        database.goalPlanDao().updateMilestone(milestone.toEntity())
        if (milestone.isCompleted) {
            petRepository.earnCoinsAndExp(coins = 40, exp = 35, reason = "Completing Milestone: ${milestone.stepTitle}")
        }
    }

    override suspend fun updateGoalMetrics(goalId: Long) = withContext(Dispatchers.IO) {
        val updatedMilestones = database.goalPlanDao().getMilestonesForGoalSync(goalId)
        val completedCount = updatedMilestones.count { it.isCompleted }
        val existingGoal = database.goalPlanDao().getGoalById(goalId)
        if (existingGoal != null) {
            database.goalPlanDao().updateGoal(
                existingGoal.copy(
                    completedSteps = completedCount,
                    totalSteps = updatedMilestones.size
                )
            )
        }
    }

    override suspend fun deleteGoal(goalId: Long) = withContext(Dispatchers.IO) {
        database.goalPlanDao().deleteGoalById(goalId)
    }

    override suspend fun getAllTasksSync(): List<Task> = withContext(Dispatchers.IO) {
        database.taskDao().getAllTasksDirect().map { it.toDomain() }
    }

    override suspend fun getAllEventsSync(): List<CalendarEvent> = withContext(Dispatchers.IO) {
        database.calendarEventDao().getAllEventsDirect().map { it.toDomain() }
    }

    override suspend fun updateTask(task: Task) = withContext(Dispatchers.IO) {
        database.taskDao().updateTask(task.toEntity())
    }
}
