package com.example.domain.tools

/**
 * Category classification for tools in the 1,000+ tool ecosystem.
 */
enum class ToolCategory {
    SYSTEM,
    CALENDAR,
    COMMUNICATION,
    HEALTH,
    CONNECTORS,
    IOT,
    UTILITY
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
 * Unified interface for any executable capability in Lumi.
 */
interface LumiTool {
    val id: String
    val displayName: String
    val description: String
    val category: ToolCategory
    val riskLevel: ToolRiskLevel
    val parameters: List<ToolParameter>

    suspend fun execute(params: Map<String, Any?>): ToolExecutionResult
}

fun LumiTool.toGeminiToolWrapper(): com.example.data.remote.GeminiToolWrapper {
    val props = parameters.associate { param ->
        val propType = when (param.type.lowercase(java.util.Locale.ROOT)) {
            "number", "int", "integer", "float", "double" -> "NUMBER"
            "boolean", "bool" -> "BOOLEAN"
            "array", "list" -> "ARRAY"
            "object", "map" -> "OBJECT"
            else -> "STRING"
        }
        param.name to com.example.data.remote.GeminiPropertySchema(
            type = propType,
            description = param.description
        )
    }
    val requiredProps = parameters.filter { it.required }.map { it.name }
    val decl = com.example.data.remote.GeminiFunctionDeclaration(
        name = id,
        description = description,
        parameters = com.example.data.remote.GeminiParametersSchema(
            properties = props,
            required = requiredProps
        )
    )
    return com.example.data.remote.GeminiToolWrapper(functionDeclarations = listOf(decl))
}
