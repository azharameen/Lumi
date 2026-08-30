with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Update UserAccountScreen in MainActivity
content = content.replace("tasks = viewModel.allTasks.collectAsStateWithLifecycle().value,", "tasks = lifeHubViewModel.allTasks.collectAsStateWithLifecycle().value,")
content = content.replace("events = viewModel.allCalendarEvents.collectAsStateWithLifecycle().value,", "events = lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle().value,")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
