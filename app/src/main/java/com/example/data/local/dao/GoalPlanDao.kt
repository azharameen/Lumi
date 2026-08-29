package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalPlanDao {
    @Query("SELECT * FROM goal_plans ORDER BY createdAt DESC")
    fun getAllGoals(): Flow<List<GoalPlanEntity>>

    @Query("SELECT * FROM goal_plans WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalPlanEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalPlanEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalPlanEntity)

    @Query("DELETE FROM goal_plans WHERE id = :id")
    suspend fun deleteGoalById(id: Long)

    // Milestones
    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY phaseNumber ASC, id ASC")
    fun getMilestonesForGoal(goalId: Long): Flow<List<GoalMilestoneEntity>>

    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY phaseNumber ASC, id ASC")
    suspend fun getMilestonesForGoalSync(goalId: Long): List<GoalMilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<GoalMilestoneEntity>)

    @Update
    suspend fun updateMilestone(milestone: GoalMilestoneEntity)

    @Query("UPDATE goal_milestones SET isCompleted = :completed WHERE id = :id")
    suspend fun setMilestoneCompleted(id: Long, completed: Boolean)
}
