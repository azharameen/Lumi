import os

def replace_in_file(filepath, replacements):
    with open(filepath, "r") as f:
        content = f.read()
    for old, new in replacements.items():
        content = content.replace(old, new)
    with open(filepath, "w") as f:
        f.write(content)

reps = {
    "LocalModelInfo": "LocalLlmModelSpec",
    "ModelDownloadState": "ModelDownloadProgress"
}

replace_in_file("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt", reps)
replace_in_file("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", reps)
replace_in_file("app/src/main/java/com/example/MainActivity.kt", reps)
