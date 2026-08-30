# Fix MainActivity
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("import com.example.ui.screens.NavDestination", "")
content = content.replace("com.example.ui.screens.NavDestination.Assistant.tabIndex", "com.example.ui.navigation.NavDestination.Assistant.tabIndex")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

# Fix ScheduleSection
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()

content = content.replace("onNavigateToChat(\"Please help me optimize and plan my schedule for today.\")", "onAction(com.example.ui.viewmodel.LumiUiAction.NavigateToChat(\"Please help me optimize and plan my schedule for today.\"))")

with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)

