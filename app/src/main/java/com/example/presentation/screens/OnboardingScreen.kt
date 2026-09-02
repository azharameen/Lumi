package com.example.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.account.LumiPersonaTone
import com.example.domain.account.UserProfileData
import com.example.domain.onboarding.DeviceCapabilityScanner
import com.example.domain.onboarding.DeviceProfile
import com.example.domain.onboarding.ModelRecommendationEngine
import com.example.domain.onboarding.RecommendedDownload
import com.example.presentation.components.LumiCard
import com.example.presentation.utils.accentColor
import com.example.presentation.utils.icon
import com.example.core.theme.*
import com.example.presentation.viewmodel.LumiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

@Composable
fun OnboardingScreen(
    viewModel: LumiViewModel,
    onComplete: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle(initialValue = UserProfileData())
    var currentStep by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val scanner = remember { DeviceCapabilityScanner(context) }
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

@Composable
fun WelcomeStep(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(LumiCyan.copy(alpha = 0.4f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = LumiCyan.copy(alpha = 0.18f),
                shape = CircleShape,
                border = BorderStroke(1.5.dp, LumiCyan),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = LumiCyan,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        Text(
            text = stringResource(R.string.text_welcome_to_lumi),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.text_your_private_intelligent_and_highly_personalized),
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Text(stringResource(id = R.string.text_begin_journey), fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}

@Composable
fun PersonaStep(
    currentPersona: LumiPersonaTone,
    onSelect: (LumiPersonaTone) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = stringResource(R.string.text_choose_a_persona),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.text_how_would_you_like_lumi_to),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            LumiPersonaTone.entries.forEach { persona ->
                val isSelected = currentPersona == persona
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(MaterialTheme.spacing.medium))
                        .clickable { onSelect(persona) },
                    color = if (isSelected) persona.accentColor.copy(alpha = 0.15f) else SurfaceDarkVariant.copy(alpha = 0.6f),
                    border = if (isSelected) BorderStroke(1.5.dp, persona.accentColor) else BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(MaterialTheme.spacing.medium)
                ) {
                    Row(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) persona.accentColor else SurfaceHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = null,
                                tint = if (isSelected) ObsidianDark else TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = persona.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) persona.accentColor else TextPrimary
                            )
                            Text(
                                text = persona.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = currentPersona.accentColor),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Text(stringResource(id = R.string.text_continue), fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}

@Composable
fun NameAndGoalStep(
    initialName: String,
    initialGoal: String,
    onNext: (String, String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var goal by remember(initialGoal) { mutableStateOf(initialGoal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = stringResource(R.string.text_final_touches),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.text_tell_lumi_a_bit_about_yourself),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(36.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(id = R.string.text_your_name), color = TextSecondary) },
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LumiCyan,
                unfocusedBorderColor = SurfaceHighlight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        OutlinedTextField(
            value = goal,
            onValueChange = { goal = it },
            label = { Text(stringResource(id = R.string.text_primary_focus_goal), color = TextSecondary) },
            textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontSize = 15.sp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LumiCyan,
                unfocusedBorderColor = SurfaceHighlight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(14.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = { onNext(name, goal) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Scan Hardware & Models", fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ObsidianDark)
            }
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}

@Composable
fun HardwareScanStep(
    profile: DeviceProfile?,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = "Hardware Diagnostics",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Scanning device RAM, storage, NPU, and AI acceleration capabilities",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        if (profile != null) {
            val totalRamGb = profile.totalRamBytes / (1024 * 1024 * 1024)
            val freeStorageGb = profile.freeStorageBytes / (1024 * 1024 * 1024)

            LumiCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DeveloperBoard, contentDescription = null, tint = LumiCyan)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("RAM: ${totalRamGb} GB (${if (profile.isLowRamDevice) "Low RAM State" else "Optimal"})", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = LumiPink)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Storage Available: ${freeStorageGb} GB", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = LumiMint)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Android AICore / NPU: ${if (profile.isAiCoreAvailable) "Built-in Ready (Gemini Nano)" else "Standard CPU/GPU Delegate"}", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Text("View Recommended Models", fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}

@Composable
fun ModelDownloadStep(
    profile: DeviceProfile?,
    onComplete: () -> Unit
) {
    val engine = remember { ModelRecommendationEngine() }
    val recommendations = remember(profile) {
        if (profile != null) engine.getRecommendations(profile) else emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.large)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        Text(
            text = "Recommended Models",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Optimal models selected for your hardware footprint",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            recommendations.forEach { rec ->
                Surface(
                    color = if (rec.isRecommended) LumiCyan.copy(alpha = 0.15f) else SurfaceDarkVariant,
                    border = BorderStroke(1.dp, if (rec.isRecommended) LumiCyan else SurfaceHighlight),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = if (rec.isRecommended) LumiCyan else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(rec.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(rec.description, color = TextSecondary, fontSize = 11.sp)
                        }
                        Text(rec.sizeDisplay, color = LumiCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(MaterialTheme.spacing.medium)
        ) {
            Text("Complete Setup & Start Lumi", fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))
    }
}
