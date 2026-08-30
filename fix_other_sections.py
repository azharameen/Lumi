import re

# ScheduleSection
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()
content = content.replace("onDeleteEvent(", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent(")
content = content.replace("onAddEvent(", "onAction(com.example.ui.viewmodel.LumiUiAction.AddCalendarEvent(")
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)

# TasksSection
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "r") as f:
    content = f.read()
content = content.replace("onToggleTask(", "onAction(com.example.ui.viewmodel.LumiUiAction.ToggleTask(")
content = content.replace("onDeleteTask(", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(")
content = content.replace("onAddTask(", "onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(")
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "w") as f:
    f.write(content)

