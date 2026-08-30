with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("fun LumiApp(viewModel: LumiViewModel) {", "fun LumiApp(viewModel: LumiViewModel, aiSettingsViewModel: AiSettingsViewModel) {")
content = content.replace("LumiApp(viewModel = viewModel)", "LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel)")
content = content.replace("val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()", "val userProfile by aiSettingsViewModel.userProfile.collectAsStateWithLifecycle()")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

