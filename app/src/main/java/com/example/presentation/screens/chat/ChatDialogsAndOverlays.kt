package com.example.presentation.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.domain.prompt.DynamicPromptSuggester
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.presentation.components.VoiceWaveformVisualizer

@Composable
fun VoiceActivityOverlayBar(
    isListening: Boolean,
    isSpeaking: Boolean,
    onStopListening: () -> Unit,
    onStopSpeaking: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isListening || isSpeaking,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Surface(
            color = SurfaceDark.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, if (isListening) LumiPink.copy(alpha = 0.6f) else LumiCyan.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isListening) LumiPink.copy(alpha = 0.2f) else LumiCyan.copy(alpha = 0.2f),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Mic else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = if (isListening) LumiPink else LumiCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = if (isListening) stringResource(R.string.text_listening_now) else stringResource(R.string.text_speaking_now),
                        color = TextPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                VoiceWaveformVisualizer(
                    isActive = true,
                    audioLevel = 0.7f,
                    modifier = Modifier.width(120.dp)
                )

                IconButton(
                    onClick = {
                        if (isListening) onStopListening() else onStopSpeaking()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = stringResource(R.string.text_stop_timer),
                        tint = if (isListening) LumiPink else LumiCyan
                    )
                }
            }
        }
    }
}

@Composable
fun QuickPromptChipsBar(
    onSelectPrompt: (String) -> Unit,
    onOpenTemplates: () -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier,
    prompts: List<String> = emptyList()
) {
    val displayPrompts = remember(prompts) {
        if (prompts.isNotEmpty()) prompts else DynamicPromptSuggester.getInitialPrompts()
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark.copy(alpha = 0.7f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Surface(
                color = LumiCyan.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LumiCyan.copy(alpha = 0.4f)),
                modifier = Modifier.clickable {
                    haptics.performTick()
                    onOpenTemplates()
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Templates",
                        color = LumiCyan,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        items(displayPrompts) { prompt ->
            Surface(
                color = SurfaceDarkVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
                modifier = Modifier.clickable {
                    haptics.performSuccess()
                    val cleanPrompt = prompt.replace(Regex("""^[^\p{L}\p{N}]+\s*"""), "").trim().ifBlank { prompt }
                    onSelectPrompt(cleanPrompt)
                }
            ) {
                Text(
                    text = prompt,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PromptTemplatePickerModal(
    onSelectPrompt: (String) -> Unit,
    onDismiss: () -> Unit,
    haptics: LumiHaptics,
    categories: List<Pair<String, List<String>>> = remember { DynamicPromptSuggester.getTemplateCategories() }
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Prompt Templates Library",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            categories.forEach { (categoryName, prompts) ->
                Text(
                    text = categoryName,
                    color = LumiCyan,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    prompts.forEach { prompt ->
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, SurfaceHighlight),
                            modifier = Modifier.clickable {
                                haptics.performSuccess()
                                onSelectPrompt(prompt)
                                onDismiss()
                            }
                        ) {
                            Text(
                                text = prompt,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ClearChatConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.text_clear_chat_title), color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(stringResource(R.string.text_clear_chat_message), color = TextSecondary)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.text_clear_chat_confirm), color = LumiPink, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_cancel), color = TextTertiary)
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun DeleteMessageConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.text_delete_message_title), color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(stringResource(R.string.text_delete_message_confirmation), color = TextSecondary)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.text_delete), color = LumiPink, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_cancel), color = TextTertiary)
            }
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ChatImagePreviewDialog(
    imageSource: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = ObsidianDark,
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, LumiCyan.copy(alpha = 0.4f)),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vision Attachment",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.desc_close), tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = SurfaceDark,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(240.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(stringResource(R.string.text_vision_payload_analyzed), color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.desc_close), color = LumiCyan)
                }
            }
        }
    }
}
