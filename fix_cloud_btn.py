import re

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    text = f.read()

start_marker = "                    Button("
end_marker = "                }"

start_idx = text.rfind(start_marker)
end_idx = text.find(end_marker, start_idx)

new_item = """                    Button(
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
"""

new_text = text[:start_idx] + new_item + text[end_idx:]

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.write(new_text)
