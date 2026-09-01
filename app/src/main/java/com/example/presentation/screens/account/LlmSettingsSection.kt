package com.example.presentation.screens.account
import androidx.compose.ui.res.stringResource
import com.example.R


import com.example.presentation.components.*
import com.example.data.remote.ModelDownloadProgress
import com.example.data.remote.LocalLlmModelSpec

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
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator

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
import com.example.data.remote.ModelDownloadStatus
import com.example.data.firebase.LumiRemoteConfigManager
import com.example.domain.model.LumiRemoteConfig
import org.koin.core.context.GlobalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.domain.connectors.ConnectorRepository
import com.example.core.theme.*
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing


@Composable
fun LlmSettingsSection(
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics(),
    userProfile: UserProfileData,
    aiRoutingMode: com.example.data.remote.AiRoutingMode,
    onSetAiRoutingMode: (com.example.data.remote.AiRoutingMode) -> Unit,
    benchmarkStatus: String?,
    localModelCatalog: List<LocalLlmModelSpec>,
    modelDownloadStates: Map<String, com.example.data.remote.ModelDownloadProgress>,
    activeLocalModelId: String?,
    selectedAccelerator: com.example.data.remote.HardwareAccelerator,
    onUpdateProfile: (UserProfileData) -> Unit,
    onDownloadLocalModel: (String) -> Unit,
    onPauseModelDownload: (String) -> Unit,
    onCancelModelDownload: (String) -> Unit,
    onDeleteLocalModel: (String) -> Unit,
    onSetActiveLocalModel: (String) -> Unit,
    onSetHardwareAccelerator: (com.example.data.remote.HardwareAccelerator) -> Unit,
    onRunGemmaBenchmark: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var temperature by remember { mutableFloatStateOf(userProfile.temperature) }

    val remoteConfigManager = remember {
        try {
            GlobalContext.get().get<LumiRemoteConfigManager>()
        } catch (_: Exception) {
            null
        }
    }
    val rcConfig = remoteConfigManager?.config?.collectAsStateWithLifecycle(initialValue = LumiRemoteConfig())?.value
    var customInstructions by remember { mutableStateOf(userProfile.customAiInstructions) }

    val cloudModels = listOf(
        AiModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash (Ultra Fast & Multimodal)", "Input: Text, Audio, Images, Video", "Output: Text, Code"),
        AiModelInfo("gemini-2.5-pro", "Gemini 2.5 Pro (Deep Reasoning & Tutoring)", "Input: Text, Audio, Images, Video", "Output: Text, Code"),
        AiModelInfo("gemini-2.5-flash-lite", "Gemini 2.5 Flash-Lite (Low Latency)", "Input: Text, Audio, Images, Video", "Output: Text, Code"),
        AiModelInfo("hybrid-auto", "Hybrid Smart Router (Edge + Cloud)", "Input: Text, Audio, Images", "Output: Text"),
        AiModelInfo("on-device-gemma", "On-Device Neural Engine (Offline & Private)", "Input: Text Only", "Output: Text")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(top = MaterialTheme.spacing.medium, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        // Firebase AI Cloud LLM Status Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.text_firebase_ai_cloud_llm),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = LumiGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Zero-Key Active",
                                    color = LumiGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                
                                )
                            }

                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.text_firebase_ai_desc),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // Active Engine Router
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = stringResource(R.string.text_active_llm_intelligence_engine),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    cloudModels.forEach { (modelId, label) ->
                        val isSelected = when (modelId) {
                            "hybrid-auto" -> aiRoutingMode == com.example.data.remote.AiRoutingMode.HYBRID_AUTO
                            "on-device-gemma" -> aiRoutingMode == com.example.data.remote.AiRoutingMode.STRICT_ON_DEVICE
                            else -> aiRoutingMode == com.example.data.remote.AiRoutingMode.CLOUD_TURBO && userProfile.geminiModelChoice == modelId
                        }
                        
                        Surface(
                            color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else SurfaceDarkVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.spacing.extraSmall)
                                .clickable {
                                    val newRoutingMode = when (modelId) {
                                        "hybrid-auto" -> com.example.data.remote.AiRoutingMode.HYBRID_AUTO
                                        "on-device-gemma" -> com.example.data.remote.AiRoutingMode.STRICT_ON_DEVICE
                                        else -> com.example.data.remote.AiRoutingMode.CLOUD_TURBO
                                    }
                                    onSetAiRoutingMode(newRoutingMode)
                                    onUpdateProfile(userProfile.copy(geminiModelChoice = modelId))
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (modelId.contains("on-device")) Icons.Default.Memory else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = stringResource(id = R.string.desc_active), tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // On-Device Benchmark & Performance Test
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.text_ondevice_neural_benchmark),
                                color = LumiPink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.text_measure_real_cpugpu_inference_latency_and),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                        Button(
                            onClick = { onRunGemmaBenchmark() },
                            colors = ButtonDefaults.buttonColors(containerColor = LumiPink),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_run_benchmark")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(MaterialTheme.spacing.medium))
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                            Text(stringResource(id = R.string.text_run_test), color = ObsidianDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (!benchmarkStatus.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(MaterialTheme.spacing.small),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = benchmarkStatus,
                                color = LumiMint,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        // Firebase Remote Config Live Sync & Cloud Parameters Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = LumiCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Firebase Cloud Parameters",
                                color = LumiCyan,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = LumiCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Live",
                                color = LumiCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (rcConfig != null) {
                        Text(
                            text = "Dynamic Temperature: ${rcConfig.aiCreativityTemperature} | Nudge Interval: ${rcConfig.proactiveNudgeIntervalHours}h",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if (rcConfig.seasonalThemeEnabled) {
                            Text(
                                text = "Seasonal Theme: ${rcConfig.seasonalThemeName}",
                                color = LumiGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (rcConfig.companionTipOfTheDay.isNotBlank()) {
                            Text(
                                text = "Daily Companion Tip: \"${rcConfig.companionTipOfTheDay}\"",
                                color = TextTertiary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "Cloud configuration synchronized with default local parameters.",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val success = remoteConfigManager?.fetchAndActivate() ?: false
                                Toast.makeText(
                                    context,
                                    if (success) "Remote Config parameters updated!" else "Remote Config checked (already up to date)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarkVariant),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = LumiCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Sync Cloud Parameters",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}




