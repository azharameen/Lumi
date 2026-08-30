def fix_file(filepath):
    with open(filepath, "r") as f:
        content = f.read()
    content = content.replace("import LocalLlmModelSpec", "import com.example.data.remote.LocalLlmModelSpec")
    with open(filepath, "w") as f:
        f.write(content)

fix_file("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt")
fix_file("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt")
