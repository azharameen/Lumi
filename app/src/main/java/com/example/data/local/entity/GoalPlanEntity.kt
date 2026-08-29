package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "goal_plans")
data class GoalPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val category: String = "Productivity", // "Career", "Health", "Learning", "Creative", "Productivity"
    val targetDate: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED", "PAUSED"
    val totalSteps: Int = 0,
    val completedSteps: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isAiGenerated: Boolean = true,
    val tagsJson: String = "[]"
)

@Entity(
    tableName = "goal_milestones",
    foreignKeys = [
        ForeignKey(
            entity = GoalPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class GoalMilestoneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val phaseNumber: Int = 1,
    val phaseTitle: String,
    val stepTitle: String,
    val stepDescription: String,
    val suggestedTool: String = "NONE", // "CALENDAR", "DOC", "TASK", "GITHUB", "SLACK", "NONE"
    val isCompleted: Boolean = false,
    val executionOutput: String = "",
    val scheduledDate: String = ""
)
