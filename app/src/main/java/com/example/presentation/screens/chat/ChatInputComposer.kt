package com.example.presentation.screens.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics

@Composable
fun ChatInputComposer(
    inputText: String,
    onSetInputText: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onShowCamera: () -> Unit,
    onStartVoiceListening: () -> Unit,
    isListening: Boolean,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDark,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera Vision Button
            IconButton(
                onClick = { haptics.performTick(); onShowCamera() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Text Input Field Container
            Surface(
                color = SurfaceDark.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (inputText.isEmpty()) {
                        Text(stringResource(R.string.text_ask_lumi_anything), color = TextTertiary, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = onSetInputText,
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(LumiCyan),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Conditional Button: Mic or Send
            AnimatedContent(
                targetState = inputText.isNotBlank(),
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.8f))
                        .togetherWith(fadeOut() + scaleOut(targetScale = 0.8f))
                        .using(SizeTransform(clip = false))
                },
                label = "InputActionButton"
            ) { hasText ->
                if (hasText) {
                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                haptics.performSuccess()
                                onSendMessage(inputText.trim())
                                onSetInputText("")
                            }
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(LumiCyan, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(20.dp))
                    }
                } else {
                    // Mic Button
                    IconButton(
                        onClick = { haptics.performTick(); onStartVoiceListening() },
                        modifier = Modifier
                            .size(44.dp)
                            .background(if (isListening) LumiPink else SurfaceHighlight, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isListening) Color.White else LumiCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
