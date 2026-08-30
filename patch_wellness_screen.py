with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel: com.example.ui.viewmodel.LumiViewModel,", "viewModel: com.example.ui.viewmodel.WellnessViewModel,\n    appViewModel: com.example.ui.viewmodel.LumiViewModel,")
content = content.replace("val uiState by viewModel.uiState.collectAsStateWithLifecycle()", "val uiState by appViewModel.uiState.collectAsStateWithLifecycle()")
content = content.replace("appViewModel.setShowBreathing", "appViewModel.setShowBreathing")
content = content.replace("onClick = { viewModel.setShowBreathing(true) }", "onClick = { appViewModel.setShowBreathing(true) }")
content = content.replace("onClick = { viewModel.lockMemoryVault() }", "onClick = { appViewModel.lockMemoryVault() }")
content = content.replace("onClick = { viewModel.unlockMemoryVault() }", "onClick = { appViewModel.unlockMemoryVault() }")

with open("app/src/main/java/com/example/ui/screens/WellnessScreen.kt", "w") as f:
    f.write(content)
