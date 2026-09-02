package com.example.presentation.home
import com.example.domain.model.AuthUser
import com.example.presentation.home.components.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.domain.account.UserProfileData
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.data.device.BatteryStatus
import com.example.data.device.LocationContext
import com.example.data.device.NetworkStatus
import com.example.data.device.NetworkType
import com.example.presentation.pet.LumiPetView
import com.example.core.theme.*
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.domain.model.LumiRemoteConfig
import org.koin.core.context.GlobalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.example.core.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    petStatus: PetStatus,
    uiState: com.example.presentation.viewmodel.LumiUiState,
    batteryStatus: BatteryStatus,
    networkStatus: NetworkStatus,
    events: List<CalendarEventEntity>,
    tasks: List<TaskEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    onPetPetted: () -> Unit,
    onPetTouched: () -> Unit,
    onTogglePetSleep: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onShowCamera: () -> Unit,
    onShowWardrobe: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToLifeHub: (Int) -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToWellness: () -> Unit,
    locationContext: LocationContext = LocationContext(),
    userProfile: UserProfileData = UserProfileData(),
    authUser: AuthUser? = null,
    onFeedPet: () -> Unit = {},
    onDancePet: () -> Unit = {},
    onPokePet: () -> Unit = {},
    onToggleTask: (Long, Boolean) -> Unit = { _, _ -> },
    onQuickAgentPrompt: (String) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val petPrimary = Color(petStatus.bloubSkinColor.primaryHex)

    var showQuestsBottomSheet by remember { mutableStateOf(false) }
    var interactionRewardEffect by remember { mutableStateOf<String?>(null) }

    val remoteConfigManager = remember {
        try { GlobalContext.get().get<LumiRemoteConfigManager>() } catch (_: Exception) { null }
    }
    val remoteConfig = remoteConfigManager?.config?.collectAsStateWithLifecycle(initialValue = LumiRemoteConfig())?.value

    // 1. Optimized Animation Definitions
    val infiniteTransition = rememberInfiniteTransition(label = "HomeAura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "BgPulse"
    )
    val neonAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f, targetValue = 0.40f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Reverse),
        label = "NeonAlpha"
    )

    fun triggerReward(text: String) {
        interactionRewardEffect = text
        coroutineScope.launch { delay(1600); if (interactionRewardEffect == text) interactionRewardEffect = null }
    }

    // 2. Immersive Background
    Box(modifier = Modifier.fillMaxSize().drawWithCache {
        onDrawBehind {
            drawRect(Brush.radialGradient(
                colors = listOf(petPrimary.copy(alpha = neonAlpha), SpaceDark, ObsidianDark),
                center = Offset(size.width / 2f, size.height * 0.3f),
                radius = size.maxDimension * 0.7f
            ))
        }
    }) {
        CyberAmbientStarsBackground(primaryColor = petPrimary)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MaterialTheme.spacing.medium)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3. HUD Section
            SeamlessRpgPlayerHud(
                petStatus = petStatus, batteryStatus = batteryStatus, networkStatus = networkStatus,
                locationContext = locationContext, userProfile = userProfile, authUser = authUser,
                onNavigateToAccount = onNavigateToAccount
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Interaction Feedback Section
            MinimalPetSpeechCard(petStatus = petStatus, petPrimary = petPrimary, onClick = onNavigateToChat)

            if (remoteConfig != null && (remoteConfig.specialEventBannerText.isNotBlank() || remoteConfig.seasonalThemeEnabled)) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                RemoteConfigSeasonalBanner(
                    bannerText = if (remoteConfig.specialEventBannerText.isNotBlank()) remoteConfig.specialEventBannerText else "Enjoy the ${remoteConfig.seasonalThemeName}!",
                    seasonalThemeName = if (remoteConfig.seasonalThemeEnabled) remoteConfig.seasonalThemeName else "",
                    petPrimary = petPrimary, onClick = onNavigateToChat
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            // 5. Mascot & Pet Care Area
            MascotInteractionArea(
                petStatus = petStatus, petPrimary = petPrimary, pulseScale = pulseScale,
                rewardEffect = interactionRewardEffect, tasksCount = tasks.count { !it.isCompleted },
                onPetTouched = onPetTouched, onPetPetted = onPetPetted, onShowWardrobe = onShowWardrobe,
                onShowQuests = { showQuestsBottomSheet = true }, onFeed = onFeedPet, onDance = onDancePet,
                onPoke = onPokePet, onSleepToggle = onTogglePetSleep, triggerReward = ::triggerReward
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            // 6. Bottom Controls
            PetBottomControlsRow(
                isListening = isListening, isSpeaking = isSpeaking, petPrimary = petPrimary,
                voiceWavePulse = 1.0f, onShowCamera = onShowCamera, onStartVoiceListening = onStartVoiceListening,
                onStopVoiceListening = onStopVoiceListening, onNavigateToChat = onNavigateToChat
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 7. Core App Destinations
            CoreAppGridSection(onNavigateToLifeHub, onNavigateToWellness)

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
        }

        // 8. Quests Overlay
        if (showQuestsBottomSheet) {
            QuestsBottomSheet(
                tasks = tasks, petPrimary = petPrimary,
                onDismiss = { showQuestsBottomSheet = false }, onToggleTask = onToggleTask,
                onNavigateToLifeHub = { onNavigateToLifeHub(1); showQuestsBottomSheet = false },
                triggerReward = ::triggerReward
            )
        }
    }
}

@Composable
private fun MascotInteractionArea(
    petStatus: PetStatus,
    petPrimary: Color,
    pulseScale: Float,
    rewardEffect: String?,
    tasksCount: Int,
    onPetTouched: () -> Unit,
    onPetPetted: () -> Unit,
    onShowWardrobe: () -> Unit,
    onShowQuests: () -> Unit,
    onFeed: () -> Unit,
    onDance: () -> Unit,
    onPoke: () -> Unit,
    onSleepToggle: () -> Unit,
    triggerReward: (String) -> Unit
) {
    val haptics = com.example.core.utils.rememberLumiHaptics()
    var isCareFabExpanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth().height(280.dp), contentAlignment = Alignment.Center) {
        HoloPedestalPlatform(petColor = petPrimary, pulseScale = pulseScale, modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-6).dp))

        LumiPetView(
            petStatus = petStatus, size = 255.dp,
            onPetTouched = { onPetTouched(); haptics.performTick(); triggerReward("+10 Bond ✨") },
            onPetPetted = { onPetPetted(); haptics.performSuccess(); triggerReward("+25 Bond 💖") },
            modifier = Modifier.testTag("lumi_pet_view").align(Alignment.Center)
        )

        PetCareFixedAnchorFab(
            isExpanded = isCareFabExpanded, onToggleExpand = { isCareFabExpanded = !isCareFabExpanded },
            petEmotion = petStatus.currentEmotion, petPrimary = petPrimary,
            onFeed = { onFeed(); haptics.performHeavyClick(); triggerReward("+15 Energy 🍓") },
            onPet = { onPetPetted(); haptics.performSuccess(); triggerReward("+20 Bond 💖") },
            onDance = { onDance(); haptics.performSuccess(); triggerReward("Dance Party! 🎵") },
            onPoke = { onPoke(); haptics.performTick(); triggerReward("Giggle! ⚡") },
            onSleepToggle = { onSleepToggle(); val mode = if (petStatus.currentEmotion == PetEmotion.SLEEPY) "Awake! ☀️" else "Nap Mode 🌙"; triggerReward(mode) },
            modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp, top = MaterialTheme.spacing.medium)
        )

        PetRightSideActionButtons(
            pendingQuestsCount = tasksCount, onShowWardrobe = onShowWardrobe, onShowQuests = onShowQuests,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 2.dp, top = MaterialTheme.spacing.medium)
        )

        AnimatedVisibility(visible = rewardEffect != null, enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut(), modifier = Modifier.align(Alignment.TopCenter).offset(y = 6.dp)) {
            rewardEffect?.let { text ->
                Surface(color = petPrimary.copy(alpha = 0.92f), shape = RoundedCornerShape(12.dp), shadowElevation = MaterialTheme.spacing.small) {
                    Text(text = text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun CoreAppGridSection(onNavigateToLifeHub: (Int) -> Unit, onNavigateToWellness: () -> Unit) {
    val haptics = com.example.core.utils.rememberLumiHaptics()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppButton(
            title = stringResource(R.string.text_life_hub), subtitle = stringResource(R.string.text_goals_schedule),
            icon = Icons.Default.Dashboard, color = LumiGold, onClick = { haptics.performTick(); onNavigateToLifeHub(0) },
            modifier = Modifier.weight(1f)
        )
        AppButton(
            title = stringResource(R.string.text_wellness), subtitle = stringResource(R.string.text_zen_vitality),
            icon = Icons.Default.SelfImprovement, color = LumiPink, onClick = { haptics.performTick(); onNavigateToWellness() },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AppButton(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.88f), shape = RoundedCornerShape(MaterialTheme.spacing.medium),
        border = BorderStroke(1.2.dp, color.copy(alpha = 0.6f)), shadowElevation = MaterialTheme.spacing.extraSmall,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = color.copy(alpha = 0.18f), shape = CircleShape, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp)) }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 10.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestsBottomSheet(
    tasks: List<TaskEntity>,
    petPrimary: Color,
    onDismiss: () -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onNavigateToLifeHub: () -> Unit,
    triggerReward: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss, containerColor = SurfaceDark, contentColor = TextPrimary,
        scrimColor = Color.Black.copy(alpha = 0.65f), shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = LumiGold, modifier = Modifier.size(MaterialTheme.spacing.large))
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(stringResource(R.string.text_active_quests_bounties), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Surface(color = SurfaceHighlight.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp)) {
                    Text("${tasks.count { !it.isCompleted }} Active", color = LumiCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = MaterialTheme.spacing.extraSmall))
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            val pending = tasks.filter { !it.isCompleted }
            if (pending.isEmpty()) {
                EmptyQuestsPlaceholder()
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(pending) { task ->
                        QuestItemRow(task = task, onToggle = { isChecked -> onToggleTask(task.id, isChecked); if (isChecked) triggerReward("+50 XP Claimed! 🏆") })
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Button(
                onClick = onNavigateToLifeHub, modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkVariant),
                shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, petPrimary.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = petPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(stringResource(R.string.text_create_new_quest_in_life_hub), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }
    }
}

@Composable
private fun EmptyQuestsPlaceholder() {
    Surface(color = SurfaceDarkVariant.copy(alpha = 0.6f), shape = RoundedCornerShape(MaterialTheme.spacing.medium), modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.large)) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.large), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LumiGreen, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Text(stringResource(R.string.text_all_quests_cleared), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(stringResource(R.string.text_your_companion_is_thrilled_che), color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}
