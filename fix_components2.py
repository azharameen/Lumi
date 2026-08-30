with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()
content = content.replace("onClick = { onAction(com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent(event.id) }", "onClick = { onAction(com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent(event.id)) }")
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)


with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "r") as f:
    content = f.read()
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(task)))) }", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(task)) }")
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc))))", "onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc))")
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "w") as f:
    f.write(content)

