package com.example.ui.screens

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
import com.example.service.BatteryStatus
import com.example.service.LocationContext
import com.example.service.NetworkStatus
import com.example.service.NetworkType
import com.example.ui.pet.LumiPetView
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Gamified Agentic AI Command Center.
 * - Top: Enterprise Cyber-RPG Player HUD Deck:
 *     - Left: Polished Bronze & Obsidian Avatar Pod with glowing outer XP Orbit Ring & Telemetry Beacon.
 *     - Top Deck: User Name + Faceted Hexagonal Level Insignia + Live XP Progress readout.
 *     - Center: Holographic HP / Vitality Energy Bar with dynamic gradient fluid & charging sweep.
 *     - Sub Deck: Micro-telemetry row with Live Location and Network Type status.
 * - Center: Living 3D Mascot with Care FAB (left) & Studio/Quest buttons (right).
 * - Below Pet: Balanced row with Mic/Cancel on left, Prominent Chat in the exact CENTER, and Camera on right.
 * - App Controls: Life Hub & Wellness with generous spacing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    petStatus: PetStatus,
    uiState: com.example.ui.viewmodel.LumiUiState,
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
                        Color(0xFF100E1D),
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
            // 1. TOP RPG HUD: ENTERPRISE CYBER-RPG DECK
            // ==========================================
            GamifiedRpgPlayerHud(
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
                            Text("Life Hub", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Goals & Schedule", color = TextSecondary, fontSize = 10.sp)
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
                            Text("Wellness", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Zen & Vitality", color = TextSecondary, fontSize = 10.sp)
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
                            Text("Active Quests & Bounties", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
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
                                Text("All Quests Cleared!", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Your companion is thrilled. Check Life Hub for new goals.", color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
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
                        Text("Create New Quest in Life Hub", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// =========================================================================
// SUB-COMPONENTS
// =========================================================================

/**
 * Enterprise Cyber-RPG Player HUD Deck:
 * - High-end glassmorphic frame with hairline holo-rim lighting.
 * - Avatar Command Pod: Deep obsidian & bronze metallic monogram avatar with glowing neon XP orbit gauge & beacon.
 * - Top Deck: Hero identity title + 3D faceted crystal Level insignia + XP readout.
 * - Center: Streamlined Energy Gauge with chamfered cyber-accent, glowing fluid gradient, and charging pulse wave.
 * - Sub Deck: Micro-telemetry bar with Geolocation & Network Link telemetry.
 */
@Composable
fun GamifiedRpgPlayerHud(
    petStatus: PetStatus,
    batteryStatus: BatteryStatus,
    networkStatus: NetworkStatus,
    locationContext: LocationContext,
    userProfile: UserProfileData,
    onNavigateToAccount: () -> Unit
) {
    val displayName = userProfile.userName.ifBlank { "Azhar Ameen" }

    // Initials calculation
    val initials = remember(displayName) {
        val parts = displayName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        when {
            parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
            displayName.length >= 2 -> displayName.take(2).uppercase()
            displayName.isNotEmpty() -> "${displayName.first().uppercaseChar()}N"
            else -> "AA"
        }
    }

    // Dynamic fluid energy color palette
    val hpFillRatio = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f)
    val (energyGradient, energyGlowColor) = when {
        batteryStatus.levelPercent >= 60 -> Pair(
            listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF00F5D4)),
            Color(0xFF00F5D4)
        )
        batteryStatus.levelPercent >= 20 -> Pair(
            listOf(Color(0xFFD97706), Color(0xFFF59E0B), Color(0xFFFDE047)),
            Color(0xFFF59E0B)
        )
        else -> Pair(
            listOf(Color(0xFFDC2626), Color(0xFFEF4444), Color(0xFFFF8FA3)),
            Color(0xFFEF4444)
        )
    }

    // XP Progress
    val xpRatio = (petStatus.exp.toFloat() / petStatus.expToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)

    // Infinite animations
    val infiniteTransition = rememberInfiniteTransition(label = "RpgHudAnimations")
    val chargingShimmer by infiniteTransition.animateFloat(
        initialValue = -80f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ChargingShimmer"
    )

    val beaconPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BeaconPulse"
    )

    // Telemetry Beacon Color
    val beaconColor = when {
        !networkStatus.isConnected || networkStatus.type == NetworkType.OFFLINE -> Color(0xFFEF4444)
        networkStatus.type == NetworkType.CELLULAR -> Color(0xFFF59E0B)
        else -> Color(0xFF10B981)
    }

    // Main Glassmorphic Deck
    Surface(
        color = Color(0xCC151928),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.45f),
                    Color(0xFF7928CA).copy(alpha = 0.25f),
                    Color(0xFF00E5FF).copy(alpha = 0.15f)
                )
            )
        ),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToAccount() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ==========================================
            // 1. AVATAR COMMAND POD WITH XP ORBIT GAUGE & BEACON
            // ==========================================
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                // Circular Orbit XP Gauge
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stroke = 3.dp.toPx()
                    // Track
                    drawArc(
                        color = Color(0xFF1F2438),
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                    // Active Orbit
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF0077B6),
                                Color(0xFF00E5FF),
                                Color(0xFF00F5D4),
                                Color(0xFF0077B6)
                            )
                        ),
                        startAngle = -90f,
                        sweepAngle = xpRatio * 360f,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                // Premium Bronze & Obsidian Avatar Core
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFB5704D), Color(0xFF5C3317), Color(0xFF2B170B))
                            )
                        )
                        .padding(1.5.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFF6E3E20), Color(0xFF26150C))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        color = Color(0xFFFFF7ED),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    )
                }

                // Telemetry Signal Beacon (Pulsing Orbit Indicator)
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = (-1).dp, y = (-1).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0C0E17))
                        .padding(1.5.dp)
                        .clip(CircleShape)
                        .background(beaconColor.copy(alpha = beaconPulseAlpha))
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ==========================================
            // 2. MAIN TELEMETRY & ENERGY DECK
            // ==========================================
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // --- TOP DECK: Name + Crystal Hexagon Insignia + XP Readout ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = displayName,
                            color = Color(0xFFE2E8F0),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.3.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // 3D Faceted Hexagonal Level Badge
                        FacetedHexagonBadge(level = petStatus.level)
                    }

                    // XP Status Badge
                    Surface(
                        color = Color(0xFF00E5FF).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(0.8.dp, Color(0xFF00E5FF).copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${petStatus.exp}/${petStatus.expToNextLevel} XP",
                            color = Color(0xFF00E5FF),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // --- CENTER: Holographic HP / Vitality Energy Gauge ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(CyberGaugeShape())
                        .background(Color(0xFF0E121E))
                        .border(1.dp, Color(0xFF283049), CyberGaugeShape())
                ) {
                    // Fluid Energy Level Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(hpFillRatio)
                            .fillMaxHeight()
                            .background(
                                if (batteryStatus.isCharging) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            energyGradient[0],
                                            Color(0xFFFFFFFF),
                                            energyGradient[1],
                                            energyGradient[2]
                                        ),
                                        startX = chargingShimmer - 60f,
                                        endX = chargingShimmer + 60f
                                    )
                                } else {
                                    Brush.horizontalGradient(energyGradient)
                                }
                            )
                    )

                    // Overlay Energy Readout
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VITALITY",
                            color = Color(0xCCFFFFFF),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (batteryStatus.isCharging) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Charging",
                                    tint = Color(0xFF00F5D4),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Text(
                                text = "${batteryStatus.levelPercent}%",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                // --- SUB DECK: Location & Network Telemetry Status ---
                val placeName = locationContext.approximatePlace.ifBlank { "Unknown" }
                val netLabel = when (networkStatus.type) {
                    NetworkType.WIFI -> "WiFi"
                    NetworkType.CELLULAR -> "Cellular"
                    NetworkType.ETHERNET -> "Ethernet"
                    NetworkType.OFFLINE -> "Offline"
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Location Telemetry
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = LumiGold,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = placeName.uppercase(),
                            color = TextSecondary,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Network Link Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(beaconColor)
                        )
                        Text(
                            text = netLabel.uppercase(),
                            color = beaconColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.4.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3D Faceted Crystal Hexagon Badge for Player / Companion Level
 */
@Composable
fun FacetedHexagonBadge(level: Int) {
    Box(
        modifier = Modifier.size(19.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(w * 0.5f, 0f)
                lineTo(w, h * 0.25f)
                lineTo(w, h * 0.75f)
                lineTo(w * 0.5f, h)
                lineTo(0f, h * 0.75f)
                lineTo(0f, h * 0.25f)
                close()
            }
            // Facet body
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF0C4A6E))
                )
            )
            // Cyber border
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                ),
                style = Stroke(width = 1.2.dp.toPx())
            )
        }
        Text(
            text = "$level",
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
    }
}

