package com.example
import com.example.data.local.mapper.*
import com.example.data.local.entity.*
import androidx.compose.ui.graphics.Color

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.framework.AppShortcutsManager
import com.example.framework.PetOverlayService
import com.example.presentation.components.BreathingExerciseModal
import com.example.presentation.components.CameraVisionDialog
import com.example.presentation.components.OverlayPermissionDialog
import com.example.core.navigation.NavDestination
import com.example.presentation.screens.ChatScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.screens.LifeHubScreen
import com.example.presentation.screens.UserAccountScreen
import com.example.presentation.screens.WellnessScreen
import com.example.core.theme.MyApplicationTheme
import com.example.core.theme.ObsidianDark
import com.example.presentation.viewmodel.*

class MainActivity : ComponentActivity() {

    private val viewModel: LumiViewModel by viewModel()
    private val aiSettingsViewModel: AiSettingsViewModel by viewModel()
    private val chatViewModel: ChatViewModel by viewModel()
    private val wellnessViewModel: WellnessViewModel by viewModel()
    private val lifeHubViewModel: LifeHubViewModel by viewModel()
    private val petViewModel: PetViewModel by viewModel()
    private val authViewModel: AuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Dynamic App Shortcuts
        AppShortcutsManager.initDynamicShortcuts(this)

        // Handle incoming intent (Shares, Shortcuts, Alarms, Widgets)
        handleIntent(intent)

