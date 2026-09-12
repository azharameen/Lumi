package com.example.domain.ai

import com.example.data.remote.FirebaseAiCloudEngine

/**
 * Utility to compress long conversation histories to fit within token windows
 * using exact subword token counts via BpeTokenizer.
 */
object ContextSummarizer {

    private const val MAX_UNCOMPRESSED_TOKENS = 1200

    suspend fun summarizeHistory(history: List<Pair<String, String>>): String {
        if (history.isEmpty()) return ""
        
        val rawText = history.joinToString("\n") { "${it.first}: ${it.second}" }
        val tokenCount = BpeTokenizer.countTokens(rawText)

        // Only compress if token count exceeds safety limit
        if (tokenCount < MAX_UNCOMPRESSED_TOKENS) {
            return rawText
        }
        
        val prompt = """
            Summarize the following conversation history into a single concise paragraph.
            Maintain key user goals, mentioned names, decisions, and emotional state.
            
            History ($tokenCount tokens):
            $rawText
        """.trimIndent()

        return try {
            val summary = FirebaseAiCloudEngine.getInstance().generateChatResponse(
                prompt = prompt,
                systemPrompt = "You are a context compression engine. Summarize history concisely."
            )
            "Conversation Summary ($tokenCount -> ${BpeTokenizer.countTokens(summary)} tokens):\n$summary"
        } catch (e: Exception) {
            val truncated = BpeTokenizer.truncateToTokenLimit(rawText, 400)
            "Truncated History: $truncated"
        }
    }
}
