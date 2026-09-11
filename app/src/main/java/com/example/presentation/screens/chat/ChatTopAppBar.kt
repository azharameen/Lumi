package com.example.presentation.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.domain.model.PetEmotion
import com.example.domain.model.PetStatus

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
            color = SurfaceDark,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
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
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.desc_back),
                            tint = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Companion Animated Hologram Avatar Badge
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(haloScale)
                            .background(
                                color = emotionColor.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .clickable {
                                haptics.performSuccess()
                                onOpenBreathing()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emotionEmoji,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = petStatus.name.ifBlank { stringResource(R.string.text_lumi) },
                                color = TextPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = emotionColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = petStatus.currentEmotion.name.lowercase().replaceFirstChar { it.uppercase() },
                                    color = emotionColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = when {
                                isListening -> stringResource(R.string.text_listening_now)
                                isSpeaking -> stringResource(R.string.text_speaking_now)
                                else -> "Level ${petStatus.level} • Neural Copilot"
                            },
                            color = when {
                                isListening -> LumiPink
                                isSpeaking -> LumiCyan
                                else -> TextTertiary
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp
                        )
                    }
                }

                // Action Buttons Toolbar
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Search in Chat Toggle
                    IconButton(
                        onClick = {
                            haptics.performTick()
                            onSearchToggle()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.text_search_messages),
                            tint = if (isSearchActive) LumiCyan else TextSecondary
                        )
                    }

                    // Text-To-Speech Toggle
                    IconButton(
                        onClick = {
                            haptics.performTick()
                            onToggleTts()
                        }
                    ) {
                        Icon(
                            imageVector = if (isTtsEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = stringResource(R.string.desc_read_aloud),
                            tint = if (isTtsEnabled) LumiCyan else TextTertiary
                        )
                    }

                    // More Menu
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
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
                                text = {
                                    Text(
                                        stringResource(R.string.text_478_coherence_breathing),
                                        color = TextPrimary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = LumiMint
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onOpenBreathing()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(R.string.text_clear_chat_title),
                                        color = LumiPink
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = null,
                                        tint = LumiPink
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClearChatRequest()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Search Filter Bar (Expandable)
        AnimatedVisibility(
            visible = isSearchActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = SurfaceDarkVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = LumiCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                        cursorBrush = SolidColor(LumiCyan),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    stringResource(R.string.text_search_messages),
                                    color = TextTertiary,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    IconButton(onClick = { onSearchToggle(); onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.desc_close), tint = TextSecondary)
                    }
                }
            }
        }
    }
}
