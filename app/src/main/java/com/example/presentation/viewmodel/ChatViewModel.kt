package com.example.presentation.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.repository.LumiRepositoryImpl
import com.example.domain.repository.LumiRepository
import com.example.data.device.VoiceEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.paging.cachedIn
import kotlinx.coroutines.launch

class ChatViewModel(
    val repository: LumiRepository,
    val voiceEngine: VoiceEngine,
    val userProfileManager: UserProfileManager
) : ViewModel() {
                private     val userProfile = userProfileManager.userProfile

    val pagedChatMessages = repository.pagedChatMessages.cachedIn(viewModelScope)

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    init {
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { isSpeaking ->
                repository.setSpeaking(isSpeaking)
            }
        }
    }

    fun sendMessage(text: String, image: Bitmap? = null) {
        viewModelScope.launch {
            val response = repository.sendMessage(text, image)
            if (userProfileManager.userProfile.value.enableSpeechOutput) {
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
                sendMessage(text)
            }
        }
    }

    fun stopVoiceListening() {
        viewModelScope.launch { repository.setListening(false) }
        voiceEngine.stopListening()
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}


