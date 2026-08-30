import re

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

if "import androidx.compose.material.icons.filled.PictureInPictureAlt" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Person", "import androidx.compose.material.icons.filled.Person\nimport androidx.compose.material.icons.filled.PictureInPictureAlt")

old_call = """                    4 -> PrivacyAndVaultSection(
                        userProfile = userProfile,
                        taskCount = tasks.size,
                        eventCount = events.size,
                        memoryCount = memories.size,
                        messageCount = messages.size,
                        onToggleBiometric = { isEnabled ->
                            viewModel.updateUserProfile(userProfile.copy(enableBiometricLock = isEnabled))
                        },
                        onResetClicked = { showResetDialog = true }
                    )"""

new_call = """                    4 -> PrivacyAndVaultSection(
                        userProfile = userProfile,
                        viewModel = viewModel,
                        uiState = uiState,
                        taskCount = tasks.size,
                        eventCount = events.size,
                        memoryCount = memories.size,
                        messageCount = messages.size,
                        onToggleBiometric = { isEnabled ->
                            viewModel.updateUserProfile(userProfile.copy(enableBiometricLock = isEnabled))
                        },
                        onResetClicked = { showResetDialog = true }
                    )"""

content = content.replace(old_call, new_call)

old_sig = """private fun PrivacyAndVaultSection(
    userProfile: UserProfileData,
    taskCount: Int,
    eventCount: Int,
    memoryCount: Int,
    messageCount: Int,
    onToggleBiometric: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {"""

new_sig = """private fun PrivacyAndVaultSection(
    userProfile: UserProfileData,
    viewModel: com.example.ui.viewmodel.LumiViewModel,
    uiState: com.example.ui.viewmodel.UiState,
    taskCount: Int,
    eventCount: Int,
    memoryCount: Int,
    messageCount: Int,
    onToggleBiometric: (Boolean) -> Unit,
    onResetClicked: () -> Unit
) {"""

content = content.replace(old_sig, new_sig)

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)

