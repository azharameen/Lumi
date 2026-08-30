package com.example.domain.account

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class UserProfileManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: UserProfileManager? = null

        fun getInstance(context: Context): UserProfileManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserProfileManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences("lumi_user_profile_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val factListType = Types.newParameterizedType(List::class.java, UserFactItem::class.java)
    private val factAdapter = moshi.adapter<List<UserFactItem>>(factListType)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfileData> = _userProfile.asStateFlow()

    private val _userFacts = MutableStateFlow(loadFacts())
    val userFacts: StateFlow<List<UserFactItem>> = _userFacts.asStateFlow()

    private fun loadProfile(): UserProfileData {
        val toneName = prefs.getString("persona_tone", LumiPersonaTone.EMPATHETIC_CHEERFUL.name)
        val tone = try {
            LumiPersonaTone.valueOf(toneName ?: LumiPersonaTone.EMPATHETIC_CHEERFUL.name)
        } catch (e: Exception) {
            LumiPersonaTone.EMPATHETIC_CHEERFUL
        }

        return UserProfileData(
            userName = prefs.getString("user_name", "Azhar Ameen") ?: "Azhar Ameen",
            userEmail = prefs.getString("user_email", "azharameen52@gmail.com") ?: "azharameen52@gmail.com",
            roleOrTitle = prefs.getString("role_title", "Software Engineer & AI Architect") ?: "Software Engineer & AI Architect",
            primaryFocusGoal = prefs.getString("primary_goal", "Deep Flow, Clean Code & Mindful Living") ?: "Deep Flow, Clean Code & Mindful Living",
            dailyFocusTargetHours = prefs.getFloat("daily_focus_target_hours", 4.0f),
            targetHydrationCups = prefs.getInt("target_hydration_cups", 8),
            targetDailySteps = prefs.getInt("target_daily_steps", 8000),
            wakeUpTime = prefs.getString("wake_up_time", "07:30 AM") ?: "07:30 AM",
            sleepTime = prefs.getString("sleep_time", "11:30 PM") ?: "11:30 PM",
            personaTone = tone,
            customAiInstructions = prefs.getString("custom_ai_instructions", "Keep answers concise, actionable, and formatted in clean markdown bullet points.") ?: "",
            geminiModelChoice = prefs.getString("gemini_model_choice", "gemini-2.5-flash") ?: "gemini-2.5-flash",
            temperature = prefs.getFloat("temperature", 0.7f),
            enableProactiveBriefings = prefs.getBoolean("enable_proactive_briefings", true),
            enableToolCalling = prefs.getBoolean("enable_tool_calling", true),
            enableBiometricLock = prefs.getBoolean("enable_biometric_lock", false),
            enableSpeechOutput = prefs.getBoolean("enable_speech_output", true),
            enableHapticFeedback = prefs.getBoolean("enable_haptic_feedback", true),
            enableAmbientLocation = prefs.getBoolean("enable_ambient_location", true),
            enableLocalAiFallback = prefs.getBoolean("enable_local_ai_fallback", true),
            enableOverlay = prefs.getBoolean("enable_overlay", true),
            hasCompletedOnboarding = prefs.getBoolean("has_completed_onboarding", false)
        )
    }

    private fun loadFacts(): List<UserFactItem> {
        val json = prefs.getString("user_facts_json", null)
        if (!json.isNullOrBlank()) {
            try {
                val parsed = factAdapter.fromJson(json)
                if (!parsed.isNullOrEmpty()) {
                    return parsed
                }
            } catch (e: Exception) {
                // fallback to defaults
            }
        }

        // Default initial profile facts for Lumi context
        return listOf(
            UserFactItem(
                id = UUID.randomUUID().toString(),
                category = "Work & Code",
                factText = "Prefers Kotlin and modern Jetpack Compose architecture with clean separation of concerns.",
                isPinned = true
            ),
            UserFactItem(
                id = UUID.randomUUID().toString(),
                category = "Preferences",
                factText = "Likes structured bullet points, concise step-by-step summaries, and direct actionable advice.",
                isPinned = true
            ),
            UserFactItem(
                id = UUID.randomUUID().toString(),
                category = "Health & Routines",
                factText = "Aims for 8 cups of water daily and a 15-minute box breathing session during afternoon slump.",
                isPinned = false
            ),
            UserFactItem(
                id = UUID.randomUUID().toString(),
                category = "Routines",
                factText = "Deep work focus block scheduled every morning between 9:00 AM and 11:30 AM.",
                isPinned = false
            )
        )
    }

    fun updateProfile(profile: UserProfileData) {
        prefs.edit()
            .putString("user_name", profile.userName)
            .putString("user_email", profile.userEmail)
            .putString("role_title", profile.roleOrTitle)
            .putString("primary_goal", profile.primaryFocusGoal)
            .putFloat("daily_focus_target_hours", profile.dailyFocusTargetHours)
            .putInt("target_hydration_cups", profile.targetHydrationCups)
            .putInt("target_daily_steps", profile.targetDailySteps)
            .putString("wake_up_time", profile.wakeUpTime)
            .putString("sleep_time", profile.sleepTime)
            .putString("persona_tone", profile.personaTone.name)
            .putString("custom_ai_instructions", profile.customAiInstructions)
            .putString("gemini_model_choice", profile.geminiModelChoice)
            .putFloat("temperature", profile.temperature)
            .putBoolean("enable_proactive_briefings", profile.enableProactiveBriefings)
            .putBoolean("enable_tool_calling", profile.enableToolCalling)
            .putBoolean("enable_biometric_lock", profile.enableBiometricLock)
            .putBoolean("enable_speech_output", profile.enableSpeechOutput)
            .putBoolean("enable_haptic_feedback", profile.enableHapticFeedback)
            .putBoolean("enable_ambient_location", profile.enableAmbientLocation)
            .putBoolean("enable_local_ai_fallback", profile.enableLocalAiFallback)
            .putBoolean("enable_overlay", profile.enableOverlay)
            .putBoolean("has_completed_onboarding", profile.hasCompletedOnboarding)
            .apply()

        _userProfile.value = profile
    }

    fun updateField(block: (UserProfileData) -> UserProfileData) {
        val updated = block(_userProfile.value)
        updateProfile(updated)
    }

    fun addUserFact(category: String, factText: String, isPinned: Boolean = false) {
        if (factText.isBlank()) return
        val newFact = UserFactItem(
            id = UUID.randomUUID().toString(),
            category = category.ifBlank { "General" },
            factText = factText.trim(),
            isPinned = isPinned
        )
        val updatedList = listOf(newFact) + _userFacts.value
        saveFacts(updatedList)
    }

    fun removeUserFact(id: String) {
        val updatedList = _userFacts.value.filter { it.id != id }
        saveFacts(updatedList)
    }

    fun togglePinFact(id: String) {
        val updatedList = _userFacts.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveFacts(updatedList)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _userProfile.value = loadProfile()
        _userFacts.value = loadFacts()
    }

    private fun saveFacts(facts: List<UserFactItem>) {
        try {
            val json = factAdapter.toJson(facts)
            prefs.edit().putString("user_facts_json", json).apply()
            _userFacts.value = facts
        } catch (e: Exception) {
            // handle error
        }
    }
}
