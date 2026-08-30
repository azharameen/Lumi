package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LumiDatabase
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.PetEvolutionEntity
import com.example.data.local.entity.PetMemoryEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.briefing.BriefingType
import com.example.domain.briefing.DailyBriefing
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.LumiRepository
import com.example.service.AudioHeadsetManager
import com.example.service.AudioHeadsetStatus
import com.example.service.BatteryStatus
import com.example.service.BatteryStatusManager
import com.example.service.BiometricVaultManager
import com.example.service.ClipboardAssistant
import com.example.service.ContextLocationEngine
import com.example.service.LocationContext
import com.example.service.LumiAlarmScheduler
import com.example.service.LumiAppWidgetProvider
import com.example.service.NetworkConnectivityEngine
import com.example.service.NetworkStatus
import com.example.service.RealtimeAudioReactiveEngine
import com.example.service.SensorsManager
import com.example.service.VoiceEngine
import com.example.service.ZenModeManager
import com.example.service.ZenModeStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LumiUiState(
    val selectedTab: Int = 0, // 0: Companion, 1: Assistant, 2: Life Hub, 3: Wellness, 4: Account
    val lifeHubSubTab: Int = 0, // 0: Schedule, 1: Tasks, 2: Goal Swarms, 3: Focus Audio
    val isOverlayEnabled: Boolean = false,
    val showCameraDialog: Boolean = false,
    val showBreathingDialog: Boolean = false,
    val showOverlayPermissionDialog: Boolean = false,
    val showLiveVoiceMode: Boolean = false,
    val isTtsVoiceOutputEnabled: Boolean = true,
    val inputText: String = "",
    val detectedClipboardText: String? = null,
    val isMemoryVaultUnlocked: Boolean = false,
    val vaultAuthError: String? = null,
    val sharedIncomingBanner: String? = null
)

class LumiViewModel(application: Application) : AndroidViewModel(application) {

    val repository: LumiRepository = LumiRepositoryImpl.getInstance(application)
    val userProfileManager = com.example.domain.account.UserProfileManager(application)

    val userProfile: StateFlow<com.example.domain.account.UserProfileData> = userProfileManager.userProfile
    val userFacts: StateFlow<List<com.example.domain.account.UserFactItem>> = userProfileManager.userFacts

    val voiceEngine = VoiceEngine(application)
    val sensorsManager = SensorsManager(application)
    val batteryManager = BatteryStatusManager(application)
    val locationEngine = ContextLocationEngine(application)
    val clipboardAssistant = ClipboardAssistant(application)
    val audioReactiveEngine = RealtimeAudioReactiveEngine(application)
    val networkEngine = NetworkConnectivityEngine(application)
    val headsetManager = AudioHeadsetManager(application)
    val zenManager = ZenModeManager(application)
    val biometricVault = BiometricVaultManager(application)
    val briefingEngine = AutonomousBriefingEngine(application)

    private val _dailyBriefing = MutableStateFlow<DailyBriefing?>(null)
    val dailyBriefing: StateFlow<DailyBriefing?> = _dailyBriefing.asStateFlow()

    private val _isBriefingGenerating = MutableStateFlow(false)
    val isBriefingGenerating: StateFlow<Boolean> = _isBriefingGenerating.asStateFlow()

    private val _isBriefingSpeaking = MutableStateFlow(false)
    val isBriefingSpeaking: StateFlow<Boolean> = _isBriefingSpeaking.asStateFlow()

