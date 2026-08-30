import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

imports_to_add = """
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
"""

if "import androidx.compose.foundation.lazy.LazyRow" not in content:
    content = content.replace("import androidx.compose.runtime.getValue", "import androidx.compose.runtime.getValue" + imports_to_add)

old_bottom = r"// Context & Bottom Dock(.*)\}\n\}"
# Wait, let's just split at "// Context & Bottom Dock" and reconstruct.
parts = content.split("// Context & Bottom Dock")

new_bottom = """// Context & Bottom Dock
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pet Action Buttons Toolbar (Floating Enterprise Dock)
            Surface(
                color = SurfaceDark.copy(alpha = 0.85f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vision
                    IconButton(onClick = { viewModel.setShowCamera(true) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Vision", tint = LumiGreen, modifier = Modifier.size(26.dp))
                    }

                    // Primary Chat/Voice button
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.linearGradient(listOf(LumiCyan, LumiViolet)),
                                shape = CircleShape
                            )
                            .clickable { onNavigateToChat() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isListening || isSpeaking) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Voice", tint = Color.White, modifier = Modifier.size(28.dp).clickable { viewModel.stopVoiceListening() })
                        } else {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                    }
                    
                    // Wardrobe
                    IconButton(onClick = { viewModel.setShowWardrobeScreen(true) }) {
                        Icon(Icons.Default.Checkroom, contentDescription = "Wardrobe", tint = LumiGold, modifier = Modifier.size(26.dp))
                    }
                }
            }

            // Tasks Slider & More Button
            var showMoreMenu by remember { mutableStateOf(false) }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tasks Slider
                val pendingTasks = tasks.filter { !it.isCompleted }
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(pendingTasks) { task ->
                        Surface(
                            color = SurfaceDark.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f)),
                            modifier = Modifier.width(220.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = LumiYellow.copy(alpha = 0.2f),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Bolt, contentDescription = "Up Next", tint = LumiYellow, modifier = Modifier.padding(6.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Up Next", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(task.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                }
                            }
                        }
                    }
                    if (pendingTasks.isEmpty()) {
                        item {
                            Surface(
                                color = SurfaceDark.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No pending tasks",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp)
                                )
                            }
                        }
                    }
                }

                // More Button
                Box {
                    Surface(
                        color = SurfaceDark.copy(alpha = 0.6f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = TextPrimary)
                        }
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false },
                        modifier = Modifier.background(SurfaceDarkVariant)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Life Hub", color = TextPrimary, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.Dashboard, tint = LumiYellow, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.setSelectedTab(NavDestination.LifeHub.tabIndex)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Wellness", color = TextPrimary, fontWeight = FontWeight.Medium) },
                            leadingIcon = { Icon(Icons.Default.SelfImprovement, tint = LumiPink, contentDescription = null) },
                            onClick = {
                                showMoreMenu = false
                                viewModel.setSelectedTab(NavDestination.Wellness.tabIndex)
                            }
                        )
                    }
                }
            }
        }
    }
}
"""

content = parts[0] + new_bottom
with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

