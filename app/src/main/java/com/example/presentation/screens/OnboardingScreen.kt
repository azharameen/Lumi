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
import androidx.compose.material.icons.filled.Face
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.account.LumiPersonaTone
import com.example.domain.account.UserProfileData
import com.example.presentation.components.LumiCard
import com.example.core.theme.*
import com.example.presentation.viewmodel.LumiViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: LumiViewModel,
    onComplete: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle(initialValue = com.example.domain.account.UserProfileData())
    var currentStep by remember { mutableIntStateOf(0) }

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
                    onComplete = { finalName, finalGoal ->
                        viewModel.userProfileManager.updateField {
                            it.copy(
                                userName = finalName.ifBlank { "Azhar Ameen" },
                                primaryFocusGoal = finalGoal.ifBlank { "Deep Flow, Clean Code & Mindful Living" },
                                hasCompletedOnboarding = true
                            )
                        }
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // Abstract logo representation using Box + Gradient
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
            text = "Welcome to Lumi",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Your private, intelligent, and highly personalized on-device companion. Designed to keep you focused, productive, and grounded.",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(id = R.string.text_begin_journey), fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
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
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Choose a Persona",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "How would you like Lumi to communicate with you?",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            LumiPersonaTone.values().forEach { persona ->
                val isSelected = currentPersona == persona
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(persona) },
                    color = if (isSelected) persona.accentColor.copy(alpha = 0.15f) else SurfaceDarkVariant.copy(alpha = 0.6f),
                    border = if (isSelected) BorderStroke(1.5.dp, persona.accentColor) else BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(stringResource(id = R.string.text_continue), fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun NameAndGoalStep(
    initialName: String,
    initialGoal: String,
    onComplete: (String, String) -> Unit
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var goal by remember(initialGoal) { mutableStateOf(initialGoal) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Final Touches",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tell Lumi a bit about yourself.",
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
            onClick = { onComplete(name, goal) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(id = R.string.text_complete_setup), fontSize = 16.sp, color = ObsidianDark, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ObsidianDark)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
