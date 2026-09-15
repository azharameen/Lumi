package com.example.presentation.screens.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.presentation.components.VoiceWaveformVisualizer

/**
 * Enterprise modern chat input composer styled after Antigravity & Copilot.
 * Integrates multi-line text input with action buttons placed cleanly on the bottom row,
 * dynamic model routing indicator, and fluid voice-listening morphing.
 */
@Composable
fun ChatInputComposer(
    inputText: String,
    onSetInputText: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onShowCamera: () -> Unit,
    onStartVoiceListening: () -> Unit,
    isListening: Boolean,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier,
    onOpenTemplates: (() -> Unit)? = null,
    onStopVoiceListening: (() -> Unit)? = null,
    selectedModelId: String = "",
    modelDisplayName: String = "",
    onSelectModel: (() -> Unit)? = null
) {
    Surface(
        color = SurfaceDark,
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            AnimatedContent(
                targetState = isListening,
                transitionSpec = {
                    (fadeIn() + scaleIn(initialScale = 0.95f))
                        .togetherWith(fadeOut() + scaleOut(targetScale = 0.95f))
                        .using(SizeTransform(clip = false))
                },
                label = "ComposerStateTransition"
            ) { listening ->
                if (listening) {
                    // Active Voice Listening Card
                    Surface(
                        color = ObsidianDark.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.2.dp, LumiPink.copy(alpha = 0.7f)),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = LumiPink.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = LumiPink,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = stringResource(R.string.text_listening_now),
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Speak to Lumi... Tap to finish",
                                        color = TextTertiary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            VoiceWaveformVisualizer(
                                isActive = true,
                                audioLevel = 0.75f,
                                modifier = Modifier.width(90.dp).height(24.dp)
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            IconButton(
                                onClick = {
                                    haptics.performTick()
                                    onStopVoiceListening?.invoke() ?: onStartVoiceListening()
                                },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(LumiPink, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop recording",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Modern Copilot / Antigravity Input Card
                    Surface(
                        color = SurfaceDarkVariant.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.65f)),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            // Top: Auto-expanding Text Input Area
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp, bottom = 6.dp, start = 2.dp, end = 2.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (inputText.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.text_ask_lumi_anything),
                                        color = TextTertiary,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                                BasicTextField(
                                    value = inputText,
                                    onValueChange = onSetInputText,
                                    textStyle = MaterialTheme.typography.titleMedium.copy(
                                        color = TextPrimary
                                    ),
                                    cursorBrush = SolidColor(LumiCyan),
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Sentences,
                                        imeAction = ImeAction.Default
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 28.dp, max = 120.dp),
                                    maxLines = 5
                                )
                            }

                            // Bottom: Enterprise Actions Bar (Tools on Left, Action on Right)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left Tools: Vision Camera, Quick Prompts & AI Badge
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Camera Vision Button
                                    Surface(
                                        color = ObsidianDark.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.clickable {
                                            haptics.performTick()
                                            onShowCamera()
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Vision Attachment",
                                                tint = LumiCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    // Prompt Templates Button
                                    if (onOpenTemplates != null) {
                                        Surface(
                                            color = ObsidianDark.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.clickable {
                                                haptics.performTick()
                                                onOpenTemplates()
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Templates",
                                                    tint = LumiCyan,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Prompts",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }

                                    // Model Engine Status Badge — tap to open model picker
                                    Surface(
                                        color = ObsidianDark.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = if (onSelectModel != null) {
                                            Modifier.clickable { onSelectModel() }
                                        } else Modifier
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val (badgeLabel, badgeColor) = when {
                                                selectedModelId.isBlank() -> Pair("✨ Auto", LumiCyan)
                                                selectedModelId.startsWith("gemma", ignoreCase = true) -> Pair("⚡ ${modelDisplayName.ifBlank { "Gemma 2B" }}", LumiGreen)
                                                else -> Pair("☁️ ${modelDisplayName.ifBlank { "Gemini 2.5" }}", LumiViolet)
                                            }
                                            Text(
                                                text = badgeLabel,
                                                color = badgeColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                // Right Action: Single 36dp Circular Action Button (Send or Voice Mic)
                                val hasText = inputText.isNotBlank()
                                val actionBgColor by animateColorAsState(
                                    targetValue = if (hasText) LumiCyan else SurfaceHighlight,
                                    animationSpec = tween(200),
                                    label = "SendMicBg"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(actionBgColor)
                                        .clickable {
                                            if (hasText) {
                                                if (inputText.isNotBlank()) {
                                                    haptics.performSuccess()
                                                    onSendMessage(inputText.trim())
                                                    onSetInputText("")
                                                }
                                            } else {
                                                haptics.performTick()
                                                onStartVoiceListening()
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Crossfade(
                                        targetState = hasText,
                                        animationSpec = tween(150),
                                        label = "SendMicIcon"
                                    ) { isSend ->
                                        if (isSend) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send message",
                                                tint = ObsidianDark,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Mic,
                                                contentDescription = "Voice Input",
                                                tint = LumiCyan,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

