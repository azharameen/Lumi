package com.example.ui.screens
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

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

import com.example.ui.navigation.NavDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    petStatus: com.example.domain.model.PetStatus,
    uiState: com.example.ui.viewmodel.LumiUiState,
    batteryStatus: com.example.service.BatteryStatus,
    networkStatus: com.example.service.NetworkStatus,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    tasks: List<com.example.data.local.entity.TaskEntity>,
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
    onNavigateToWellness: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(petStatus.bloubSkinColor.primaryHex).copy(alpha = 0.25f),
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
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(petStatus.bloubSkinColor.primaryHex),
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = text,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .heightIn(max = 120.dp)
                                    .verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }

                LumiPetView(
                    petStatus = petStatus,
                    size = 320.dp, // Larger for game-like feel
                    onPetTouched = {
                        onPetTouched()
                        // Instead of navigating to text chat, instantly launch Live Voice Mode (Talking with AI)
                        onStartVoiceListening()
                    },
                    onPetPetted = { onPetPetted() },
                    modifier = Modifier.testTag("lumi_pet_view")
                )
            }
        }

        // --- TOP HUD ---
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
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
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
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
                            Text(
                                text = "${batteryStatus.levelPercent}%",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // XP Bar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "XP", 
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary, 
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
                                        .background(androidx.compose.material3.MaterialTheme.colorScheme.primary)
                                )
                            }
                            Text(
                                text = "${petStatus.exp}/${petStatus.expToNextLevel}",
                                color = TextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
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
                                tint = if (networkStatus.isConnected) androidx.compose.material3.MaterialTheme.colorScheme.primary else LumiPink,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (networkStatus.isConnected) "Online" else "Offline",
                                color = if (networkStatus.isConnected) androidx.compose.material3.MaterialTheme.colorScheme.primary else LumiPink,
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
                        .clickable { onNavigateToAccount() }
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
                        onClick = { onTogglePetSleep() },
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
            // Pet Action Buttons Toolbar (Floating Enterprise Dock)
            Surface(
                color = SurfaceDark.copy(alpha = 0.85f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vision
                    IconButton(onClick = { onShowCamera() }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Vision", tint = LumiGreen, modifier = Modifier.size(26.dp))
                    }

                    // Primary Chat/Voice button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(Color(petStatus.bloubSkinColor.primaryHex), Color(petStatus.bloubSkinColor.endHex))),
                                shape = CircleShape
                            )
                            .clickable { onNavigateToChat() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening || isSpeaking) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Voice", tint = Color.White, modifier = Modifier.size(28.dp).clickable { onStopVoiceListening() })
                        } else {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    
                    // Wardrobe
                    IconButton(onClick = { onShowWardrobe() }) {
                        Icon(Icons.Default.Checkroom, contentDescription = "Wardrobe", tint = LumiGold, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // Bottom Items Slider
            val pendingTasks = tasks.filter { !it.isCompleted }
            var showTasksBottomSheet by remember { mutableStateOf(false) }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Life Hub (Always Visible)
                item {
                    CompactIconButton(
                        icon = Icons.Default.Dashboard, 
                        label = "Life Hub", 
                        color = LumiYellow, 
                        onClick = { onNavigateToLifeHub(0) }
                    )
                }

                // Wellness (Always Visible)
                item {
                    CompactIconButton(
                        icon = Icons.Default.SelfImprovement, 
                        label = "Wellness", 
                        color = LumiPink, 
                        onClick = { onNavigateToWellness() }
                    )
                }

                if (pendingTasks.isNotEmpty()) {
                    val displayTasks = pendingTasks.take(2)
                    items(displayTasks) { task ->
                        TaskCard(task)
                    }

                    if (pendingTasks.size > 2) {
                        item {
                            Surface(
                                color = SurfaceDark.copy(alpha = 0.6f),
                                shape = CircleShape,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f)),
                                modifier = Modifier.size(56.dp)
                            ) {
                                IconButton(onClick = { showTasksBottomSheet = true }) {
                                    Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
            
            if (showTasksBottomSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showTasksBottomSheet = false },
                    containerColor = SurfaceDark,
                    contentColor = TextPrimary,
                    scrimColor = Color.Black.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("All Pending Tasks", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(pendingTasks) { task ->
                                TaskCard(task = task, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CompactIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.padding(6.dp))
            }
            Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TaskCard(task: TaskEntity, modifier: Modifier = Modifier.width(220.dp)) {
    Surface(
        color = SurfaceDark.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f)),
        modifier = modifier
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
                Icon(imageVector = Icons.Default.Bolt, contentDescription = "Task", tint = LumiYellow, modifier = Modifier.padding(6.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("Up Next", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(task.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
    }
}
