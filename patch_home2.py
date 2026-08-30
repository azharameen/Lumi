import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# 1. Update the Top Left game-style stats
old_top_left = r"// Top Left: Game-Style Stats \(HP & Connectivity\).*?// Top Right: Profile & Action Icons"

new_top_left = """// Top Left: Game-Style Stats (HP, XP & Connectivity)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status Bars
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Level
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = SurfaceDarkVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Lv.${petStatus.level}",
                                color = LumiCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // HP / Battery Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HP", 
                            color = LumiGreen, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.width(20.dp)
                        )
                        val batteryProgress = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceDarkVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(batteryProgress)
                                    .height(10.dp)
                                    .background(if (batteryStatus.isCharging) LumiMint else if (batteryStatus.isLow) LumiPink else LumiGreen)
                            )
                        }
                    }
                    
                    // XP Bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "XP", 
                            color = LumiViolet, 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.width(20.dp)
                        )
                        val xpProgress = (petStatus.exp.toFloat() / petStatus.expToNextLevel).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .width(90.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceDarkVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(xpProgress)
                                    .height(10.dp)
                                    .background(LumiViolet)
                            )
                        }
                    }

                    // Connectivity Icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        val netIcon = when (networkStatus.type) {
                            NetworkType.WIFI -> Icons.Default.Wifi
                            NetworkType.CELLULAR -> Icons.Default.SignalCellularAlt
                            NetworkType.ETHERNET -> Icons.Default.Wifi
                            NetworkType.OFFLINE -> Icons.Default.WifiOff
                        }
                        Icon(
                            imageVector = netIcon,
                            contentDescription = "Connection",
                            tint = if (networkStatus.isConnected) LumiCyan else LumiPink,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (networkStatus.isConnected) "Online" else "Offline",
                            color = if (networkStatus.isConnected) LumiCyan else LumiPink,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top Right: Profile & Action Icons"""

content = re.sub(old_top_left, new_top_left, content, flags=re.DOTALL)

# 2. Remove Camera from Top Right Action Icons
old_top_right = r"""                    IconButton\(
                        onClick = \{ viewModel.setShowCamera\(true\) \},
                        modifier = Modifier.background\(SurfaceDarkVariant, CircleShape\).size\(36.dp\)
                    \) \{
                        Icon\(imageVector = Icons.Default.Visibility.*?\}
"""

content = re.sub(old_top_right, "", content, flags=re.DOTALL)

# 3. Add the action row below LumiPetView
old_pet_view = r"modifier = Modifier.testTag\(\"lumi_pet_view\"\)\s*\)\s*\}\s*\}"

new_pet_view = """modifier = Modifier.testTag("lumi_pet_view")
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Voice Mode Cancel / Chat / Camera Row
                val isListening by viewModel.voiceEngine.isListening.collectAsState()
                val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedVisibility(visible = isListening || isSpeaking) {
                        IconButton(
                            onClick = { viewModel.stopVoiceListening() },
                            modifier = Modifier.background(SurfaceDarkVariant, CircleShape).size(48.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel Voice", tint = LumiPink, modifier = Modifier.size(24.dp))
                        }
                    }
                    IconButton(
                        onClick = onNavigateToChat,
                        modifier = Modifier.background(SurfaceDarkVariant, CircleShape).size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = "Text Chat", tint = LumiCyan, modifier = Modifier.size(24.dp))
                    }
                    IconButton(
                        onClick = { viewModel.setShowCamera(true) },
                        modifier = Modifier.background(SurfaceDarkVariant, CircleShape).size(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Vision", tint = LumiGreen, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }"""

content = re.sub(old_pet_view, new_pet_view, content, flags=re.DOTALL)

# 4. Modify BOTTOM HUD to be in a row
old_bottom_hud = r"// --- BOTTOM HUD ---.*?Row\([\s\S]*?horizontalArrangement = Arrangement.SpaceBetween,.*?// Bottom Left: Productivity & Wellness.*?Column\(verticalArrangement = Arrangement.spacedBy\(12.dp\)\) \{.*?HudButton\([\s\S]*?onClick = \{ viewModel.setSelectedTab\(NavDestination.LifeHub.tabIndex\) \}.*?\).*?HudButton\([\s\S]*?onClick = \{ viewModel.setSelectedTab\(NavDestination.Wellness.tabIndex\) \}.*?\).*?\}\s*\}\s*\}"

new_bottom_hud = """// --- BOTTOM HUD ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HudButton(
                icon = Icons.Default.CheckCircle,
                label = "Life Hub",
                color = LumiYellow,
                onClick = { viewModel.setSelectedTab(NavDestination.LifeHub.tabIndex) }
            )
            HudButton(
                icon = Icons.Default.SelfImprovement,
                label = "Wellness",
                color = LumiPink,
                onClick = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) }
            )
        }
    }
}"""

content = re.sub(old_bottom_hud, new_bottom_hud, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
