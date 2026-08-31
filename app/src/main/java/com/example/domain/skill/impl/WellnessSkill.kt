package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

class WellnessSkill : AgentSkill {
    override val id: String = "WELLNESS"
    override val displayName: String = "Emotional & Physical Wellness"
    override val description: String = "Logs mood scores, triggers mindful breathing, and provides circadian tips."
    override val systemPromptExtension: String = "Focus: Stress relief, emotional regulation, mindful breathing, and wellness tracking."

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "log_wellness",
                    description = "Record the user's emotional mood score, energy, hydration, or gratitude note in the wellness database.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "moodScore" to GeminiPropertySchema(type = "INTEGER", description = "Mood rating 1 (distressed) to 5 (thriving)"),
                            "moodLabel" to GeminiPropertySchema(type = "STRING", description = "Descriptive word for emotional state (e.g. Joyful, Calm, Overwhelmed, Tired, Grateful)"),
                            "energyLevel" to GeminiPropertySchema(type = "INTEGER", description = "Energy level 1 (drained) to 5 (fully energized)"),
                            "hydrationIncrementCups" to GeminiPropertySchema(type = "INTEGER", description = "Number of water cups drank (e.g. 1, 2)"),
                            "gratitudeNote" to GeminiPropertySchema(type = "STRING", description = "Something the user is thankful for today")
                        )
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "get_wellness_insights",
                    description = "Query the external wellness knowledge base and user history for personalized circadian, hydration, and stress recovery tips.",
                    parameters = GeminiParametersSchema(properties = emptyMap())
                ),
                GeminiFunctionDeclaration(
                    name = "start_breathing_exercise",
                    description = "Trigger a real-time mindful breathing exercise session where Lumi expands/contracts with the user's breath.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "pattern" to GeminiPropertySchema(type = "STRING", description = "Breathing pattern", enum = listOf("Box Breathing (4-4-4-4)", "Relaxing (4-7-8)", "Energy Boost (4-2-4-2)")),
                            "cycles" to GeminiPropertySchema(type = "INTEGER", description = "Number of breath cycles (e.g. 4, 6, 8)")
                        )
                    )
                ),
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
                )
            )
        )
    )
}
