package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pet_evolution")
data class PetEvolutionEntity(
    @PrimaryKey
    val id: Int = 1, // Single active pet profile
    val name: String = "Lumi",
    val level: Int = 1,
    val exp: Int = 0,
    val expToNextLevel: Int = 100,
    val bondScore: Int = 50, // 0 to 100
    val happiness: Int = 85, // 0 to 100
    val energy: Int = 90, // 0 to 100
    val personalityTrait: String = "Empathetic Explorer", // Empathetic, Playful, Stoic Guide, Joyful Motivator
    val activeAccessory: String = "NONE", // NONE, SPROUT, GLASSES, HALO, CROWN, HEADPHONES
    val unlockedAccessoriesCsv: String = "NONE,SPROUT",
    val daysTogether: Int = 1,
    val totalInteractions: Int = 0,
    val totalBreathingSessions: Int = 0,
    val tasksHelpedComplete: Int = 0,
    val lastInteractionTimestamp: Long = System.currentTimeMillis()
)
