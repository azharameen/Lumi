package com.example.domain.repository

import android.graphics.Bitmap
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import kotlinx.coroutines.flow.Flow

interface LumiRepository {
    val petEvolution: Flow<PetEvolutionEntity?>
    val petStatus: Flow<PetStatus>
    val allTasks: Flow<List<TaskEntity>>
    val allCalendarEvents: Flow<List<CalendarEventEntity>>
    val allWellnessLogs: Flow<List<WellnessLogEntity>>
    val pagedWellnessLogs: Flow<androidx.paging.PagingData<WellnessLogEntity>>
    val allMemories: Flow<List<PetMemoryEntity>>
    val chatMessages: Flow<List<ChatMessageEntity>>
    val pagedChatMessages: Flow<androidx.paging.PagingData<ChatMessageEntity>>

    suspend fun sendMessage(userText: String, image: Bitmap? = null): ChatMessageEntity
    suspend fun petTheCharacter()
    suspend fun setBloubShape(shape: com.example.domain.model.BloubShape)
    suspend fun setBloubSkinColor(skinColor: com.example.domain.model.BloubSkinColor)
    suspend fun addTask(title: String, priority: String, category: String, estimatedMinutes: Int, notes: String): Long
    suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean)
    suspend fun deleteTask(task: TaskEntity)
    suspend fun addCalendarEvent(event: CalendarEventEntity): Long
    suspend fun deleteCalendarEvent(eventId: Long)
    suspend fun logWellness(moodScore: Int, moodLabel: String, energyLevel: Int, hydrationCups: Int, gratitudeNote: String): Long
    suspend fun incrementHydration(logId: Long)
    suspend fun addMemory(topic: String, note: String, sentiment: String)
    suspend fun toggleMemoryPin(memoryId: Long)
    suspend fun setPetEmotion(emotion: PetEmotion)
    suspend fun setSpeaking(isSpeaking: Boolean)
    suspend fun setListening(isListening: Boolean)
    val isOverlayActive: Flow<Boolean>
    fun setOverlayActive(active: Boolean)
    suspend fun setSpeechBubbleText(text: String?)

    // Human-In-The-Loop (HITL) Actions
    val pendingHitlActions: Flow<List<HitlPendingAction>>
    suspend fun resolveHitlAction(stateId: String, approved: Boolean): String?

    // AI Analytics & On-Device Gemma Management
    val aiExecutionLogs: Flow<List<com.example.data.local.entity.AiExecutionLogEntity>>
    val aiRoutingMode: Flow<com.example.data.remote.AiRoutingMode>
    fun setAiRoutingMode(mode: com.example.data.remote.AiRoutingMode)
    suspend fun clearAiAnalyticsLogs()
    suspend fun benchmarkOnDeviceGemma(): Pair<String, Long>

    // Gamification & Wardrobe Economy
    suspend fun earnCoinsAndExp(coins: Int, exp: Int, reason: String = "")
    suspend fun earnGems(gems: Int, reason: String = "")
    suspend fun buyAccessory(accessory: com.example.domain.model.PetAccessory): Boolean
    suspend fun equipAccessory(accessoryId: String)
    suspend fun updatePetName(name: String)

    // Autonomous Goal Planner ("Agent Swarms")
    val allGoalPlans: Flow<List<com.example.data.local.entity.GoalPlanEntity>>
    fun getMilestonesForGoal(goalId: Long): Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>
    suspend fun decomposeGoal(title: String, description: String, category: String, targetDate: String): com.example.domain.planner.DecomposedGoalResult
    suspend fun executeMilestoneTool(milestoneId: Long, goalId: Long): String
    suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean)
    suspend fun deleteGoal(goalId: Long)

    // Proactive AI Briefing Engine
    suspend fun getDailyBriefing(): com.example.domain.briefing.DailyBriefing

    // Procedural Ambient Soundscape Engine
    val soundscapeState: kotlinx.coroutines.flow.StateFlow<com.example.data.device.SoundscapeState>
    fun startSoundscape(type: com.example.data.device.SoundscapeType)
    fun stopSoundscape()
    fun setSoundscapeVolume(volume: Float)
    fun startFocusTimerWithSoundscape(minutes: Int)
    fun stopFocusTimerWithSoundscape()
}
