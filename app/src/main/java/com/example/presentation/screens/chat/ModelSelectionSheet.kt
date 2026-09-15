package com.example.presentation.screens.chat

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.res.stringResource
import com.example.R
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
    modifier: Modifier = Modifier,
    onNavigateToDownloadHub: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
            Text(
                text = "Select AI Model",
                color = TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose which AI model processes your messages.",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
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
                    style = MaterialTheme.typography.labelSmall,
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
                    style = MaterialTheme.typography.labelSmall,
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
                                Text(stringResource(R.string.text_no_local_models), color = TextSecondary, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    text = "Download a model in Account → LLM Hub",
                                    color = LumiGreen,
                                    style = MaterialTheme.typography.labelMedium,
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
                    style = MaterialTheme.typography.labelSmall,
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
        shape = RoundedCornerShape(MaterialTheme.spacing.cornerMedium),
        border = if (isSelected) BorderStroke(
            1.dp, accentColor.copy(alpha = 0.4f)
        ) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = MaterialTheme.spacing.extraSmall),
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
                Text(displayName, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.labelMedium, lineHeight = 14.sp)
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
