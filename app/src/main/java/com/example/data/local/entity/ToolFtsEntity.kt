package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

/**
 * FTS4/FTS5 Room entity for ultra-fast full-text indexing of 1,000+ LumiTools.
 */
@Fts4
@Entity(tableName = "tools_fts")
data class ToolFtsEntity(
    @PrimaryKey
    val rowid: Int = 0,
    val toolId: String,
    val displayName: String,
    val description: String,
    val category: String,
    val keywords: String
)
