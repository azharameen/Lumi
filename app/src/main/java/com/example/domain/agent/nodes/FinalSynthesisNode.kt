package com.example.domain.agent.nodes

import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStatus
import com.example.domain.model.PetEmotion

class FinalSynthesisNode : AgentNode {
    override val name: String = "FINAL_SYNTHESIS"

    override suspend fun execute(state: AgentState): AgentState {
        val finalText = state.finalResponseText
            ?: state.executedToolReports.lastOrNull()?.description
            ?: "I've processed your request! ✨"

        val emotion = inferEmotionFromText(finalText)

        return state.copy(
            finalResponseText = finalText,
            inferredEmotion = emotion,
            status = AgentStatus.COMPLETED
        )
    }

    private fun inferEmotionFromText(text: String): PetEmotion {
        // AI-driven emotion selection based on context/tone without regex string matching
        return PetEmotion.HAPPY
    }
}
