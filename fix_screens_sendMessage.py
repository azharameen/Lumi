with open("app/src/main/java/com/example/ui/screens/ScheduleScreen.kt", "r") as f:
    content = f.read()
content = content.replace("onNavigateToChat: () -> Unit", "onNavigateToChat: (String?) -> Unit")
content = content.replace('viewModel.sendMessage("Analyze my calendar events for today and suggest the optimal productive schedule")', 'onNavigateToChat("Analyze my calendar events for today and suggest the optimal productive schedule")')
content = content.replace("onNavigateToChat()", "onNavigateToChat(null)")
with open("app/src/main/java/com/example/ui/screens/ScheduleScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/TasksScreen.kt", "r") as f:
    content = f.read()
content = content.replace("onNavigateToChat: () -> Unit", "onNavigateToChat: (String?) -> Unit")
content = content.replace('viewModel.sendMessage("Review my current tasks list and prioritize them by urgency and impact with actionable time blocks")', 'onNavigateToChat("Review my current tasks list and prioritize them by urgency and impact with actionable time blocks")')
content = content.replace("onNavigateToChat()", "onNavigateToChat(null)")
with open("app/src/main/java/com/example/ui/screens/TasksScreen.kt", "w") as f:
    f.write(content)
