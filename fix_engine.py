import re

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    text = f.read()

# Replace the mangled item with a clean one
start_marker = "// Active Engine Router"
end_marker = "// On-Device Benchmark & Performance Test"

start_idx = text.find(start_marker)
end_idx = text.find(end_marker)

new_item = """// Active Engine Router
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
        
        """

new_text = text[:start_idx] + new_item + text[end_idx:]

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.write(new_text)
