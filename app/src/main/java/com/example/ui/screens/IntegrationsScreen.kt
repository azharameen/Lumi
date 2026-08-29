package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.connectors.ConnectorManager
import com.example.domain.connectors.IntegrationProviderType
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiGold
import com.example.ui.theme.LumiGreen
import com.example.ui.theme.LumiMint
import com.example.ui.theme.LumiPink
import com.example.ui.theme.LumiViolet
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.LumiViewModel

@Composable
fun IntegrationsScreen(
    viewModel: LumiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val connectorManager = remember { ConnectorManager(context) }

    val isGoogleConnected by connectorManager.googleConnected.collectAsState()
    val googleEmail by connectorManager.googleAccount.collectAsState()

    val isGithubConnected by connectorManager.githubConnected.collectAsState()
    val githubUser by connectorManager.githubUser.collectAsState()
    val githubToken by connectorManager.githubToken.collectAsState()

    val isSlackConnected by connectorManager.slackConnected.collectAsState()
    val slackChannel by connectorManager.slackChannel.collectAsState()
    val slackWebhook by connectorManager.slackWebhook.collectAsState()

    var showGithubDialog by remember { mutableStateOf(false) }
    var showSlackDialog by remember { mutableStateOf(false) }

    var inputGithubUser by remember { mutableStateOf(githubUser) }
    var inputGithubToken by remember { mutableStateOf(githubToken) }

    var inputSlackChannel by remember { mutableStateOf(slackChannel) }
    var inputSlackWebhook by remember { mutableStateOf(slackWebhook) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = "Integrations",
                    tint = LumiCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Providers, Connectors & Agent Tools",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Connect Google Workspace, GitHub & Slack to empower Lumi",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // 2. Active Integrations Status Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AGENT TOOL PERMISSION MATRIX",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ConnectorMiniBadge(
                            name = "Google",
                            isConnected = isGoogleConnected,
                            color = LumiCyan,
                            modifier = Modifier.weight(1f)
                        )
                        ConnectorMiniBadge(
                            name = "GitHub",
                            isConnected = isGithubConnected,
                            color = LumiViolet,
                            modifier = Modifier.weight(1f)
                        )
                        ConnectorMiniBadge(
                            name = "Slack",
                            isConnected = isSlackConnected,
                            color = LumiGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. Google Workspace Card (OAuth)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LumiCyan.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Google",
                                    tint = LumiCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Google Workspace",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "OAuth 2.0 Client • Calendar, Tasks, Gmail, Docs, Sheets, Slides, Keep",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = isGoogleConnected,
                            onCheckedChange = { checked ->
                                connectorManager.setGoogleConnection(checked, "azharameen52@gmail.com")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LumiCyan,
                                checkedTrackColor = LumiCyan.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("toggle_google_oauth")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isGoogleConnected) {
                        Surface(
                            color = LumiCyan.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = LumiCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Signed in as $googleEmail",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Scopes active: Calendar, Tasks, Gmail, Docs, Sheets, Slides & Drive file sync",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                connectorManager.setGoogleConnection(true, "azharameen52@gmail.com")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connect_google_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sign In & Grant Google Permissions",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Enabled Agent Tools: add_calendar_event, get_daily_schedule, create_task, google_send_email, google_create_doc, google_append_sheet_row, google_create_slides, google_sync_keep_note",
                        color = TextTertiary,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 4. GitHub Connector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LumiViolet.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = "GitHub",
                                    tint = LumiViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GitHub Developer Hub",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Repositories, Projects, Issue Tracker, Pull Requests & Summaries",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = isGithubConnected,
                            onCheckedChange = { checked ->
                                if (checked && githubUser.isEmpty()) {
                                    showGithubDialog = true
                                } else {
                                    connectorManager.setGithubConnection(checked, githubUser, githubToken)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LumiViolet,
                                checkedTrackColor = LumiViolet.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("toggle_github_connector")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isGithubConnected) {
                        Surface(
                            color = LumiViolet.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LumiViolet,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Active account: @${if (githubUser.isNotBlank()) githubUser else "azharameen"}",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                TextButton(
                                    onClick = { showGithubDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("Configure", color = LumiViolet, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { showGithubDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiViolet),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connect_github_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Connect GitHub Account / Token",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Enabled Agent Tools: github_create_issue, github_summarize_repo",
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 5. Slack Connector Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(LumiGreen.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Forum,
                                    contentDescription = "Slack",
                                    tint = LumiGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Slack Workplace & Channels",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Channel Broadcasts, Focus Mode DND & Sync",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Switch(
                            checked = isSlackConnected,
                            onCheckedChange = { checked ->
                                if (checked && slackWebhook.isEmpty()) {
                                    showSlackDialog = true
                                } else {
                                    connectorManager.setSlackConnection(checked, slackChannel, slackWebhook)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = LumiGreen,
                                checkedTrackColor = LumiGreen.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.testTag("toggle_slack_connector")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSlackConnected) {
                        Surface(
                            color = LumiGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = LumiGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Target channel: $slackChannel",
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                TextButton(
                                    onClick = { showSlackDialog = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("Configure", color = LumiGreen, fontSize = 11.sp)
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { showSlackDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("connect_slack_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Connect Slack Workspace",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Enabled Agent Tools: slack_post_message, slack_set_focus_status",
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    // GitHub Config Modal
    if (showGithubDialog) {
        AlertDialog(
            onDismissRequest = { showGithubDialog = false },
            title = { Text("Configure GitHub Connector", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Provide your GitHub username and personal access token (or fine-grained repo token).",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = inputGithubUser,
                        onValueChange = { inputGithubUser = it },
                        label = { Text("Username") },
                        placeholder = { Text("e.g. azharameen") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiViolet,
                            unfocusedBorderColor = SurfaceDarkVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputGithubToken,
                        onValueChange = { inputGithubToken = it },
                        label = { Text("Personal Access Token (ghp_...)") },
                        placeholder = { Text("ghp_****************") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiViolet,
                            unfocusedBorderColor = SurfaceDarkVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = if (inputGithubUser.isBlank()) "azharameen" else inputGithubUser
                        connectorManager.setGithubConnection(true, user, inputGithubToken)
                        showGithubDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumiViolet)
                ) {
                    Text("Save & Connect", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showGithubDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Slack Config Modal
    if (showSlackDialog) {
        AlertDialog(
            onDismissRequest = { showSlackDialog = false },
            title = { Text("Configure Slack Connector", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Configure the default Slack channel and incoming webhook URL for broadcasting updates.",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = inputSlackChannel,
                        onValueChange = { inputSlackChannel = it },
                        label = { Text("Channel") },
                        placeholder = { Text("#general or #focus") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGreen,
                            unfocusedBorderColor = SurfaceDarkVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputSlackWebhook,
                        onValueChange = { inputSlackWebhook = it },
                        label = { Text("Incoming Webhook URL") },
                        placeholder = { Text("https://hooks.slack.com/services/...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGreen,
                            unfocusedBorderColor = SurfaceDarkVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ch = if (inputSlackChannel.isBlank()) "#general" else inputSlackChannel
                        connectorManager.setSlackConnection(true, ch, inputSlackWebhook)
                        showSlackDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumiGreen)
                ) {
                    Text("Save & Connect", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSlackDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun ConnectorMiniBadge(
    name: String,
    isConnected: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isConnected) color.copy(alpha = 0.15f) else SurfaceDarkVariant,
        shape = RoundedCornerShape(10.dp),
        border = if (isConnected) androidx.compose.foundation.BorderStroke(1.dp, color) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) color else TextTertiary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = name,
                color = if (isConnected) TextPrimary else TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
