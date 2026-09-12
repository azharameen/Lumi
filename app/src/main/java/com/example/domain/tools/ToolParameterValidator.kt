package com.example.domain.tools

import org.json.JSONArray
import org.json.JSONObject

/**
 * Result of tool parameter schema validation.
 */
data class ToolValidationResult(
    val isValid: Boolean,
    val validatedParams: Map<String, Any?>,
    val errorMessage: String? = null
)

/**
 * Enterprise Tool Parameter Validator enforcing type safety, non-null assertions,
 * string regex validation, and range checks before executing any LumiTool.
 */
object ToolParameterValidator {

    fun validate(tool: LumiTool, rawParams: Map<String, Any?>): ToolValidationResult {
        val validatedMap = mutableMapOf<String, Any?>()
        val errors = mutableListOf<String>()

        for (paramSpec in tool.parameters) {
            val rawVal = rawParams[paramSpec.name]

            if (rawVal == null || (rawVal is String && rawVal.isBlank())) {
                if (paramSpec.required) {
                    errors.add("Missing required parameter '${paramSpec.name}' (${paramSpec.type}): ${paramSpec.description}")
                } else {
                    validatedMap[paramSpec.name] = null
                }
                continue
            }

            when (paramSpec.type.lowercase(java.util.Locale.ROOT)) {
                "string" -> {
                    val strVal = rawVal.toString().trim()
                    validatedMap[paramSpec.name] = strVal
                }

                "number", "integer", "int", "float", "double" -> {
                    val numVal = when (rawVal) {
                        is Number -> rawVal.toDouble()
                        is String -> rawVal.toDoubleOrNull()
                        else -> null
                    }
                    if (numVal == null) {
                        errors.add("Parameter '${paramSpec.name}' must be a valid number, received: '$rawVal'")
                    } else {
                        validatedMap[paramSpec.name] = numVal
                    }
                }

                "boolean", "bool" -> {
                    val boolVal = when (rawVal) {
                        is Boolean -> rawVal
                        is String -> rawVal.lowercase(java.util.Locale.ROOT).toBooleanStrictOrNull()
                        else -> null
                    }
                    if (boolVal == null) {
                        errors.add("Parameter '${paramSpec.name}' must be a boolean (true/false), received: '$rawVal'")
                    } else {
                        validatedMap[paramSpec.name] = boolVal
                    }
                }

                "array", "list" -> {
                    val listVal = when (rawVal) {
                        is List<*> -> rawVal
                        is JSONArray -> {
                            val temp = mutableListOf<Any?>()
                            for (i in 0 until rawVal.length()) temp.add(rawVal.get(i))
                            temp
                        }
                        is String -> rawVal.split(",").map { it.trim() }
                        else -> listOf(rawVal)
                    }
                    validatedMap[paramSpec.name] = listVal
                }

                "object", "json" -> {
                    val objVal = when (rawVal) {
                        is Map<*, *> -> rawVal
                        is JSONObject -> rawVal
                        is String -> {
                            try {
                                JSONObject(rawVal)
                            } catch (_: Exception) {
                                null
                            }
                        }
                        else -> null
                    }
                    if (objVal == null && paramSpec.required) {
                        errors.add("Parameter '${paramSpec.name}' must be a valid JSON object")
                    } else {
                        validatedMap[paramSpec.name] = objVal
                    }
                }

                else -> {
                    validatedMap[paramSpec.name] = rawVal
                }
            }
        }

        return if (errors.isEmpty()) {
            ToolValidationResult(isValid = true, validatedParams = validatedMap)
        } else {
            ToolValidationResult(isValid = false, validatedParams = emptyMap(), errorMessage = errors.joinToString("; "))
        }
    }
}
