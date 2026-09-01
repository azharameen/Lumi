package com.example.data.repository

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.PetMemoryEntity
import com.example.domain.repository.PetMemoryRepository
import kotlinx.coroutines.flow.Flow

class PetMemoryRepositoryImpl(
    private val database: LumiDatabase
) : PetMemoryRepository {

    override val allMemories: Flow<List<PetMemoryEntity>> = database.petMemoryDao().getAllMemories()

    override suspend fun addMemory(topic: String, note: String, sentiment: String) {
        database.petMemoryDao().insertMemory(
            PetMemoryEntity(
                category = topic,
                memoryText = note,
                sentiment = sentiment
            )
        )
    }

    override suspend fun toggleMemoryPin(memoryId: Long) {
        database.petMemoryDao().togglePin(memoryId)
    }
}
