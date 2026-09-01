package com.example.data.firebase

import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.AppCheckToken
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Enterprise Firebase App Check Manager.
 * 
 * Protects Firebase backend services (Firestore, Auth, Storage, Realtime Database, AI)
 * by verifying that requests originate exclusively from authentic, untampered app instances
 * via Google Play Integrity API.
 */
class LumiAppCheckManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _providerName = MutableStateFlow("Uninitialized")
    val providerName: StateFlow<String> = _providerName.asStateFlow()

    private val _latestToken = MutableStateFlow<String?>(null)
    val latestToken: StateFlow<String?> = _latestToken.asStateFlow()

    private val _tokenExpiryMillis = MutableStateFlow<Long?>(null)
    val tokenExpiryMillis: StateFlow<Long?> = _tokenExpiryMillis.asStateFlow()

    private val _statusMessage = MutableStateFlow("App Check not yet initialized")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    companion object {
        private const val TAG = "LumiAppCheck"

        @Volatile
        private var instance: LumiAppCheckManager? = null

        fun getInstance(): LumiAppCheckManager {
            return instance ?: synchronized(this) {
                instance ?: LumiAppCheckManager().also { instance = it }
            }
        }
    }

    /**
     * Initializes Firebase App Check with Play Integrity provider.
     * In debug environments, supports fallback to DebugAppCheckProviderFactory
     * or PlayIntegrity provider.
     */
    fun initialize(useDebugProviderInDebug: Boolean = false) {
        try {
            if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isEmpty()) {
                Log.w(TAG, "FirebaseApp is not initialized yet. Skipping App Check setup.")
                _statusMessage.value = "Skipped: FirebaseApp not ready"
                return
            }

            val firebaseAppCheck = FirebaseAppCheck.getInstance()

            val isEmulator = (android.os.Build.BRAND.startsWith("generic") && android.os.Build.DEVICE.startsWith("generic"))
                    || android.os.Build.FINGERPRINT.startsWith("generic")
                    || android.os.Build.FINGERPRINT.startsWith("unknown")
                    || android.os.Build.HARDWARE.contains("goldfish")
                    || android.os.Build.HARDWARE.contains("ranchu")
                    || android.os.Build.HARDWARE.contains("cutf_cvm")
                    || android.os.Build.MODEL.contains("google_sdk")
                    || android.os.Build.MODEL.contains("Emulator")
                    || android.os.Build.MODEL.contains("Android SDK built for x86")
                    || android.os.Build.MANUFACTURER.contains("Genymotion")
                    || android.os.Build.PRODUCT.contains("sdk_google")
                    || android.os.Build.PRODUCT.contains("google_sdk")
                    || android.os.Build.PRODUCT.contains("sdk")
                    || android.os.Build.PRODUCT.contains("sdk_x86")
                    || android.os.Build.PRODUCT.contains("vbox86p")
                    || android.os.Build.PRODUCT.contains("emulator")
                    || android.os.Build.PRODUCT.contains("simulator")
            
            if (BuildConfig.DEBUG && isEmulator) {
                Log.d(TAG, "Running on emulator. Using statically defined DebugAppCheckProviderFactory token.")
            }

            val providerFactory = if (BuildConfig.DEBUG) {
                _providerName.value = "Debug Provider"
                Log.d(TAG, "Installing DebugAppCheckProviderFactory for debug build")
                DebugAppCheckProviderFactory.getInstance()
            } else {
                _providerName.value = "Play Integrity"
                Log.i(TAG, "Installing PlayIntegrityAppCheckProviderFactory for authentic app attestation")
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }

            firebaseAppCheck.installAppCheckProviderFactory(providerFactory)
            firebaseAppCheck.setTokenAutoRefreshEnabled(true)

            // Register listener for token changes
            firebaseAppCheck.addAppCheckListener { appCheckToken: AppCheckToken ->
                _latestToken.value = appCheckToken.token
                _tokenExpiryMillis.value = appCheckToken.expireTimeMillis
                _statusMessage.value = "Active (Expires in ${(appCheckToken.expireTimeMillis - System.currentTimeMillis()) / 1000}s)"
                Log.d(TAG, "App Check token refreshed successfully. Valid until: ${appCheckToken.expireTimeMillis}")
            }

            _isInitialized.value = true
            _statusMessage.value = "Initialized with ${_providerName.value}"
            Log.i(TAG, "Firebase App Check successfully initialized with ${_providerName.value}")

            // Pre-fetch initial token asynchronously
            fetchTokenAsync()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase App Check", e)
            _statusMessage.value = "Initialization error: ${e.localizedMessage}"
        }
    }

    /**
     * Retrieves the current App Check token, with optional forced refresh.
     */
    suspend fun getAppCheckToken(forceRefresh: Boolean = false): String? {
        return try {
            val appCheck = FirebaseAppCheck.getInstance()
            val tokenResult = appCheck.getAppCheckToken(forceRefresh).await()
            _latestToken.value = tokenResult.token
            _tokenExpiryMillis.value = tokenResult.expireTimeMillis
            _statusMessage.value = "Token retrieved (valid for ${(tokenResult.expireTimeMillis - System.currentTimeMillis()) / 1000}s)"
            tokenResult.token
        } catch (e: Exception) {
            Log.e(TAG, "Error obtaining App Check token", e)
            _statusMessage.value = "Token fetch failed: ${e.localizedMessage}"
            null
        }
    }

    private fun fetchTokenAsync() {
        scope.launch {
            getAppCheckToken(forceRefresh = false)
        }
    }
}
