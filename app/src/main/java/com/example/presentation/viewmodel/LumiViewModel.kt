package com.example.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.device.BatteryStatus
import com.example.data.device.LocationContext
import com.example.data.device.NetworkStatus
import com.example.data.remote.AiRoutingMode
import com.example.domain.account.UserProfileRepository
import com.example.domain.agent.AgentStatus
import com.example.domain.agent.AgentStreamEvent
import com.example.domain.briefing.BriefingType
import com.example.domain.briefing.DailyBriefing
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.PetRepository
import com.example.domain.repository.WellnessRepository
import com.example.domain.service.DeviceSensorsService
import com.example.domain.usecase.chat.SendMessageUseCase
import com.example.domain.usecase.device.HandleUserInteractionUseCase
import com.example.domain.usecase.device.ManageAudioPerceptionUseCase
import com.example.domain.usecase.device.ObserveEnvironmentUseCase
import com.example.domain.usecase.pet.PetInteractionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

data class LumiUiState(
    val selectedTab: Int = 0,
    val lifeHubSubTab: Int = 0,
    val isOverlayEnabled: Boolean = false,
    val showCameraDialog: Boolean = false,
    val showOverlayPermissionDialog: Boolean = false,
    val showWardrobeScreen: Boolean = false,
    val isTtsVoiceOutputEnabled: Boolean = true,
    val inputText: String = "",
    val detectedClipboardText: String? = null,
    val isMemoryVaultUnlocked: Boolean = false,
    val vaultAuthError: String? = null,
    val sharedIncomingBanner: String? = null,
    val agentThought: String? = null,
    val executingTool: String? = null,
    val completedTools: List<String> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class LumiViewModel(
    private val petRepository: PetRepository,
    private val chatRepository: ChatRepository,
    private val wellnessRepository: WellnessRepository,
    private val sendMessageUseCase: SendMessageUseCase,
    private val petInteractionUseCase: PetInteractionUseCase,
    val userProfileManager: UserProfileRepository,
    observeEnvironmentUseCase: ObserveEnvironmentUseCase,
    private val manageAudioPerceptionUseCase: ManageAudioPerceptionUseCase,
    private val handleUserInteractionUseCase: HandleUserInteractionUseCase,
    val sensorsManager: DeviceSensorsService
) : ViewModel() {
    val userProfile = userProfileManager.userProfile
    val userFacts = userProfileManager.userFacts
    
    private val _uiState = MutableStateFlow(LumiUiState())
    val uiState: StateFlow<LumiUiState> = _uiState.asStateFlow()

    val chatMessages = chatRepository.chatMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiRoutingMode = chatRepository.aiRoutingMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AiRoutingMode.HYBRID_AUTO)

    val petStatus = petRepository.petStatus.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PetStatus())

    private val _benchmarkStatus = MutableStateFlow<String?>(null)
    val benchmarkStatus: StateFlow<String?> = _benchmarkStatus.asStateFlow()

    val environmentState = observeEnvironmentUseCase()
    val batteryStatus: StateFlow<BatteryStatus> = environmentState.map { it.battery }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BatteryStatus())
    val networkStatus: StateFlow<NetworkStatus> = environmentState.map { it.network }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkStatus())
    val locationState: StateFlow<LocationContext> = environmentState.map { it.location }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocationContext())

    init {
        sensorsManager.startListening(
            onShake = {
                viewModelScope.launch {
                    sensorsManager.vibratePurr()
                    petRepository.setPetEmotion(PetEmotion.PLAYFUL)
                }
            }
        )
        viewModelScope.launch {
            batteryStatus.collect { status ->
                if (status.isCharging) {
                    petRepository.setPetEmotion(PetEmotion.ENERGETIC)
                } else if (status.isLow) {
                    petRepository.setPetEmotion(PetEmotion.SLEEPY)
                }
            }
        }
        viewModelScope.launch {
            handleUserInteractionUseCase.getClipboardSnippet()?.let { snippet ->
                _uiState.update { it.copy(detectedClipboardText = snippet) }
            }
        }
        viewModelScope.launch {
            networkStatus.collect { net ->
                if (!net.isConnected) {
                    petRepository.setPetEmotion(PetEmotion.THINKING)
                }
            }
        }
        viewModelScope.launch {
            userProfile.collect { profile ->
                _uiState.update { it.copy(isOverlayEnabled = profile.enableOverlay) }
            }
        }
        viewModelScope.launch {
            petRepository.isOverlayActive.collect { active ->
                _uiState.update { it.copy(isOverlayEnabled = active) }
            }
        }
        viewModelScope.launch {
            chatRepository.agentThoughts.collect { thought ->
                _uiState.update { it.copy(agentThought = thought) }
            }
        }
        viewModelScope.launch {
            chatRepository.agentStreamEvents.collect { event ->
                when (event) {
                    is AgentStreamEvent.ToolExecuting -> {
                        _uiState.update { it.copy(executingTool = event.toolName) }
                    }
                    is AgentStreamEvent.ToolCompleted -> {
                        _uiState.update { state ->
                            state.copy(
                                executingTool = null,
                                completedTools = state.completedTools + event.toolName
                            )
                        }
                    }
                    is AgentStreamEvent.StatusChanged -> {
                        if (event.status == AgentStatus.COMPLETED || event.status == AgentStatus.FAILED) {
                            _uiState.update { it.copy(executingTool = null) }
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun setShowWardrobeScreen(show: Boolean) { _uiState.value = _uiState.value.copy(showWardrobeScreen = show) }
    fun setSelectedTab(tab: Int) { _uiState.value = _uiState.value.copy(selectedTab = tab) }
    fun setLifeHubSubTab(tab: Int) { _uiState.value = _uiState.value.copy(lifeHubSubTab = tab) }
    fun navigateToLifeHub(subTab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = 1, lifeHubSubTab = subTab)
    }
    fun setInputText(text: String) { _uiState.value = _uiState.value.copy(inputText = text) }
    fun setShowCamera(show: Boolean) { _uiState.value = _uiState.value.copy(showCameraDialog = show) }
    fun setShowOverlayPermission(show: Boolean) { _uiState.value = _uiState.value.copy(showOverlayPermissionDialog = show) }
    fun setOverlayEnabled(enabled: Boolean) { 
        _uiState.value = _uiState.value.copy(isOverlayEnabled = enabled) 
        petRepository.setOverlayActive(enabled)
        viewModelScope.launch {
            userProfileManager.updateField { it.copy(enableOverlay = enabled) }
        }
    }
    fun toggleVoiceOutput() { _uiState.value = _uiState.value.copy(isTtsVoiceOutputEnabled = !_uiState.value.isTtsVoiceOutputEnabled) }
    fun dismissClipboardSnippet() { _uiState.value = _uiState.value.copy(detectedClipboardText = null) }

    fun processClipboardWithLumi(snippet: String) {
        dismissClipboardSnippet()
        sendMessage("Analyze this clipboard text: $snippet")
    }
    fun handleIncomingSharedText(text: String) {
        _uiState.value = _uiState.value.copy(sharedIncomingBanner = "Shared: $text")
        sendMessage("User shared text: $text")
    }
    fun handleIncomingSharedImage(bitmap: Bitmap) { sendMessage("User shared an image", bitmap) }
    fun handleShortcutAction(action: String) {
        when(action) {
            "ACTION_VOICE" -> startVoiceListening()
            "ACTION_TASK" -> navigateToLifeHub(1)
        }
    }

    fun setAiRoutingMode(mode: AiRoutingMode) { chatRepository.setAiRoutingMode(mode) }
    fun clearAiAnalytics() { viewModelScope.launch { chatRepository.clearAiAnalyticsLogs() } }
    
    fun runGemmaBenchmark() {
        viewModelScope.launch {
            try {
                _benchmarkStatus.value = "Initializing engine..."
                chatRepository.benchmarkOnDeviceGemma()
                _benchmarkStatus.value = "Benchmark Complete"
            } catch (e: Exception) {
                _benchmarkStatus.value = "Benchmark Failed: ${e.localizedMessage}"
            }
        }
    }

    fun sendMessage(text: String, image: Bitmap? = null) {
        viewModelScope.launch {
            val imageBytes = image?.let { 
                val stream = ByteArrayOutputStream()
                it.compress(CompressFormat.JPEG, 80, stream)
                stream.toByteArray() 
            }
            val response = sendMessageUseCase(text, imageBytes)
            if (userProfile.value.enableSpeechOutput) {
                manageAudioPerceptionUseCase.playBriefingAudio(DailyBriefing(title = "Response", greeting = response.content, dateFormatted = "", highlights = emptyList(), motivationalQuote = "", focusGoal = "", recommendedAction = "", recommendedActionType = "", audioScript = response.content, type = BriefingType.MORNING))
            }
        }
    }
    fun startVoiceListening() { 
        viewModelScope.launch { petRepository.setListening(true) }
        manageAudioPerceptionUseCase.startVoiceListening()
    } 

    fun logWellness(moodScore: Int, moodLabel: String, energyLevel: Int, hydrationCups: Int, gratitude: String) {
        viewModelScope.launch { wellnessRepository.logWellness(moodScore, moodLabel, energyLevel, hydrationCups, gratitude) }
    }

    fun unlockMemoryVault() {
        viewModelScope.launch {
            val result = handleUserInteractionUseCase.unlockVault()
            if (result.isSuccess) {
                _uiState.update { it.copy(isMemoryVaultUnlocked = true, vaultAuthError = null) }
            } else {
                _uiState.update { it.copy(vaultAuthError = result.exceptionOrNull()?.localizedMessage ?: "Failed") }
            }
        }
    }
    fun lockMemoryVault() {
        handleUserInteractionUseCase.lockVault()
        _uiState.update { it.copy(isMemoryVaultUnlocked = false) }
    }

    fun addUserFact(category: String, factText: String, isPinned: Boolean = false) {
        viewModelScope.launch { userProfileManager.addUserFact(category, factText, isPinned) }
    }
    fun removeUserFact(id: String) {
        viewModelScope.launch { userProfileManager.removeUserFact(id) }
    }
    fun togglePinFact(id: String) {
        viewModelScope.launch { userProfileManager.togglePinFact(id) }
    }

    fun petTheCharacter() {
        viewModelScope.launch {
            petInteractionUseCase.petTheAnimal()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorsManager.stopListening()
    }
}
