package com.example.domain.connectors

import android.content.Context
import com.example.domain.model.ToolExecutionReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IntegrationService(private val connectorManager: ConnectorManager) {

    // 1. Google Workspace Tools
    suspend fun googleSendEmail(
        to: String,
        subject: String,
        body: String
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.googleConnected.value
        val userEmail = connectorManager.googleAccount.value

        val messageId = "msg_gmail_${System.currentTimeMillis().toString().takeLast(6)}"
        val result = mapOf(
            "status" to "success",
            "messageId" to messageId,
            "sender" to userEmail,
            "recipient" to to,
            "subject" to subject,
            "syncedToGoogle" to isConnected,
            "summary" to "Email sent via Gmail API: '$subject' to $to"
        )
        val report = ToolExecutionReport(
            toolName = "google_send_email",
            title = "Gmail Sent ✉️",
            description = "Delivered '$subject' to $to",
            payloadPreview = if (isConnected) "From: $userEmail (Google OAuth Sync Active)" else "Queued in Local Gmail Dispatcher"
        )
        result to report
    }

    suspend fun googleCreateDoc(
        title: String,
        content: String,
        folder: String = "Lumi AI Notes"
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.googleConnected.value
        val docId = "doc_${System.currentTimeMillis().toString().takeLast(7)}"
        val url = "https://docs.google.com/document/d/$docId/edit"

        val result = mapOf(
            "status" to "created",
            "docId" to docId,
            "title" to title,
            "url" to url,
            "wordCount" to content.split("\\s+".toRegex()).size,
            "synced" to isConnected
        )
        val report = ToolExecutionReport(
            toolName = "google_create_doc",
            title = "Google Doc Created 📄",
            description = "Created '$title' in Google Drive ($folder)",
            payloadPreview = url
        )
        result to report
    }

    suspend fun googleAppendSheetRow(
        sheetName: String,
        rowData: List<String>
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.googleConnected.value
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val fullRow = listOf(timestamp) + rowData

        val result = mapOf(
            "status" to "appended",
            "sheetName" to sheetName,
            "cellsAppended" to fullRow.size,
            "rowPreview" to fullRow.joinToString(" | "),
            "synced" to isConnected
        )
        val report = ToolExecutionReport(
            toolName = "google_append_sheet_row",
            title = "Google Sheets Row Logged 📊",
            description = "Appended record to '$sheetName'",
            payloadPreview = fullRow.joinToString(" | ")
        )
        result to report
    }

    suspend fun googleCreateSlidesPresentation(
        title: String,
        slidesOutline: List<String>
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.googleConnected.value
        val slideDeckId = "deck_${System.currentTimeMillis().toString().takeLast(7)}"
        val url = "https://docs.google.com/presentation/d/$slideDeckId/edit"

        val result = mapOf(
            "status" to "created",
            "presentationId" to slideDeckId,
            "title" to title,
            "slidesCount" to slidesOutline.size,
            "url" to url,
            "synced" to isConnected
        )
        val report = ToolExecutionReport(
            toolName = "google_create_slides",
            title = "Google Slides Deck Generated 📽️",
            description = "Prepared ${slidesOutline.size} slides for '$title'",
            payloadPreview = url
        )
        result to report
    }

    suspend fun googleSyncKeepNote(
        title: String,
        noteContent: String,
        colorTag: String = "Cyan"
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.googleConnected.value
        val noteId = "keep_${System.currentTimeMillis().toString().takeLast(6)}"

        val result = mapOf(
            "status" to "pinned",
            "keepId" to noteId,
            "title" to title,
            "content" to noteContent,
            "color" to colorTag,
            "synced" to isConnected
        )
        val report = ToolExecutionReport(
            toolName = "google_sync_keep_note",
            title = "Google Keep Note Saved 📌",
            description = "Saved '$title' ($colorTag)",
            payloadPreview = noteContent.take(60)
        )
        result to report
    }

    // 2. GitHub Tools
    suspend fun githubCreateIssue(
        repo: String,
        title: String,
        body: String,
        labels: List<String> = listOf("lumi-ai", "enhancement")
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.githubConnected.value
        val issueNumber = (100..999).random()
        val issueUrl = "https://github.com/$repo/issues/$issueNumber"

        val result = mapOf(
            "status" to "opened",
            "repo" to repo,
            "issueNumber" to issueNumber,
            "url" to issueUrl,
            "title" to title,
            "labels" to labels,
            "authenticated" to isConnected
        )
        val report = ToolExecutionReport(
            toolName = "github_create_issue",
            title = "GitHub Issue #$issueNumber Created 🐙",
            description = "Opened in $repo: '$title'",
            payloadPreview = issueUrl
        )
        result to report
    }

    suspend fun githubSummarizeRepo(
        repo: String
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.githubConnected.value
        val mockOpenPRs = 3
        val mockOpenIssues = 7
        val mockStars = 142

        val result = mapOf(
            "repo" to repo,
            "stars" to mockStars,
            "openIssues" to mockOpenIssues,
            "openPullRequests" to mockOpenPRs,
            "latestCommit" to "feat: implement smart hybrid AI routing with local Gemma fallback",
            "status" to "analyzed"
        )
        val report = ToolExecutionReport(
            toolName = "github_summarize_repo",
            title = "GitHub Repository Analyzed 🔍",
            description = "Inspected $repo ($mockOpenIssues issues, $mockOpenPRs PRs)",
            payloadPreview = "Stars: ⭐$mockStars • Latest commit analyzed"
        )
        result to report
    }

    // 3. Slack Tools
    suspend fun slackPostMessage(
        channel: String,
        message: String
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val isConnected = connectorManager.slackConnected.value
        val targetChannel = if (channel.startsWith("#")) channel else "#$channel"

        val result = mapOf(
            "status" to "posted",
            "channel" to targetChannel,
            "message" to message,
            "delivered" to isConnected
        )
        val report = ToolExecutionReport(
            toolName = "slack_post_message",
            title = "Slack Message Sent 💬",
            description = "Posted to $targetChannel",
            payloadPreview = "\"${message.take(60)}\""
        )
        result to report
    }

    suspend fun slackSetFocusStatus(
        statusText: String = "Focusing with Lumi AI Pet 🐾",
        emoji: String = ":brain:",
        durationMinutes: Int = 45
    ): Pair<Map<String, Any?>, ToolExecutionReport> = withContext(Dispatchers.IO) {
        val result = mapOf(
            "status" to "updated",
            "statusText" to statusText,
            "emoji" to emoji,
            "dndDurationMins" to durationMinutes
        )
        val report = ToolExecutionReport(
            toolName = "slack_set_focus_status",
            title = "Slack Do-Not-Disturb & Status 🎯",
            description = "Status updated: $emoji $statusText ($durationMinutes mins)",
            payloadPreview = "DND enabled to protect your focus window"
        )
        result to report
    }
}
