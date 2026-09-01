package com.example.domain.planner

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.remote.FirebaseAiCloudEngine
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.domain.connectors.IntegrationService
import com.example.domain.tools.AgentToolDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Result data class returned when decomposing a high-level goal.
 */
data class DecomposedGoalResult(
    val goalId: Long,
    val totalMilestones: Int,
    val phasesCount: Int,
    val summary: String
)

/**
 * Structured milestone data representation before Room persistence.
 */
data class PlannedMilestone(
    val phaseNumber: Int,
    val phaseTitle: String,
    val stepTitle: String,
    val stepDescription: String,
    val suggestedTool: String,
    val scheduledDate: String = ""
)

/**
 * Autonomous goal planner and agent swarm orchestrator.
 * Parses structured AI plans with robust schema validation directly into Room entities,
 * eliminating brittle regex extractions.
 */
class AutonomousGoalPlanner(
    private val database: LumiDatabase,
    private val toolDispatcher: AgentToolDispatcher,
    private val integrationService: IntegrationService
) {

    /**
     * Decomposes a high-level user goal into structured Room milestones.
     */
    suspend fun decomposeAndSaveGoal(
        title: String,
        description: String = "",
        category: String = "Productivity",
        targetDate: String = ""
    ): DecomposedGoalResult = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()

        // 1. Initial Plan Entity Creation in Room
        val initialGoalEntity = GoalPlanEntity(
            title = title,
            description = description,
            category = category,
            targetDate = targetDate.ifBlank {
                val cal = java.util.Calendar.getInstance().apply { add(java.util.Calendar.DAY_OF_YEAR, 14) }
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            },
            totalSteps = 0,
            completedSteps = 0
        )
        val goalId = database.goalPlanDao().insertGoal(initialGoalEntity)

        // 2. Structured JSON generation via Firebase AI Logic or deterministic fallback
        val milestones = try {
            val prompt = """
                You are an Autonomous AI Goal Planner. Decompose this objective into 3 to 4 sequential phases, each with 1 to 2 concrete execution steps.
                
                Goal: "$title"
                Details: "$description"
                Category: "$category"
                
                For each step, specify the best automated action tool:
                - "CALENDAR" (schedule focus blocks, events)
                - "DOC" (create docs, specifications, outlines)
                - "GITHUB" (open issues, code tracking)
                - "SLACK" (team broadcast, status updates)
                - "TASK" (internal action item)
                - "NONE" (general step)
                
                Return ONLY a JSON array of objects with keys:
                [
                  {
                    "phaseNumber": 1,
                    "phaseTitle": "Phase 1: Foundation",
                    "stepTitle": "Step title",
                    "stepDescription": "Detailed actionable step",
                    "suggestedTool": "CALENDAR|DOC|GITHUB|SLACK|TASK|NONE"
                  }
                ]
            """.trimIndent()

            val rawText = FirebaseAiCloudEngine.getInstance().generateStructuredText(
                systemInstruction = "You are Lumi's Goal Planner. Always output strictly valid JSON array of milestone objects.",
                prompt = prompt,
                temperature = 0.2f
            )

            if (rawText.isNotBlank()) {
                parseJsonMilestones(rawText)
            } else {
                generateFallbackMilestones(title, category)
            }
        } catch (e: Exception) {
            generateFallbackMilestones(title, category)
        }

        // 3. Persist Validated Milestones into Room
        val entities = milestones.map { m ->
            GoalMilestoneEntity(
                goalId = goalId,
                phaseNumber = m.phaseNumber,
                phaseTitle = m.phaseTitle,
                stepTitle = m.stepTitle,
                stepDescription = m.stepDescription,
                suggestedTool = m.suggestedTool,
                isCompleted = false,
                executionOutput = ""
            )
        }
        database.goalPlanDao().insertMilestones(entities)

        // 4. Update parent GoalPlanEntity metrics
        val distinctPhases = milestones.map { it.phaseNumber }.distinct().size
        database.goalPlanDao().updateGoal(
            initialGoalEntity.copy(
                id = goalId,
                totalSteps = entities.size
            )
        )

        DecomposedGoalResult(
            goalId = goalId,
            totalMilestones = entities.size,
            phasesCount = distinctPhases,
            summary = "Decomposed '$title' into ${entities.size} actionable milestones across $distinctPhases phases."
        )
    }

    /**
     * Executes an individual milestone autonomously and synchronizes state into Room.
     */
    suspend fun executeMilestoneTool(milestoneId: Long, goalId: Long): String = withContext(Dispatchers.IO) {
        val milestones = database.goalPlanDao().getMilestonesForGoalSync(goalId)
        val milestone = milestones.find { it.id == milestoneId }
            ?: return@withContext "Milestone not found"

        var executionOutput = ""

        try {
            when (milestone.suggestedTool.uppercase()) {
                "CALENDAR" -> {
                    val (res, report) = toolDispatcher.executeTool(
                        "add_calendar_event",
                        mapOf(
                            "title" to milestone.stepTitle,
                            "startTimeOffsetHours" to 1.0,
                            "durationMinutes" to 45,
                            "category" to "Focus",
                            "description" to milestone.stepDescription
                        )
                    )
                    executionOutput = "Scheduled calendar block: ${report.description}"
                }

                "DOC" -> {
                    val (res, report) = toolDispatcher.executeTool(
                        "google_create_doc",
                        mapOf(
                            "title" to "Roadmap: ${milestone.stepTitle}",
                            "content" to "# ${milestone.stepTitle}\n\n${milestone.stepDescription}\n\nAutonomously generated by Lumi Planner on ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}"
                        )
                    )
                    executionOutput = "Created Google Doc: ${report.description}"
                }

                "GITHUB" -> {
                    val (res, report) = toolDispatcher.executeTool(
                        "github_create_issue",
                        mapOf(
                            "repo" to "azharameen/lumi-companion",
                            "title" to milestone.stepTitle,
                            "body" to milestone.stepDescription
                        )
                    )
                    executionOutput = "GitHub Issue: ${report.description}"
                }

                "SLACK" -> {
                    val (res, report) = toolDispatcher.executeTool(
                        "slack_post_message",
                        mapOf(
                            "channel" to "#standup",
                            "message" to "🚀 Starting Milestone: ${milestone.stepTitle} - ${milestone.stepDescription}"
                        )
                    )
                    executionOutput = "Broadcasted to Slack: ${report.description}"
                }

                else -> {
                    // Standard Task Creation in Room
                    database.taskDao().insertTask(
                        TaskEntity(
                            title = milestone.stepTitle,
                            notes = milestone.stepDescription,
                            priority = "HIGH",
                            category = "Goals",
                            estimatedMinutes = 30,
                            isCompleted = false
                        )
                    )
                    executionOutput = "Created Task Item in Task Manager"
                }
            }

            // Mark milestone completed in Room
            database.goalPlanDao().updateMilestone(
                milestone.copy(
                    isCompleted = true,
                    executionOutput = executionOutput
                )
            )

            // Update parent GoalPlanEntity metrics
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

            executionOutput
        } catch (e: Exception) {
            val errorMsg = "Execution error: ${e.localizedMessage ?: "Failed"}"
            database.goalPlanDao().updateMilestone(
                milestone.copy(executionOutput = errorMsg)
            )
            errorMsg
        }
    }

    suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        val milestones = database.goalPlanDao().getMilestonesForGoalSync(goalId)
        val milestone = milestones.find { it.id == milestoneId }
        if (milestone != null) {
            database.goalPlanDao().updateMilestone(milestone.copy(isCompleted = isCompleted))
            val updated = database.goalPlanDao().getMilestonesForGoalSync(goalId)
            val completedCount = updated.count { it.isCompleted }
            val existingGoal = database.goalPlanDao().getGoalById(goalId)
            if (existingGoal != null) {
                database.goalPlanDao().updateGoal(
                    existingGoal.copy(
                        completedSteps = completedCount,
                        totalSteps = updated.size
                    )
                )
            }
        }
    }

    private fun parseJsonMilestones(rawJson: String): List<PlannedMilestone> {
        val list = mutableListOf<PlannedMilestone>()
        try {
            val clean = rawJson.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val array = if (clean.startsWith("[")) {
                JSONArray(clean)
            } else if (clean.startsWith("{")) {
                val obj = JSONObject(clean)
                obj.optJSONArray("milestones") ?: JSONArray()
            } else {
                JSONArray()
            }

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val phaseNum = item.optInt("phaseNumber", (i / 2) + 1)
                val phaseTitle = item.optString("phaseTitle", "Phase $phaseNum")
                val stepTitle = item.optString("stepTitle", "Milestone ${i + 1}")
                val stepDesc = item.optString("stepDescription", "Action step")
                val tool = when (item.optString("suggestedTool", "TASK").uppercase()) {
                    "CALENDAR", "DOC", "GITHUB", "SLACK", "TASK" -> item.optString("suggestedTool", "TASK").uppercase()
                    else -> "TASK"
                }
                list.add(PlannedMilestone(phaseNum, phaseTitle, stepTitle, stepDesc, tool))
            }
        } catch (_: Exception) {}

        return if (list.isNotEmpty()) list else generateFallbackMilestones("Goal", "Productivity")
    }

    private fun generateFallbackMilestones(title: String, category: String): List<PlannedMilestone> {
        return listOf(
            PlannedMilestone(
                phaseNumber = 1,
                phaseTitle = "Phase 1: Discovery & Architecture",
                stepTitle = "Draft Project Scope Document",
                stepDescription = "Synthesize requirements, milestones, and success criteria for '$title'.",
                suggestedTool = "DOC"
            ),
            PlannedMilestone(
                phaseNumber = 2,
                phaseTitle = "Phase 2: Execution Sprint",
                stepTitle = "Deep Focus Implementation Block",
                stepDescription = "Schedule dedicated 45-minute focus session to build core components.",
                suggestedTool = "CALENDAR"
            ),
            PlannedMilestone(
                phaseNumber = 3,
                phaseTitle = "Phase 3: Integration & Testing",
                stepTitle = "Open Tracking Issue on GitHub",
                stepDescription = "File tracking issue for QA, edge cases, and continuous integration.",
                suggestedTool = "GITHUB"
            ),
            PlannedMilestone(
                phaseNumber = 4,
                phaseTitle = "Phase 4: Launch & Retrospective",
                stepTitle = "Broadcast Objective Success to Team",
                stepDescription = "Send completion message and summary update to team channel.",
                suggestedTool = "SLACK"
            )
        )
    }
}
