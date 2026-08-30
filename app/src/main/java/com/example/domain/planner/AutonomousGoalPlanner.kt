package com.example.domain.planner

import android.content.Context
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
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
     * Decomposes a natural language goal into structured multi-phase milestones with tool assignments using dynamic LLM generation.
     */
    suspend fun decomposeAndSaveGoal(
        goalTitle: String,
        goalDescription: String,
        category: String = "Productivity",
        targetDate: String = ""
    ): DecomposedGoalResult = withContext(Dispatchers.IO) {
        val (detectedCategory, milestones) = generateDynamicLlmPlan(goalTitle, goalDescription, category)

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
                        "content" to "# ${milestone.stepTitle}\n\n**Goal**: ${goal?.title}\n\n## Overview\n${milestone.stepDescription}\n\n## Action Items\n- [ ] Initial Research\n- [ ] Draft Implementation\n- [ ] Review & Publish"
                    )
                )
                resultText = report.description
            }
            "GITHUB" -> {
                val (_, report) = toolDispatcher.executeTool(
                    "github_create_issue",
                    mapOf(
                        "repo" to "workspace/projects",
                        "title" to "[Milestone] ${milestone.stepTitle}",
                        "body" to "${milestone.stepDescription}\n\n*Linked Goal: ${goal?.title ?: ""}*"
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
        updateGoalProgress(goalId)
        resultText
    }

    suspend fun setMilestoneCompleted(milestoneId: Long, goalId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        goalDao.setMilestoneCompleted(milestoneId, isCompleted)
        updateGoalProgress(goalId)
    }

    suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        setMilestoneCompleted(milestoneId, goalId, isCompleted)
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

    private suspend fun generateDynamicLlmPlan(
        title: String,
        description: String,
        category: String
    ): Pair<String, List<GoalMilestoneEntity>> {
        val apiKey = GeminiClient.getApiKey()

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    You are an autonomous executive AI goal decomposition agent.
                    Decompose the following user goal into a high-leverage 3-phase execution plan.
                    
                    Goal Title: "$title"
                    Description: "$description"
                    Category Hint: "$category"
                    
                    Return ONLY a JSON object formatted strictly as:
                    {
                      "detectedCategory": "Engineering" | "Health" | "Learning" | "Creative" | "Productivity",
                      "milestones": [
                        {
                          "phaseNumber": 1,
                          "phaseTitle": "Phase 1: Foundation & Architecture",
                          "stepTitle": "Clear, specific action title",
                          "stepDescription": "Detailed action description",
                          "suggestedTool": "CALENDAR" | "TASK" | "DOC" | "GITHUB" | "SLACK"
                        }
                      ]
                    }
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.3f
                    )
                )

                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

                if (!jsonText.isNullOrBlank()) {
                    val cleanJson = jsonText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                    val jsonObj = JSONObject(cleanJson)
                    val detectedCategory = jsonObj.optString("detectedCategory", category.ifBlank { "Productivity" })
                    val milestonesArray = jsonObj.getJSONArray("milestones")
                    val parsedMilestones = mutableListOf<GoalMilestoneEntity>()

                    for (i in 0 until milestonesArray.length()) {
                        val mObj = milestonesArray.getJSONObject(i)
                        parsedMilestones.add(
                            GoalMilestoneEntity(
                                goalId = 0,
                                phaseNumber = mObj.optInt("phaseNumber", 1),
                                phaseTitle = mObj.optString("phaseTitle", "Phase 1"),
                                stepTitle = mObj.optString("stepTitle", "Action Step"),
                                stepDescription = mObj.optString("stepDescription", "Execute action item"),
                                suggestedTool = mObj.optString("suggestedTool", "TASK")
                            )
                        )
                    }

                    if (parsedMilestones.isNotEmpty()) {
                        return Pair(detectedCategory, parsedMilestones)
                    }
                }
            } catch (e: Exception) {
                // Fallback handled below
            }
        }

        // Strictly no heuristic catalogs. If the LLM call fails or API key is missing,
        // we return a single, minimal un-styled failure milestone rather than a simulated "plan".
        return Pair(
            category.ifBlank { "Uncategorized" },
            listOf(
                GoalMilestoneEntity(
                    goalId = 0,
                    phaseNumber = 1,
                    phaseTitle = "Action Required",
                    stepTitle = "Configure AI Engine",
                    stepDescription = "LLM generation failed. Please configure your AI API key in Settings to unlock autonomous planning.",
                    suggestedTool = "SETTINGS"
                )
            )
        )
    }

    private fun getDefaultTargetDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val twoWeeksLater = System.currentTimeMillis() + (14L * 24 * 60 * 60 * 1000)
        return sdf.format(Date(twoWeeksLater))
    }
}
