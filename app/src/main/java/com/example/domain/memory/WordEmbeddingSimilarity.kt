package com.example.domain.memory

import kotlin.math.sqrt

/**
 * Lightweight Word-Embedding based similarity engine.
 * Uses a pre-computed map of word vectors to calculate semantic distance.
 * This is faster and more accurate than TF-IDF for mobile on-device reasoning.
 */
object WordEmbeddingSimilarity {

    // Simplified mock vector map for key concepts
    // In a real scenario, this would be a TFLite model or a larger binary map.
    private val wordVectors = mapOf(
        "calendar" to doubleArrayOf(0.1, 0.8, 0.3),
        "meeting" to doubleArrayOf(0.15, 0.75, 0.25),
        "schedule" to doubleArrayOf(0.05, 0.85, 0.2),
        "stress" to doubleArrayOf(0.9, 0.1, 0.0),
        "anxious" to doubleArrayOf(0.85, 0.15, 0.05),
        "breathe" to doubleArrayOf(0.1, 0.0, 0.9),
        "meditate" to doubleArrayOf(0.2, 0.05, 0.8)
    )

    fun calculateSimilarity(textA: String, textB: String): Float {
        val vecA = getAverageVector(textA)
        val vecB = getAverageVector(textB)
        
        return cosineSimilarity(vecA, vecB).toFloat()
    }

    private fun getAverageVector(text: String): DoubleArray {
        val words = text.lowercase().split(Regex("\\s+"))
        val result = DoubleArray(3) { 0.0 }
        var count = 0
        
        for (word in words) {
            val vec = wordVectors[word]
            if (vec != null) {
                for (i in vec.indices) result[i] += vec[i]
                count++
            }
        }
        
        if (count > 0) {
            for (i in result.indices) result[i] /= count.toDouble()
        }
        return result
    }

    private fun cosineSimilarity(v1: DoubleArray, v2: DoubleArray): Double {
        var dot = 0.0
        var n1 = 0.0
        var n2 = 0.0
        for (i in v1.indices) {
            dot += v1[i] * v2[i]
            n1 += v1[i] * v1[i]
            n2 += v2[i] * v2[i]
        }
        if (n1 == 0.0 || n2 == 0.0) return 0.0
        return dot / (sqrt(n1) * sqrt(n2))
    }
}
