package com.example.domain.usecase.chat

import com.example.domain.model.ChatMessage
import com.example.domain.repository.ChatRepository

class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userText: String, image: ByteArray? = null, modelId: String? = null): ChatMessage {
        return chatRepository.sendMessage(userText, image, modelId)
    }
}
