package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

class GeneralCompanionSkill : AgentSkill {
    override val id: String = "GENERAL_COMPANION"
    override val displayName: String = "General Companion & Life Assistant"
    override val description: String = "Empathetic conversation, personal memory formation, and general daily queries."
    override val systemPromptExtension: String = "Focus: General warm companion dialogue, empathetic active listening, and forming core personal memories."

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "save_pet_memory",
                    description = "Store a meaningful insight, user preference, emotion, or life goal into Lumi's long-term memory archive so Lumi remembers and evolves.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "topic" to GeminiPropertySchema(type = "STRING", description = "Memory category or theme (e.g. Work Stress, Favorite Song, Life Dream, Friend Birthday)"),
                            "note" to GeminiPropertySchema(type = "STRING", description = "Summary of what user shared"),
                            "sentiment" to GeminiPropertySchema(type = "STRING", description = "Sentiment: Positive, Neutral, Gentle Support")
                        ),
                        required = listOf("topic", "note")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "get_daily_schedule",
                    description = "Retrieve all scheduled calendar events and routines for today to analyze availability or conflicts.",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "list_pending_tasks",
                    description = "Fetch all open, pending tasks to help the user prioritize their day.",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                )
            )
        )
    )
}
