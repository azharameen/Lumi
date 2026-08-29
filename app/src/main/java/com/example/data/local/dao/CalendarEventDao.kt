package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY startTimeMillis ASC")
    fun getAllEvents(): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE endTimeMillis >= :startOfDay AND startTimeMillis <= :endOfDay ORDER BY startTimeMillis ASC")
    fun getEventsForDateRange(startOfDay: Long, endOfDay: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE startTimeMillis >= :now ORDER BY startTimeMillis ASC LIMIT 5")
    fun getUpcomingEvents(now: Long = System.currentTimeMillis()): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Update
    suspend fun updateEvent(event: CalendarEventEntity)

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("SELECT * FROM calendar_events")
    suspend fun getAllEventsDirect(): List<CalendarEventEntity>
}
