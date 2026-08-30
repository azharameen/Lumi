import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# 1. Imports for tasks/events
if "import com.example.domain.account.TaskItem" not in content:
    content = content.replace("import com.example.domain.model.PetEmotion", "import com.example.domain.account.TaskItem\nimport com.example.domain.model.PetEmotion")

# 2. Add tasks and events collection
old_state = r"val networkStatus by viewModel\.networkStatus\.collectAsState\(\)"
new_state = """val networkStatus by viewModel.networkStatus.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val events by viewModel.allCalendarEvents.collectAsState()"""
content = re.sub(old_state, new_state, content)

# 3. Dynamic background glow
old_bg = r"Brush\.verticalGradient\(\s*colors = listOf\(Color\(0xFF16161E\), ObsidianDark\)\s*\)"
new_bg = """Brush.radialGradient(
                    colors = listOf(
                        when (petStatus.currentEmotion) {
                            PetEmotion.HAPPY, PetEmotion.ENERGETIC -> LumiCyan.copy(alpha = 0.2f)
                            PetEmotion.LOVING -> LumiPink.copy(alpha = 0.2f)
                            PetEmotion.CALM, PetEmotion.SLEEPY -> LumiMint.copy(alpha = 0.2f)
                            PetEmotion.CONCERNED -> LumiGold.copy(alpha = 0.2f)
                            PetEmotion.THINKING -> LumiViolet.copy(alpha = 0.2f)
                            else -> LumiCyan.copy(alpha = 0.2f)
                        },
                        Color(0xFF16161E),
                        ObsidianDark
                    ),
                    radius = 1200f
                )"""
content = re.sub(old_bg, new_bg, content)

# 4. Remove quick action row and Bottom HUD, replace with unified Premium Dock & Context Card
old_bottom = r"// Voice Mode Cancel / Chat / Camera Row.*\} // End of Box"
# We need to capture from "// Voice Mode Cancel / Chat / Camera Row" all the way to the end of HomeScreen function.
# Wait, let's use a simpler replacement strategy. We will replace everything from "// Voice Mode Cancel" down to the end of HomeScreen.

content_split = content.split("// Voice Mode Cancel / Chat / Camera Row")
if len(content_split) == 2:
    # Find the end of HomeScreen which is marked by the second to last '}' before HudButton
    bottom_part = content_split[1]
    
    # We will inject our new bottom architecture
    new_bottom = """
                // Context & Bottom Dock
                val isListening by viewModel.voiceEngine.isListening.collectAsState()
                val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Up Next Card (Contextual Intelligence)
                    val nextTask = tasks.firstOrNull { !it.isCompleted }
                    if (nextTask != null) {
                        Surface(
                            color = SurfaceDark.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth(0.9f)
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
                                    Text(nextTask.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                                }
                            }
                        }
                    }

                    // Floating Enterprise Dock
                    Surface(
                        color = SurfaceDark.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Wellness
                            IconButton(onClick = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) }) {
                                Icon(Icons.Default.SelfImprovement, contentDescription = "Wellness", tint = LumiPink, modifier = Modifier.size(26.dp))
                            }
                            
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

                            // Life Hub
                            IconButton(onClick = { viewModel.setSelectedTab(NavDestination.LifeHub.tabIndex) }) {
                                Icon(Icons.Default.Dashboard, contentDescription = "Life Hub", tint = LumiYellow, modifier = Modifier.size(26.dp))
                            }
                        }
                    }
                }
            }
        }
"""
    
    # We replace everything from "// Voice Mode Cancel / Chat / Camera Row" to the end of the HomeScreen function (which is before the @Composable private fun HudButton)
    # The end of the function is right before "@Composable\nprivate fun HudButton"
    bottom_part_split = bottom_part.split("@Composable\nprivate fun HudButton")
    
    # reconstruct content
    content = content_split[0] + new_bottom + "\n@Composable\nprivate fun HudButton" + bottom_part_split[1]

# We should also hide the redundant Wardrobe button from the Top Right since we moved it to the dock
old_top_right_action = r"""// Action Icons \(Wardrobe, Nap/Wake, Vision\).*?Row\(horizontalArrangement = Arrangement.spacedBy\(8.dp\)\) \{.*?IconButton\(.*?onClick = \{ viewModel.setShowWardrobeScreen\(true\) \}.*?modifier = Modifier.background\(SurfaceDarkVariant, CircleShape\).size\(36.dp\).*?\).*?\{.*?Icon\(imageVector = Icons.Default.Checkroom, contentDescription = "Wardrobe", tint = LumiPink, modifier = Modifier.size\(18.dp\)\).*?\}.*?IconButton\(.*?onClick = \{ viewModel.togglePetSleep\(\) \}.*?modifier = Modifier.background\(SurfaceDarkVariant, CircleShape\).size\(36.dp\).*?\).*?\{.*?Icon\(imageVector = if \(petStatus.currentEmotion == PetEmotion.SLEEPY\) Icons.Default.WbSunny else Icons.Default.Bedtime, contentDescription = "Nap/Wake", tint = LumiGold, modifier = Modifier.size\(18.dp\)\).*?\}.*?\}"""

new_top_right_action = """// Action Icons (Nap/Wake)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.togglePetSleep() },
                        modifier = Modifier.background(SurfaceDarkVariant.copy(alpha = 0.5f), CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = if (petStatus.currentEmotion == PetEmotion.SLEEPY) Icons.Default.WbSunny else Icons.Default.Bedtime, contentDescription = "Nap/Wake", tint = LumiGold, modifier = Modifier.size(18.dp))
                    }
                }"""

content = re.sub(old_top_right_action, new_top_right_action, content, flags=re.DOTALL)

# Make the TOP LEFT HUD Glassmorphic
old_top_left = r"// Top Left: Game-Style Stats \(HP, XP & Connectivity\).*?Row\([\s\S]*?horizontalArrangement = Arrangement.spacedBy\(12.dp\)\s*\)\s*\{"

new_top_left = """// Top Left: Premium Game-Style Stats
            Surface(
                color = SurfaceDark.copy(alpha = 0.6f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceHighlight.copy(alpha = 0.3f))
            ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {"""

content = re.sub(old_top_left, new_top_left, content, flags=re.DOTALL)

# Close the new surface
old_top_left_close = r"\} // End of Top Left Row.*?// Top Right: Profile & Action Icons"
# Let's use a simpler match to close the Surface
old_top_left_end = r"\} // End of Column for stats.*?\}\s*// Top Right:"
# the original looks like:
#                        )
#                    }
#                }
#            }
#
#            // Top Right: Profile & Action Icons
# We just need to replace the last brace of the row
content = content.replace("""                    }
                }
            }

            // Top Right: Profile & Action Icons""", """                    }
                }
            }
            } // Close Surface

            // Top Right: Profile & Action Icons""")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

