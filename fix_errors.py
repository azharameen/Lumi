import re

# Fix HomeScreenComponents.kt
with open('app/src/main/java/com/example/presentation/home/components/HomeScreenComponents.kt', 'r') as f:
    lines = f.readlines()
for i in range(len(lines)):
    if "fun MinimalPetSpeechCard(" in lines[i]:
        pass

# Fix UserAccountScreen.kt
with open('app/src/main/java/com/example/presentation/screens/UserAccountScreen.kt', 'r') as f:
    text = f.read()
if "val haptics = com.example.core.utils.rememberLumiHaptics()" not in text:
    text = text.replace("    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()", "    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()\n    val haptics = com.example.core.utils.rememberLumiHaptics(isEnabled = userProfile.enableHapticFeedback)")
    with open('app/src/main/java/com/example/presentation/screens/UserAccountScreen.kt', 'w') as f:
        f.write(text)

# Fix LlmSettingsSection.kt
with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    text = f.read()

text = text.replace("com.example.data.remote.DownloadStatus.DOWNLOADED", "com.example.domain.model.DownloadStatus.DOWNLOADED")
text = text.replace("com.example.data.remote.DownloadStatus.DOWNLOADING", "com.example.domain.model.DownloadStatus.DOWNLOADING")
text = text.replace("Icons.Default.Info", "androidx.compose.material.icons.Icons.Default.Info")

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.write(text)
