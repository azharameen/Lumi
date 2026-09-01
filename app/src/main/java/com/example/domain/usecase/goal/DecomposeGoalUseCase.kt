package com.example.domain.usecase.goal

import com.example.domain.planner.DecomposedGoalResult
import com.example.domain.repository.TaskGoalRepository

class DecomposeGoalUseCase(
    private val taskGoalRepository: TaskGoalRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        category: String,
        targetDate: String
    ): DecomposedGoalResult {
        return taskGoalRepository.decomposeGoal(title, description, category, targetDate)
    }
}
