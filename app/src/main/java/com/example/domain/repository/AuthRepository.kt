package com.example.domain.repository


import com.example.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow

/**
 * Enterprise Authentication & Identity Repository contract.
 * Manages Firebase Auth, Google Credential Manager identity flows, and Cloud Firestore user sync.
 */
interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>

    /**
     * Initiates Google Sign-In via Android Credential Manager and exchanges ID token with Firebase Auth.
     */
    suspend fun signInWithGoogle(context: Any): Result<AuthUser>

    /**
     * Signs out the current Firebase user and clears active credentials.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Retrieves the currently cached Firebase user profile.
     */
    suspend fun getCurrentUser(): AuthUser?

    /**
     * Synchronizes user metadata with Cloud Firestore:
     * - If user document does not exist: creates a new user profile document in "users" collection.
     * - If user already exists: updates last login timestamp without overwriting data.
     * Returns true if a new user was registered, false if an existing user logged in.
     */
    suspend fun syncUserWithFirestore(user: AuthUser): Result<Boolean>

    /**
     * Returns the SHA-1 Certificate Fingerprint for Firebase Console configuration.
     */
    fun getSha1Fingerprint(): String

    /**
     * Returns the SHA-256 Certificate Fingerprint for Firebase Console configuration.
     */
    fun getSha256Fingerprint(): String

    /**
     * Returns the Web Client ID used for Google Sign-In.
     */
    fun getWebClientId(): String
}
