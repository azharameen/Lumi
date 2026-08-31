package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

class SlackSkill : AgentSkill {
    override val id: String = "SLACK"
    override val displayName: String = "Slack Integration"
    override val description: String = "Posts channel updates and synchronizes focus status on Slack."
    override val systemPromptExtension: String = "Focus: Team communications and focus status sync."

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "slack_post_message",
                    description = "Broadcast a channel update, progress report, or reminder to Slack.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "channel" to GeminiPropertySchema(type = "STRING", description = "Target channel like #general or #standup"),
                            "message" to GeminiPropertySchema(type = "STRING", description = "Message text to broadcast")
                        ),
                        required = listOf("channel", "message")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "slack_set_focus_status",
                    description = "Synchronize Do-Not-Disturb and set user Slack status while focusing with Lumi.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "statusText" to GeminiPropertySchema(type = "STRING", description = "Status message"),
                            "emoji" to GeminiPropertySchema(type = "STRING", description = "Slack emoji tag like :brain: or :paw_prints:"),
                            "durationMinutes" to GeminiPropertySchema(type = "INTEGER", description = "DND duration in minutes")
                        )
                    )
                )
            )
        )
    )
}
