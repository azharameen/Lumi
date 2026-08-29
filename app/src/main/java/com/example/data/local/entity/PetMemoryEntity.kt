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
    val isPinned: Boolean = false
)
