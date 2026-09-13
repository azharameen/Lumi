package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

/**
 * Enterprise Communication Skill for phone calls, dialing, and SMS messaging.
 * Marked as transactional (isTransactional = true) to ensure zero-turn history isolation
 * and eliminate conversational context bleeding.
 */
class CommunicationSkill : AgentSkill {
    override val id: String = "COMMUNICATION"
    override val displayName: String = "Phone & Communication"
    override val description: String = "Direct actions for dialing phone numbers and drafting SMS text messages."
    override val systemPromptExtension: String = "Focus: Launching phone dialer or drafting SMS messages accurately when requested."
    override val isTransactional: Boolean = true

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "communication_dial_number",
                    description = "Opens phone dialer pre-filled with the specified phone number",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "phoneNumber" to GeminiPropertySchema(
                                type = "STRING",
                                description = "Phone number or digits to dial"
                            )
                        ),
                        required = listOf("phoneNumber")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "communication_draft_sms",
                    description = "Opens SMS app pre-filled with phone number and text message body",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "phoneNumber" to GeminiPropertySchema(
                                type = "STRING",
                                description = "Target recipient phone number"
                            ),
                            "message" to GeminiPropertySchema(
                                type = "STRING",
                                description = "SMS text message body to send"
                            )
                        ),
                        required = listOf("phoneNumber", "message")
                    )
                )
            )
        )
    )
}
