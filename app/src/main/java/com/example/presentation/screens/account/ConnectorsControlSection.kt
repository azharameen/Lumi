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
import com.example.data.repository.ConnectorRepositoryImpl
import com.example.domain.connectors.ConnectorRepository
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
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing


import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.framework.LumiFirebaseMessagingService
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectorsControlSection(
    
) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val connectorManager = remember { ConnectorRepositoryImpl(context) }

    val remoteConfigManager = remember {
        try {
            GlobalContext.get().get<LumiRemoteConfigManager>()
        } catch (_: Exception) {
            LumiRemoteConfigManager()
        }
    }
    val analyticsManager = remember {
        try {
            GlobalContext.get().get<LumiAnalyticsManager>()
        } catch (_: Exception) {
            LumiAnalyticsManager(context)
        }
    }
    val crashlyticsManager = remember {
        try {
            GlobalContext.get().get<LumiCrashlyticsManager>()
        } catch (_: Exception) {
            LumiCrashlyticsManager()
        }
    }

    val remoteConfig by remoteConfigManager.config.collectAsStateWithLifecycle()
    val isFetchingRc by remoteConfigManager.isFetching.collectAsStateWithLifecycle()
    val rcStatus by remoteConfigManager.lastStatus.collectAsStateWithLifecycle()

    var showRcDetails by remember { mutableStateOf(false) }

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
            .padding(horizontal = MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 90.dp),
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

        // Firebase Cloud Platform Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, LumiMint.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .testTag("card_connector_firebase")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(LumiMint.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = "Firebase",
                                    tint = LumiMint,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Column {
                                Text(
                                    text = "Firebase Cloud Platform",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Project: studio-8325749739-eefac",
                                    color = LumiMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Surface(
                            color = LumiGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(LumiGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ACTIVE",
                                    color = LumiGreen,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feature Pill Grid
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "🔔 Proactive FCM Push",
                            "⚙️ Remote Config",
                            "🛡️ Crashlytics Logs",
                            "📊 Companion Analytics",
                            "⚡ Performance Tracing"
                        ).forEach { tag ->
                            Surface(
                                color = SurfaceDarkVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, SurfaceHighlight)
                            ) {
                                Text(
                                    text = tag,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceHighlight, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Remote Config Status & Inspection Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showRcDetails = !showRcDetails }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Live Remote Config Parameters",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isFetchingRc) {
                                    androidx.compose.material3.CircularProgressIndicator(
                                        modifier = Modifier.size(12.dp),
                                        strokeWidth = 1.5.dp,
                                        color = LumiMint
                                    )
                                }
                            }
                            Text(
                                text = "Status: $rcStatus",
                                color = LumiMint.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = if (showRcDetails) "Hide ▲" else "Inspect ▼",
                            color = LumiGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    AnimatedVisibility(visible = showRcDetails) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(SurfaceDarkVariant, RoundedCornerShape(8.dp))
                                .border(0.5.dp, SurfaceHighlight, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Welcome Greeting:", color = TextTertiary, fontSize = 11.sp)
                                Text(remoteConfig.welcomeGreeting.take(28) + if (remoteConfig.welcomeGreeting.length > 28) "..." else "", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tip of the Day:", color = TextTertiary, fontSize = 11.sp)
                                Text(remoteConfig.companionTipOfTheDay.take(28) + if (remoteConfig.companionTipOfTheDay.length > 28) "..." else "", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("AI Creativity Temp:", color = TextTertiary, fontSize = 11.sp)
                                Text("${remoteConfig.aiCreativityTemperature}", color = LumiGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Proactive Nudge Interval:", color = TextTertiary, fontSize = 11.sp)
                                Text("${remoteConfig.proactiveNudgeIntervalHours} hours", color = LumiGold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Seasonal Theme:", color = TextTertiary, fontSize = 11.sp)
                                Text(if (remoteConfig.seasonalThemeEnabled) remoteConfig.seasonalThemeName else "Default Theme", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val result = remoteConfigManager.forceRefreshConfig()
                                    result.onSuccess {
                                        analyticsManager.logRemoteConfigSync("success")
                                        Toast.makeText(context, "Remote Config Synced: ${it.welcomeGreeting.take(24)}...", Toast.LENGTH_SHORT).show()
                                    }.onFailure { err ->
                                        analyticsManager.logRemoteConfigSync("failed")
                                        Toast.makeText(context, "Sync Failed: ${err.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_sync_remote_config"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LumiMint),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LumiMint.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync Config", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                LumiFirebaseMessagingService.sendLocalTestNotification(
                                    context = context,
                                    title = "✨ Lumi: Mindful Alert",
                                    body = "Proactive FCM push received! Your companion is in sync with your schedule.",
                                    targetTab = 1,
                                    alertType = "manual_test_alert"
                                )
                                analyticsManager.logScreenView("FCM_Test_Notification_Triggered")
                                crashlyticsManager.log("Triggered local test companion notification from Connectors UI")
                                Toast.makeText(context, "Proactive alert sent to notification shade", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_test_fcm_alert"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LumiMint, contentColor = ObsidianDark),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test FCM Push", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
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
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
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
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)) {
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
