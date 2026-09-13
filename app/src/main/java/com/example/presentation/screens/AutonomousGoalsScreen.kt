package com.example.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.theme.*
import com.example.core.utils.LumiHaptics
import com.example.core.utils.rememberLumiHaptics
import com.example.domain.model.GoalMilestone
import com.example.domain.model.GoalPlan
import com.example.presentation.screens.goals.*
import com.example.presentation.viewmodel.LumiUiAction
import kotlinx.coroutines.flow.Flow

@Composable
fun AutonomousGoalsScreen(
    haptics: LumiHaptics = rememberLumiHaptics(),
    goalPlans: List<GoalPlan>,
    getMilestonesForGoal: (Long) -> Flow<List<GoalMilestone>>,
    onAction: (LumiUiAction) -> Unit
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
                AutonomousGoalsHeaderCard(
                    onDecomposeClicked = {
                        haptics.performSuccess()
                        showCreateGoalDialog = true
                    }
                )
            }

            if (goalPlans.isEmpty()) {
                item {
                    AutonomousGoalsEmptyState()
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
            onClick = {
                haptics.performSuccess()
                showCreateGoalDialog = true
            },
            containerColor = MaterialTheme.colorScheme.primary,
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
                    onAction(LumiUiAction.DecomposeGoal(title, description, category, targetDate))
                    showCreateGoalDialog = false
                    Toast.makeText(context, "Lumi agent swarm decomposed your goal!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
