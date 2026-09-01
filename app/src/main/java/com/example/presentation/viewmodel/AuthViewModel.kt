package com.example.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.repository.AuthCancellationException
import com.example.domain.account.UserProfileManager
import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: AuthUser? = null,
    val isLoading: Boolean = false,
    val loadingMessage: String? = null,
    val error: String? = null,
    val isNewUserCreated: Boolean? = null,
    val isGuestMode: Boolean = false,
    val isFirestoreSynced: Boolean = false
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userProfileManager: UserProfileManager,
    private val crashlytics: LumiCrashlyticsManager? = null,
    private val analytics: LumiAnalyticsManager? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val sha1: String = authRepository.getSha1Fingerprint()
    val sha256: String = authRepository.getSha256Fingerprint()
    val webClientId: String = authRepository.getWebClientId()

    init {
        // Observe repository user changes
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        user = user,
                        isGuestMode = if (user != null) false else current.isGuestMode
                    )
                }
                // Sync profile data and update Crashlytics user ID
                if (user != null) {
                    crashlytics?.setUserId(user.uid)
                    crashlytics?.setCustomKey("user_email", user.email ?: "")
                    crashlytics?.setCustomKey("is_guest", false)
                    userProfileManager.updateField { profile ->
                        profile.copy(
                            userName = user.displayName ?: profile.userName,
                            hasCompletedOnboarding = true
                        )
                    }
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        _uiState.update {
            it.copy(
                isLoading = true,
                loadingMessage = "Connecting with Google Credential Manager...",
                error = null
            )
        }

        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadingMessage = null,
                        user = user,
                        isNewUserCreated = user.isNewUser,
                        isFirestoreSynced = true,
                        error = null
                    )
                }
                crashlytics?.setUserId(user.uid)
                crashlytics?.log("Google Sign-In successful for uid=${user.uid}")
                analytics?.logAuthEvent(method = "google", isNewUser = user.isNewUser)

                userProfileManager.updateField { profile ->
                    profile.copy(
                        userName = user.displayName ?: profile.userName,
                        hasCompletedOnboarding = true
                    )
                }
            }.onFailure { throwable ->
                if (throwable is AuthCancellationException) {
                    // User closed dialog, silently reset loading
                    _uiState.update {
                        it.copy(isLoading = false, loadingMessage = null, error = null)
                    }
                } else {
                    crashlytics?.recordException(throwable, "GoogleSignInFailure")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadingMessage = null,
                            error = throwable.localizedMessage ?: "Authentication failed. Please try again."
                        )
                    }
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Signing out...") }
            authRepository.signOut()
            crashlytics?.setUserId(null)
            crashlytics?.setCustomKey("is_guest", false)
            analytics?.logAuthEvent(method = "sign_out", isNewUser = false)
            _uiState.update {
                it.copy(
                    user = null,
                    isLoading = false,
                    loadingMessage = null,
                    isGuestMode = false,
                    error = null
                )
            }
        }
    }

    fun continueAsGuest() {
        crashlytics?.setUserId("guest_${System.currentTimeMillis()}")
        crashlytics?.setCustomKey("is_guest", true)
        analytics?.logAuthEvent(method = "guest", isNewUser = false)
        _uiState.update { it.copy(isGuestMode = true, error = null) }
        userProfileManager.updateField { it.copy(hasCompletedOnboarding = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
