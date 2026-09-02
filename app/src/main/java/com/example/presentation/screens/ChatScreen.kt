package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.R
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiPink
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceGlass
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextTertiary
import com.example.core.theme.spacing
import com.example.core.utils.LumiHaptics
import com.example.core.utils.rememberLumiHaptics
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.model.LumiRemoteConfig
import com.example.domain.model.PetStatus
import com.example.presentation.screens.chat.AgentThoughtStreamCard
import com.example.presentation.screens.chat.ChatEmptyStateView
import com.example.presentation.screens.chat.ChatImagePreviewDialog
import com.example.presentation.screens.chat.ChatMessageBubble
import com.example.presentation.screens.chat.ChatTopAppBar
import com.example.presentation.screens.chat.ClearChatConfirmDialog
import com.example.presentation.screens.chat.ClipboardPromptBanner
import com.example.presentation.screens.chat.HitlApprovalActionCard
import com.example.presentation.screens.chat.PromptTemplatePickerModal
import com.example.presentation.screens.chat.QuickPromptChipsBar
import com.example.presentation.screens.chat.VoiceActivityOverlayBar
import com.example.presentation.viewmodel.LumiUiState
import org.koin.core.context.GlobalContext

@Composable
fun ChatScreen(
    haptics: LumiHaptics = rememberLumiHaptics(),
    uiState: LumiUiState,
    petStatus: PetStatus,
    chatMessages: LazyPagingItems<ChatMessageEntity>,
    pendingHitlActions: List<HitlPendingAction> = emptyList(),
    isListening: Boolean,
    isSpeaking: Boolean,
    onSendMessage: (String) -> Unit,
    onSetInputText: (String) -> Unit,
    onShowCamera: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onToggleVoiceOutput: () -> Unit,
    onClearChat: () -> Unit = {},
    onDeleteMessage: (Long) -> Unit = {},
    onSpeakMessage: (String) -> Unit = {},
    onResolveHitlAction: (String, Boolean) -> Unit = { _, _ -> },
    onDismissClipboard: () -> Unit = {},
    onProcessClipboard: (String) -> Unit = {},
    onOpenBreathingExercise: () -> Unit = {},
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var previewImageSource by remember { mutableStateOf<String?>(null) }

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
            listState.animateScrollToItem(0)
        }
    }

    val copyToClipboard: (String) -> Unit = { text ->
        try {
            val clip = ClipData.newPlainText("Lumi Message", text)
            clipboardManager?.setPrimaryClip(clip)
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(innerPadding)
    ) {
        // Top Companion Bar
        ChatTopAppBar(
            petStatus = petStatus,
            isListening = isListening,
            isSpeaking = isSpeaking,
            isTtsEnabled = uiState.isTtsVoiceOutputEnabled,
            onToggleTts = onToggleVoiceOutput,
            onNavigateBack = onNavigateBack,
            onClearChatRequest = { showClearConfirmDialog = true },
            onSearchToggle = { isSearchActive = !isSearchActive },
            isSearchActive = isSearchActive,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onOpenBreathing = onOpenBreathingExercise,
            haptics = haptics
        )

        // Pending Human-In-The-Loop Autonomous Approvals
        pendingHitlActions.forEach { action ->
            HitlApprovalActionCard(
                action = action,
                onApprove = { onResolveHitlAction(action.stateId, true) },
                onDecline = { onResolveHitlAction(action.stateId, false) },
                haptics = haptics
            )
        }

        // Live Chain of Thought Stream
        if (petStatus.isThinking || uiState.agentThought != null) {
            AgentThoughtStreamCard(
                thoughtText = uiState.agentThought ?: "Lumi is reasoning, synthesizing context, and orchestrating response..."
            )
        }

        // Detected Clipboard Assistant Banner
        if (uiState.detectedClipboardText != null) {
            ClipboardPromptBanner(
                snippet = uiState.detectedClipboardText,
                onAnalyze = { snippet ->
                    onProcessClipboard(snippet)
                    onSendMessage("Analyze and summarize this clipboard snippet: $snippet")
                },
                onDismiss = onDismissClipboard,
                haptics = haptics
            )
        }

        // Messages Flow
        if (chatMessages.itemCount == 0) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                ChatEmptyStateView(
                    remoteConfig = remoteConfig,
                    onSelectStarter = { prompt ->
                        onSendMessage(prompt)
                    },
                    haptics = haptics
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                reverseLayout = true
            ) {
                items(
                    count = chatMessages.itemCount,
                    key = chatMessages.itemKey { it.id },
                    contentType = chatMessages.itemContentType { "chat_message" }
                ) { index ->
                    val msg = chatMessages[index]
                    if (msg != null) {
                        val matchesSearch = searchQuery.isBlank() || msg.content.contains(searchQuery, ignoreCase = true)
                        if (matchesSearch) {
                            ChatMessageBubble(
                                message = msg,
                                onCopyMessage = copyToClipboard,
                                onSpeakMessage = onSpeakMessage,
                                onDeleteMessage = onDeleteMessage,
                                onImageClick = { previewImageSource = it },
                                haptics = haptics
                            )
                        }
                    }
                }
            }
        }

        // Voice Listening/Speaking Realtime Overlay
        VoiceActivityOverlayBar(
            isListening = isListening,
            isSpeaking = isSpeaking,
            onStopListening = onStopVoiceListening,
            onStopSpeaking = { onSpeakMessage("") }
        )

        // Quick Starter Chips Carousel
        QuickPromptChipsBar(
            onSelectPrompt = { prompt ->
                onSendMessage(prompt)
            },
            onOpenTemplates = { showTemplatePicker = true },
            haptics = haptics
        )

        // Bottom Input Bar Composer
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
                // Camera Vision Button
                IconButton(
                    onClick = {
                        haptics.performTick()
                        onShowCamera()
                    },
                    modifier = Modifier.testTag("chat_camera_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(id = R.string.desc_camera_vision),
                        tint = LumiCyan
                    )
                }

                // Voice Mic Button
                IconButton(
                    onClick = {
                        haptics.performTick()
                        if (isListening) onStopVoiceListening() else onStartVoiceListening()
                    },
                    modifier = Modifier
                        .background(if (isListening) LumiPink else Color.Transparent, CircleShape)
                        .testTag("chat_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = stringResource(id = R.string.desc_voice_input),
                        tint = if (isListening) ObsidianDark else LumiCyan
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text Input Field
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = onSetInputText,
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.text_ask_lumi_anything),
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    },
                    trailingIcon = {
                        if (uiState.inputText.isNotEmpty()) {
                            IconButton(onClick = { onSetInputText("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiCyan,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(6.dp))

                val hasText = uiState.inputText.isNotBlank()
                val sendScale by animateFloatAsState(
                    targetValue = if (hasText) 1.05f else 0.95f,
                    label = "SendScale"
                )

                // Send Button
                IconButton(
                    onClick = {
                        val text = uiState.inputText.trim()
                        if (text.isNotBlank()) {
                            haptics.performSuccess()
                            onSendMessage(text)
                            onSetInputText("")
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .scale(sendScale)
                        .background(
                            if (hasText) LumiCyan else SurfaceDark,
                            CircleShape
                        )
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(id = R.string.desc_send),
                        tint = if (hasText) ObsidianDark else TextTertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Confirmation Dialog for Clearing Chat History
    if (showClearConfirmDialog) {
        ClearChatConfirmDialog(
            onConfirm = {
                showClearConfirmDialog = false
                haptics.performSuccess()
                onClearChat()
            },
            onDismiss = { showClearConfirmDialog = false }
        )
    }

    // Modal Prompt Template Picker
    if (showTemplatePicker) {
        PromptTemplatePickerModal(
            onSelectPrompt = { prompt ->
                onSendMessage(prompt)
            },
            onDismiss = { showTemplatePicker = false },
            haptics = haptics
        )
    }

    // Image Zoom Dialog
    previewImageSource?.let { src ->
        ChatImagePreviewDialog(
            imageSource = src,
            onDismiss = { previewImageSource = null }
        )
    }
}
