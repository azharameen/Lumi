package com.example.presentation.screens.goals

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.theme.*
import com.example.domain.model.GoalMilestone
import com.example.domain.model.GoalPlan
import com.example.presentation.viewmodel.LumiUiAction
import kotlinx.coroutines.flow.Flow

@Composable
fun GoalPlanItemCard(
    goal: GoalPlan,
    getMilestonesForGoal: (Long) -> Flow<List<GoalMilestone>>,
    onAction: (LumiUiAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val milestones by getMilestonesForGoal(goal.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var isExpanded by remember { mutableStateOf(true) }
    val progress = if (goal.totalSteps > 0) goal.completedSteps.toFloat() / goal.totalSteps.toFloat() else 0f
    val isAllDone = goal.completedSteps >= goal.totalSteps && goal.totalSteps > 0

    Card(
        modifier = modifier.fillMaxWidth(),
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
                            "Engineering" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            "Health" -> LumiGreen.copy(alpha = 0.2f)
                            "Learning" -> LumiGold.copy(alpha = 0.2f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        },
                        shape = RoundedCornerShape(MaterialTheme.spacing.small)
                    ) {
                        Text(
                            text = goal.category,
                            color = when (goal.category) {
                                "Engineering" -> MaterialTheme.colorScheme.primary
                                "Health" -> LumiGreen
                                "Learning" -> LumiGold
                                else -> MaterialTheme.colorScheme.primary
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
                    onClick = { onAction(LumiUiAction.DeleteGoal(goal.id)) },
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
                    color = if (isAllDone) LumiGreen else MaterialTheme.colorScheme.primary,
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
                color = if (isAllDone) LumiGreen else MaterialTheme.colorScheme.primary,
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
                    color = MaterialTheme.colorScheme.primary,
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
    milestone: GoalMilestone,
    goalId: Long,
    onAction: (LumiUiAction) -> Unit
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
                onClick = { onAction(LumiUiAction.ToggleMilestone(milestone.id, goalId, !milestone.isCompleted)) },
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
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (!milestone.isCompleted && milestone.suggestedTool != "NONE") {
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        onAction(LumiUiAction.ExecuteMilestone(milestone.id, goalId))
                        Toast.makeText(context, "Executing ${milestone.suggestedTool} tool...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (milestone.suggestedTool) {
                            "CALENDAR" -> MaterialTheme.colorScheme.primary
                            "DOC" -> LumiGold
                            "GITHUB" -> LumiMint
                            "SLACK" -> LumiPink
                            else -> MaterialTheme.colorScheme.primary
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
