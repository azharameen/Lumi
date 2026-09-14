package com.example.domain.repository

import com.example.domain.model.PetMemory
import kotlinx.coroutines.flow.Flow

interface PetMemoryRepository {
    val allMemories: Flow<List<PetMemory>>
    suspend fun addMemory(topic: String, note: String, sentiment: String)
    suspend fun toggleMemoryPin(memoryId: Long)

    suspend fun getAllMemoriesSync(): List<PetMemory>
}
