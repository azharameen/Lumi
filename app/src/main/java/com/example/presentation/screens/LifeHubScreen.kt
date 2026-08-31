package com.example.presentation.screens
import com.example.R

import androidx.compose.ui.res.stringResource

import com.example.presentation.screens.lifehub.*
import com.example.presentation.components.*
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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.core.theme.LumiCoral

import com.example.core.theme.LumiGold
import com.example.core.theme.LumiGreen
import com.example.core.theme.LumiMint
import com.example.core.theme.LumiPink

import com.example.core.theme.LumiYellow
import com.example.core.theme.ObsidianDark
import com.example.core.theme.SurfaceDark
import com.example.core.theme.SurfaceDarkVariant
import com.example.core.theme.SurfaceHighlight
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary

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
    uiState: com.example.presentation.viewmodel.LumiUiState,
    tasks: List<com.example.data.local.entity.TaskEntity>,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    wellnessLogs: List<com.example.data.local.entity.WellnessLogEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    dailyBriefing: com.example.domain.briefing.DailyBriefing?,
    goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    soundState: com.example.data.device.SoundscapeState,
    onAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    
    
    
    

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.desc_back),
                                    tint = TextPrimary
                                )
                            }
                            Text(
                                text = stringResource(R.string.text_productivity_hub),
                                color = TextPrimary,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                                    text = stringResource(R.string.text_lumi_sync),
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
                                    0 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                    1 -> LumiYellow
                                    2 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                    3 -> LumiMint
                                    else -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                },
                                height = 3.dp
                            )
                        }
                    ) {
                        subTabs.forEachIndexed { index, title ->
                            val isSelected = uiState.lifeHubSubTab.coerceIn(0, subTabs.size - 1) == index
                            val tabColor = when (index) {
                                0 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                1 -> LumiYellow
                                2 -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                3 -> LumiMint
                                else -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                            }
                            Tab(
                                selected = isSelected,
                                onClick = { onAction(com.example.presentation.viewmodel.LumiUiAction.SetLifeHubSubTab(index)) },
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
                        dailyBriefing = dailyBriefing,
                        onAction = onAction
                    )
                    1 -> TasksSection(
                        tasks = tasks,
                        onAction = onAction
                    )
                    2 -> AutonomousGoalsScreen(
                        goalPlans = goalPlans,
                        getMilestonesForGoal = getMilestonesForGoal,
                        onAction = onAction
                    )
                    3 -> AmbientSoundscapesScreen(
                        soundState = soundState,
                        onAction = onAction
                    )
                    else -> ScheduleSection(
                        events = events,
                        dailyBriefing = dailyBriefing,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SECTION 1: SCHEDULE & AGENDA
// -------------------------------------------------------------


// -------------------------------------------------------------
// SECTION 2: ACTION TASKS & KANBAN
// -------------------------------------------------------------


// -------------------------------------------------------------
// SECTION 3: WELLNESS, MINDFULNESS & BIOMETRIC VAULT
// -------------------------------------------------------------


// -------------------------------------------------------------
// DIALOGS & HELPERS
// -------------------------------------------------------------







