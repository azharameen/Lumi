package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_logs")
data class WellnessLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val moodScore: Int = 3, // 1 to 5
    val moodLabel: String = "Balanced", // Joyful, Calm, Stressed, Exhausted, Anxious, Inspired
    val energyLevel: Int = 3, // 1 to 5
    val hydrationCups: Int = 0,
    val sleepHours: Float = 7.0f,
    val gratitudeNote: String = "",
    val stressLevel: Int = 2, // 1 to 5
    val breathingMinutesCompleted: Int = 0
)
