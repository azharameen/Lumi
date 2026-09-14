package com.example.domain.service

interface AnalyticsService {
    fun logPetInteraction(action: String, petMood: String, happinessLevel: Int)
    fun logPetLevelUp(oldLevel: Int, newLevel: Int, formName: String)
    fun logSoundscapeSession(soundscapeTitle: String, isPlaying: Boolean)
    fun logVaultAction(action: String, isSuccess: Boolean)
    fun logRemoteConfigSync(status: String)
    fun logGoalMilestone(goalTitle: String, category: String, isCompleted: Boolean)
    fun logWellnessSession(exerciseType: String, durationSeconds: Int)
    fun logAiChatMessage(mode: String, messageLength: Int, modelUsed: String)
    fun logScreenView(screenName: String, screenClass: String = "MainActivity")
    fun logAuthEvent(method: String, isNewUser: Boolean)
    fun setUserProperty(name: String, value: String)
}
