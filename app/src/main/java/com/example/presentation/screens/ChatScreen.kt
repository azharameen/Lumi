package com.example.presentation.screens
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.firebase.LumiRemoteConfigManager
import org.koin.core.context.GlobalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.domain.model.LumiRemoteConfig
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.presentation.components.VoiceWaveformVisualizer
import com.example.core.theme.*

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.core.theme.spacing

@Composable
fun ChatScreen(
    uiState: com.example.presentation.viewmodel.LumiUiState,
    petStatus: com.example.domain.model.PetStatus,
    chatMessages: LazyPagingItems<com.example.data.local.entity.ChatMessageEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    onSendMessage: (String) -> Unit,
    onSetInputText: (String) -> Unit,
    onShowCamera: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onToggleVoiceOutput: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    val listState = rememberLazyListState()

    val remoteConfigManager = remember {
        try {
            GlobalContext.get().get<LumiRemoteConfigManager>()
        } catch (_: Exception) {
            null
        }
    }
    val remoteConfig = remoteConfigManager?.config?.collectAsStateWithLifecycle(initialValue = LumiRemoteConfig())?.value ?: LumiRemoteConfig()

    LaunchedEffect(chatMessages.itemCount) {
        if (chatMessages.itemCount > 0) {
            listState.animateScrollToItem(0) // Items are reversed, newest at 0
        }
    }

    val quickStarters = listOf(
        "Plan my day efficiently",
        "I feel overwhelmed with work",
        "Start 4-7-8 breathing session",
        "Log 2 cups of water & mood",
        "Give me personalized wellness insights",
        "Add high priority task: Finish project"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        // Top Companion Status Header
        Surface(
            color = SurfaceGlass,
            border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.radialGradient(
                                    listOf(androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Color.Transparent)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            shape = CircleShape,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = "✨", fontSize = 16.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.text_lumi_neural_companion),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(LumiMint, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (petStatus.isThinking) "Thinking deeply..." else petStatus.currentEmotion.displayName,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }

                // Voice output toggle
                IconButton(
                    onClick = { onToggleVoiceOutput() },
                    modifier = Modifier.testTag("toggle_voice_output")
                ) {
                    Icon(
                        imageVector = if (uiState.isTtsVoiceOutputEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                        contentDescription = stringResource(id = R.string.desc_voice_toggle),
                        tint = if (uiState.isTtsVoiceOutputEnabled) androidx.compose.material3.MaterialTheme.colorScheme.primary else TextTertiary
                    )
                }
            }
        }

        // Messages List
        if (chatMessages.itemCount == 0) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = SurfaceDark.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(LumiCyan.copy(alpha = 0.5f), LumiPink.copy(alpha = 0.5f)))),
                    shadowElevation = 6.dp,
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "✨", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = remoteConfig?.welcomeGreeting ?: stringResource(R.string.text_lumi_neural_companion),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = remoteConfig?.companionTipOfTheDay ?: "I'm here to help manage your schedule, log wellness, and keep you company. Ask me anything!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    count = chatMessages.itemCount,
                    key = chatMessages.itemKey { it.id },
                    contentType = chatMessages.itemContentType { "chat_message" }
                ) { index ->
                    val msg = chatMessages[index]
                    if (msg != null) {
                        ChatMessageBubble(message = msg)
                    }
                }
            }
        }

        // Realtime waveform if voice active
        AnimatedVisibility(visible = isListening || isSpeaking) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkVariant.copy(alpha = 0.9f))
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceWaveformVisualizer(
                    isActive = true,
                    audioLevel = 0f
                )
            }
        }

        // Quick Starter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark.copy(alpha = 0.6f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            items(quickStarters) { prompt ->
                Surface(
                    color = SurfaceDarkVariant.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onSendMessage(prompt) }
                ) {
                    Text(
                        text = prompt,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Input Row
        Surface(
            color = SurfaceGlass,
            border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = MaterialTheme.spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camera Vision
                IconButton(
                    onClick = { onShowCamera() },
                    modifier = Modifier.testTag("chat_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(id = R.string.desc_camera_vision),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }

                // Voice Mic
                IconButton(
                    onClick = {
                        if (isListening) onStopVoiceListening() else onStartVoiceListening()
                    },
                    modifier = Modifier
                        .background(if (isListening) LumiPink else Color.Transparent, CircleShape)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = stringResource(id = R.string.desc_voice_input),
                        tint = if (isListening) ObsidianDark else androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))

                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { onSetInputText(it) },
                    placeholder = { Text(stringResource(id = R.string.text_ask_lumi_anything), color = TextTertiary, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        val text = uiState.inputText
                        if (text.isNotBlank()) {
                            onSendMessage(text)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(id = R.string.desc_send),
                        tint = ObsidianDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessageEntity) {
    val isUser = message.sender == "USER"
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            val isGemmaOnDevice = message.content.contains("[Gemma On-Device]")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 5.dp, start = MaterialTheme.spacing.extraSmall)
            ) {
                Text(text = stringResource(R.string.text_lumi), color = androidx.compose.material3.MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                message.petEmotion.let { emo ->
                    Surface(
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
                    ) {
                        Text(
                            text = emo,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (isGemmaOnDevice) LumiGreen.copy(alpha = 0.15f) else androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
                ) {
                    Text(
                        text = if (isGemmaOnDevice) "⚡ Gemma 2B (Local)" else "☁️ Gemini 2.5 Flash",
                        color = if (isGemmaOnDevice) LumiGreen else androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall, vertical = 1.dp)
                    )
                }
            }
        }

        Surface(
            color = if (isUser) androidx.compose.material3.MaterialTheme.colorScheme.primary else SurfaceDarkVariant.copy(alpha = 0.95f),
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else MaterialTheme.spacing.extraSmall,
                bottomEnd = if (isUser) MaterialTheme.spacing.extraSmall else 18.dp
            ),
            border = if (isUser) null else BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.imageBase64OrUri != null) {
                    Row(
                        modifier = Modifier
                            .background(ObsidianDark.copy(alpha = 0.6f), RoundedCornerShape(MaterialTheme.spacing.small))
                            .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = if (isUser) ObsidianDark else androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(text = stringResource(R.string.text_captured_image_shared), color = if (isUser) ObsidianDark else TextPrimary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.content,
                    color = if (isUser) ObsidianDark else TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Tool Execution Report Badge
                if (message.toolUsedName != null) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianDark.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(MaterialTheme.spacing.small)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = LumiGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = message.toolResultJson ?: message.toolUsedName,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = timeFormat.format(Date(message.timestamp)),
            color = TextTertiary,
            fontSize = 9.sp,
            modifier = Modifier.padding(top = 3.dp, start = MaterialTheme.spacing.extraSmall, end = MaterialTheme.spacing.extraSmall)
        )
    }
}
