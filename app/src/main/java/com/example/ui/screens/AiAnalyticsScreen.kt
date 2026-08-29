package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AiExecutionLogEntity
import com.example.data.remote.AiRoutingMode
import com.example.domain.ai.AiModelRegistry
import com.example.domain.ai.AiTaskCategory
import com.example.domain.ai.ModelSpec
import com.example.ui.theme.LumiCyan
import com.example.ui.theme.LumiGold
import com.example.ui.theme.LumiGreen
import com.example.ui.theme.LumiMint
import com.example.ui.theme.LumiPink
import com.example.ui.theme.LumiViolet
import com.example.ui.theme.LumiYellow
import com.example.ui.theme.ObsidianDark
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceDarkVariant
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.LumiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AiAnalyticsScreen(
    viewModel: LumiViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.aiExecutionLogs.collectAsState()
    val routingMode by viewModel.aiRoutingMode.collectAsState()
    val benchmarkStatus by viewModel.benchmarkStatus.collectAsState()

    var selectedFilterEngine by remember { mutableStateOf("ALL") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showRoutingMatrix by remember { mutableStateOf(false) }

    val filteredLogs = remember(logs, selectedFilterEngine) {
        when (selectedFilterEngine) {
            "ON_DEVICE_GEMMA" -> logs.filter { it.engineType == "ON_DEVICE_GEMMA" }
            "CLOUD_GEMINI" -> logs.filter { it.engineType == "CLOUD_GEMINI" }
            else -> logs
        }
    }

    // Aggregates
    val totalInvocations = logs.size
    val totalTokens = logs.sumOf { it.totalTokens.toLong() }
    val totalPromptTokens = logs.sumOf { it.promptTokens.toLong() }
    val totalCompletionTokens = logs.sumOf { it.completionTokens.toLong() }
    val totalCostUsd = logs.sumOf { it.estimatedCostUsd }
    val onDeviceCount = logs.count { it.engineType == "ON_DEVICE_GEMMA" }
    val cloudCount = logs.count { it.engineType == "CLOUD_GEMINI" }
    val fallbackCount = logs.count { it.fallbackTriggered }
    val avgLatencyMs = if (logs.isNotEmpty()) logs.map { it.durationMs }.average().toInt() else 0

    // Estimated money saved by running on-device Gemma instead of cloud
    val onDeviceTokens = logs.filter { it.engineType == "ON_DEVICE_GEMMA" }.sumOf { it.totalTokens.toLong() }
    val savedCostUsd = onDeviceTokens * 0.00000020 // Average estimated cloud savings

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianDark)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Clear Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Hub,
                            contentDescription = "Analytics",
                            tint = LumiCyan,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Router & Engine Architecture",
                            color = TextPrimary,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Intelligent Local LLM vs Cloud LLM routing & real-time telemetry",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (logs.isNotEmpty()) {
                    IconButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.testTag("clear_ai_logs_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            tint = TextTertiary
                        )
                    }
                }
            }
        }

        // 2. Engine Routing Mode Selector
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
                        Text(
                            text = "AI ENGINE ROUTING MODE",
                            color = TextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )

                        TextButton(
                            onClick = { showRoutingMatrix = !showRoutingMatrix },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = LumiCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showRoutingMatrix) "Hide Matrix" else "Routing Matrix",
                                color = LumiCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoutingModeOption(
                            title = "Smart Hybrid",
                            subtitle = "Auto Routing",
                            icon = Icons.Default.Bolt,
                            isSelected = routingMode == AiRoutingMode.HYBRID_AUTO,
                            selectedColor = LumiCyan,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAiRoutingMode(AiRoutingMode.HYBRID_AUTO) }
                        )

                        RoutingModeOption(
                            title = "Gemma 100%",
                            subtitle = "Strict Offline",
                            icon = Icons.Default.Security,
                            isSelected = routingMode == AiRoutingMode.STRICT_ON_DEVICE,
                            selectedColor = LumiGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAiRoutingMode(AiRoutingMode.STRICT_ON_DEVICE) }
                        )

                        RoutingModeOption(
                            title = "Cloud Turbo",
                            subtitle = "Gemini Flash/Pro",
                            icon = Icons.Default.Cloud,
                            isSelected = routingMode == AiRoutingMode.CLOUD_TURBO,
                            selectedColor = LumiViolet,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.setAiRoutingMode(AiRoutingMode.CLOUD_TURBO) }
                        )
                    }
                }
            }
        }

        // 3. Expandable Task Routing Decision Matrix
        if (showRoutingMatrix) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarkVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AltRoute,
                                contentDescription = null,
                                tint = LumiCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dynamic Task Routing Matrix",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "How Lumi decides between On-Device Gemma and Cloud Gemini:",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        RoutingMatrixRow(
                            task = "Private Mood & Wellness",
                            engine = "⚡ On-Device Gemma 2B",
                            reason = "100% Privacy — reflections & stress logs never leave the device",
                            isLocal = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RoutingMatrixRow(
                            task = "Companion Chat & Banter",
                            engine = "⚡ On-Device Gemma 2B",
                            reason = "Sub-120ms latency, zero cloud cost, continuous offline availability",
                            isLocal = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RoutingMatrixRow(
                            task = "Quick Task / Water Actions",
                            engine = "⚡ On-Device Gemma 2B",
                            reason = "Fast deterministic device tool execution",
                            isLocal = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RoutingMatrixRow(
                            task = "Camera Vision & Screenshots",
                            engine = "☁️ Cloud Gemini 2.5 Flash",
                            reason = "Requires multimodal vision encoder and large parameter scale",
                            isLocal = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RoutingMatrixRow(
                            task = "Deep Reasoning & Coding Tutoring",
                            engine = "☁️ Cloud Gemini 3.1 Pro",
                            reason = "Complex multi-step STEM reasoning and code synthesis",
                            isLocal = false
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        RoutingMatrixRow(
                            task = "Resilient Fallback Pipeline",
                            engine = "⚡ Auto Fallback to Gemma",
                            reason = "Zero dropped user turns if cloud times out or connection drops",
                            isLocal = true
                        )
                    }
                }
            }
        }

        // 4. Metrics Summary Grid (4 KPI Cards)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSummaryCard(
                        title = "Total Invocations",
                        value = "$totalInvocations runs",
                        subValue = "⚡ $onDeviceCount Local | ☁️ $cloudCount Cloud" + if (fallbackCount > 0) " | 🔄 $fallbackCount Fallbacks" else "",
                        accentColor = LumiCyan,
                        icon = Icons.Default.Memory,
                        modifier = Modifier.weight(1f)
                    )
                    MetricSummaryCard(
                        title = "Total Tokens",
                        value = "${String.format(Locale.US, "%,d", totalTokens)}",
                        subValue = "In: $totalPromptTokens | Out: $totalCompletionTokens",
                        accentColor = LumiGold,
                        icon = Icons.Default.Storage,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricSummaryCard(
                        title = "Est. Cloud Cost",
                        value = "$${String.format(Locale.US, "%.5f", totalCostUsd)}",
                        subValue = "Saved: ~$${String.format(Locale.US, "%.4f", savedCostUsd)} via Gemma",
                        accentColor = LumiMint,
                        icon = Icons.Default.Savings,
                        modifier = Modifier.weight(1f)
                    )
                    MetricSummaryCard(
                        title = "Avg Latency",
                        value = "$avgLatencyMs ms",
                        subValue = if (avgLatencyMs < 200) "⚡ Ultra-Fast Local" else "☁️ Normal Cloud Ping",
                        accentColor = LumiPink,
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. Active Model Registry Specs
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
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(LumiGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ACTIVE MODEL REGISTRY",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            color = LumiGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Dual-Engine Pipeline",
                                color = LumiGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    for (model in AiModelRegistry.ALL_MODELS) {
                        ModelRegistryItem(model = model)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { viewModel.runGemmaBenchmark() },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceHighlight),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("benchmark_gemma_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Benchmark",
                            tint = LumiCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Run On-Device Inference Benchmark",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (benchmarkStatus != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = benchmarkStatus ?: "",
                                color = LumiMint,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Execution Logs Filter & History Header
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXECUTION LOG & ROUTING AUDIT (${filteredLogs.size})",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilterEngine == "ALL",
                            onClick = { selectedFilterEngine = "ALL" },
                            label = { Text("All (${logs.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LumiCyan.copy(alpha = 0.2f),
                                selectedLabelColor = LumiCyan
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterEngine == "ON_DEVICE_GEMMA",
                            onClick = { selectedFilterEngine = "ON_DEVICE_GEMMA" },
                            label = { Text("⚡ Gemma Local ($onDeviceCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LumiGreen.copy(alpha = 0.2f),
                                selectedLabelColor = LumiGreen
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilterEngine == "CLOUD_GEMINI",
                            onClick = { selectedFilterEngine = "CLOUD_GEMINI" },
                            label = { Text("☁️ Gemini Cloud ($cloudCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LumiViolet.copy(alpha = 0.2f),
                                selectedLabelColor = LumiViolet
                            )
                        )
                    }
                }
            }
        }

        // 7. Log Entries List
        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = "Empty",
                            tint = TextTertiary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No AI Execution Logs Yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Interact with Lumi via Chat, Schedule, Wellness, or Vision to populate detailed telemetry.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            items(filteredLogs, key = { it.id }) { log ->
                AiExecutionLogCard(log = log)
            }
        }
    }

    // Confirmation Dialog for Clearing Logs
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Analytics History?", color = TextPrimary) },
            text = {
                Text(
                    "This will delete all saved token and latency execution logs. Total counter aggregates will reset.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAiAnalytics()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LumiPink)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = SurfaceDark
        )
    }
}

@Composable
private fun RoutingMatrixRow(
    task: String,
    engine: String,
    reason: String,
    isLocal: Boolean
) {
    val color = if (isLocal) LumiGreen else LumiViolet
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = engine,
                    color = color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = reason,
                color = TextSecondary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ModelRegistryItem(model: ModelSpec) {
    val isLocal = model.isOfflineCapable
    val accent = if (isLocal) LumiGreen else LumiCyan

    Surface(
        color = SurfaceDarkVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isLocal) Icons.Default.Bolt else Icons.Default.Cloud,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = model.displayName,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = if (isLocal) "FREE (Local GPU)" else "$${model.inputCostPerMillionTokensUsd}/$${model.outputCostPerMillionTokensUsd} per 1M tok",
                    color = if (isLocal) LumiGreen else LumiGold,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "${model.description} • Latency: ~${model.typicalLatencyMs}ms • Target: ${model.hardwareTarget}",
                color = TextTertiary,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun RoutingModeOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) selectedColor.copy(alpha = 0.15f) else SurfaceDarkVariant,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, selectedColor) else null,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) selectedColor else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = if (isSelected) TextPrimary else TextSecondary,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = subtitle,
                color = if (isSelected) selectedColor else TextTertiary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun MetricSummaryCard(
    title: String,
    value: String,
    subValue: String,
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = TextTertiary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subValue,
                color = accentColor,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AiExecutionLogCard(log: AiExecutionLogEntity) {
    val isOnDevice = log.engineType == "ON_DEVICE_GEMMA"
    val engineColor = if (isOnDevice) LumiGreen else LumiViolet
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()) }
    val startTimeFormatted = remember(log.startTimeMillis) { dateFormat.format(Date(log.startTimeMillis)) }
    val finishTimeFormatted = remember(log.finishTimeMillis) { dateFormat.format(Date(log.finishTimeMillis)) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task Category & Model
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = LumiCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = log.taskCategory,
                            color = LumiCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = log.modelName,
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Engine Badge
                Surface(
                    color = engineColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isOnDevice) Icons.Default.Bolt else Icons.Default.Cloud,
                            contentDescription = null,
                            tint = engineColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOnDevice) "On-Device Local" else "Cloud Server",
                            color = engineColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Fallback indicator badge if triggered
            if (log.fallbackTriggered) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = LumiPink.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = LumiPink,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto Fallback Triggered (Cloud Offline -> On-Device Gemma)",
                            color = LumiPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Routing Reason
            if (log.routingReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = LumiCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = log.routingReason,
                        color = LumiCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timing & Hardware Target
            Text(
                text = "⏱️ Start: $startTimeFormatted\n🏁 Finish: $finishTimeFormatted (${log.durationMs}ms) • 🎯 ${log.hardwareTarget}",
                color = TextTertiary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Token & Cost Metrics Row
            Surface(
                color = SurfaceDarkVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tokens: ${log.promptTokens} in / ${log.completionTokens} out = ${log.totalTokens} tot",
                        color = LumiGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = if (isOnDevice) "0 Credits (Free Offline)" else "$${String.format(Locale.US, "%.5f", log.estimatedCostUsd)}",
                        color = if (isOnDevice) LumiGreen else LumiMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Snippet preview
            if (log.promptPreview.isNotBlank()) {
                Text(
                    text = "Prompt: \"${log.promptPreview}\"",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (log.responsePreview.isNotBlank()) {
                Text(
                    text = "Lumi: \"${log.responsePreview}\"",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
