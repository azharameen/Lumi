package com.example.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface UserInteractionRepository {
    val isVaultUnlocked: StateFlow<Boolean>
    suspend fun unlockVault(): Result<Unit>
    fun lockVault()
    fun getClipboardSnippet(): String?
}
