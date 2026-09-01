package com.example.domain.agent.nodes

import com.example.domain.agent.AgentNode
import com.example.domain.agent.AgentState

class StartNode : AgentNode {
    override val name: String = "START"
    override suspend fun execute(state: AgentState): AgentState = state
}
