import re

# Fix LlmSettingsSection.kt
with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    text = f.read()

text = text.replace("com.example.domain.model.DownloadStatus.DOWNLOADED", "com.example.data.remote.ModelDownloadStatus.DOWNLOADED")
text = text.replace("com.example.domain.model.DownloadStatus.DOWNLOADING", "com.example.data.remote.ModelDownloadStatus.DOWNLOADING")

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.write(text)

# Fix UserAccountScreen.kt
with open('app/src/main/java/com/example/presentation/screens/UserAccountScreen.kt', 'r') as f:
    text = f.read()

target = """fun UserAccountScreen(
    userProfile: com.example.domain.account.UserProfileData,"""
replacement = """fun UserAccountScreen(
    userProfile: com.example.domain.account.UserProfileData,
    haptics: com.example.core.utils.LumiHaptics = com.example.core.utils.rememberLumiHaptics(isEnabled = userProfile.enableHapticFeedback),"""
text = text.replace(target, replacement)

# What if it's already there? or it's formatted differently
target_backup = "fun UserAccountScreen("
if "haptics" not in text.split("fun UserAccountScreen")[1].split("{")[0]:
    # Fallback to replace the first line inside the function body
    pass 

with open('app/src/main/java/com/example/presentation/screens/UserAccountScreen.kt', 'w') as f:
    f.write(text)

