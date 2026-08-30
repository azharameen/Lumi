with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("wellnessLogs = viewModel.allWellnessLogs.collectAsStateWithLifecycle().value", "wellnessLogs = wellnessViewModel.allWellnessLogs.collectAsStateWithLifecycle().value")
content = content.replace("memories = viewModel.allMemories.collectAsStateWithLifecycle().value", "memories = wellnessViewModel.allMemories.collectAsStateWithLifecycle().value")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
