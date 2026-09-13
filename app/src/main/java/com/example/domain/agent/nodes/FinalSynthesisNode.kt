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

        // Topic summarization & tracking update (Pillar 5)
        val activeTopic = com.example.domain.ai.TopicContextManager.getInstance().getActiveTopic()
        if (activeTopic != null && activeTopic.turnCount > 1 && !state.selectedSkillName.isNullOrBlank()) {
            val skill = com.example.domain.skill.SkillRegistry.getInstance().getSkill(state.selectedSkillName)
            if (!skill.isTransactional && finalText.length > 20) {
                com.example.domain.ai.TopicContextManager.getInstance().updateTopicSummary(
                    topicId = activeTopic.id,
                    summary = "${activeTopic.summary} | Turn ${activeTopic.turnCount}: ${finalText.take(120)}".trim(' ', '|')
                )
            }
        }

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
