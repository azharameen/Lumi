package com.example.data.firebase

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Manages Firebase Crashlytics crash reporting, custom breadcrumb logs,
 * user identification, and non-fatal exception telemetry.
 */
class LumiCrashlyticsManager {

    private val crashlytics: FirebaseCrashlytics? by lazy {
        try {
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Crashlytics is not available: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "LumiCrashlytics"
    }

    /**
     * Sets the authenticated User ID for crash correlation.
     */
    fun setUserId(userId: String?) {
        try {
            if (userId != null) {
                crashlytics?.setUserId(userId)
            } else {
                crashlytics?.setUserId("")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set Crashlytics user ID", e)
        }
    }

    /**
     * Sets a custom string key-value attribute in crash reports.
     */
    fun setCustomKey(key: String, value: String) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: $key", e)
        }
    }

    /**
     * Sets a custom boolean key-value attribute in crash reports.
     */
    fun setCustomKey(key: String, value: Boolean) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: $key", e)
        }
    }

    /**
     * Sets a custom integer key-value attribute in crash reports.
     */
    fun setCustomKey(key: String, value: Int) {
        try {
            crashlytics?.setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key: $key", e)
        }
    }

    /**
     * Sets companion context attributes in Crashlytics reports.
     */
    fun setCompanionState(happinessLevel: Int, evolutionLevel: Int, mood: String) {
        try {
            crashlytics?.setCustomKey("companion_happiness", happinessLevel)
            crashlytics?.setCustomKey("companion_evolution_level", evolutionLevel)
            crashlytics?.setCustomKey("companion_mood", mood)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set companion state in Crashlytics", e)
        }
    }

    /**
     * Records a diagnostic breadcrumb log with a specific tag to attach to future crash reports.
     */
    fun logBreadcrumb(tag: String, message: String) {
        try {
            val formatted = "[$tag] $message"
            Log.d(TAG, formatted)
            crashlytics?.log(formatted)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log tagged breadcrumb", e)
        }
    }

    /**
     * Records a diagnostic breadcrumb log to attach to future crash reports.
     */
    fun log(message: String) {
        try {
            Log.d(TAG, message)
            crashlytics?.log(message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log breadcrumb", e)
        }
    }

    /**
     * Records a non-fatal exception to the Crashlytics dashboard.
     */
    fun recordException(throwable: Throwable, contextTag: String? = null) {
        try {
            if (contextTag != null) {
                crashlytics?.setCustomKey("last_error_tag", contextTag)
            }
            crashlytics?.recordException(throwable)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to record non-fatal exception", e)
        }
    }
}
