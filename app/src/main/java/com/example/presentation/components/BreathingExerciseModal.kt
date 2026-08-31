package com.example.presentation.components
import androidx.compose.ui.res.stringResource
import com.example.R


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import com.example.core.theme.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiViolet
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun BreathingExerciseModal(
    pattern: String = "Box Breathing (4-4-4-4)",
    totalCycles: Int = 4,
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    var currentCycle by remember { mutableIntStateOf(1) }
    var instructionText by remember { mutableStateOf("Inhale slowly...") }
    val scaleAnim = remember { Animatable(0.6f) }

    LaunchedEffect(Unit) {
        for (cycle in 1..totalCycles) {
            currentCycle = cycle

            // 1. Inhale (4s)
            instructionText = "Inhale slowly & deeply..."
            scaleAnim.animateTo(1.25f, tween(4000, easing = FastOutSlowInEasing))

            // 2. Hold (4s)
            instructionText = "Hold gently & be still..."
            delay(4000)

            // 3. Exhale (4s)
            instructionText = "Exhale slowly & release tension..."
            scaleAnim.animateTo(0.6f, tween(4000, easing = FastOutSlowInEasing))

            // 4. Rest (4s)
            instructionText = "Rest & center yourself..."
            delay(4000)
        }
        instructionText = "Wonderful work! Feeling refreshed. ✨"
        delay(1200)
        onComplete()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = ObsidianDark,
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.text_mindful_coherence),
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cycle $currentCycle of $totalCycles • $pattern",
                            color = LumiCyan,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(SurfaceDark, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = stringResource(id = R.string.desc_close), tint = TextSecondary)
                    }
                }

                // Center animated expanding breath sphere
                Box(
                    modifier = Modifier.size(280.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val scale = scaleAnim.value
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2
                        val cy = size.height / 2
                        val radius = (size.width / 3f) * scale

                        // Outer glowing pulse ring
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(LumiCyan.copy(alpha = 0.35f), Color.Transparent),
                                center = Offset(cx, cy),
                                radius = radius * 1.4f
                            ),
                            radius = radius * 1.4f,
                            center = Offset(cx, cy)
                        )

                        // Inner vibrant sphere
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(LumiCyanBright, LumiViolet, LumiPink),
                                center = Offset(cx - radius * 0.3f, cy - radius * 0.3f),
                                radius = radius * 1.2f
                            ),
                            radius = radius,
                            center = Offset(cx, cy)
                        )

                        // Boundary stroke
                        drawCircle(
                            color = Color.White.copy(alpha = 0.6f),
                            radius = radius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 3f)
                        )
                    }

                    Text(
                        text = if (scaleAnim.value > 1.0f) "FULL" else if (scaleAnim.value < 0.75f) "EMPTY" else "BREATHE",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp
                    )
                }

                // Bottom prompt & finish
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = instructionText,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark, contentColor = TextPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(id = R.string.text_finish_exercise))
                    }
                }
            }
        }
    }
}
