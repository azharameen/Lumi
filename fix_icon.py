import re

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'r') as f:
    text = f.read()

text = text.replace("androidx.compose.material.icons.Icons.Default.Info", "androidx.compose.material.icons.filled.Info")

with open('app/src/main/java/com/example/presentation/screens/account/LlmSettingsSection.kt', 'w') as f:
    f.write(text)
