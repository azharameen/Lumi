package com.example.data.repository

import com.example.data.local.dao.AgentCheckpointDao
import com.example.data.local.entity.AgentCheckpointEntity
import com.example.domain.agent.AgentState
import com.example.domain.repository.AgentStateRepository

class AgentStateRepositoryImpl(
    private val checkpointDao: AgentCheckpointDao
) : AgentStateRepository {
    override suspend fun saveCheckpoint(state: AgentState) {
        checkpointDao.saveCheckpoint(
            AgentCheckpointEntity(
                stateId = state.id,
                userQuery = state.userQuery,
                currentNodeName = state.currentNodeName,
                status = state.status.name,
                pendingToolName = state.pendingToolName,
                pendingToolArgsJson = state.pendingToolArgs?.toString(),
                serializedStateJson = ""
            )
        )
    }

    override suspend fun deleteCheckpoint(stateId: String) {
        checkpointDao.deleteCheckpoint(stateId)
    }
}
