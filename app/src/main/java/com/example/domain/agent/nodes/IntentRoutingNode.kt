package com.example.domain.agent.nodes

import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiInlineData
import com.example.data.remote.GeminiPart
import com.example.data.remote.OnDeviceGemmaEngine
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import java.io.ByteArrayOutputStream

class IntentRoutingNode(
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null
) : AgentNode {
    override val name: String = "INTENT_ROUTING"

    override suspend fun execute(state: AgentState): AgentState = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        val queryLower = state.userQuery.lowercase()

        // 1. Semantic Skill Classification (On-Device LLM preferred)
        val skill = onDeviceGemmaEngine?.classifyIntent(state.userQuery) ?: detectSkillViaAi(state.userQuery)

        // 2. Local execution preference (Hardware & model readiness)
        val isLocal = onDeviceGemmaEngine?.isModelReady() == true || state.imageAttachment == null

        val contentsList = mutableListOf<GeminiContent>()

        // Add history turns (last 6 turns)
        for (turn in state.history.takeLast(6)) {
            val role = if (turn.first == "USER") "user" else "model"
            contentsList.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = turn.second))
                )
            )
        }

        // Add current user turn
        val currentParts = mutableListOf<GeminiPart>()
        currentParts.add(GeminiPart(text = state.userQuery))
        if (state.imageAttachment != null) {
            currentParts.add(
                GeminiPart(
                    inlineData = GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = state.imageAttachment.toBase64()
                    )
                )
            )
        }

        contentsList.add(
            GeminiContent(
                role = "user",
                parts = currentParts
            )
        )

        state.copy(
            isLocalExecution = isLocal,
            selectedSkillName = skill,
            contentsList = contentsList,
            currentThought = "Analyzing your request with local semantic routing..."
        )
    }

    private fun detectSkillViaAi(query: String): String {
        val category = com.example.domain.ai.SemanticIntentClassifier.classifyTask(query)
        return when (category) {
            com.example.domain.ai.AiTaskCategory.WELLNESS_MOOD -> "WELLNESS"
            com.example.domain.ai.AiTaskCategory.TIMELINE_PLANNING -> "LIFE_ORGANIZER"
            com.example.domain.ai.AiTaskCategory.QUICK_DEVICE_ACTION -> "LIFE_ORGANIZER"
            else -> "GENERAL_COMPANION"
        }
    }

    private fun ByteArray.toBase64(): String {
        return java.util.Base64.getEncoder().encodeToString(this)
    }
}
