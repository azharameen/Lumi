package com.example.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import com.example.data.remote.AiRoutingMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.domain.model.PetStatus
import com.example.service.NetworkType
import com.example.service.PetOverlayService
import com.example.ui.components.DailyBriefingCard
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.pet.LumiPetView
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
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.LumiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Star
import com.example.domain.model.PetAccessory

@Composable
fun HomeScreen(
    viewModel: LumiViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToLifeHub: (subTab: Int) -> Unit
) {
    val context = LocalContext.current
    val petStatus by viewModel.petStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val events by viewModel.allCalendarEvents.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val audioLevel by viewModel.voiceEngine.audioWaveformLevel.collectAsState()
    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val locationContext by viewModel.locationContext.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val headsetStatus by viewModel.headsetStatus.collectAsState()
    val zenStatus by viewModel.zenStatus.collectAsState()
    val dailyBriefing by viewModel.dailyBriefing.collectAsState()
    val isBriefingGenerating by viewModel.isBriefingGenerating.collectAsState()
    val isBriefingSpeaking by viewModel.isBriefingSpeaking.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Native System Telemetry Dashboard Pill (Battery, WiFi, Headset, Zen DND, Location)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkVariant, RoundedCornerShape(16.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Row: Hardware Sensors (Battery, Network, Headset, Zen)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Battery
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (batteryStatus.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                            contentDescription = "Battery",
                            tint = if (batteryStatus.isCharging) LumiMint else if (batteryStatus.isLow) LumiPink else LumiCyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${batteryStatus.levelPercent}%" + if (batteryStatus.isCharging) " ⚡" else "",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Network State
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val netIcon = when (networkStatus.type) {
                            NetworkType.WIFI -> Icons.Default.Wifi
                            NetworkType.CELLULAR -> Icons.Default.SignalCellularAlt
                            NetworkType.ETHERNET -> Icons.Default.Wifi
                            NetworkType.OFFLINE -> Icons.Default.WifiOff
                        }
                        val netTint = if (networkStatus.isConnected) LumiCyan else LumiPink
                        Icon(
                            imageVector = netIcon,
                            contentDescription = "Network",
                            tint = netTint,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = networkStatus.description,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    // Audio Headset
                    if (headsetStatus.isHeadsetConnected) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (headsetStatus.isBluetooth) Icons.Default.Bluetooth else Icons.Default.Headphones,
                                contentDescription = "Audio Device",
                                tint = LumiViolet,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = headsetStatus.deviceName,
                                color = LumiViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Zen / DND Mode
                    if (zenStatus.isDndActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DoNotDisturbOn,
                                contentDescription = "Zen DND Active",
                                tint = LumiGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Zen",
                                color = LumiGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bottom Row: Location Telemetry
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.refreshLocation() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = LumiGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (locationContext.hasPermission) locationContext.approximatePlace else "Tap to detect local sanctuary location",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // AI Engine Telemetry & Analytics Pill
                val routingMode by viewModel.aiRoutingMode.collectAsState()
                val aiLogs by viewModel.aiExecutionLogs.collectAsState()

                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigateToLifeHub(3) }
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "AI Engine",
                            tint = when (routingMode) {
                                AiRoutingMode.STRICT_ON_DEVICE -> LumiGreen
                                AiRoutingMode.CLOUD_TURBO -> LumiViolet
                                else -> LumiCyan
                            },
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (routingMode) {
                                AiRoutingMode.STRICT_ON_DEVICE -> "On-Device Gemma (100% Offline)"
                                AiRoutingMode.CLOUD_TURBO -> "Cloud Gemini 2.5 Flash Turbo"
                                else -> "Smart Hybrid AI (Gemma + Gemini)"
                            },
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${aiLogs.size} runs",
                            color = LumiGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = "Analytics",
                            tint = LumiGold,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        // 2. Incoming Shared Content Notification Banner
        if (uiState.sharedIncomingBanner != null) {
            item {
                Surface(
                    color = LumiCyan.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LumiCyan.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = LumiCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.sharedIncomingBanner!!,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissSharedBanner() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 3. Clipboard Smart Assistant Trigger Banner
        if (uiState.detectedClipboardText != null) {
            item {
                Surface(
                    color = LumiViolet.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LumiViolet.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = null,
                                tint = LumiViolet,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Copied text detected",
                                    color = LumiViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "\"${uiState.detectedClipboardText}\"",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row {
                            Button(
                                onClick = { viewModel.processClipboardWithLumi(uiState.detectedClipboardText!!) },
                                colors = ButtonDefaults.buttonColors(containerColor = LumiViolet, contentColor = ObsidianDark),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Ask Lumi", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.dismissClipboardSnippet() },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 4. Autonomous Daily AI Intelligence Briefing
        item {
            DailyBriefingCard(
                briefing = dailyBriefing,
                isGenerating = isBriefingGenerating,
                isSpeaking = isBriefingSpeaking,
                onRefreshBriefing = { type -> viewModel.refreshDailyBriefing(type) },
                onToggleAudioPlay = { briefing -> viewModel.playBriefingAudio(briefing) },
                onActionClicked = { actionType ->
                    when (actionType) {
                        "BREATHING" -> viewModel.setShowBreathing(true)
                        "HYDRATE" -> onNavigateToLifeHub(2) // Wellness tab
                        "TASKS" -> onNavigateToLifeHub(1) // Tasks tab
                        "SCHEDULE" -> onNavigateToLifeHub(0) // Schedule tab
                        else -> onNavigateToChat()
                    }
                }
            )
        }

        // 5. Top Level & XP Bar + Floating Pet Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lumi Companion",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = LumiCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Lv.${petStatus.level}",
                                    color = LumiCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = petStatus.personalityTrait,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Exp bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.width(180.dp)
                        ) {
                            val progress = (petStatus.exp.toFloat() / petStatus.expToNextLevel).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = LumiCyan,
                                trackColor = SurfaceHighlight
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${petStatus.exp}/${petStatus.expToNextLevel} XP",
                                color = LumiCyan,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Floating overlay toggle
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Screen Pet",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = uiState.isOverlayEnabled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                                            viewModel.setShowOverlayPermission(true)
                                        } else {
                                            viewModel.setOverlayEnabled(true)
                                            val serviceIntent = Intent(context, PetOverlayService::class.java)
                                            ContextCompat.startForegroundService(context, serviceIntent)
                                        }
                                    } else {
                                        viewModel.setOverlayEnabled(false)
                                        val serviceIntent = Intent(context, PetOverlayService::class.java)
                                        context.stopService(serviceIntent)
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ObsidianDark,
                                    checkedTrackColor = LumiCyan,
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = SurfaceHighlight
                                ),
                                modifier = Modifier.testTag("floating_pet_toggle")
                            )
                        }
                    }
                }
            }
        }

        // 5. Interactive 3D Pet Hero & Live Speech Bubble
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SurfaceDarkVariant, SurfaceDark)
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Speech bubble
                petStatus.speechBubbleText?.let { text ->
                    Surface(
                        color = ObsidianDark.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable { onNavigateToChat() }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LumiPink,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = text,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3D Procedural Pet View
                LumiPetView(
                    petStatus = petStatus,
                    size = 230.dp,
                    onPetTouched = { viewModel.onPetTouched() },
                    onPetPetted = { viewModel.onPetPetted() },
                    modifier = Modifier.testTag("lumi_pet_view")
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap to pet • Drag to gaze • Double-finger stroke to purr",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Realtime Voice Waveform & Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vision Camera trigger
                    IconButton(
                        onClick = { viewModel.setShowCamera(true) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(SurfaceHighlight, CircleShape)
                            .testTag("camera_vision_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Let Lumi See",
                            tint = LumiCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Main Voice Mic Interaction Button
                    Button(
                        onClick = {
                            if (isListening) {
                                viewModel.stopVoiceListening()
                            } else {
                                viewModel.startVoiceListening()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) LumiPink else LumiCyan,
                            contentColor = ObsidianDark
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("voice_talk_button")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Voice Talk",
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isListening) "Listening..." else "Talk with Lumi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Mindfulness Breath trigger
                    IconButton(
                        onClick = { viewModel.setShowBreathing(true) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(SurfaceHighlight, CircleShape)
                            .testTag("breathing_trigger_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Breathe with Lumi",
                            tint = LumiGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Audio Waveform Visualizer
                AnimatedVisibility(visible = isListening || petStatus.isSpeaking) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        VoiceWaveformVisualizer(
                            isActive = isListening || petStatus.isSpeaking,
                            audioLevel = audioLevel
                        )
                    }
                }
            }
        }

        // 6. Interactive Wardrobe & Accessories Studio
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = LumiGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Lumi's Wardrobe",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Bond ${petStatus.bondScore}%",
                            color = LumiPink,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PetAccessory.entries) { acc ->
                            val isUnlocked = petStatus.level >= acc.requiredLevel
                            val isEquipped = petStatus.activeAccessory == acc

                            val emoji = when (acc) {
                                PetAccessory.NONE -> "✨"
                                PetAccessory.SPROUT -> "🌱"
                                PetAccessory.GLASSES -> "👓"
                                PetAccessory.HEADPHONES -> "🎧"
                                PetAccessory.HALO -> "😇"
                                PetAccessory.CROWN -> "👑"
                            }

                            Surface(
                                color = if (isEquipped) LumiCyan.copy(alpha = 0.2f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = if (isEquipped) androidx.compose.foundation.BorderStroke(1.5.dp, LumiCyan) else null,
                                modifier = Modifier
                                    .width(105.dp)
                                    .clickable(enabled = isUnlocked) {
                                        viewModel.setAccessory(acc)
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 22.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = acc.displayName,
                                        color = if (isUnlocked) TextPrimary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isEquipped) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (isEquipped) "Equipped" else if (isUnlocked) "Wear" else "Lv.${acc.requiredLevel}",
                                        color = if (isEquipped) LumiCyan else if (isUnlocked) LumiMint else TextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 7. Quick Action Chips to Life Hub & Tools
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    QuickActionChip(
                        icon = Icons.Default.GraphicEq,
                        label = "Live Voice",
                        color = LumiCyan,
                        onClick = { viewModel.setShowLiveVoiceMode(true) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.CalendarMonth,
                        label = "Schedule",
                        color = LumiCyan,
                        onClick = { onNavigateToLifeHub(0) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.CheckCircleOutline,
                        label = "Tasks",
                        color = LumiYellow,
                        onClick = { onNavigateToLifeHub(1) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.AutoAwesome,
                        label = "Goal Swarms",
                        color = LumiViolet,
                        onClick = { onNavigateToLifeHub(2) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.Headphones,
                        label = "Focus Audio",
                        color = LumiMint,
                        onClick = { onNavigateToLifeHub(3) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.SelfImprovement,
                        label = "Wellness",
                        color = LumiPink,
                        onClick = { onNavigateToLifeHub(4) }
                    )
                }
                item {
                    QuickActionChip(
                        icon = Icons.Default.Air,
                        label = "4-7-8 Breath",
                        color = LumiGreen,
                        onClick = { viewModel.setShowBreathing(true) }
                    )
                }
            }
        }

        // 8. Daily Schedule Summary Card (Life Hub Link)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLifeHub(0) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = LumiCyan)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Today's Agenda",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(text = "Open Schedule →", color = LumiCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (events.isEmpty()) {
                        Text(
                            text = "No events scheduled today. Tap to plan in Life Hub!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        events.take(2).forEach { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(LumiCyan, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${timeFormat.format(Date(event.startTimeMillis))} - ${event.title}",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 9. Active Tasks Checklist Card (Life Hub Link)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLifeHub(1) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = LumiYellow)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pending Tasks",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(text = "${tasks.count { !it.isCompleted }} open →", color = LumiYellow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val openTasks = tasks.filter { !it.isCompleted }.take(2)
                    if (openTasks.isEmpty()) {
                        Text(
                            text = "All clear! You're completely caught up.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    } else {
                        openTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTask(task.id, it) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = LumiYellow,
                                        uncheckedColor = TextSecondary
                                    ),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}
