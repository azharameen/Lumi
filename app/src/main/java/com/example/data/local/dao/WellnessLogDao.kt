package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.paging.PagingSource
import androidx.room.Update
import com.example.data.local.entity.WellnessLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WellnessLogDao {
    @Query("SELECT * FROM wellness_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<WellnessLogEntity>>

    @Query("SELECT * FROM wellness_logs ORDER BY timestamp DESC")
    fun getPagedWellnessLogs(): PagingSource<Int, WellnessLogEntity>

    @Query("SELECT * FROM wellness_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLatestLog(): Flow<WellnessLogEntity?>

    @Query("SELECT * FROM wellness_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp ASC")
    fun getRecentLogs(sinceTimestamp: Long): Flow<List<WellnessLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: WellnessLogEntity): Long

    @Update
    suspend fun updateLog(log: WellnessLogEntity)

    @Query("UPDATE wellness_logs SET hydrationCups = hydrationCups + 1 WHERE id = :logId")
    suspend fun incrementHydration(logId: Long)

    @Query("SELECT * FROM wellness_logs")
    suspend fun getAllLogsDirect(): List<WellnessLogEntity>
}
