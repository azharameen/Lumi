package com.example.presentation.overlay.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiPink
import com.example.core.theme.ObsidianDark
import com.example.core.theme.TextPrimary

/**
 * Floating speech/listening bubble rendered directly above the compact Lumi overlay pet.
 */
@Composable
fun OverlaySpeechBubble(
    speechText: String?,
    isVisible: Boolean,
    isListening: Boolean = false,
    isThinking: Boolean = false,
    onBubbleClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayIcon = when {
        isListening -> Icons.Default.Mic
        isThinking -> Icons.Default.Psychology
        else -> Icons.Default.AutoAwesome
    }

    val iconTint = when {
        isListening -> LumiPink
        isThinking -> LumiGold
        else -> LumiCyan
    }

    val bubbleBorderColor = when {
        isListening -> LumiPink.copy(alpha = 0.7f)
        isThinking -> LumiGold.copy(alpha = 0.6f)
        else -> LumiCyan.copy(alpha = 0.45f)
    }

    val textToDisplay = when {
        isListening -> "Listening... Speak to Lumi 🎙️"
        isThinking -> "Thinking & executing... ✨"
        else -> speechText
    }

    val shouldBeVisible = isVisible || isListening || isThinking

    AnimatedVisibility(
        visible = shouldBeVisible && !textToDisplay.isNullOrBlank(),
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = ObsidianDark.copy(alpha = 0.94f),
            border = BorderStroke(1.2.dp, bubbleBorderColor),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
                .clickable { onBubbleClicked() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = displayIcon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = textToDisplay ?: "",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    maxLines = 3,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

