package com.example.domain.usecase.goal

import com.example.domain.planner.AutonomousGoalPlanner

class ExecuteMilestoneUseCase(
    private val goalPlanner: AutonomousGoalPlanner
) {
    suspend operator fun invoke(milestoneId: Long, goalId: Long): String {
        return goalPlanner.executeMilestoneTool(milestoneId, goalId)
    }
}
