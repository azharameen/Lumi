package com.example.presentation.viewmodel
import com.example.domain.account.UserProfileRepository

import android.app.Application
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap.CompressFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiPerformanceManager
import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.PetRepository
import com.example.data.device.VoiceEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.paging.cachedIn
import kotlinx.coroutines.launch
import com.example.domain.prompt.DynamicPromptSuggester

class ChatViewModel(
    val chatRepository: ChatRepository, val petRepository: PetRepository,
    val voiceEngine: VoiceEngine,
    val userProfileManager: UserProfileRepository,
    private val analytics: LumiAnalyticsManager? = null,
    private val performance: LumiPerformanceManager? = null,
    private val onDeviceGemmaEngine: com.example.data.remote.OnDeviceGemmaEngine? = null
) : ViewModel() {
    private val userProfile = userProfileManager.userProfile

    val pagedChatMessages = chatRepository.pagedChatMessages.cachedIn(viewModelScope)

    val chatMessages: StateFlow<List<ChatMessageEntity>> = chatRepository.chatMessages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val streamingAiMessage: StateFlow<ChatMessageEntity?> = chatRepository.streamingAiMessage.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val quickPrompts: StateFlow<List<String>> = kotlinx.coroutines.flow.combine(
        chatRepository.chatMessages,
        chatRepository.streamingAiMessage
    ) { messages: List<ChatMessageEntity>, streaming: ChatMessageEntity? ->
        if (streaming != null) messages + streaming else messages
    }.flatMapLatest { combinedMessages ->
        flow {
            emit(DynamicPromptSuggester.getInitialPrompts(combinedMessages))
            if (combinedMessages.isNotEmpty()) {
                val aiSuggestions = DynamicPromptSuggester.getQuickPrompts(
                    recentMessages = combinedMessages.takeLast(4),
                    onDeviceGemmaEngine = onDeviceGemmaEngine
                )
                if (aiSuggestions.isNotEmpty()) {
                    emit(aiSuggestions)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DynamicPromptSuggester.getInitialPrompts())

    val pendingHitlActions = chatRepository.pendingHitlActions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _currentlySpeakingMessageId = kotlinx.coroutines.flow.MutableStateFlow<Long?>(null)
    val currentlySpeakingMessageId: StateFlow<Long?> = _currentlySpeakingMessageId.asStateFlow()

    init {
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { isSpeaking ->
                petRepository.setSpeaking(isSpeaking)
                if (!isSpeaking) {
                    _currentlySpeakingMessageId.value = null
                }
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearChatHistory()
        }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch {
            chatRepository.deleteMessage(id)
        }
    }

    fun speakMessage(text: String) {
        if (text.isBlank()) {
            stopSpeaking()
        } else {
            voiceEngine.speak(text)
        }
    }

    fun stopSpeaking() {
        voiceEngine.stopSpeaking()
        _currentlySpeakingMessageId.value = null
    }

    fun toggleSpeakMessage(messageId: Long, text: String) {
        if (_currentlySpeakingMessageId.value == messageId && voiceEngine.isSpeaking.value) {
            stopSpeaking()
        } else {
            _currentlySpeakingMessageId.value = messageId
            voiceEngine.speak(text) {
                _currentlySpeakingMessageId.value = null
            }
        }
    }

    fun resolveHitlAction(stateId: String, approved: Boolean) {
        viewModelScope.launch {
            chatRepository.resolveHitlAction(stateId, approved)
        }
    }

    fun sendMessage(text: String, image: Bitmap? = null) {
        viewModelScope.launch {
            val modelId = userProfile.value.selectedChatModelId.ifEmpty { null }
            analytics?.logAiChatMessage(
                mode = if (image != null) "multimodal_vision" else "text",
                messageLength = text.length,
                modelUsed = modelId ?: "auto"
            )
            val response = if (performance != null) {
                performance.traceAsync(LumiPerformanceManager.TRACE_AI_INFERENCE) {
                    val imageBytes = image?.let { val stream = ByteArrayOutputStream(); it.compress(CompressFormat.JPEG, 80, stream); stream.toByteArray() }
                    chatRepository.sendMessage(text, imageBytes, modelId)
                }
            } else {
                val imageBytes = image?.let { val stream = ByteArrayOutputStream(); it.compress(CompressFormat.JPEG, 80, stream); stream.toByteArray() }
                chatRepository.sendMessage(text, imageBytes, modelId)
            }
            if (userProfileManager.userProfile.value.enableSpeechOutput) {
                _currentlySpeakingMessageId.value = response.id
                voiceEngine.speak(response.content) {
                    _currentlySpeakingMessageId.value = null
                }
            }
        }
    }
    
    fun sendMessageToAi(prompt: String) { sendMessage(prompt) }

    /** Exposes the currently selected chat model ID. Empty string means Auto. */
    val selectedChatModelId: StateFlow<String> = userProfileManager.userProfile
        .map { it.selectedChatModelId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /** Updates the selected chat model and persists the preference. */
    fun setSelectedModel(modelId: String) {
        viewModelScope.launch {
            userProfileManager.updateProfile(
                userProfileManager.userProfile.value.copy(selectedChatModelId = modelId)
            )
        }
    }

    fun startVoiceListening() {
        viewModelScope.launch { petRepository.setListening(true) }
        voiceEngine.startListening { text ->
            viewModelScope.launch { petRepository.setListening(false) }
            if (text.isNotBlank()) {
                sendMessage(text)
            }
        }
    }

    fun stopVoiceListening() {
        viewModelScope.launch { petRepository.setListening(false) }
        voiceEngine.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}



