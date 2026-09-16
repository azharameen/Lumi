package com.example.domain.usecase.device

import com.example.domain.repository.UserInteractionRepository
import kotlinx.coroutines.flow.StateFlow

class HandleUserInteractionUseCase(
    private val repository: UserInteractionRepository
) {
    val isVaultUnlocked: StateFlow<Boolean> = repository.isVaultUnlocked

    suspend fun unlockVault(): Result<Unit> = repository.unlockVault()
    fun lockVault() = repository.lockVault()
    fun getClipboardSnippet(): String? = repository.getClipboardSnippet()
}
