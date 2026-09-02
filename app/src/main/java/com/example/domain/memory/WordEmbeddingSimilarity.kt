package com.example.domain.memory

import com.example.data.ai.MediaPipeTextEmbedder

/**
 * On-Device Dense Vector Similarity Engine.
 * Replaces hardcoded 7-word mock map with MediaPipe TextEmbedder.
 */
object WordEmbeddingSimilarity {

    private var embedder: MediaPipeTextEmbedder? = null

    fun init(embedderInstance: MediaPipeTextEmbedder) {
        embedder = embedderInstance
    }

    suspend fun calculateSimilarity(textA: String, textB: String): Float {
        val activeEmbedder = embedder
        if (activeEmbedder != null) {
            val vecA = activeEmbedder.embed(textA)
            val vecB = activeEmbedder.embed(textB)
            return activeEmbedder.cosineSimilarity(vecA, vecB)
        }
        
        // Jaccard similarity fallback if embedder uninitialized
        val setA = textA.lowercase().split(Regex("\\s+")).toSet()
        val setB = textB.lowercase().split(Regex("\\s+")).toSet()
        val intersection = setA.intersect(setB).size
        val union = setA.union(setB).size
        return if (union > 0) intersection.toFloat() / union.toFloat() else 0f
    }
}
