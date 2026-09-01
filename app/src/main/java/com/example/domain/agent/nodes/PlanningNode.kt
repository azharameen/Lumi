package com.example.domain.agent.nodes

import com.example.data.remote.FirebaseAiCloudEngine
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStatus

class PlanningNode : AgentNode {
    override val name: String = "PLANNING"

    override suspend fun execute(state: AgentState): AgentState {
        // Planning logic: Generate a structured sequence of steps for the ReasoningNode to follow
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
                retrievedContext = state.retrievedContext + "\n\nSTRATEGIC PLAN:\n$plan"
            )
        } catch (e: Exception) {
            state.copy(lastError = "Planning failed: ${e.message}")
        }
    }
}
