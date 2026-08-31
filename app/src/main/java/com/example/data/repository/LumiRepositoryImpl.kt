package com.example.data.repository

import com.example.data.tools.FastToolIndex
import com.example.domain.tools.ToolRetriever
import com.example.framework.tools.SystemToolSuite


import android.content.Context
import com.example.data.device.HealthConnectManager
import android.graphics.Bitmap
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.data.remote.AiRoutingMode
import com.example.data.remote.HybridAiEngine
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.LumiRepository
import com.example.domain.tools.AgentToolDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.min

class LumiRepositoryImpl private constructor(
    private val database: LumiDatabase,
    private val scope: CoroutineScope,
    private val context: Context,
    private val healthConnectManager: HealthConnectManager? = null
) : LumiRepository {

    private val connectorManager = com.example.domain.connectors.ConnectorManager(context)
    private val integrationService = com.example.domain.connectors.IntegrationService(connectorManager)
    private val toolDispatcher = AgentToolDispatcher(database, integrationService, healthConnectManager)
    private val fastToolIndex = FastToolIndex(database.toolFtsDao())
    private val toolRetriever = ToolRetriever(fastToolIndex)
    private val hybridAiEngine = HybridAiEngine(toolDispatcher, database.aiExecutionLogDao(), database, context, toolRetriever)
    private val autonomousGoalPlanner = com.example.domain.planner.AutonomousGoalPlanner(context, database, toolDispatcher, integrationService)
    private val autonomousBriefingEngine = com.example.domain.briefing.AutonomousBriefingEngine(context)
    private val soundscapeEngine = com.example.data.device.ProceduralSoundscapeEngine.getInstance(context)

    init {
        SystemToolSuite.registerAll(context)
        scope.launch(Dispatchers.IO) {
            try {
                toolRetriever.initializeIndex()
            } catch (e: Exception) {
                // Log FTS index failure gracefully
            }
        }
    }


    private val _currentEmotion = MutableStateFlow(PetEmotion.HAPPY)
    private val _isSpeaking = MutableStateFlow(false)
    private val _isListening = MutableStateFlow(false)
    private val _isThinking = MutableStateFlow(false)
    private val _speechBubbleText = MutableStateFlow<String?>("Hi! I'm Lumi, your personal AI friend ✨")
    private val _isOverlayActive = MutableStateFlow(false)
    override val currentEmotion: Flow<PetEmotion> = _currentEmotion.asStateFlow()
    override val isSpeaking: Flow<Boolean> = _isSpeaking.asStateFlow()
    override val isListening: Flow<Boolean> = _isListening.asStateFlow()
    override val isThinking: Flow<Boolean> = _isThinking.asStateFlow()
    override val speechBubbleText: Flow<String?> = _speechBubbleText.asStateFlow()

    override suspend fun petTheAnimal() {
        petTheCharacter()
    }

    override suspend fun feedPet(foodName: String) {
        earnCoinsAndExp(5, 10, 'Fed pet ')
    }

    override suspend fun playWithPet() {
        earnCoinsAndExp(10, 20, 'Played with pet')
    }


    override val isOverlayActive: Flow<Boolean> = _isOverlayActive.asStateFlow()

    override fun setOverlayActive(active: Boolean) {
        _isOverlayActive.value = active
    }

    override suspend fun setSpeechBubbleText(text: String?) {
        _speechBubbleText.value = text
    }

    override val aiExecutionLogs: Flow<List<AiExecutionLogEntity>> = database.aiExecutionLogDao().getAllLogs()
    override val aiRoutingMode: Flow<AiRoutingMode> = hybridAiEngine.routingMode

    override fun setAiRoutingMode(mode: AiRoutingMode) {
        hybridAiEngine.setRoutingMode(mode)
    }

    override suspend fun clearAiAnalyticsLogs() {
        database.aiExecutionLogDao().clearAllLogs()
    }

    override suspend fun benchmarkOnDeviceGemma(): Pair<String, Long> {
        val result = hybridAiEngine.onDeviceGemmaEngine.benchmarkOnDeviceGemma()
        // Log benchmark invocation
        val now = System.currentTimeMillis()
        val log = AiExecutionLogEntity(
            taskCategory = "On-Device Benchmark",
            engineType = "ON_DEVICE_GEMMA",
            modelName = "gemma-2b-it-int4",
            promptPreview = "[Benchmark] Warmup & token throughput test",
            responsePreview = result.first,
            promptTokens = 16,
            completionTokens = 32,
            totalTokens = 48,
            estimatedCostUsd = 0.0,
            startTimeMillis = now - result.second,
            finishTimeMillis = now,
            durationMs = result.second,
            isSuccess = true,
            isOffline = true,
            hardwareTarget = "GPU OpenCL / NPU"
        )
        database.aiExecutionLogDao().insertLog(log)
        return result
    }

    override val petEvolution: Flow<PetEvolutionEntity?> = database.petEvolutionDao().getPetEvolution()
    override val allTasks: Flow<List<TaskEntity>> = database.taskDao().getAllTasks()
    override val allCalendarEvents: Flow<List<CalendarEventEntity>> = database.calendarEventDao().getAllEvents()
    override val allWellnessLogs: Flow<List<WellnessLogEntity>> = database.wellnessLogDao().getAllLogs()

    override val pagedWellnessLogs: Flow<androidx.paging.PagingData<WellnessLogEntity>> = androidx.paging.Pager(
        config = androidx.paging.PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.wellnessLogDao().getPagedWellnessLogs() }
    ).flow

    override val allMemories: Flow<List<PetMemoryEntity>> = database.petMemoryDao().getAllMemories()
    override val chatMessages: Flow<List<ChatMessageEntity>> = database.chatMessageDao().getAllMessages()
    
    override val pagedChatMessages: kotlinx.coroutines.flow.Flow<androidx.paging.PagingData<com.example.data.local.entity.ChatMessageEntity>> = androidx.paging.Pager(
        config = androidx.paging.PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = { database.chatMessageDao().getPagedMessages() }
    ).flow


    override val petStatus: Flow<PetStatus> = combine(
        combine(petEvolution, _currentEmotion, _isSpeaking) { evo, emotion, speaking ->
            Triple(evo, emotion, speaking)
        },
        combine(_isListening, _isThinking, _speechBubbleText) { listening, thinking, bubbleText ->
            Triple(listening, thinking, bubbleText)
        }
    ) { (evo, emotion, speaking), (listening, thinking, bubbleText) ->
        val entity = evo ?: PetEvolutionEntity()
        val shape = try {
            com.example.domain.model.BloubShape.valueOf(entity.bloubShape)
        } catch (e: Exception) {
            com.example.domain.model.BloubShape.SPHERE
        }
        val skinColor = try {
            com.example.domain.model.BloubSkinColor.valueOf(entity.bloubSkinColor)
        } catch (e: Exception) {
            com.example.domain.model.BloubSkinColor.ELECTRIC_CYAN
        }

        PetStatus(
            name = entity.name,
            level = entity.level,
            exp = entity.exp,
            expToNextLevel = entity.expToNextLevel,
            coins = entity.coins,
            gems = entity.gems,
            streakDays = entity.streakDays,
            bondScore = entity.bondScore,
            happiness = entity.happiness,
            energy = entity.energy,
            personalityTrait = entity.personalityTrait,
            activeAccessory = entity.activeAccessory,
            currentEmotion = emotion,
            bloubShape = shape,
            bloubSkinColor = skinColor,
            unlockedAccessoriesCsv = entity.unlockedAccessoriesCsv,
            unlockedSkinsCsv = entity.unlockedSkinsCsv,
            unlockedShapesCsv = entity.unlockedShapesCsv,
            isSpeaking = speaking,
            isListening = listening,
            isThinking = thinking,
            speechBubbleText = bubbleText,
            daysTogether = entity.daysTogether,
            totalInteractions = entity.totalInteractions
        )
    }

    init {
        // Initialize default state if first launch
        scope.launch(Dispatchers.IO) {
            val existing = database.petEvolutionDao().getPetEvolutionDirect()
            if (existing == null) {
                database.petEvolutionDao().insertOrUpdate(PetEvolutionEntity())
                // Seed initial introductory message
                database.chatMessageDao().insertMessage(
                    ChatMessageEntity(
                        sender = "LUMI",
                        content = "Hello there! I'm Lumi, your AI companion. I'm here to support your mental wellness, organize your schedule, and help you thrive every day! How are you feeling today?",
                        petEmotion = "HAPPY"
                    )
                )
                // Seed introductory task
                database.taskDao().insertTask(
                    TaskEntity(
                        title = "Take 3 deep breaths with Lumi",
                        notes = "Practice our first mindful moment together",
                        priority = "HIGH",
                        category = "Wellness",
                        estimatedMinutes = 5
                    )
                )
            }
        }
    }

    override suspend fun sendMessage(userText: String, image: Bitmap?): ChatMessageEntity {
        // Save user message
        val userEntity = ChatMessageEntity(
            sender = "USER",
            content = userText,
            imageBase64OrUri = if (image != null) "IMAGE_ATTACHED" else null
        )
        database.chatMessageDao().insertMessage(userEntity)

        _isThinking.value = true
        _currentEmotion.value = PetEmotion.THINKING
        _speechBubbleText.value = "Thinking..."

        val recentEntities = database.chatMessageDao().getRecentMessagesDirect()
        val historyTurns = recentEntities.reversed().map { it.sender to it.content }

        val agentResult = hybridAiEngine.executeUserTurn(
            userMessage = userText,
            recentHistory = historyTurns,
            imageAttachment = image
        )

        _isThinking.value = false
        _currentEmotion.value = agentResult.inferredEmotion
        _speechBubbleText.value = agentResult.responseText

        val toolName = agentResult.toolReports.firstOrNull()?.toolName
        val toolResult = agentResult.toolReports.firstOrNull()?.description

        val aiEntity = ChatMessageEntity(
            sender = "LUMI",
            content = agentResult.responseText,
            petEmotion = agentResult.inferredEmotion.name,
            toolUsedName = toolName,
            toolResultJson = toolResult
        )
        database.chatMessageDao().insertMessage(aiEntity)

        return aiEntity
    }

    override suspend fun petTheCharacter() {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        val newHappiness = min(100, current.happiness + 8)
        val newBond = min(100, current.bondScore + 3)
        var newExp = current.exp + 10
        var newLevel = current.level
        var expNeeded = current.expToNextLevel
        var newCoins = current.coins + 5
        var newGems = current.gems

        if (newExp >= expNeeded) {
            newExp -= expNeeded
            newLevel += 1
            expNeeded = (expNeeded * 1.3).toInt()
            newCoins += 50
            newGems += 5
        }

        database.petEvolutionDao().insertOrUpdate(
            current.copy(
                happiness = newHappiness,
                bondScore = newBond,
                exp = newExp,
                level = newLevel,
                expToNextLevel = expNeeded,
                coins = newCoins,
                gems = newGems,
                totalInteractions = current.totalInteractions + 1
            )
        )

        _currentEmotion.value = PetEmotion.LOVING
        _speechBubbleText.value = "..."
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val response = hybridAiEngine.executeUserTurn("The user just pet you affectionately. Give a very short, cute 1-sentence reaction as a virtual companion.")
                _speechBubbleText.value = response.responseText
            } catch (e: Exception) {
                _speechBubbleText.value = "*Purrrrr* So warm!"
            }
        }
    }

    override suspend fun earnCoinsAndExp(coins: Int, exp: Int, reason: String) {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        var newExp = current.exp + exp
        var newLevel = current.level
        var expNeeded = current.expToNextLevel
        var newCoins = current.coins + coins
        var newGems = current.gems

        val leveledUp = newExp >= expNeeded
        while (newExp >= expNeeded) {
            newExp -= expNeeded
            newLevel += 1
            expNeeded = (expNeeded * 1.3).toInt()
            newCoins += 50
            newGems += 5
        }

        database.petEvolutionDao().updateProgression(
            exp = newExp,
            level = newLevel,
            expToNextLevel = expNeeded,
            coins = newCoins,
            gems = newGems
        )

        if (leveledUp) {
            _currentEmotion.value = PetEmotion.ENERGETIC
            _speechBubbleText.value = "LEVEL UP! We are now Level $newLevel! +50 Coins 🪙 +5 Gems 💎"
        } else if (coins > 0 || exp > 0) {
            _speechBubbleText.value = "+$coins 🪙 +$exp XP ${if (reason.isNotBlank()) "for $reason" else ""}"
        }
    }

    override suspend fun earnGems(gems: Int, reason: String) {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        val newGems = current.gems + gems
        database.petEvolutionDao().updateCurrencies(current.coins, newGems)
        _currentEmotion.value = PetEmotion.HAPPY
        _speechBubbleText.value = "+$gems Starlight Gems! 💎"
    }

    override suspend fun buyAccessory(accessory: com.example.domain.model.PetAccessory): Boolean {
        val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
        if (current.coins < accessory.coinCost || current.gems < accessory.gemCost) {
            return false
        }
        val unlockedList = current.unlockedAccessoriesCsv.split(",").toMutableSet()
        unlockedList.add(accessory.id)
        val newCsv = unlockedList.joinToString(",")
        val newCoins = current.coins - accessory.coinCost
        val newGems = current.gems - accessory.gemCost

        database.petEvolutionDao().insertOrUpdate(
            current.copy(
                coins = newCoins,
                gems = newGems,
                unlockedAccessoriesCsv = newCsv,
                activeAccessory = accessory.id
            )
        )
        _currentEmotion.value = PetEmotion.PLAYFUL
        _speechBubbleText.value = "Equipped ${accessory.displayName}! ${accessory.iconEmoji} How stylish!"
        return true
    }

    override suspend fun equipAccessory(accessoryId: String) {
        database.petEvolutionDao().setActiveAccessory(accessoryId)
        _currentEmotion.value = PetEmotion.HAPPY
        _speechBubbleText.value = "Changed look! ✨ Looking sharp!"
    }

    override suspend fun updatePetName(name: String) {
        database.petEvolutionDao().updatePetName(name)
        _speechBubbleText.value = "My new name is $name! I love it!"
    }

    override suspend fun setBloubShape(shape: com.example.domain.model.BloubShape) {
        database.petEvolutionDao().setBloubShape(shape.name)
        _currentEmotion.value = PetEmotion.HAPPY
        _speechBubbleText.value = "Transformed into ${shape.displayName}! ${shape.iconEmoji} How do I look?"
    }

    override suspend fun setBloubSkinColor(skinColor: com.example.domain.model.BloubSkinColor) {
        database.petEvolutionDao().setBloubSkinColor(skinColor.name)
        _currentEmotion.value = PetEmotion.ENERGETIC
        _speechBubbleText.value = "Ooh, new ${skinColor.displayName} clay skin! ✨ Glowing!"
    }

    override suspend fun addTask(
        title: String,
        priority: String,
        category: String,
        estimatedMinutes: Int,
        notes: String
    ): Long {
        return database.taskDao().insertTask(
            TaskEntity(
                title = title,
                notes = notes,
                priority = priority,
                category = category,
                estimatedMinutes = estimatedMinutes
            )
        )
    }

    override suspend fun toggleTaskCompleted(taskId: Long, isCompleted: Boolean) {
        database.taskDao().setTaskCompleted(taskId, isCompleted)
        if (isCompleted) {
            val current = database.petEvolutionDao().getPetEvolutionDirect() ?: PetEvolutionEntity()
            database.petEvolutionDao().insertOrUpdate(
                current.copy(
                    tasksHelpedComplete = current.tasksHelpedComplete + 1,
                    bondScore = min(100, current.bondScore + 2)
                )
            )
            earnCoinsAndExp(coins = 25, exp = 20, reason = "Completing Task")
            _currentEmotion.value = PetEmotion.ENERGETIC
        }
    }

    override suspend fun deleteTask(task: TaskEntity) {
        database.taskDao().deleteTask(task)
    }

    override suspend fun addCalendarEvent(event: CalendarEventEntity): Long {
        return database.calendarEventDao().insertEvent(event)
    }

    override suspend fun deleteCalendarEvent(eventId: Long) {
        database.calendarEventDao().deleteEventById(eventId)
    }

    override suspend fun logWellness(
        moodScore: Int,
        moodLabel: String,
        energyLevel: Int,
        hydrationCups: Int,
        gratitudeNote: String
    ): Long {
        val id = database.wellnessLogDao().insertLog(
            WellnessLogEntity(
                moodScore = moodScore,
                moodLabel = moodLabel,
                energyLevel = energyLevel,
                hydrationCups = hydrationCups,
                gratitudeNote = gratitudeNote
            )
        )
        earnCoinsAndExp(coins = 15, exp = 15, reason = "Wellness Reflection")
        return id
    }

    override suspend fun incrementHydration(logId: Long) {
        database.wellnessLogDao().incrementHydration(logId)
        _currentEmotion.value = PetEmotion.HAPPY
        _speechBubbleText.value = "Great job hydrating! 💧 Stay energized!"
    }

    override suspend fun addMemory(topic: String, note: String, sentiment: String) {
        database.petMemoryDao().insertMemory(
            PetMemoryEntity(
                category = topic,
                memoryText = note,
                sentiment = sentiment
            )
        )
    }

    override suspend fun toggleMemoryPin(memoryId: Long) {
        database.petMemoryDao().togglePin(memoryId)
    }

    override suspend fun setPetEmotion(emotion: PetEmotion) {
        _currentEmotion.value = emotion
    }

    override suspend fun setSpeaking(isSpeaking: Boolean) {
        _isSpeaking.value = isSpeaking
    }

    override suspend fun setListening(isListening: Boolean) {
        _isListening.value = isListening
    }

    // Autonomous Goal Planner ("Agent Swarms")
    override val allGoalPlans: Flow<List<com.example.data.local.entity.GoalPlanEntity>> =
        database.goalPlanDao().getAllGoals()

    override fun getMilestonesForGoal(goalId: Long): Flow<List<com.example.data.local.entity.GoalMilestoneEntity>> =
        database.goalPlanDao().getMilestonesForGoal(goalId)

    override suspend fun decomposeGoal(
        title: String,
        description: String,
        category: String,
        targetDate: String
    ): com.example.domain.planner.DecomposedGoalResult {
        return autonomousGoalPlanner.decomposeAndSaveGoal(title, description, category, targetDate)
    }

    override suspend fun executeMilestoneTool(milestoneId: Long, goalId: Long): String {
        return autonomousGoalPlanner.executeMilestoneTool(milestoneId, goalId)
    }

    override suspend fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) {
        autonomousGoalPlanner.toggleMilestone(milestoneId, goalId, isCompleted)
        if (isCompleted) {
            earnCoinsAndExp(coins = 35, exp = 30, reason = "Goal Milestone Conquered")
            earnGems(gems = 2, reason = "Goal Milestone")
        }
    }

    override suspend fun deleteGoal(goalId: Long) {
        database.goalPlanDao().deleteGoalById(goalId)
    }

    // Proactive AI Briefing Engine
    override suspend fun getDailyBriefing(): com.example.domain.briefing.DailyBriefing {
        val tasks = allTasks.firstOrNull() ?: emptyList()
        val events = allCalendarEvents.firstOrNull() ?: emptyList()
        val wellness = allWellnessLogs.firstOrNull() ?: emptyList()
        val petEvolution = database.petEvolutionDao().getPetEvolutionDirect()
        val currentPet = petStatus.firstOrNull() ?: PetStatus()
        return autonomousBriefingEngine.generateBriefing(
            type = null,
            petStatus = currentPet,
            petEvolution = petEvolution,
            tasks = tasks,
            events = events,
            wellnessLogs = wellness
        )
    }

    // Procedural Ambient Soundscape Engine
    override val soundscapeState: kotlinx.coroutines.flow.StateFlow<com.example.data.device.SoundscapeState> =
        soundscapeEngine.state

    override fun startSoundscape(type: com.example.data.device.SoundscapeType) {
        soundscapeEngine.startSoundscape(type)
    }

    override fun stopSoundscape() {
        soundscapeEngine.stopSoundscape()
    }

    override fun setSoundscapeVolume(volume: Float) {
        soundscapeEngine.setVolume(volume)
    }

    override fun startFocusTimerWithSoundscape(minutes: Int) {
        soundscapeEngine.startFocusTimer(minutes)
    }

    override fun stopFocusTimerWithSoundscape() {
        soundscapeEngine.stopFocusTimer()
    }

    override val pendingHitlActions: kotlinx.coroutines.flow.Flow<List<com.example.domain.agent.hitl.HitlPendingAction>>
        get() = hybridAiEngine.hitlApprovalManager.pendingActions

    override suspend fun resolveHitlAction(stateId: String, approved: Boolean): String? {
        val resultState = hybridAiEngine.hitlApprovalManager.resolveAction(stateId, approved)
        if (resultState != null) {
            val responseText = resultState.finalResponseText ?: resultState.executedToolReports.lastOrNull()?.description ?: "Action completed."
            _currentEmotion.value = resultState.inferredEmotion
            _speechBubbleText.value = responseText

            val toolName = resultState.executedToolReports.firstOrNull()?.toolName
            val toolDesc = resultState.executedToolReports.firstOrNull()?.description

            val aiEntity = ChatMessageEntity(
                sender = "LUMI",
                content = responseText,
                petEmotion = resultState.inferredEmotion.name,
                toolUsedName = toolName,
                toolResultJson = toolDesc
            )
            database.chatMessageDao().insertMessage(aiEntity)
            return responseText
        }
        return null
    }

    companion object {
        @Volatile
        private var INSTANCE: LumiRepositoryImpl? = null

        fun getInstance(context: Context, healthConnectManager: HealthConnectManager? = null): LumiRepositoryImpl {
            return INSTANCE ?: synchronized(this) {
                val appContext = context.applicationContext
                val db = LumiDatabase.getDatabase(appContext)
                val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
                val instance = LumiRepositoryImpl(db, applicationScope, appContext, healthConnectManager)
                INSTANCE = instance
                instance
            }
        }
    }
}
