package com.example.data.repository

import com.example.domain.connectors.*

import android.content.Context
import androidx.datastore.preferences.core.*
import com.example.data.preferences.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Clean architectural manager for external provider credentials and decoupled connection status.
 */
class ConnectorRepositoryImpl(private val context: Context) : ConnectorRepository {

    companion object {
        private val GOOGLE_CONNECTED = booleanPreferencesKey("google_connected")
        private val GOOGLE_ACCOUNT = stringPreferencesKey("google_account")
        private val GITHUB_CONNECTED = booleanPreferencesKey("github_connected")
        private val GITHUB_USER = stringPreferencesKey("github_user")
        private val GITHUB_TOKEN = stringPreferencesKey("github_token")
        private val SLACK_CONNECTED = booleanPreferencesKey("slack_connected")
        private val SLACK_CHANNEL = stringPreferencesKey("slack_channel")
        private val SLACK_WEBHOOK = stringPreferencesKey("slack_webhook")
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Decoupled Contract Statuses
    private val _googleStatus = MutableStateFlow<ConnectorSyncStatus>(ConnectorSyncStatus.Disconnected)
    override val googleStatus: StateFlow<ConnectorSyncStatus> = _googleStatus.asStateFlow()

    private val _googleConnected = MutableStateFlow(false)
    override val googleConnected: StateFlow<Boolean> = _googleConnected.asStateFlow()

    private val _googleAccount = MutableStateFlow("azharameen52@gmail.com")
    override val googleAccount: StateFlow<String> = _googleAccount.asStateFlow()

    private val _githubStatus = MutableStateFlow<ConnectorSyncStatus>(ConnectorSyncStatus.Disconnected)
    override val githubStatus: StateFlow<ConnectorSyncStatus> = _githubStatus.asStateFlow()

    private val _githubConnected = MutableStateFlow(false)
    override val githubConnected: StateFlow<Boolean> = _githubConnected.asStateFlow()

    private val _githubUser = MutableStateFlow("")
    override val githubUser: StateFlow<String> = _githubUser.asStateFlow()

    private val _githubToken = MutableStateFlow("")
    override val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _slackStatus = MutableStateFlow<ConnectorSyncStatus>(ConnectorSyncStatus.Disconnected)
    override val slackStatus: StateFlow<ConnectorSyncStatus> = _slackStatus.asStateFlow()

    private val _slackConnected = MutableStateFlow(false)
    override val slackConnected: StateFlow<Boolean> = _slackConnected.asStateFlow()

    private val _slackChannel = MutableStateFlow("#general")
    override val slackChannel: StateFlow<String> = _slackChannel.asStateFlow()

    private val _slackWebhook = MutableStateFlow("")
    override val slackWebhook: StateFlow<String> = _slackWebhook.asStateFlow()

    init {
        scope.launch {
            val prefs = context.dataStore.data.first()
            
            val gConnected = prefs[GOOGLE_CONNECTED] ?: false
            _googleConnected.value = gConnected
            _googleAccount.value = prefs[GOOGLE_ACCOUNT] ?: "azharameen52@gmail.com"
            _googleStatus.value = if (gConnected) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected

            val ghConnected = prefs[GITHUB_CONNECTED] ?: false
            _githubConnected.value = ghConnected
            _githubUser.value = prefs[GITHUB_USER] ?: ""
            _githubToken.value = prefs[GITHUB_TOKEN] ?: ""
            _githubStatus.value = if (ghConnected) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected

            val sConnected = prefs[SLACK_CONNECTED] ?: false
            _slackConnected.value = sConnected
            _slackChannel.value = prefs[SLACK_CHANNEL] ?: "#general"
            _slackWebhook.value = prefs[SLACK_WEBHOOK] ?: ""
            _slackStatus.value = if (sConnected) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
        }
    }

    override fun setGoogleConnection(connected: Boolean, email: String) {
        _googleConnected.value = connected
        _googleAccount.value = email
        _googleStatus.value = if (connected) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected
        
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[GOOGLE_CONNECTED] = connected
                prefs[GOOGLE_ACCOUNT] = email
            }
        }
    }

    override fun updateGoogleStatus(status: ConnectorSyncStatus) {
        _googleStatus.value = status
        _googleConnected.value = status is ConnectorSyncStatus.Connected
    }

    override fun setGithubConnection(connected: Boolean, user: String, token: String) {
        _githubConnected.value = connected
        _githubUser.value = user
        _githubToken.value = token
        _githubStatus.value = if (connected && token.isNotBlank()) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected

        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[GITHUB_CONNECTED] = connected
                prefs[GITHUB_USER] = user
                prefs[GITHUB_TOKEN] = token
            }
        }
    }

    override fun updateGithubStatus(status: ConnectorSyncStatus) {
        _githubStatus.value = status
        _githubConnected.value = status is ConnectorSyncStatus.Connected
    }

    override fun setSlackConnection(connected: Boolean, channel: String, webhook: String) {
        _slackConnected.value = connected
        _slackChannel.value = channel
        _slackWebhook.value = webhook
        _slackStatus.value = if (connected && webhook.isNotBlank()) ConnectorSyncStatus.Connected else ConnectorSyncStatus.Disconnected

        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[SLACK_CONNECTED] = connected
                prefs[SLACK_CHANNEL] = channel
                prefs[SLACK_WEBHOOK] = webhook
            }
        }
    }

    override fun updateSlackStatus(status: ConnectorSyncStatus) {
        _slackStatus.value = status
        _slackConnected.value = status is ConnectorSyncStatus.Connected
    }
}
