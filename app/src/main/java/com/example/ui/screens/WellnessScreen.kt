package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiGold
import com.example.ui.theme.LumiGreen
import com.example.ui.theme.LumiMint
import com.example.ui.theme.LumiPink
import com.example.ui.theme.LumiViolet
import com.example.ui.theme.LumiYellow
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LumiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WellnessScreen(
    viewModel: LumiViewModel,
    onNavigateToChat: () -> Unit
) {
    val logs by viewModel.allWellnessLogs.collectAsState()
    val memories by viewModel.allMemories.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var moodScore by remember { mutableFloatStateOf(8f) }
    var energyLevel by remember { mutableFloatStateOf(7f) }
    var hydrationCups by remember { mutableIntStateOf(4) }
    var gratitudeText by remember { mutableStateOf("") }
    var isSubmittedToday by remember { mutableStateOf(false) }

    val moodEmojis = listOf("😔", "😕", "😐", "🙂", "😊", "🤩")
    val selectedEmojiIndex = ((moodScore - 1) / 1.8f).toInt().coerceIn(0, moodEmojis.size - 1)

    val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Holistic Wellness",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mindfulness, hydration & energy balance",
                        color = LumiPink,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.setShowBreathing(true) },
                    modifier = Modifier
                        .size(42.dp)
                        .background(LumiGreen.copy(alpha = 0.2f), CircleShape)
                        .testTag("wellness_breathing_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Breathing Exercise",
                        tint = LumiGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Daily Check-In Interactive Card
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = moodEmojis[selectedEmojiIndex],
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mood Slider
                    Text(
                        text = "Mood State: ${moodScore.toInt()}/10",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
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

                    // Energy Level Slider
                    Text(
                        text = "Energy Battery: ${energyLevel.toInt()}/10",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = energyLevel,
                        onValueChange = { energyLevel = it },
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
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hydration: $hydrationCups cups (Goal: 8)",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                        }

                        Row {
                            IconButton(
                                onClick = { if (hydrationCups > 0) hydrationCups-- },
                                modifier = Modifier.size(32.dp).background(SurfaceHighlight, CircleShape)
                            ) {
                                Text("-", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { hydrationCups++ },
                                modifier = Modifier.size(32.dp).background(LumiCyan, CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Cup", tint = ObsidianDark, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gratitude Note
                    OutlinedTextField(
                        value = gratitudeText,
                        onValueChange = { gratitudeText = it },
                        placeholder = { Text("What are you grateful for today?", color = TextSecondary, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiPink,
                            unfocusedBorderColor = SurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gratitude_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val moodLabel = when {
                                moodScore >= 8 -> "Joyful & Centered"
                                moodScore >= 6 -> "Balanced & Calm"
                                moodScore >= 4 -> "Neutral"
                                else -> "Needs Rejuvenation"
                            }
                            viewModel.logWellness(
                                moodScore = moodScore.toInt(),
                                moodLabel = moodLabel,
                                energyLevel = energyLevel.toInt(),
                                hydrationCups = hydrationCups,
                                gratitude = gratitudeText
                            )
                            isSubmittedToday = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiPink, contentColor = ObsidianDark),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("save_wellness_button")
                    ) {
                        Text(
                            text = if (isSubmittedToday) "Logged! ✨" else "Save Daily Check-In (+25 Pet XP)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Biometric Secured Private Memory & Reflection Vault
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
                                contentDescription = "Vault Security",
                                tint = if (uiState.isMemoryVaultUnlocked) LumiGreen else LumiGold,
                                modifier = Modifier.size(20.dp)
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
                            Button(
                                onClick = { viewModel.lockMemoryVault() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight, contentColor = TextSecondary),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Lock", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!uiState.isMemoryVaultUnlocked) {
                        Text(
                            text = "Lumi's long-term memory bank and confidential thoughts are secured with Android Biometrics.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.unlockMemoryVault() },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiGold, contentColor = ObsidianDark),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("unlock_biometric_vault_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock with Fingerprint / PIN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        uiState.vaultAuthError?.let { err ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = err, color = LumiPink, fontSize = 11.sp)
                        }
                    } else {
                        // Unlocked State: Show Lumi's Learned Memory Bank
                        Text(
                            text = "Unlocked: Lumi's Persistent Memory Bank (${memories.size} items stored)",
                            color = LumiGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (memories.isEmpty()) {
                            Text(
                                text = "Lumi hasn't learned memories yet. Chat with Lumi about your favorite hobbies, routines, and goals!",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            memories.forEach { memory ->
                                Surface(
                                    color = SurfaceDarkVariant,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = null,
                                            tint = LumiViolet,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = memory.category.uppercase(),
                                                color = LumiViolet,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = memory.memoryText,
                                                color = TextPrimary,
                                                fontSize = 12.sp
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

        // Wellness Log History
        item {
            Text(
                text = "Recent Wellness Logs",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (logs.isEmpty()) {
            item {
                Text(
                    text = "No previous logs. Complete your first check-in above!",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        } else {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = log.moodLabel,
                                    color = LumiPink,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mood ${log.moodScore}/10 • Energy ${log.energyLevel}/10",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💧 ${log.hydrationCups} cups water • ${dateFormat.format(Date(log.timestamp))}",
                                color = LumiCyan,
                                fontSize = 11.sp
                            )
                            if (!log.gratitudeNote.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${log.gratitudeNote}\"",
                                    color = TextPrimary.copy(alpha = 0.85f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.incrementHydration(log.id) },
                            modifier = Modifier
                                .size(34.dp)
                                .background(LumiCyan.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = "Add Water", tint = LumiCyan, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
