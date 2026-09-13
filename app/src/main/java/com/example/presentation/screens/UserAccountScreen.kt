package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.rememberLumiHaptics
import com.example.data.remote.AiRoutingMode
import com.example.data.remote.HardwareAccelerator
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadProgress
import com.example.domain.account.UserFactItem
import com.example.domain.account.UserProfileData
import com.example.domain.model.AuthUser
import com.example.domain.model.CalendarEvent
import com.example.domain.model.ChatMessage
import com.example.domain.model.PetStatus
import com.example.domain.model.Task
import com.example.presentation.screens.account.*

@Composable
fun UserAccountScreen(
    userProfile: com.example.domain.account.UserProfileData,
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics(isEnabled = userProfile.enableHapticFeedback),
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
    val sectionTabs = listOf("Profile & Persona", "Memory Feeder", "Tools & Connectors", "LLM Settings", "Privacy & Vault")

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAddFactDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .navigationBarsPadding()
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
                        .statusBarsPadding()
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
                            onClick = { 
                                haptics.performClick()
                                showEditProfileDialog = true 
                            },
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
                    2 -> ToolsAndConnectorsSection()
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