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
        val lower = text.lowercase()
        return when {
            lower.contains("hug") || lower.contains("love") || lower.contains("heart") || lower.contains("caring") -> PetEmotion.LOVING
            lower.contains("yay") || lower.contains("dance") || lower.contains("energy") || lower.contains("celebrat") || lower.contains("awesome") -> PetEmotion.ENERGETIC
            lower.contains("calm") || lower.contains("breathe") || lower.contains("peace") || lower.contains("rest") -> PetEmotion.CALM
            lower.contains("overwhelm") || lower.contains("sad") || lower.contains("sorry") || lower.contains("stress") -> PetEmotion.CONCERNED
            lower.contains("haha") || lower.contains("hehe") || lower.contains("play") || lower.contains("joke") -> PetEmotion.PLAYFUL
            lower.contains("analyz") || lower.contains("thinking") || lower.contains("calculat") -> PetEmotion.THINKING
            else -> PetEmotion.HAPPY
        }
    }
}
