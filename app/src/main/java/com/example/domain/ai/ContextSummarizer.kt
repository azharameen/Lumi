package com.example.domain.ai

import com.example.data.remote.FirebaseAiCloudEngine

/**
 * Utility to compress long conversation histories to fit within token windows
 * while preserving key context.
 */
object ContextSummarizer {

    suspend fun summarizeHistory(history: List<Pair<String, String>>): String {
        if (history.isEmpty()) return ""
        
        val rawText = history.joinToString("\n") { "${it.first}: ${it.second}" }
        
        val prompt = """
            Summarize the following conversation history into a single concise paragraph.
            Maintain key user goals, mentioned names, and emotional state.
            
            History:
            $rawText
        """.trimIndent()

        return try {
            FirebaseAiCloudEngine.getInstance().generateChatResponse(
                prompt = prompt,
                systemPrompt = "You are a context compression engine. Summarize history concisely."
            )
        } catch (e: Exception) {
            "History summary failed. User previously mentioned: ${history.lastOrNull()?.second ?: "nothing"}"
        }
    }
}
