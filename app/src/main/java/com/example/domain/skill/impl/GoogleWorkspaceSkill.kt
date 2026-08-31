package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

class GoogleWorkspaceSkill : AgentSkill {
    override val id: String = "GOOGLE_WORKSPACE"
    override val displayName: String = "Google Workspace Integrations"
    override val description: String = "Drafts emails, creates Docs, appends Sheets rows, generates Slides, and pins Keep notes."
    override val systemPromptExtension: String = """
        Focus: Professional document creation, communication, and Google Cloud workspace productivity.
        Guidelines: Confirm details cleanly when drafting emails or documents for the user.
    """.trimIndent()

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "google_send_email",
                    description = "Draft and send an email to a contact via the authenticated Google Workspace / Gmail connector.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "to" to GeminiPropertySchema(type = "STRING", description = "Recipient email address"),
                            "subject" to GeminiPropertySchema(type = "STRING", description = "Email subject line"),
                            "body" to GeminiPropertySchema(type = "STRING", description = "Email body message")
                        ),
                        required = listOf("to", "subject", "body")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_create_doc",
                    description = "Create a rich Google Doc in Google Drive with notes, summaries, or action plans.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Document title"),
                            "content" to GeminiPropertySchema(type = "STRING", description = "Document markdown or text body"),
                            "folder" to GeminiPropertySchema(type = "STRING", description = "Target Drive folder")
                        ),
                        required = listOf("title", "content")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_append_sheet_row",
                    description = "Log structured metrics or habit entries into a Google Sheets spreadsheet.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "sheetName" to GeminiPropertySchema(type = "STRING", description = "Name of target Sheet"),
                            "values" to GeminiPropertySchema(type = "ARRAY", description = "Row data values to append")
                        ),
                        required = listOf("sheetName")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_create_slides",
                    description = "Generate a Google Slides presentation outline and slide deck in Google Drive.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Presentation deck title"),
                            "slides" to GeminiPropertySchema(type = "ARRAY", description = "List of slide topics or bullets")
                        ),
                        required = listOf("title")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "google_sync_keep_note",
                    description = "Pin and sync a note, grocery list, or brainstorm to Google Keep.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "title" to GeminiPropertySchema(type = "STRING", description = "Keep note title"),
                            "note" to GeminiPropertySchema(type = "STRING", description = "Keep note body content"),
                            "colorTag" to GeminiPropertySchema(type = "STRING", description = "Color label (Cyan, Yellow, Pink, Green)")
                        ),
                        required = listOf("title", "note")
                    )
                )
            )
        )
    )
}
