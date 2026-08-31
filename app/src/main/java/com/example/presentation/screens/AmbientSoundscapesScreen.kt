package com.example.presentation.screens
import androidx.compose.ui.res.stringResource
import com.example.R


import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.device.SoundscapeType

import com.example.core.theme.LumiCoral
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiGreen
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink

import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceDarkVariant
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing


@Composable
fun AmbientSoundscapesScreen(
    soundState: com.example.data.device.SoundscapeState,
    onAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit,
                ) {
    val context = LocalContext.current
    

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.medium),
            contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // Hero Focus Room Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                        androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Headphones,
                                        contentDescription = null,
                                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                    Text(
                                        text = stringResource(R.string.text_ambient_focus_room),
                                        color = TextPrimary,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    color = if (soundState.isPlaying) LumiGreen.copy(alpha = 0.2f) else SurfaceHighlight,
                                    shape = RoundedCornerShape(MaterialTheme.spacing.small)
                                ) {
                                    Text(
                                        text = if (soundState.isPlaying) "PLAYING" else "IDLE",
                                        color = if (soundState.isPlaying) LumiGreen else TextSecondary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Animated Circular Core
                            Surface(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = if (soundState.isPlaying) 0.25f else 0.10f),
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(110.dp)
                                    .scale(if (soundState.isPlaying) pulseScale else 1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = soundState.activeType.iconEmoji,
                                        fontSize = 44.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = soundState.activeType.title,
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = soundState.activeType.description,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                            // Timer Display if active
                            if (soundState.isTimerActive) {
                                val mins = soundState.remainingSeconds / 60
                                val secs = soundState.remainingSeconds % 60
                                val timerProgress = soundState.remainingSeconds.toFloat() / soundState.totalSeconds.coerceAtLeast(1).toFloat()

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = String.format("%02d:%02d", mins, secs),
                                        color = LumiGold,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                                    LinearProgressIndicator(
                                        progress = { timerProgress },
                                        color = LumiGold,
                                        trackColor = SurfaceHighlight,
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(MaterialTheme.spacing.extraSmall)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                }
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                            }

                            // Play / Pause Main Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        if (soundState.isPlaying) {
                                            onAction(com.example.presentation.viewmodel.LumiUiAction.StopSoundscape)
                                        } else {
                                            onAction(com.example.presentation.viewmodel.LumiUiAction.StartSoundscape(soundState.activeType))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (soundState.isPlaying) LumiCoral else androidx.compose.material3.MaterialTheme.colorScheme.primary
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("btn_soundscape_toggle")
                                ) {
                                    Icon(
                                        imageVector = if (soundState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (soundState.isPlaying) "Pause Ambient" else "Play Ambient",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (soundState.isTimerActive) {
                                    Button(
                                        onClick = { onAction(com.example.presentation.viewmodel.LumiUiAction.StopFocusTimer) },
                                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkVariant),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Stop,
                                            contentDescription = null,
                                            tint = TextPrimary,
                                            modifier = Modifier.size(MaterialTheme.spacing.medium)
                                        )
                                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                        Text(stringResource(id = R.string.text_stop_timer), color = TextPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Focus Timer Presets
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = LumiGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(
                                text = stringResource(R.string.text_focus_session_timers),
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                        ) {
                            listOf(
                                Triple(15, "15 min", "Quick Reset"),
                                Triple(25, "25 min", "Pomodoro"),
                                Triple(45, "45 min", "Deep Flow")
                            ).forEach { (minutes, label, sub) ->
                                Surface(
                                    color = SurfaceDarkVariant,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            onAction(com.example.presentation.viewmodel.LumiUiAction.StartFocusTimer(minutes))
                                            Toast.makeText(context, "Started $minutes min focus timer with ${soundState.activeType.title}", Toast.LENGTH_SHORT).show()
                                        }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            color = LumiGold,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = sub,
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Volume Control
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                Text(
                                    text = stringResource(R.string.text_soundscape_volume),
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${(soundState.volume * 100).toInt()}%",
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = soundState.volume,
                            onValueChange = { onAction(com.example.presentation.viewmodel.LumiUiAction.SetSoundscapeVolume(it)) },
                            colors = SliderDefaults.colors(
                                thumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                activeTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = SurfaceHighlight
                            )
                        )
                    }
                }
            }

            // Soundscape Options List
            item {
                Text(
                    text = stringResource(R.string.text_select_procedural_soundscape),
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall)
                )
            }

            items(SoundscapeType.entries.toTypedArray()) { type ->
                val isSelected = soundState.activeType == type
                Surface(
                    color = if (isSelected) SurfaceHighlight else SurfaceDark,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onAction(com.example.presentation.viewmodel.LumiUiAction.StartSoundscape(type))
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else SurfaceDarkVariant,
                            shape = CircleShape,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = type.iconEmoji, fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = type.title,
                                color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = type.description,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (isSelected && soundState.isPlaying) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