/**
 * Sculpted Chamfered Cyber-Gauge Shape for the HP bar
 */
class CyberGaugeShape : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val w = size.width
        val h = size.height
        val chamfer = 5f

        val path = Path().apply {
            moveTo(chamfer, 0f)
            lineTo(w - chamfer * 1.8f, 0f)
            lineTo(w, h * 0.5f)
            lineTo(w - chamfer * 1.8f, h)
            lineTo(chamfer, h)
            lineTo(0f, h - chamfer)
            lineTo(0f, chamfer)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

/**
 * Minimal Pet Speech Card: Left = Response Text, Right = Mood Badge.
 */
@Composable
fun MinimalPetSpeechCard(
    petStatus: PetStatus,
    petPrimary: Color,
    onClick: () -> Unit
) {
    val speechText = petStatus.speechBubbleText ?: "Hey friend! How are you feeling today?"

    Surface(
        color = SurfaceDark.copy(alpha = 0.88f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, petPrimary.copy(alpha = 0.3f)),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: AI / Pet Response Text
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = petPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = speechText,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Mood Badge
            Surface(
                color = SurfaceDarkVariant,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.6f))
            ) {
                Text(
                    text = petStatus.currentEmotion.displayName.split(" ")[0],
                    color = petPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Left Side of Pet: Fixed Anchor FAB (Button stays stationary at the top, items expand downwards!)
 */
@Composable
fun PetCareFixedAnchorFab(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    petEmotion: PetEmotion,
    petPrimary: Color,
    onFeed: () -> Unit,
    onPet: () -> Unit,
    onDance: () -> Unit,
    onPoke: () -> Unit,
    onSleepToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Fixed Anchor Button (Always stays in place)
        Surface(
            color = if (isExpanded) petPrimary else SurfaceDark.copy(alpha = 0.9f),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, petPrimary),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(42.dp)
                .clickable { onToggleExpand() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.AutoAwesome,
                    contentDescription = "Pet Care Actions",
                    tint = if (isExpanded) Color.White else petPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Expanded items opening downwards without shifting the master button
        androidx.compose.animation.AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.5f, 0f)),
            exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(0.5f, 0f))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PetMiniActionButton(emoji = "🍓", onClick = onFeed, tooltip = "Feed")
                PetMiniActionButton(emoji = "💖", onClick = onPet, tooltip = "Pet")
                PetMiniActionButton(emoji = "🎵", onClick = onDance, tooltip = "Dance")
                PetMiniActionButton(emoji = "⚡", onClick = onPoke, tooltip = "Poke")
                PetMiniActionButton(
                    emoji = if (petEmotion == PetEmotion.SLEEPY) "☀️" else "🌙",
                    onClick = onSleepToggle,
                    tooltip = if (petEmotion == PetEmotion.SLEEPY) "Wake" else "Nap"
                )
            }
        }
    }
}

