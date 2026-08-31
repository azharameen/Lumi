package com.example.domain.agent

import com.example.data.local.dao.AgentCheckpointDao
import com.example.data.local.entity.AgentCheckpointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * State Machine Engine for executing Agentic workflows using Kotlin Coroutines & Flows.
 * Replaces unstructured ReAct loops with a deterministic Directed Acyclic Graph (DAG).
 */
class AgentStateMachine(
    private val checkpointDao: AgentCheckpointDao? = null
) {

    private val nodes = ConcurrentHashMap<String, AgentNode>()
    private val transitions = ConcurrentHashMap<String, (AgentState) -> String>()

    /**
     * Registers a node in the state machine graph.
     */
    fun registerNode(node: AgentNode): AgentStateMachine {
        nodes[node.name] = node
        return this
    }

    /**
     * Defines a conditional edge from [sourceNodeName] to a target node name determined by [transitionLogic].
     */
    fun addEdge(sourceNodeName: String, transitionLogic: (AgentState) -> String): AgentStateMachine {
        transitions[sourceNodeName] = transitionLogic
        return this
    }

    /**
     * Executes the state machine starting from initial state, emitting intermediate states via [Flow].
     * Can pause if [AgentStatus.WAITING_FOR_HITL] is set, allowing safe mobile lifecycle suspensions.
     */
    fun run(initialState: AgentState): Flow<AgentState> = flow {
        var currentState = initialState

        while (currentState.status == AgentStatus.RUNNING) {
            // Protect against infinite execution loops
            if (currentState.stepCount >= currentState.maxSteps) {
                val failedState = currentState.copy(
                    status = AgentStatus.FAILED,
                    lastError = "Agent exceeded maximum step limit of ${currentState.maxSteps}"
                )
                emit(failedState)
                checkpointDao?.deleteCheckpoint(currentState.id)
                return@flow
            }

            val currentNodeName = currentState.currentNodeName
            val node = nodes[currentNodeName]

            if (node == null) {
                val failedState = currentState.copy(
                    status = AgentStatus.FAILED,
                    lastError = "No registered node found for name: $currentNodeName"
                )
                emit(failedState)
                checkpointDao?.deleteCheckpoint(currentState.id)
                return@flow
            }

            // Increment step count and execute current node
            currentState = currentState.copy(stepCount = currentState.stepCount + 1)
            currentState = node.execute(currentState)
            emit(currentState)

            // Check if state was suspended for HITL approval or completed/failed
            if (currentState.status != AgentStatus.RUNNING) {
                if (currentState.status == AgentStatus.WAITING_FOR_HITL) {
                    // Checkpoint state to database for process-death resilience
                    checkpointDao?.saveCheckpoint(
                        AgentCheckpointEntity(
                            stateId = currentState.id,
                            userQuery = currentState.userQuery,
                            currentNodeName = currentState.currentNodeName,
                            status = currentState.status.name,
                            pendingToolName = currentState.pendingToolName,
                            pendingToolArgsJson = currentState.pendingToolArgs?.toString(),
                            serializedStateJson = ""
                        )
                    )
                } else {
                    checkpointDao?.deleteCheckpoint(currentState.id)
                }
                break
            }

            // Determine next node using transition rules
            val transition = transitions[currentNodeName]
            if (transition != null) {
                val nextNodeName = transition(currentState)
                currentState = currentState.copy(currentNodeName = nextNodeName)
            } else {
                // Default termination if no transition edge defined
                currentState = currentState.copy(status = AgentStatus.COMPLETED)
                emit(currentState)
                checkpointDao?.deleteCheckpoint(currentState.id)
                break
            }
        }
    }

    /**
     * Resumes execution after Human-In-The-Loop (HITL) approval or rejection.
     */
    fun resumeFromHitl(state: AgentState, approved: Boolean): Flow<AgentState> {
        val updatedState = if (approved) {
            state.copy(
                status = AgentStatus.RUNNING,
                hitlRequired = false,
                currentNodeName = "TOOL_EXECUTION"
            )
        } else {
            state.copy(
                status = AgentStatus.RUNNING,
                hitlRequired = false,
                pendingToolName = null,
                pendingToolArgs = null,
                lastError = "Tool execution rejected by user",
                currentNodeName = "REASONING"
            )
        }
        return run(updatedState)
    }
}
