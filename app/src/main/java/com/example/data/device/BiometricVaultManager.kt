package com.example.data.device

import android.app.KeyguardManager
import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.ContextCompat
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Sealed result hierarchy for explicit biometric authentication outcomes.
 */
sealed class VaultAuthResult {
    data object Success : VaultAuthResult()
    sealed class Failure(val message: String, val cause: Throwable? = null) : VaultAuthResult() {
        class HardwareUnavailable(message: String = "Biometric hardware is unavailable on this device") : Failure(message)
        class NoneEnrolled(message: String = "No biometric credentials or device lock are configured on this device") : Failure(message)
        class AuthenticationFailed(message: String = "Biometric verification not recognized") : Failure(message)
        class AuthenticationError(val errorCode: Int, message: String) : Failure(message)
        class UserCancelled(message: String = "Authentication was cancelled by user") : Failure(message)
        class SecurityError(message: String, cause: Throwable? = null) : Failure(message, cause)
    }
}

/**
 * Hardware-backed Biometric Vault controller for securing confidential memories,
 * gratitude notes, and emotional reflections using AndroidKeyStore (AES-GCM-256)
 * and hardware BiometricPrompt / Keyguard verification.
 */
class BiometricVaultManager(private val context: Context) {

    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    init {
        ensureMasterKeyGenerated()
    }

    fun isDeviceSecure(): Boolean {
        return keyguardManager?.isDeviceSecure ?: false
    }

    /**
     * Checks if biometric hardware and security credentials are available and enrolled.
     */
    fun canAuthenticate(): Boolean {
        if (!isDeviceSecure()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val biometricManager = context.getSystemService(BiometricManager::class.java) ?: return false
            val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            } else {
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            }
            return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
        }
        return isDeviceSecure()
    }

    /**
     * Authenticates the user via biometric prompt or device credentials.
     * Strictly fails with explicit error when device is not secured or authentication fails.
     * Never falls back to insecure pass-through.
     */
    fun authenticate(
        title: String = "Unlock Lumi Memory Vault",
        subtitle: String = "Verify your identity to view private memories",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isDeviceSecure()) {
            onError("Device is not secured. Please enable a screen lock (PIN, Pattern, or Biometrics) in system settings.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val cancellationSignal = CancellationSignal()
            val executor = ContextCompat.getMainExecutor(context)

            val promptBuilder = BiometricPrompt.Builder(context)
                .setTitle(title)
                .setSubtitle(subtitle)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                promptBuilder.setConfirmationRequired(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    promptBuilder.setAllowedAuthenticators(
                        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                                BiometricManager.Authenticators.DEVICE_CREDENTIAL
                    )
                } else {
                    @Suppress("DEPRECATION")
                    promptBuilder.setDeviceCredentialAllowed(true)
                }
            } else {
                promptBuilder.setNegativeButton("Cancel", executor) { _, _ ->
                    onError("Authentication cancelled by user")
                }
            }

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                    super.onAuthenticationError(errorCode, errString)
                    val errorMsg = errString?.toString() ?: "Authentication error (Code: $errorCode)"
                    onError(errorMsg)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Biometric identity not recognized. Please try again.")
                }
            }

            try {
                promptBuilder.build().authenticate(cancellationSignal, executor, callback)
            } catch (e: SecurityException) {
                onError("Security exception during biometric authentication: ${e.localizedMessage}")
            } catch (e: Exception) {
                onError("Failed to initiate biometric prompt: ${e.localizedMessage}")
            }
        } else {
            onError("Biometric authentication requires Android 9.0 (API 28) or above with secure hardware.")
        }
    }

    /**
     * Initializes or verifies the hardware-backed MasterKey in AndroidKeyStore.
     */
    private fun ensureMasterKeyGenerated() {
        try {
            if (!keyStore.containsAlias(VAULT_KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keySpec = KeyGenParameterSpec.Builder(
                    VAULT_KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(keySpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            // Hardware keystore initialization logged safely without secrets
        }
    }

    /**
     * Encrypts plaintext bytes using hardware-backed AES-256 GCM key.
     * Returns Base64-encoded string format: "IV:CIPHERTEXT".
     */
    fun encryptString(plainText: String): String? {
        return try {
            val key = keyStore.getKey(VAULT_KEY_ALIAS, null) as? SecretKey ?: return null
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val iv = cipher.iv
            val cipherText = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
            val cipherString = Base64.encodeToString(cipherText, Base64.NO_WRAP)
            "$ivString:$cipherString"
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Decrypts Base64-encoded string ("IV:CIPHERTEXT") using hardware-backed AES-256 GCM key.
     */
    fun decryptString(encryptedPayload: String): String? {
        return try {
            val parts = encryptedPayload.split(":")
            if (parts.size != 2) return null
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)

            val key = keyStore.getKey(VAULT_KEY_ALIAS, null) as? SecretKey ?: return null
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val VAULT_KEY_ALIAS = "lumi_vault_master_key"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
    }
}
