package com.example.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.core.utils.rememberLumiHaptics
import com.example.presentation.screens.wellness.*
import com.example.presentation.viewmodel.LumiViewModel
import com.example.presentation.viewmodel.WellnessViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun WellnessScreen(
    haptics: LumiHaptics = rememberLumiHaptics(),
    logs: androidx.paging.compose.LazyPagingItems<com.example.domain.model.WellnessLog>,
    memories: List<com.example.domain.model.PetMemory>,
    isMemoryVaultUnlocked: Boolean,
    vaultAuthError: String?,
    onLogWellness: (Int, String, Int, Int, String) -> Unit,
    onIncrementHydration: (Long) -> Unit,
    onUnlockVault: () -> Unit,
    onLockVault: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateBack: () -> Unit
) {

    var moodScore by remember { mutableFloatStateOf(8f) }
    var energyLevel by remember { mutableFloatStateOf(7f) }
    var hydrationCups by remember { mutableIntStateOf(4) }
    var gratitudeText by remember { mutableStateOf("") }
    var isSubmittedToday by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .navigationBarsPadding()
            .padding(horizontal = MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        // Top Header
        item {
            WellnessHeader(
                onNavigateBack = onNavigateBack
            )
        }

        // Daily Check-In Interactive Card
        item {
            DailyCheckInCard(
                moodScore = moodScore,
                onMoodChange = { moodScore = it },
                energyLevel = energyLevel,
                onEnergyChange = { energyLevel = it },
                hydrationCups = hydrationCups,
                onHydrationChange = { hydrationCups = it },
                gratitudeText = gratitudeText,
                onGratitudeChange = { gratitudeText = it },
                isSubmittedToday = isSubmittedToday,
                onSubmit = {
                    val moodLabel = when {
                        moodScore >= 8 -> "Joyful & Centered"
                        moodScore >= 6 -> "Balanced & Calm"
                        moodScore >= 4 -> "Neutral"
                        else -> "Needs Rejuvenation"
                    }
                    onLogWellness(moodScore.toInt(), moodLabel, energyLevel.toInt(), hydrationCups, gratitudeText)
                    isSubmittedToday = true
                },
                haptics = haptics
            )
        }

        // Biometric Secured Private Memory & Reflection Vault
        item {
            BiometricMemoryVaultCard(
                isUnlocked = isMemoryVaultUnlocked,
                vaultAuthError = vaultAuthError,
                memories = memories,
                onUnlock = { onUnlockVault() },
                onLock = { onLockVault() }
            )
        }

        // Wellness Log History
        item {
            Text(
                text = stringResource(R.string.text_recent_wellness_logs),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (logs.itemCount == 0) {
            item {
                Text(
                    text = stringResource(R.string.text_no_previous_logs_complete_your_first),
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        } else {
            items(
                count = logs.itemCount,
                key = logs.itemKey { it.id },
                contentType = logs.itemContentType { "wellness_log" }
            ) { index ->
                val log = logs[index]
                if (log != null) {
                    WellnessLogItemCard(
                        log = log,
                        dateFormat = dateFormat,
                        onIncrementHydration = { onIncrementHydration(it) }
                    )
                }
            }
        }
    }
}
