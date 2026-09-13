package com.example.domain.ai

/**
 * Represents a cloud LLM model descriptor.
 * Instances are parsed dynamically from Firebase Remote Config JSON — never hardcoded.
 */
data class CloudModelSpec(
    val id: String,
    val displayName: String,
    val description: String
)
