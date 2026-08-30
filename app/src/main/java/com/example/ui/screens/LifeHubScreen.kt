package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.WellnessLogEntity
import com.example.ui.theme.LumiCoral
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiGold
import com.example.ui.theme.LumiGreen
import com.example.ui.theme.LumiMint
import com.example.ui.theme.LumiPink
import com.example.ui.theme.LumiViolet
import com.example.ui.theme.LumiYellow
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LumiViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Life Hub Screen: Unifies Schedule & Agenda, Task Management, and Mind & Wellness into a powerful,
 * well-organized 3-segment productivity powerhouse.
 */
@Composable
fun LifeHubScreen(
    viewModel: LumiViewModel,
    onNavigateToChat: (prompt: String?) -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val events by viewModel.allCalendarEvents.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val wellnessLogs by viewModel.allWellnessLogs.collectAsState()
    val memories by viewModel.allMemories.collectAsState()

    val subTabs = listOf("Schedule", "Tasks", "Goal Swarms", "Focus Audio")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Segmented Tab Header
            Surface(
                color = SurfaceDark,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Productivity Hub",
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = LumiGold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LumiGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lumi Sync",
                                    color = LumiGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    androidx.compose.material3.ScrollableTabRow(
                        selectedTabIndex = uiState.lifeHubSubTab.coerceIn(0, subTabs.size - 1),
                        containerColor = SurfaceDark,
                        contentColor = LumiGold,
                        edgePadding = 12.dp,
                        indicator = { tabPositions ->
                            val currentTabIndex = uiState.lifeHubSubTab.coerceIn(0, subTabs.size - 1)
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[currentTabIndex]),
                                color = when (currentTabIndex) {
                                    0 -> LumiCyan
                                    1 -> LumiYellow
                                    2 -> LumiViolet
                                    3 -> LumiMint
                                    else -> LumiCyan
                                },
                                height = 3.dp
                            )
                        }
                    ) {
                        subTabs.forEachIndexed { index, title ->
                            val isSelected = uiState.lifeHubSubTab.coerceIn(0, subTabs.size - 1) == index
                            val tabColor = when (index) {
                                0 -> LumiCyan
                                1 -> LumiYellow
                                2 -> LumiViolet
                                3 -> LumiMint
                                else -> LumiCyan
                            }
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.setLifeHubSubTab(index) },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (isSelected) tabColor else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                },
                                icon = {
                                    Icon(
                                        imageVector = when (index) {
                                            0 -> Icons.Default.CalendarMonth
                                            1 -> Icons.Default.CheckCircleOutline
                                            2 -> Icons.Default.AutoAwesome
                                            3 -> Icons.Default.Timer
                                            else -> Icons.Default.CalendarMonth
                                        },
                                        contentDescription = title,
                                        tint = if (isSelected) tabColor else TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier.testTag("life_hub_subtab_$index")
                            )
                        }
                    }
                }
            }

            // Sub-Screen Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (uiState.lifeHubSubTab.coerceIn(0, subTabs.size - 1)) {
                    0 -> ScheduleSection(
                        events = events,
                        viewModel = viewModel,
                        onNavigateToChat = onNavigateToChat
                    )
                    1 -> TasksSection(
                        tasks = tasks,
                        viewModel = viewModel,
                        onNavigateToChat = onNavigateToChat
                    )
                    2 -> AutonomousGoalsScreen(
                        viewModel = viewModel,
                        onNavigateToChat = onNavigateToChat
                    )
                    3 -> AmbientSoundscapesScreen(
                        viewModel = viewModel
                    )
                    else -> ScheduleSection(
                        events = events,
                        viewModel = viewModel,
                        onNavigateToChat = onNavigateToChat
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 1: SCHEDULE & AGENDA
// -------------------------------------------------------------
@Composable
private fun ScheduleSection(
    events: List<CalendarEventEntity>,
    viewModel: LumiViewModel,
    onNavigateToChat: (String?) -> Unit
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
                com.example.ui.components.ProactiveDailyBriefingCard(
                    viewModel = viewModel,
                    onNavigateToChat = onNavigateToChat
                )
            }
            // Header Card with AI Planning Trigger
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentDate,
                                    color = LumiCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${events.size} Events Scheduled",
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Button(
                                onClick = { onNavigateToChat("Please help me optimize and plan my schedule for today.") },
                                colors = ButtonDefaults.buttonColors(containerColor = LumiCyan.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = LumiCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "AI Plan Day", color = LumiCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Event List
            if (events.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = LumiCyan.copy(alpha = 0.6f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Your Agenda is Clear",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap the + button below or ask Lumi in Chat to add your meetings and deadlines!",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(events) { event ->
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    val startTimeStr = timeFormat.format(Date(event.startTimeMillis))
                    val endTimeStr = timeFormat.format(Date(event.endTimeMillis))

                    val eventColor = when (event.colorHex.uppercase()) {
                        "#00E5FF" -> LumiCyan
                        "#FF4081" -> LumiPink
                        "#FFD700" -> LumiGold
                        "#00E676" -> LumiGreen
                        "#7C4DFF" -> LumiViolet
                        else -> LumiCyan
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 4.dp, height = 48.dp)
                                    .background(eventColor, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = event.title,
                                    color = TextPrimary,
                                    fontSize = 15.sp,
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
                                        fontSize = 12.sp
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
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteCalendarEvent(event.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Event",
                                    tint = TextSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
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
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("btn_add_event")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Event")
        }

        if (showAddEventDialog) {
            AddEventDialog(
                onDismiss = { showAddEventDialog = false },
                onAddEvent = { title, desc, startOffsetHours, durationHours, loc, color ->
                    val now = System.currentTimeMillis()
                    val start = now + (startOffsetHours * 3600000L)
                    val end = start + (durationHours * 3600000L)
                    viewModel.addCalendarEvent(
                        CalendarEventEntity(
                            title = title,
                            description = desc,
                            startTimeMillis = start,
                            endTimeMillis = end,
                            location = loc,
                            colorHex = color
                        )
                    )
                    showAddEventDialog = false
                }
            )
        }
    }
}

// -------------------------------------------------------------
// SECTION 2: ACTION TASKS & KANBAN
// -------------------------------------------------------------
@Composable
private fun TasksSection(
    tasks: List<TaskEntity>,
    viewModel: LumiViewModel,
    onNavigateToChat: (String?) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Work", "Personal", "Health", "Study", "Creative")

    val filteredTasks = tasks.filter {
        if (selectedCategory == "All") true else it.category.equals(selectedCategory, ignoreCase = true)
    }

    val completedCount = tasks.count { it.isCompleted }
    val totalCount = tasks.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Task Progress Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily Action Goals",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$completedCount of $totalCount completed (+15 XP each)",
                                    color = LumiYellow,
                                    fontSize = 12.sp
                                )
                            }
                            Surface(
                                color = LumiYellow.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    color = LumiYellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = LumiYellow,
                            trackColor = SurfaceHighlight
                        )
                    }
                }
            }

            // Category Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            color = if (isSelected) LumiYellow else SurfaceDarkVariant,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.clickable { selectedCategory = category }
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) ObsidianDark else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Task Items
            if (filteredTasks.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = LumiYellow.copy(alpha = 0.6f),
                                modifier = Modifier.size(42.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No tasks in this category",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add a task using the button below or ask Lumi to organize your priorities!",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks) { task ->
                    val priorityColor = when (task.priority.uppercase()) {
                        "HIGH" -> LumiCoral
                        "MEDIUM" -> LumiYellow
                        else -> LumiGreen
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (task.isCompleted) SurfaceDarkVariant.copy(alpha = 0.5f) else SurfaceDark
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { checked ->
                                    viewModel.toggleTask(task.id, checked)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LumiYellow,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = ObsidianDark
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = priorityColor.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = task.priority.uppercase(),
                                            color = priorityColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = task.category,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    if (task.estimatedMinutes > 0) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${task.estimatedMinutes}m",
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteTask(task) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Task",
                                    tint = TextSecondary.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = LumiYellow,
            contentColor = ObsidianDark,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .testTag("btn_add_task")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAddTask = { title, desc, cat, prio, est ->
                    viewModel.addTask(
                        title = title,
                        priority = prio,
                        category = cat,
                        estimatedMinutes = est,
                        notes = desc
                    )
                    showAddTaskDialog = false
                }
            )
        }
    }
}

