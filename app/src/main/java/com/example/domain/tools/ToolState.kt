package com.example.domain.tools

/**
 * Individual tool enable/disable override.
 * When null (no override), the tool follows its group's state.
 */
data class ToolState(
    val toolId: String,
    /** null = follow group state, true/false = explicit override */
    val overrideEnabled: Boolean? = null
)

/**
 * A tool group — either static (from ToolGroupCatalog) or dynamic (MCP server).
 * Groups are the primary unit of enable/disable management.
 */
data class ToolGroupState(
    val groupId: String,
    val displayName: String,
    val description: String,
    val category: ToolCategory,
    /** Master switch: when disabled, ALL tools in this group are disabled regardless of individual overrides. */
    val isEnabled: Boolean = true,
    /** True for MCP server groups (discovered at runtime), false for built-in static groups. */
    val isDynamic: Boolean = false,
    /** For dynamic groups: the MCP server endpoint URL. Null for static groups. */
    val endpointUrl: String? = null,
    /** Individual tool overrides within this group. */
    val toolStates: List<ToolState> = emptyList()
) {
    /**
     * Resolves whether a specific tool in this group is effectively enabled.
     * A tool is enabled only if:
     * 1. The group is enabled, AND
     * 2. The tool has no override OR its override is true
     */
    fun isToolEffectivelyEnabled(toolId: String): Boolean {
        if (!isEnabled) return false
        val override = toolStates.find { it.toolId == toolId }?.overrideEnabled
        return override ?: true
    }
}
