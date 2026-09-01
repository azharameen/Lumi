package com.example.data.repository

import com.example.domain.account.*

import android.content.Context
import androidx.datastore.preferences.core.*
import com.example.data.preferences.dataStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

class UserProfileRepositoryImpl(private val context: Context) : UserProfileRepository {

        
        companion object {
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val ROLE_TITLE = stringPreferencesKey("role_title")
        private val PRIMARY_GOAL = stringPreferencesKey("primary_goal")
        private val DAILY_FOCUS = floatPreferencesKey("daily_focus_target_hours")
        private val HYDRATION = intPreferencesKey("target_hydration_cups")
        private val DAILY_STEPS = intPreferencesKey("target_daily_steps")
        private val WAKE_UP = stringPreferencesKey("wake_up_time")
        private val SLEEP_TIME = stringPreferencesKey("sleep_time")
        private val PERSONA_TONE = stringPreferencesKey("persona_tone")
        private val CUSTOM_AI = stringPreferencesKey("custom_ai_instructions")
        private val GEMINI_MODEL = stringPreferencesKey("gemini_model_choice")
        private val TEMPERATURE = floatPreferencesKey("temperature")
        private val PROACTIVE_BRIEF = booleanPreferencesKey("enable_proactive_briefings")
        private val TOOL_CALLING = booleanPreferencesKey("enable_tool_calling")
        private val BIOMETRIC_LOCK = booleanPreferencesKey("enable_biometric_lock")
        private val SPEECH_OUTPUT = booleanPreferencesKey("enable_speech_output")
        private val HAPTIC = booleanPreferencesKey("enable_haptic_feedback")
        private val AMBIENT_LOC = booleanPreferencesKey("enable_ambient_location")
        private val LOCAL_AI = booleanPreferencesKey("enable_local_ai_fallback")
        private val OVERLAY = booleanPreferencesKey("enable_overlay")
        private val HAS_ONBOARDED = booleanPreferencesKey("has_completed_onboarding")
        private val USER_FACTS = stringPreferencesKey("user_facts_json")
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val factListType = Types.newParameterizedType(List::class.java, UserFactItem::class.java)
    private val factAdapter = moshi.adapter<List<UserFactItem>>(factListType)

    private val _userProfile = MutableStateFlow(UserProfileData())
    override val userProfile: StateFlow<UserProfileData> = _userProfile.asStateFlow()

    private val _userFacts = MutableStateFlow(getDefaultFacts())
    override val userFacts: StateFlow<List<UserFactItem>> = _userFacts.asStateFlow()

    init {
        scope.launch {
            val prefs = context.dataStore.data.first()
            _userProfile.value = loadProfile(prefs)
            _userFacts.value = loadFacts(prefs)
        }
    }

    private fun loadProfile(prefs: Preferences): UserProfileData {
        val toneName = prefs[PERSONA_TONE] ?: LumiPersonaTone.EMPATHETIC_CHEERFUL.name
        val tone = try {
            LumiPersonaTone.valueOf(toneName)
        } catch (e: Exception) {
            LumiPersonaTone.EMPATHETIC_CHEERFUL
        }

        return UserProfileData(
            userName = prefs[USER_NAME] ?: "Azhar Ameen",
            userEmail = prefs[USER_EMAIL] ?: "azharameen52@gmail.com",
            roleOrTitle = prefs[ROLE_TITLE] ?: "Software Engineer & AI Architect",
            primaryFocusGoal = prefs[PRIMARY_GOAL] ?: "Deep Flow, Clean Code & Mindful Living",
            dailyFocusTargetHours = prefs[DAILY_FOCUS] ?: 4.0f,
            targetHydrationCups = prefs[HYDRATION] ?: 8,
            targetDailySteps = prefs[DAILY_STEPS] ?: 8000,
            wakeUpTime = prefs[WAKE_UP] ?: "07:30 AM",
            sleepTime = prefs[SLEEP_TIME] ?: "11:30 PM",
            personaTone = tone,
            customAiInstructions = prefs[CUSTOM_AI] ?: "Keep answers concise, actionable, and formatted in clean markdown bullet points.",
            geminiModelChoice = prefs[GEMINI_MODEL] ?: "gemini-2.5-flash",
            temperature = prefs[TEMPERATURE] ?: 0.7f,
            enableProactiveBriefings = prefs[PROACTIVE_BRIEF] ?: true,
            enableToolCalling = prefs[TOOL_CALLING] ?: true,
            enableBiometricLock = prefs[BIOMETRIC_LOCK] ?: false,
            enableSpeechOutput = prefs[SPEECH_OUTPUT] ?: true,
            enableHapticFeedback = prefs[HAPTIC] ?: true,
            enableAmbientLocation = prefs[AMBIENT_LOC] ?: true,
            enableLocalAiFallback = prefs[LOCAL_AI] ?: true,
            enableOverlay = prefs[OVERLAY] ?: true,
            hasCompletedOnboarding = prefs[HAS_ONBOARDED] ?: false
        )
    }

    private fun loadFacts(prefs: Preferences): List<UserFactItem> {
        val json = prefs[USER_FACTS]
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
        return getDefaultFacts()
    }

    private fun getDefaultFacts(): List<UserFactItem> {
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

    override fun updateProfile(profile: UserProfileData) {
        _userProfile.value = profile
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[USER_NAME] = profile.userName
                prefs[USER_EMAIL] = profile.userEmail
                prefs[ROLE_TITLE] = profile.roleOrTitle
                prefs[PRIMARY_GOAL] = profile.primaryFocusGoal
                prefs[DAILY_FOCUS] = profile.dailyFocusTargetHours
                prefs[HYDRATION] = profile.targetHydrationCups
                prefs[DAILY_STEPS] = profile.targetDailySteps
                prefs[WAKE_UP] = profile.wakeUpTime
                prefs[SLEEP_TIME] = profile.sleepTime
                prefs[PERSONA_TONE] = profile.personaTone.name
                prefs[CUSTOM_AI] = profile.customAiInstructions
                prefs[GEMINI_MODEL] = profile.geminiModelChoice
                prefs[TEMPERATURE] = profile.temperature
                prefs[PROACTIVE_BRIEF] = profile.enableProactiveBriefings
                prefs[TOOL_CALLING] = profile.enableToolCalling
                prefs[BIOMETRIC_LOCK] = profile.enableBiometricLock
                prefs[SPEECH_OUTPUT] = profile.enableSpeechOutput
                prefs[HAPTIC] = profile.enableHapticFeedback
                prefs[AMBIENT_LOC] = profile.enableAmbientLocation
                prefs[LOCAL_AI] = profile.enableLocalAiFallback
                prefs[OVERLAY] = profile.enableOverlay
                prefs[HAS_ONBOARDED] = profile.hasCompletedOnboarding
            }
        }
    }

    override fun updateField(block: (UserProfileData) -> UserProfileData) {
        val updated = block(_userProfile.value)
        updateProfile(updated)
    }

    override fun addUserFact(category: String, factText: String, isPinned: Boolean) {
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

    override fun removeUserFact(id: String) {
        val updatedList = _userFacts.value.filter { it.id != id }
        saveFacts(updatedList)
    }

    override fun togglePinFact(id: String) {
        val updatedList = _userFacts.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveFacts(updatedList)
    }

    override fun resetToDefaults() {
        val defaultProfile = UserProfileData()
        val defaultFacts = getDefaultFacts()
        _userProfile.value = defaultProfile
        _userFacts.value = defaultFacts
        scope.launch {
            context.dataStore.edit { it.clear() }
        }
    }

    private fun saveFacts(facts: List<UserFactItem>) {
        _userFacts.value = facts
        scope.launch {
            try {
                val json = factAdapter.toJson(facts)
                context.dataStore.edit { prefs ->
                    prefs[USER_FACTS] = json
                }
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
