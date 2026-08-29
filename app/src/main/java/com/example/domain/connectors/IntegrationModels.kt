package com.example.domain.connectors

enum class IntegrationProviderType(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val category: String,
    val authType: AuthType
) {
    GOOGLE_WORKSPACE(
        id = "google_workspace",
        title = "Google Workspace",
        description = "Calendar, Tasks, Gmail, Docs, Sheets, Slides & Drive file sync",
        iconName = "google",
        category = "Productivity",
        authType = AuthType.OAUTH
    ),
    GITHUB(
        id = "github",
        title = "GitHub",
        description = "Repositories, Projects, Issue tracking, PR summaries & code search",
        iconName = "github",
        category = "Developer Tools",
        authType = AuthType.PERSONAL_ACCESS_TOKEN
    ),
    SLACK(
        id = "slack",
        title = "Slack",
        description = "Channel messages, focus mode status updates, reminders & team pings",
        iconName = "slack",
        category = "Communication",
        authType = AuthType.WEBHOOK_OR_TOKEN
    )
}

enum class AuthType {
    OAUTH,
    PERSONAL_ACCESS_TOKEN,
    WEBHOOK_OR_TOKEN
}

data class ConnectorAccount(
    val providerId: String,
    val isConnected: Boolean,
    val accountEmailOrUser: String? = null,
    val lastSyncedMillis: Long? = null,
    val activeScopes: List<String> = emptyList(),
    val customTokenOrWebhook: String? = null
)
