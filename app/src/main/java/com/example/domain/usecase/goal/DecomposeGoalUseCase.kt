package com.example.domain.usecase.goal

import com.example.domain.planner.AutonomousGoalPlanner
import com.example.domain.planner.DecomposedGoalResult

class DecomposeGoalUseCase(
    private val goalPlanner: AutonomousGoalPlanner
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        category: String,
        targetDate: String
    ): DecomposedGoalResult {
        return goalPlanner.decomposeAndSaveGoal(title, description, category, targetDate)
    }
}
