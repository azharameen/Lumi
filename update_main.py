import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

handler = """
    val handleLifeHubAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit = { action ->
        when (action) {
            is com.example.ui.viewmodel.LumiUiAction.NavigateToChat -> {
                viewModel.setSelectedTab(com.example.ui.screens.NavDestination.Assistant.tabIndex)
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
    }
"""

# Insert handler before `Scaffold(`
content = content.replace("Scaffold(", handler + "\n        Scaffold(")

# Replace the LifeHubScreen call
lifehub_call_old = """                    NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        tasks = viewModel.allTasks.collectAsStateWithLifecycle().value,
                        events = viewModel.allCalendarEvents.collectAsStateWithLifecycle().value,
                        wellnessLogs = viewModel.allWellnessLogs.collectAsStateWithLifecycle().value,
                        memories = viewModel.allMemories.collectAsStateWithLifecycle().value,
                        onSetSubTab = { viewModel.setLifeHubSubTab(it) },
                        onDeleteEvent = { id -> viewModel.deleteCalendarEvent(id) },
                        onAddEvent = { event -> viewModel.addCalendarEvent(event) },
                        dailyBriefing = viewModel.dailyBriefing.collectAsStateWithLifecycle().value,
                        onSpeakBriefing = { viewModel.speakBriefing() },
                        onToggleTask = { id, checked -> viewModel.toggleTask(id, checked) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onAddTask = { title, priority, cat, est, notes -> viewModel.addTask(title, priority, cat, est, notes) },
                        goalPlans = viewModel.allGoalPlans.collectAsStateWithLifecycle().value,
                        getMilestonesForGoal = { id -> viewModel.repository.getMilestonesForGoal(id) },
                        onDecomposeGoal = { title, desc, cat, date -> viewModel.decomposeGoal(title, desc, cat, date) },
                        onDeleteGoal = { id -> viewModel.deleteGoal(id) },
                        onToggleMilestone = { mId, gId, checked -> viewModel.toggleMilestone(mId, gId, checked) },
                        onExecuteMilestone = { mId, gId -> viewModel.executeMilestone(mId, gId) },
                        soundState = viewModel.soundscapeState.collectAsStateWithLifecycle().value,
                        onStartSoundscape = { t -> viewModel.startSoundscape(t) },
                        onStopSoundscape = { viewModel.stopSoundscape() },
                        onSetVolume = { v -> viewModel.setSoundscapeVolume(v) },
                        onStartFocusTimer = { m -> viewModel.startFocusTimerWithSoundscape(m) },
                        onStopFocusTimer = { viewModel.stopFocusTimerWithSoundscape() },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""
                    
lifehub_call_new = """                    NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        tasks = viewModel.allTasks.collectAsStateWithLifecycle().value,
                        events = viewModel.allCalendarEvents.collectAsStateWithLifecycle().value,
                        wellnessLogs = viewModel.allWellnessLogs.collectAsStateWithLifecycle().value,
                        memories = viewModel.allMemories.collectAsStateWithLifecycle().value,
                        dailyBriefing = viewModel.dailyBriefing.collectAsStateWithLifecycle().value,
                        goalPlans = viewModel.allGoalPlans.collectAsStateWithLifecycle().value,
                        getMilestonesForGoal = { id -> viewModel.repository.getMilestonesForGoal(id) },
                        soundState = viewModel.soundscapeState.collectAsStateWithLifecycle().value,
                        onAction = handleLifeHubAction
                    )"""
                    
content = content.replace(lifehub_call_old, lifehub_call_new)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

