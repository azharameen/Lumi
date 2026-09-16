package com.example.domain.tools

import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing tool group and individual tool enable/disable state.
 * State is persisted and survives app restarts.
 *
 * Resolution order for whether a tool is active:
 * 1. If group is disabled → tool is disabled (regardless of override)
 * 2. If tool has explicit override → use override
 * 3. Otherwise → tool follows group state (enabled)
 */
interface ToolGroupRepository {
    /** All group states (static + dynamic), as a reactive stream. */
    val allGroups: Flow<List<ToolGroupState>>

    /** Resolve whether a specific tool is currently active (group + override). */
    suspend fun isToolActive(toolId: String): Boolean

    /** Enable or disable an entire group. */
    suspend fun setGroupEnabled(groupId: String, enabled: Boolean)

    /** Set an individual tool override within a group. Pass null to clear override (follow group). */
    suspend fun setToolOverride(groupId: String, toolId: String, enabled: Boolean?)

    /** Register a new dynamic group (e.g. MCP server) if not already tracked. */
    suspend fun registerDynamicGroup(groupId: String, displayName: String, description: String, category: ToolCategory, endpointUrl: String?)

    /** Register a tool under a group (so it appears in the group's tool list). */
    suspend fun registerToolInGroup(groupId: String, toolId: String, toolName: String)

    /** Get the group ID that a tool belongs to (via ToolGroupCatalog or dynamic tracking). */
    suspend fun getGroupIdForTool(toolId: String): String?
}
