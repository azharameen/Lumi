package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ChatMessageEntity
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiGreen
import com.example.ui.theme.LumiPink
import com.example.ui.theme.LumiViolet
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LumiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: LumiViewModel
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isListening by viewModel.voiceEngine.isListening.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val audioLevel by viewModel.voiceEngine.audioWaveformLevel.collectAsState()
    val petStatus by viewModel.petStatus.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
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
        // Top Header
        Surface(
            color = SurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(LumiCyan.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "✨", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Lumi Companion Chat",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (petStatus.isThinking) "Thinking deeply..." else petStatus.currentEmotion.displayName,
                            color = LumiCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                // Voice output toggle
                IconButton(
                    onClick = { viewModel.toggleVoiceOutput() },
                    modifier = Modifier.testTag("toggle_voice_output")
                ) {
                    Icon(
                        imageVector = if (uiState.isTtsVoiceOutputEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Voice Toggle",
                        tint = if (uiState.isTtsVoiceOutputEnabled) LumiCyan else TextSecondary
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(chatMessages) { msg ->
                ChatMessageBubble(message = msg)
            }
        }

        // Realtime waveform if voice active
        AnimatedVisibility(visible = isListening || isSpeaking) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarkVariant)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VoiceWaveformVisualizer(
                    isActive = true,
                    audioLevel = audioLevel
                )
            }
        }

        // Quick Starter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickStarters) { prompt ->
                Surface(
                    color = SurfaceDarkVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { viewModel.sendMessage(prompt) }
                ) {
                    Text(
                        text = prompt,
                        color = LumiCyan,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Bottom Input Row
        Surface(
            color = SurfaceDark,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 70.dp) // accommodate navigation bar
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Camera Vision
                IconButton(
                    onClick = { viewModel.setShowCamera(true) },
                    modifier = Modifier.testTag("chat_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera Vision",
                        tint = LumiCyan
                    )
                }

                // Voice Mic
                IconButton(
                    onClick = {
                        if (isListening) viewModel.stopVoiceListening() else viewModel.startVoiceListening()
                    },
                    modifier = Modifier
                        .background(if (isListening) LumiPink else Color.Transparent, CircleShape)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListening) ObsidianDark else LumiViolet
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = { viewModel.setInputText(it) },
                    placeholder = { Text("Ask Lumi anything...", color = TextSecondary, fontSize = 13.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiCyan,
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

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = {
                        val text = uiState.inputText
                        if (text.isNotBlank()) {
                            viewModel.sendMessage(text)
                        }
                    },
                    modifier = Modifier
                        .background(LumiCyan, CircleShape)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
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
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(text = "Lumi", color = LumiCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                message.petEmotion.let { emo ->
                    Surface(
                        color = LumiViolet.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = emo,
                            color = LumiViolet,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = if (isGemmaOnDevice) LumiGreen.copy(alpha = 0.15f) else LumiCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (isGemmaOnDevice) "⚡ Gemma 2B (Local)" else "☁️ Gemini 2.5 Flash",
                        color = if (isGemmaOnDevice) LumiGreen else LumiCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Surface(
            color = if (isUser) LumiViolet else SurfaceDarkVariant,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.imageBase64OrUri != null) {
                    Row(
                        modifier = Modifier
                            .background(ObsidianDark.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Captured Image Shared", color = TextPrimary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.content,
                    color = if (isUser) Color.White else TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                // Tool Execution Report Badge
                if (message.toolUsedName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianDark.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
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
                                color = LumiCyan,
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
            color = TextSecondary.copy(alpha = 0.6f),
            fontSize = 9.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
