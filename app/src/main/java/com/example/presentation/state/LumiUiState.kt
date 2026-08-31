package com.example.presentation.state

import com.example.data.remote.AiRoutingMode
import com.example.data.remote.HardwareAccelerator
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadProgress
import com.example.domain.account.UserProfileData
import com.example.domain.model.CalendarEvent
import com.example.domain.model.ChatMessage
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.domain.model.Task
import com.example.domain.model.UserFact

/**
 * Single Immutable Uni-directional MVI State object for Lumi App Presentation Layer.
 * Prevents race conditions, state fragmentation, and UI flickering.
 */
data class LumiUiState(
    val userProfile: UserProfileData = UserProfileData(),
    val petStatus: PetStatus = PetStatus(),
    val currentEmotion: PetEmotion = PetEmotion.HAPPY,
    val isThinking: Boolean = false,
    val isSpeaking: Boolean = false,
    val isListening: Boolean = false,
    val isOverlayActive: Boolean = false,
    val speechBubbleText: String? = "Hi! I'm Lumi, your personal AI friend ✨",
    val tasks: List<Task> = emptyList(),
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val userFacts: List<UserFact> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val aiRoutingMode: AiRoutingMode = AiRoutingMode.HYBRID_AUTO,
    val selectedAccelerator: HardwareAccelerator = HardwareAccelerator.GPU_OPENCL,
    val activeLocalModelId: String? = null,
    val benchmarkStatus: String = "Not executed yet",
    val localModelCatalog: List<LocalLlmModelSpec> = emptyList(),
    val modelDownloadStates: Map<String, ModelDownloadProgress> = emptyMap(),
    val errorMessage: String? = null
)
