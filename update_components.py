import re

# 1. ScheduleSection
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()

content = content.replace("onDeleteEvent: (Long) -> Unit,", "onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit,")
content = re.sub(r'onAddEvent: \(.*?\) -> Unit,\n', '', content)
content = re.sub(r'onSpeakBriefing: \(\) -> Unit,\n', '', content)
content = re.sub(r'onNavigateToChat: \(\(String\?\) -> Unit\)\? = null,', '', content)
content = content.replace("onNavigateToChat = onNavigateToChat", "onNavigateToChat = { onAction(com.example.ui.viewmodel.LumiUiAction.NavigateToChat(it)) }")
content = content.replace("onSpeakBriefing = onSpeakBriefing", "onSpeakBriefing = { onAction(com.example.ui.viewmodel.LumiUiAction.SpeakBriefing) }")
content = content.replace("onDeleteEvent(it.id)", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent(it.id))")
content = content.replace("onAddEvent(event)", "onAction(com.example.ui.viewmodel.LumiUiAction.AddCalendarEvent(event))")
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)

# 2. TasksSection
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "r") as f:
    content = f.read()

content = content.replace("onToggleTask: (Long, Boolean) -> Unit,", "onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit,")
content = re.sub(r'onDeleteTask: \(.*?\) -> Unit,\n', '', content)
content = re.sub(r'onAddTask: \(.*?\) -> Unit,\n', '', content)
content = re.sub(r'onNavigateToChat: \(\(String\?\) -> Unit\)\? = null,', '', content)

content = content.replace("onToggleTask(task.id, isChecked)", "onAction(com.example.ui.viewmodel.LumiUiAction.ToggleTask(task.id, isChecked))")
content = content.replace("onDeleteTask(task)", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(task))")
content = content.replace("onAddTask(title, prio, cat, est, desc)", "onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc))")
content = content.replace("onNavigateToChat = onNavigateToChat", "onNavigateToChat = { onAction(com.example.ui.viewmodel.LumiUiAction.NavigateToChat(it)) }")
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "w") as f:
    f.write(content)

# 3. AutonomousGoalsScreen
with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onDecomposeGoal: (String, String, String, String) -> Unit,", "onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit,")
content = re.sub(r'onDeleteGoal: \(Long\) -> Unit,\n', '', content)
content = re.sub(r'onToggleMilestone: \(Long, Long, Boolean\) -> Unit,\n', '', content)
content = re.sub(r'onExecuteMilestone: \(Long, Long\) -> Unit,\n', '', content)
content = re.sub(r'onNavigateToChat: \(\(String\?\) -> Unit\)\? = null,', '', content)

content = content.replace("onDeleteGoal(goal.id)", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteGoal(goal.id))")
content = content.replace("onDecomposeGoal(title, desc, cat, deadline)", "onAction(com.example.ui.viewmodel.LumiUiAction.DecomposeGoal(title, desc, cat, deadline))")
content = content.replace("onNavigateToChat = onNavigateToChat", "onNavigateToChat = { onAction(com.example.ui.viewmodel.LumiUiAction.NavigateToChat(it)) }")

content = content.replace("onDeleteGoal =", "// onDeleteGoal = ")
content = content.replace("onToggleMilestone =", "// onToggleMilestone = ")
content = content.replace("onExecuteMilestone =", "// onExecuteMilestone = ")
content = content.replace("onAction = onAction,", "// onAction = onAction,") # Don't accidentally duplicate
content = content.replace("goal = goal,", "goal = goal, onAction = onAction,")

content = content.replace("onToggleMilestone(milestone.id, goalId, isChecked)", "onAction(com.example.ui.viewmodel.LumiUiAction.ToggleMilestone(milestone.id, goalId, isChecked))")
content = content.replace("onExecuteMilestone(milestone.id, goalId)", "onAction(com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone(milestone.id, goalId))")

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "w") as f:
    f.write(content)

# 4. AmbientSoundscapesScreen
with open("app/src/main/java/com/example/ui/screens/lifehub/AmbientSoundscapesScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onStartSoundscape: (com.example.service.SoundscapeType) -> Unit,", "onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit,")
content = re.sub(r'onStopSoundscape: \(\) -> Unit,\n', '', content)
content = re.sub(r'onSetVolume: \(Float\) -> Unit,\n', '', content)
content = re.sub(r'onStartFocusTimer: \(Int\) -> Unit,\n', '', content)
content = re.sub(r'onStopFocusTimer: \(\) -> Unit\n', '', content)

content = content.replace("onStartSoundscape(it)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartSoundscape(it))")
content = content.replace("onStopSoundscape()", "onAction(com.example.ui.viewmodel.LumiUiAction.StopSoundscape)")
content = content.replace("onSetVolume(it)", "onAction(com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume(it))")
content = content.replace("onStartFocusTimer(m)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartFocusTimer(m))")
content = content.replace("onStopFocusTimer()", "onAction(com.example.ui.viewmodel.LumiUiAction.StopFocusTimer)")

with open("app/src/main/java/com/example/ui/screens/lifehub/AmbientSoundscapesScreen.kt", "w") as f:
    f.write(content)

