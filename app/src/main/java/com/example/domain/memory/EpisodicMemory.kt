package com.example.domain.memory

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.PetMemoryEntity

/**
 * 2. Episodic Memory Tier:
 * Persistent timestamped interaction logs stored in Room DB (`PetMemoryEntity`).
 */
class EpisodicMemory(private val database: LumiDatabase) {

    suspend fun recordEvent(category: String, memoryText: String, sentiment: String = "NEUTRAL", emotionalImpact: Int = 3): Long {
        val entity = PetMemoryEntity(
            timestamp = System.currentTimeMillis(),
            category = category,
            memoryText = memoryText,
            sentiment = sentiment,
            emotionalImpact = emotionalImpact
        )
        return database.petMemoryDao().insertMemory(entity)
    }

    suspend fun getRecentMemories(limit: Int = 10): List<PetMemoryEntity> {
        return database.petMemoryDao().getRecentMemoriesDirect().take(limit)
    }
}
