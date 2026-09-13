package com.example.domain.ai

import com.example.domain.skill.SkillRegistry

/**
 * Intelligent Context Pruner & Topic Boundary Manager.
 * Prevents conversational context bleeding (where prior brainstorming threads
 * bleed into unrelated tool actions or new topics) without requiring a new chat session.
 */
class ContextRelevancePruner(
    private val skillRegistry: SkillRegistry = SkillRegistry.getInstance()
) {

    companion object {
        @Volatile
        private var instance: ContextRelevancePruner? = null

        fun getInstance(): ContextRelevancePruner {
            return instance ?: synchronized(this) {
                instance ?: ContextRelevancePruner().also { instance = it }
            }
        }

        // Detects anaphoric pronoun references that require looking back at the immediate prior turn
        private val ANAPHORA_REGEX = Regex(
            """(?i)\b(?:it|that|this|these|those|him|her|them|again|same|the number|the person|the app)\b"""
        )

        // Filters out internal tool execution markers from history to keep dialog clean
        private val TOOL_TAG_REGEX = Regex(
            """<tool_call>.*?</tool_call>""",
            RegexOption.DOT_MATCHES_ALL
        )
    }

    /**
     * Determines the optimal, relevant history turns to present to the AI model.
     *
     * @param userQuery The immediate user prompt
     * @param fullHistory The full recent chronological message history
     * @param classifiedSkill The skill domain classified for the userQuery (e.g. DEVICE_CONTROLS)
     * @return Pruned, relevant history turns
     */
    fun pruneHistory(
        userQuery: String,
        fullHistory: List<Pair<String, String>>,
        classifiedSkill: String = "GENERAL_COMPANION"
    ): List<Pair<String, String>> {
        if (fullHistory.isEmpty()) return emptyList()

        val skill = skillRegistry.getSkill(classifiedSkill)
        val isTransactional = skill.isTransactional
        val hasAnaphora = ANAPHORA_REGEX.containsMatchIn(userQuery)

        return when {
            // 1. Transactional command with NO anaphora (e.g. "Dial 9876543210", "Turn on flashlight")
            // Completely isolated: 0 turns of history. Zero topic bleed.
            isTransactional && !hasAnaphora -> {
                emptyList()
            }

            // 2. Transactional command WITH anaphora (e.g. "Call him", "Turn it off", "Do it again")
            // Preserve strictly the single prior turn to resolve referents, preventing full topic bleed.
            isTransactional && hasAnaphora -> {
                fullHistory.takeLast(1).map { sanitizeTurn(it) }
            }

            // 3. Conversational / Planning / Wellness Skill
            // Keep recent turns (up to 6), filtering out stale tool execution artifacts
            else -> {
                fullHistory.takeLast(6)
                    .map { sanitizeTurn(it) }
                    .filter { it.second.isNotBlank() }
            }
        }
    }

    /**
     * Sanitizes raw history turns by stripping internal tool tags or machine noise.
     */
    private fun sanitizeTurn(turn: Pair<String, String>): Pair<String, String> {
        val cleanContent = turn.second
            .replace(TOOL_TAG_REGEX, "")
            .trim()
        return turn.first to cleanContent
    }
}
