package com.example.presentation.viewmodel

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.example.data.firebase.LumiPerformanceManager
import com.example.data.remote.OnDeviceGemmaEngine
import com.example.domain.account.UserProfileRepository
import com.example.domain.briefing.BriefingType
import com.example.domain.briefing.DailyBriefing
import com.example.domain.model.ChatMessage
import com.example.domain.prompt.DynamicPromptSuggester
import com.example.domain.repository.ChatRepository
import com.example.domain.repository.PetRepository
import com.example.domain.service.AnalyticsService
import com.example.domain.usecase.device.ManageAudioPerceptionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ChatViewModel(
    val chatRepository: ChatRepository,
    val petRepository: PetRepository,
    val manageAudioPerceptionUseCase: ManageAudioPerceptionUseCase,
    val userProfileManager: UserProfileRepository,
    private val analytics: AnalyticsService? = null,
    private val performance: LumiPerformanceManager? = null,
    private val onDeviceGemmaEngine: OnDeviceGemmaEngine? = null
) : ViewModel() {
    private val userProfile = userProfileManager.userProfile

    val pagedChatMessages = chatRepository.pagedChatMessages.cachedIn(viewModelScope)

    val chatMessages: StateFlow<List<ChatMessage>> = chatRepository.chatMessages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val streamingAiMessage: StateFlow<ChatMessage?> = chatRepository.streamingAiMessage.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), null
    )

    val isListening: StateFlow<Boolean> = manageAudioPerceptionUseCase.state.map { it.isListening }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isSpeaking: StateFlow<Boolean> = manageAudioPerceptionUseCase.state.map { it.isBriefingSpeaking }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val quickPrompts: StateFlow<List<String>> = combine(
        chatRepository.chatMessages,
        chatRepository.streamingAiMessage
    ) { messages: List<ChatMessage>, streaming: ChatMessage? ->
        Pair(messages, streaming)
    }.flatMapLatest { (messages, streaming) ->
        flow {
            val allMessages = if (streaming != null) messages + streaming else messages
            emit(DynamicPromptSuggester.getInitialPrompts(allMessages))
            if (streaming == null && allMessages.isNotEmpty() && allMessages.lastOrNull()?.sender != "user") {
                val aiSuggestions = DynamicPromptSuggester.getQuickPrompts(
                    recentMessages = allMessages.takeLast(4),
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

    private val _currentlySpeakingMessageId = MutableStateFlow<Long?>(null)
    val currentlySpeakingMessageId: StateFlow<Long?> = _currentlySpeakingMessageId.asStateFlow()

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
            manageAudioPerceptionUseCase.playBriefingAudio(DailyBriefing(title = "", greeting = text, dateFormatted = "", highlights = emptyList(), motivationalQuote = "", focusGoal = "", recommendedAction = "", recommendedActionType = "", audioScript = text, type = BriefingType.MORNING))
        }
    }

    fun stopSpeaking() {
        manageAudioPerceptionUseCase.stopBriefingAudio()
        _currentlySpeakingMessageId.value = null
    }

    fun toggleSpeakMessage(messageId: Long, text: String) {
        if (_currentlySpeakingMessageId.value == messageId && isSpeaking.value) {
            stopSpeaking()
        } else {
            _currentlySpeakingMessageId.value = messageId
            speakMessage(text)
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
                speakMessage(response.content)
            }
        }
    }
    
    fun sendMessageToAi(prompt: String) { sendMessage(prompt) }

    val selectedChatModelId: StateFlow<String> = userProfileManager.userProfile
        .map { it.selectedChatModelId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    fun setSelectedModel(modelId: String) {
        viewModelScope.launch {
            userProfileManager.updateProfile(
                userProfileManager.userProfile.value.copy(selectedChatModelId = modelId)
            )
        }
    }

    fun startVoiceListening() {
        viewModelScope.launch { petRepository.setListening(true) }
        manageAudioPerceptionUseCase.startVoiceListening()
    }

    fun stopVoiceListening() {
        viewModelScope.launch { petRepository.setListening(false) }
        manageAudioPerceptionUseCase.stopVoiceListening()
    }
}
