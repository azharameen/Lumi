package com.example.domain.memory

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.FactKnowledgeEntity
import com.example.data.local.entity.PetMemoryEntity

/**
 * On-Device Semantic Memory Engine.
 * Employs Sub-word cosine similarity vectorization to rank and retrieve
 * the top relevant episodic memories and knowledge graph facts for the active turn.
 */
class SemanticMemoryEngine(
    private val database: LumiDatabase
) {
    /**
     * Retrieves the top [limit] most semantically relevant memories and facts for the given [query].
     */
    suspend fun retrieveRelevantContext(query: String, limit: Int = 4): String = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
        val memories = database.petMemoryDao().getAllMemoriesDirect()
        val facts = database.factKnowledgeDao().getAllFactsDirect()

        if (memories.isEmpty() && facts.isEmpty()) {
            return@withContext ""
        }

        // 1. Score and rank episodic memories
        val scoredMemories = memories.map { memory ->
            val content = "${memory.category} ${memory.memoryText}"
            val embeddingScore = WordEmbeddingSimilarity.calculateSimilarity(query, content)
            
            memory to embeddingScore
        }.sortedByDescending { it.second }

        // 2. Score and rank knowledge graph facts
        val scoredFacts = facts.map { fact ->
            val content = "${fact.predicate} ${fact.objectValue}"
            val embeddingScore = WordEmbeddingSimilarity.calculateSimilarity(query, content)
            
            fact to embeddingScore
        }.sortedByDescending { it.second }

        val topMemories = scoredMemories
            .filter { it.second > 0.05f }
            .take(limit)
            .map { it.first }

        val topFacts = scoredFacts
            .filter { it.second > 0.05f }
            .take(limit)
            .map { it.first }

        if (topMemories.isEmpty() && topFacts.isEmpty()) {
            return@withContext ""
        }

        return@withContext buildContextString(topMemories, topFacts)
    }

    private fun buildContextString(memories: List<PetMemoryEntity>, facts: List<FactKnowledgeEntity>): String {
        return buildString {
            if (facts.isNotEmpty()) {
                append("User Profile Knowledge Graph:\n")
                facts.forEach { fact ->
                    append("• ${fact.subject} ${fact.predicate}: ${fact.objectValue}\n")
                }
                append("\n")
            }
            if (memories.isNotEmpty()) {
                append("Relevant Episodic Memories:\n")
                memories.forEach { mem ->
                    append("• [${mem.category}] ${mem.memoryText}\n")
                }
            }
        }.trim()
    }
}
