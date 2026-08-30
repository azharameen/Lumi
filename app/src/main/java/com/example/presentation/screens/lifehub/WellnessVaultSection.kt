package com.example.presentation.screens.lifehub
import androidx.compose.ui.res.stringResource
import com.example.R


import com.example.presentation.components.*

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.core.theme.LumiCoral
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiGreen
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiYellow
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceDarkVariant
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import com.example.presentation.viewmodel.LumiViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun WellnessVaultSection(
    wellnessLogs: List<WellnessLogEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    uiState: com.example.presentation.viewmodel.LumiUiState,
    viewModel: LumiViewModel,
    onNavigateToChat: (String?) -> Unit
) {
    var moodScore by remember { mutableFloatStateOf(8f) }
    var energyScore by remember { mutableFloatStateOf(8f) }
    var waterCount by remember { mutableIntStateOf(3) }
    var gratitudeText by remember { mutableStateOf("") }
    var isSubmittedToday by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coherence Breathing Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setShowBreathing(true) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(LumiGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = LumiGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "4-7-8 Coherence Breathing",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calm your vagus nerve with guided tactile pacing",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.setShowBreathing(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Start", color = ObsidianDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Daily Check-In Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Wellness Check-In",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = null, tint = LumiPink)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mood Slider
                    Text(
                        text = "Mood Balance: ${moodScore.toInt()}/10 ${getMoodEmoji(moodScore.toInt())}",
                        color = LumiPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = moodScore,
                        onValueChange = { moodScore = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = LumiPink,
                            activeTrackColor = LumiPink,
                            inactiveTrackColor = SurfaceHighlight
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Energy Slider
                    Text(
                        text = "Energy Level: ${energyScore.toInt()}/10 ⚡",
                        color = LumiYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = energyScore,
                        onValueChange = { energyScore = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = LumiYellow,
                            activeTrackColor = LumiYellow,
                            inactiveTrackColor = SurfaceHighlight
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hydration Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hydration: $waterCount / 8 cups",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { if (waterCount > 0) waterCount-- },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(SurfaceDarkVariant, CircleShape)
                            ) {
                                Text(text = "−", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { waterCount++ },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Text(text = "+", color = ObsidianDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gratitude TextField
                    OutlinedTextField(
                        value = gratitudeText,
                        onValueChange = { gratitudeText = it },
                        label = { Text(stringResource(id = R.string.text_what_are_you_grateful_for_toda)) },
                        placeholder = { Text(stringResource(id = R.string.text_a_productive_morning_good_coff)) },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiPink,
                            unfocusedBorderColor = SurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.logWellness(
                                moodScore = moodScore.toInt(),
                                moodLabel = getMoodLabel(moodScore.toInt()),
                                energyLevel = energyScore.toInt(),
                                hydrationCups = waterCount,
                                gratitude = gratitudeText.ifBlank { "Daily check-in completed" }
                            )
                            isSubmittedToday = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiPink),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSubmittedToday) "✓ Saved & Synced with Lumi!" else "Save Wellness Check-In",
                            color = ObsidianDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Biometric Secured Memory Vault
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isMemoryVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.isMemoryVaultUnlocked) LumiGreen else androidx.compose.material3.MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Biometric Memory Vault",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.isMemoryVaultUnlocked) {
                            TextButton(onClick = { viewModel.lockMemoryVault() }) {
                                Text(text = "Lock Vault", color = LumiPink, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!uiState.isMemoryVaultUnlocked) {
                        Text(
                            text = "Lumi securely stores your habits, preferences, and personal insights. Authenticate with biometrics to unlock.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.unlockMemoryVault() },
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_unlock_vault")
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Unlock with Biometrics", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        if (memories.isEmpty()) {
                            Text(
                                text = "Vault unlocked. No memories logged yet. As you converse with Lumi, learned preferences will be securely archived here.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                memories.forEach { mem ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = mem.category.uppercase(),
                                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${mem.sentiment} • Impact ${mem.emotionalImpact}/5",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = mem.memoryText,
                                                color = TextPrimary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
