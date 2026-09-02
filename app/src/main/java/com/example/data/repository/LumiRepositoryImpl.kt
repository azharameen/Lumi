package com.example.data.repository

import android.content.Context
import com.example.data.device.HealthConnectManager
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.*
import com.example.data.remote.AiRoutingMode
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.*
import com.example.domain.planner.DecomposedGoalResult
import com.example.domain.briefing.DailyBriefing
import com.example.data.device.SoundscapeState
import com.example.data.device.SoundscapeType
import com.example.domain.agent.hitl.HitlPendingAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.SupervisorJob
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LumiRepositoryImpl private constructor(
    private val context: Context
) : LumiRepository, KoinComponent {

    private val petRepository: PetRepository by inject()
    private val chatRepository: ChatRepository by inject()
    private val wellnessRepository: WellnessRepository by inject()
    private val taskGoalRepository: TaskGoalRepository by inject()
    private val briefingEngine: com.example.domain.briefing.AutonomousBriefingEngine by inject()
    private val soundscapeEngine: com.example.data.device.ProceduralSoundscapeEngine by inject()

    override val currentEmotion: Flow<PetEmotion> get() = petRepository.currentEmotion
    override val isSpeaking: Flow<Boolean> get() = petRepository.isSpeaking
    override val isListening: Flow<Boolean> get() = petRepository.isListening
    override val isThinking: Flow<Boolean> get() = petRepository.isThinking
    override val speechBubbleText: Flow<String?> get() = petRepository.speechBubbleText
    override val isOverlayActive: Flow<Boolean> get() = petRepository.isOverlayActive
    override val petEvolution: Flow<PetEvolutionEntity?> get() = petRepository.petEvolution
    override val petStatus: Flow<PetStatus> get() = petRepository.petStatus

    override suspend fun petTheAnimal() = petRepository.petTheAnimal()
    override suspend fun feedPet(foodName: String) = petRepository.feedPet(foodName)
    override suspend fun playWithPet() = petRepository.playWithPet()
    override fun setOverlayActive(active: Boolean) = petRepository.setOverlayActive(active)
    override suspend fun setSpeechBubbleText(text: String?) = petRepository.setSpeechBubbleText(text)
    override suspend fun petTheCharacter() = petRepository.petTheAnimal()
    override suspend fun setBloubShape(shape: com.example.domain.model.BloubShape) = petRepository.setBloubShape(shape)
    override suspend fun setBloubSkinColor(skinColor: com.example.domain.model.BloubSkinColor) = petRepository.setBloubSkinColor(skinColor)
    override suspend fun setPetEmotion(emotion: PetEmotion) = petRepository.setPetEmotion(emotion)
    override suspend fun setSpeaking(isSpeaking: Boolean) = petRepository.setSpeaking(isSpeaking)
    override suspend fun setListening(isListening: Boolean) = petRepository.setListening(isListening)
    override suspend fun earnCoinsAndExp(coins: Int, exp: Int, reason: String) = petRepository.earnCoinsAndExp(coins, exp, reason)
    override suspend fun earnGems(gems: Int, reason: String) = petRepository.earnGems(gems, reason)
    override suspend fun buyAccessory(accessory: com.example.domain.model.PetAccessory) = petRepository.buyAccessory(accessory)
    override suspend fun equipAccessory(accessoryId: String) = petRepository.equipAccessory(accessoryId)
    override suspend fun updatePetName(name: String) = petRepository.updatePetName(name)

    override val chatMessages: Flow<List<ChatMessageEntity>> get() = chatRepository.chatMessages
    override val pagedChatMessages: Flow<androidx.paging.PagingData<ChatMessageEntity>> get() = chatRepository.pagedChatMessages
    override suspend fun clearChatHistory() = chatRepository.clearChatHistory()
    override suspend fun deleteMessage(id: Long) = chatRepository.deleteMessage(id)
    override suspend fun sendMessage(userText: String, image: ByteArray?) = chatRepository.sendMessage(userText, image)
    override val aiExecutionLogs: Flow<List<AiExecutionLogEntity>> get() = chatRepository.aiExecutionLogs
    override val aiRoutingMode: Flow<AiRoutingMode> get() = chatRepository.aiRoutingMode
    override fun setAiRoutingMode(mode: AiRoutingMode) = chatRepository.setAiRoutingMode(mode)
    override suspend fun clearAiAnalyticsLogs() = chatRepository.clearAiAnalyticsLogs()
    override suspend fun benchmarkOnDeviceGemma() = chatRepository.benchmarkOnDeviceGemma()
    override val pendingHitlActions: Flow<List<HitlPendingAction>> get() = chatRepository.pendingHitlActions
    override suspend fun resolveHitlAction(stateId: String, approved: Boolean) = chatRepository.resolveHitlAction(stateId, approved)

    override val allTasks: Flow<List<TaskEntity>> get() = taskGoalRepository.allTasks
    override val allCalendarEvents: Flow<List<CalendarEventEntity>> get() = taskGoalRepository.allCalendarEvents
    override val allGoalPlans: Flow<List<GoalPlanEntity>> get() = taskGoalRepository.allGoalPlans
    override fun getMilestonesForGoal(goalId: Long) = taskGoalRepository.getMilestonesForGoal(goalId)
    override suspend fun addTask(title: String, priority: String, category: String, estimatedMinutes: Int, notes: String) = 
        taskGoalRepository.addTask(title, priority, category, estimatedMinutes, notes)
    override suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) = taskGoalRepository.toggleTaskCompleted(taskId, isCompleted)
    override suspend fun deleteTask(task: TaskEntity) = taskGoalRepository.deleteTask(task)
    override suspend fun addCalendarEvent(event: CalendarEventEntity) = taskGoalRepository.addCalendarEvent(event)
    override suspend fun deleteCalendarEvent(eventId: Long) = taskGoalRepository.deleteCalendarEvent(eventId)
    override suspend fun decomposeGoal(title: String, description: String, category: String, targetDate: String) = 
        taskGoalRepository.decomposeGoal(title, description, category, targetDate)
    override suspend fun executeMilestoneTool(milestoneId: Long, goalId: Long) = taskGoalRepository.executeMilestoneTool(milestoneId, goalId)
    override suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) = taskGoalRepository.toggleMilestone(milestoneId, goalId, isCompleted)
    override suspend fun deleteGoal(goalId: Long) = taskGoalRepository.deleteGoal(goalId)

    override val allWellnessLogs: Flow<List<WellnessLogEntity>> get() = wellnessRepository.allWellnessLogs
    override val pagedWellnessLogs: Flow<androidx.paging.PagingData<WellnessLogEntity>> get() = wellnessRepository.pagedWellnessLogs
    override suspend fun logWellness(moodScore: Int, moodLabel: String, energyLevel: Int, hydrationCups: Int, gratitudeNote: String) = 
        wellnessRepository.logWellness(moodScore, moodLabel, energyLevel, hydrationCups, gratitudeNote)
    override suspend fun incrementHydration(logId: Long) = wellnessRepository.incrementHydration(logId)

    override val allMemories: Flow<List<PetMemoryEntity>> get() = (database as LumiDatabase).petMemoryDao().getAllMemories()
    private val database: LumiDatabase by inject()

    override suspend fun addMemory(topic: String, note: String, sentiment: String) {
        database.petMemoryDao().insertMemory(PetMemoryEntity(category = topic, memoryText = note, sentiment = sentiment))
    }
    override suspend fun toggleMemoryPin(memoryId: Long) = database.petMemoryDao().togglePin(memoryId)

    override suspend fun getDailyBriefing(): DailyBriefing {
        // Implementation details omitted for brevity, would call briefingEngine.generateBriefing(...)
        return briefingEngine.generateBriefing(null, petStatus = com.example.domain.model.PetStatus(), petEvolution = null, tasks = emptyList(), events = emptyList(), wellnessLogs = emptyList())
    }

    override val soundscapeState: StateFlow<SoundscapeState> get() = soundscapeEngine.state
    override fun startSoundscape(type: SoundscapeType) = soundscapeEngine.startSoundscape(type)
    override fun stopSoundscape() = soundscapeEngine.stopSoundscape()
    override fun setSoundscapeVolume(volume: Float) = soundscapeEngine.setVolume(volume)
    override fun startFocusTimerWithSoundscape(minutes: Int) = soundscapeEngine.startFocusTimer(minutes)
    override fun stopFocusTimerWithSoundscape() = soundscapeEngine.stopFocusTimer()

    companion object {
        @Volatile
        private var INSTANCE: LumiRepositoryImpl? = null

        fun getInstance(context: Context, healthConnectManager: HealthConnectManager? = null): LumiRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val instance = LumiRepositoryImpl(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
