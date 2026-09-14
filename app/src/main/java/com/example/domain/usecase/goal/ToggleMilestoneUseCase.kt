package com.example.domain.usecase.goal

import com.example.domain.planner.AutonomousGoalPlanner

class ToggleMilestoneUseCase(
    private val goalPlanner: AutonomousGoalPlanner
) {
    suspend operator fun invoke(milestoneId: Long, goalId: Long, isCompleted: Boolean) {
        goalPlanner.toggleMilestone(milestoneId, goalId, isCompleted)
    }
}
