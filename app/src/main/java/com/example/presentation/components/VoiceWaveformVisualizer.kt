package com.example.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiViolet
import kotlin.math.sin
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

@Composable
fun VoiceWaveformVisualizer(
    isActive: Boolean,
    audioLevel: Float,
    modifier: Modifier = Modifier
) {
    val phaseAnim = remember { Animatable(0f) }
    LaunchedEffect(isActive) {
        if (isActive) {
            phaseAnim.animateTo(
                targetValue = (Math.PI * 2).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        } else {
            phaseAnim.snapTo(0f)
        }
    }

    Canvas(
        modifier = modifier
            .width(180.dp)
            .height(36.dp)
    ) {
        val barCount = 18
        val barWidth = MaterialTheme.spacing.extraSmall.toPx()
        val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
        val cy = size.height / 2

        for (i in 0 until barCount) {
            val x = i * (barWidth + spacing)
            val normalizedIdx = i.toFloat() / barCount
            val wave = if (isActive) {
                (sin(normalizedIdx * 8f + phaseAnim.value) * 0.5f + 0.5f) * audioLevel.coerceIn(0.2f, 1f)
            } else {
                0.12f
            }

            val barHeight = (size.height * 0.85f * wave).coerceAtLeast(6f)
            val top = cy - barHeight / 2

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(LumiCyan, LumiViolet, LumiPink)
                ),
                topLeft = Offset(x, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }
    }
}
