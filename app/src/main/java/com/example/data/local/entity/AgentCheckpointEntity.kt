package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores serialized checkpoints of the Agent State Machine.
 * Allows suspended graphs (e.g. WAITING_FOR_HITL) to resume even after app process death.
 */
@Entity(tableName = "agent_checkpoints")
data class AgentCheckpointEntity(
    @PrimaryKey
    val stateId: String,
    val userQuery: String,
    val currentNodeName: String,
    val status: String,
    val pendingToolName: String?,
    val pendingToolArgsJson: String?,
    val serializedStateJson: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
