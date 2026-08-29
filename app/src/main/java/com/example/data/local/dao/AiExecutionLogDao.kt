package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AiExecutionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiExecutionLogDao {
    @Query("SELECT * FROM ai_execution_logs ORDER BY startTimeMillis DESC")
    fun getAllLogs(): Flow<List<AiExecutionLogEntity>>

    @Query("SELECT * FROM ai_execution_logs ORDER BY startTimeMillis DESC LIMIT :limit")
    fun getRecentLogs(limit: Int): Flow<List<AiExecutionLogEntity>>

    @Query("SELECT * FROM ai_execution_logs WHERE engineType = :engine ORDER BY startTimeMillis DESC")
    fun getLogsByEngine(engine: String): Flow<List<AiExecutionLogEntity>>

    @Query("SELECT * FROM ai_execution_logs WHERE taskCategory = :category ORDER BY startTimeMillis DESC")
    fun getLogsByCategory(category: String): Flow<List<AiExecutionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AiExecutionLogEntity): Long

    @Query("DELETE FROM ai_execution_logs")
    suspend fun clearAllLogs()

    @Query("SELECT COUNT(*) FROM ai_execution_logs")
    suspend fun getTotalInvocationsCount(): Int

    @Query("SELECT SUM(totalTokens) FROM ai_execution_logs")
    suspend fun getTotalTokensUsed(): Long?

    @Query("SELECT AVG(durationMs) FROM ai_execution_logs WHERE isSuccess = 1")
    suspend fun getAverageDurationMs(): Double?
}
