def add_imports(filepath):
    with open(filepath, "r") as f:
        content = f.read()
    
    imports = [
        "import com.example.data.remote.LocalModelInfo",
        "import com.example.data.remote.ModelDownloadState",
        "import com.example.data.remote.HardwareAccelerator"
    ]
    for imp in imports:
        if imp not in content:
            content = content.replace("import com.example.ui.components.*", f"import com.example.ui.components.*\n{imp}")
    
    with open(filepath, "w") as f:
        f.write(content)

add_imports("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt")
add_imports("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt")
