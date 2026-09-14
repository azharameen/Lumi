package com.example.domain.memory

import com.example.domain.model.PetMemory
import com.example.domain.model.UserFact
import com.example.domain.repository.PetMemoryRepository
import com.example.domain.repository.UserMemoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * On-Device Semantic Memory Engine.
 * Employs Sub-word cosine similarity vectorization to rank and retrieve
 * the top relevant episodic memories and knowledge graph facts for the active turn.
 */
class SemanticMemoryEngine(
    private val petMemoryRepository: PetMemoryRepository,
    private val userMemoryRepository: UserMemoryRepository
) {
    /**
     * Retrieves the top [limit] most semantically relevant memories and facts for the given [query].
     */
    suspend fun retrieveRelevantContext(query: String, limit: Int = 4): String = withContext(Dispatchers.Default) {
        val memories = petMemoryRepository.getAllMemoriesSync()
        val facts = userMemoryRepository.getAllFactsSync()

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
            val content = "${fact.factKey} ${fact.factValue}"
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

    private fun buildContextString(memories: List<PetMemory>, facts: List<UserFact>): String {
        return buildString {
            if (facts.isNotEmpty()) {
                append("User Profile Knowledge Graph:\n")
                facts.forEach { fact ->
                    append("• ${fact.factKey}: ${fact.factValue}\n")
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
