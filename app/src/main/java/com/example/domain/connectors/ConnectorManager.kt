package com.example.domain.connectors

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Clean architectural manager for external provider credentials and decoupled connection status.
 */
class ConnectorManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("lumi_connectors_prefs", Context.MODE_PRIVATE)

    // Decoupled Contract Statuses
    private val _googleStatus = MutableStateFlow<ConnectorSyncStatus>(
        if (prefs.getBoolean("google_connected", false)) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
    )
    val googleStatus: StateFlow<ConnectorSyncStatus> = _googleStatus.asStateFlow()

    private val _googleConnected = MutableStateFlow(prefs.getBoolean("google_connected", false))
    val googleConnected: StateFlow<Boolean> = _googleConnected.asStateFlow()

    private val _googleAccount = MutableStateFlow(prefs.getString("google_account", "azharameen52@gmail.com") ?: "azharameen52@gmail.com")
    val googleAccount: StateFlow<String> = _googleAccount.asStateFlow()

    private val _githubStatus = MutableStateFlow<ConnectorSyncStatus>(
        if (prefs.getBoolean("github_connected", false)) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
    )
    val githubStatus: StateFlow<ConnectorSyncStatus> = _githubStatus.asStateFlow()

    private val _githubConnected = MutableStateFlow(prefs.getBoolean("github_connected", false))
    val githubConnected: StateFlow<Boolean> = _githubConnected.asStateFlow()

    private val _githubUser = MutableStateFlow(prefs.getString("github_user", "") ?: "")
    val githubUser: StateFlow<String> = _githubUser.asStateFlow()

    private val _githubToken = MutableStateFlow(prefs.getString("github_token", "") ?: "")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _slackStatus = MutableStateFlow<ConnectorSyncStatus>(
        if (prefs.getBoolean("slack_connected", false)) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
    )
    val slackStatus: StateFlow<ConnectorSyncStatus> = _slackStatus.asStateFlow()

    private val _slackConnected = MutableStateFlow(prefs.getBoolean("slack_connected", false))
    val slackConnected: StateFlow<Boolean> = _slackConnected.asStateFlow()

    private val _slackChannel = MutableStateFlow(prefs.getString("slack_channel", "#general") ?: "#general")
    val slackChannel: StateFlow<String> = _slackChannel.asStateFlow()

    private val _slackWebhook = MutableStateFlow(prefs.getString("slack_webhook", "") ?: "")
    val slackWebhook: StateFlow<String> = _slackWebhook.asStateFlow()

    fun setGoogleConnection(connected: Boolean, email: String = "azharameen52@gmail.com") {
        prefs.edit()
            .putBoolean("google_connected", connected)
            .putString("google_account", email)
            .apply()
        _googleConnected.value = connected
        _googleAccount.value = email
        _googleStatus.value = if (connected) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
    }

    fun updateGoogleStatus(status: ConnectorSyncStatus) {
        _googleStatus.value = status
        _googleConnected.value = status is ConnectorSyncStatus.Connected
    }

    fun setGithubConnection(connected: Boolean, user: String, token: String) {
        prefs.edit()
            .putBoolean("github_connected", connected)
            .putString("github_user", user)
            .putString("github_token", token)
            .apply()
        _githubConnected.value = connected
        _githubUser.value = user
        _githubToken.value = token
        _githubStatus.value = if (connected && token.isNotBlank()) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
    }

    fun updateGithubStatus(status: ConnectorSyncStatus) {
        _githubStatus.value = status
        _githubConnected.value = status is ConnectorSyncStatus.Connected
    }

    fun setSlackConnection(connected: Boolean, channel: String, webhook: String) {
        prefs.edit()
            .putBoolean("slack_connected", connected)
            .putString("slack_channel", channel)
            .putString("slack_webhook", webhook)
            .apply()
        _slackConnected.value = connected
        _slackChannel.value = channel
        _slackWebhook.value = webhook
        _slackStatus.value = if (connected && webhook.isNotBlank()) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
    }

    fun updateSlackStatus(status: ConnectorSyncStatus) {
        _slackStatus.value = status
        _slackConnected.value = status is ConnectorSyncStatus.Connected
    }
}
