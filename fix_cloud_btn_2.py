import re

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    text = f.read()

start_marker = "// Firebase Remote Config Live Sync & Cloud Parameters Card"
end_marker = "    }\n}" # End of the LazyColumn and the fun LlmSettingsSection

start_idx = text.find(start_marker)
end_idx = text.rfind(end_marker)

if start_idx == -1 or end_idx == -1:
    print("Could not find markers")
    exit(1)

new_item = """// Firebase Remote Config Live Sync & Cloud Parameters Card
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
                                text = "Daily Companion Tip: \\\"${rcConfig.companionTipOfTheDay}\\\"",
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
"""

new_text = text[:start_idx] + new_item + text[end_idx:]

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.write(new_text)
