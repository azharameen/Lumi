package com.example.domain.tools

interface ToolPermissionChecker {
    fun hasPermissionForTool(toolId: String): Pair<Boolean, String?>
}
