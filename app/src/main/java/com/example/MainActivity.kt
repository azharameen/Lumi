package com.example

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.theme.MyApplicationTheme
import com.example.framework.AppShortcutsManager
import com.example.presentation.LumiApp
import com.example.presentation.viewmodel.AiSettingsViewModel
import com.example.presentation.viewmodel.AuthViewModel
import com.example.presentation.viewmodel.ChatViewModel
import com.example.presentation.viewmodel.LifeHubViewModel
import com.example.presentation.viewmodel.LumiViewModel
import com.example.presentation.viewmodel.PetViewModel
import com.example.presentation.viewmodel.WellnessViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

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
            val petPrimary = Color(petStatus.bloubSkinColor.primaryHex)
            val petSecondary = Color(petStatus.bloubSkinColor.endHex)
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

    override fun onResume() {
        super.onResume()
        syncOverlayState()
    }

    private fun syncOverlayState() {
        val canDraw = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(this)
        } else {
            true
        }

        if (!canDraw) {
            val stopIntent = Intent(this, com.example.framework.PetOverlayService::class.java).apply {
                action = com.example.framework.PetOverlayService.ACTION_STOP
            }
            try {
                startService(stopIntent)
                stopService(Intent(this, com.example.framework.PetOverlayService::class.java))
            } catch (_: Exception) {}
            viewModel.setOverlayEnabled(false)
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
            val type: com.example.domain.briefing.BriefingType? = when (briefingStr.uppercase(java.util.Locale.ROOT)) {
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
