package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiGold
import com.example.ui.theme.LumiGreen
import com.example.ui.theme.LumiMint
import com.example.ui.theme.LumiPink
import com.example.ui.theme.LumiViolet
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LumiViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LiveVoiceModeScreen(
    viewModel: LumiViewModel,
    onClose: () -> Unit
) {
    val petStatus by viewModel.petStatus.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val recognizedText by viewModel.voiceEngine.recognizedText.collectAsState()
    val audioLevel by viewModel.voiceEngine.audioWaveformLevel.collectAsState()
    val soundState by viewModel.soundscapeState.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "voicePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        // Glowing Ambient Background Particles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2.3f)
            val baseRadius = (size.width * 0.35f) * (if (isListening || isSpeaking) (1f + audioLevel * 0.4f) else pulseScale)

            // Outer multi-ring aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LumiCyan.copy(alpha = if (isListening) 0.25f else 0.12f),
                        LumiViolet.copy(alpha = if (isSpeaking) 0.22f else 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.5f
                ),
                center = center,
                radius = baseRadius * 1.5f
            )

            // Dynamic orbiting energy nodes
            for (i in 0 until 6) {
                val angle = Math.toRadians((rotationAngle + i * 60).toDouble())
                val orbitDist = baseRadius * (1.1f + (i % 2) * 0.15f)
                val nodeX = center.x + (orbitDist * cos(angle)).toFloat()
                val nodeY = center.y + (orbitDist * sin(angle)).toFloat()
                drawCircle(
                    color = if (i % 2 == 0) LumiCyan.copy(alpha = 0.6f) else LumiPink.copy(alpha = 0.5f),
                    radius = 4.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SurfaceDarkVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (isListening) LumiPink else if (isSpeaking) LumiCyan else LumiMint,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isListening) "LISTENING..." else if (isSpeaking) "SPEAKING" else "READY",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.stopVoiceListening()
                        onClose()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(SurfaceDarkVariant, CircleShape)
                        .testTag("btn_close_live_voice")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Live Voice",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Center Orb & Holographic Companion
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Interactive Glowing Orb
                Surface(
                    color = SurfaceDark,
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(
                        width = 2.dp,
                        brush = Brush.sweepGradient(listOf(LumiCyan, LumiViolet, LumiPink, LumiCyan))
                    ),
                    modifier = Modifier
                        .size(170.dp)
                        .scale(if (isListening || isSpeaking) (1f + audioLevel * 0.25f) else pulseScale)
                        .clickable {
                            if (isListening) {
                                viewModel.stopVoiceListening()
                            } else {
                                viewModel.startVoiceListening()
                            }
                        }
                        .testTag("live_voice_orb")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (petStatus.currentEmotion) {
                                    com.example.domain.model.PetEmotion.HAPPY -> "✨"
                                    com.example.domain.model.PetEmotion.CALM -> "🌿"
                                    com.example.domain.model.PetEmotion.ENERGETIC -> "⚡"
                                    com.example.domain.model.PetEmotion.SLEEPY -> "🌙"
                                    com.example.domain.model.PetEmotion.THINKING -> "🔮"
                                    com.example.domain.model.PetEmotion.LOVING -> "💖"
                                    com.example.domain.model.PetEmotion.PLAYFUL -> "🎀"
                                    com.example.domain.model.PetEmotion.CONCERNED -> "🌸"
                                },
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = petStatus.name,
                                color = LumiCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Spoken / Recognized Transcript Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.85f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isListening) {
                                if (recognizedText.isNotBlank()) "\"$recognizedText\"" else "Listening to your voice..."
                            } else if (isSpeaking) {
                                petStatus.speechBubbleText ?: "Speaking response..."
                            } else {
                                "Tap the glowing orb or say \"Hey Lumi\" to talk."
                            },
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Bottom Quick Voice Commands & Controls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Voice Prompts
                Text(
                    text = "Quick Voice Commands",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val promptList = listOf(
                        "What's my day look like?",
                        "Decompose my startup goal",
                        "Start 25 min Gamma focus",
                        "Play rain soundscape",
                        "How are you feeling Lumi?"
                    )
                    items(promptList) { prompt ->
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable {
                                viewModel.sendMessageToAi(prompt)
                            }
                        ) {
                            Text(
                                text = prompt,
                                color = LumiCyan,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                // Voice Control Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Soundscape Toggle
                    IconButton(
                        onClick = {
                            if (soundState.isPlaying) {
                                viewModel.stopSoundscape()
                            } else {
                                viewModel.startSoundscape(com.example.service.SoundscapeType.BINAURAL_FOCUS)
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (soundState.isPlaying) LumiCyan.copy(alpha = 0.2f) else SurfaceDarkVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = "Ambient Soundscape",
                            tint = if (soundState.isPlaying) LumiCyan else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Main Mic Action Button
                    Surface(
                        color = if (isListening) LumiPink else LumiCyan,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(72.dp)
                            .clickable {
                                if (isListening) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    viewModel.startVoiceListening()
                                }
                            }
                            .testTag("btn_main_live_mic")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mic",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    // Sound Output Voice Toggle
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                viewModel.voiceEngine.stopSpeaking()
                            } else {
                                viewModel.speakBriefing()
                            }
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isSpeaking) LumiViolet.copy(alpha = 0.2f) else SurfaceDarkVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = "TTS Voice",
                            tint = if (isSpeaking) LumiViolet else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
