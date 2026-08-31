package com.example.presentation.screens
import androidx.compose.ui.res.stringResource
import com.example.R


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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GoalMilestoneEntity
import com.example.data.local.entity.GoalPlanEntity
import com.example.core.theme.*
import com.example.core.theme.TextPrimary
import com.example.core.theme.TextSecondary
import com.example.core.theme.spacing


@Composable
fun AutonomousGoalsScreen(
    goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit,
                
) {
    val context = LocalContext.current
    
    var showCreateGoalDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = MaterialTheme.spacing.medium),
            contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                                        androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                    )
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        shape = CircleShape,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Hub,
                                                contentDescription = null,
                                                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = stringResource(R.string.text_autonomous_goal_swarms),
                                            color = TextPrimary,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = stringResource(R.string.text_multistep_ai_planner_tool_executor),
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = stringResource(R.string.text_define_any_objective_lumis_agent_swarm),
                                color = TextPrimary.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showCreateGoalDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("btn_decompose_goal")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(MaterialTheme.spacing.medium)
                                )
                                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                Text(
                                    text = stringResource(R.string.text_decompose_new_big_objective),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (goalPlans.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = stringResource(R.string.text_no_active_goal_swarms_yet),
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(R.string.text_try_planning_launch_mobile_product_run),
                                color = TextSecondary,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(goalPlans, key = { it.id }) { goal ->
                    GoalPlanItemCard(
                        goal = goal,
                        getMilestonesForGoal = getMilestonesForGoal,
                        onAction = onAction
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showCreateGoalDialog = true },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_goal")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.desc_new_goal_swarm)
            )
        }

        // Decompose Goal Dialog
        if (showCreateGoalDialog) {
            CreateGoalDecomposeDialog(
                onDismiss = { showCreateGoalDialog = false },
                onConfirm = { title, description, category, targetDate ->
                    onAction(com.example.presentation.viewmodel.LumiUiAction.DecomposeGoal(title, description, category, targetDate))
                    showCreateGoalDialog = false
                    Toast.makeText(context, "Lumi agent swarm decomposed your goal!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun GoalPlanItemCard(
    goal: GoalPlanEntity,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit
) {
    val milestones by getMilestonesForGoal(goal.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var isExpanded by remember { mutableStateOf(true) }
    val progress = if (goal.totalSteps > 0) goal.completedSteps.toFloat() / goal.totalSteps.toFloat() else 0f
    val isAllDone = goal.completedSteps >= goal.totalSteps && goal.totalSteps > 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = when (goal.category) {
                            "Engineering" -> androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            "Health" -> LumiGreen.copy(alpha = 0.2f)
                            "Learning" -> LumiGold.copy(alpha = 0.2f)
                            else -> androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(MaterialTheme.spacing.small)
                    ) {
                        Text(
                            text = goal.category,
                            color = when (goal.category) {
                                "Engineering" -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                                "Health" -> LumiGreen
                                "Learning" -> LumiGold
                                else -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    if (isAllDone) {
                        Surface(
                            color = LumiGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(MaterialTheme.spacing.small)
                        ) {
                            Text(
                                text = stringResource(R.string.text_completed),
                                color = LumiGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { onAction(com.example.presentation.viewmodel.LumiUiAction.DeleteGoal(goal.id)) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.desc_delete_goal),
                        tint = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.size(MaterialTheme.spacing.medium)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Text(
                text = goal.title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (goal.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                Text(
                    text = goal.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goal.completedSteps} of ${goal.totalSteps} Milestones",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = if (isAllDone) LumiGreen else androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isAllDone) LumiGreen else androidx.compose.material3.MaterialTheme.colorScheme.primary,
                trackColor = SurfaceHighlight
            )

            // Milestones list toggle
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = MaterialTheme.spacing.extraSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Milestones" else "Show All Milestones (${milestones.size})",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    milestones.forEach { milestone ->
                        MilestoneItemRow(
                            milestone = milestone,
                            goalId = goal.id,
                            onAction = onAction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestoneItemRow(
    milestone: GoalMilestoneEntity,
    goalId: Long,
    onAction: (com.example.presentation.viewmodel.LumiUiAction) -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = SurfaceDarkVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onAction(com.example.presentation.viewmodel.LumiUiAction.ToggleMilestone(milestone.id, goalId, !milestone.isCompleted)) },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (milestone.isCompleted) Icons.Default.CheckCircle else Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = if (milestone.isCompleted) LumiGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = milestone.stepTitle,
                        color = if (milestone.isCompleted) TextSecondary else TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (milestone.isCompleted) TextDecoration.LineThrough else null
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = milestone.stepDescription,
                    color = TextSecondary.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                if (milestone.executionOutput.isNotBlank()) {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = "⚡ ${milestone.executionOutput}",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!milestone.isCompleted && milestone.suggestedTool != "NONE") {
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        onAction(com.example.presentation.viewmodel.LumiUiAction.ExecuteMilestone(milestone.id, goalId))
                        Toast.makeText(context, "Executing ${milestone.suggestedTool} tool...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (milestone.suggestedTool) {
                            "CALENDAR" -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                            "DOC" -> LumiGold
                            "GITHUB" -> LumiMint
                            "SLACK" -> LumiPink
                            else -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(MaterialTheme.spacing.small),
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.extraSmall),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = when (milestone.suggestedTool) {
                            "CALENDAR" -> Icons.Default.CalendarMonth
                            "DOC" -> Icons.Default.Description
                            "GITHUB" -> Icons.Default.Code
                            "SLACK" -> Icons.Default.Forum
                            else -> Icons.Default.TaskAlt
                        },
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                    Text(
                        text = stringResource(R.string.text_autorun),
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateGoalDecomposeDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, category: String, targetDate: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Productivity") }
    var targetDate by remember { mutableStateOf("") }

    val categories = listOf("Productivity", "Engineering", "Health", "Learning", "Creative")

    com.example.presentation.components.LumiDialog(
        onDismissRequest = onDismiss,
        title = "Decompose Goal Plan",
        subtitle = "Lumi's agentic swarm will construct phases and assign tools",
        icon = Icons.Default.AutoAwesome,
        accentColor = LumiCyan,
        confirmButtonText = "Decompose with Lumi",
        isConfirmEnabled = title.isNotBlank(),
        onConfirm = {
            if (title.isNotBlank()) {
                onConfirm(title, description, selectedCategory, targetDate)
            }
        },
        onDismiss = onDismiss
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(id = R.string.text_goal_title_e_g_launch_mobile_a), color = TextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiCyan,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("input_goal_title")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(id = R.string.text_key_outcomes_notes_optional), color = TextSecondary) },
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LumiCyan,
                    unfocusedBorderColor = SurfaceHighlight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.text_category_domain),
                color = TextSecondary,
                style = MaterialTheme.typography.labelSmall
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(3).forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) LumiCyan else SurfaceDarkVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) ObsidianDark else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                        )
                    }
                }
            }
        }
    }
}
