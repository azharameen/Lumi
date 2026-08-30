package com.example.presentation.screens.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.domain.account.UserProfileData
import com.example.presentation.components.LumiDialog
import com.example.core.theme.*

@Composable
fun EditProfileDialog(
    currentProfile: UserProfileData,
    onDismiss: () -> Unit,
    onSave: (UserProfileData) -> Unit
) {
    var name by remember { mutableStateOf(currentProfile.userName) }
    var email by remember { mutableStateOf(currentProfile.userEmail) }
    var role by remember { mutableStateOf(currentProfile.roleOrTitle) }
    var goal by remember { mutableStateOf(currentProfile.primaryFocusGoal) }
    var focusHours by remember { mutableFloatStateOf(currentProfile.dailyFocusTargetHours) }
    var hydrationCups by remember { mutableIntStateOf(currentProfile.targetHydrationCups) }
    var steps by remember { mutableIntStateOf(currentProfile.targetDailySteps) }
    var wakeTime by remember { mutableStateOf(currentProfile.wakeUpTime) }
    var sleepTime by remember { mutableStateOf(currentProfile.sleepTime) }

    LumiDialog(
        onDismissRequest = onDismiss,
        title = "Edit User Profile",
        subtitle = "Personalize your identity and circadian rhythms",
        icon = Icons.Default.AccountCircle,
        accentColor = LumiMint,
        confirmButtonText = "Save Changes",
        onConfirm = {
            onSave(
                currentProfile.copy(
                    userName = name.ifBlank { "User" },
                    userEmail = email.ifBlank { "user@example.com" },
                    roleOrTitle = role.ifBlank { "Productivity Seeker" },
                    primaryFocusGoal = goal.ifBlank { "Daily Growth" },
                    dailyFocusTargetHours = focusHours,
                    targetHydrationCups = hydrationCups,
                    targetDailySteps = steps,
                    wakeUpTime = wakeTime,
                    sleepTime = sleepTime
                )
            )
        },
        onDismiss = onDismiss
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(id = R.string.text_your_name), color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiMint,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(id = R.string.text_email_address), color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiMint,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text(stringResource(id = R.string.text_role_occupation), color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiMint,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text(stringResource(id = R.string.text_primary_goal_mission), color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LumiMint,
                        unfocusedBorderColor = SurfaceHighlight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = { wakeTime = it },
                        label = { Text(stringResource(id = R.string.text_wake_time), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGold,
                            unfocusedBorderColor = SurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = sleepTime,
                        onValueChange = { sleepTime = it },
                        label = { Text(stringResource(id = R.string.text_sleep_time), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiViolet,
                            unfocusedBorderColor = SurfaceHighlight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
