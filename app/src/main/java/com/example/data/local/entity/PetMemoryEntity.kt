package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_memories")
data class PetMemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Emotion", // Emotion, Goal, Preference, Event
    val memoryText: String,
    val sentiment: String = "Positive", // Positive, Neutral, Negative
    val emotionalImpact: Int = 3, // 1 to 5
    val isPinned: Boolean = false,
    val embeddingBlob: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PetMemoryEntity
        if (id != other.id) return false
        if (timestamp != other.timestamp) return false
        if (category != other.category) return false
        if (memoryText != other.memoryText) return false
        if (sentiment != other.sentiment) return false
        if (emotionalImpact != other.emotionalImpact) return false
        if (isPinned != other.isPinned) return false
        if (embeddingBlob != null) {
            if (other.embeddingBlob == null) return false
            if (!embeddingBlob.contentEquals(other.embeddingBlob)) return false
        } else if (other.embeddingBlob != null) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + memoryText.hashCode()
        result = 31 * result + sentiment.hashCode()
        result = 31 * result + emotionalImpact.hashCode()
        result = 31 * result + isPinned.hashCode()
        result = 31 * result + (embeddingBlob?.contentHashCode() ?: 0)
        return result
    }
}
