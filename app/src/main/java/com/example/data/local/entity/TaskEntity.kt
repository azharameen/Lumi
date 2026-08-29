package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val notes: String = "",
    val dueDate: Long? = null,
    val priority: String = "MEDIUM", // LOW, MEDIUM, HIGH, URGENT
    val isCompleted: Boolean = false,
    val category: String = "General", // Work, Wellness, Personal, Study
    val estimatedMinutes: Int = 30,
    val createdAt: Long = System.currentTimeMillis()
)
