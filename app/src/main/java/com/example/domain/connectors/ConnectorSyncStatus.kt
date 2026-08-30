package com.example.domain.connectors

/**
 * Standard decoupled status contract representing external service sync states.
 */
sealed interface ConnectorSyncStatus {
    object Connected : ConnectorSyncStatus
    object Disconnected : ConnectorSyncStatus
    data class SyncFailed(val reason: String) : ConnectorSyncStatus
    data class Unauthorized(val message: String = "Authentication required or token expired") : ConnectorSyncStatus

    val isConnected: Boolean
        get() = this is Connected

    val displayText: String
        get() = when (this) {
            is Connected -> "Connected"
            is Disconnected -> "Disconnected"
            is SyncFailed -> "Sync Failed: $reason"
            is Unauthorized -> "Unauthorized"
        }
}
