package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import com.example.service.AppShortcutsManager
import com.example.service.PetOverlayService
import com.example.ui.components.BreathingExerciseModal
import com.example.ui.components.CameraVisionDialog
import com.example.ui.components.OverlayPermissionDialog
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LifeHubScreen
import com.example.ui.screens.UserAccountScreen

import com.example.ui.screens.WellnessScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianDark
import com.example.ui.viewmodel.LumiViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.WellnessViewModel
import com.example.ui.viewmodel.LifeHubViewModel
import com.example.ui.viewmodel.PetViewModel
import com.example.ui.viewmodel.AiSettingsViewModel

/**
 * Main Activity hosting Lumi's full-screen application experience.
 * Fully integrated with Android System Share Sheet, App Shortcuts, and Proactive Services.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: LumiViewModel by viewModels()
    private val aiSettingsViewModel: AiSettingsViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val wellnessViewModel: WellnessViewModel by viewModels()
    private val lifeHubViewModel: LifeHubViewModel by viewModels()
    private val petViewModel: PetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Dynamic App Shortcuts
        AppShortcutsManager.initDynamicShortcuts(this)

        // Audio and Location permissions
        val permissionsToRequest = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_RECORD_AUDIO_CODE)
        }

        // Handle incoming intent (Shares, Shortcuts, Alarms, Widgets)
        handleIntent(intent)

        setContent {
            val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()
            val petPrimary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.primaryHex)
            val petSecondary = androidx.compose.ui.graphics.Color(petStatus.bloubSkinColor.endHex)
            MyApplicationTheme(petColorPrimary = petPrimary, petColorSecondary = petSecondary) {
                LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel, chatViewModel = chatViewModel, wellnessViewModel = wellnessViewModel, lifeHubViewModel = lifeHubViewModel, petViewModel = petViewModel)
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

    companion object {
        private const val PERMISSION_RECORD_AUDIO_CODE = 101
    }
}

@Composable
fun LumiApp(viewModel: LumiViewModel, aiSettingsViewModel: AiSettingsViewModel, chatViewModel: ChatViewModel, wellnessViewModel: WellnessViewModel, lifeHubViewModel: LifeHubViewModel, petViewModel: PetViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userProfile by aiSettingsViewModel.userProfile.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (!userProfile.hasCompletedOnboarding) {
        com.example.ui.screens.OnboardingScreen(
            viewModel = viewModel,
            onComplete = { /* State handles recomposition automatically */ }
        )
    } else {
        
    val handleLifeHubAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit = { action ->
        when (action) {
            is com.example.ui.viewmodel.LumiUiAction.NavigateToChat -> {
                viewModel.setSelectedTab(com.example.ui.navigation.NavDestination.Assistant.tabIndex)
                action.prompt?.let { chatViewModel.sendMessage(it) }
            }
            is com.example.ui.viewmodel.LumiUiAction.SetLifeHubSubTab -> viewModel.setLifeHubSubTab(action.tabIndex)
            is com.example.ui.viewmodel.LumiUiAction.AddCalendarEvent -> lifeHubViewModel.addCalendarEvent(action.event)
            is com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent -> lifeHubViewModel.deleteCalendarEvent(action.id)
            is com.example.ui.viewmodel.LumiUiAction.SpeakBriefing -> {} // Removed voice briefing output for now // Remains in LumiViewModel for now if voice output
            is com.example.ui.viewmodel.LumiUiAction.AddTask -> lifeHubViewModel.addTask(action.title, action.priority, action.category, action.estimatedMinutes, action.notes)
            is com.example.ui.viewmodel.LumiUiAction.ToggleTask -> lifeHubViewModel.toggleTask(action.id, action.isCompleted)
            is com.example.ui.viewmodel.LumiUiAction.DeleteTask -> lifeHubViewModel.deleteTask(action.task)
            is com.example.ui.viewmodel.LumiUiAction.DecomposeGoal -> lifeHubViewModel.decomposeGoal(action.title, action.description, action.category, action.deadline)
            is com.example.ui.viewmodel.LumiUiAction.DeleteGoal -> lifeHubViewModel.deleteGoal(action.id)
            is com.example.ui.viewmodel.LumiUiAction.ToggleMilestone -> lifeHubViewModel.toggleMilestone(action.milestoneId, action.goalId, action.isCompleted)
            is com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone -> lifeHubViewModel.executeMilestone(action.milestoneId, action.goalId)
            is com.example.ui.viewmodel.LumiUiAction.StartSoundscape -> lifeHubViewModel.startSoundscape(action.type)
            is com.example.ui.viewmodel.LumiUiAction.StopSoundscape -> lifeHubViewModel.stopSoundscape()
            is com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume -> lifeHubViewModel.setSoundscapeVolume(action.volume)
            is com.example.ui.viewmodel.LumiUiAction.StartFocusTimer -> lifeHubViewModel.startFocusTimerWithSoundscape(action.minutes)
            is com.example.ui.viewmodel.LumiUiAction.StopFocusTimer -> lifeHubViewModel.stopFocusTimerWithSoundscape()
        }
    }

        Scaffold(
            modifier = Modifier.fillMaxSize()
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
                    NavDestination.PetCompanion.tabIndex -> HomeScreen(
                        petStatus = petViewModel.petStatus.collectAsStateWithLifecycle().value,
                        uiState = uiState,
                        batteryStatus = viewModel.batteryStatus.collectAsStateWithLifecycle().value,
                        networkStatus = viewModel.networkStatus.collectAsStateWithLifecycle().value,
                        events = lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle().value,
                        tasks = lifeHubViewModel.allTasks.collectAsStateWithLifecycle().value,
                        isListening = chatViewModel.voiceEngine.isListening.collectAsStateWithLifecycle().value,
                        isSpeaking = chatViewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle().value,
                        onPetPetted = { petViewModel.onPetPetted() },
                        onPetTouched = { petViewModel.onPetTouched() },
                        onTogglePetSleep = { petViewModel.togglePetSleep() },
                        onStartVoiceListening = { chatViewModel.startVoiceListening() },
                        onStopVoiceListening = { chatViewModel.stopVoiceListening() },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onShowWardrobe = { viewModel.setShowWardrobeScreen(true) },
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) },
                        onNavigateToAccount = { viewModel.setSelectedTab(NavDestination.Account.tabIndex) },
                        onNavigateToWellness = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) },
                        locationContext = viewModel.locationState.collectAsStateWithLifecycle().value,
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
                    NavDestination.Assistant.tabIndex -> ChatScreen(
                        
                        uiState = uiState,
                        petStatus = petViewModel.petStatus.collectAsStateWithLifecycle().value,
                        chatMessages = chatViewModel.pagedChatMessages.collectAsLazyPagingItems(),
                        isListening = chatViewModel.voiceEngine.isListening.collectAsStateWithLifecycle().value,
                        isSpeaking = chatViewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle().value,
                        onSendMessage = { text -> chatViewModel.sendMessage(text) },
                        onSetInputText = { text -> viewModel.setInputText(text) },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onStartVoiceListening = { chatViewModel.startVoiceListening() },
                        onStopVoiceListening = { chatViewModel.stopVoiceListening() },
                        onToggleVoiceOutput = { viewModel.toggleVoiceOutput() }
                    )
                    NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        tasks = lifeHubViewModel.allTasks.collectAsStateWithLifecycle().value,
                        events = lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle().value,
                        wellnessLogs = wellnessViewModel.allWellnessLogs.collectAsStateWithLifecycle().value,
                        memories = wellnessViewModel.allMemories.collectAsStateWithLifecycle().value,
                        dailyBriefing = lifeHubViewModel.dailyBriefing.collectAsStateWithLifecycle().value,
                        goalPlans = lifeHubViewModel.allGoalPlans.collectAsStateWithLifecycle().value,
                        getMilestonesForGoal = { id -> lifeHubViewModel.getMilestonesForGoal(id) },
                        soundState = lifeHubViewModel.soundscapeState.collectAsStateWithLifecycle().value,
                        onAction = handleLifeHubAction
                    )
                    NavDestination.Wellness.tabIndex -> WellnessScreen(
                        viewModel = wellnessViewModel,
                        appViewModel = viewModel,
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) }
                    )
                    NavDestination.Account.tabIndex -> UserAccountScreen(
                        userProfile = userProfile,
                        userFacts = viewModel.userFacts.collectAsStateWithLifecycle().value,
                        petStatus = petViewModel.petStatus.collectAsStateWithLifecycle().value,
                        benchmarkStatus = viewModel.benchmarkStatus.collectAsStateWithLifecycle().value ?: "",
                        tasks = lifeHubViewModel.allTasks.collectAsStateWithLifecycle().value,
                        events = lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle().value,
                        memories = wellnessViewModel.allMemories.collectAsStateWithLifecycle().value,
                        messages = viewModel.chatMessages.collectAsStateWithLifecycle().value,
                        localModelCatalog = aiSettingsViewModel.localModelCatalog,
                        modelDownloadStates = aiSettingsViewModel.modelDownloadStates.collectAsStateWithLifecycle().value,
                        activeLocalModelId = aiSettingsViewModel.activeLocalModelId.collectAsStateWithLifecycle().value,
                        selectedAccelerator = aiSettingsViewModel.selectedAccelerator.collectAsStateWithLifecycle().value,
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
                        }
                    )
                    else -> HomeScreen(
                        petStatus = petViewModel.petStatus.collectAsStateWithLifecycle().value,
                        uiState = uiState,
                        batteryStatus = viewModel.batteryStatus.collectAsStateWithLifecycle().value,
                        networkStatus = viewModel.networkStatus.collectAsStateWithLifecycle().value,
                        events = lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle().value,
                        tasks = lifeHubViewModel.allTasks.collectAsStateWithLifecycle().value,
                        isListening = chatViewModel.voiceEngine.isListening.collectAsStateWithLifecycle().value,
                        isSpeaking = chatViewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle().value,
                        onPetPetted = { petViewModel.onPetPetted() },
                        onPetTouched = { petViewModel.onPetTouched() },
                        onTogglePetSleep = { petViewModel.togglePetSleep() },
                        onStartVoiceListening = { chatViewModel.startVoiceListening() },
                        onStopVoiceListening = { chatViewModel.stopVoiceListening() },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onShowWardrobe = { viewModel.setShowWardrobeScreen(true) },
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) },
                        onNavigateToAccount = { viewModel.setSelectedTab(NavDestination.Account.tabIndex) },
                        onNavigateToWellness = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) },
                        locationContext = viewModel.locationState.collectAsStateWithLifecycle().value,
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
                com.example.ui.screens.WardrobeScreen(petViewModel = petViewModel, wellnessViewModel = wellnessViewModel, onClose = { viewModel.setShowWardrobeScreen(false) })
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


            // Global Back to Home Button
            if (uiState.selectedTab != NavDestination.PetCompanion.tabIndex) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { viewModel.setSelectedTab(NavDestination.PetCompanion.tabIndex) },
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Back to Home"
                    )
                }
            }
        }
    }
}
}
