package com.example.domain.billing

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.dataStore by preferencesDataStore(name = "lumi_billing_prefs")

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

/**
 * Enterprise Token & Credits Management Engine.
 * Tracks daily user token balance, calculates usage cost, enforces daily quotas,
 * and alerts users before exhausting API bandwidth.
 */
class TokenBillingManager(private val context: Context) {

    companion object {
        private val KEY_REMAINING_TOKENS = intPreferencesKey("remaining_tokens_today")
        private val KEY_LAST_RESET_DATE = longPreferencesKey("last_reset_timestamp")
        private val KEY_LIFETIME_TOKENS = longPreferencesKey("lifetime_tokens_used")
        const val DEFAULT_DAILY_QUOTA = 50_000
    }

    val userAccountState: Flow<UserCreditAccount> = context.dataStore.data.map { prefs ->
        val lastReset = prefs[KEY_LAST_RESET_DATE] ?: 0L
        val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val lastResetStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(lastReset))

        val remaining = if (todayStr != lastResetStr) {
            DEFAULT_DAILY_QUOTA
        } else {
            prefs[KEY_REMAINING_TOKENS] ?: DEFAULT_DAILY_QUOTA
        }

        val lifetimeTokens = prefs[KEY_LIFETIME_TOKENS] ?: 0L
        val estCost = (lifetimeTokens / 1_000_000.0) * 0.15 // $0.15 per million tokens avg

        val status = when {
            remaining <= 0 -> BillingStatus.QUOTA_EXHAUSTED
            remaining < 5_000 -> BillingStatus.LOW_CREDITS_WARNING
            else -> BillingStatus.ACTIVE_QUOTA_OK
        }

        UserCreditAccount(
            dailyFreeQuota = DEFAULT_DAILY_QUOTA,
            remainingTokensToday = remaining,
            totalTokensUsedLifetime = lifetimeTokens,
            estimatedCostUsdLifetime = estCost,
            billingStatus = status
        )
    }

    /**
     * Deducts tokens used in a conversational turn and returns whether execution is allowed.
     */
    suspend fun recordTokenUsage(tokensUsed: Int, isOfflineModel: Boolean = false): Boolean {
        if (isOfflineModel) return true // Local Gemma on-device runs at $0 cost and unlimited tokens

        var isAllowed = true
        context.dataStore.edit { prefs ->
            val lastReset = prefs[KEY_LAST_RESET_DATE] ?: 0L
            val todayStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val lastResetStr = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(lastReset))

            var currentRemaining = if (todayStr != lastResetStr) {
                prefs[KEY_LAST_RESET_DATE] = System.currentTimeMillis()
                DEFAULT_DAILY_QUOTA
            } else {
                prefs[KEY_REMAINING_TOKENS] ?: DEFAULT_DAILY_QUOTA
            }

            if (currentRemaining <= 0) {
                isAllowed = false
            } else {
                currentRemaining = (currentRemaining - tokensUsed).coerceAtLeast(0)
                prefs[KEY_REMAINING_TOKENS] = currentRemaining
                val currentLifetime = prefs[KEY_LIFETIME_TOKENS] ?: 0L
                prefs[KEY_LIFETIME_TOKENS] = currentLifetime + tokensUsed
            }
        }
        return isAllowed
    }

    suspend fun resetQuotaForDebug() {
        context.dataStore.edit { prefs ->
            prefs[KEY_REMAINING_TOKENS] = DEFAULT_DAILY_QUOTA
            prefs[KEY_LAST_RESET_DATE] = System.currentTimeMillis()
        }
    }
}
