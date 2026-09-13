package com.example.presentation.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.theme.*
import com.example.data.remote.LocalLlmModelSpec
import com.example.domain.ai.CloudModelSpec

/**
 * Bottom sheet for selecting the active AI model for the current chat session.
 * Shows an Auto option, only downloaded local models, and all available cloud models.
 * Model lists are data-driven — never hardcoded in this composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectionSheet(
    selectedModelId: String,
    downloadedLocalModels: List<LocalLlmModelSpec>,
    availableCloudModels: List<CloudModelSpec>,
    onSelectModel: (String) -> Unit,
    onDismiss: () -> Unit,
    onNavigateToDownloadHub: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
            Text(
                text = "Select AI Model",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose which AI model processes your messages.",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 500.dp)
        ) {
            // Intelligent Routing (Auto)
            item {
                Text(
                    text = "INTELLIGENT ROUTING",
                    color = LumiCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            item {
                ModelOptionCard(
                    displayName = "Auto (Smart Model Selection)",
                    subtitle = "Automatically routes between local on-device & cloud models based on task.",
                    isSelected = selectedModelId.isBlank(),
                    icon = Icons.Default.AutoAwesome,
                    accentColor = LumiCyan,
                    onSelect = { onSelectModel(""); onDismiss() }
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            // On-Device section
            item {
                Text(
                    text = "ON-DEVICE (PRIVATE)",
                    color = LumiGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            if (downloadedLocalModels.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ObsidianDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.OfflineBolt,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("No local models downloaded", color = TextSecondary, fontSize = 13.sp)
                                Text(
                                    text = "Download a model in Account → LLM Hub",
                                    color = LumiGreen,
                                    fontSize = 11.sp,
                                    modifier = Modifier.clickable { onNavigateToDownloadHub() }
                                )
                            }
                        }
                    }
                }
            } else {
                items(downloadedLocalModels, key = { it.id }) { spec ->
                    ModelOptionCard(
                        displayName = spec.displayName,
                        subtitle = "${spec.hardwareTarget} • ${spec.sizeDisplay}",
                        isSelected = selectedModelId == spec.id,
                        icon = Icons.Default.OfflineBolt,
                        accentColor = LumiGreen,
                        onSelect = { onSelectModel(spec.id); onDismiss() }
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            // Cloud section
            item {
                Text(
                    text = "CLOUD (ONLINE)",
                    color = LumiViolet,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            items(availableCloudModels, key = { it.id }) { spec ->
                ModelOptionCard(
                    displayName = spec.displayName,
                    subtitle = spec.description,
                    isSelected = selectedModelId == spec.id,
                    icon = Icons.Default.Cloud,
                    accentColor = LumiViolet,
                    onSelect = { onSelectModel(spec.id); onDismiss() }
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun ModelOptionCard(
    displayName: String,
    subtitle: String,
    isSelected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.12f) else ObsidianDark
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            1.dp, accentColor.copy(alpha = 0.4f)
        ) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(displayName, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
