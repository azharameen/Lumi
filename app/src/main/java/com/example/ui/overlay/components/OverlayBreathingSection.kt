package com.example.ui.overlay.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiPink
import com.example.ui.theme.LumiViolet

/**
 * 4-7-8 Breathing Coherence section inside the floating companion hub.
 */
@Composable
fun OverlayBreathingSection(
    isBreathingRunning: Boolean,
    breathingPhase: String,
    breathingProgress: Float,
    onToggleBreathing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isBreathingRunning) breathingPhase else "4-7-8 Breathing Coherence",
            color = LumiCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Mini Animated Breathing Circle
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(70.dp)
        ) {
            val sizeFactor = if (isBreathingRunning) (0.6f + breathingProgress * 0.4f) else 0.8f
            Box(
                modifier = Modifier
                    .size((60 * sizeFactor).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(LumiCyan.copy(alpha = 0.6f), LumiViolet.copy(alpha = 0.2f))
                        )
                    )
                    .border(1.5.dp, LumiCyan, CircleShape)
            )
            Icon(
                imageVector = Icons.Default.SelfImprovement,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isBreathingRunning) LumiPink.copy(alpha = 0.2f) else LumiCyan.copy(alpha = 0.2f),
            border = BorderStroke(1.dp, if (isBreathingRunning) LumiPink else LumiCyan),
            modifier = Modifier.clickable { onToggleBreathing() }
        ) {
            Text(
                text = if (isBreathingRunning) "Pause Session" else "Start 4-7-8 Breathing",
                color = if (isBreathingRunning) LumiPink else LumiCyan,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