// -------------------------------------------------------------
// SECTION 3: WELLNESS, MINDFULNESS & BIOMETRIC VAULT
// -------------------------------------------------------------
@Composable
private fun WellnessVaultSection(
    wellnessLogs: List<WellnessLogEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    uiState: com.example.ui.viewmodel.LumiUiState,
    viewModel: LumiViewModel,
    onNavigateToChat: (String?) -> Unit
) {
    var moodScore by remember { mutableFloatStateOf(8f) }
    var energyScore by remember { mutableFloatStateOf(8f) }
    var waterCount by remember { mutableIntStateOf(3) }
    var gratitudeText by remember { mutableStateOf("") }
    var isSubmittedToday by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Coherence Breathing Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setShowBreathing(true) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(LumiGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = null,
                            tint = LumiGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "4-7-8 Coherence Breathing",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Calm your vagus nerve with guided tactile pacing",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.setShowBreathing(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Start", color = ObsidianDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Daily Check-In Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Wellness Check-In",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = null, tint = LumiPink)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Mood Slider
                    Text(
                        text = "Mood Balance: ${moodScore.toInt()}/10 ${getMoodEmoji(moodScore.toInt())}",
                        color = LumiPink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = moodScore,
                        onValueChange = { moodScore = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = LumiPink,
                            activeTrackColor = LumiPink,
                            inactiveTrackColor = SurfaceHighlight
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Energy Slider
                    Text(
                        text = "Energy Level: ${energyScore.toInt()}/10 ⚡",
                        color = LumiYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = energyScore,
                        onValueChange = { energyScore = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = LumiYellow,
                            activeTrackColor = LumiYellow,
                            inactiveTrackColor = SurfaceHighlight
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hydration Stepper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, tint = LumiCyan)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Hydration: $waterCount / 8 cups",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { if (waterCount > 0) waterCount-- },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(SurfaceDarkVariant, CircleShape)
                            ) {
                                Text(text = "−", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { waterCount++ },
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(LumiCyan, CircleShape)
                            ) {
                                Text(text = "+", color = ObsidianDark, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gratitude TextField
                    OutlinedTextField(
                        value = gratitudeText,
                        onValueChange = { gratitudeText = it },
                        label = { Text("What are you grateful for today?") },
                        placeholder = { Text("A productive morning, good coffee...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiPink,
                            unfocusedBorderColor = SurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.logWellness(
                                moodScore = moodScore.toInt(),
                                moodLabel = getMoodLabel(moodScore.toInt()),
                                energyLevel = energyScore.toInt(),
                                hydrationCups = waterCount,
                                gratitude = gratitudeText.ifBlank { "Daily check-in completed" }
                            )
                            isSubmittedToday = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiPink),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isSubmittedToday) "✓ Saved & Synced with Lumi!" else "Save Wellness Check-In",
                            color = ObsidianDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Biometric Secured Memory Vault
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (uiState.isMemoryVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (uiState.isMemoryVaultUnlocked) LumiGreen else LumiViolet
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Biometric Memory Vault",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.isMemoryVaultUnlocked) {
                            TextButton(onClick = { viewModel.lockMemoryVault() }) {
                                Text(text = "Lock Vault", color = LumiPink, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!uiState.isMemoryVaultUnlocked) {
                        Text(
                            text = "Lumi securely stores your habits, preferences, and personal insights. Authenticate with biometrics to unlock.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.unlockMemoryVault() },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiViolet),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_unlock_vault")
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Unlock with Biometrics", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        if (memories.isEmpty()) {
                            Text(
                                text = "Vault unlocked. No memories logged yet. As you converse with Lumi, learned preferences will be securely archived here.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                memories.forEach { mem ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = mem.category.uppercase(),
                                                    color = LumiViolet,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${mem.sentiment} • Impact ${mem.emotionalImpact}/5",
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = mem.memoryText,
                                                color = TextPrimary,
                                                fontSize = 13.sp
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

// -------------------------------------------------------------
// DIALOGS & HELPERS
// -------------------------------------------------------------
@Composable
private fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (title: String, desc: String, startOffset: Int, duration: Int, location: String, color: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var startOffsetHours by remember { mutableIntStateOf(1) }
    var durationHours by remember { mutableIntStateOf(1) }
    var location by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#00E5FF") }

    val colors = listOf("#00E5FF", "#FF4081", "#FFD700", "#00E676", "#7C4DFF")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Schedule New Event", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiCyan,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Link (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiCyan,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Starts in: +${startOffsetHours}h", color = TextSecondary, fontSize = 12.sp)
                    Row {
                        IconButton(onClick = { if (startOffsetHours > 0) startOffsetHours-- }) {
                            Text(text = "−", color = TextPrimary, fontSize = 16.sp)
                        }
                        IconButton(onClick = { startOffsetHours++ }) {
                            Text(text = "+", color = LumiCyan, fontSize = 16.sp)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { hex ->
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(
                                    when (hex) {
                                        "#00E5FF" -> LumiCyan
                                        "#FF4081" -> LumiPink
                                        "#FFD700" -> LumiGold
                                        "#00E676" -> LumiGreen
                                        else -> LumiViolet
                                    },
                                    CircleShape
                                )
                                .clickable { selectedColor = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ObsidianDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddEvent(title, desc, startOffsetHours, durationHours, location, selectedColor)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LumiCyan)
            ) {
                Text("Schedule & Alarm", color = ObsidianDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAddTask: (title: String, desc: String, category: String, priority: String, estimated: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Work") }
    var priority by remember { mutableStateOf("Medium") }
    var estimatedMinutes by remember { mutableIntStateOf(25) }

    val categories = listOf("Work", "Personal", "Health", "Study", "Creative")
    val priorities = listOf("High", "Medium", "Low")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Add Action Task", color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Description") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiYellow,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Text(text = "Category", color = TextSecondary, fontSize = 11.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        Surface(
                            color = if (category == cat) LumiYellow else SurfaceDarkVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                color = if (category == cat) ObsidianDark else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Text(text = "Priority", color = TextSecondary, fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    priorities.forEach { prio ->
                        Surface(
                            color = if (priority == prio) LumiCoral else SurfaceDarkVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { priority = prio }
                        ) {
                            Text(
                                text = prio,
                                color = if (priority == prio) ObsidianDark else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAddTask(title, "", category, priority, estimatedMinutes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = LumiYellow)
            ) {
                Text("Add Task", color = ObsidianDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

private fun getMoodEmoji(score: Int): String {
    return when (score) {
        1, 2 -> "😔"
        3, 4 -> "😐"
        5, 6 -> "🙂"
        7, 8 -> "😊"
        else -> "✨"
    }
}

private fun getMoodLabel(score: Int): String {
    return when (score) {
        1, 2 -> "Drained"
        3, 4 -> "Meh"
        5, 6 -> "Calm"
        7, 8 -> "Great"
        else -> "Ecstatic"
    }
}
