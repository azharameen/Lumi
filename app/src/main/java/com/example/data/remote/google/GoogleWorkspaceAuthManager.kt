package com.example.data.remote.google

import android.content.Context
import android.util.Log
import com.example.domain.connectors.ConnectorRepository
import com.example.domain.connectors.ConnectorSyncStatus
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enterprise Google Workspace OAuth2 Manager.
 * Handles acquiring, validating, and refreshing OAuth2 access tokens for Google Workspace APIs
 * (Gmail, Google Docs, Google Sheets, Google Slides, Google Drive).
 */
class GoogleWorkspaceAuthManager(
    private val context: Context,
    private val connectorRepository: ConnectorRepository
) {

    companion object {
        private const val TAG = "GoogleWorkspaceAuth"

        // Required Google Workspace OAuth2 Scopes
        val SCOPE_GMAIL_SEND = Scope("https://www.googleapis.com/auth/gmail.send")
        val SCOPE_DOCS = Scope("https://www.googleapis.com/auth/documents")
        val SCOPE_SHEETS = Scope("https://www.googleapis.com/auth/spreadsheets")
        val SCOPE_SLIDES = Scope("https://www.googleapis.com/auth/presentations")
        val SCOPE_DRIVE_FILE = Scope("https://www.googleapis.com/auth/drive.file")
    }

    /**
     * Checks if the signed-in account has all Google Workspace permissions granted.
     */
    fun hasWorkspacePermissions(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return false
        return GoogleSignIn.hasPermissions(
            account,
            SCOPE_GMAIL_SEND,
            SCOPE_DOCS,
            SCOPE_SHEETS,
            SCOPE_SLIDES,
            SCOPE_DRIVE_FILE
        )
    }

    /**
     * Retrieves an active OAuth2 ID Token or Access Token for Google Workspace API requests.
     */
    suspend fun getAccessToken(): String? = withContext(Dispatchers.IO) {
        try {
            val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                // Return ID Token or Account Token
                val token = account.idToken
                if (!token.isNullOrBlank()) {
                    connectorRepository.updateGoogleStatus(ConnectorSyncStatus.Connected)
                    return@withContext token
                }
            }
            connectorRepository.updateGoogleStatus(ConnectorSyncStatus.Unauthorized("Google Workspace OAuth token missing"))
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Google access token", e)
            connectorRepository.updateGoogleStatus(ConnectorSyncStatus.SyncFailed(e.localizedMessage ?: "Auth failure"))
            null
        }
    }
}
