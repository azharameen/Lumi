with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

content = content.replace("null /* viewModel removed */: LumiViewModel,", "uiState: com.example.ui.viewmodel.LumiUiState,\n    tasks: List<com.example.data.local.entity.TaskEntity>,\n    events: List<com.example.data.local.entity.CalendarEventEntity>,\n    wellnessLogs: List<com.example.data.local.entity.WellnessLogEntity>,\n    memories: List<com.example.data.local.entity.PetMemoryEntity>,\n    onSetSubTab: (Int) -> Unit,")

content = content.replace("val events by null /* viewModel removed */.allCalendarEvents.collectAsState()", "")
content = content.replace("val tasks by null /* viewModel removed */.allTasks.collectAsState()", "")
content = content.replace("val wellnessLogs by null /* viewModel removed */.allWellnessLogs.collectAsState()", "")
content = content.replace("val memories by null /* viewModel removed */.allMemories.collectAsState()", "")

# Fix uiState
content = content.replace("null /* viewModel removed */.uiState", "uiState")

# Fix onSetSubTab
content = content.replace("null /* viewModel removed */.setLifeHubSubTab(", "onSetSubTab(")

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Update LifeHubScreen call in MainActivity
old_life = """NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        onSetSubTab = { viewModel.setLifeHubSubTab(it) },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""
new_life = """NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        tasks = viewModel.allTasks.collectAsState().value,
                        events = viewModel.allCalendarEvents.collectAsState().value,
                        wellnessLogs = viewModel.allWellnessLogs.collectAsState().value,
                        memories = viewModel.allMemories.collectAsState().value,
                        onSetSubTab = { viewModel.setLifeHubSubTab(it) },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )"""

content = content.replace(old_life, new_life)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
