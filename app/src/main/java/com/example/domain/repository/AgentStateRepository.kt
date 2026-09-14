package com.example.domain.repository

import com.example.domain.agent.AgentState

interface AgentStateRepository {
    suspend fun saveCheckpoint(state: AgentState)
    suspend fun deleteCheckpoint(stateId: String)
}
