package com.example.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.core.utils.rememberLumiHaptics
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.model.LumiRemoteConfig
import com.example.domain.model.PetStatus
import com.example.presentation.screens.chat.*
import com.example.presentation.viewmodel.LumiUiState
import kotlinx.coroutines.launch
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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var previewImageSource by remember { mutableStateOf<String?>(null) }

    val isAtBottom by remember {
        derivedStateOf {
            val firstVisibleIndex = listState.firstVisibleItemIndex
            firstVisibleIndex <= 1
        }
    }

    val remoteConfigManager = remember {
        try {
            GlobalContext.get().get<LumiRemoteConfigManager>()
        } catch (_: Exception) {
            null
        }
    }
    val remoteConfig = remoteConfigManager?.config?.collectAsStateWithLifecycle(initialValue = LumiRemoteConfig())?.value ?: LumiRemoteConfig()

    LaunchedEffect(chatMessages.itemCount) {
        if (chatMessages.itemCount > 0 && isAtBottom) {
            listState.animateScrollToItem(0)
        }
    }

    val copyToClipboard: (String) -> Unit = { text ->
        try {
            val clip = ClipData.newPlainText("Lumi Message", text)
            clipboardManager?.setPrimaryClip(clip)
        } catch (_: Exception) {}
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .imePadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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

            // Pending Approvals & Thoughts
            Column {
                pendingHitlActions.forEach { action ->
                    HitlApprovalActionCard(
                        action = action,
                        onApprove = { onResolveHitlAction(action.stateId, true) },
                        onDecline = { onResolveHitlAction(action.stateId, false) },
                        haptics = haptics
                    )
                }

                if (petStatus.isThinking || uiState.agentThought != null) {
                    AgentThoughtStreamCard(
                        thoughtText = uiState.agentThought ?: "Lumi is reasoning..."
                    )
                }

                if (uiState.detectedClipboardText != null) {
                    ClipboardPromptBanner(
                        snippet = uiState.detectedClipboardText,
                        onAnalyze = { snippet ->
                            onProcessClipboard(snippet)
                            onSendMessage("Analyze this: $snippet")
                        },
                        onDismiss = onDismissClipboard,
                        haptics = haptics
                    )
                }
            }

            // Messages Flow
            Box(modifier = Modifier.weight(1f)) {
                if (chatMessages.itemCount == 0) {
                    ChatEmptyStateView(
                        remoteConfig = remoteConfig,
                        onSelectStarter = { onSendMessage(it) },
                        haptics = haptics
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
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
                            if (msg != null && (searchQuery.isBlank() || msg.content.contains(searchQuery, ignoreCase = true))) {
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

                // Scroll to Bottom FAB
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isAtBottom,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch { listState.animateScrollToItem(0) }
                            haptics.performTick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark.copy(alpha = 0.9f)),
                        border = BorderStroke(1.dp, LumiCyan.copy(alpha = 0.5f)),
                        shape = CircleShape,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("New Messages", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Footer Section
            Column {
                VoiceActivityOverlayBar(
                    isListening = isListening,
                    isSpeaking = isSpeaking,
                    onStopListening = onStopVoiceListening,
                    onStopSpeaking = { onSpeakMessage("") }
                )

                QuickPromptChipsBar(
                    onSelectPrompt = { onSendMessage(it) },
                    onOpenTemplates = { showTemplatePicker = true },
                    haptics = haptics
                )

                ChatInputComposer(
                    inputText = uiState.inputText,
                    onSetInputText = onSetInputText,
                    onSendMessage = onSendMessage,
                    onShowCamera = onShowCamera,
                    onStartVoiceListening = onStartVoiceListening,
                    isListening = isListening,
                    haptics = haptics
                )
            }
        }
    }

    // Dialogs
    if (showClearConfirmDialog) {
        ClearChatConfirmDialog(
            onConfirm = { showClearConfirmDialog = false; onClearChat() },
            onDismiss = { showClearConfirmDialog = false }
        )
    }

    if (showTemplatePicker) {
        PromptTemplatePickerModal(
            onSelectPrompt = { onSendMessage(it) },
            onDismiss = { showTemplatePicker = false },
            haptics = haptics
        )
    }

    previewImageSource?.let { src ->
        ChatImagePreviewDialog(imageSource = src, onDismiss = { previewImageSource = null })
    }
}
