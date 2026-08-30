with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "r") as f:
    content = f.read()

content = content.replace("fun cancelModelDownload(modelId: String) { modelDownloadManager.cancelDownload(modelId) }", "fun cancelModelDownload(modelId: String) { modelDownloadManager.cancelDownload(modelId) }\n    fun pauseModelDownload(modelId: String) { modelDownloadManager.pauseDownload(modelId) }")

with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "w") as f:
    f.write(content)

