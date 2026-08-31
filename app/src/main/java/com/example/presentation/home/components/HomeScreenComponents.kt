package com.example.presentation.home.components
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
// =========================================================================
// SUB-COMPONENTS
// =========================================================================

/**
 * Seamless Floating RPG Player HUD Banner:
 * - Left: Solid warm brown Avatar touching the dual progress bars directly.
 * - Center: Dual attached progress bars (HP Bar on top, XP Bar directly attached below).
 * - Top line: User Name + Hexagonal Level Badge.
 * - Bottom line: Location telemetry & Network link status.
 */
@Composable
fun SeamlessRpgPlayerHud(
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

    val hpFillRatio = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f)
    val energyGradient = when {
        batteryStatus.levelPercent >= 60 -> listOf(LumiMint, LumiMintBright)
        batteryStatus.levelPercent >= 20 -> listOf(LumiYellow, LumiGoldBright)
        else -> listOf(LumiCoral, LumiCoralDark)
    }

    val xpRatio = (petStatus.exp.toFloat() / petStatus.expToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
    val xpGradient = listOf(LumiCyanDark, LumiCyan)

    val beaconColor = when {
        !networkStatus.isConnected || networkStatus.type == NetworkType.OFFLINE -> LumiCoral
        networkStatus.type == NetworkType.CELLULAR -> LumiYellow
        else -> LumiMint
    }

    val netLabel = when (networkStatus.type) {
        NetworkType.WIFI -> "WIFI"
        NetworkType.CELLULAR -> "CELL"
        NetworkType.ETHERNET -> "ETH"
        NetworkType.OFFLINE -> "OFFLINE"
    }

    val placeName = locationContext.approximatePlace.ifBlank { "Unknown" }.uppercase()

    // No enclosing Surface card. Use a Box that fills width with a subtle gradient top-down for readability.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(ObsidianDark.copy(alpha = 0.6f), Color.Transparent)
                )
            )
            .clickable { onNavigateToAccount() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Avatar with overlapping Level Badge
            Box(
                modifier = Modifier.padding(end = 12.dp)
            ) {
                // Outer Tech Ring
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Brush.sweepGradient(listOf(LumiCyan, LumiPink, LumiCyan)))
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(ObsidianDark)
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AvatarBronzeLight, AvatarBronzeDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Level Badge overlapping at bottom right
                Box(modifier = Modifier.align(Alignment.BottomEnd).offset(x = 6.dp, y = 4.dp)) {
                    SeamlessHexagonLevelBadge(level = petStatus.level)
                }
            }

            // RIGHT: Info & Bars
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Row: Name + Resources + Telemetry
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Name
                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Currencies
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Coins
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🪙", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${petStatus.coins}",
                                color = LumiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Gems
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "💎", fontSize = 10.sp)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${petStatus.gems}",
                                color = LumiCyanLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // HP Bar
                SciFiProgressBar(
                    label = "HP",
                    labelColor = Color.White.copy(alpha = 0.7f),
                    fillRatio = hpFillRatio,
                    gradient = energyGradient,
                    height = 10.dp,
                    trailingText = "${batteryStatus.levelPercent}%",
                    trailingTextColor = Color.White.copy(alpha = 0.9f)
                )

                // XP Bar & Telemetry
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SciFiProgressBar(
                        label = "XP",
                        labelColor = LumiCyan,
                        fillRatio = xpRatio,
                        gradient = xpGradient,
                        height = 8.dp,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    // Small Tech Telemetry replacing exact XP numbers to save space and look cooler
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.width(60.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(beaconColor)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = netLabel,
                            color = beaconColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SciFiProgressBar(
    label: String,
    labelColor: Color,
    fillRatio: Float,
    gradient: List<Color>,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    trailingTextColor: Color = Color.Unspecified
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(
            text = label,
            color = labelColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.width(20.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(height)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val skew = size.height * 0.5f
                val w = size.width
                val h = size.height
                
                val bgPath = Path().apply {
                    moveTo(skew, 0f)
                    lineTo(w, 0f)
                    lineTo(w - skew, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(bgPath, color = Color.White.copy(alpha = 0.1f))
                
                val fillW = (w * fillRatio).coerceAtLeast(0f)
                if (fillW > 0) {
                    val fgPath = Path().apply {
                        moveTo(skew, 0f)
                        lineTo(maxOf(skew, fillW), 0f)
                        lineTo(maxOf(0f, fillW - skew), h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(fgPath, brush = Brush.horizontalGradient(gradient))
                }
            }
        }
        if (trailingText != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = trailingText,
                color = trailingTextColor,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Sleek Hexagonal Level Badge
 */
@Composable
fun SeamlessHexagonLevelBadge(level: Int) {
    Box(
        modifier = Modifier.size(20.dp),
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
            drawPath(path, color = LumiCyanDark)
            drawPath(path, color = LumiCyan, style = Stroke(width = 1.2.dp.toPx()))
        }
        Text(
            text = "$level",
            color = ObsidianDark,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black
        )
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
                    contentDescription = stringResource(id = R.string.desc_pet_care_actions),
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
                    contentDescription = stringResource(id = R.string.desc_pet_studio),
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
                        contentDescription = stringResource(id = R.string.desc_quests),
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
                        contentDescription = stringResource(id = R.string.desc_cancel_voice_mode),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(id = R.string.desc_voice_input),
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
                    contentDescription = stringResource(id = R.string.desc_chat_with_lumi),
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
                    contentDescription = stringResource(id = R.string.desc_camera_lens),
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
                    Text(stringResource(id = R.string.text_), color = TextTertiary, fontSize = 9.sp)
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
                    text = stringResource(R.string.text_50_xp),
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
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    val stars = remember {
        List(30) {
            Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        stars.forEach { (nx, ny, starRadius) ->
            val x = nx * size.width
            val y = ((ny + offsetY) % 1f) * size.height
            drawCircle(
                color = primaryColor.copy(alpha = 0.4f),
                radius = starRadius.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
