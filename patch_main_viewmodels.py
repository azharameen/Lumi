with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add imports
if "ChatViewModel" not in content:
    content = content.replace("import com.example.ui.viewmodel.LumiViewModel", "import com.example.ui.viewmodel.LumiViewModel\nimport com.example.ui.viewmodel.ChatViewModel\nimport com.example.ui.viewmodel.WellnessViewModel")

# Add viewmodels
content = content.replace("private val aiSettingsViewModel: AiSettingsViewModel by viewModels()", "private val aiSettingsViewModel: AiSettingsViewModel by viewModels()\n    private val chatViewModel: ChatViewModel by viewModels()\n    private val wellnessViewModel: WellnessViewModel by viewModels()")

content = content.replace("fun LumiApp(viewModel: LumiViewModel, aiSettingsViewModel: AiSettingsViewModel) {", "fun LumiApp(viewModel: LumiViewModel, aiSettingsViewModel: AiSettingsViewModel, chatViewModel: ChatViewModel, wellnessViewModel: WellnessViewModel) {")
content = content.replace("LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel)", "LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel, chatViewModel = chatViewModel, wellnessViewModel = wellnessViewModel)")

# Update screens
content = content.replace("NavDestination.Assistant.tabIndex -> ChatScreen(", "NavDestination.Assistant.tabIndex -> ChatScreen(\n                        chatViewModel = chatViewModel,")
content = content.replace("NavDestination.Wellness.tabIndex -> WellnessScreen(\n                        viewModel = viewModel,", "NavDestination.Wellness.tabIndex -> WellnessScreen(\n                        viewModel = wellnessViewModel,\n                        appViewModel = viewModel,")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
