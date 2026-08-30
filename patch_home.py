import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

# Replace TOP HUD
# The Top HUD currently has:
# Row(
#     modifier = Modifier
#         .align(Alignment.TopCenter)
#         ...

old_hud_pattern = r"// --- TOP HUD ---.*?// --- BOTTOM HUD ---"

new_hud = """// --- TOP HUD ---
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Top Left: Game-Style Stats (HP & Connectivity)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // HP / Battery Bar
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "HP", 
                            color = LumiGreen, 
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                        val batteryProgress = (batteryStatus.levelPercent / 100f).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .width(100.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(SurfaceDarkVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(batteryProgress)
                                    .height(14.dp)
                                    .background(if (batteryStatus.isCharging) LumiMint else if (batteryStatus.isLow) LumiPink else LumiGreen)
                            )
                        }
                        Text(
                            text = "${batteryStatus.levelPercent}%",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Connectivity Icon below HP
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
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
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (networkStatus.isConnected) "Online" else "Offline",
                            color = if (networkStatus.isConnected) LumiCyan else LumiPink,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Top Right: Profile & Action Icons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // User Profile Button
                Surface(
                    color = SurfaceDarkVariant,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { viewModel.setSelectedTab(NavDestination.Account.tabIndex) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = TextPrimary,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                // Action Icons (Wardrobe, Nap/Wake, Vision)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { viewModel.setShowWardrobeScreen(true) },
                        modifier = Modifier.background(SurfaceDarkVariant, CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Checkroom, contentDescription = "Wardrobe", tint = LumiPink, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { viewModel.togglePetSleep() },
                        modifier = Modifier.background(SurfaceDarkVariant, CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = if (petStatus.currentEmotion == PetEmotion.SLEEPY) Icons.Default.WbSunny else Icons.Default.Bedtime, contentDescription = "Nap/Wake", tint = LumiGold, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { viewModel.setShowCameraVision(true) },
                        modifier = Modifier.background(SurfaceDarkVariant, CircleShape).size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "Vision", tint = LumiCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // --- BOTTOM HUD ---"""

content = re.sub(old_hud_pattern, new_hud, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)

