package com.example.presentation.screens.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.theme.*
import com.example.data.firebase.LumiAnalyticsManager
import com.example.data.firebase.LumiCrashlyticsManager
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.data.repository.ConnectorRepositoryImpl
import com.example.domain.connectors.ConnectorRepository
import com.example.domain.tools.*
import com.example.framework.LumiFirebaseMessagingService
import com.example.presentation.components.ConnectorCard
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolsAndConnectorsSection() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val connectorManager: ConnectorRepository = remember {
        try {
            GlobalContext.get().get<ConnectorRepository>()
        } catch (_: Exception) {
            ConnectorRepositoryImpl(context)
        }
    }

    val remoteConfigManager = remember {
        try {
            GlobalContext.get().getOrNull<LumiRemoteConfigManager>() ?: LumiRemoteConfigManager()
        } catch (_: Exception) {
            LumiRemoteConfigManager()
        }
    }
    val analyticsManager = remember {
        try {
            GlobalContext.get().getOrNull<LumiAnalyticsManager>() ?: LumiAnalyticsManager(context)
        } catch (_: Exception) {
            LumiAnalyticsManager(context)
        }
    }
    val crashlyticsManager = remember {
        try {
            GlobalContext.get().getOrNull<LumiCrashlyticsManager>() ?: LumiCrashlyticsManager()
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

    // Dynamically retrieve all registered tools from the authoritative ToolRegistry
    val toolRegistry = remember { ToolRegistry.getInstance() }
    val registeredTools = remember { toolRegistry.getAllTools().sortedBy { it.displayName } }

    var selectedSubTab by remember { mutableIntStateOf(0) } // 0 = Tools Catalog, 1 = Connectors
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<ToolCategory?>(null) }
    var expandedToolId by remember { mutableStateOf<String?>(null) }

    val filteredTools = remember(registeredTools, searchQuery, selectedCategoryFilter) {
        registeredTools.filter { tool ->
            val matchesCategory = selectedCategoryFilter == null || tool.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                tool.displayName.contains(searchQuery, ignoreCase = true) ||
                tool.id.contains(searchQuery, ignoreCase = true) ||
                tool.description.contains(searchQuery, ignoreCase = true) ||
                tool.category.name.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Tools & Connectors",
                            color = LumiGold,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Live detected capabilities from ToolRegistry (${registeredTools.size} Tools • 6 Connectors)",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Surface(
                        color = LumiMint.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, LumiMint.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(LumiMint)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SYSTEM DETECTED",
                                color = LumiMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-tab switcher: Tools vs Connectors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceDarkVariant)
                        .padding(4.dp)
                ) {
                    val isTools = selectedSubTab == 0
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isTools) LumiMint else Color.Transparent)
                            .clickable { selectedSubTab = 0 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = if (isTools) ObsidianDark else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tools Catalog (${registeredTools.size})",
                                color = if (isTools) ObsidianDark else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isTools) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    val isConnectors = selectedSubTab == 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isConnectors) LumiGold else Color.Transparent)
                            .clickable { selectedSubTab = 1 }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = if (isConnectors) ObsidianDark else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connectors & Cloud (6)",
                                color = if (isConnectors) ObsidianDark else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isConnectors) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        if (selectedSubTab == 0) {
            // === SUB-TAB 0: DYNAMIC TOOLS CATALOG FROM TOOLREGISTRY ===
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        placeholder = { Text("Search tools by name, ID, or description...", fontSize = 12.sp, color = TextTertiary) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextTertiary, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = LumiMint,
                            unfocusedBorderColor = SurfaceDarkVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )

                    // Category Filter Chips
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All (${registeredTools.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LumiMint,
                                selectedLabelColor = ObsidianDark,
                                containerColor = SurfaceDarkVariant,
                                labelColor = TextSecondary
                            )
                        )
                        ToolCategory.values().forEach { cat ->
                            val count = registeredTools.count { it.category == cat }
                            if (count > 0) {
                                FilterChip(
                                    selected = selectedCategoryFilter == cat,
                                    onClick = {
                                        selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                                    },
                                    label = { Text("${cat.name} ($count)", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LumiMint,
                                        selectedLabelColor = ObsidianDark,
                                        containerColor = SurfaceDarkVariant,
                                        labelColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (filteredTools.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tools matching '$searchQuery'",
                            color = TextTertiary,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredTools, key = { it.id }) { tool ->
                    val isExpanded = expandedToolId == tool.id
                    val categoryColor = when (tool.category) {
                        ToolCategory.SYSTEM -> LumiMint
                        ToolCategory.CALENDAR -> LumiGreen
                        ToolCategory.COMMUNICATION -> LumiGold
                        ToolCategory.HEALTH -> LumiPink
                        ToolCategory.CONNECTORS -> LumiYellow
                        ToolCategory.UTILITY -> androidx.compose.material3.MaterialTheme.colorScheme.primary
                        ToolCategory.IOT -> LumiCyan
                    }
                    val categoryIcon = when (tool.category) {
                        ToolCategory.SYSTEM -> Icons.Default.SettingsSuggest
                        ToolCategory.CALENDAR -> Icons.Default.Schedule
                        ToolCategory.COMMUNICATION -> Icons.Default.Email
                        ToolCategory.HEALTH -> Icons.Default.FitnessCenter
                        ToolCategory.CONNECTORS -> Icons.Default.Hub
                        ToolCategory.UTILITY -> Icons.Default.Build
                        ToolCategory.IOT -> Icons.Default.DeveloperBoard
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, if (isExpanded) categoryColor else SurfaceHighlight, RoundedCornerShape(14.dp))
                            .clickable {
                                expandedToolId = if (isExpanded) null else tool.id
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
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
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(9.dp))
                                            .background(categoryColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = categoryIcon,
                                            contentDescription = null,
                                            tint = categoryColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tool.displayName,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = tool.id,
                                            color = categoryColor,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Risk level badge
                                    val (riskBg, riskFg) = when (tool.riskLevel) {
                                        ToolRiskLevel.LOW -> LumiGreen.copy(alpha = 0.2f) to LumiGreen
                                        ToolRiskLevel.MEDIUM -> LumiGold.copy(alpha = 0.2f) to LumiGold
                                        ToolRiskLevel.HIGH -> LumiPink.copy(alpha = 0.2f) to LumiPink
                                    }
                                    Surface(
                                        color = riskBg,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = tool.riskLevel.name,
                                            color = riskFg,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = tool.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                maxLines = if (isExpanded) 10 else 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            // Expanded Parameter Details
                            AnimatedVisibility(visible = isExpanded) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                        .background(SurfaceDarkVariant, RoundedCornerShape(8.dp))
                                        .border(0.5.dp, SurfaceHighlight, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Category:", color = TextTertiary, fontSize = 10.sp)
                                        Text(tool.category.name, color = categoryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Execution Engine:", color = TextTertiary, fontSize = 10.sp)
                                        Text("On-Device Gemma & Cloud Gemini", color = TextSecondary, fontSize = 10.sp)
                                    }

                                    HorizontalDivider(color = SurfaceHighlight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))

                                    Text(
                                        text = "PARAMETERS (${tool.parameters.size}):",
                                        color = TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    if (tool.parameters.isEmpty()) {
                                        Text("No parameters required (Autonomous zero-arg execution)", color = TextTertiary, fontSize = 10.sp)
                                    } else {
                                        tool.parameters.forEach { param ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(param.name, color = LumiCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
                                                    Text(" (${param.type})", color = TextTertiary, fontSize = 9.sp)
                                                    if (param.required) {
                                                        Text(" *", color = LumiPink, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Text(
                                                    text = param.description,
                                                    color = TextSecondary,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
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
        } else {
            // === SUB-TAB 1: CONNECTORS & CLOUD PLATFORMS ===
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
                                        CircularProgressIndicator(
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
                                            android.widget.Toast.makeText(context, "Remote Config Synced: ${it.welcomeGreeting.take(24)}...", android.widget.Toast.LENGTH_SHORT).show()
                                        }.onFailure { err ->
                                            analyticsManager.logRemoteConfigSync("failed")
                                            android.widget.Toast.makeText(context, "Sync Failed: ${err.message}", android.widget.Toast.LENGTH_SHORT).show()
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
                                    android.widget.Toast.makeText(context, "Proactive alert sent to notification shade", android.widget.Toast.LENGTH_SHORT).show()
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
                        android.widget.Toast.makeText(context, if (enable) "Google Workspace Connected" else "Google Workspace Disconnected", android.widget.Toast.LENGTH_SHORT).show()
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
                TextButton(
                    onClick = {
                        connectorManager.setGithubConnection(true, userText, tokenText)
                        showGithubDialog = false
                    }
                ) {
                    Text(stringResource(id = R.string.text_save_connect), color = androidx.compose.material3.MaterialTheme.colorScheme.primary)
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
                TextButton(
                    onClick = {
                        connectorManager.setSlackConnection(true, channelText, webhookText)
                        showSlackDialog = false
                    }
                ) {
                    Text(stringResource(id = R.string.text_save_connect), color = LumiGold)
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
