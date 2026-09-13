package com.example.domain.agent.nodes

import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiInlineData
import com.example.data.remote.GeminiPart
import com.example.data.remote.OnDeviceGemmaEngine
import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState
import com.example.domain.ai.ContextRelevancePruner
import java.io.ByteArrayOutputStream

class IntentRoutingNode(
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null,
    private val contextRelevancePruner: ContextRelevancePruner = ContextRelevancePruner.getInstance()
) : AgentNode {
    override val name: String = "INTENT_ROUTING"

    override suspend fun execute(state: AgentState): AgentState = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        // 1. Semantic Skill Classification (On-Device LLM preferred; default to GENERAL_COMPANION if not ready)
        val skill = onDeviceGemmaEngine?.classifyIntent(state.userQuery) ?: "GENERAL_COMPANION"
        val activeSkillObj = com.example.domain.skill.SkillRegistry.getInstance().getSkill(skill)

        // 2. Track & Manage Topic Stack (suspension, continuation, resumption)
        val topicManager = com.example.domain.ai.TopicContextManager.getInstance()
        topicManager.trackTopicTurn(
            userMessage = state.userQuery,
            skillName = skill,
            isTransactional = activeSkillObj.isTransactional
        )

        // 3. Local execution preference (Hardware & model readiness)
        val isLocal = onDeviceGemmaEngine?.isModelReady() == true && state.imageAttachment == null

        // 4. Intelligent Context Relevance Pruning (eliminates topic bleed for direct actions)
        val prunedHistory = contextRelevancePruner.pruneHistory(
            userQuery = state.userQuery,
            fullHistory = state.history,
            classifiedSkill = skill
        )

        val contentsList = mutableListOf<GeminiContent>()

        // Add pruned history turns only
        for (turn in prunedHistory) {
            val role = if (turn.first.equals("USER", ignoreCase = true)) "user" else "model"
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
            history = prunedHistory,
            contentsList = contentsList,
            currentThought = "Analyzing your request with local semantic routing..."
        )
    }

    private fun ByteArray.toBase64(): String {
        return java.util.Base64.getEncoder().encodeToString(this)
    }
}
