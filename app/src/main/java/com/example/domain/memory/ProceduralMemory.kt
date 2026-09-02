package com.example.domain.memory

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.GoalMilestoneEntity

/**
 * 4. Procedural Memory Tier:
 * Saved multi-step task workflows and goal plans derived from successfully completed goals.
 */
class ProceduralMemory(private val database: LumiDatabase) {

    suspend fun getCompletedGoalTemplates(): List<Pair<GoalPlanEntity, List<GoalMilestoneEntity>>> {
        val goals = database.goalPlanDao().getMilestonesForGoalSync(1) // Placeholder query
        return emptyList()
    }
}
