package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.theme.*
import com.example.data.device.DeviceCapabilityScannerImpl
import com.example.domain.account.UserProfileData
import com.example.domain.onboarding.DeviceProfile
import com.example.presentation.screens.onboarding.*
import com.example.presentation.viewmodel.LumiViewModel

@Composable
fun OnboardingScreen(
    viewModel: LumiViewModel,
    onComplete: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle(initialValue = UserProfileData())
    var currentStep by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val scanner = remember { DeviceCapabilityScannerImpl(context) }
    var deviceProfile by remember { mutableStateOf<DeviceProfile?>(null) }

    // Aesthetic gradient background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            ObsidianDark,
            SlateDark,
            ObsidianDark
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn(tween(400))) togetherWith
                        (slideOutHorizontally { width -> -width } + fadeOut(tween(400)))
            },
            modifier = Modifier.fillMaxSize(),
            label = "OnboardingTransition"
        ) { step ->
            when (step) {
                0 -> WelcomeStep(onNext = { currentStep++ })
                1 -> PersonaStep(
                    currentPersona = userProfile.personaTone,
                    onSelect = { persona ->
                        viewModel.userProfileManager.updateField { it.copy(personaTone = persona) }
                    },
                    onNext = { currentStep++ }
                )
                2 -> NameAndGoalStep(
                    initialName = userProfile.userName,
                    initialGoal = userProfile.primaryFocusGoal,
                    onNext = { name, goal ->
                        viewModel.userProfileManager.updateField {
                            it.copy(
                                userName = name.ifBlank { "User" },
                                primaryFocusGoal = goal.ifBlank { "Stay focused, balanced & mindful" }
                            )
                        }
                        deviceProfile = scanner.scanDevice()
                        currentStep++
                    }
                )
                3 -> HardwareScanStep(
                    profile = deviceProfile,
                    onNext = { currentStep++ }
                )
                4 -> ModelDownloadStep(
                    profile = deviceProfile,
                    onComplete = {
                        viewModel.userProfileManager.updateField { it.copy(hasCompletedOnboarding = true) }
                        onComplete()
                    }
                )
            }
        }
    }
}
