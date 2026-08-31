package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing an explicit factual knowledge triple extracted by the AI companion.
 * Forms the on-device Knowledge Graph.
 */
@Entity(tableName = "fact_knowledge_graph")
data class FactKnowledgeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subject: String = "User",
    val predicate: String,       // e.g. "prefers", "has_friend", "works_at", "diet", "goal"
    val objectValue: String,     // e.g. "Morning walks", "Sarah", "Google", "Vegetarian"
    val confidence: Float = 0.95f,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
