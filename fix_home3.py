import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Let's fix the layout properly.
# The layout should be:
# Box(fillMaxSize) {
#    // Background is in Box modifier
#    Box(align(Center)) { Column { Pet } }
#    Row(align(TopCenter)) { Top HUD }
#    Column(align(BottomCenter)) { Up Next & Dock }
# }

# We will just write a cleaner version from scratch for the layout part to avoid regex hell.
import sys

def get_full_file():
    return """package com.example.ui.screens

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.PetEmotion
import com.example.service.NetworkType
import com.example.service.BatteryStatus
import com.example.service.NetworkStatus
import com.example.service.PetOverlayService
import com.example.ui.pet.LumiPetView
import com.example.ui.theme.*
import com.example.ui.viewmodel.LumiViewModel
import com.example.ui.navigation.NavDestination

@Composable
fun HomeScreen(
    viewModel: LumiViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToLifeHub: (subTab: Int) -> Unit
) {
    val context = LocalContext.current
    val petStatus by viewModel.petStatus.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val events by viewModel.allCalendarEvents.collectAsState()
    
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        when (petStatus.currentEmotion) {
                            PetEmotion.HAPPY, PetEmotion.ENERGETIC -> LumiCyan.copy(alpha = 0.2f)
                            PetEmotion.LOVING -> LumiPink.copy(alpha = 0.2f)
                            PetEmotion.CALM, PetEmotion.SLEEPY -> LumiMint.copy(alpha = 0.2f)
                            PetEmotion.CONCERNED -> LumiGold.copy(alpha = 0.2f)
                            PetEmotion.THINKING -> LumiViolet.copy(alpha = 0.2f)
                            else -> LumiCyan.copy(alpha = 0.2f)
                        },
                        Color(0xFF16161E),
                        ObsidianDark
                    ),
                    radius = 1200f
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // --- CENTER: LUMI PET ---
        Box(
            modifier = Modifier.align(Alignment.Center).offset(y = (-40).dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Speech bubble
                petStatus.speechBubbleText?.let { text ->
                    Surface(
                        color = ObsidianDark.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .padding(start = 32.dp, end = 32.dp, bottom = 16.dp)
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                LumiPetView(
                    petStatus = petStatus,
                    size = 320.dp, // Larger for game-like feel
                    onPetTouched = {
                        viewModel.onPetTouched()
                        // Instead of navigating to text chat, instantly launch Live Voice Mode (Talking with AI)
                        viewModel.startVoiceListening()
                    },
                    onPetPetted = { viewModel.onPetPetted() },
                    modifier = Modifier.testTag("lumi_pet_view")
                )
            }
        }

        // --- TOP HUD ---
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Top Left: Premium Game-Style Stats
            Surface(
                color = SurfaceDark.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Status Bars
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Level
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = SurfaceDarkVariant,
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

                        // HP / Battery Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "HP", 
                                color = LumiGreen, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.width(20.dp)
                            )
                            val batteryProgress = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceDarkVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(batteryProgress)
                                        .height(10.dp)
                                        .background(if (batteryStatus.isCharging) LumiMint else if (batteryStatus.isLow) LumiPink else LumiGreen)
                                )
                            }
                        }
                        
                        // XP Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "XP", 
                                color = LumiViolet, 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.width(20.dp)
                            )
                            val xpProgress = (petStatus.exp.toFloat() / petStatus.expToNextLevel).coerceIn(0f, 1f)
                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SurfaceDarkVariant)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(xpProgress)
                                        .height(10.dp)
                                        .background(LumiViolet)
                                )
                            }
                        }

                        // Connectivity Icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            val netIcon = when (networkStatus.type) {
                                NetworkType.WIFI -> Icons.Default.Wifi
                                NetworkType.CELLULAR -> Icons.Default.SignalCellularAlt
                                NetworkType.ETHERNET -> Icons.Default.Wifi
                                NetworkType.OFFLINE -> Icons.Default.WifiOff
                            }
                            Icon(
                                imageVector = netIcon,
                                contentDescription = "Connection",
                                tint = if (networkStatus.isConnected) LumiCyan else LumiPink,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (networkStatus.isConnected) "Online" else "Offline",
                                color = if (networkStatus.isConnected) LumiCyan else LumiPink,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            } // Close Surface

            // Top Right: Profile & Action Icons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User Profile Button
                Surface(
                    color = SurfaceDarkVariant,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { viewModel.setSelectedTab(NavDestination.Account.tabIndex) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = TextPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Action Icons (Nap/Wake)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.togglePetSleep() },
                        modifier = Modifier.background(SurfaceDarkVariant.copy(alpha = 0.5f), CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = if (petStatus.currentEmotion == PetEmotion.SLEEPY) Icons.Default.WbSunny else Icons.Default.Bedtime, contentDescription = "Nap/Wake", tint = LumiGold, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Context & Bottom Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Up Next Card (Contextual Intelligence)
            val nextTask = tasks.firstOrNull { !it.isCompleted }
            if (nextTask != null) {
                Surface(
                    color = SurfaceDark.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = LumiYellow.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = "Up Next", tint = LumiYellow, modifier = Modifier.padding(6.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Up Next", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(nextTask.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                        }
                    }
                }
            }

            // Floating Enterprise Dock
            Surface(
                color = SurfaceDark.copy(alpha = 0.85f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Wellness
                    IconButton(onClick = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) }) {
                        Icon(Icons.Default.SelfImprovement, contentDescription = "Wellness", tint = LumiPink, modifier = Modifier.size(26.dp))
                    }
                    
                    // Vision
                    IconButton(onClick = { viewModel.setShowCamera(true) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Vision", tint = LumiGreen, modifier = Modifier.size(26.dp))
                    }

                    // Primary Chat/Voice button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(LumiCyan, LumiViolet)),
                                shape = CircleShape
                            )
                            .clickable { onNavigateToChat() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening || isSpeaking) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Voice", tint = Color.White, modifier = Modifier.size(28.dp).clickable { viewModel.stopVoiceListening() })
                        } else {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    
                    // Wardrobe
                    IconButton(onClick = { viewModel.setShowWardrobeScreen(true) }) {
                        Icon(Icons.Default.Checkroom, contentDescription = "Wardrobe", tint = LumiGold, modifier = Modifier.size(26.dp))
                    }

                    // Life Hub
                    IconButton(onClick = { viewModel.setSelectedTab(NavDestination.LifeHub.tabIndex) }) {
                        Icon(Icons.Default.Dashboard, contentDescription = "Life Hub", tint = LumiYellow, modifier = Modifier.size(26.dp))
                    }
                }
            }
        }
    }
}
"""

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(get_full_file())

