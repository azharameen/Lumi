package com.example.domain.usecase.chat

import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.repository.ChatRepository

class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(userText: String, image: ByteArray? = null): ChatMessageEntity {
        return chatRepository.sendMessage(userText, image)
    }
}
