package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an explicit factual knowledge triple extracted by the AI companion.
 * Forms the on-device Knowledge Graph.
 */
@Entity(tableName = "fact_knowledge_graph")
data class FactKnowledgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String = "User",
    val predicate: String,       // e.g. "prefers", "has_friend", "works_at", "diet", "goal"
    val objectValue: String,     // e.g. "Morning walks", "Sarah", "Google", "Vegetarian"
    val confidence: Float = 0.95f,
    val lastUpdatedMillis: Long = System.currentTimeMillis(),
    val accessCount: Int = 1,
    val isPinned: Boolean = false,
    val embeddingBlob: ByteArray? = null
) {
    /**
     * Calculates effective confidence incorporating temporal decay.
     * Pinned facts do not decay. Unpinned facts have a 30-day half-life.
     * Frequent access (accessCount) buffers against decay.
     */
    fun effectiveConfidence(currentMillis: Long = System.currentTimeMillis()): Float {
        if (isPinned) return confidence
        val ageMillis = (currentMillis - lastUpdatedMillis).coerceAtLeast(0L)
        val thirtyDaysMillis = 30L * 24L * 60L * 60L * 1000L
        val decayPeriods = ageMillis.toDouble() / thirtyDaysMillis.toDouble()
        val decayFactor = Math.pow(0.5, decayPeriods).toFloat()
        val accessBonus = (accessCount * 0.02f).coerceAtMost(0.2f)
        return ((confidence * decayFactor) + accessBonus).coerceIn(0.01f, 1.0f)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FactKnowledgeEntity
        if (id != other.id) return false
        if (subject != other.subject) return false
        if (predicate != other.predicate) return false
        if (objectValue != other.objectValue) return false
        if (confidence != other.confidence) return false
        if (lastUpdatedMillis != other.lastUpdatedMillis) return false
        if (accessCount != other.accessCount) return false
        if (isPinned != other.isPinned) return false
        if (embeddingBlob != null) {
            if (other.embeddingBlob == null) return false
            if (!embeddingBlob.contentEquals(other.embeddingBlob)) return false
        } else if (other.embeddingBlob != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + subject.hashCode()
        result = 31 * result + predicate.hashCode()
        result = 31 * result + objectValue.hashCode()
        result = 31 * result + confidence.hashCode()
        result = 31 * result + lastUpdatedMillis.hashCode()
        result = 31 * result + accessCount.hashCode()
        result = 31 * result + isPinned.hashCode()
        result = 31 * result + (embeddingBlob?.contentHashCode() ?: 0)
        return result
    }
}
