package com.example.presentation.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiGreen
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiViolet
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceDarkVariant
import com.example.core.theme.SurfaceGlass
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import com.example.core.theme.TextTertiary
import com.example.core.theme.spacing
import com.example.core.utils.LumiHaptics
import com.example.data.local.entity.ChatMessageEntity
import com.example.domain.agent.hitl.HitlPendingAction
import com.example.domain.model.LumiRemoteConfig
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus
import com.example.presentation.components.VoiceWaveformVisualizer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val LumiAmber = Color(0xFFFFB300)

@Composable
fun ChatTopAppBar(
    petStatus: PetStatus,
    isListening: Boolean,
    isSpeaking: Boolean,
    isTtsEnabled: Boolean,
    onToggleTts: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearChatRequest: () -> Unit,
    onSearchToggle: () -> Unit,
    isSearchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenBreathing: () -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "CompanionHoloTransition")
    val haloScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    val emotionColor = when (petStatus.currentEmotion) {
        PetEmotion.HAPPY -> LumiCyan
        PetEmotion.LOVING, PetEmotion.PLAYFUL -> LumiPink
        PetEmotion.ENERGETIC -> LumiAmber
        PetEmotion.CALM -> LumiMint
        PetEmotion.THINKING -> LumiViolet
        PetEmotion.SLEEPY -> TextTertiary
        else -> TextPrimary
    }

    val emotionEmoji = when (petStatus.currentEmotion) {
        PetEmotion.HAPPY -> "✨"
        PetEmotion.LOVING -> "💖"
        PetEmotion.PLAYFUL -> "🐾"
        PetEmotion.ENERGETIC -> "⚡"
        PetEmotion.CALM -> "🌿"
        PetEmotion.THINKING -> "🧠"
        PetEmotion.SLEEPY -> "🌙"
        else -> "🤖"
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            color = SurfaceGlass,
            border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back & Companion Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = { haptics.performTick(); onNavigateBack() },
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Holographic Companion Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(haloScale)
                            .background(
                                Brush.radialGradient(
                                    listOf(emotionColor.copy(alpha = 0.35f), Color.Transparent)
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            color = emotionColor.copy(alpha = 0.2f),
                            shape = CircleShape,
                            border = BorderStroke(1.5.dp, emotionColor.copy(alpha = 0.6f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = emotionEmoji,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = stringResource(R.string.text_lumi_neural_companion),
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (petStatus.isThinking) LumiAmber else LumiMint,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = when {
                                    petStatus.isThinking -> "Thinking deeply..."
                                    isListening -> "Listening..."
                                    isSpeaking -> "Speaking..."
                                    else -> "${petStatus.currentEmotion.displayName} • Synced"
                                },
                                color = if (petStatus.isThinking) LumiAmber else emotionColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Action Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Search toggle
                    IconButton(
                        onClick = { haptics.performTick(); onSearchToggle() },
                        modifier = Modifier.testTag("chat_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.text_search_messages),
                            tint = if (isSearchActive) LumiCyan else TextSecondary
                        )
                    }

                    // Voice output toggle
                    IconButton(
                        onClick = { haptics.performTick(); onToggleTts() },
                        modifier = Modifier.testTag("toggle_voice_output")
                    ) {
                        Icon(
                            imageVector = if (isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = stringResource(R.string.desc_voice_toggle),
                            tint = if (isTtsEnabled) LumiCyan else TextTertiary
                        )
                    }

                    // Overflow Menu
                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.testTag("chat_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                            modifier = Modifier.background(SurfaceDark)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.text_478_coherence_breathing), color = TextPrimary) },
                                onClick = {
                                    menuExpanded = false
                                    haptics.performTick()
                                    onOpenBreathing()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Spa, contentDescription = null, tint = LumiMint)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.text_clear_chat_confirm), color = LumiPink) },
                                onClick = {
                                    menuExpanded = false
                                    haptics.performTick()
                                    onClearChatRequest()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = LumiPink)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Expandable Search Bar
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = SurfaceDark,
                border = BorderStroke(1.dp, SurfaceHighlight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text(stringResource(R.string.text_search_messages), color = TextTertiary, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiCyan,
                            unfocusedBorderColor = SurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_search_field")
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = { onSearchToggle(); onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.desc_close), tint = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun HitlApprovalActionCard(
    action: HitlPendingAction,
    onApprove: () -> Unit,
    onDecline: () -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.95f)),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(LumiAmber, LumiCyan)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    color = LumiAmber.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = LumiAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.text_action_pending_approval),
                        color = LumiAmber,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = action.actionTitle,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = action.actionDescription,
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp
            )

            if (action.payloadPreview.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = ObsidianDark.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = action.payloadPreview,
                        color = LumiCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        haptics.performTick()
                        onDecline()
                    }
                ) {
                    Text(stringResource(R.string.text_decline), color = TextTertiary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = LumiGreen,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable {
                        haptics.performSuccess()
                        onApprove()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = ObsidianDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.text_approve),
                            color = ObsidianDark,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AgentThoughtStreamCard(
    thoughtText: String,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ThoughtPulseTransition")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.medium, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark.copy(alpha = 0.9f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(LumiViolet.copy(alpha = pulseAlpha), LumiCyan.copy(alpha = pulseAlpha))))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = LumiViolet,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.text_neural_thought_stream),
                    color = LumiViolet,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
                Text(
                    text = thoughtText,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ClipboardPromptBanner(
    snippet: String,
    onAnalyze: (String) -> Unit,
    onDismiss: () -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SurfaceDarkVariant.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, LumiCyan.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = LumiCyan,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Copied text detected",
                    color = LumiCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = snippet,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Surface(
                color = LumiCyan,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.clickable {
                    haptics.performSuccess()
                    onAnalyze(snippet)
                }
            ) {
                Text(
                    text = "Ask Lumi",
                    color = ObsidianDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.desc_close),
                    tint = TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun ChatEmptyStateView(
    remoteConfig: LumiRemoteConfig?,
    onSelectStarter: (String) -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Ambient Orb Card
        Surface(
            color = SurfaceDark.copy(alpha = 0.85f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(LumiCyan.copy(alpha = 0.4f), LumiViolet.copy(alpha = 0.4f), LumiPink.copy(alpha = 0.4f))
                )
            ),
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth(0.96f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            Brush.radialGradient(listOf(LumiCyan.copy(alpha = 0.3f), Color.Transparent)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨",
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = remoteConfig?.welcomeGreeting ?: "Hello! I'm Lumi, your Neural Companion.",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = remoteConfig?.companionTipOfTheDay ?: "I can help break down goals, plan your morning, guide coherent breathing, and keep you company. Ask me anything!",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Suggested Explorations",
            color = TextSecondary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        val starters = listOf(
            StarterPrompt("🌿 Start 4-7-8 Breathing", "Calm your nervous system with guided pacing", LumiMint),
            StarterPrompt("📅 Plan My Schedule for Today", "Prioritize tasks and organize time blocks", LumiCyan),
            StarterPrompt("🎯 Break Down a Goal", "Create a decomposed milestone action swarm", LumiAmber),
            StarterPrompt("💧 Log Hydration & Mood", "Record 2 cups of water and energetic mood", LumiPink),
            StarterPrompt("💡 Personalized Wellness Insights", "Review patterns and optimize daily rhythm", LumiViolet)
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            starters.forEach { item ->
                Surface(
                    color = SurfaceDarkVariant.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptics.performSuccess()
                            onSelectStarter(item.title.drop(2).trim())
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title.take(2),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title.drop(2).trim(),
                                color = TextPrimary,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = item.subtitle,
                                color = TextTertiary,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = item.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class StarterPrompt(
    val title: String,
    val subtitle: String,
    val accentColor: Color
)

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    onCopyMessage: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onImageClick: (String) -> Unit,
    haptics: LumiHaptics,
    modifier: Modifier = Modifier
) {
    val isUser = message.sender == "USER"
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    var isLiked by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (!isUser) {
            val isGemmaOnDevice = message.content.contains("[Gemma On-Device]") || message.petEmotion == "OFFLINE"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 5.dp, start = MaterialTheme.spacing.extraSmall)
            ) {
                Surface(
                    color = LumiCyan.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "✨", fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = stringResource(R.string.text_lumi),
                    color = LumiCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = LumiCyan.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = message.petEmotion,
                        color = LumiCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    color = if (isGemmaOnDevice) LumiGreen.copy(alpha = 0.15f) else LumiViolet.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (isGemmaOnDevice) "⚡ Gemma Local" else "☁️ Gemini 2.5",
                        color = if (isGemmaOnDevice) LumiGreen else LumiViolet,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }

        // Message Surface
        Surface(
            color = if (isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                SurfaceDarkVariant.copy(alpha = 0.95f)
            },
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            border = if (isUser) null else BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.6f)),
            shadowElevation = 3.dp,
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (message.imageBase64OrUri != null) {
                    Surface(
                        color = ObsidianDark.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onImageClick(message.imageBase64OrUri) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (isUser) ObsidianDark else LumiCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "📷 Vision Attachment",
                                color = if (isUser) ObsidianDark else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.content,
                    color = if (isUser) ObsidianDark else TextPrimary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )

                if (message.toolUsedName != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianDark.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, LumiGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = LumiGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Tool Executed: ${message.toolUsedName}",
                                    color = LumiGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!message.toolResultJson.isNullOrBlank()) {
                                    Text(
                                        text = message.toolResultJson,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Toolbar & Timestamp
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            Text(
                text = timeFormat.format(Date(message.timestamp)),
                color = TextTertiary,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = stringResource(R.string.text_copy_message),
                tint = TextTertiary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable {
                        haptics.performSuccess()
                        onCopyMessage(message.content)
                    }
            )

            if (!isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(R.string.desc_read_aloud),
                    tint = TextTertiary,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {
                            haptics.performTick()
                            onSpeakMessage(message.content)
                        }
                )

                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (isLiked) LumiPink else TextTertiary,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable {
                            isLiked = !isLiked
                            haptics.performTick()
                        }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = stringResource(R.string.text_delete_message),
                tint = TextTertiary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable {
                        haptics.performTick()
                        onDeleteMessage(message.id)
                    }
            )
        }
    }
}

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
    modifier: Modifier = Modifier
) {
    val quickStarters = listOf(
        "✨ Plan my day",
        "🌿 4-7-8 Breathing",
        "💧 Log 2 cups water",
        "🎯 Break down goal",
        "📝 Add high priority task",
        "🧘 How to calm stress"
    )

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

        items(quickStarters) { prompt ->
            Surface(
                color = SurfaceDarkVariant.copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.5f)),
                modifier = Modifier.clickable {
                    haptics.performSuccess()
                    onSelectPrompt(prompt.drop(2).trim())
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
    haptics: LumiHaptics
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

            val categories = listOf(
                "🚀 Focus & Productivity" to listOf(
                    "Plan my day efficiently with time blocks",
                    "Break down my complex project into 3 daily quests",
                    "Help me prioritize: Task A vs Task B",
                    "Summarize my upcoming calendar events"
                ),
                "🌿 Mindfulness & Vitality" to listOf(
                    "Guide me through a 4-7-8 breathing session",
                    "Log 3 cups of water and energetic mood",
                    "I am feeling stressed. Help me ground myself",
                    "Review my sleep and hydration habits this week"
                ),
                "💭 Brainstorming & Reflection" to listOf(
                    "Help me write an uplifting morning gratitude note",
                    "Brainstorm 5 creative ideas for my project",
                    "What should I learn next to level up my skills?"
                )
            )

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
                            Text("Vision payload analyzed by Gemini", color = TextSecondary, fontSize = 12.sp)
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
