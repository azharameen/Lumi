package com.example.presentation.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.domain.model.PetEmotion

/**
 * Left Side of Pet: Fixed Anchor FAB (Button stays stationary at the top, items expand downwards)
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
        // Fixed Anchor Button
        Surface(
            color = if (isExpanded) petPrimary else SurfaceDark.copy(alpha = 0.9f),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, petPrimary),
            shadowElevation = MaterialTheme.spacing.small,
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

        // Expanded items opening downwards without shifting master button
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + scaleIn(transformOrigin = TransformOrigin(0.5f, 0f)),
            exit = fadeOut() + scaleOut(transformOrigin = TransformOrigin(0.5f, 0f))
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PetMiniActionButton(icon = androidx.compose.material.icons.Icons.Default.Restaurant, onClick = onFeed, tooltip = "Feed")
                PetMiniActionButton(icon = androidx.compose.material.icons.Icons.Default.Favorite, onClick = onPet, tooltip = "Pet")
                PetMiniActionButton(icon = androidx.compose.material.icons.Icons.Default.MusicNote, onClick = onDance, tooltip = "Dance")
                PetMiniActionButton(icon = androidx.compose.material.icons.Icons.Default.Bolt, onClick = onPoke, tooltip = "Poke")
                PetMiniActionButton(
                    icon = if (petEmotion == PetEmotion.SLEEPY) androidx.compose.material.icons.Icons.Default.WbSunny else androidx.compose.material.icons.Icons.Default.Bedtime,
                    onClick = onSleepToggle,
                    tooltip = if (petEmotion == PetEmotion.SLEEPY) "Wake" else "Nap"
                )
            }
        }
    }
}

@Composable
fun PetMiniActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit, tooltip: String) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.92f),
        shape = CircleShape,
        border = BorderStroke(1.dp, SurfaceHighlight),
        shadowElevation = MaterialTheme.spacing.extraSmall,
        modifier = Modifier
            .size(34.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = tooltip,
                tint = TextPrimary,
                modifier = Modifier.size(16.dp)
            )
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
                        .size(MaterialTheme.spacing.medium)
                        .align(Alignment.TopEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$pendingQuestsCount",
                            color = ObsidianDark,
                            fontSize = 9.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Black
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
            size = Size(size.width * 0.7f, size.height * 0.6f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

/**
 * Controller Below Pet:
 * Balanced 3-Item Row:
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
        // 1. MIC / CANCEL AUDIO BUTTON
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
                        modifier = Modifier.size(MaterialTheme.spacing.large)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(id = R.string.desc_voice_input),
                        tint = petPrimary,
                        modifier = Modifier.size(MaterialTheme.spacing.large)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // 2. PROMINENT HERO CHAT BUTTON
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

        // 3. CAMERA / VISION LENS BUTTON
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
