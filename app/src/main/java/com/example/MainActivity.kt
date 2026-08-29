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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.service.AppShortcutsManager
import com.example.service.PetOverlayService
import com.example.ui.components.BreathingExerciseModal
import com.example.ui.components.CameraVisionDialog
import com.example.ui.components.OverlayPermissionDialog
import com.example.ui.navigation.LumiBottomNavigation
import com.example.ui.navigation.NavDestination
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LifeHubScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianDark
import com.example.ui.viewmodel.LumiViewModel

/**
 * Main Activity hosting Lumi's full-screen application experience.
 * Fully integrated with Android System Share Sheet, App Shortcuts, and Proactive Services.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: LumiViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Dynamic App Shortcuts
        AppShortcutsManager.initDynamicShortcuts(this)

        // Audio permission for speech recognition features
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), PERMISSION_RECORD_AUDIO_CODE)
        }

        // Handle incoming intent (Shares, Shortcuts, Alarms, Widgets)
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                LumiApp(viewModel = viewModel)
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
            val type = when (briefingStr.uppercase()) {
                "MORNING" -> com.example.domain.briefing.BriefingType.MORNING
                "EVENING" -> com.example.domain.briefing.BriefingType.EVENING
                else -> null
            }
            viewModel.refreshDailyBriefing(type)
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
fun LumiApp(viewModel: LumiViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            LumiBottomNavigation(
                selectedTabIndex = uiState.selectedTab,
                onTabSelected = { tabIndex -> viewModel.setSelectedTab(tabIndex) }
            )
        }
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
                        viewModel = viewModel,
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) }
                    )
                    NavDestination.Assistant.tabIndex -> ChatScreen(
                        viewModel = viewModel
                    )
                    NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )
                    else -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) }
                    )
                }
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

            // Fullscreen Live Voice Conversation Companion Mode
            if (uiState.showLiveVoiceMode) {
                com.example.ui.screens.LiveVoiceModeScreen(
                    viewModel = viewModel,
                    onClose = { viewModel.setShowLiveVoiceMode(false) }
                )
            }
        }
    }
}
