package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PetMemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetMemoryDao {
    @Query("SELECT * FROM pet_memories ORDER BY isPinned DESC, timestamp DESC")
    fun getAllMemories(): Flow<List<PetMemoryEntity>>

    @Query("SELECT * FROM pet_memories ORDER BY timestamp DESC")
    suspend fun getAllMemoriesDirect(): List<PetMemoryEntity>

    @Query("SELECT * FROM pet_memories ORDER BY timestamp DESC LIMIT 10")
    suspend fun getRecentMemoriesDirect(): List<PetMemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: PetMemoryEntity): Long

    @Delete
    suspend fun deleteMemory(memory: PetMemoryEntity)

    @Query("UPDATE pet_memories SET isPinned = NOT isPinned WHERE id = :id")
    suspend fun togglePin(id: Long)
}
