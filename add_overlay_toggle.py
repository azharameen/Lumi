import re

# 1. Update PrivacyAndVaultSection.kt
with open("app/src/main/java/com/example/ui/screens/account/PrivacyAndVaultSection.kt", "r") as f:
    content = f.read()

sig_old = """fun PrivacyAndVaultSection(
    userProfile: UserProfileData,
    taskCount: Int,
    eventCount: Int,
    memoryCount: Int,
    messageCount: Int,
    onToggleBiometric: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {"""
sig_new = """fun PrivacyAndVaultSection(
    userProfile: UserProfileData,
    taskCount: Int,
    eventCount: Int,
    memoryCount: Int,
    messageCount: Int,
    onToggleBiometric: (Boolean) -> Unit,
    isOverlayEnabled: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {"""
content = content.replace(sig_old, sig_new)

# Add ToggleSettingRow for Overlay under "Security & Biometric Access" card or create a new card for "System Permissions"
overlay_card = """
            // System Permissions
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "System Permissions",
                        color = LumiPink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ToggleSettingRow(
                        title = "Floating Pet Overlay",
                        subtitle = "Allow Lumi to float over other apps (Draw over other apps)",
                        icon = Icons.Default.Layers,
                        isChecked = isOverlayEnabled,
                        onCheckedChange = onToggleOverlay,
                        iconTint = LumiPink
                    )
                }
            }
"""
content = content.replace("            // Security Card", overlay_card + "\n            // Security Card")

with open("app/src/main/java/com/example/ui/screens/account/PrivacyAndVaultSection.kt", "w") as f:
    f.write(content)

# 2. Update UserAccountScreen.kt
with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

acct_sig_old = """    onRunGemmaBenchmark: () -> Unit,
    onNavigateToChat: ((String?) -> Unit)? = null,"""
acct_sig_new = """    onRunGemmaBenchmark: () -> Unit,
    isOverlayEnabled: Boolean,
    onToggleOverlay: (Boolean) -> Unit,
    onNavigateToChat: ((String?) -> Unit)? = null,"""
content = content.replace(acct_sig_old, acct_sig_new)

vault_call_old = """                    4 -> PrivacyAndVaultSection(
                        userProfile = userProfile,
                        taskCount = tasks.size,
                        eventCount = events.size,
                        memoryCount = memories.size,
                        messageCount = messages.size,
                        onToggleBiometric = { /* TODO */ },
                        onResetClicked = { showResetDialog = true }
                    )"""
vault_call_new = """                    4 -> PrivacyAndVaultSection(
                        userProfile = userProfile,
                        taskCount = tasks.size,
                        eventCount = events.size,
                        memoryCount = memories.size,
                        messageCount = messages.size,
                        onToggleBiometric = { /* TODO */ },
                        isOverlayEnabled = isOverlayEnabled,
                        onToggleOverlay = onToggleOverlay,
                        onResetClicked = { showResetDialog = true }
                    )"""
content = content.replace(vault_call_old, vault_call_new)

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)

# 3. Update MainActivity.kt
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

main_call_old = """                        onRunGemmaBenchmark = { viewModel.runGemmaBenchmark() },
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) }"""
main_call_new = """                        onRunGemmaBenchmark = { viewModel.runGemmaBenchmark() },
                        isOverlayEnabled = uiState.isOverlayEnabled,
                        onToggleOverlay = { if (it) viewModel.setShowOverlayPermission(true) else viewModel.setOverlayEnabled(false) },
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) }"""
content = content.replace(main_call_old, main_call_new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

