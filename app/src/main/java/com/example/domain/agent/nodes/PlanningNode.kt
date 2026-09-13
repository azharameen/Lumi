package com.example.domain.agent.nodes

import com.example.data.remote.FirebaseAiCloudEngine
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStatus

class PlanningNode(
    private val onDeviceGemmaEngine: com.example.data.remote.OnDeviceGemmaEngine? = null
) : AgentNode {
    override val name: String = "PLANNING"

    override suspend fun execute(state: AgentState): AgentState {
        val query = state.userQuery.trim()

        // 1. If executing locally, perform fast on-device semantic step decomposition
        if (state.isLocalExecution || onDeviceGemmaEngine?.isModelReady() == true) {
            val localPlanSteps = decomposeLocalPlan(query)
            return if (localPlanSteps.size > 1) {
                val planText = localPlanSteps.mapIndexed { idx, step -> "${idx + 1}. $step" }.joinToString("\n")
                state.copy(
                    currentThought = "Decomposed into ${localPlanSteps.size} actionable steps.",
                    retrievedContext = if (state.retrievedContext.isNotBlank()) {
                        "${state.retrievedContext}\n\nExecution Plan:\n$planText"
                    } else {
                        "Execution Plan:\n$planText"
                    }
                )
            } else {
                state.copy(
                    currentThought = "Local execution: single intent identified."
                )
            }
        }

        // 2. Cloud-based strategic planning for multi-step reasoning
        val prompt = """
            You are Lumi's Strategic Planner. Breakdown the user's request into a concrete list of 1-3 execution steps.
            User Message: "${state.userQuery}"
            
            Current Context: ${state.retrievedContext.take(200)}
            
            Return ONLY a numbered list of steps.
        """.trimIndent()

        return try {
            val plan = FirebaseAiCloudEngine.getInstance().generateChatResponse(
                prompt = prompt,
                systemPrompt = "You are a specialized Planning Agent. Output ONLY the execution steps."
            )
            
            state.copy(
                currentThought = "Created execution plan: ${plan.take(100)}...",
                retrievedContext = if (state.retrievedContext.isNotBlank()) {
                    "${state.retrievedContext}\n\nSTRATEGIC PLAN:\n$plan"
                } else {
                    "STRATEGIC PLAN:\n$plan"
                }
            )
        } catch (e: Exception) {
            // Soft fail: do not fail state, let ReasoningNode proceed
            state.copy(currentThought = "Proceeding directly to reasoning.")
        }
    }

    private fun decomposeLocalPlan(query: String): List<String> {
        val delimiters = listOf(" and then ", " then ", " and also ", " also ", " and ", ", then ")
        var segments = listOf(query)
        for (delimiter in delimiters) {
            segments = segments.flatMap { segment ->
                if (segment.contains(delimiter, ignoreCase = true)) {
                    segment.split(Regex(delimiter, RegexOption.IGNORE_CASE))
                } else {
                    listOf(segment)
                }
            }
        }
        val cleaned = segments.map { it.trim().trim(',', '.') }.filter { it.length > 3 }
        return if (cleaned.size > 1) cleaned else emptyList()
    }
}
