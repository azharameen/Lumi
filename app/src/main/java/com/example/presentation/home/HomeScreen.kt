package com.example.presentation.home
import com.example.presentation.home.components.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Gamified Agentic AI Command Center.
 * - Top: Seamless RPG Player HUD Banner:
 *     - Avatar on left with solid brown core & status beacon.
 *     - Attached dual gauge extending directly from avatar: HP Bar on top, XP Bar attached below.
 *     - Top line: User Name + Hexagonal Level Badge.
 *     - Bottom line: Location & Network status.
 * - Center: Living 3D Mascot with Care FAB (left) & Studio/Quest buttons (right).
 * - Below Pet: Balanced row with Mic/Cancel on left, Prominent Chat in the exact CENTER, and Camera on right.
 * - App Controls: Life Hub & Wellness with generous spacing.
 */
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
    onFeedPet: () -> Unit = {},
    onDancePet: () -> Unit = {},
    onPokePet: () -> Unit = {},
    onToggleTask: (Long, Boolean) -> Unit = { _, _ -> },
    onQuickAgentPrompt: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val petPrimary = Color(petStatus.bloubSkinColor.primaryHex)

    var showQuestsBottomSheet by remember { mutableStateOf(false) }
    var interactionRewardEffect by remember { mutableStateOf<String?>(null) }
    var isCareFabExpanded by remember { mutableStateOf(false) }

    // Ambient floating background pulses
    val infiniteTransition = rememberInfiniteTransition(label = "HomeGamifiedAura")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgPulse"
    )

    val neonGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "NeonGlowAlpha"
    )

    val voiceWavePulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VoiceWavePulse"
    )

    fun triggerRewardToast(text: String) {
        interactionRewardEffect = text
        coroutineScope.launch {
            delay(1600)
            if (interactionRewardEffect == text) {
                interactionRewardEffect = null
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        petPrimary.copy(alpha = neonGlowAlpha),
                        Color(0xFF0F111E),
                        ObsidianDark
                    ),
                    center = Offset(540f, 500f),
                    radius = 1300f
                )
            )
    ) {
        // --- 1. AMBIENT CYBER STARS BACKGROUND ---
        CyberAmbientStarsBackground(primaryColor = petPrimary)

        // --- 2. MAIN SCROLLABLE CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ==========================================
            // 1. TOP RPG HUD: ATTACHED HP & XP BARS STARTING FROM USER PIC
            // ==========================================
            SeamlessRpgPlayerHud(
                petStatus = petStatus,
                batteryStatus = batteryStatus,
                networkStatus = networkStatus,
                locationContext = locationContext,
                userProfile = userProfile,
                onNavigateToAccount = onNavigateToAccount
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // 2. MINIMAL PET SPEECH CARD (RESPONSE TEXT + MOOD)
            // ==========================================
            MinimalPetSpeechCard(
                petStatus = petStatus,
                petPrimary = petPrimary,
                onClick = onNavigateToChat
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ==========================================
            // 3. CENTER: LIVING AI COMPANION WITH CARE FAB (LEFT) & STUDIO/QUESTS (RIGHT)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                // Holo-Stage Platform
                HoloPedestalPlatform(
                    petColor = petPrimary,
                    pulseScale = pulseScale,
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = (-6).dp)
                )

                // Living Mascot (Tap gives cute bond reward)
                LumiPetView(
                    petStatus = petStatus,
                    size = 255.dp,
                    onPetTouched = {
                        onPetTouched()
                        triggerRewardToast("+10 Bond ✨")
                    },
                    onPetPetted = {
                        onPetPetted()
                        triggerRewardToast("+25 Bond 💖")
                    },
                    modifier = Modifier
                        .testTag("lumi_pet_view")
                        .align(Alignment.Center)
                )

                // LEFT SIDE OF PET: Fixed Anchor Expandable Pet Care FAB (Opens downwards)
                PetCareFixedAnchorFab(
                    isExpanded = isCareFabExpanded,
                    onToggleExpand = { isCareFabExpanded = !isCareFabExpanded },
                    petEmotion = petStatus.currentEmotion,
                    petPrimary = petPrimary,
                    onFeed = {
                        onFeedPet()
                        triggerRewardToast("+15 Energy 🍓")
                    },
                    onPet = {
                        onPetPetted()
                        triggerRewardToast("+20 Bond 💖")
                    },
                    onDance = {
                        onDancePet()
                        triggerRewardToast("Dance Party! 🎵")
                    },
                    onPoke = {
                        onPokePet()
                        triggerRewardToast("Giggle! ⚡")
                    },
                    onSleepToggle = {
                        onTogglePetSleep()
                        val mode = if (petStatus.currentEmotion == PetEmotion.SLEEPY) "Awake! ☀️" else "Nap Mode 🌙"
                        triggerRewardToast(mode)
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 2.dp, top = 16.dp)
                )

                // RIGHT SIDE OF PET: Studio & Quest Buttons
                PetRightSideActionButtons(
                    pendingQuestsCount = tasks.count { !it.isCompleted },
                    onShowWardrobe = onShowWardrobe,
                    onShowQuests = { showQuestsBottomSheet = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 2.dp, top = 16.dp)
                )

                // Floating Reward Pop-up animation
                androidx.compose.animation.AnimatedVisibility(
                    visible = interactionRewardEffect != null,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut(),
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = 6.dp)
                ) {
                    interactionRewardEffect?.let { text ->
                        Surface(
                            color = petPrimary.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(12.dp),
                            shadowElevation = 8.dp
                        ) {
                            Text(
                                text = text,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ==========================================
            // 4. ACTION BUTTONS BELOW PET: MIC (LEFT) | CHAT (CENTER) | CAMERA (RIGHT)
            // ==========================================
            PetBottomControlsRow(
                isListening = isListening,
                isSpeaking = isSpeaking,
                petPrimary = petPrimary,
                voiceWavePulse = voiceWavePulse,
                onShowCamera = onShowCamera,
                onStartVoiceListening = onStartVoiceListening,
                onStopVoiceListening = onStopVoiceListening,
                onNavigateToChat = onNavigateToChat
            )

            // Distinct vertical gap between pet buttons and app buttons
            Spacer(modifier = Modifier.height(22.dp))

            // ==========================================
            // 5. PROMINENT APP BUTTONS: LIFE HUB & WELLNESS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Life Hub Button
                Surface(
                    color = SurfaceDark.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, LumiGold.copy(alpha = 0.6f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToLifeHub(0) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = LumiGold.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Dashboard, contentDescription = null, tint = LumiGold, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(id = R.string.text_life_hub), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(stringResource(id = R.string.text_goals_schedule), color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }

                // Wellness Button
                Surface(
                    color = SurfaceDark.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, LumiPink.copy(alpha = 0.6f)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToWellness() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = LumiPink.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = LumiPink, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(stringResource(id = R.string.text_wellness), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(stringResource(id = R.string.text_zen_vitality), color = TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==========================================
        // QUESTS MODAL BOTTOM SHEET
        // ==========================================
        if (showQuestsBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showQuestsBottomSheet = false },
                containerColor = SurfaceDark,
                contentColor = TextPrimary,
                scrimColor = Color.Black.copy(alpha = 0.65f),
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = LumiGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(id = R.string.text_active_quests_bounties), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Surface(
                            color = SurfaceHighlight.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "${tasks.count { !it.isCompleted }} Active",
                                color = LumiCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val pending = tasks.filter { !it.isCompleted }
                    if (pending.isEmpty()) {
                        Surface(
                            color = SurfaceDarkVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LumiGreen, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(stringResource(id = R.string.text_all_quests_cleared), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(stringResource(id = R.string.text_your_companion_is_thrilled_che), color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 380.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(pending) { task ->
                                QuestItemRow(
                                    task = task,
                                    onToggle = { isChecked ->
                                        onToggleTask(task.id, isChecked)
                                        if (isChecked) triggerRewardToast("+50 XP Claimed! 🏆")
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            showQuestsBottomSheet = false
                            onNavigateToLifeHub(1)
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkVariant),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, petPrimary.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = petPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(id = R.string.text_create_new_quest_in_life_hub), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

