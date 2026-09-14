package com.example.data.repository

import com.example.data.local.dao.AgentCheckpointDao
import com.example.data.local.entity.AgentCheckpointEntity
import com.example.domain.agent.AgentState
import com.example.domain.repository.AgentStateRepository

import com.example.domain.agent.AgentStateSerializer

class AgentStateRepositoryImpl(
    private val checkpointDao: AgentCheckpointDao
) : AgentStateRepository {
    override suspend fun saveCheckpoint(state: AgentState) {
        val serializedJson = AgentStateSerializer.serialize(state)
        checkpointDao.saveCheckpoint(
            AgentCheckpointEntity(
                stateId = state.id,
                userQuery = state.userQuery,
                currentNodeName = state.currentNodeName,
                status = state.status.name,
                pendingToolName = state.pendingToolName,
                pendingToolArgsJson = state.pendingToolArgs?.toString(),
                serializedStateJson = serializedJson
            )
        )
    }

    override suspend fun deleteCheckpoint(stateId: String) {
        checkpointDao.deleteCheckpoint(stateId)
    }

    override suspend fun getCheckpoint(stateId: String): AgentState? {
        val entity = checkpointDao.getCheckpoint(stateId) ?: return null
        return AgentStateSerializer.deserialize(entity.serializedStateJson)
    }

    override suspend fun getPendingHitlCheckpoints(): List<AgentState> {
        val entities = checkpointDao.getPendingHitlCheckpoints()
        return entities.mapNotNull { AgentStateSerializer.deserialize(it.serializedStateJson) }
    }
}
