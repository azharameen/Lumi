package com.example.domain.agent.hitl

import com.example.data.local.LumiDatabase
import com.example.domain.agent.AgentState
import com.example.domain.agent.AgentStatus
import com.example.domain.agent.LumiAgentGraph
import com.example.domain.tools.AgentToolDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Enterprise Human-In-The-Loop (HITL) approval manager.
 * Intercepts high-stakes agent actions (emails, GitHub issues, Slack messages)
 * and holds execution until explicit user confirmation in the Android UI.
 */
class HitlApprovalManager(
    private val database: LumiDatabase,
    private val toolDispatcher: AgentToolDispatcher
) {

    private val _pendingActions = MutableStateFlow<List<HitlPendingAction>>(emptyList())
    val pendingActions: StateFlow<List<HitlPendingAction>> = _pendingActions.asStateFlow()

    private val activeSuspendedStates = ConcurrentHashMap<String, AgentState>()

    /**
     * Intercepts a state machine that paused at WAITING_FOR_HITL.
     */
    fun enqueueHitlAction(state: AgentState) {
        val tool = state.pendingToolName ?: return
        val args = state.pendingToolArgs ?: emptyMap()

        val (title, description, preview) = when (tool) {
            "google_send_email" -> Triple(
                "Send Email via Gmail",
                "Draft to: ${args["to"]} • Subject: ${args["subject"]}",
                args["body"]?.toString()?.take(100) ?: ""
            )
            "google_create_doc" -> Triple(
                "Create Google Doc",
                "Document title: '${args["title"]}'",
                args["content"]?.toString()?.take(100) ?: ""
            )
            "github_create_issue" -> Triple(
                "Open GitHub Issue",
                "Target repo: ${args["repo"]} • Title: ${args["title"]}",
                args["body"]?.toString()?.take(100) ?: ""
            )
            "slack_post_message" -> Triple(
                "Broadcast to Slack",
                "Target channel: ${args["channel"]}",
                args["message"]?.toString()?.take(100) ?: ""
            )
            else -> Triple("Approve Tool Execution", "Tool: $tool", args.toString().take(100))
        }

        val action = HitlPendingAction(
            stateId = state.id,
            toolName = tool,
            actionTitle = title,
            actionDescription = description,
            payloadPreview = preview,
            state = state
        )

        activeSuspendedStates[state.id] = state
        _pendingActions.value = _pendingActions.value.filter { it.stateId != state.id } + action
    }

    /**
     * Resumes the paused state machine graph upon user approval or rejection.
     */
    suspend fun resolveAction(stateId: String, approved: Boolean): AgentState? {
        val suspendedState = activeSuspendedStates.remove(stateId) ?: return null
        _pendingActions.value = _pendingActions.value.filter { it.stateId != stateId }

        val stateMachine = LumiAgentGraph.create(database, toolDispatcher)
        var finalState = suspendedState

        stateMachine.resumeFromHitl(suspendedState, approved).collect { state ->
            finalState = state
        }

        return finalState
    }

    fun getPendingActionCount(): Int = _pendingActions.value.size
}
