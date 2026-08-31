package com.example.presentation.screens.account
import androidx.compose.ui.res.stringResource
import com.example.R


import com.example.presentation.components.*

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.remote.HardwareAccelerator
import com.example.data.remote.LocalLlmModelSpec
import com.example.data.remote.ModelDownloadStatus
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.account.LumiPersonaTone
import com.example.domain.account.UserFactItem
import com.example.domain.account.UserProfileData
import com.example.domain.connectors.ConnectorManager
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
import com.example.core.theme.TextTertiary


@Composable
fun ConnectorsControlSection(
    
) {
    val context = LocalContext.current
    val connectorManager = remember { ConnectorManager(context) }

    val isGoogleConnected by connectorManager.googleConnected.collectAsStateWithLifecycle()
    val googleEmail by connectorManager.googleAccount.collectAsStateWithLifecycle()

    val isGithubConnected by connectorManager.githubConnected.collectAsStateWithLifecycle()
    val githubUser by connectorManager.githubUser.collectAsStateWithLifecycle()
    val githubToken by connectorManager.githubToken.collectAsStateWithLifecycle()

    val isSlackConnected by connectorManager.slackConnected.collectAsStateWithLifecycle()
    val slackChannel by connectorManager.slackChannel.collectAsStateWithLifecycle()
    val slackWebhook by connectorManager.slackWebhook.collectAsStateWithLifecycle()

    var showGithubDialog by remember { mutableStateOf(false) }
    var showSlackDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.text_connected_ecosystem_tools),
                color = LumiGold,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.text_connect_lumi_to_your_digital_workspace),
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Google Workspace Card
        item {
            ConnectorCard(
                title = "Google Workspace",
                subtitle = "Google Calendar, Gmail & Tasks Sync",
                accountText = if (isGoogleConnected) googleEmail else "Not connected",
                isConnected = isGoogleConnected,
                accentColor = LumiGreen,
                icon = Icons.Default.Email,
                onToggle = { enable ->
                    connectorManager.setGoogleConnection(enable, if (enable) "azharameen52@gmail.com" else "")
                    Toast.makeText(context, if (enable) "Google Workspace Connected" else "Google Workspace Disconnected", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // GitHub Connector Card
        item {
            ConnectorCard(
                title = "GitHub Developer",
                subtitle = "Repository issues, PR triage, and dispatch",
                accountText = if (isGithubConnected) "@$githubUser" else "Not configured",
                isConnected = isGithubConnected,
                accentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                icon = Icons.Default.Code,
                onToggle = { enable ->
                    if (enable) {
                        showGithubDialog = true
                    } else {
                        connectorManager.setGithubConnection(false, "", "")
                    }
                },
                onConfigure = { showGithubDialog = true }
            )
        }

        // Slack / Discord Connector Card
        item {
            ConnectorCard(
                title = "Slack & Discord Webhooks",
                subtitle = "Post daily briefings, goal alerts & milestones",
                accountText = if (isSlackConnected) slackChannel else "Not configured",
                isConnected = isSlackConnected,
                accentColor = LumiGold,
                icon = Icons.Default.Hub,
                onToggle = { enable ->
                    if (enable) {
                        showSlackDialog = true
                    } else {
                        connectorManager.setSlackConnection(false, "", "")
                    }
                },
                onConfigure = { showSlackDialog = true }
            )
        }

        // Health & Vitals Connector
        item {
            ConnectorCard(
                title = "Health Connect & Google Fit",
                subtitle = "Step telemetry, sleep logs & resting heart rate",
                accountText = "Active telemetry sync",
                isConnected = true,
                accentColor = LumiPink,
                icon = Icons.Default.FitnessCenter,
                onToggle = { }
            )
        }

        // System Hardware Sensors
        item {
            ConnectorCard(
                title = "Android Hardware Sensors",
                subtitle = "Battery state, Location context & Accelerometer",
                accountText = "Live sensor streaming enabled",
                isConnected = true,
                accentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                icon = Icons.Default.Bolt,
                onToggle = { }
            )
        }
    }

    // GitHub Config Dialog
    if (showGithubDialog) {
        var userText by remember { mutableStateOf(githubUser) }
        var tokenText by remember { mutableStateOf(githubToken) }

        AlertDialog(
            onDismissRequest = { showGithubDialog = false },
            title = { Text(stringResource(id = R.string.text_configure_github_token), color = androidx.compose.material3.MaterialTheme.colorScheme.primary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = userText,
                        onValueChange = { userText = it },
                        label = { Text(stringResource(id = R.string.text_github_username)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tokenText,
                        onValueChange = { tokenText = it },
                        label = { Text(stringResource(id = R.string.text_personal_access_token_pat)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        connectorManager.setGithubConnection(userText.isNotBlank(), userText, tokenText)
                        showGithubDialog = false
                        Toast.makeText(context, "GitHub connection configured", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(id = R.string.text_save_connect), color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGithubDialog = false }) {
                    Text(stringResource(id = R.string.text_cancel), color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Slack Config Dialog
    if (showSlackDialog) {
        var channelText by remember { mutableStateOf(slackChannel) }
        var webhookText by remember { mutableStateOf(slackWebhook) }

        AlertDialog(
            onDismissRequest = { showSlackDialog = false },
            title = { Text(stringResource(id = R.string.text_configure_slack_webhook), color = LumiGold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = channelText,
                        onValueChange = { channelText = it },
                        label = { Text(stringResource(id = R.string.text_channel_e_g_daily_briefings)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGold,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = webhookText,
                        onValueChange = { webhookText = it },
                        label = { Text(stringResource(id = R.string.text_webhook_url)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGold,
                            unfocusedBorderColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        connectorManager.setSlackConnection(webhookText.isNotBlank() || channelText.isNotBlank(), channelText, webhookText)
                        showSlackDialog = false
                        Toast.makeText(context, "Slack connection saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumiGold)
                ) {
                    Text(stringResource(id = R.string.text_save_connect), color = ObsidianDark, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlackDialog = false }) {
                    Text(stringResource(id = R.string.text_cancel), color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}
