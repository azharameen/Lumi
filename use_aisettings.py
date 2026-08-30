with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add import if not present
if "com.example.ui.viewmodel.AiSettingsViewModel" not in content:
    content = content.replace("import com.example.ui.viewmodel.LumiViewModel", "import com.example.ui.viewmodel.LumiViewModel\nimport com.example.ui.viewmodel.AiSettingsViewModel")

# Add the viewmodel instantiation
content = content.replace("private val viewModel: LumiViewModel by viewModels()", "private val viewModel: LumiViewModel by viewModels()\n    private val aiSettingsViewModel: AiSettingsViewModel by viewModels()")

# Use the new viewmodel in UserAccountScreen
content = content.replace("localModelCatalog = viewModel.localModelCatalog", "localModelCatalog = aiSettingsViewModel.localModelCatalog")
content = content.replace("modelDownloadStates = viewModel.modelDownloadStates", "modelDownloadStates = aiSettingsViewModel.modelDownloadStates")
content = content.replace("activeLocalModelId = viewModel.activeLocalModelId", "activeLocalModelId = aiSettingsViewModel.activeLocalModelId")
content = content.replace("selectedAccelerator = viewModel.selectedAccelerator", "selectedAccelerator = aiSettingsViewModel.selectedAccelerator")
content = content.replace("onUpdateProfile = { updated -> viewModel.updateUserProfile(updated) }", "onUpdateProfile = { updated -> aiSettingsViewModel.updateUserProfile(updated) }")

content = content.replace("onDownloadLocalModel = { id -> viewModel.downloadLocalModel(id) }", "onDownloadLocalModel = { id -> aiSettingsViewModel.downloadLocalModel(id) }")
content = content.replace("onCancelModelDownload = { id -> viewModel.cancelModelDownload(id) }", "onCancelModelDownload = { id -> aiSettingsViewModel.cancelModelDownload(id) }")
content = content.replace("onPauseModelDownload = { id -> viewModel.pauseModelDownload(id) }", "onPauseModelDownload = { id -> aiSettingsViewModel.pauseModelDownload(id) }")
content = content.replace("onDeleteLocalModel = { id -> viewModel.deleteLocalModel(id) }", "onDeleteLocalModel = { id -> aiSettingsViewModel.deleteLocalModel(id) }")
content = content.replace("onSetActiveLocalModel = { id -> viewModel.setActiveLocalModel(id) }", "onSetActiveLocalModel = { id -> aiSettingsViewModel.setActiveLocalModel(id) }")
content = content.replace("onSetHardwareAccelerator = { acc -> viewModel.setHardwareAccelerator(acc) }", "onSetHardwareAccelerator = { acc -> aiSettingsViewModel.setHardwareAccelerator(acc) }")


with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

