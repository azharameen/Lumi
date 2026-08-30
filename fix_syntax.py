import re

# 1. AutonomousGoalsScreen
with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "r") as f:
    content = f.read()

# Fix Decompose missing )
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.DecomposeGoal(title, description, category, targetDate)", "onAction(com.example.ui.viewmodel.LumiUiAction.DecomposeGoal(title, description, category, targetDate))")

# Fix MilestoneItemRow signature
content = content.replace("    goalId: Long,\n        onExecuteMilestone: (Long, Long) -> Unit", "    goalId: Long,\n    onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit")

# Fix MilestoneItemRow call
old_milestone_call = """                        MilestoneItemRow(
                            milestone = milestone,
                            goalId = goal.id,
                            // onToggleMilestone =  onToggleMilestone,
                            // onExecuteMilestone =  onExecuteMilestone
                        )"""
new_milestone_call = """                        MilestoneItemRow(
                            milestone = milestone,
                            goalId = goal.id,
                            onAction = onAction
                        )"""
content = content.replace(old_milestone_call, new_milestone_call)

# Fix Toggle missing )
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.ToggleMilestone(milestone.id, goalId, !milestone.isCompleted)", "onAction(com.example.ui.viewmodel.LumiUiAction.ToggleMilestone(milestone.id, goalId, !milestone.isCompleted))")

# Fix Execute missing )
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone(milestone.id, goalId)", "onAction(com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone(milestone.id, goalId))")

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "w") as f:
    f.write(content)


# 2. ScheduleSection
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()

content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent(it.id)", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent(it.id))")
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.AddCalendarEvent(event)", "onAction(com.example.ui.viewmodel.LumiUiAction.AddCalendarEvent(event))")

with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)


# 3. TasksSection
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "r") as f:
    content = f.read()

content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.ToggleTask(task.id, isChecked)", "onAction(com.example.ui.viewmodel.LumiUiAction.ToggleTask(task.id, isChecked))")
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(task)", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteTask(task))")
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc)", "onAction(com.example.ui.viewmodel.LumiUiAction.AddTask(title, prio, cat, est, desc))")

with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "w") as f:
    f.write(content)


# 4. AmbientSoundscapesScreen
with open("app/src/main/java/com/example/ui/screens/AmbientSoundscapesScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.StartSoundscape(it)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartSoundscape(it))")
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume(it)", "onAction(com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume(it))")
content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.StartFocusTimer(m)", "onAction(com.example.ui.viewmodel.LumiUiAction.StartFocusTimer(m))")

with open("app/src/main/java/com/example/ui/screens/AmbientSoundscapesScreen.kt", "w") as f:
    f.write(content)

