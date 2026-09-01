package com.example.presentation.screens
import androidx.compose.ui.res.stringResource
import com.example.R
import coil.compose.AsyncImage
import com.example.domain.model.AuthUser



import com.example.presentation.screens.account.*
import com.example.presentation.components.*
import com.example.data.remote.ModelDownloadProgress
import com.example.data.remote.LocalLlmModelSpec
import com.example.domain.model.Task
import com.example.domain.model.CalendarEvent
import com.example.domain.model.ChatMessage
import com.example.domain.model.UserFact

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.domain.connectors.ConnectorRepository

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
import com.example.core.theme.TextTertiary
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing


data class AiModelInfo(
    val id: String,
    val label: String,
    val inputTypes: String,
    val outputTypes: String
)

@Composable
fun UserAccountScreen(
    userProfile: com.example.domain.account.UserProfileData,
    authUser: AuthUser? = null,
    onSignInWithGoogle: () -> Unit = {},
    onSignOut: () -> Unit = {},
    userFacts: List<com.example.domain.account.UserFactItem>,
    petStatus: com.example.domain.model.PetStatus,
    benchmarkStatus: String,
    tasks: List<Task>,
    events: List<CalendarEvent>,
    messages: List<ChatMessage>,
    aiRoutingMode: com.example.data.remote.AiRoutingMode,
    onSetAiRoutingMode: (com.example.data.remote.AiRoutingMode) -> Unit,
    localModelCatalog: List<LocalLlmModelSpec>,
    modelDownloadStates: Map<String, com.example.data.remote.ModelDownloadProgress>,
    activeLocalModelId: String?,
    selectedAccelerator: com.example.data.remote.HardwareAccelerator,
    onUpdateProfile: (com.example.domain.account.UserProfileData) -> Unit,
    onAddUserFact: (String, String, Boolean) -> Unit,
    onRemoveUserFact: (String) -> Unit,
    onTogglePinFact: (String) -> Unit,
    onClearAiAnalytics: () -> Unit,
    onDownloadLocalModel: (String) -> Unit,
    onPauseModelDownload: (String) -> Unit,
    onCancelModelDownload: (String) -> Unit,
    onDeleteLocalModel: (String) -> Unit,
    onSetActiveLocalModel: (String) -> Unit,
    onSetHardwareAccelerator: (com.example.data.remote.HardwareAccelerator) -> Unit,
    onRunGemmaBenchmark: () -> Unit,
    isOverlayEnabled: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    onNavigateToChat: ((String?) -> Unit)? = null,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
                        .padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.desc_back),
                                    tint = TextPrimary
                                )
                            }
                            // Gradient Avatar Ring
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(LumiMint, androidx.compose.material3.MaterialTheme.colorScheme.primary, androidx.compose.material3.MaterialTheme.colorScheme.primary)
                                        )
                                    )
                                    .padding(2.5.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                if (authUser?.photoUrl != null) {
                                    AsyncImage(
                                        model = authUser.photoUrl,
                                        contentDescription = stringResource(id = R.string.desc_user_avatar),
                                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = stringResource(id = R.string.desc_user_avatar),
                                        tint = LumiMint,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = authUser?.displayName ?: userProfile.userName,
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = authUser?.email ?: userProfile.userEmail,
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
                                contentDescription = stringResource(id = R.string.desc_edit_profile),
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
                                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LumiMint,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                Text(
                                    text = stringResource(R.string.text_lumi_synchronized),
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
                            1 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                            2 -> LumiGold
                            3 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
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
                        1 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                        2 -> LumiGold
                        3 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
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
                                modifier = Modifier.size(MaterialTheme.spacing.medium)
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
                        authUser = authUser,
                        onSignInWithGoogle = onSignInWithGoogle,
                        onSignOut = onSignOut,
                        onUpdateProfile = { updated -> onUpdateProfile(updated) },
                        onEditClicked = { showEditProfileDialog = true }
                    )
                    1 -> MemoryFeederSection(
                        facts = userFacts,
                        onAddFactClicked = { showAddFactDialog = true },
                        onDeleteFact = { id -> onRemoveUserFact(id) },
                        onTogglePin = { id -> onTogglePinFact(id) }
                    )
                    2 -> ConnectorsControlSection(
                        
                    )
                    3 -> LlmSettingsSection(
                        userProfile = userProfile,
                        benchmarkStatus = benchmarkStatus,
                        aiRoutingMode = aiRoutingMode,
                        onSetAiRoutingMode = onSetAiRoutingMode,
                        localModelCatalog = localModelCatalog,
                        modelDownloadStates = modelDownloadStates,
                        activeLocalModelId = activeLocalModelId,
                        selectedAccelerator = selectedAccelerator,
                        onUpdateProfile = onUpdateProfile,
                        onDownloadLocalModel = onDownloadLocalModel,
                        onCancelModelDownload = onCancelModelDownload,
                        onPauseModelDownload = onPauseModelDownload,
                        onDeleteLocalModel = onDeleteLocalModel,
                        onSetActiveLocalModel = onSetActiveLocalModel,
                        onSetHardwareAccelerator = onSetHardwareAccelerator,
                        onRunGemmaBenchmark = onRunGemmaBenchmark
                    )
                    4 -> PrivacyAndVaultSection(
                        userProfile = userProfile,
                        taskCount = tasks.size,
                        eventCount = events.size,
                        memoryCount = userFacts.size,
                        messageCount = messages.size,
                        onToggleBiometric = { isEnabled ->
                            onUpdateProfile(userProfile.copy(enableBiometricLock = isEnabled))
                        },
                        isOverlayEnabled = isOverlayEnabled,
                        onToggleOverlay = onToggleOverlay,
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
                    onUpdateProfile(updated)
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
                    onAddUserFact(category, factText, isPinned)
                    showAddFactDialog = false
                    Toast.makeText(context, "Memory fact added to Lumi's context", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Reset Data Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text(stringResource(id = R.string.text_reset_memory_history), color = LumiPink) },
                text = {
                    Text(
                        "This will clear chat conversation logs and reset AI analytics. Your user profile and custom memory facts will remain intact.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onClearAiAnalytics()
                            showResetDialog = false
                            Toast.makeText(context, "Chat and analytics logs cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiPink)
                    ) {
                        Text(stringResource(id = R.string.text_clear_logs), color = ObsidianDark, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text(stringResource(id = R.string.text_cancel), color = TextSecondary)
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


// -------------------------------------------------------------
// 2. Memory Feeder Section (User Context & Personal Facts)
// -------------------------------------------------------------


// -------------------------------------------------------------
// 3. Connectors & Integrations Section
// -------------------------------------------------------------


// -------------------------------------------------------------
// 4. LLM Brain & AI Engine Settings Section (with Local LLM Hub)
// -------------------------------------------------------------



// -------------------------------------------------------------
// 5. Privacy, Security & Data Vault Section
// -------------------------------------------------------------


// -------------------------------------------------------------
// Helper UI Components
// -------------------------------------------------------------













// -------------------------------------------------------------
// Dialogs
// -------------------------------------------------------------





