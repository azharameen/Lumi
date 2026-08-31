package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AgentCheckpointEntity

@Dao
interface AgentCheckpointDao {
    @Query("SELECT * FROM agent_checkpoints WHERE stateId = :stateId LIMIT 1")
    suspend fun getCheckpoint(stateId: String): AgentCheckpointEntity?

    @Query("SELECT * FROM agent_checkpoints WHERE status = 'WAITING_FOR_HITL' ORDER BY timestampMillis DESC")
    suspend fun getPendingHitlCheckpoints(): List<AgentCheckpointEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCheckpoint(checkpoint: AgentCheckpointEntity)

    @Query("DELETE FROM agent_checkpoints WHERE stateId = :stateId")
    suspend fun deleteCheckpoint(stateId: String)
}
