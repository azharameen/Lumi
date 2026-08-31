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
    val coins: Int = 150, // RPG Gold/Coins currency
    val gems: Int = 20, // Rare Starlight Gems currency
    val streakDays: Int = 1, // Consecutive daily streaks
    val bondScore: Int = 50, // 0 to 100
    val happiness: Int = 85, // 0 to 100
    val energy: Int = 90, // 0 to 100
    val personalityTrait: String = "Empathetic Explorer", // Empathetic, Playful, Stoic Guide, Joyful Motivator
    val activeAccessory: String = "NONE",
    val bloubShape: String = "SPHERE", // SPHERE, CUBE, CAPSULE
    val bloubSkinColor: String = "CYAN", // CYAN, PINK, GOLD, LAVENDER, MINT, MATCHA, PEACH, OBSIDIAN
    val unlockedAccessoriesCsv: String = "NONE,SPROUT",
    val unlockedSkinsCsv: String = "CYAN,PINK,GOLD,LAVENDER,MINT,MATCHA,PEACH,OBSIDIAN",
    val unlockedShapesCsv: String = "SPHERE,CUBE,CAPSULE",
    val daysTogether: Int = 1,
    val totalInteractions: Int = 0,
    val totalBreathingSessions: Int = 0,
    val tasksHelpedComplete: Int = 0,
    val lastInteractionTimestamp: Long = System.currentTimeMillis()
)
