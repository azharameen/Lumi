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

        // Planning step for multi-step reasoning
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
}
