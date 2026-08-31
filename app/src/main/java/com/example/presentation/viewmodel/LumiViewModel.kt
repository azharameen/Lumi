package com.example.presentation.viewmodel
import com.example.domain.account.UserProfileManager
import com.example.data.remote.ModelDownloadManager

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.briefing.AutonomousBriefingEngine
import com.example.domain.briefing.BriefingType
import com.example.domain.briefing.DailyBriefing
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.repository.LumiRepository
import com.example.data.device.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LumiUiState(
    val selectedTab: Int = 0,
    val lifeHubSubTab: Int = 0,
    val isOverlayEnabled: Boolean = false,
    val showCameraDialog: Boolean = false,
    val showBreathingDialog: Boolean = false,
    val showOverlayPermissionDialog: Boolean = false,
    val showWardrobeScreen: Boolean = false,
    val isTtsVoiceOutputEnabled: Boolean = true,
    val inputText: String = "",
    val detectedClipboardText: String? = null,
    val isMemoryVaultUnlocked: Boolean = false,
    val vaultAuthError: String? = null,
    val sharedIncomingBanner: String? = null
)

class LumiViewModel(
    val repository: LumiRepository,
    val userProfileManager: UserProfileManager,
    val voiceEngine: VoiceEngine,
    val sensorsManager: SensorsManager,
    val batteryManager: BatteryStatusManager,
    val locationEngine: ContextLocationEngine,
    val clipboardAssistant: ClipboardAssistant,
    val audioReactiveEngine: RealtimeAudioReactiveEngine,
    val networkEngine: NetworkConnectivityEngine,
    val headsetManager: AudioHeadsetManager,
    val zenManager: ZenModeManager,
    val biometricVault: BiometricVaultManager,
    val briefingEngine: AutonomousBriefingEngine
) : ViewModel() {
                val userProfile = userProfileManager.userProfile
    val userFacts = userProfileManager.userFacts
    
                                                private val _uiState = MutableStateFlow(LumiUiState())
    val uiState: StateFlow<LumiUiState> = _uiState.asStateFlow()

    
    
    
    
    private val _isBriefingSpeaking = MutableStateFlow(false)
    val isBriefingSpeaking: StateFlow<Boolean> = _isBriefingSpeaking.asStateFlow()

    
    
    
    
    val allWellnessLogs = repository.allWellnessLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allMemories = repository.allMemories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    
    val chatMessages = repository.chatMessages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiExecutionLogs = repository.aiExecutionLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val aiRoutingMode = repository.aiRoutingMode.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.example.data.remote.AiRoutingMode.HYBRID_AUTO)

        
    private val _benchmarkStatus = MutableStateFlow<String?>(null)
    val benchmarkStatus: StateFlow<String?> = _benchmarkStatus.asStateFlow()

    val batteryStatus = batteryManager.batteryStatus
    val networkStatus = networkEngine.networkStatus
    val headsetStatus = headsetManager.headsetStatus
    val zenStatus = zenManager.zenStatus
    val locationState = locationEngine.locationState

    init {
        locationEngine.startLocationUpdates()
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { isSpeaking ->
                repository.setSpeaking(isSpeaking)
            }
        }
        sensorsManager.startListening(
            onShake = {
                viewModelScope.launch {
                    sensorsManager.vibratePurr()
                    repository.setPetEmotion(PetEmotion.PLAYFUL)
                }
            }
        )
        batteryManager.startListening { status ->
            viewModelScope.launch {
                if (status.isCharging) {
                    repository.setPetEmotion(PetEmotion.ENERGETIC)
                } else if (status.isLow) {
                    repository.setPetEmotion(PetEmotion.SLEEPY)
                }
            }
        }
        clipboardAssistant.startListening { snippet ->
            _uiState.value = _uiState.value.copy(detectedClipboardText = snippet)
        }
        networkEngine.startListening { net ->
            if (!net.isConnected) {
                viewModelScope.launch {
                    repository.setPetEmotion(PetEmotion.THINKING)
                }
            }
        }
        viewModelScope.launch {
            userProfile.collect { profile ->
                _uiState.update { it.copy(isOverlayEnabled = profile.enableOverlay) }
            }
        }
        
    }

    fun setShowWardrobeScreen(show: Boolean) { _uiState.value = _uiState.value.copy(showWardrobeScreen = show) }
    fun setSelectedTab(tab: Int) { _uiState.value = _uiState.value.copy(selectedTab = tab) }
    fun setLifeHubSubTab(subTab: Int) { _uiState.value = _uiState.value.copy(lifeHubSubTab = subTab) }
    fun navigateToLifeHub(subTab: Int = 0) {
        _uiState.value = _uiState.value.copy(selectedTab = 2, lifeHubSubTab = subTab)
    }
    fun setInputText(text: String) { _uiState.value = _uiState.value.copy(inputText = text) }
    fun setShowCamera(show: Boolean) { _uiState.value = _uiState.value.copy(showCameraDialog = show) }
    fun setShowBreathing(show: Boolean) { _uiState.value = _uiState.value.copy(showBreathingDialog = show) }
    fun setShowOverlayPermission(show: Boolean) { _uiState.value = _uiState.value.copy(showOverlayPermissionDialog = show) }
    fun setOverlayEnabled(enabled: Boolean) { 
        _uiState.value = _uiState.value.copy(isOverlayEnabled = enabled) 
        viewModelScope.launch {
            userProfileManager.updateField { it.copy(enableOverlay = enabled) }
        }
    }
    fun toggleVoiceOutput() { _uiState.value = _uiState.value.copy(isTtsVoiceOutputEnabled = !_uiState.value.isTtsVoiceOutputEnabled) }
    fun dismissClipboardSnippet() { _uiState.value = _uiState.value.copy(detectedClipboardText = null) }
    fun dismissSharedBanner() { _uiState.value = _uiState.value.copy(sharedIncomingBanner = null) }

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




    
    
    
    

    
    
    


    
    

    

    fun logWellness(moodScore: Int, moodLabel: String, energyLevel: Int, hydrationCups: Int, gratitude: String) {
        viewModelScope.launch { repository.logWellness(moodScore, moodLabel, energyLevel, hydrationCups, gratitude) }
    }
    fun incrementHydration(logId: Long) {
        viewModelScope.launch { repository.incrementHydration(logId) }
    }







    fun setAiRoutingMode(mode: com.example.data.remote.AiRoutingMode) { repository.setAiRoutingMode(mode) }
    fun clearAiAnalytics() { viewModelScope.launch { repository.clearAiAnalyticsLogs() } }
    fun runGemmaBenchmark() {
        viewModelScope.launch {
            _benchmarkStatus.value = "Running Benchmark..."
            _benchmarkStatus.value = "Benchmark Complete"
        }
    }



    
    
    
    
    


    fun sendMessage(text: String, image: Bitmap? = null) {
        viewModelScope.launch {
            val response = repository.sendMessage(text, image)
            if (userProfile.value.enableSpeechOutput) {
                voiceEngine.speak(response.content)
            }
        }
    }
    fun sendMessageToAi(prompt: String) { sendMessage(prompt) }
    fun startVoiceListening() { 
        viewModelScope.launch { repository.setListening(true) }
        voiceEngine.startListening { text -> 
            viewModelScope.launch { repository.setListening(false) }
            if (text.isNotBlank()) {
                sendMessageToAi(text)
            }
        } 
    } 
    fun stopVoiceListening() { 
        viewModelScope.launch { repository.setListening(false) }
        voiceEngine.stopListening() 
    }
    fun startAudioReactiveMode() { audioReactiveEngine.startListening() }
    fun stopAudioReactiveMode() { audioReactiveEngine.stopListening() }

    fun unlockMemoryVault() {
        biometricVault.authenticate(
            title = "Unlock Biometric Vault",
            subtitle = "Verify identity to access private wellness logs and memories",
            onSuccess = {
                _uiState.value = _uiState.value.copy(isMemoryVaultUnlocked = true, vaultAuthError = null)
            },
            onError = { err ->
                _uiState.value = _uiState.value.copy(vaultAuthError = err)
            }
        )
    }
    fun lockMemoryVault() {
        _uiState.value = _uiState.value.copy(isMemoryVaultUnlocked = false)
    }


    fun playBriefingAudio(briefing: DailyBriefing) {} 
     
    fun stopBriefingAudio() {} 


    fun addUserFact(category: String, factText: String, isPinned: Boolean = false) {
        viewModelScope.launch { userProfileManager.addUserFact(category, factText, isPinned) }
    }
    fun removeUserFact(id: String) {
        viewModelScope.launch { userProfileManager.removeUserFact(id) }
    }
    fun togglePinFact(id: String) {
        viewModelScope.launch { userProfileManager.togglePinFact(id) }
    }
    fun resetUserProfile() {
        viewModelScope.launch { userProfileManager.resetToDefaults() }
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
        sensorsManager.stopListening()
        batteryManager.stopListening()
        clipboardAssistant.stopListening()
        audioReactiveEngine.stopListening()
        networkEngine.stopListening()
        headsetManager.stopListening()
        zenManager.stopListening()
    }
}

