package com.example.domain.repository

import com.example.data.local.entity.PetMemoryEntity
import kotlinx.coroutines.flow.Flow

interface PetMemoryRepository {
    val allMemories: Flow<List<PetMemoryEntity>>
    suspend fun addMemory(topic: String, note: String, sentiment: String)
    suspend fun toggleMemoryPin(memoryId: Long)
}
