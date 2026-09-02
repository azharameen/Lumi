package com.example.domain.connectors

import kotlinx.coroutines.flow.StateFlow

interface ConnectorRepository {
    val googleStatus: StateFlow<ConnectorSyncStatus>
    val googleConnected: StateFlow<Boolean>
    val googleAccount: StateFlow<String>

    val githubStatus: StateFlow<ConnectorSyncStatus>
    val githubConnected: StateFlow<Boolean>
    val githubUser: StateFlow<String>
    val githubToken: StateFlow<String>

    val slackStatus: StateFlow<ConnectorSyncStatus>
    val slackConnected: StateFlow<Boolean>
    val slackChannel: StateFlow<String>
    val slackWebhook: StateFlow<String>

    fun setGoogleConnection(connected: Boolean, email: String = "")
    fun updateGoogleStatus(status: ConnectorSyncStatus)

    fun setGithubConnection(connected: Boolean, user: String, token: String)
    fun updateGithubStatus(status: ConnectorSyncStatus)

    fun setSlackConnection(connected: Boolean, channel: String, webhook: String)
    fun updateSlackStatus(status: ConnectorSyncStatus)
}
