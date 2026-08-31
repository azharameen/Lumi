package com.example.presentation.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.theme.*
import com.example.domain.model.PetStatus
import com.example.presentation.pet.LumiPetView
import com.example.presentation.viewmodel.AuthViewModel

/**
 * Clean & friendly Google Sign-In & Onboarding Screen featuring Lumi the mascot.
 */
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    petStatus: PetStatus = PetStatus(),
    onLoginSuccess: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Reactively trigger success callback when user is authenticated
    LaunchedEffect(uiState.user) {
        if (uiState.user != null) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianDark,
                        SlateDark.copy(alpha = 0.5f),
                        ObsidianDark
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Subtle ambient background glow behind Lumi
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        LumiCyan.copy(alpha = 0.15f),
                        LumiViolet.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(canvasWidth / 2, canvasHeight * 0.32f),
                    radius = canvasWidth * 0.65f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // TOP: Interactive Pet Mascot Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Animated interactive Lumi Pet
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(190.dp)
                        .padding(bottom = 8.dp)
                ) {
                    LumiPetView(
                        petStatus = petStatus,
                        size = 180.dp,
                        enableInternalGestures = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "IYWA • LUMI",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your intelligent adaptive companion & mindful life operating system",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // MIDDLE: Actions & Error Notice
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Friendly Error Alert Banner
                AnimatedVisibility(
                    visible = uiState.error != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LumiCoral.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, LumiCoral.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.WarningAmber,
                                contentDescription = "Notice",
                                tint = LumiCoral,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.error ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextPrimary.copy(alpha = 0.95f)
                                )
                            }
                            IconButton(
                                onClick = { authViewModel.clearError() },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Loading State Banner
                AnimatedVisibility(
                    visible = uiState.isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceDark,
                        border = BorderStroke(1.dp, LumiCyan.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = LumiCyan,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = uiState.loadingMessage ?: "Connecting...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Primary Google Sign-In Button
                GoogleSignInEnterpriseButton(
                    isLoading = uiState.isLoading,
                    onClick = {
                        authViewModel.signInWithGoogle(context)
                    },
                    modifier = Modifier.testTag("google_sign_in_button")
                )

                // Secondary: Guest / Offline Mode Button
                OutlinedButton(
                    onClick = {
                        authViewModel.continueAsGuest()
                        onContinueAsGuest()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("guest_mode_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = TextSecondary
                        )
                        Text(
                            text = "Continue as Guest",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // BOTTOM: Clean App Version Footer
            Text(
                text = "v1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

/**
 * Clean Google Sign-In Button with authentic Google Branding.
 */
@Composable
fun GoogleSignInEnterpriseButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.White.copy(alpha = 0.15f),
                spotColor = LumiCyan.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFDADCE0)),
        onClick = onClick,
        enabled = !isLoading
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF1F1F1F),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Connecting...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F1F1F)
                    )
                )
            } else {
                GoogleLogoIcon(modifier = Modifier.size(22.dp))

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Sign in with Google",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F1F1F),
                        letterSpacing = 0.2.sp
                    )
                )
            }
        }
    }
}

/**
 * Authentic Canvas Drawing of the 4-color Google "G" logo.
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val strokeWidth = w * 0.18f

        // Red segment (top arc)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 190f,
            sweepAngle = 120f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Yellow segment (left arc)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 120f,
            sweepAngle = 70f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Green segment (bottom arc)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 30f,
            sweepAngle = 90f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Blue segment (right arc & horizontal bar)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 330f,
            sweepAngle = 60f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
        )

        // Blue horizontal crossbar
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(cx - strokeWidth * 0.2f, cy),
            end = Offset(w - strokeWidth * 0.5f, cy),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Square
        )
    }
}
