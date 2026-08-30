package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.LumiViewModel
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
            Color(0xFF0F0F13), // Deep Obsidian
            Color(0xFF16161E),
            Color(0xFF0F0F13)
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
                    name = userProfile.userName,
                    goal = userProfile.primaryFocusGoal,
                    onNameChange = { n -> viewModel.userProfileManager.updateField { it.copy(userName = n) } },
                    onGoalChange = { g -> viewModel.userProfileManager.updateField { it.copy(primaryFocusGoal = g) } },
                    onNext = {
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
                    Brush.linearGradient(
                        colors = listOf(androidx.compose.material3.MaterialTheme.colorScheme.primary, androidx.compose.material3.MaterialTheme.colorScheme.primary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Face,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Welcome to Lumi",
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-1).sp
            ),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your private, intelligent, and highly personalized on-device companion. Designed to keep you focused and grounded.",
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
            color = Color.LightGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Begin Journey", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
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
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Choose a Persona",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "How would you like Lumi to communicate with you?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            LumiPersonaTone.values().forEach { persona ->
                val isSelected = currentPersona == persona
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelect(persona) },
                    color = if (isSelected) persona.accentColor.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.2f),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, persona.accentColor) else null,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) persona.accentColor else Color.Gray.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = persona.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White
                            )
                            Text(
                                text = persona.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray
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
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = currentPersona.accentColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun NameAndGoalStep(
    name: String,
    goal: String,
    onNameChange: (String) -> Unit,
    onGoalChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Final Touches",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tell Lumi a bit about yourself.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name", color = Color.LightGray) },
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 18.sp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.DarkGray
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = goal,
            onValueChange = onGoalChange,
            label = { Text("Primary Focus / Goal", color = Color.LightGray) },
            textStyle = LocalTextStyle.current.copy(color = Color.White, fontSize = 16.sp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.DarkGray
            )
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Complete Setup", fontSize = 18.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Black)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
