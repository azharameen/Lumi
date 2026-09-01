package com.example.domain.ai

import java.util.Locale
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * A lightweight, pure-Kotlin Natural Language Processing (NLP) classifier.
 * Uses Term Frequency-Inverse Document Frequency (TF-IDF) and Cosine Similarity
 * to semantically classify user intent as "Local" or "Cloud" without brittle Regex.
 */
object SemanticIntentClassifier {

    private val localCorpus = listOf(
        "i feel sad", "i am anxious", "log my water", "set a timer for 5 minutes",
        "turn off the lights", "hello", "hi", "good morning", "how are you today",
        "my head hurts", "i am feeling stressed", "health check", "track my mood",
        "stop the alarm", "pause music", "resume playing", "wake me up at 7am",
        "brightness up", "volume down", "open the calendar", "thanks lumi",
        "goodbye", "see you later", "turn on bluetooth", "turn on wifi"
    )

    private val cloudCorpus = listOf(
        "write a python script", "summarize this long article", "explain quantum physics to me",
        "what is the capital of france", "plan a 3 day trip to tokyo", "translate this to spanish",
        "how do i build a react application", "give me a recipe for chocolate cake",
        "what is the history of the roman empire", "compare and contrast ios and android",
        "generate a sci-fi short story", "write a professional email to my boss",
        "who won the world cup in 2022", "solve this calculus problem",
        "what are the economic impacts of inflation", "decompose this objective into milestones"
    )

    private val vocabulary = mutableSetOf<String>()
    private val idfMap = mutableMapOf<String, Double>()
    
    private val localCentroid = mutableMapOf<String, Double>()
    private val cloudCentroid = mutableMapOf<String, Double>()

    init {
        train()
    }

    private fun tokenize(text: String): List<String> {
        return text.lowercase(Locale.getDefault())
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
    }

    private fun train() {
        val allDocs = localCorpus + cloudCorpus
        val tokenizedDocs = allDocs.map { tokenize(it) }
        
        // Build Vocabulary
        tokenizedDocs.forEach { doc -> vocabulary.addAll(doc) }

        // Calculate IDF (Inverse Document Frequency)
        val nDocs = allDocs.size.toDouble()
        for (word in vocabulary) {
            val docsContainingWord = tokenizedDocs.count { it.contains(word) }
            // Add 1 for smoothing
            idfMap[word] = ln(nDocs / (1.0 + docsContainingWord))
        }

        // Calculate Centroids
        computeCentroid(localCorpus, localCentroid)
        computeCentroid(cloudCorpus, cloudCentroid)
    }

    private fun computeCentroid(corpus: List<String>, centroid: MutableMap<String, Double>) {
        val totalVectors = mutableMapOf<String, Double>()
        
        for (doc in corpus) {
            val vector = getTfIdfVector(doc)
            for ((word, weight) in vector) {
                totalVectors[word] = (totalVectors[word] ?: 0.0) + weight
            }
        }
        
        // Average the vectors
        val count = corpus.size.toDouble()
        for ((word, totalWeight) in totalVectors) {
            centroid[word] = totalWeight / count
        }
    }

    private fun getTfIdfVector(text: String): Map<String, Double> {
        val tokens = tokenize(text)
        val tfMap = mutableMapOf<String, Int>()
        for (token in tokens) {
            tfMap[token] = (tfMap[token] ?: 0) + 1
        }

        val tfIdfVector = mutableMapOf<String, Double>()
        for ((word, count) in tfMap) {
            val tf = count.toDouble() / tokens.size
            val idf = idfMap[word] ?: ln((localCorpus.size + cloudCorpus.size).toDouble() / 1.0) // fallback IDF
            tfIdfVector[word] = tf * idf
        }
        return tfIdfVector
    }

    private fun cosineSimilarity(vec1: Map<String, Double>, vec2: Map<String, Double>): Double {
        val allKeys = vec1.keys + vec2.keys
        var dotProduct = 0.0
        var norm1 = 0.0
        var norm2 = 0.0

        for (key in allKeys) {
            val v1 = vec1[key] ?: 0.0
            val v2 = vec2[key] ?: 0.0
            dotProduct += v1 * v2
            norm1 += v1 * v1
            norm2 += v2 * v2
        }

        if (norm1 == 0.0 || norm2 == 0.0) return 0.0
        return dotProduct / (sqrt(norm1) * sqrt(norm2))
    }

    /**
     * Analyzes the text and returns true if the intent is highly likely to be a "Local" task.
     * Returns false if it leans towards "Cloud" or is too ambiguous/complex.
     */
    fun isLocalIntent(text: String): Boolean {
        // Fallback length check: if it's very long, it's safer to route to cloud
        if (text.length > 250) return false

        val inputVector = getTfIdfVector(text)
        if (inputVector.isEmpty()) return true // Empty/whitespace usually handled quickly locally

        val simLocal = cosineSimilarity(inputVector, localCentroid)
        val simCloud = cosineSimilarity(inputVector, cloudCentroid)

        // If it's a completely unknown prompt with zero similarity, default to Cloud for capability.
        if (simLocal == 0.0 && simCloud == 0.0) return false

        // Route to Local only if it matches Local centroid stronger than Cloud centroid.
        // We add a slight bias (0.05) to Cloud to ensure complex tasks don't get trapped locally.
        return simLocal > (simCloud + 0.05)
    }
}
