with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "r") as f:
    content = f.read()

content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.ToggleTask(task.id, checked)", "onAction(com.example.ui.viewmodel.LumiUiAction.ToggleTask(task.id, checked))")
# Also fix add task if missing )
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc)", "onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc))")
# Also fix delete task if missing )
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(task)", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(task))")


with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "w") as f:
    f.write(content)
