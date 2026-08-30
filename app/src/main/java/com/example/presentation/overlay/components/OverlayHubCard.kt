package com.example.presentation.overlay.components
import androidx.compose.ui.res.stringResource
import com.example.R


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.domain.model.PetStatus
import com.example.presentation.overlay.models.OverlayTab
import com.example.core.theme.LumiCyan
import com.example.core.theme.LumiGold
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink
import com.example.core.theme.LumiViolet
import com.example.core.theme.LumiYellow
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceDarkVariant
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import com.example.core.theme.TextTertiary

/**
 * Expanded companion card containing the live status header, tab selectors,
 * top focus task preview, quick voice/hydration/petting controls, and subviews.
 */
@Composable
fun OverlayHubCard(
    isExpanded: Boolean,
    petStatus: PetStatus,
    activeTab: OverlayTab,
    onTabSelected: (OverlayTab) -> Unit,
    tasks: List<TaskEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    isRoamMode: Boolean,
    askInputText: String,
    onAskInputChanged: (String) -> Unit,
    isSendingPrompt: Boolean,
    onSendPrompt: () -> Unit,
    isBreathingRunning: Boolean,
    breathingPhase: String,
    breathingProgress: Float,
    onToggleBreathing: () -> Unit,
    onToggleRoam: () -> Unit,
    onToggleTaskComplete: (Long) -> Unit,
    onVoiceToggle: () -> Unit,
    onHydrateClicked: () -> Unit,
    onPetJoyClicked: () -> Unit,
    onOpenApp: () -> Unit,
    onMinimize: () -> Unit,
    onCloseService: () -> Unit,
    onDragStart: (Float, Float) -> Unit,
    onDragMove: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = SurfaceDark.copy(alpha = 0.96f),
            border = BorderStroke(1.5.dp, Brush.linearGradient(listOf(LumiCyan, LumiViolet))),
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Top Draggable Handle Pill for Repositioning Expanded Card
                OverlayCardDragBar(
                    onDragStart = onDragStart,
                    onDragMove = onDragMove,
                    onDragEnd = onDragEnd
                )

                // Header with Lumi status & action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isSpeaking) LumiPink else if (isListening) LumiMint else LumiCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Lumi (Lv.${petStatus.level})",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(
                            onClick = onOpenApp,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInFull,
                                contentDescription = stringResource(id = R.string.desc_open_app),
                                tint = LumiCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        IconButton(
                            onClick = onMinimize,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.desc_minimize),
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab Selector for Floating Hub
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDarkVariant, RoundedCornerShape(10.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OverlayTab.values().forEach { tab ->
                        OverlayTabItem(
                            title = tab.title,
                            isSelected = activeTab == tab,
                            onClick = { onTabSelected(tab) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Content View by Active Tab
                when (activeTab) {
                    OverlayTab.QUICK_MENU -> {
                        Column {
                            // Top Task Preview
                            val pendingTask = tasks.firstOrNull { !it.isCompleted }
                            if (pendingTask != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = SurfaceHighlight,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Top Focus Task",
                                                color = LumiYellow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = pendingTask.title,
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                        IconButton(
                                            onClick = { onToggleTaskComplete(pendingTask.id) },
                                            modifier = Modifier.size(26.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = stringResource(id = R.string.desc_complete),
                                                tint = LumiMint,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Quick Action Buttons Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OverlayQuickActionButton(
                                    icon = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    label = if (isListening) "Listening..." else "Talk",
                                    accentColor = if (isListening) LumiMint else LumiViolet,
                                    onClick = onVoiceToggle
                                )

                                OverlayQuickActionButton(
                                    icon = Icons.Default.WaterDrop,
                                    label = "+1 Water",
                                    accentColor = LumiCyan,
                                    onClick = onHydrateClicked
                                )

                                OverlayQuickActionButton(
                                    icon = Icons.Default.Favorite,
                                    label = "Pet",
                                    accentColor = LumiPink,
                                    onClick = onPetJoyClicked
                                )

                                OverlayQuickActionButton(
                                    icon = Icons.Default.AutoAwesome,
                                    label = if (isRoamMode) "Roam: ON" else "Docked",
                                    accentColor = if (isRoamMode) LumiGold else TextTertiary,
                                    onClick = onToggleRoam
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Dismiss Service Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Close Floating Pet",
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .clickable { onCloseService() }
                                        .padding(4.dp)
                                )
                            }
                        }
                    }

                    OverlayTab.ASK -> {
                        OverlayAskSection(
                            inputText = askInputText,
                            onInputTextChanged = onAskInputChanged,
                            isSending = isSendingPrompt,
                            onSendPrompt = onSendPrompt
                        )
                    }

                    OverlayTab.BREATHE -> {
                        OverlayBreathingSection(
                            isBreathingRunning = isBreathingRunning,
                            breathingPhase = breathingPhase,
                            breathingProgress = breathingProgress,
                            onToggleBreathing = onToggleBreathing
                        )
                    }
                }
            }
        }
    }
}