@Composable
fun PetMiniActionButton(emoji: String, onClick: () -> Unit, tooltip: String) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.92f),
        shape = CircleShape,
        border = BorderStroke(1.dp, SurfaceHighlight),
        shadowElevation = 4.dp,
        modifier = Modifier
            .size(34.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = emoji, fontSize = 15.sp)
        }
    }
}

/**
 * Right Side of Pet: Quick Action Buttons for Studio (Wardrobe) and Quest Log
 */
@Composable
fun PetRightSideActionButtons(
    pendingQuestsCount: Int,
    onShowWardrobe: () -> Unit,
    onShowQuests: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Studio / Wardrobe Button
        Surface(
            color = SurfaceDark.copy(alpha = 0.9f),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, LumiGold.copy(alpha = 0.7f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(42.dp)
                .clickable { onShowWardrobe() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Checkroom,
                    contentDescription = "Pet Studio",
                    tint = LumiGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 2. Quest Log Button with Active Count Badge
        Box(
            modifier = Modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = SurfaceDark.copy(alpha = 0.9f),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, LumiYellow.copy(alpha = 0.7f)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onShowQuests() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Quests",
                        tint = LumiYellow,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Mini Badge for Pending Quests
            if (pendingQuestsCount > 0) {
                Surface(
                    color = LumiYellow,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$pendingQuestsCount",
                            color = ObsidianDark,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HoloPedestalPlatform(petColor: Color, pulseScale: Float, modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .width(220.dp)
            .height(44.dp)
    ) {
        val cx = size.width / 2
        val cy = size.height / 2

        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(petColor.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(cx, cy),
                radius = size.width / 2 * pulseScale
            ),
            topLeft = Offset(0f, 0f),
            size = size
        )

        drawOval(
            color = petColor.copy(alpha = 0.5f),
            topLeft = Offset(size.width * 0.15f, size.height * 0.2f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.7f, size.height * 0.6f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Controller Below Pet:
 * Balanced 3-Item Row with:
 * - Left: Mic / Cancel Audio Button (48dp)
 * - CENTER: Prominent Hero Chat Button (64dp, Solid Primary Color)
 * - Right: Camera / Vision Lens Button (48dp)
 */
@Composable
fun PetBottomControlsRow(
    isListening: Boolean,
    isSpeaking: Boolean,
    petPrimary: Color,
    voiceWavePulse: Float,
    onShowCamera: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val isActiveVoice = isListening || isSpeaking

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. MIC / CANCEL AUDIO BUTTON (To the left of Chat)
        Surface(
            color = if (isActiveVoice) LumiCoral else SurfaceDark.copy(alpha = 0.9f),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, if (isActiveVoice) LumiCoral else petPrimary.copy(alpha = 0.7f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(48.dp)
                .scale(if (isActiveVoice) voiceWavePulse else 1f)
                .clickable {
                    if (isActiveVoice) {
                        onStopVoiceListening()
                    } else {
                        onStartVoiceListening()
                    }
                }
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isActiveVoice) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Voice Mode",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = petPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // 2. PROMINENT HERO CHAT BUTTON (EXACT CENTER - 64dp, Solid Primary Color)
        Surface(
            color = petPrimary,
            shape = CircleShape,
            shadowElevation = 10.dp,
            modifier = Modifier
                .size(64.dp)
                .clickable { onNavigateToChat() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Chat with Lumi",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // 3. CAMERA / VISION LENS BUTTON (To the right of Chat)
        Surface(
            color = SurfaceDark.copy(alpha = 0.88f),
            shape = CircleShape,
            border = BorderStroke(1.2.dp, LumiMint.copy(alpha = 0.6f)),
            shadowElevation = 6.dp,
            modifier = Modifier
                .size(48.dp)
                .clickable { onShowCamera() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera Lens",
                    tint = LumiMint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun QuestItemRow(task: TaskEntity, onToggle: (Boolean) -> Unit) {
    Surface(
        color = SurfaceDarkVariant.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = LumiGreen,
                    uncheckedColor = TextSecondary
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val priorityColor = when (task.priority.uppercase()) {
                        "HIGH", "CRITICAL" -> LumiCoral
                        "MEDIUM" -> LumiYellow
                        else -> LumiMint
                    }
                    Text(
                        text = task.priority.uppercase(),
                        color = priorityColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("•", color = TextTertiary, fontSize = 9.sp)
                    Text(
                        text = "${task.estimatedMinutes}m",
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            Surface(
                color = LumiGold.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "+50 XP",
                    color = LumiGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CyberAmbientStarsBackground(primaryColor: Color) {
    val stars = remember {
        List(24) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { (nx, ny, starRadius) ->
            val x = nx * size.width
            val y = ny * size.height
            drawCircle(
                color = primaryColor.copy(alpha = 0.22f),
                radius = starRadius.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
