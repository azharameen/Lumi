package com.example.domain.planner

import android.content.Context
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.domain.connectors.IntegrationService
import com.example.domain.tools.AgentToolDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DecomposedGoalResult(
    val goalId: Long,
    val title: String,
    val category: String,
    val totalPhases: Int,
    val totalMilestones: Int,
    val milestones: List<GoalMilestoneEntity>
)

class AutonomousGoalPlanner(
    private val context: Context,
    private val database: LumiDatabase,
    private val toolDispatcher: AgentToolDispatcher,
    private val integrationService: IntegrationService
) {
    private val goalDao = database.goalPlanDao()

    /**
     * Decomposes a natural language goal into structured multi-phase milestones with tool assignments.
     */
    suspend fun decomposeAndSaveGoal(
        goalTitle: String,
        goalDescription: String,
        category: String = "Productivity",
        targetDate: String = ""
    ): DecomposedGoalResult = withContext(Dispatchers.IO) {
        val (detectedCategory, milestones) = generatePlanStructure(goalTitle, goalDescription, category)

        val planEntity = GoalPlanEntity(
            title = goalTitle,
            description = goalDescription.ifEmpty { "Autonomous agentic execution plan for $goalTitle" },
            category = detectedCategory,
            targetDate = targetDate.ifEmpty { getDefaultTargetDate() },
            status = "ACTIVE",
            totalSteps = milestones.size,
            completedSteps = 0,
            createdAt = System.currentTimeMillis(),
            isAiGenerated = true
        )

        val insertedGoalId = goalDao.insertGoal(planEntity)

        val mappedMilestones = milestones.map {
            it.copy(goalId = insertedGoalId)
        }
        goalDao.insertMilestones(mappedMilestones)

        // Award companion XP for autonomous planning
        addPetExp(35, 15)

        DecomposedGoalResult(
            goalId = insertedGoalId,
            title = goalTitle,
            category = detectedCategory,
            totalPhases = mappedMilestones.map { it.phaseNumber }.distinct().size,
            totalMilestones = mappedMilestones.size,
            milestones = mappedMilestones
        )
    }

    private suspend fun addPetExp(expGained: Int, happinessGained: Int) {
        val currentPet = database.petEvolutionDao().getPetEvolutionDirect() ?: com.example.data.local.entity.PetEvolutionEntity()
        val updated = currentPet.copy(
            exp = currentPet.exp + expGained,
            happiness = (currentPet.happiness + happinessGained).coerceIn(0, 100)
        )
        database.petEvolutionDao().insertOrUpdate(updated)
    }

    /**
     * Executes the assigned agent tool for a milestone (e.g. Schedule Calendar Block, Create Task, Draft Doc, Open GitHub Issue).
     */
    suspend fun executeMilestoneTool(milestoneId: Long, goalId: Long): String = withContext(Dispatchers.IO) {
        val allMilestones = goalDao.getMilestonesForGoalSync(goalId)
        val milestone = allMilestones.find { it.id == milestoneId } ?: return@withContext "Milestone not found"
        val goal = goalDao.getGoalById(goalId)

        var resultText = "Executed milestone successfully"

        when (milestone.suggestedTool.uppercase()) {
            "CALENDAR" -> {
                val (_, report) = toolDispatcher.executeTool(
                    "add_calendar_event",
                    mapOf(
                        "title" to "[Goal Focus] ${milestone.stepTitle}",
                        "description" to "${milestone.stepDescription} (Part of goal: ${goal?.title ?: ""})",
                        "startTimeOffsetHours" to 1.0,
                        "durationMinutes" to 60
                    )
                )
                resultText = report.description
            }
            "TASK" -> {
                val (_, report) = toolDispatcher.executeTool(
                    "create_task",
                    mapOf(
                        "title" to milestone.stepTitle,
                        "priority" to "HIGH",
                        "notes" to milestone.stepDescription
                    )
                )
                resultText = report.description
            }
            "DOC" -> {
                val (_, report) = toolDispatcher.executeTool(
                    "google_create_doc",
                    mapOf(
                        "title" to "${goal?.title ?: "Goal"} - ${milestone.stepTitle}",
                        "initial_content" to "# ${milestone.stepTitle}\n\n**Goal**: ${goal?.title}\n\n## Overview\n${milestone.stepDescription}\n\n## Action Items\n- [ ] Initial Research\n- [ ] Draft Implementation\n- [ ] Review & Publish"
                    )
                )
                resultText = report.description
            }
            "GITHUB" -> {
                val (_, report) = toolDispatcher.executeTool(
                    "github_create_issue",
                    mapOf(
                        "repo_name" to "workspace-projects",
                        "title" to "[Milestone] ${milestone.stepTitle}",
                        "body" to "${milestone.stepDescription}\n\n*Linked Goal: ${goal?.title ?: ""}*",
                        "labels" to listOf("goal-milestone", "lumi-agent")
                    )
                )
                resultText = report.description
            }
            "SLACK" -> {
                val (_, report) = toolDispatcher.executeTool(
                    "slack_post_message",
                    mapOf(
                        "channel" to "#general",
                        "message" to "🚀 Starting goal milestone: *${milestone.stepTitle}* (${goal?.title ?: ""})"
                    )
                )
                resultText = report.description
            }
            else -> {
                resultText = "Marked as in-progress"
            }
        }

        val updated = milestone.copy(
            isCompleted = true,
            executionOutput = resultText
        )
        goalDao.updateMilestone(updated)

        // Update overall goal completed count
        updateGoalProgress(goalId)

        resultText
    }

    suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        goalDao.setMilestoneCompleted(milestoneId, isCompleted)
        updateGoalProgress(goalId)
    }

    private suspend fun updateGoalProgress(goalId: Long) {
        val goal = goalDao.getGoalById(goalId) ?: return
        val milestones = goalDao.getMilestonesForGoalSync(goalId)
        val completedCount = milestones.count { it.isCompleted }
        val totalCount = milestones.size

        val newStatus = if (completedCount == totalCount && totalCount > 0) "COMPLETED" else "ACTIVE"
        val updatedGoal = goal.copy(
            completedSteps = completedCount,
            totalSteps = totalCount,
            status = newStatus
        )
        goalDao.updateGoal(updatedGoal)

        if (newStatus == "COMPLETED") {
            addPetXp(60)
        }
    }

    private suspend fun addPetXp(amount: Int) {
        val pet = database.petEvolutionDao().getPetEvolutionDirect() ?: return
        val newExp = pet.exp + amount
        var newLevel = pet.level
        var expToNext = pet.expToNextLevel
        var currentExp = newExp
        while (currentExp >= expToNext) {
            currentExp -= expToNext
            newLevel++
            expToNext = (expToNext * 1.25f).toInt().coerceAtLeast(50)
        }
        database.petEvolutionDao().insertOrUpdate(
            pet.copy(
                exp = currentExp,
                level = newLevel,
                expToNextLevel = expToNext,
                happiness = minOf(100, pet.happiness + 10)
            )
        )
    }

    private fun generatePlanStructure(
        title: String,
        description: String,
        category: String
    ): Pair<String, List<GoalMilestoneEntity>> {
        val lower = (title + " " + description + " " + category).lowercase()

        val detectedCategory = when {
            lower.contains("health") || lower.contains("run") || lower.contains("marathon") || lower.contains("diet") || lower.contains("workout") -> "Health"
            lower.contains("learn") || lower.contains("read") || lower.contains("study") || lower.contains("course") -> "Learning"
            lower.contains("launch") || lower.contains("app") || lower.contains("code") || lower.contains("dev") || lower.contains("github") -> "Engineering"
            lower.contains("write") || lower.contains("book") || lower.contains("music") || lower.contains("design") -> "Creative"
            else -> "Productivity"
        }

        val milestones = when (detectedCategory) {
            "Engineering" -> listOf(
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Architecture & Specs",
                    stepTitle = "Draft Product Architecture Spec",
                    stepDescription = "Outline database schemas, integration contracts, and core features.",
                    suggestedTool = "DOC"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Architecture & Specs",
                    stepTitle = "Open Tracking Issues on GitHub",
                    stepDescription = "Create issues for MVP deliverables and backlog milestones.",
                    suggestedTool = "GITHUB"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Deep Implementation",
                    stepTitle = "Schedule 2-Hour Deep Focus Code Blocks",
                    stepDescription = "Protect uninterrupted calendar slots for building core logic.",
                    suggestedTool = "CALENDAR"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Deep Implementation",
                    stepTitle = "Verify Automated Test Suite & Code Review",
                    stepDescription = "Run unit tests and lint checks across the codebase.",
                    suggestedTool = "TASK"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 3,
                    phaseTitle = "Phase 3: Launch & Rollout",
                    stepTitle = "Broadcast Launch Changelog to Slack",
                    stepDescription = "Share feature demo, release notes, and documentation with the team.",
                    suggestedTool = "SLACK"
                )
            )
            "Health" -> listOf(
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Baseline & Preparation",
                    stepTitle = "Log Baseline Hydration & Sleep Routine",
                    stepDescription = "Establish 2L/day water intake and consistent 8-hour sleep schedule.",
                    suggestedTool = "TASK"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Baseline & Preparation",
                    stepTitle = "Schedule Recurring Morning Workout Blocks",
                    stepDescription = "Block 45-minute calendar slots on Mon/Wed/Fri mornings.",
                    suggestedTool = "CALENDAR"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Progressive Overload",
                    stepTitle = "Increase Cardio Distance & Pace by 10%",
                    stepDescription = "Track heart rate zones and recovery intervals.",
                    suggestedTool = "TASK"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 3,
                    phaseTitle = "Phase 3: Recovery & Maintenance",
                    stepTitle = "Complete Weekly Mindful Relaxation Reflection",
                    stepDescription = "Perform 10-minute guided breathing and sleep reflection.",
                    suggestedTool = "TASK"
                )
            )
            "Learning" -> listOf(
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Curriculum & Syllabus",
                    stepTitle = "Synthesize Core Learning Topics in Google Doc",
                    stepDescription = "Create summary index of key textbooks, research papers, and lectures.",
                    suggestedTool = "DOC"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Daily Study Blocks",
                    stepTitle = "Schedule 1-Hour Daily Study Sessions",
                    stepDescription = "Set dedicated calendar focus time with DND notifications.",
                    suggestedTool = "CALENDAR"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 3,
                    phaseTitle = "Phase 3: Practical Project Application",
                    stepTitle = "Build & Deploy Hands-On Capstone Project",
                    stepDescription = "Apply theoretical concepts to a real-world working project.",
                    suggestedTool = "TASK"
                )
            )
            else -> listOf(
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Discovery & Strategy",
                    stepTitle = "Draft Project Plan & Key Milestones",
                    stepDescription = "Create structured outline with deliverables and timelines.",
                    suggestedTool = "DOC"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Phase 1: Discovery & Strategy",
                    stepTitle = "Schedule Kickoff & Review Calendar Blocks",
                    stepDescription = "Reserve time for initial setup and strategic milestones.",
                    suggestedTool = "CALENDAR"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 2,
                    phaseTitle = "Phase 2: Active Execution",
                    stepTitle = "Execute Primary Priority Tasks",
                    stepDescription = "Complete core deliverables tracked in prioritized task list.",
                    suggestedTool = "TASK"
                ),
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 3,
                    phaseTitle = "Phase 3: Review & Broadcast",
                    stepTitle = "Publish Summary Update to Team Channel",
                    stepDescription = "Share completed outcomes, lessons learned, and next milestones.",
                    suggestedTool = "SLACK"
                )
            )
        }

        return Pair(detectedCategory, milestones)
    }

    private fun getDefaultTargetDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val twoWeeksLater = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000)
        return sdf.format(Date(twoWeeksLater))
    }
}
