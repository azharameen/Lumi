with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onDownloadLocalModel: (String) -> Unit,", "onDownloadLocalModel: (String) -> Unit,\n    onPauseModelDownload: (String) -> Unit,")
content = content.replace("onCancelModelDownload = { id -> viewModel.cancelModelDownload(id) },", "onCancelModelDownload = { id -> viewModel.cancelModelDownload(id) },\n                        onPauseModelDownload = { id -> viewModel.pauseModelDownload(id) },")
content = content.replace("onCancelModelDownload = onCancelModelDownload,", "onCancelModelDownload = onCancelModelDownload,\n                        onPauseModelDownload = onPauseModelDownload,")

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt", "r") as f:
    content = f.read()

content = content.replace("onDownloadLocalModel: (String) -> Unit,", "onDownloadLocalModel: (String) -> Unit,\n    onPauseModelDownload: (String) -> Unit,")

ui_logic_old = """
                                        }
                                        ModelDownloadStatus.DOWNLOADING -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    LinearProgressIndicator(
                                                        progress = { state.progress },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(6.dp)
                                                            .clip(RoundedCornerShape(3.dp)),
                                                        color = LumiMint,
                                                        trackColor = SurfaceHighlight
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text("${(state.progress * 100).toInt()}% • ETA ${state.etaSeconds}s", color = LumiMint, fontSize = 11.sp)
                                                        Text("${String.format("%.1f", state.speedMegaBytesPerSec)} MB/s", color = TextSecondary, fontSize = 11.sp)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                TextButton(
                                                    onClick = { onCancelModelDownload(spec.id) },
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Cancel", color = LumiPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        ModelDownloadStatus.VERIFYING -> {
"""

ui_logic_new = """
                                        }
                                        ModelDownloadStatus.DOWNLOADING, ModelDownloadStatus.PAUSED, ModelDownloadStatus.ERROR -> {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    val progColor = when(state.status) {
                                                        ModelDownloadStatus.ERROR -> LumiPink
                                                        ModelDownloadStatus.PAUSED -> LumiYellow
                                                        else -> LumiMint
                                                    }
                                                    LinearProgressIndicator(
                                                        progress = { state.progress },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(6.dp)
                                                            .clip(RoundedCornerShape(3.dp)),
                                                        color = progColor,
                                                        trackColor = SurfaceHighlight
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        if (state.status == ModelDownloadStatus.ERROR) {
                                                            Text("Error: ${state.errorMessage ?: "Failed"}", color = LumiPink, fontSize = 11.sp)
                                                        } else if (state.status == ModelDownloadStatus.PAUSED) {
                                                            Text("Paused • ${(state.progress * 100).toInt()}%", color = LumiYellow, fontSize = 11.sp)
                                                        } else {
                                                            Text("${(state.progress * 100).toInt()}% • ETA ${state.etaSeconds}s", color = LumiMint, fontSize = 11.sp)
                                                            Text("${String.format("%.1f", state.speedMegaBytesPerSec)} MB/s", color = TextSecondary, fontSize = 11.sp)
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(8.dp))
                                                if (state.status == ModelDownloadStatus.DOWNLOADING) {
                                                    TextButton(
                                                        onClick = { onPauseModelDownload(spec.id) },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Pause", color = LumiYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    TextButton(
                                                        onClick = { onDownloadLocalModel(spec.id) },
                                                        contentPadding = PaddingValues(0.dp)
                                                    ) {
                                                        Text("Resume", color = LumiMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                TextButton(
                                                    onClick = { onCancelModelDownload(spec.id) },
                                                    contentPadding = PaddingValues(0.dp)
                                                ) {
                                                    Text("Cancel", color = LumiPink, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        ModelDownloadStatus.VERIFYING -> {
"""
content = content.replace(ui_logic_old, ui_logic_new)

with open("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt", "w") as f:
    f.write(content)

