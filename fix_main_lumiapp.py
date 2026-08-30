with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel,  wellnessViewModel = wellnessViewModel)", "LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel, chatViewModel = chatViewModel, wellnessViewModel = wellnessViewModel)")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

