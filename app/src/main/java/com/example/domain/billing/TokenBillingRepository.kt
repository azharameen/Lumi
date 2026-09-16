package com.example.domain.billing

import kotlinx.coroutines.flow.Flow

interface TokenBillingRepository {
    val userAccountState: Flow<UserCreditAccount>
    suspend fun recordTokenUsage(tokensUsed: Int, isOfflineModel: Boolean = false): Boolean
    suspend fun resetQuotaForDebug()
}
