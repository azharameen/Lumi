with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel.refreshDailyBriefing(type)", "lifeHubViewModel.refreshDailyBriefing(type, petViewModel.petStatus.value, petViewModel.petEvolution.value, wellnessViewModel.allWellnessLogs.value)")
content = content.replace("is com.example.ui.viewmodel.LumiUiAction.SpeakBriefing -> viewModel.speakBriefing()", "is com.example.ui.viewmodel.LumiUiAction.SpeakBriefing -> {} // Removed voice briefing output for now")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
