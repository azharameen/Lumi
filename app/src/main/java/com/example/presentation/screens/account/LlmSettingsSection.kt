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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.core.theme.*
import androidx.compose.material3.MaterialTheme
import com.example.core.theme.spacing


@Composable
fun LlmSettingsSection(
    userProfile: UserProfileData,
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
    var temperature by remember { mutableFloatStateOf(userProfile.temperature) }
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

        // On-Device Local LLM Model Hub & Downloader
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
                            Icon(Icons.Default.Download, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(
                                text = stringResource(R.string.text_ondevice_local_llm_hub),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(MaterialTheme.spacing.small)
                        ) {
                            Text(
                                text = stringResource(R.string.text_100_offline_private),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(R.string.text_download_genuine_gguflitert_neural_weights_to),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = MaterialTheme.spacing.extraSmall, bottom = 12.dp)
                    )

                    // Hardware Accelerator selection
                    Text(
                        text = stringResource(R.string.text_neural_hardware_acceleration),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        HardwareAccelerator.values().forEach { acc ->
                            val isAccSelected = selectedAccelerator == acc
                            Surface(
                                color = if (isAccSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else SurfaceDarkVariant,
                                shape = RoundedCornerShape(MaterialTheme.spacing.small),
                                border = if (isAccSelected) androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetHardwareAccelerator(acc) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(MaterialTheme.spacing.small),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = when (acc) {
                                            HardwareAccelerator.GPU_OPENCL -> "GPU OpenCL"
                                            HardwareAccelerator.NPU_NNAPI -> "NPU NNAPI"
                                            HardwareAccelerator.CPU_MULTITHREAD -> "CPU (4-Core)"
                                        },
                                        color = if (isAccSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isAccSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SurfaceDarkVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Model Catalog List
                    localModelCatalog.forEach { modelSpec ->
                        val progress = modelDownloadStates[modelSpec.id]
                        val isDownloaded = progress?.status == ModelDownloadStatus.DOWNLOADED
                        val isDownloading = progress?.status == ModelDownloadStatus.DOWNLOADING
                        val isActive = activeLocalModelId == modelSpec.id && isDownloaded

                        Surface(
                            color = SurfaceDarkVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isActive) androidx.compose.foundation.BorderStroke(1.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = modelSpec.name,
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(MaterialTheme.spacing.extraSmall)
                                            ) {
                                                Text(
                                                    text = modelSpec.quantization,
                                                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.extraSmall, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${modelSpec.parameterCount} • ${modelSpec.sizeDisplay} • RAM: ${modelSpec.memoryRequiredRam}",
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    if (isActive) {
                                        Surface(
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = stringResource(R.string.text_active),
                                                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = modelSpec.description,
                                    color = TextTertiary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                // Download Progress if active
                                if (isDownloading && progress != null) {
                                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                                    LinearProgressIndicator(
                                        progress = { progress.progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp),
                                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                        trackColor = ObsidianDark
                                    )
                                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${(progress.progress * 100).toInt()}% (${progress.bytesDownloaded / (1024 * 1024)} MB / ${modelSpec.sizeDisplay})",
                                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                            fontSize = 10.sp
                                        )
                                        Text(
                                            text = "${progress.speedMegaBytesPerSec} MB/s • ETA ${progress.etaSeconds}s",
                                            color = TextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isDownloading) {
                                        OutlinedButton(
                                            onClick = { onCancelModelDownload(modelSpec.id) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LumiCoral),
                                            shape = RoundedCornerShape(MaterialTheme.spacing.small),
                                            modifier = Modifier.height(MaterialTheme.spacing.extraLarge)
                                        ) {
                                            Text(stringResource(id = R.string.text_cancel), fontSize = 11.sp)
                                        }
                                    } else if (isDownloaded) {
                                        if (!isActive) {
                                            Button(
                                                onClick = { onSetActiveLocalModel(modelSpec.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(MaterialTheme.spacing.small),
                                                modifier = Modifier.height(MaterialTheme.spacing.extraLarge)
                                            ) {
                                                Text(stringResource(id = R.string.text_set_active), color = ObsidianDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                                        }

                                        IconButton(
                                            onClick = { onDeleteLocalModel(modelSpec.id) },
                                            modifier = Modifier.size(MaterialTheme.spacing.extraLarge)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.desc_delete_model_weights), tint = TextTertiary, modifier = Modifier.size(MaterialTheme.spacing.medium))
                                        }
                                    } else {
                                        Button(
                                            onClick = { onDownloadLocalModel(modelSpec.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary),
                                            shape = RoundedCornerShape(MaterialTheme.spacing.small),
                                            modifier = Modifier.height(MaterialTheme.spacing.extraLarge)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                                            Text("Download (${modelSpec.sizeDisplay})", color = ObsidianDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Temperature Slider & Creativity Card
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
                        Text(
                            text = stringResource(R.string.text_creativity_temperature),
                            color = LumiGold,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = String.format("%.2f", temperature),
                            color = LumiGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = temperature,
                        onValueChange = {
                            temperature = it
                            onUpdateProfile(userProfile.copy(temperature = it))
                        },
                        valueRange = 0.0f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = LumiGold,
                            activeTrackColor = LumiGold,
                            inactiveTrackColor = SurfaceDarkVariant
                        ),
                        modifier = Modifier.testTag("slider_temperature")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(id = R.string.text_0_0_precise_deterministic), color = TextTertiary, fontSize = 10.sp)
                        Text(stringResource(id = R.string.text_1_0_creative_playful), color = TextTertiary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Custom System Instructions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text(
                        text = stringResource(R.string.text_custom_ai_system_instructions),
                        color = LumiGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.text_specify_persistent_system_rules_eg_format),
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp, bottom = MaterialTheme.spacing.small)
                    )

                    OutlinedTextField(
                        value = customInstructions,
                        onValueChange = { customInstructions = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("input_system_instructions"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LumiGold,
                            unfocusedBorderColor = SurfaceDarkVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 5,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    Button(
                        onClick = {
                            onUpdateProfile(userProfile.copy(customAiInstructions = customInstructions))
                            Toast.makeText(context, "System instructions saved", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LumiGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = ObsidianDark, modifier = Modifier.size(MaterialTheme.spacing.medium))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                        Text(stringResource(id = R.string.text_save_instructions), color = ObsidianDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature Toggles (Tool Calling, Proactive Briefing, Speech)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Text(
                        text = stringResource(R.string.text_ai_autonomy_capabilities),
                        color = LumiGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ToggleSettingRow(
                        title = "Autonomous Tool Dispatch",
                        subtitle = "Allow Lumi to schedule calendar events and create tasks automatically",
                        isChecked = userProfile.enableToolCalling,
                        accentColor = LumiGreen,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableToolCalling = it)) }
                    )

                    ToggleSettingRow(
                        title = "Proactive Daily Briefings",
                        subtitle = "Auto-synthesize morning & evening productivity briefings",
                        isChecked = userProfile.enableProactiveBriefings,
                        accentColor = LumiGold,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableProactiveBriefings = it)) }
                    )

                    ToggleSettingRow(
                        title = "Voice Speech Synthesis (TTS)",
                        subtitle = "Speak responses automatically during voice dialogue",
                        isChecked = userProfile.enableSpeechOutput,
                        accentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableSpeechOutput = it)) }
                    )

                    ToggleSettingRow(
                        title = "On-Device Neural Fallback",
                        subtitle = "Route private notes to local neural engine when offline",
                        isChecked = userProfile.enableLocalAiFallback,
                        accentColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        onCheckedChange = { onUpdateProfile(userProfile.copy(enableLocalAiFallback = it)) }
                    )
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
    }
}


