with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onAction(com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone(milestone.id, goalId)))", "onAction(com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone(milestone.id, goalId))")

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "w") as f:
    f.write(content)
