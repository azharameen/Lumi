package com.example.presentation

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.core.navigation.NavDestination
import com.example.core.theme.ObsidianDark
import com.example.core.utils.rememberLumiHaptics
import com.example.data.local.mapper.toDomain
import com.example.framework.PetOverlayService
import com.example.presentation.components.CameraVisionDialog
import com.example.presentation.components.OverlayPermissionDialog
import com.example.presentation.home.HomeScreen
import com.example.presentation.screens.ChatScreen
import com.example.presentation.screens.LifeHubScreen
import com.example.presentation.screens.OnboardingScreen
import com.example.presentation.screens.UserAccountScreen
import com.example.presentation.screens.WardrobeScreen
import com.example.presentation.screens.WellnessScreen
import com.example.presentation.screens.auth.LoginScreen
import com.example.presentation.viewmodel.AiSettingsViewModel
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.ChatViewModel
import com.example.presentation.viewmodel.LifeHubViewModel
import com.example.presentation.viewmodel.LumiUiAction
import com.example.presentation.viewmodel.LumiViewModel
import com.example.presentation.viewmodel.PetViewModel
import com.example.presentation.viewmodel.WellnessViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * Root Application Composable orchestrating state routing, back-navigation,
 * dialogs, and primary tab transitions.
 */
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
    val pendingHitlActions by chatViewModel.pendingHitlActions.collectAsStateWithLifecycle()
    val streamingAiMessage by chatViewModel.streamingAiMessage.collectAsStateWithLifecycle()
    val currentlySpeakingMessageId by chatViewModel.currentlySpeakingMessageId.collectAsStateWithLifecycle()

    val modelDownloadStates by aiSettingsViewModel.modelDownloadStates.collectAsStateWithLifecycle()
    val downloadedLocalModels by aiSettingsViewModel.downloadedLocalModels.collectAsStateWithLifecycle()
    val availableCloudModels by aiSettingsViewModel.availableCloudModels.collectAsStateWithLifecycle()
    val selectedChatModelId by chatViewModel.selectedChatModelId.collectAsStateWithLifecycle()
    val activeLocalModelId by aiSettingsViewModel.activeLocalModelId.collectAsStateWithLifecycle()
    val selectedAccelerator by aiSettingsViewModel.selectedAccelerator.collectAsStateWithLifecycle()

    val modelSelectionEngine = remember { org.koin.core.context.GlobalContext.get().get<com.example.domain.ai.ModelSelectionEngine>() }
    val modelDisplayName = remember(selectedChatModelId, downloadedLocalModels, availableCloudModels) {
        modelSelectionEngine.getModelDisplayName(selectedChatModelId)
    }

    val context = LocalContext.current
    val haptics = rememberLumiHaptics(isEnabled = userProfile.enableHapticFeedback)

    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    val handleStartVoiceListening = {
        if (audioPermissionState.status.isGranted) {
            chatViewModel.startVoiceListening()
        } else {
            audioPermissionState.launchPermissionRequest()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val canDraw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.canDrawOverlays(context)
                } else true

                if (!canDraw) {
                    if (uiState.isOverlayEnabled) {
                        viewModel.setOverlayEnabled(false)
                    }
                    val stopIntent = Intent(context, PetOverlayService::class.java).apply {
                        action = PetOverlayService.ACTION_STOP
                    }
                    try {
                        context.startService(stopIntent)
                        context.stopService(Intent(context, PetOverlayService::class.java))
                    } catch (_: Exception) {}
                } else {
                    if (uiState.showOverlayPermissionDialog) {
                        viewModel.setShowOverlayPermission(false)
                        viewModel.setOverlayEnabled(true)
                        val serviceIntent = Intent(context, PetOverlayService::class.java)
                        ContextCompat.startForegroundService(context, serviceIntent)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (authUiState.user == null && !authUiState.isGuestMode) {
        LoginScreen(
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
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = { /* State handles recomposition automatically */ }
        )
    } else {
        BackHandler(
            enabled = uiState.selectedTab != NavDestination.PetCompanion.tabIndex || 
                      uiState.showWardrobeScreen || 
                      uiState.showCameraDialog || 
                      uiState.showOverlayPermissionDialog ||
                      uiState.lifeHubSubTab != 0
        ) {
            haptics.performTick()
            when {
                uiState.showWardrobeScreen -> viewModel.setShowWardrobeScreen(false)
                uiState.showCameraDialog -> viewModel.setShowCamera(false)
                uiState.showOverlayPermissionDialog -> viewModel.setShowOverlayPermission(false)
                uiState.selectedTab == NavDestination.LifeHub.tabIndex && uiState.lifeHubSubTab != 0 -> {
                    viewModel.setLifeHubSubTab(0)
                }
                uiState.selectedTab != NavDestination.PetCompanion.tabIndex -> {
                    viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex)
                }
            }
        }

        val handleLifeHubAction: (LumiUiAction) -> Unit = { action ->
            when (action) {
                is LumiUiAction.NavigateToChat -> {
                    viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                    action.prompt?.let { chatViewModel.sendMessage(it) }
                }
                is LumiUiAction.SetLifeHubSubTab -> viewModel.setLifeHubSubTab(action.tabIndex)
                is LumiUiAction.AddCalendarEvent -> lifeHubViewModel.addCalendarEvent(action.event)
                is LumiUiAction.DeleteCalendarEvent -> lifeHubViewModel.deleteCalendarEvent(action.id)
                is LumiUiAction.SpeakBriefing -> {} // Handled via voice engine
                is LumiUiAction.AddTask -> lifeHubViewModel.addTask(action.title, action.priority, action.category, action.estimatedMinutes, action.notes)
                is LumiUiAction.ToggleTask -> lifeHubViewModel.toggleTask(action.id, action.isCompleted)
                is LumiUiAction.DeleteTask -> lifeHubViewModel.deleteTask(action.task)
                is LumiUiAction.DecomposeGoal -> lifeHubViewModel.decomposeGoal(action.title, action.description, action.category, action.deadline)
                is LumiUiAction.DeleteGoal -> lifeHubViewModel.deleteGoal(action.id)
                is LumiUiAction.ToggleMilestone -> lifeHubViewModel.toggleMilestone(action.milestoneId, action.goalId, action.isCompleted)
                is LumiUiAction.ExecuteMilestone -> lifeHubViewModel.executeMilestone(action.milestoneId, action.goalId)
                is LumiUiAction.StartSoundscape -> lifeHubViewModel.startSoundscape(action.type)
                is LumiUiAction.StopSoundscape -> lifeHubViewModel.stopSoundscape()
                is LumiUiAction.SetSoundscapeVolume -> lifeHubViewModel.setSoundscapeVolume(action.volume)
                is LumiUiAction.StartFocusTimer -> lifeHubViewModel.startFocusTimerWithSoundscape(action.minutes)
                is LumiUiAction.StopFocusTimer -> lifeHubViewModel.stopFocusTimerWithSoundscape()
            }
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ObsidianDark)
            ) {
                Crossfade(
                    targetState = uiState.selectedTab,
                    label = "ScreenTransition"
                ) { tab ->
                    when (tab) {
                        NavDestination.Assistant.tabIndex -> ChatScreen(
                            haptics = haptics,
                            uiState = uiState,
                            petStatus = petStatus,
                            chatMessages = chatViewModel.pagedChatMessages.collectAsLazyPagingItems(),
                            pendingHitlActions = pendingHitlActions,
                            isListening = isListening,
                            isSpeaking = isSpeaking,
                            streamingMessage = streamingAiMessage,
                            currentlySpeakingMessageId = currentlySpeakingMessageId,
                            onSendMessage = { text -> chatViewModel.sendMessage(text) },
                            onSetInputText = { text -> viewModel.setInputText(text) },
                            onShowCamera = { viewModel.setShowCamera(true) },
                            onStartVoiceListening = { handleStartVoiceListening() },
                            onStopVoiceListening = { chatViewModel.stopVoiceListening() },
                            onToggleVoiceOutput = { viewModel.toggleVoiceOutput() },
                            onClearChat = { chatViewModel.clearChatHistory() },
                            onDeleteMessage = { id -> chatViewModel.deleteMessage(id) },
                            onSpeakMessage = { text -> chatViewModel.speakMessage(text) },
                            onToggleSpeakMessage = { id, text -> chatViewModel.toggleSpeakMessage(id, text) },
                            onStopSpeaking = { chatViewModel.stopSpeaking() },
                            onResolveHitlAction = { stateId, approved -> chatViewModel.resolveHitlAction(stateId, approved) },
                            onDismissClipboard = { viewModel.dismissClipboardSnippet() },
                            onProcessClipboard = { snippet -> viewModel.processClipboardWithLumi(snippet) },
                            selectedModelId = selectedChatModelId,
                            modelDisplayName = modelDisplayName,
                            onSelectModel = { modelId -> chatViewModel.setSelectedModel(modelId) },
                            downloadedLocalModels = downloadedLocalModels,
                            availableCloudModels = availableCloudModels,
                            onNavigateToDownloadHub = {
                                viewModel.setSelectedTab(NavDestination.Account.tabIndex)
                            },
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
                            onToggleOverlay = { enabled ->
                                if (enabled) {
                                    val canDraw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        Settings.canDrawOverlays(context)
                                    } else true

                                    if (canDraw) {
                                        viewModel.setOverlayEnabled(true)
                                        val serviceIntent = Intent(context, PetOverlayService::class.java)
                                        ContextCompat.startForegroundService(context, serviceIntent)
                                    } else {
                                        viewModel.setShowOverlayPermission(true)
                                    }
                                } else {
                                    viewModel.setOverlayEnabled(false)
                                    val stopIntent = Intent(context, PetOverlayService::class.java).apply {
                                        action = PetOverlayService.ACTION_STOP
                                    }
                                    try {
                                        context.startService(stopIntent)
                                        context.stopService(Intent(context, PetOverlayService::class.java))
                                    } catch (_: Exception) {}
                                }
                            },
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
                    WardrobeScreen(
                        petViewModel = petViewModel,
                        wellnessViewModel = wellnessViewModel,
                        onClose = { viewModel.setShowWardrobeScreen(false) }
                    )
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

                // Overlay Permission Dialog Modal
                if (uiState.showOverlayPermissionDialog) {
                    OverlayPermissionDialog(
                        onDismiss = { 
                            viewModel.setShowOverlayPermission(false) 
                            viewModel.setOverlayEnabled(false)
                        },
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
