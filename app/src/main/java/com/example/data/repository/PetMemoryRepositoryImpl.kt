package com.example.data.repository

import com.example.data.local.LumiDatabase
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.mapper.toDomain
import com.example.domain.model.PetMemory
import com.example.domain.repository.PetMemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PetMemoryRepositoryImpl(
    private val database: LumiDatabase
) : PetMemoryRepository {

    override val allMemories: Flow<List<PetMemory>> = 
        database.petMemoryDao().getAllMemories().map { list -> list.map { it.toDomain() } }

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

    override suspend fun getAllMemoriesSync(): List<PetMemory> {
        return database.petMemoryDao().getAllMemoriesDirect().map { it.toDomain() }
    }
}
