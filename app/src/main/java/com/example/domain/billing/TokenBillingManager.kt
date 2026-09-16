package com.example.domain.billing

enum class BillingStatus {
    ACTIVE_UNLIMITED,
    ACTIVE_QUOTA_OK,
    LOW_CREDITS_WARNING,
    QUOTA_EXHAUSTED
}

data class UserCreditAccount(
    val dailyFreeQuota: Int = 50_000,
    val remainingTokensToday: Int = 50_000,
    val totalTokensUsedLifetime: Long = 0L,
    val estimatedCostUsdLifetime: Double = 0.0,
    val billingStatus: BillingStatus = BillingStatus.ACTIVE_QUOTA_OK,
    val tierName: String = "Free Tier"
)
