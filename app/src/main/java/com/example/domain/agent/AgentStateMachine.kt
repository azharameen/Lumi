package com.example.domain.agent

import com.example.data.local.dao.AgentCheckpointDao
import com.example.data.local.entity.AgentCheckpointEntity
import com.example.domain.model.PetEmotion
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * State Machine Engine for executing Agentic workflows using Kotlin Coroutines & Flows.
 * Supports both sequential and parallel node execution.
 */
class AgentStateMachine(
    private val checkpointDao: AgentCheckpointDao? = null,
    private val hooks: List<com.example.domain.agent.hooks.AgentNodeHook> = listOf(
        com.example.domain.agent.hooks.TelemetryHook(),
        com.example.domain.agent.hooks.SensorContextHook(),
        com.example.domain.agent.hooks.SecurityAuditHook()
    )
) {

    private val nodes = ConcurrentHashMap<String, AgentNode>()
    private val transitions = ConcurrentHashMap<String, (AgentState) -> List<String>>()

    /**
     * Registers a node in the state machine graph.
     */
    fun registerNode(node: AgentNode): AgentStateMachine {
        nodes[node.name] = node
        return this
    }

    /**
     * Defines a sequential edge from [sourceNodeName] to a target node.
     */
    fun addEdge(sourceNodeName: String, transitionLogic: (AgentState) -> String): AgentStateMachine {
        transitions[sourceNodeName] = { state -> listOf(transitionLogic(state)) }
        return this
    }

    /**
     * Defines a branching edge from [sourceNodeName] to multiple parallel target nodes.
     */
    fun addParallelEdge(sourceNodeName: String, transitionLogic: (AgentState) -> List<String>): AgentStateMachine {
        transitions[sourceNodeName] = transitionLogic
        return this
    }

    /**
     * Executes the state machine starting from initial state, emitting intermediate states via [Flow].
     */
    fun run(initialState: AgentState): Flow<AgentState> = flow {
        var currentState = initialState
        var activeNodes = listOf(currentState.currentNodeName)

        while (currentState.status == AgentStatus.RUNNING && activeNodes.isNotEmpty()) {
            if (currentState.stepCount >= currentState.maxSteps) {
                emit(currentState.copy(status = AgentStatus.FAILED, lastError = "Step limit exceeded"))
                return@flow
            }

            // Execute active nodes (potentially in parallel)
            val nextState = coroutineScope {
                val deferredResults = activeNodes.map { nodeName ->
                    val node = nodes[nodeName]
                    if (node == null) throw IllegalStateException("Node $nodeName not found")
                    async {
                        var nodeState = currentState
                        for (hook in hooks) {
                            nodeState = hook.onBeforeNode(nodeName, nodeState)
                        }
                        try {
                            var result = node.execute(nodeState)
                            for (hook in hooks) {
                                result = hook.onAfterNode(nodeName, result)
                            }
                            result
                        } catch (e: Throwable) {
                            var errorState = nodeState
                            for (hook in hooks) {
                                errorState = hook.onNodeError(nodeName, errorState, e)
                            }
                            throw e
                        }
                    }
                }
                
                // Merge results sequentially (assuming nodes modify disjoint state fields)
                var merged = currentState.copy(stepCount = currentState.stepCount + 1)
                deferredResults.forEach { deferred ->
                    val result = deferred.await()
                    merged = mergeStates(merged, result)
                }
                merged
            }

            currentState = nextState
            emit(currentState)

            if (currentState.status != AgentStatus.RUNNING) {
                handleCheckpointing(currentState)
                break
            }

            // Determine next active nodes based on transitions
            // For simplicity, we take transitions from the LAST node in the parallel batch if multiple existed
            // or we could support complex join logic. Here we assume the last node triggers the next phase.
            val lastNodeName = activeNodes.last()
            activeNodes = transitions[lastNodeName]?.invoke(currentState) ?: emptyList()
            
            if (activeNodes.isNotEmpty()) {
                currentState = currentState.copy(currentNodeName = activeNodes.first())
            } else {
                currentState = currentState.copy(status = AgentStatus.COMPLETED)
                emit(currentState)
                handleCheckpointing(currentState)
                break
            }
        }
    }

    private fun mergeStates(base: AgentState, update: AgentState): AgentState {
        // Merge specific fields updated by nodes
        return base.copy(
            isLocalExecution = if (update.isLocalExecution != base.isLocalExecution) update.isLocalExecution else base.isLocalExecution,
            selectedSkillName = update.selectedSkillName ?: base.selectedSkillName,
            contentsList = if (update.contentsList.size > base.contentsList.size) update.contentsList else base.contentsList,
            retrievedContext = if (update.retrievedContext.isNotBlank()) update.retrievedContext else base.retrievedContext,
            pendingToolName = update.pendingToolName ?: base.pendingToolName,
            pendingToolArgs = update.pendingToolArgs ?: base.pendingToolArgs,
            finalResponseText = update.finalResponseText ?: base.finalResponseText,
            inferredEmotion = if (update.inferredEmotion != PetEmotion.HAPPY) update.inferredEmotion else base.inferredEmotion,
            status = if (update.status != AgentStatus.RUNNING) update.status else base.status,
            lastError = update.lastError ?: base.lastError
        )
    }

    private suspend fun handleCheckpointing(state: AgentState) {
        if (state.status == AgentStatus.WAITING_FOR_HITL) {
            checkpointDao?.saveCheckpoint(
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
        } else {
            checkpointDao?.deleteCheckpoint(state.id)
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
