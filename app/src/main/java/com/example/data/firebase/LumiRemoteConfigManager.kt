package com.example.data.firebase

import android.util.Log
import com.example.domain.model.LumiRemoteConfig
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Manages Firebase Remote Config for real-time dynamic personalization,
 * companion prompt tuning, seasonal themes, and live announcements.
 */
class LumiRemoteConfigManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _config = MutableStateFlow(LumiRemoteConfig())
    val config: StateFlow<LumiRemoteConfig> = _config.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    private val _lastFetchTime = MutableStateFlow(System.currentTimeMillis())
    val lastFetchTime: StateFlow<Long> = _lastFetchTime.asStateFlow()

    private val _lastStatus = MutableStateFlow("Initialized with defaults")
    val lastStatus: StateFlow<String> = _lastStatus.asStateFlow()

    companion object {
        private const val TAG = "LumiRemoteConfig"

        // Remote Config Parameter Keys
        const val KEY_WELCOME_GREETING = "welcome_greeting"
        const val KEY_COMPANION_TIP = "companion_tip_of_the_day"
        const val KEY_SEASONAL_THEME_ENABLED = "seasonal_theme_enabled"
        const val KEY_SEASONAL_THEME_NAME = "seasonal_theme_name"
        const val KEY_AI_TEMPERATURE = "ai_creativity_temperature"
        const val KEY_PROACTIVE_NUDGE_HOURS = "proactive_nudge_interval_hours"
        const val KEY_ENABLE_VOICE_AMBIENT = "enable_voice_ambient_mode"
        const val KEY_SPECIAL_BANNER = "special_event_banner_text"
        const val KEY_DEFAULT_CLOUD_MODEL_NAME = "default_cloud_model_name"
        const val KEY_ENABLE_PET_ACCESSORIES = "enable_pet_accessories"
        const val KEY_ENABLE_LOCAL_GEMMA_FALLBACK = "enable_local_gemma_fallback"
        const val KEY_MAX_AGENT_EXECUTION_STEPS = "max_agent_execution_steps"
        const val KEY_HITL_DEFAULT_SECURITY_MODE = "hitl_default_security_mode"
        const val KEY_VECTOR_EMBEDDING_THRESHOLD = "vector_embedding_similarity_threshold"
        const val KEY_VOICE_SPEECH_RATE = "voice_speech_rate"
        const val KEY_VOICE_PITCH = "voice_pitch"
        const val KEY_ENABLE_ONBOARDING_MODEL_DOWNLOAD = "enable_onboarding_model_download"
        const val KEY_ENABLE_GEMINI_NANO_AICORE = "enable_gemini_nano_aicore"
        const val KEY_ENABLE_MCP_CLIENT = "enable_mcp_client"
        const val KEY_ENABLE_GOOGLE_WORKSPACE = "enable_google_workspace"
        const val KEY_ENABLE_SLACK_INTEGRATION = "enable_slack_integration"
        const val KEY_ENABLE_GITHUB_INTEGRATION = "enable_github_integration"
        const val KEY_ENABLE_PURCHASE_BUTTONS = "enable_purchase_buttons"
    }

    private val remoteConfigInstance: FirebaseRemoteConfig? by lazy {
        try {
            FirebaseRemoteConfig.getInstance().apply {
                val configSettings = FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(3600) // 1 hour cache in production
                    .build()
                setConfigSettingsAsync(configSettings)

                // In-App Defaults
                val defaults = mapOf<String, Any>(
                    KEY_WELCOME_GREETING to "Ready for a mindful, productive day with Lumi? ✨",
                    KEY_COMPANION_TIP to "Taking a 2-minute conscious breath reset revitalizes neural focus and balance.",
                    KEY_SEASONAL_THEME_ENABLED to false,
                    KEY_SEASONAL_THEME_NAME to "Obsidian Neon",
                    KEY_AI_TEMPERATURE to 0.75,
                    KEY_PROACTIVE_NUDGE_HOURS to 3,
                    KEY_ENABLE_VOICE_AMBIENT to true,
                    KEY_SPECIAL_BANNER to "",
                    KEY_DEFAULT_CLOUD_MODEL_NAME to "gemini-2.5-flash",
                    KEY_ENABLE_PET_ACCESSORIES to true,
                    KEY_ENABLE_LOCAL_GEMMA_FALLBACK to true,
                    KEY_MAX_AGENT_EXECUTION_STEPS to 10,
                    KEY_HITL_DEFAULT_SECURITY_MODE to "SMART_RISK",
                    KEY_VECTOR_EMBEDDING_THRESHOLD to 0.55,
                    KEY_VOICE_SPEECH_RATE to 1.02,
                    KEY_VOICE_PITCH to 1.15,
                    KEY_ENABLE_ONBOARDING_MODEL_DOWNLOAD to true,
                    KEY_ENABLE_GEMINI_NANO_AICORE to true,
                    KEY_ENABLE_MCP_CLIENT to false,
                    KEY_ENABLE_GOOGLE_WORKSPACE to false,
                    KEY_ENABLE_SLACK_INTEGRATION to false,
                    KEY_ENABLE_GITHUB_INTEGRATION to true,
                    KEY_ENABLE_PURCHASE_BUTTONS to false
                )
                setDefaultsAsync(defaults)

                // Attach real-time configuration update listener
                try {
                    addOnConfigUpdateListener(object : ConfigUpdateListener {
                        override fun onUpdate(configUpdate: ConfigUpdate) {
                            Log.i(TAG, "Real-time config update received. Keys updated: ${configUpdate.updatedKeys}")
                            scope.launch {
                                activate().awaitTask()
                                updateLocalConfig(this@apply)
                                _lastStatus.value = "Updated real-time at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
                            }
                        }

                        override fun onError(error: FirebaseRemoteConfigException) {
                            Log.w(TAG, "Config update listener error: ${error.message}")
                        }
                    })
                } catch (e: Exception) {
                    Log.w(TAG, "Could not attach Real-time ConfigUpdateListener", e)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Remote Config is not available: ${e.message}")
            null
        }
    }

    init {
        // Asynchronously fetch and activate latest values on startup
        scope.launch {
            fetchAndActivate()
        }
    }

    /**
     * Fetches and activates latest parameters from Firebase Remote Config.
     */
    suspend fun fetchAndActivate(): Boolean = withContext(Dispatchers.IO) {
        val rc = remoteConfigInstance ?: return@withContext false
        _isFetching.value = true
        try {
            val updated = rc.fetchAndActivate().awaitTask()
            updateLocalConfig(rc)
            _lastFetchTime.value = System.currentTimeMillis()
            _lastStatus.value = if (updated) "Fetched & Activated" else "Up to date"
            Log.d(TAG, "Remote Config updated successfully. Greeting: '${_config.value.welcomeGreeting}'")
            updated
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Remote Config", e)
            _lastStatus.value = "Fetch error: ${e.localizedMessage ?: "Offline"}"
            false
        } finally {
            _isFetching.value = false
        }
    }

    /**
     * Forcefully fetches latest values bypassing cache (0 seconds cache duration).
     */
    suspend fun forceRefreshConfig(): Result<LumiRemoteConfig> = withContext(Dispatchers.IO) {
        val rc = remoteConfigInstance ?: return@withContext Result.failure(Exception("Remote Config instance unavailable"))
        _isFetching.value = true
        try {
            // Temporarily set 0 cache interval for immediate fetch
            val devSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build()
            rc.setConfigSettingsAsync(devSettings).awaitTask()
            
            rc.fetchAndActivate().awaitTask()
            updateLocalConfig(rc)
            _lastFetchTime.value = System.currentTimeMillis()
            _lastStatus.value = "Force-synced at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"

            // Restore standard cache interval
            val prodSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build()
            rc.setConfigSettingsAsync(prodSettings)

            Result.success(_config.value)
        } catch (e: Exception) {
            Log.e(TAG, "Force refresh Remote Config failed", e)
            _lastStatus.value = "Sync error: ${e.localizedMessage ?: "Offline"}"
            Result.failure(e)
        } finally {
            _isFetching.value = false
        }
    }

    fun updateConfig(newConfig: LumiRemoteConfig) {
        _config.value = newConfig
    }

    private fun updateLocalConfig(rc: FirebaseRemoteConfig) {
        val newConfig = LumiRemoteConfig(
            welcomeGreeting = rc.getString(KEY_WELCOME_GREETING).ifBlank { _config.value.welcomeGreeting },
            companionTipOfTheDay = rc.getString(KEY_COMPANION_TIP).ifBlank { _config.value.companionTipOfTheDay },
            seasonalThemeEnabled = rc.getBoolean(KEY_SEASONAL_THEME_ENABLED),
            seasonalThemeName = rc.getString(KEY_SEASONAL_THEME_NAME).ifBlank { "Obsidian Neon" },
            aiCreativityTemperature = rc.getDouble(KEY_AI_TEMPERATURE).toFloat().coerceIn(0.1f, 1.2f),
            proactiveNudgeIntervalHours = rc.getLong(KEY_PROACTIVE_NUDGE_HOURS).toInt().coerceIn(1, 24),
            enableVoiceAmbientMode = rc.getBoolean(KEY_ENABLE_VOICE_AMBIENT),
            specialEventBannerText = rc.getString(KEY_SPECIAL_BANNER),
            defaultCloudModelName = rc.getString(KEY_DEFAULT_CLOUD_MODEL_NAME).ifBlank { "gemini-2.5-flash" },
            enablePetAccessories = rc.getBoolean(KEY_ENABLE_PET_ACCESSORIES),
            enableLocalGemmaFallback = rc.getBoolean(KEY_ENABLE_LOCAL_GEMMA_FALLBACK),
            maxAgentExecutionSteps = rc.getLong(KEY_MAX_AGENT_EXECUTION_STEPS).toInt().coerceIn(1, 50),
            hitlDefaultSecurityMode = rc.getString(KEY_HITL_DEFAULT_SECURITY_MODE).ifBlank { "SMART_RISK" },
            vectorEmbeddingSimilarityThreshold = rc.getDouble(KEY_VECTOR_EMBEDDING_THRESHOLD).toFloat().coerceIn(0.1f, 0.99f),
            voiceSpeechRate = rc.getDouble(KEY_VOICE_SPEECH_RATE).toFloat().coerceIn(0.5f, 2.0f),
            voicePitch = rc.getDouble(KEY_VOICE_PITCH).toFloat().coerceIn(0.5f, 2.0f),
            enableOnboardingModelDownload = rc.getBoolean(KEY_ENABLE_ONBOARDING_MODEL_DOWNLOAD),
            enableGeminiNanoAiCore = rc.getBoolean(KEY_ENABLE_GEMINI_NANO_AICORE),
            enableMcpClient = rc.getBoolean(KEY_ENABLE_MCP_CLIENT),
            enableGoogleWorkspace = rc.getBoolean(KEY_ENABLE_GOOGLE_WORKSPACE),
            enableSlackIntegration = rc.getBoolean(KEY_ENABLE_SLACK_INTEGRATION),
            enableGithubIntegration = rc.getBoolean(KEY_ENABLE_GITHUB_INTEGRATION),
            enablePurchaseButtons = rc.getBoolean(KEY_ENABLE_PURCHASE_BUTTONS)
        )
        _config.value = newConfig
    }

    /**
     * Helper extension to await Google Play Tasks in coroutines.
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
