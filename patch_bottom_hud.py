import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

old_bottom_hud = r"// --- BOTTOM HUD ---.*?\}\s*\}\s*@Composable"

new_bottom_hud = """// --- BOTTOM HUD ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Bottom Left: Productivity & Wellness
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
    }
}

@Composable"""

content = re.sub(old_bottom_hud, new_bottom_hud, content, flags=re.DOTALL)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
