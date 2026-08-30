with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "r") as f:
    content = f.read()

# Remove modelDownloadManager and related state
content = content.replace("val modelDownloadManager = com.example.data.remote.ModelDownloadManager.getInstance(application)", "")
content = content.replace("val localModelCatalog = modelDownloadManager.catalog\n    val modelDownloadStates = modelDownloadManager.downloadStates\n    val activeLocalModelId = modelDownloadManager.activeModelId\n    val selectedAccelerator = modelDownloadManager.selectedAccelerator\n", "")

funcs_to_remove = [
    """    fun downloadLocalModel(modelId: String) {
        sensorsManager.vibrateTap()
        modelDownloadManager.downloadModel(modelId)
    }""",
    """    fun cancelModelDownload(modelId: String) { modelDownloadManager.cancelDownload(modelId) }""",
    """    fun pauseModelDownload(modelId: String) { modelDownloadManager.pauseDownload(modelId) }""",
    """    fun deleteLocalModel(modelId: String) {
        sensorsManager.vibrateTap()
        modelDownloadManager.deleteModel(modelId)
    }""",
    """    fun setActiveLocalModel(modelId: String) {
        sensorsManager.vibrateTap()
        modelDownloadManager.setActiveModel(modelId)
    }""",
    """    fun setHardwareAccelerator(accelerator: com.example.data.remote.HardwareAccelerator) {
        sensorsManager.vibrateTap()
        modelDownloadManager.setAccelerator(accelerator)
    }""",
    """    fun updateUserProfile(profile: com.example.domain.account.UserProfileData) {
        viewModelScope.launch { userProfileManager.updateProfile(profile) }
    }"""
]

for func in funcs_to_remove:
    content = content.replace(func, "")

with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "w") as f:
    f.write(content)

