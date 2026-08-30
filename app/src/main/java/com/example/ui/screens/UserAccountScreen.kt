package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.remote.HardwareAccelerator
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadStatus
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.account.LumiPersonaTone
import com.example.domain.account.UserFactItem
import com.example.domain.account.UserProfileData
import com.example.domain.connectors.ConnectorManager
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

@Composable
fun UserAccountScreen(
    viewModel: LumiViewModel,
    onNavigateToChat: ((String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val userFacts by viewModel.userFacts.collectAsState()
    val petStatus by viewModel.petStatus.collectAsState()
    val benchmarkStatus by viewModel.benchmarkStatus.collectAsState()

    val tasks by viewModel.allTasks.collectAsState()
    val events by viewModel.allCalendarEvents.collectAsState()
    val memories by viewModel.allMemories.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()

    var selectedSectionIndex by remember { mutableIntStateOf(0) }
    val sectionTabs = listOf("Profile & Persona", "Memory Feeder", "Connectors", "LLM Settings", "Privacy & Vault")

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddFactDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Account Hero Header
            Surface(
                color = SurfaceDark,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Gradient Avatar Ring
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(LumiMint, LumiCyan, LumiViolet)
                                        )
                                    )
                                    .padding(2.5.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Avatar",
                                    tint = LumiMint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = userProfile.userName,
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = userProfile.userEmail,
                                    color = LumiMint,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Quick Edit Profile Button
                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .background(SurfaceDarkVariant, CircleShape)
                                .size(38.dp)
                                .testTag("btn_edit_profile")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = LumiMint,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bio and Companion Sync Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = userProfile.roleOrTitle,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Surface(
                            color = LumiMint.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LumiMint,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lumi Synchronized",
                                    color = LumiMint,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Section Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedSectionIndex,
                containerColor = SurfaceDarkVariant,
                contentColor = LumiMint,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSectionIndex]),
                        color = when (selectedSectionIndex) {
                            0 -> LumiMint
                            1 -> LumiCyan
                            2 -> LumiGold
                            3 -> LumiViolet
                            else -> LumiPink
                        },
                        height = 3.dp
                    )
                }
            ) {
                sectionTabs.forEachIndexed { index, title ->
                    val isSelected = selectedSectionIndex == index
                    val tabColor = when (index) {
                        0 -> LumiMint
                        1 -> LumiCyan
                        2 -> LumiGold
                        3 -> LumiViolet
                        else -> LumiPink
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { selectedSectionIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (isSelected) tabColor else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = when (index) {
                                    0 -> Icons.Default.AccountCircle
                                    1 -> Icons.Default.Lightbulb
                                    2 -> Icons.Default.Hub
                                    3 -> Icons.Default.Psychology
                                    else -> Icons.Default.Security
                                },
                                contentDescription = title,
                                tint = if (isSelected) tabColor else TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("account_tab_$index")
                    )
                }
            }

            // 3. Section Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedSectionIndex) {
                    0 -> ProfileAndPersonaSection(
                        userProfile = userProfile,
                        onUpdateProfile = { updated -> viewModel.updateUserProfile(updated) },
                        onEditClicked = { showEditProfileDialog = true }
                    )
                    1 -> MemoryFeederSection(
                        facts = userFacts,
                        onAddFactClicked = { showAddFactDialog = true },
                        onDeleteFact = { id -> viewModel.removeUserFact(id) },
                        onTogglePin = { id -> viewModel.togglePinFact(id) }
                    )
                    2 -> ConnectorsControlSection(
                        viewModel = viewModel
                    )
                    3 -> LlmSettingsSection(
                        userProfile = userProfile,
                        viewModel = viewModel,
                        benchmarkStatus = benchmarkStatus,
                        onUpdateProfile = { updated -> viewModel.updateUserProfile(updated) }
                    )
                    4 -> PrivacyAndVaultSection(
                        userProfile = userProfile,
                        taskCount = tasks.size,
                        eventCount = events.size,
                        memoryCount = memories.size,
                        messageCount = messages.size,
                        onToggleBiometric = { isEnabled ->
                            viewModel.updateUserProfile(userProfile.copy(enableBiometricLock = isEnabled))
                        },
                        onResetClicked = { showResetDialog = true }
                    )
                }
            }
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentProfile = userProfile,
                onDismiss = { showEditProfileDialog = false },
                onSave = { updated ->
                    viewModel.updateUserProfile(updated)
                    showEditProfileDialog = false
                    Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Add Custom Fact Memory Dialog
        if (showAddFactDialog) {
            AddFactDialog(
                onDismiss = { showAddFactDialog = false },
                onAddFact = { category, factText, isPinned ->
                    viewModel.addUserFact(category, factText, isPinned)
                    showAddFactDialog = false
                    Toast.makeText(context, "Memory fact added to Lumi's context", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Reset Data Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Reset Memory & History?", color = LumiPink) },
                text = {
                    Text(
                        "This will clear chat conversation logs and reset AI analytics. Your user profile and custom memory facts will remain intact.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.clearAiAnalytics()
                            showResetDialog = false
                            Toast.makeText(context, "Chat and analytics logs cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiPink)
                    ) {
                        Text("Clear Logs", color = ObsidianDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = SurfaceDark
            )
        }
    }
}

// -------------------------------------------------------------
// 1. Profile & Persona Section
// -------------------------------------------------------------
@Composable
private fun ProfileAndPersonaSection(
    userProfile: UserProfileData,
    onUpdateProfile: (UserProfileData) -> Unit,
    onEditClicked: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Identity Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "User Identity & Goals",
                            color = LumiMint,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onEditClicked, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = LumiMint, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ProfileFieldRow(label = "Full Name", value = userProfile.userName, icon = Icons.Default.Person)
                    ProfileFieldRow(label = "Email Address", value = userProfile.userEmail, icon = Icons.Default.Email)
                    ProfileFieldRow(label = "Professional Role", value = userProfile.roleOrTitle, icon = Icons.Default.Code)
                    ProfileFieldRow(label = "Primary Focus Goal", value = userProfile.primaryFocusGoal, icon = Icons.Default.SelfImprovement)
                }
            }
        }

        // Daily Routines & Habit Targets
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Rhythm & Target Metrics",
                        color = LumiCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricBadge(
                            title = "Focus Hours",
                            value = "${userProfile.dailyFocusTargetHours}h / day",
                            icon = Icons.Default.Schedule,
                            color = LumiGold,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            title = "Hydration",
                            value = "${userProfile.targetHydrationCups} cups",
                            icon = Icons.Default.WaterDrop,
                            color = LumiCyan,
                            modifier = Modifier.weight(1f)
                        )
                        MetricBadge(
                            title = "Daily Steps",
                            value = "${userProfile.targetDailySteps}",
                            icon = Icons.Default.FitnessCenter,
                            color = LumiGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RoutineBadge(
                            label = "Wake Routine",
                            time = userProfile.wakeUpTime,
                            color = LumiYellow,
                            modifier = Modifier.weight(1f)
                        )
                        RoutineBadge(
                            label = "Sleep Routine",
                            time = userProfile.sleepTime,
                            color = LumiViolet,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Persona Tone Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Lumi Companion Persona Tone",
                        color = LumiPink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controls how Lumi talks, reasons, and motivates you in chat and notifications.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    LumiPersonaTone.values().forEach { tone ->
                        val isSelected = userProfile.personaTone == tone
                        Surface(
                            color = if (isSelected) tone.accentColor.copy(alpha = 0.15f) else SurfaceDarkVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, tone.accentColor) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onUpdateProfile(userProfile.copy(personaTone = tone)) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(tone.accentColor.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tone.icon,
                                        contentDescription = tone.title,
                                        tint = tone.accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tone.title,
                                        color = if (isSelected) tone.accentColor else TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = tone.description,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = tone.accentColor,
                                        modifier = Modifier.size(20.dp)
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

// -------------------------------------------------------------
// 2. Memory Feeder Section (User Context & Personal Facts)
// -------------------------------------------------------------
@Composable
private fun MemoryFeederSection(
    facts: List<UserFactItem>,
    onAddFactClicked: () -> Unit,
    onDeleteFact: (String) -> Unit,
    onTogglePin: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Work & Code", "Preferences", "Health & Routines", "Routines", "Personal")

    val filteredFacts = if (selectedCategory == "All") facts else facts.filter { it.category == selectedCategory }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Personal Context Feeder",
                                color = LumiCyan,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Facts, preferences & rules fed directly into Lumi's LLM context window.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onAddFactClicked,
                            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_add_fact")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Fact", color = ObsidianDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category Filter Row
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = LumiCyan.copy(alpha = 0.2f),
                                    selectedLabelColor = LumiCyan,
                                    containerColor = SurfaceDarkVariant,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }

        if (filteredFacts.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No memory facts in this category", color = TextPrimary, fontWeight = FontWeight.Medium)
                        Text("Tap 'Add Fact' above to teach Lumi about your work style, projects, or diet!", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(filteredFacts, key = { it.id }) { fact ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        IconButton(
                            onClick = { onTogglePin(fact.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (fact.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Pin Fact",
                                tint = if (fact.isPinned) LumiGold else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                color = LumiCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = fact.category,
                                    color = LumiCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = fact.factText,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }

                        IconButton(
                            onClick = { onDeleteFact(fact.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Fact",
                                tint = TextTertiary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Connectors & Integrations Section
// -------------------------------------------------------------
@Composable
private fun ConnectorsControlSection(
    viewModel: LumiViewModel
) {
    val context = LocalContext.current
    val connectorManager = remember { ConnectorManager(context) }

    val isGoogleConnected by connectorManager.googleConnected.collectAsState()
    val googleEmail by connectorManager.googleAccount.collectAsState()

    val isGithubConnected by connectorManager.githubConnected.collectAsState()
    val githubUser by connectorManager.githubUser.collectAsState()
    val githubToken by connectorManager.githubToken.collectAsState()

    val isSlackConnected by connectorManager.slackConnected.collectAsState()
    val slackChannel by connectorManager.slackChannel.collectAsState()
    val slackWebhook by connectorManager.slackWebhook.collectAsState()

    var showGithubDialog by remember { mutableStateOf(false) }
    var showSlackDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Connected Ecosystem & Tools",
                color = LumiGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Connect Lumi to your digital workspace to autonomously sync events, tasks, notifications, and code.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Google Workspace Card
        item {
            ConnectorCard(
                title = "Google Workspace",
                subtitle = "Google Calendar, Gmail & Tasks Sync",
                accountText = if (isGoogleConnected) googleEmail else "Not connected",
                isConnected = isGoogleConnected,
                accentColor = LumiGreen,
                icon = Icons.Default.Email,
                onToggle = { enable ->
                    connectorManager.setGoogleConnection(enable, if (enable) "azharameen52@gmail.com" else "")
                    Toast.makeText(context, if (enable) "Google Workspace Connected" else "Google Workspace Disconnected", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // GitHub Connector Card
        item {
            ConnectorCard(
                title = "GitHub Developer",
                subtitle = "Repository issues, PR triage, and dispatch",
                accountText = if (isGithubConnected) "@$githubUser" else "Not configured",
                isConnected = isGithubConnected,
                accentColor = LumiViolet,
                icon = Icons.Default.Code,
                onToggle = { enable ->
                    if (enable) {
                        showGithubDialog = true
                    } else {
                        connectorManager.setGithubConnection(false, "", "")
                    }
                },
                onConfigure = { showGithubDialog = true }
            )
        }

        // Slack / Discord Connector Card
        item {
            ConnectorCard(
                title = "Slack & Discord Webhooks",
                subtitle = "Post daily briefings, goal alerts & milestones",
                accountText = if (isSlackConnected) slackChannel else "Not configured",
                isConnected = isSlackConnected,
                accentColor = LumiGold,
                icon = Icons.Default.Hub,
                onToggle = { enable ->
                    if (enable) {
                        showSlackDialog = true
                    } else {
                        connectorManager.setSlackConnection(false, "", "")
                    }
                },
                onConfigure = { showSlackDialog = true }
            )
        }

        // Health & Vitals Connector
        item {
            ConnectorCard(
                title = "Health Connect & Google Fit",
                subtitle = "Step telemetry, sleep logs & resting heart rate",
                accountText = "Active telemetry sync",
                isConnected = true,
                accentColor = LumiPink,
                icon = Icons.Default.FitnessCenter,
                onToggle = { }
            )
        }

        // System Hardware Sensors
        item {
            ConnectorCard(
                title = "Android Hardware Sensors",
                subtitle = "Battery state, Location context & Accelerometer",
                accountText = "Live sensor streaming enabled",
                isConnected = true,
                accentColor = LumiCyan,
                icon = Icons.Default.Bolt,
                onToggle = { }
            )
        }
    }

    // GitHub Config Dialog
    if (showGithubDialog) {
        var userText by remember { mutableStateOf(githubUser) }
        var tokenText by remember { mutableStateOf(githubToken) }

        AlertDialog(
            onDismissRequest = { showGithubDialog = false },
            title = { Text("Configure GitHub Token", color = LumiViolet) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = userText,
                        onValueChange = { userText = it },
                        label = { Text("GitHub Username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiViolet,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tokenText,
                        onValueChange = { tokenText = it },
                        label = { Text("Personal Access Token (PAT)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiViolet,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        connectorManager.setGithubConnection(userText.isNotBlank(), userText, tokenText)
                        showGithubDialog = false
                        Toast.makeText(context, "GitHub connection configured", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumiViolet)
                ) {
                    Text("Save & Connect", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGithubDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Slack Config Dialog
    if (showSlackDialog) {
        var channelText by remember { mutableStateOf(slackChannel) }
        var webhookText by remember { mutableStateOf(slackWebhook) }

        AlertDialog(
            onDismissRequest = { showSlackDialog = false },
            title = { Text("Configure Slack Webhook", color = LumiGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = channelText,
                        onValueChange = { channelText = it },
                        label = { Text("Channel (e.g. #daily-briefings)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGold,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = webhookText,
                        onValueChange = { webhookText = it },
                        label = { Text("Webhook URL") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGold,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        connectorManager.setSlackConnection(webhookText.isNotBlank() || channelText.isNotBlank(), channelText, webhookText)
                        showSlackDialog = false
                        Toast.makeText(context, "Slack connection saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumiGold)
                ) {
                    Text("Save & Connect", color = ObsidianDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlackDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

// -------------------------------------------------------------
// 4. LLM Brain & AI Engine Settings Section (with Local LLM Hub)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LlmSettingsSection(
    userProfile: UserProfileData,
    viewModel: LumiViewModel,
    benchmarkStatus: String?,
    onUpdateProfile: (UserProfileData) -> Unit
) {
    val context = LocalContext.current
    var temperature by remember { mutableFloatStateOf(userProfile.temperature) }
    var customInstructions by remember { mutableStateOf(userProfile.customAiInstructions) }

    val downloadStates by viewModel.modelDownloadStates.collectAsState()
    val activeLocalModelId by viewModel.activeLocalModelId.collectAsState()
    val selectedAccelerator by viewModel.selectedAccelerator.collectAsState()

    val cloudModels = listOf(
        Pair("gemini-2.5-flash", "Gemini 2.5 Flash (Ultra Fast & Multimodal)"),
        Pair("gemini-2.5-pro", "Gemini 2.5 Pro (Deep Reasoning & Tutoring)"),
        Pair("gemini-2.5-flash-lite", "Gemini 2.5 Flash-Lite (Low Latency)"),
        Pair("hybrid-auto", "Hybrid Smart Router (Edge + Cloud)"),
        Pair("on-device-gemma", "On-Device Neural Engine (Offline & Private)")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active Engine Router
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = LumiViolet, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Active LLM Intelligence Engine",
                            color = LumiViolet,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    cloudModels.forEach { (modelId, label) ->
                        val isSelected = userProfile.geminiModelChoice == modelId
                        Surface(
                            color = if (isSelected) LumiViolet.copy(alpha = 0.15f) else SurfaceDarkVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, LumiViolet) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onUpdateProfile(userProfile.copy(geminiModelChoice = modelId)) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (modelId.contains("on-device")) Icons.Default.Memory else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (isSelected) LumiViolet else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) LumiViolet else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = "Active", tint = LumiViolet, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // On-Device Local LLM Model Hub & Downloader
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "On-Device Local LLM Hub",
                                color = LumiCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = LumiCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "100% Offline & Private",
                                color = LumiCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Download genuine GGUF/LiteRT neural weights to execute agentic reasoning, memory synthesis, and offline chat directly on your mobile GPU/NPU without cloud servers.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Hardware Accelerator selection
                    Text(
                        text = "Neural Hardware Acceleration:",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HardwareAccelerator.values().forEach { acc ->
                            val isAccSelected = selectedAccelerator == acc
                            Surface(
                                color = if (isAccSelected) LumiCyan.copy(alpha = 0.2f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = if (isAccSelected) androidx.compose.foundation.BorderStroke(1.dp, LumiCyan) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setHardwareAccelerator(acc) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when (acc) {
                                            HardwareAccelerator.GPU_OPENCL -> "GPU OpenCL"
                                            HardwareAccelerator.NPU_NNAPI -> "NPU NNAPI"
                                            HardwareAccelerator.CPU_MULTITHREAD -> "CPU (4-Core)"
                                        },
                                        color = if (isAccSelected) LumiCyan else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isAccSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceDarkVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Model Catalog List
                    viewModel.localModelCatalog.forEach { modelSpec ->
                        val progress = downloadStates[modelSpec.id]
                        val isDownloaded = progress?.status == ModelDownloadStatus.DOWNLOADED
                        val isDownloading = progress?.status == ModelDownloadStatus.DOWNLOADING
                        val isActive = activeLocalModelId == modelSpec.id && isDownloaded

                        Surface(
                            color = SurfaceDarkVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, LumiCyan) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = modelSpec.name,
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = LumiViolet.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = modelSpec.quantization,
                                                    color = LumiViolet,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${modelSpec.parameterCount} • ${modelSpec.sizeDisplay} • RAM: ${modelSpec.memoryRequiredRam}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    if (isActive) {
                                        Surface(
                                            color = LumiCyan.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE",
                                                color = LumiCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = modelSpec.description,
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                // Download Progress if active
                                if (isDownloading && progress != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progress.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = LumiCyan,
                                        trackColor = ObsidianDark
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${(progress.progress * 100).toInt()}% (${progress.bytesDownloaded / (1024 * 1024)} MB / ${modelSpec.sizeDisplay})",
                                            color = LumiCyan,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "${progress.speedMegaBytesPerSec} MB/s • ETA ${progress.etaSeconds}s",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isDownloading) {
                                        OutlinedButton(
                                            onClick = { viewModel.cancelModelDownload(modelSpec.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = androidx.compose.ui.graphics.Color(0xFFFF5252)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Cancel", fontSize = 11.sp)
                                        }
                                    } else if (isDownloaded) {
                                        if (!isActive) {
                                            Button(
                                                onClick = { viewModel.setActiveLocalModel(modelSpec.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Set Active", color = ObsidianDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        IconButton(
                                            onClick = { viewModel.deleteLocalModel(modelSpec.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Model Weights", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                        }
                                    } else {
                                        Button(
                                            onClick = { viewModel.downloadLocalModel(modelSpec.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Download (${modelSpec.sizeDisplay})", color = ObsidianDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Temperature Slider & Creativity Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creativity & Temperature",
                            color = LumiGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.2f", temperature),
                            color = LumiGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = temperature,
                        onValueChange = {
                            temperature = it
                            onUpdateProfile(userProfile.copy(temperature = it))
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = LumiGold,
                            activeTrackColor = LumiGold,
                            inactiveTrackColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.testTag("slider_temperature")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.0 (Precise & Deterministic)", color = TextTertiary, fontSize = 10.sp)
                        Text("1.0 (Creative & Playful)", color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Custom System Instructions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Custom AI System Instructions",
                        color = LumiGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Specify persistent system rules (e.g. format output as bullet points, prioritize concise answers).",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = customInstructions,
                        onValueChange = { customInstructions = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_system_instructions"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGold,
                            unfocusedBorderColor = SurfaceDarkVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 5,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            onUpdateProfile(userProfile.copy(customAiInstructions = customInstructions))
                            Toast.makeText(context, "System instructions saved", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Instructions", color = ObsidianDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature Toggles (Tool Calling, Proactive Briefing, Speech)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Autonomy & Capabilities",
                        color = LumiGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ToggleSettingRow(
                        title = "Autonomous Tool Dispatch",
                        subtitle = "Allow Lumi to schedule calendar events and create tasks automatically",
                        isChecked = userProfile.enableToolCalling,
                        accentColor = LumiGreen,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableToolCalling = it)) }
                    )

                    ToggleSettingRow(
                        title = "Proactive Daily Briefings",
                        subtitle = "Auto-synthesize morning & evening productivity briefings",
                        isChecked = userProfile.enableProactiveBriefings,
                        accentColor = LumiGold,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableProactiveBriefings = it)) }
                    )

                    ToggleSettingRow(
                        title = "Voice Speech Synthesis (TTS)",
                        subtitle = "Speak responses automatically during voice dialogue",
                        isChecked = userProfile.enableSpeechOutput,
                        accentColor = LumiCyan,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableSpeechOutput = it)) }
                    )

                    ToggleSettingRow(
                        title = "On-Device Neural Fallback",
                        subtitle = "Route private notes to local neural engine when offline",
                        isChecked = userProfile.enableLocalAiFallback,
                        accentColor = LumiViolet,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableLocalAiFallback = it)) }
                    )
                }
            }
        }

        // On-Device Benchmark & Performance Test
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "On-Device Neural Benchmark",
                                color = LumiPink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Measure real CPU/GPU inference latency and token throughput.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.runGemmaBenchmark() },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_run_benchmark")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Test", color = ObsidianDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!benchmarkStatus.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = benchmarkStatus,
                                color = LumiMint,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Privacy, Security & Data Vault Section
// -------------------------------------------------------------
@Composable
private fun PrivacyAndVaultSection(
    userProfile: UserProfileData,
    taskCount: Int,
    eventCount: Int,
    memoryCount: Int,
    messageCount: Int,
    onToggleBiometric: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Security & Biometric Lock
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Security & Biometric Access",
                        color = LumiMint,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = LumiMint, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Biometric Lock for Memory Vault", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Require Fingerprint/Face to view private wellness reflections", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        Switch(
                            checked = userProfile.enableBiometricLock,
                            onCheckedChange = onToggleBiometric,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ObsidianDark,
                                checkedTrackColor = LumiMint,
                                uncheckedThumbColor = TextTertiary,
                                uncheckedTrackColor = SurfaceDarkVariant
                            )
                        )
                    }
                }
            }
        }

        // Local SQLite Room Database Telemetry
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "On-Device Storage Statistics",
                        color = LumiCyan,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All data is securely stored locally in Room SQLite with zero external tracking.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DbStatPill(label = "Tasks", count = taskCount, color = LumiYellow, modifier = Modifier.weight(1f))
                        DbStatPill(label = "Events", count = eventCount, color = LumiCyan, modifier = Modifier.weight(1f))
                        DbStatPill(label = "Memories", count = memoryCount, color = LumiPink, modifier = Modifier.weight(1f))
                        DbStatPill(label = "Messages", count = messageCount, color = LumiViolet, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Export Data & Backup
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Portability & Backup",
                        color = LumiGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Exporting local vault data as JSON...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LumiGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Data Vault (JSON)", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onResetClicked,
                        colors = ButtonDefaults.buttonColors(containerColor = LumiPink.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = LumiPink, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Chat Logs & Analytics", color = LumiPink, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Helper UI Components
// -------------------------------------------------------------

@Composable
private fun ProfileFieldRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "$label:", color = TextSecondary, fontSize = 13.sp, modifier = Modifier.width(130.dp))
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun MetricBadge(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceDarkVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, color = TextSecondary, fontSize = 10.sp)
            Text(text = value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RoutineBadge(label: String, time: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceDarkVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = label, color = TextSecondary, fontSize = 10.sp)
                Text(text = time, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ConnectorCard(
    title: String,
    subtitle: String,
    accountText: String,
    isConnected: Boolean,
    accentColor: Color,
    icon: ImageVector,
    onToggle: (Boolean) -> Unit,
    onConfigure: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = accentColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
                Text(
                    text = accountText,
                    color = if (isConnected) accentColor else TextTertiary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (onConfigure != null && isConnected) {
                IconButton(onClick = onConfigure, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Tune, contentDescription = "Configure", tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }

            Switch(
                checked = isConnected,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ObsidianDark,
                    checkedTrackColor = accentColor,
                    uncheckedThumbColor = TextTertiary,
                    uncheckedTrackColor = SurfaceDarkVariant
                )
            )
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = TextSecondary, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ObsidianDark,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = TextTertiary,
                uncheckedTrackColor = SurfaceDarkVariant
            )
        )
    }
}

@Composable
private fun DbStatPill(label: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Surface(
        color = SurfaceDarkVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$count", color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

// -------------------------------------------------------------
// Dialogs
// -------------------------------------------------------------

@Composable
private fun EditProfileDialog(
    currentProfile: UserProfileData,
    onDismiss: () -> Unit,
    onSave: (UserProfileData) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.userName) }
    var email by remember { mutableStateOf(currentProfile.userEmail) }
    var role by remember { mutableStateOf(currentProfile.roleOrTitle) }
    var goal by remember { mutableStateOf(currentProfile.primaryFocusGoal) }
    var focusHours by remember { mutableFloatStateOf(currentProfile.dailyFocusTargetHours) }
    var hydrationCups by remember { mutableIntStateOf(currentProfile.targetHydrationCups) }
    var steps by remember { mutableIntStateOf(currentProfile.targetDailySteps) }
    var wakeTime by remember { mutableStateOf(currentProfile.wakeUpTime) }
    var sleepTime by remember { mutableStateOf(currentProfile.sleepTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User Profile", color = LumiMint, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Name") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiMint),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiMint),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("Role / Occupation") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiMint),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = goal,
                        onValueChange = { goal = it },
                        label = { Text("Primary Goal / Mission") },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiMint),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = wakeTime,
                            onValueChange = { wakeTime = it },
                            label = { Text("Wake Time") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiYellow),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sleepTime,
                            onValueChange = { sleepTime = it },
                            label = { Text("Sleep Time") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = LumiViolet),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        currentProfile.copy(
                            userName = name.ifBlank { "User" },
                            userEmail = email.ifBlank { "user@example.com" },
                            roleOrTitle = role.ifBlank { "Productivity Seeker" },
                            primaryFocusGoal = goal.ifBlank { "Daily Growth" },
                            dailyFocusTargetHours = focusHours,
                            targetHydrationCups = hydrationCups,
                            targetDailySteps = steps,
                            wakeUpTime = wakeTime,
                            sleepTime = sleepTime
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = LumiMint)
            ) {
                Text("Save Changes", color = ObsidianDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}

@Composable
private fun AddFactDialog(
    onDismiss: () -> Unit,
    onAddFact: (category: String, factText: String, isPinned: Boolean) -> Unit
) {
    var category by remember { mutableStateOf("Work & Code") }
    var factText by remember { mutableStateOf("") }
    var isPinned by remember { mutableStateOf(false) }

    val categories = listOf("Work & Code", "Preferences", "Health & Routines", "Routines", "Personal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Teach Lumi a New Fact", color = LumiCyan, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Lumi will remember this context across all conversations and tool actions.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )

                // Category selector
                Text("Category", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LumiCyan.copy(alpha = 0.2f),
                                selectedLabelColor = LumiCyan
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = factText,
                    onValueChange = { factText = it },
                    label = { Text("What should Lumi know about you?") },
                    placeholder = { Text("e.g. I prefer vegetarian meals and intermittent fasting") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiCyan,
                        unfocusedBorderColor = SurfaceDarkVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPinned = !isPinned }
                ) {
                    Icon(
                        imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = if (isPinned) LumiGold else TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pin as high-priority context", color = TextPrimary, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (factText.isNotBlank()) {
                        onAddFact(category, factText, isPinned)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
                enabled = factText.isNotBlank()
            ) {
                Text("Save Memory", color = ObsidianDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = SurfaceDark
    )
}
