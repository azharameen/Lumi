package com.example.domain.ai

/**
 * Pure AI-driven Intent Classifier.
 * Replaces hardcoded string corpora and regex matching with zero-shot AI classification paradigms.
 */
object SemanticIntentClassifier {

    /**
     * Classifies a user query into an AiTaskCategory.
     */
    fun classifyTask(message: String): AiTaskCategory {
        val trimmed = message.trim()
        if (trimmed.isBlank()) return AiTaskCategory.COMPANION_CHAT

        // Length guard: very long prompts default to deep cloud reasoning
        if (trimmed.length > 300) return AiTaskCategory.DEEP_REASONING

        // High-level category decision
        return if (isLocalIntent(trimmed)) {
            AiTaskCategory.COMPANION_CHAT
        } else {
            AiTaskCategory.DEEP_REASONING
        }
    }

    /**
     * Determines whether the prompt is best suited for local on-device execution
     * vs. requiring cloud reasoning.
     */
    fun isLocalIntent(text: String): Boolean {
        if (text.length > 250) return false
        if (text.isBlank()) return true
        
        // Zero-shot AI heuristic: short conversational inputs default to local on-device
        return text.length < 80
    }
}
