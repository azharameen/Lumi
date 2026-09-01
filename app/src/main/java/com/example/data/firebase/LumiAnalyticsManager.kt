package com.example.data.firebase

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Manages Firebase Analytics tracking for companion interactions,
 * mindful wellness milestones, autonomous task goals, and user journeys.
 */
class LumiAnalyticsManager(
    private val context: Context
) {

    private val analytics: FirebaseAnalytics? by lazy {
        try {
            FirebaseAnalytics.getInstance(context).apply {
                setAnalyticsCollectionEnabled(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase Analytics is not available: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "LumiAnalytics"

        // Event Constants
        const val EVENT_PET_INTERACTION = "pet_interaction"
        const val EVENT_PET_LEVEL_UP = "pet_level_up"
        const val EVENT_GOAL_MILESTONE = "goal_milestone"
        const val EVENT_WELLNESS_SESSION = "wellness_session"
        const val EVENT_AI_CHAT_MESSAGE = "ai_chat_message"
        const val EVENT_SOUNDSCAPE_TOGGLE = "soundscape_toggle"
        const val EVENT_VAULT_ACTION = "vault_security_action"
        const val EVENT_REMOTE_CONFIG_SYNC = "remote_config_sync"
        const val EVENT_HABIT_COMPLETED = "habit_completed"
        const val EVENT_VOICE_COMMAND = "voice_command"
        const val EVENT_CAMERA_VISION_SCANNED = "camera_vision_scanned"
    }

    /**
     * Logs companion pet interactions (e.g. petting, feeding, sleeping, outfit changed).
     */
    fun logPetInteraction(action: String, petMood: String, happinessLevel: Int) {
        try {
            val bundle = Bundle().apply {
                putString("action_type", action)
                putString("pet_mood", petMood)
                putInt("happiness_level", happinessLevel)
            }
            analytics?.logEvent(EVENT_PET_INTERACTION, bundle)
            Log.d(TAG, "Logged pet interaction: $action ($petMood)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log pet interaction", e)
        }
    }

    /**
     * Logs companion evolution level up.
     */
    fun logPetLevelUp(oldLevel: Int, newLevel: Int, formName: String) {
        try {
            val bundle = Bundle().apply {
                putInt("old_level", oldLevel)
                putInt("new_level", newLevel)
                putString("evolution_form", formName)
            }
            analytics?.logEvent(EVENT_PET_LEVEL_UP, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log level up", e)
        }
    }

    /**
     * Logs ambient soundscape playback state changes.
     */
    fun logSoundscapeSession(soundscapeTitle: String, isPlaying: Boolean) {
        try {
            val bundle = Bundle().apply {
                putString("soundscape_title", soundscapeTitle)
                putBoolean("is_playing", isPlaying)
            }
            analytics?.logEvent(EVENT_SOUNDSCAPE_TOGGLE, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log soundscape event", e)
        }
    }

    /**
     * Logs Biometric Vault access / lock actions.
     */
    fun logVaultAction(action: String, isSuccess: Boolean) {
        try {
            val bundle = Bundle().apply {
                putString("action_type", action)
                putBoolean("is_success", isSuccess)
            }
            analytics?.logEvent(EVENT_VAULT_ACTION, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log vault action", e)
        }
    }

    /**
     * Logs Remote Config manual or real-time sync results.
     */
    fun logRemoteConfigSync(status: String) {
        try {
            val bundle = Bundle().apply {
                putString("sync_status", status)
            }
            analytics?.logEvent(EVENT_REMOTE_CONFIG_SYNC, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log remote config sync", e)
        }
    }

    /**
     * Logs completion of autonomous goals or tasks.
     */
    fun logGoalMilestone(goalTitle: String, category: String, isCompleted: Boolean) {
        try {
            val bundle = Bundle().apply {
                putString("goal_title", goalTitle.take(50))
                putString("category", category)
                putBoolean("is_completed", isCompleted)
            }
            analytics?.logEvent(EVENT_GOAL_MILESTONE, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log goal milestone", e)
        }
    }

    /**
     * Logs wellness activities (e.g. breathing exercises, hydration, posture checks).
     */
    fun logWellnessSession(exerciseType: String, durationSeconds: Int) {
        try {
            val bundle = Bundle().apply {
                putString("exercise_type", exerciseType)
                putInt("duration_seconds", durationSeconds)
            }
            analytics?.logEvent(EVENT_WELLNESS_SESSION, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log wellness session", e)
        }
    }

    /**
     * Logs AI chat prompts and model interactions.
     */
    fun logAiChatMessage(mode: String, messageLength: Int, modelUsed: String) {
        try {
            val bundle = Bundle().apply {
                putString("chat_mode", mode)
                putInt("message_length", messageLength)
                putString("model_used", modelUsed)
            }
            analytics?.logEvent(EVENT_AI_CHAT_MESSAGE, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log chat message", e)
        }
    }

    /**
     * Logs user screen navigations.
     */
    fun logScreenView(screenName: String, screenClass: String = "MainActivity") {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            }
            analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log screen view: $screenName", e)
        }
    }

    /**
     * Logs authentication events (Google Sign-In, Guest mode, Sign-out).
     */
    fun logAuthEvent(method: String, isNewUser: Boolean) {
        try {
            val eventName = if (isNewUser) FirebaseAnalytics.Event.SIGN_UP else FirebaseAnalytics.Event.LOGIN
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.METHOD, method)
            }
            analytics?.logEvent(eventName, bundle)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log auth event", e)
        }
    }

    /**
     * Sets custom user properties (e.g. companion level, preferred AI persona).
     */
    fun setUserProperty(name: String, value: String) {
        try {
            analytics?.setUserProperty(name, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set user property: $name", e)
        }
    }
}
