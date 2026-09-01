package com.example.domain.memory

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.FactKnowledgeEntity
import com.example.data.local.entity.PetMemoryEntity
import kotlin.math.sqrt

/**
 * On-Device Semantic Memory Engine.
 * Employs TF-IDF / Sub-word cosine similarity vectorization to rank and retrieve
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
            val tfIdfScore = computeCosineSimilarity(query, content)
            val embeddingScore = WordEmbeddingSimilarity.calculateSimilarity(query, content)
            
            // Weighted ensemble score
            val combinedScore = (tfIdfScore * 0.3f) + (embeddingScore * 0.7f)
            memory to combinedScore
        }.sortedByDescending { it.second }

        // 2. Score and rank knowledge graph facts
        val scoredFacts = facts.map { fact ->
            val content = "${fact.predicate} ${fact.objectValue}"
            val tfIdfScore = computeCosineSimilarity(query, content)
            val embeddingScore = WordEmbeddingSimilarity.calculateSimilarity(query, content)
            
            val combinedScore = (tfIdfScore * 0.3f) + (embeddingScore * 0.7f)
            fact to combinedScore
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
            // Fallback to recent items if no high similarity match
            val recentMemories = memories.take(2)
            return@withContext buildContextString(recentMemories, facts.take(3))
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

    /**
     * Computes vector cosine similarity based on word-frequency n-grams.
     */
    private fun computeCosineSimilarity(textA: String, textB: String): Float {
        val vectorA = getTermFrequencyVector(textA)
        val vectorB = getTermFrequencyVector(textB)

        val allKeys = vectorA.keys + vectorB.keys
        if (allKeys.isEmpty()) return 0f

        var dotProduct = 0.0
        var normA = 0.0
        var normB = 0.0

        for (key in allKeys) {
            val countA = vectorA[key] ?: 0
            val countB = vectorB[key] ?: 0

            dotProduct += countA * countB
            normA += countA * countA
            normB += countB * countB
        }

        if (normA == 0.0 || normB == 0.0) return 0f
        return (dotProduct / (sqrt(normA) * sqrt(normB))).toFloat()
    }

    private fun getTermFrequencyVector(text: String): Map<String, Int> {
        val words = text.lowercase()
            .replace(Regex("[^a-zA-Z0-9 ]"), " ")
            .split(Regex("\\s+"))
            .filter { it.length > 2 }

        val map = mutableMapOf<String, Int>()
        for (w in words) {
            map[w] = (map[w] ?: 0) + 1
        }
        return map
    }
}