        setContent {
            val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()
            val petPrimary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.primaryHex)
            val petSecondary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.endHex)
            MyApplicationTheme(petColorPrimary = petPrimary, petColorSecondary = petSecondary) {
                LumiApp(
                    viewModel = viewModel,
                    aiSettingsViewModel = aiSettingsViewModel,
                    chatViewModel = chatViewModel,
                    wellnessViewModel = wellnessViewModel,
                    lifeHubViewModel = lifeHubViewModel,
                    petViewModel = petViewModel,
                    authViewModel = authViewModel
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        val type = intent.type

        // 1. Handle Android System Share Sheet (ACTION_SEND)
        if (Intent.ACTION_SEND == action && type != null) {
            if ("text/plain" == type) {
                intent.getStringExtra(Intent.EXTRA_TEXT)?.let { sharedText ->
                    viewModel.handleIncomingSharedText(sharedText)
                }
            } else if (type.startsWith("image/")) {
                val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_STREAM)
                }
                imageUri?.let { uri ->
                    val bitmap = loadBitmapFromUri(uri)
                    if (bitmap != null) {
                        viewModel.handleIncomingSharedImage(bitmap)
                    }
                }
            }
        }

        // 2. Handle Launcher App Shortcut Actions
        intent.getStringExtra("SHORTCUT_ACTION")?.let { shortcutAction ->
            viewModel.handleShortcutAction(shortcutAction)
        }

        // 3. Handle explicit tab navigation
        if (intent.hasExtra("NAVIGATE_TAB")) {
            val tab = intent.getIntExtra("NAVIGATE_TAB", 0)
            viewModel.setSelectedTab(tab)
        }

        // 4. Handle Daily Briefing Notification Deep Link
        intent.getStringExtra("OPEN_BRIEFING")?.let { briefingStr ->
            viewModel.setSelectedTab(0)
            val type: com.example.domain.briefing.BriefingType? = when (briefingStr.uppercase()) {
                "MORNING" -> com.example.domain.briefing.BriefingType.MORNING
                "EVENING" -> com.example.domain.briefing.BriefingType.EVENING
                else -> null
            }
            lifeHubViewModel.refreshDailyBriefing(type, petViewModel.petStatus.value, petViewModel.petEvolution.value, wellnessViewModel.allWellnessLogs.value)
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(contentResolver, uri)
            }
        } catch (e: Exception) {
            null
        }
    }

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun LumiApp(
    viewModel: LumiViewModel,
    aiSettingsViewModel: AiSettingsViewModel,
    chatViewModel: ChatViewModel,
    wellnessViewModel: WellnessViewModel,
    lifeHubViewModel: LifeHubViewModel,
    petViewModel: PetViewModel,
    authViewModel: AuthViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()
    val batteryStatus by viewModel.batteryStatus.collectAsStateWithLifecycle()
    val networkStatus by viewModel.networkStatus.collectAsStateWithLifecycle()
    val locationContext by viewModel.locationState.collectAsStateWithLifecycle()
    val userFacts by viewModel.userFacts.collectAsStateWithLifecycle()
    val benchmarkStatus by viewModel.benchmarkStatus.collectAsStateWithLifecycle()
    val chatMessagesList by viewModel.chatMessages.collectAsStateWithLifecycle()
    val aiRoutingMode by viewModel.aiRoutingMode.collectAsStateWithLifecycle()

    val calendarEvents by lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle()
    val tasks by lifeHubViewModel.allTasks.collectAsStateWithLifecycle()
    val dailyBriefing by lifeHubViewModel.dailyBriefing.collectAsStateWithLifecycle()
    val goalPlans by lifeHubViewModel.allGoalPlans.collectAsStateWithLifecycle()
    val soundState by lifeHubViewModel.soundscapeState.collectAsStateWithLifecycle()

    val wellnessLogs by wellnessViewModel.allWellnessLogs.collectAsStateWithLifecycle()
    val memories by wellnessViewModel.allMemories.collectAsStateWithLifecycle()

    val isListening by chatViewModel.voiceEngine.isListening.collectAsStateWithLifecycle()
    val isSpeaking by chatViewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle()

    val modelDownloadStates by aiSettingsViewModel.modelDownloadStates.collectAsStateWithLifecycle()
    val activeLocalModelId by aiSettingsViewModel.activeLocalModelId.collectAsStateWithLifecycle()
    val selectedAccelerator by aiSettingsViewModel.selectedAccelerator.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val haptics = com.example.core.utils.rememberLumiHaptics(isEnabled = userProfile.enableHapticFeedback)

    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val handleStartVoiceListening = {
        if (audioPermissionState.status.isGranted) {
            chatViewModel.startVoiceListening()
        } else {
            audioPermissionState.launchPermissionRequest()
        }
    }

    if (authUiState.user == null && !authUiState.isGuestMode) {
        com.example.presentation.screens.auth.LoginScreen(
            authViewModel = authViewModel,
            petStatus = petStatus,
            onLoginSuccess = {
                // User signed in successfully
            },
            onContinueAsGuest = {
                authViewModel.continueAsGuest()
            }
        )
    } else if (!userProfile.hasCompletedOnboarding) {
        com.example.presentation.screens.OnboardingScreen(
            viewModel = viewModel,
            onComplete = { /* State handles recomposition automatically */ }
        )
    } else {
        BackHandler(
            enabled = uiState.selectedTab != com.example.core.navigation.NavDestination.PetCompanion.tabIndex || 
                      uiState.showWardrobeScreen || 
                      uiState.showCameraDialog || 
                      uiState.showBreathingDialog ||
                      uiState.showOverlayPermissionDialog ||
                      uiState.lifeHubSubTab != 0
        ) {
            haptics.performTick()
            when {
                uiState.showWardrobeScreen -> viewModel.setShowWardrobeScreen(false)
                uiState.showCameraDialog -> viewModel.setShowCamera(false)
                uiState.showBreathingDialog -> viewModel.setShowBreathing(false)
                uiState.showOverlayPermissionDialog -> viewModel.setShowOverlayPermission(false)
                uiState.selectedTab == com.example.core.navigation.NavDestination.LifeHub.tabIndex && uiState.lifeHubSubTab != 0 -> {
                    viewModel.setLifeHubSubTab(0)
                }
                uiState.selectedTab != com.example.core.navigation.NavDestination.PetCompanion.tabIndex -> {
                    viewModel.setSelectedTab(com.example.core.navigation.NavDestination.PetCompanion.tabIndex)
                }
            }
        }
    val handleLifeHubAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit = { action ->
 
        when (action) {
            is com.example.presentation.viewmodel.LumiUiAction.NavigateToChat -> {
                viewModel.setSelectedTab(com.example.core.navigation.NavDestination.Assistant.tabIndex)
                action.prompt?.let { chatViewModel.sendMessage(it) }
            }
            is com.example.presentation.viewmodel.LumiUiAction.SetLifeHubSubTab -> viewModel.setLifeHubSubTab(action.tabIndex)
            is com.example.presentation.viewmodel.LumiUiAction.AddCalendarEvent -> lifeHubViewModel.addCalendarEvent(action.event)
            is com.example.presentation.viewmodel.LumiUiAction.DeleteCalendarEvent -> lifeHubViewModel.deleteCalendarEvent(action.id)
            is com.example.presentation.viewmodel.LumiUiAction.SpeakBriefing -> {} // Handled via voice engine
            is com.example.presentation.viewmodel.LumiUiAction.AddTask -> lifeHubViewModel.addTask(action.title, action.priority, action.category, action.estimatedMinutes, action.notes)
            is com.example.presentation.viewmodel.LumiUiAction.ToggleTask -> lifeHubViewModel.toggleTask(action.id, action.isCompleted)
            is com.example.presentation.viewmodel.LumiUiAction.DeleteTask -> lifeHubViewModel.deleteTask(action.task)
            is com.example.presentation.viewmodel.LumiUiAction.DecomposeGoal -> lifeHubViewModel.decomposeGoal(action.title, action.description, action.category, action.deadline)
            is com.example.presentation.viewmodel.LumiUiAction.DeleteGoal -> lifeHubViewModel.deleteGoal(action.id)
            is com.example.presentation.viewmodel.LumiUiAction.ToggleMilestone -> lifeHubViewModel.toggleMilestone(action.milestoneId, action.goalId, action.isCompleted)
            is com.example.presentation.viewmodel.LumiUiAction.ExecuteMilestone -> lifeHubViewModel.executeMilestone(action.milestoneId, action.goalId)
            is com.example.presentation.viewmodel.LumiUiAction.StartSoundscape -> lifeHubViewModel.startSoundscape(action.type)
            is com.example.presentation.viewmodel.LumiUiAction.StopSoundscape -> lifeHubViewModel.stopSoundscape()
            is com.example.presentation.viewmodel.LumiUiAction.SetSoundscapeVolume -> lifeHubViewModel.setSoundscapeVolume(action.volume)
            is com.example.presentation.viewmodel.LumiUiAction.StartFocusTimer -> lifeHubViewModel.startFocusTimerWithSoundscape(action.minutes)
            is com.example.presentation.viewmodel.LumiUiAction.StopFocusTimer -> lifeHubViewModel.stopFocusTimerWithSoundscape()
        }
    }

        val petPrimary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.primaryHex)

        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) {
            innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ObsidianDark)
            ) {

                Crossfade(
                targetState = uiState.selectedTab,
                label = "ScreenTransition"
            ) {
 tab ->
                when (tab) {
                    NavDestination.Assistant.tabIndex -> ChatScreen(
                        uiState = uiState,
                        petStatus = petStatus,
                        chatMessages = chatViewModel.pagedChatMessages.collectAsLazyPagingItems(),
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        onSendMessage = { text -> chatViewModel.sendMessage(text) },
                        onSetInputText = { text -> viewModel.setInputText(text) },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onStartVoiceListening = { handleStartVoiceListening() },
                        onStopVoiceListening = { chatViewModel.stopVoiceListening() },
                        onToggleVoiceOutput = { viewModel.toggleVoiceOutput() },
                        onNavigateBack = { haptics.performTick(); viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
                    )
                    NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        tasks = tasks,
                        events = calendarEvents,
                        wellnessLogs = wellnessLogs,
                        memories = memories,
                        dailyBriefing = dailyBriefing,
                        goalPlans = goalPlans,
                        getMilestonesForGoal = { id -> lifeHubViewModel.getMilestonesForGoal(id) },
                        soundState = soundState,
                        onAction = handleLifeHubAction,
                        onNavigateBack = { haptics.performTick(); viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
                    )
                    NavDestination.Wellness.tabIndex -> WellnessScreen(
                        viewModel = wellnessViewModel,
                        appViewModel = viewModel,
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateBack = { haptics.performTick(); viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
                    )
                    NavDestination.Account.tabIndex -> UserAccountScreen(
                        userProfile = userProfile,
                        authUser = authUiState.user,
                        onSignInWithGoogle = { authViewModel.signInWithGoogle(context) },
                        onSignOut = { authViewModel.signOut() },
                        userFacts = userFacts,
                        petStatus = petStatus,
                        benchmarkStatus = benchmarkStatus ?: "",
                        tasks = tasks.map { it.toDomain() },
                        events = calendarEvents.map { it.toDomain() },
                        messages = chatMessagesList.map { it.toDomain() },
                        aiRoutingMode = aiRoutingMode,
                        onSetAiRoutingMode = { mode -> viewModel.setAiRoutingMode(mode) },
                        localModelCatalog = aiSettingsViewModel.localModelCatalog,
                        modelDownloadStates = modelDownloadStates,
                        activeLocalModelId = activeLocalModelId,
                        selectedAccelerator = selectedAccelerator,
                        onUpdateProfile = { updated -> aiSettingsViewModel.updateUserProfile(updated) },
                        onAddUserFact = { cat, txt, isPinned -> viewModel.addUserFact(cat, txt, isPinned) },
                        onRemoveUserFact = { id -> viewModel.removeUserFact(id) },
                        onTogglePinFact = { id -> viewModel.togglePinFact(id) },
                        onClearAiAnalytics = { viewModel.clearAiAnalytics() },
                        onDownloadLocalModel = { id -> aiSettingsViewModel.downloadLocalModel(id) },
                        onCancelModelDownload = { id -> aiSettingsViewModel.cancelModelDownload(id) },
                        onPauseModelDownload = { id -> aiSettingsViewModel.pauseModelDownload(id) },
                        onDeleteLocalModel = { id -> aiSettingsViewModel.deleteLocalModel(id) },
                        onSetActiveLocalModel = { id -> aiSettingsViewModel.setActiveLocalModel(id) },
                        onSetHardwareAccelerator = { acc -> aiSettingsViewModel.setHardwareAccelerator(acc) },
                        onRunGemmaBenchmark = { viewModel.runGemmaBenchmark() },
                        isOverlayEnabled = uiState.isOverlayEnabled,
                        onToggleOverlay = { if (it) viewModel.setShowOverlayPermission(true) else viewModel.setOverlayEnabled(false) },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        },
                        onNavigateBack = { haptics.performTick(); viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) }
                    )
                    else -> HomeScreen(
                        petStatus = petStatus,
                        uiState = uiState,
                        batteryStatus = batteryStatus,
                        networkStatus = networkStatus,
                        events = calendarEvents,
                        tasks = tasks,
                        isListening = isListening,
                        isSpeaking = isSpeaking,
                        authUser = authUiState.user,
                        onPetPetted = { petViewModel.onPetPetted() },
                        onPetTouched = { petViewModel.onPetTouched() },
                        onTogglePetSleep = { petViewModel.togglePetSleep() },
                        onStartVoiceListening = { handleStartVoiceListening() },
                        onStopVoiceListening = { chatViewModel.stopVoiceListening() },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onShowWardrobe = { viewModel.setShowWardrobeScreen(true) },
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) },
                        onNavigateToAccount = { viewModel.setSelectedTab(NavDestination.Account.tabIndex) },
                        onNavigateToWellness = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) },
                        locationContext = locationContext,
                        userProfile = userProfile,
                        onFeedPet = { petViewModel.feedPet() },
                        onDancePet = { petViewModel.dancePet() },
                        onPokePet = { petViewModel.pokePet() },
                        onToggleTask = { id, isCompleted -> lifeHubViewModel.toggleTask(id, isCompleted) },
                        onQuickAgentPrompt = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            chatViewModel.sendMessage(prompt)
                        }
                    )
                }
            }
            if (uiState.showWardrobeScreen) {
                com.example.presentation.screens.WardrobeScreen(petViewModel = petViewModel, wellnessViewModel = wellnessViewModel, onClose = { viewModel.setShowWardrobeScreen(false) })
            }
            // Camera / Vision Dialog Modal
            if (uiState.showCameraDialog) {
                CameraVisionDialog(
                    onDismiss = { viewModel.setShowCamera(false) },
                    onImageCaptured = { bitmap, prompt ->
                        viewModel.setShowCamera(false)
                        viewModel.sendMessage(prompt, bitmap)
                        viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                    }
                )
            }

            // Breathing Exercise Dialog Modal
            if (uiState.showBreathingDialog) {
                BreathingExerciseModal(
                    onDismiss = { viewModel.setShowBreathing(false) },
                    onComplete = {
                        viewModel.setShowBreathing(false)
                        viewModel.logWellness(
                            moodScore = 9,
                            moodLabel = "Centered & Relaxed",
                            energyLevel = 8,
                            hydrationCups = 0,
                            gratitude = "Completed 4-7-8 Breathing Coherence with Lumi"
                        )
                    }
                )
            }

            // Overlay Permission Dialog Modal
            if (uiState.showOverlayPermissionDialog) {
                OverlayPermissionDialog(
                    onDismiss = { viewModel.setShowOverlayPermission(false) },
                    onGranted = {
                        viewModel.setShowOverlayPermission(false)
                        viewModel.setOverlayEnabled(true)
                        val serviceIntent = Intent(context, PetOverlayService::class.java)
                        ContextCompat.startForegroundService(context, serviceIntent)
                    }
                )
            }
        }
    }
}
}


