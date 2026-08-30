with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_action = """    val handleLifeHubAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit = { action ->
        when (action) {
            is com.example.ui.viewmodel.LumiUiAction.NavigateToChat -> {
                viewModel.setSelectedTab(com.example.ui.navigation.NavDestination.Assistant.tabIndex)
                action.prompt?.let { viewModel.sendMessage(it) }
            }
            is com.example.ui.viewmodel.LumiUiAction.SetLifeHubSubTab -> viewModel.setLifeHubSubTab(action.tabIndex)
            is com.example.ui.viewmodel.LumiUiAction.AddCalendarEvent -> viewModel.addCalendarEvent(action.event)
            is com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent -> viewModel.deleteCalendarEvent(action.id)
            is com.example.ui.viewmodel.LumiUiAction.SpeakBriefing -> viewModel.speakBriefing()
            is com.example.ui.viewmodel.LumiUiAction.AddTask -> viewModel.addTask(action.title, action.priority, action.category, action.estimatedMinutes, action.notes)
            is com.example.ui.viewmodel.LumiUiAction.ToggleTask -> viewModel.toggleTask(action.id, action.isCompleted)
            is com.example.ui.viewmodel.LumiUiAction.DeleteTask -> viewModel.deleteTask(action.task)
            is com.example.ui.viewmodel.LumiUiAction.DecomposeGoal -> viewModel.decomposeGoal(action.title, action.description, action.category, action.deadline)
            is com.example.ui.viewmodel.LumiUiAction.DeleteGoal -> viewModel.deleteGoal(action.id)
            is com.example.ui.viewmodel.LumiUiAction.ToggleMilestone -> viewModel.toggleMilestone(action.milestoneId, action.goalId, action.isCompleted)
            is com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone -> viewModel.executeMilestone(action.milestoneId, action.goalId)
            is com.example.ui.viewmodel.LumiUiAction.StartSoundscape -> viewModel.startSoundscape(action.type)
            is com.example.ui.viewmodel.LumiUiAction.StopSoundscape -> viewModel.stopSoundscape()
            is com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume -> viewModel.setSoundscapeVolume(action.volume)
            is com.example.ui.viewmodel.LumiUiAction.StartFocusTimer -> viewModel.startFocusTimerWithSoundscape(action.minutes)
            is com.example.ui.viewmodel.LumiUiAction.StopFocusTimer -> viewModel.stopFocusTimerWithSoundscape()
        }
    }"""

new_action = """    val handleLifeHubAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit = { action ->
        when (action) {
            is com.example.ui.viewmodel.LumiUiAction.NavigateToChat -> {
                viewModel.setSelectedTab(com.example.ui.navigation.NavDestination.Assistant.tabIndex)
                action.prompt?.let { chatViewModel.sendMessage(it) }
            }
            is com.example.ui.viewmodel.LumiUiAction.SetLifeHubSubTab -> viewModel.setLifeHubSubTab(action.tabIndex)
            is com.example.ui.viewmodel.LumiUiAction.AddCalendarEvent -> lifeHubViewModel.addCalendarEvent(action.event)
            is com.example.ui.viewmodel.LumiUiAction.DeleteCalendarEvent -> lifeHubViewModel.deleteCalendarEvent(action.id)
            is com.example.ui.viewmodel.LumiUiAction.SpeakBriefing -> viewModel.speakBriefing() // Remains in LumiViewModel for now if voice output
            is com.example.ui.viewmodel.LumiUiAction.AddTask -> lifeHubViewModel.addTask(action.title, action.priority, action.category, action.estimatedMinutes, action.notes)
            is com.example.ui.viewmodel.LumiUiAction.ToggleTask -> lifeHubViewModel.toggleTask(action.id, action.isCompleted)
            is com.example.ui.viewmodel.LumiUiAction.DeleteTask -> lifeHubViewModel.deleteTask(action.task)
            is com.example.ui.viewmodel.LumiUiAction.DecomposeGoal -> lifeHubViewModel.decomposeGoal(action.title, action.description, action.category, action.deadline)
            is com.example.ui.viewmodel.LumiUiAction.DeleteGoal -> lifeHubViewModel.deleteGoal(action.id)
            is com.example.ui.viewmodel.LumiUiAction.ToggleMilestone -> lifeHubViewModel.toggleMilestone(action.milestoneId, action.goalId, action.isCompleted)
            is com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone -> lifeHubViewModel.executeMilestone(action.milestoneId, action.goalId)
            is com.example.ui.viewmodel.LumiUiAction.StartSoundscape -> lifeHubViewModel.startSoundscape(action.type)
            is com.example.ui.viewmodel.LumiUiAction.StopSoundscape -> lifeHubViewModel.stopSoundscape()
            is com.example.ui.viewmodel.LumiUiAction.SetSoundscapeVolume -> lifeHubViewModel.setSoundscapeVolume(action.volume)
            is com.example.ui.viewmodel.LumiUiAction.StartFocusTimer -> lifeHubViewModel.startFocusTimerWithSoundscape(action.minutes)
            is com.example.ui.viewmodel.LumiUiAction.StopFocusTimer -> lifeHubViewModel.stopFocusTimerWithSoundscape()
        }
    }"""

content = content.replace(old_action, new_action)

# Fix lifeHubViewModel injection in LifeHubScreen call
content = content.replace("NavDestination.LifeHub.tabIndex -> LifeHubScreen(\n                        lifeHubViewModel = lifeHubViewModel,", "NavDestination.LifeHub.tabIndex -> LifeHubScreen(")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
