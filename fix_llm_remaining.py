with open("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel.localModelCatalog", "localModelCatalog")
content = content.replace("import com.example.data.remote.HardwareAccelerator", "import com.example.data.remote.HardwareAccelerator\nimport com.example.data.remote.ModelDownloadStatus")

with open("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt", "w") as f:
    f.write(content)
