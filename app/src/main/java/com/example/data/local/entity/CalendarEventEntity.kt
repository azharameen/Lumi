package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val location: String = "",
    val isAllDay: Boolean = false,
    val category: String = "Routine", // Work, Health, Focus, Social, Rest
    val colorHex: String = "#00F0FF",
    val reminderMinutesBefore: Int = 15,
    val createdAt: Long = System.currentTimeMillis()
)
