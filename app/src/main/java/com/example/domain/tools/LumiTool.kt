package com.example.domain.tools

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import java.util.Locale

/**
 * Category classification for tools in the 1,000+ tool ecosystem.
 * Used for grouping, filtering, and UI display.
 */
enum class ToolCategory(val displayName: String) {
    SYSTEM("System & Device"),
    CALENDAR("Calendar & Tasks"),
    COMMUNICATION("Communication"),
    HEALTH("Health & Wellness"),
    CONNECTORS("Cloud Connectors"),
    IOT("Smart Home & IoT"),
    UTILITY("Utility"),
    AUDIO("Audio & Media"),
    TIME("Time & Scheduling"),
    LOCATION("Location & Navigation"),
    KNOWLEDGE("Knowledge & Search"),
    FINANCE("Finance & Budget"),
    VISION("Camera & Vision"),
    LEARNING("Learning & Flashcards"),
    PRODUCTIVITY("Productivity & Notes"),
    LEARN("Microsoft Learn & Docs"),
    PROACTIVE("Proactive & Ambient"),
    SECURITY("Security & Biometrics")
}

/**
 * Risk classification to enforce safety guardrails.
 */
enum class ToolRiskLevel {
    LOW,       // Safe to auto-execute (e.g. read battery, read time)
    MEDIUM,    // Mild impact (e.g. set alarm, toggle flashlight)
    HIGH       // Requires explicit user confirmation dialog (e.g. send SMS, delete data)
}

/**
 * Parameter definition for tool JSON schemas.
 */
data class ToolParameter(
    val name: String,
    val type: String, // "string", "number", "boolean"
    val description: String,
    val required: Boolean = true
)

/**
 * Execution result returned by any LumiTool implementation.
 */
data class ToolExecutionResult(
    val success: Boolean,
    val resultText: String,
    val errorDetails: String? = null,
    val payload: Map<String, Any?> = emptyMap()
)

/**
 * Represents a named group of related tools for organized registration and display.
 */
data class ToolGroup(
    val groupId: String,
    val displayName: String,
    val description: String,
    val category: ToolCategory,
    val toolIds: List<String>
)

/**
 * Unified interface for any executable capability in Lumi.
 */
interface LumiTool {
    val id: String
    val displayName: String
    val description: String
    val category: ToolCategory
    val riskLevel: ToolRiskLevel
    val parameters: List<ToolParameter>

    /**
     * Optional group ID for organizing tools into logical clusters.
     * Defaults to null (ungrouped).
     */
    val groupId: String? get() = null

    suspend fun execute(params: Map<String, Any?>): ToolExecutionResult
}

fun LumiTool.toGeminiToolWrapper(): GeminiToolWrapper {
    val props = parameters.associate { param ->
        val propType = when (param.type.lowercase(Locale.ROOT)) {
            "number", "int", "integer", "float", "double" -> "NUMBER"
            "boolean", "bool" -> "BOOLEAN"
            "array", "list" -> "ARRAY"
            "object", "map" -> "OBJECT"
            else -> "STRING"
        }
        param.name to GeminiPropertySchema(
            type = propType,
            description = param.description
        )
    }
    val requiredProps = parameters.filter { it.required }.map { it.name }
    val decl = GeminiFunctionDeclaration(
        name = id,
        description = description,
        parameters = GeminiParametersSchema(
            properties = props,
            required = requiredProps
        )
    )
    return GeminiToolWrapper(functionDeclarations = listOf(decl))
}