    val petStatus: StateFlow<PetStatus> = repository.petStatus.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PetStatus()
    )

    val petEvolution: StateFlow<PetEvolutionEntity?> = repository.petEvolution.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCalendarEvents: StateFlow<List<CalendarEventEntity>> = repository.allCalendarEvents.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allWellnessLogs: StateFlow<List<WellnessLogEntity>> = repository.allWellnessLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allMemories: StateFlow<List<PetMemoryEntity>> = repository.allMemories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allGoalPlans: StateFlow<List<com.example.data.local.entity.GoalPlanEntity>> = repository.allGoalPlans.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val soundscapeState: StateFlow<com.example.service.SoundscapeState> = repository.soundscapeState

    fun decomposeGoal(title: String, description: String, category: String = "Productivity", targetDate: String = "") {
        viewModelScope.launch {
            sensorsManager.vibrateCelebration()
            repository.decomposeGoal(title, description, category, targetDate)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun executeMilestone(milestoneId: Long, goalId: Long) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.executeMilestoneTool(milestoneId, goalId)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun toggleMilestone(milestoneId: Long, goalId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            sensorsManager.vibrateTap()
            repository.toggleMilestone(milestoneId, goalId, isCompleted)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(goalId)
        }
    }

    fun startSoundscape(type: com.example.service.SoundscapeType) {
        sensorsManager.vibrateTap()
        repository.startSoundscape(type)
    }

    fun stopSoundscape() {
        repository.stopSoundscape()
    }

    fun setSoundscapeVolume(volume: Float) {
        repository.setSoundscapeVolume(volume)
    }

    fun startFocusTimerWithSoundscape(minutes: Int) {
        sensorsManager.vibrateCelebration()
        repository.startFocusTimerWithSoundscape(minutes)
    }

    fun stopFocusTimerWithSoundscape() {
        repository.stopFocusTimerWithSoundscape()
    }

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val aiExecutionLogs: StateFlow<List<com.example.data.local.entity.AiExecutionLogEntity>> = repository.aiExecutionLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val modelDownloadManager = com.example.data.remote.ModelDownloadManager.getInstance(application)
    val localModelCatalog = modelDownloadManager.catalog
    val modelDownloadStates = modelDownloadManager.downloadStates
    val activeLocalModelId = modelDownloadManager.activeModelId
    val selectedAccelerator = modelDownloadManager.selectedAccelerator

    fun downloadLocalModel(modelId: String) {
        sensorsManager.vibrateTap()
        modelDownloadManager.downloadModel(modelId)
    }

    fun cancelModelDownload(modelId: String) {
        modelDownloadManager.cancelDownload(modelId)
    }

    fun deleteLocalModel(modelId: String) {
        sensorsManager.vibrateTap()
        modelDownloadManager.deleteModel(modelId)
    }

    fun setActiveLocalModel(modelId: String) {
        sensorsManager.vibrateTap()
        modelDownloadManager.setActiveModel(modelId)
    }

    fun setHardwareAccelerator(accelerator: com.example.data.remote.HardwareAccelerator) {
        sensorsManager.vibrateTap()
        modelDownloadManager.setAccelerator(accelerator)
    }

    val aiRoutingMode: StateFlow<com.example.data.remote.AiRoutingMode> = repository.aiRoutingMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.example.data.remote.AiRoutingMode.HYBRID_AUTO
    )

    private val _benchmarkStatus = MutableStateFlow<String?>(null)
    val benchmarkStatus: StateFlow<String?> = _benchmarkStatus.asStateFlow()

    fun setAiRoutingMode(mode: com.example.data.remote.AiRoutingMode) {
        repository.setAiRoutingMode(mode)
    }

    fun clearAiAnalytics() {
        viewModelScope.launch {
            repository.clearAiAnalyticsLogs()
        }
    }

    fun runGemmaBenchmark() {
        viewModelScope.launch {
            _benchmarkStatus.value = "Running On-Device Gemma 2B benchmark..."
            val (result, duration) = repository.benchmarkOnDeviceGemma()
            _benchmarkStatus.value = "✅ $result"
        }
    }

    val batteryStatus: StateFlow<BatteryStatus> = batteryManager.batteryStatus
    val locationContext: StateFlow<LocationContext> = locationEngine.locationState
    val audioLoudness: StateFlow<Float> = audioReactiveEngine.audioLoudness
    val copiedSnippet: StateFlow<String?> = clipboardAssistant.latestCopiedSnippet
    val networkStatus: StateFlow<NetworkStatus> = networkEngine.networkStatus
    val headsetStatus: StateFlow<AudioHeadsetStatus> = headsetManager.headsetStatus
    val zenStatus: StateFlow<ZenModeStatus> = zenManager.zenStatus

    private val _uiState = MutableStateFlow(LumiUiState())
    val uiState: StateFlow<LumiUiState> = _uiState.asStateFlow()

    init {
        // Accelerometer & shake listener
        sensorsManager.startListening(
            onShake = {
                viewModelScope.launch {
                    sensorsManager.vibratePurr()
                    repository.setPetEmotion(PetEmotion.PLAYFUL)
                }
            }
        )

        // Native battery & power listener
        batteryManager.startListening { status ->
            viewModelScope.launch {
                if (status.isCharging) {
                    repository.setPetEmotion(PetEmotion.ENERGETIC)
                } else if (status.isLow) {
                    repository.setPetEmotion(PetEmotion.SLEEPY)
                }
            }
        }

        // Native clipboard assistant listener
        clipboardAssistant.startListening { snippet ->
            _uiState.value = _uiState.value.copy(detectedClipboardText = snippet)
        }

        // Network connectivity monitoring
        networkEngine.startListening { net ->
            if (!net.isConnected) {
                viewModelScope.launch {
                    repository.setPetEmotion(PetEmotion.THINKING)
                }
            }
        }

        // Headphone & Bluetooth audio monitoring
        headsetManager.startListening { head ->
            if (head.isHeadsetConnected) {
                sensorsManager.vibrateTap()
            }
        }

        // Zen & DND mode monitoring
        zenManager.startListening { zen ->
            if (zen.isDndActive) {
                _uiState.value = _uiState.value.copy(isTtsVoiceOutputEnabled = false)
                viewModelScope.launch {
                    repository.setPetEmotion(PetEmotion.SLEEPY)
                }
            }
        }

        // Location context setup
        if (locationEngine.checkPermission()) {
            locationEngine.startLocationUpdates()
        }

        // Sync overlay active state across app & overlay service
        viewModelScope.launch {
            repository.isOverlayActive.collect { active ->
                _uiState.value = _uiState.value.copy(isOverlayEnabled = active)
            }
        }

        // Schedule periodic wellness nudge and daily briefings
        LumiAlarmScheduler.scheduleNextWellnessNudge(application)
        LumiAlarmScheduler.scheduleDailyBriefings(application)

        // Initial daily briefing generation
        refreshDailyBriefing()
    }

    fun refreshDailyBriefing(type: BriefingType? = null) {
        viewModelScope.launch {
            _isBriefingGenerating.value = true
            kotlinx.coroutines.delay(250)
            val briefing = briefingEngine.generateBriefing(
                type = type,
                petStatus = petStatus.value,
                petEvolution = petEvolution.value,
                tasks = allTasks.value,
                events = allCalendarEvents.value,
                wellnessLogs = allWellnessLogs.value,
                locationCity = locationContext.value.approximatePlace
            )
            _dailyBriefing.value = briefing
            _isBriefingGenerating.value = false
        }
    }

    fun playBriefingAudio(briefing: DailyBriefing) {
        if (_isBriefingSpeaking.value) {
            voiceEngine.stopSpeaking()
            _isBriefingSpeaking.value = false
            return
        }
        _isBriefingSpeaking.value = true
        voiceEngine.speak(briefing.audioScript) {
            _isBriefingSpeaking.value = false
        }
    }

    fun speakBriefing() {
        val b = _dailyBriefing.value ?: return
        playBriefingAudio(b)
    }

    fun stopBriefingAudio() {
        voiceEngine.stopSpeaking()
        _isBriefingSpeaking.value = false
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun setLifeHubSubTab(subTab: Int) {
        _uiState.value = _uiState.value.copy(lifeHubSubTab = subTab)
    }

    fun navigateToLifeHub(subTab: Int = 0) {
        _uiState.value = _uiState.value.copy(selectedTab = 2, lifeHubSubTab = subTab)
    }

    fun setInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun setShowCamera(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCameraDialog = show)
    }

    fun setShowBreathing(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBreathingDialog = show)
    }

    fun setShowOverlayPermission(show: Boolean) {
        _uiState.value = _uiState.value.copy(showOverlayPermissionDialog = show)
    }

    fun setShowLiveVoiceMode(show: Boolean) {
        _uiState.value = _uiState.value.copy(showLiveVoiceMode = show)
    }

    fun sendMessageToAi(prompt: String) {
        sendMessage(prompt)
    }

    fun setOverlayEnabled(enabled: Boolean) {
        repository.setOverlayActive(enabled)
        _uiState.value = _uiState.value.copy(isOverlayEnabled = enabled)
    }

    fun toggleVoiceOutput() {
        _uiState.value = _uiState.value.copy(isTtsVoiceOutputEnabled = !_uiState.value.isTtsVoiceOutputEnabled)
    }

    fun dismissClipboardSnippet() {
        clipboardAssistant.clearSnippet()
        _uiState.value = _uiState.value.copy(detectedClipboardText = null)
    }

    fun dismissSharedBanner() {
        _uiState.value = _uiState.value.copy(sharedIncomingBanner = null)
    }

    fun processClipboardWithLumi(snippet: String) {
        dismissClipboardSnippet()
        setSelectedTab(1) // Chat tab
        sendMessage("Here is text I copied: \"$snippet\". Please analyze it and summarize or extract actionable tasks.")
    }

    fun handleIncomingSharedText(text: String) {
        setSelectedTab(1) // Chat tab
        _uiState.value = _uiState.value.copy(sharedIncomingBanner = "Shared from another app: \"$text\"")
        sendMessage("I received this shared content from another app: \"$text\". Please summarize it and extract any tasks or calendar events.")
    }

    fun handleIncomingSharedImage(bitmap: Bitmap) {
        setSelectedTab(1) // Chat tab
        _uiState.value = _uiState.value.copy(sharedIncomingBanner = "Shared image received")
        sendMessage("Here is an image shared from another app. What do you see?", bitmap)
    }

    fun handleShortcutAction(action: String) {
        when (action) {
            "VOICE_CHAT" -> {
                setSelectedTab(1)
                startVoiceListening()
            }
            "QUICK_WATER" -> {
                logWellness(8, "Quick Hydration", 8, 1, "Quick hydration logged via App Shortcut")
                sensorsManager.vibrateCelebration()
            }
            "START_BREATHING" -> {
                setSelectedTab(0)
                setShowBreathing(true)
            }
            "TOGGLE_OVERLAY" -> {
                // Toggled in MainActivity / service
            }
        }
    }

    fun unlockMemoryVault() {
        biometricVault.authenticate(
            title = "Unlock Lumi Memory Vault",
            subtitle = "Verify biometric or device credential",
            onSuccess = {
                _uiState.value = _uiState.value.copy(isMemoryVaultUnlocked = true, vaultAuthError = null)
                sensorsManager.vibrateCelebration()
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(vaultAuthError = error)
                sensorsManager.vibratePurr()
            }
        )
    }

    fun lockMemoryVault() {
        _uiState.value = _uiState.value.copy(isMemoryVaultUnlocked = false)
    }

    fun onPetTouched() {
        sensorsManager.vibrateTap()
        viewModelScope.launch {
            repository.petTheCharacter()
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun onPetPetted() {
        sensorsManager.vibratePurr()
        viewModelScope.launch {
            repository.petTheCharacter()
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun sendMessage(text: String, image: Bitmap? = null) {
        if (text.isBlank() && image == null) return
        _uiState.value = _uiState.value.copy(inputText = "")
        viewModelScope.launch {
            val response = repository.sendMessage(text, image)
            if (_uiState.value.isTtsVoiceOutputEnabled && !zenStatus.value.isDndActive) {
                voiceEngine.speak(response.content)
            }
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun startVoiceListening() {
        voiceEngine.startListening { spokenText ->
            sendMessage(spokenText)
        }
    }

    fun stopVoiceListening() {
        voiceEngine.stopListening()
    }

    fun toggleTask(taskId: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            sensorsManager.vibrateCelebration()
            repository.toggleTaskCompleted(taskId, isCompleted)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun addTask(title: String, priority: String, category: String, estimatedMinutes: Int, notes: String) {
        viewModelScope.launch {
            repository.addTask(title, priority, category, estimatedMinutes, notes)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun addCalendarEvent(event: CalendarEventEntity) {
        viewModelScope.launch {
            val id = repository.addCalendarEvent(event)
            // Schedule Alarm for event
            if (event.startTimeMillis > System.currentTimeMillis()) {
                LumiAlarmScheduler.scheduleEventAlarm(
                    context = getApplication(),
                    eventId = id,
                    title = event.title,
                    description = event.description ?: "Upcoming event: ${event.title}",
                    triggerAtMillis = event.startTimeMillis - (10 * 60 * 1000L) // 10 min prior
                )
            }
        }
    }

    fun deleteCalendarEvent(id: Long) {
        viewModelScope.launch {
            LumiAlarmScheduler.cancelEventAlarm(getApplication(), id)
            repository.deleteCalendarEvent(id)
        }
    }

    fun logWellness(moodScore: Int, moodLabel: String, energyLevel: Int, hydrationCups: Int, gratitude: String) {
        viewModelScope.launch {
            repository.logWellness(moodScore, moodLabel, energyLevel, hydrationCups, gratitude)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun incrementHydration(logId: Long) {
        sensorsManager.vibrateTap()
        viewModelScope.launch {
            repository.incrementHydration(logId)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun setBloubShape(shape: com.example.domain.model.BloubShape) {
        sensorsManager.vibrateTap()
        viewModelScope.launch {
            repository.setBloubShape(shape)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun setBloubSkinColor(skinColor: com.example.domain.model.BloubSkinColor) {
        sensorsManager.vibrateTap()
        viewModelScope.launch {
            repository.setBloubSkinColor(skinColor)
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun feedPet() {
        sensorsManager.vibrateCelebration()
        viewModelScope.launch {
            repository.setPetEmotion(com.example.domain.model.PetEmotion.HAPPY)
            repository.setSpeechBubbleText("Yum! That was delicious! Full of fresh energy! 🍓✨")
            repository.petTheCharacter()
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun dancePet() {
        sensorsManager.vibrateCelebration()
        viewModelScope.launch {
            repository.setPetEmotion(com.example.domain.model.PetEmotion.ENERGETIC)
            repository.setSpeechBubbleText("Dancing time! Feel that rhythm! 🎶🕺✨")
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun pokePet() {
        sensorsManager.vibrateTap()
        viewModelScope.launch {
            repository.setPetEmotion(com.example.domain.model.PetEmotion.PLAYFUL)
            repository.setSpeechBubbleText("*Boing!* Hehe that tickles! 🫧")
            repository.petTheCharacter()
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun togglePetSleep() {
        sensorsManager.vibrateTap()
        viewModelScope.launch {
            val current = petStatus.value.currentEmotion
            if (current == com.example.domain.model.PetEmotion.SLEEPY) {
                repository.setPetEmotion(com.example.domain.model.PetEmotion.HAPPY)
                repository.setSpeechBubbleText("Good morning sunshine! Ready to shine! ☀️")
            } else {
                repository.setPetEmotion(com.example.domain.model.PetEmotion.SLEEPY)
                repository.setSpeechBubbleText("Time for cozy power nap... Zzz 😴")
            }
            LumiAppWidgetProvider.triggerWidgetUpdate(getApplication())
        }
    }

    fun startAudioReactiveMode() {
        audioReactiveEngine.startListening()
    }

    fun stopAudioReactiveMode() {
        audioReactiveEngine.stopListening()
    }

    fun refreshLocation() {
        if (locationEngine.checkPermission()) {
            locationEngine.startLocationUpdates()
        }
    }

    fun updateUserProfile(profile: com.example.domain.account.UserProfileData) {
        userProfileManager.updateProfile(profile)
        sensorsManager.vibrateTap()
    }

    fun addUserFact(category: String, factText: String, isPinned: Boolean = false) {
        userProfileManager.addUserFact(category, factText, isPinned)
        sensorsManager.vibrateTap()
    }

    fun removeUserFact(id: String) {
        userProfileManager.removeUserFact(id)
        sensorsManager.vibrateTap()
    }

    fun togglePinFact(id: String) {
        userProfileManager.togglePinFact(id)
        sensorsManager.vibrateTap()
    }

    fun resetUserProfile() {
        userProfileManager.resetToDefaults()
        sensorsManager.vibrateTap()
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
        sensorsManager.stopListening()
        batteryManager.stopListening()
        clipboardAssistant.stopListening()
        locationEngine.stopLocationUpdates()
        audioReactiveEngine.stopListening()
        networkEngine.stopListening()
        headsetManager.stopListening()
        zenManager.stopListening()
    }
}
