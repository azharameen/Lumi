package com.example.presentation.screens.lifehub

import com.example.presentation.components.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TaskEntity
import com.example.core.theme.*
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing

@Composable
fun TasksSection(
    tasks: List<TaskEntity>,
    onAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit,
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
                .padding(horizontal = MaterialTheme.spacing.medium),
            contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Task Progress Summary Card
            item {
                LumiCard(
                    borderColor = LumiGold.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.text_daily_action_goals),
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount of $totalCount completed (+15 XP each)",
                                color = LumiGold,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        LumiBadge(
                            text = "${(progress * 100).toInt()}%",
                            accentColor = LumiGold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LumiProgressBar(
                        progress = progress,
                        barColor = LumiGold,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            color = if (isSelected) LumiGold else SurfaceDarkVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { selectedCategory = category }
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) ObsidianDark else TextSecondary,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = MaterialTheme.spacing.small)
                            )
                        }
                    }
                }
            }

            // Task Items or Empty State
            if (filteredTasks.isEmpty()) {
                item {
                    LumiEmptyState(
                        title = "No Tasks in $selectedCategory",
                        description = "Add a task using the button below or ask Lumi in Chat to organize your daily priorities!",
                        icon = Icons.Default.CheckCircleOutline,
                        accentColor = LumiGold,
                        actionButtonText = "Add Task",
                        onActionClick = { showAddTaskDialog = true }
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    val priorityColor = when (task.priority.uppercase()) {
                        "HIGH" -> LumiCoral
                        "MEDIUM" -> LumiGold
                        else -> LumiGreen
                    }

                    LumiCard(
                        borderColor = if (task.isCompleted) SurfaceHighlight.copy(alpha = 0.3f) else priorityColor.copy(alpha = 0.35f),
                        backgroundColor = if (task.isCompleted) SurfaceDarkVariant.copy(alpha = 0.4f) else SurfaceGlass,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = task.isCompleted,
                                onCheckedChange = { checked ->
                                    onAction(com.example.presentation.viewmodel.LumiUiAction.ToggleTask(task.id, checked))
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LumiGold,
                                    uncheckedColor = TextSecondary,
                                    checkmarkColor = ObsidianDark
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = if (task.isCompleted) TextSecondary else TextPrimary,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Bold,
                                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LumiBadge(
                                        text = task.priority.uppercase(),
                                        accentColor = priorityColor
                                    )
                                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                    Text(
                                        text = task.category,
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (task.estimatedMinutes > 0) {
                                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${task.estimatedMinutes}m",
                                            color = TextSecondary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onAction(com.example.presentation.viewmodel.LumiUiAction.DeleteTask(task)) },
                                modifier = Modifier.size(MaterialTheme.spacing.extraLarge)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.desc_delete_task),
                                    tint = TextTertiary,
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
            containerColor = LumiGold,
            contentColor = ObsidianDark,
            shape = RoundedCornerShape(MaterialTheme.spacing.medium),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = MaterialTheme.spacing.large, end = 20.dp)
                .testTag("btn_add_task")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(id = R.string.desc_add_task))
        }

        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAddTask = { title, desc, cat, prio, est ->
                    onAction(com.example.presentation.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc))
                    showAddTaskDialog = false
                }
            )
        }
    }
}
