package com.example.domain.tools

import com.example.domain.connectors.IntegrationService

object IntegrationToolsModule {

    fun register(
        integrationService: IntegrationService,
        registry: ToolRegistry = ToolRegistry.getInstance()
    ) {
        registry.registerTools(listOf(
            GoogleCreateDocTool(integrationService),
            GithubCreateIssueTool(integrationService),
            SlackPostMessageTool(integrationService)
        ))
    }
}

class GoogleCreateDocTool(private val integrationService: IntegrationService) : LumiTool {
    override val id: String = "google_create_doc"
    override val displayName: String = "Create Google Doc 📄"
    override val description: String = "Creates a new document in Google Drive"
    override val category: ToolCategory = ToolCategory.CONNECTORS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.HIGH
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("title", "string", "Document title"),
        ToolParameter("content", "string", "Document content"),
        ToolParameter("folder", "string", "Target folder name", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val title = params["title"] as? String ?: return ToolExecutionResult(false, "Missing title")
        val content = params["content"] as? String ?: ""
        val folder = params["folder"] as? String ?: "Lumi AI Notes"
        
        integrationService.googleCreateDoc(title, content, folder)
        
        return ToolExecutionResult(
            success = true, 
            resultText = "Created Google Doc: $title", 
            payload = mapOf("title" to title)
        )
    }
}

class GithubCreateIssueTool(private val integrationService: IntegrationService) : LumiTool {
    override val id: String = "github_create_issue"
    override val displayName: String = "Create GitHub Issue 🛠️"
    override val description: String = "Opens a new issue in a GitHub repository"
    override val category: ToolCategory = ToolCategory.CONNECTORS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.HIGH
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("repo", "string", "Repository name (owner/repo)"),
        ToolParameter("title", "string", "Issue title"),
        ToolParameter("body", "string", "Issue description"),
        ToolParameter("labels", "string", "Comma separated labels", required = false)
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val repo = params["repo"] as? String ?: return ToolExecutionResult(false, "Missing repo")
        val title = params["title"] as? String ?: return ToolExecutionResult(false, "Missing title")
        val body = params["body"] as? String ?: ""
        val labels = (params["labels"] as? String)?.split(",") ?: emptyList()
        
        integrationService.githubCreateIssue(repo, title, body, labels)
        
        return ToolExecutionResult(
            success = true, 
            resultText = "Created GitHub Issue in $repo", 
            payload = mapOf("repo" to repo, "issueTitle" to title)
        )
    }
}

class SlackPostMessageTool(private val integrationService: IntegrationService) : LumiTool {
    override val id: String = "slack_post_message"
    override val displayName: String = "Post to Slack 💬"
    override val description: String = "Sends a message to a Slack channel"
    override val category: ToolCategory = ToolCategory.CONNECTORS
    override val riskLevel: ToolRiskLevel = ToolRiskLevel.HIGH
    override val parameters: List<ToolParameter> = listOf(
        ToolParameter("channel", "string", "Channel name or ID"),
        ToolParameter("message", "string", "Message text")
    )

    override suspend fun execute(params: Map<String, Any?>): ToolExecutionResult {
        val channel = params["channel"] as? String ?: return ToolExecutionResult(false, "Missing channel")
        val message = params["message"] as? String ?: ""
        
        integrationService.slackPostMessage(channel, message)
        
        return ToolExecutionResult(
            success = true, 
            resultText = "Message posted to #$channel", 
            payload = mapOf("channel" to channel)
        )
    }
}
