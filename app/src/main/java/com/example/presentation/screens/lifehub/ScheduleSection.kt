package com.example.presentation.screens.lifehub

import com.example.presentation.components.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CalendarEventEntity
import com.example.core.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleSection(
    events: List<CalendarEventEntity>,
    onAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit,
    dailyBriefing: com.example.domain.briefing.DailyBriefing?,
) {
    var showAddEventDialog by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Proactive AI Briefing Card
            item {
                ProactiveDailyBriefingCard(
                    briefing = dailyBriefing,
                    onSpeakBriefing = { onAction(com.example.presentation.viewmodel.LumiUiAction.SpeakBriefing) },
                    onNavigateToChat = { onAction(com.example.presentation.viewmodel.LumiUiAction.NavigateToChat(it)) }
                )
            }

            // Header Card with AI Planning Trigger
            item {
                LumiCard(
                    borderColor = LumiCyan.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = currentDate,
                                color = LumiCyan,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${events.size} Events Scheduled",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { onAction(com.example.presentation.viewmodel.LumiUiAction.NavigateToChat("Please help me optimize and plan my schedule for today.")) },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan.copy(alpha = 0.18f)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = LumiCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI Plan Day",
                                color = LumiCyan,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Event List or Empty State
            if (events.isEmpty()) {
                item {
                    LumiEmptyState(
                        title = "Your Agenda is Clear",
                        description = "Tap the '+' button below or ask Lumi in Chat to organize your meetings, deep work sessions, and workouts!",
                        icon = Icons.Default.CalendarMonth,
                        accentColor = LumiCyan,
                        actionButtonText = "Schedule Event",
                        onActionClick = { showAddEventDialog = true }
                    )
                }
            } else {
                items(events, key = { it.id }) { event ->
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val startTimeStr = timeFormat.format(Date(event.startTimeMillis))
                    val endTimeStr = timeFormat.format(Date(event.endTimeMillis))

                    val eventColor = when (event.colorHex.uppercase()) {
                        "#00E5FF" -> LumiCyan
                        "#FF4081" -> LumiPink
                        "#FFD700" -> LumiGold
                        "#00E676" -> LumiGreen
                        else -> LumiViolet
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timeline Rail Dot and Time
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(62.dp)
                        ) {
                            Text(
                                text = startTimeStr,
                                color = eventColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(eventColor)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(28.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(eventColor.copy(alpha = 0.8f), SurfaceHighlight)
                                        )
                                    )
                            )
                        }

                        // Event Card
                        LumiCard(
                            borderColor = eventColor.copy(alpha = 0.35f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        color = TextPrimary,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$startTimeStr - $endTimeStr",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (!event.location.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = TextSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = event.location,
                                                color = TextSecondary,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = { onAction(com.example.presentation.viewmodel.LumiUiAction.DeleteCalendarEvent(event.id)) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(id = R.string.desc_delete_event),
                                        tint = TextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Event
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            containerColor = LumiCyan,
            contentColor = ObsidianDark,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("btn_add_event")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.desc_add_event))
        }

        if (showAddEventDialog) {
            AddEventDialog(
                onDismiss = { showAddEventDialog = false },
                onAddEvent = { title, desc, startOffsetHours, durationHours, loc, color ->
                    val now = System.currentTimeMillis()
                    val start = now + (startOffsetHours * 3600000L)
                    val end = start + (durationHours * 3600000L)
                    onAction(com.example.presentation.viewmodel.LumiUiAction.AddCalendarEvent(
                        CalendarEventEntity(
                            title = title,
                            description = desc,
                            startTimeMillis = start,
                            endTimeMillis = end,
                            location = loc,
                            colorHex = color
                        )
                    ))
                    showAddEventDialog = false
                }
            )
        }
    }
}
