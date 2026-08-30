import re

def fix_fqn(filepath):
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace("com.example.data.remote.LocalLlmModelSpec", "LocalLlmModelSpec")
    with open(filepath, "w") as f:
        f.write(content)

fix_fqn("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt")
fix_fqn("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt")
