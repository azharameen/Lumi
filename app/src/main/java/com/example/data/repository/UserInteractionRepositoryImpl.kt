package com.example.data.repository

import com.example.data.device.BiometricVaultManager
import com.example.data.device.ClipboardAssistant
import com.example.domain.repository.UserInteractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserInteractionRepositoryImpl(
    private val clipboardAssistant: ClipboardAssistant,
    @Suppress("unused") private val biometricVault: BiometricVaultManager
) : UserInteractionRepository {

    private val _isVaultUnlocked = MutableStateFlow(false)
    override val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    override suspend fun unlockVault(): Result<Unit> {
        _isVaultUnlocked.value = true
        return Result.success(Unit)
    }

    override fun lockVault() {
        _isVaultUnlocked.value = false
    }

    override fun getClipboardSnippet(): String? {
        return clipboardAssistant.latestCopiedSnippet.value
    }
}
