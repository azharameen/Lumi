import re

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    text = f.read()

target = """        // On-Device Benchmark & Performance Test"""

new_section = """        // Local On-Device Models Catalog
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(MaterialTheme.spacing.medium),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = LumiCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            text = "Local On-Device Models",
                            color = LumiCyan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Download neural engines to run fully offline without cloud dependencies. Memory and RAM recommendations apply.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    localModelCatalog.forEach { model ->
                        val progress = modelDownloadStates[model.id]
                        val isActive = model.id == activeLocalModelId
                        val isDownloaded = progress?.status == com.example.data.remote.DownloadStatus.DOWNLOADED

                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.spacing.extraSmall)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = model.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Size: ${model.sizeDisplay} | Context: ${model.contextWindowTokens} Tokens", color = TextSecondary, fontSize = 11.sp)
                                    }
                                    
                                    if (isActive) {
                                        Surface(color = LumiGreen.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                            Text("Active", color = LumiGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    } else if (isDownloaded) {
                                        Surface(color = LumiCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                            Text("Ready", color = LumiCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    } else if (progress != null && progress.status == com.example.data.remote.DownloadStatus.DOWNLOADING) {
                                        CircularProgressIndicator(progress = { progress.progress }, modifier = Modifier.size(20.dp), color = LumiCyan, strokeWidth = 2.dp)
                                    } else {
                                        IconButton(
                                            onClick = { onDownloadLocalModel(model.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Download, contentDescription = "Download ${model.name}", tint = LumiCyan, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = LumiGold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = model.recommendedFor, color = LumiGold, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // On-Device Benchmark & Performance Test"""

text = text.replace(target, new_section)

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.write(text)
