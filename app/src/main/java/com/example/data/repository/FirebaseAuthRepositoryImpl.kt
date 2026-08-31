package com.example.data.repository

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.domain.model.AuthUser
import com.example.domain.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    companion object {
        private const val TAG = "FirebaseAuthRepo"
        const val WEB_CLIENT_ID = "663377968514-tgfegglsv6r62ojpcp0lhmvhuis384gq.apps.googleusercontent.com"
        const val SHA1_FINGERPRINT = "26:78:5C:06:2E:B4:25:52:22:E6:DE:55:EC:45:35:FF:A7:F9:9C:7B"
        const val SHA256_FINGERPRINT = "18:E3:72:DC:DE:9A:50:91:A8:06:05:1C:D5:A6:91:DC:AB:33:3A:43:98:F7:EF:17:5F:DC:C1:05:F7:B2:8E:DA"
    }

    init {
        // Observe Firebase Auth state changes
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            _currentUser.value = user?.toDomainModel()
            Log.d(TAG, "Auth state updated: uid=${user?.uid}, email=${user?.email}")
        }
    }

    override fun getSha1Fingerprint(): String = SHA1_FINGERPRINT
    override fun getSha256Fingerprint(): String = SHA256_FINGERPRINT
    override fun getWebClientId(): String = WEB_CLIENT_ID

    override suspend fun getCurrentUser(): AuthUser? {
        return firebaseAuth.currentUser?.toDomainModel()
    }

    override suspend fun signInWithGoogle(context: Context): Result<AuthUser> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = try {
                credentialManager.getCredential(
                    request = request,
                    context = context
                )
            } catch (e: GetCredentialCancellationException) {
                Log.i(TAG, "User dismissed Google Sign-In sheet")
                return@withContext Result.failure(AuthCancellationException("Sign-in cancelled by user."))
            } catch (e: NoCredentialException) {
                Log.w(TAG, "No Google accounts available on device: ${e.message}")
                return@withContext Result.failure(NoAccountException("No Google account found on device. Please add a Google account in system settings."))
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager error: ${e.type} -> ${e.message}", e)
                return@withContext Result.failure(AuthGeneralException("Google Sign-In failed: ${e.message ?: "Authentication error"}"))
            }

            val credential = result.credential
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val idToken = googleIdTokenCredential.idToken

            // Exchange Google ID Token with Firebase Auth
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(authCredential).awaitTask()

            val firebaseUser = authResult.user
                ?: return@withContext Result.failure(AuthGeneralException("Firebase user is null after authentication."))

            val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
            val domainUser = firebaseUser.toDomainModel(isNewUser = isNewUser)
            _currentUser.value = domainUser

            // Sync user metadata with Firestore
            syncUserWithFirestore(domainUser)

            Result.success(domainUser)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during signInWithGoogle", e)
            Result.failure(e)
        }
    }

    override suspend fun syncUserWithFirestore(user: AuthUser): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val userDocRef = firestore.collection("users").document(user.uid)
            val snapshot = userDocRef.get().awaitTask()

            if (!snapshot.exists()) {
                // New User: Create initial Firestore document entry
                val newUserData = hashMapOf<String, Any?>(
                    "uid" to user.uid,
                    "email" to (user.email ?: ""),
                    "displayName" to (user.displayName ?: "Lumi Explorer"),
                    "photoUrl" to (user.photoUrl ?: ""),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastLoginAt" to FieldValue.serverTimestamp(),
                    "appId" to "com.iywa.app",
                    "role" to "user",
                    "status" to "active"
                )
                userDocRef.set(newUserData).awaitTask()
                Log.i(TAG, "Created new Firestore user entry for uid=${user.uid}")
                Result.success(true)
            } else {
                // Existing User: Update only last login timestamp without overwriting data
                userDocRef.update("lastLoginAt", FieldValue.serverTimestamp()).awaitTask()
                Log.i(TAG, "Updated lastLoginAt for existing user uid=${user.uid}")
                Result.success(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync user with Firestore for uid=${user.uid}", e)
            // Still return success false or failure so auth flow is not blocked if Firestore is offline
            Result.failure(e)
        }
    }

    override suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firebaseAuth.signOut()
            _currentUser.value = null
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error signing out", e)
            Result.failure(e)
        }
    }

    private fun FirebaseUser.toDomainModel(isNewUser: Boolean = false): AuthUser {
        return AuthUser(
            uid = uid,
            email = email,
            displayName = displayName,
            photoUrl = photoUrl?.toString(),
            isNewUser = isNewUser,
            isAnonymous = isAnonymous,
            createdAt = metadata?.creationTimestamp ?: System.currentTimeMillis(),
            lastLoginAt = metadata?.lastSignInTimestamp ?: System.currentTimeMillis()
        )
    }

    /**
     * Coroutine extension to safely await Play Services Task with cancellation support.
     */
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { result ->
                if (cont.isActive) cont.resume(result)
            }
            addOnFailureListener { exception ->
                if (cont.isActive) cont.resumeWithException(exception)
            }
            addOnCanceledListener {
                if (cont.isActive) cont.cancel()
            }
        }
}

class AuthCancellationException(message: String) : Exception(message)
class NoAccountException(message: String) : Exception(message)
class AuthGeneralException(message: String) : Exception(message)
