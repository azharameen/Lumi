package com.example.domain.skill

import com.example.data.remote.GeminiToolWrapper

/**
 * Modular Skill interface.
 * Encapsulates specific domain capabilities, system prompt extensions, and filtered tool catalogs.
 */
interface AgentSkill {
    val id: String
    val displayName: String
    val description: String
    val systemPromptExtension: String
    val tools: List<GeminiToolWrapper>
}
