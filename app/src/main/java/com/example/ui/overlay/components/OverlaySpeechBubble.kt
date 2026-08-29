package com.example.ui.overlay.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.TextPrimary

/**
 * Floating speech bubble rendered directly over the compact Lumi avatar when speaking or offering advice.
 */
@Composable
fun OverlaySpeechBubble(
    speechText: String?,
    isVisible: Boolean,
    onBubbleClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    speechText?.let { text ->
        AnimatedVisibility(
            visible = isVisible && text.isNotBlank(),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = modifier
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ObsidianDark.copy(alpha = 0.94f),
                border = BorderStroke(1.dp, LumiCyan.copy(alpha = 0.4f)),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clickable { onBubbleClicked() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = LumiCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = text,
                        color = TextPrimary,
                        fontSize = 11.sp,
                        maxLines = 2,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
