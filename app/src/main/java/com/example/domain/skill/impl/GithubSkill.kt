package com.example.domain.skill.impl

import com.example.data.remote.GeminiFunctionDeclaration
import com.example.data.remote.GeminiParametersSchema
import com.example.data.remote.GeminiPropertySchema
import com.example.data.remote.GeminiToolWrapper
import com.example.domain.skill.AgentSkill

class GithubSkill : AgentSkill {
    override val id: String = "GITHUB"
    override val displayName: String = "GitHub Integration"
    override val description: String = "Inspects GitHub repositories and opens issues or feature requests."
    override val systemPromptExtension: String = "Focus: Software engineering tasks, issue tracking, and repository summaries."

    override val tools: List<GeminiToolWrapper> = listOf(
        GeminiToolWrapper(
            functionDeclarations = listOf(
                GeminiFunctionDeclaration(
                    name = "github_create_issue",
                    description = "Open a new issue or feature card in a GitHub repository.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "repo" to GeminiPropertySchema(type = "STRING", description = "Repository in owner/repo format"),
                            "title" to GeminiPropertySchema(type = "STRING", description = "Issue title"),
                            "body" to GeminiPropertySchema(type = "STRING", description = "Issue details and specs"),
                            "labels" to GeminiPropertySchema(type = "ARRAY", description = "Optional issue labels")
                        ),
                        required = listOf("repo", "title")
                    )
                ),
                GeminiFunctionDeclaration(
                    name = "github_summarize_repo",
                    description = "Inspect repository health, open issues, active PRs, and recent commits on GitHub.",
                    parameters = GeminiParametersSchema(
                        properties = mapOf(
                            "repo" to GeminiPropertySchema(type = "STRING", description = "Repository in owner/repo format")
                        ),
                        required = listOf("repo")
                    )
                )
            )
        )
    )
}
