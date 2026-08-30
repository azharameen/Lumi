import re

# ScheduleSection
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel: LumiViewModel,", "onDeleteEvent: (Long) -> Unit,\n    onAddEvent: (com.example.data.local.entity.CalendarEventEntity) -> Unit,")
content = content.replace("viewModel.deleteCalendarEvent(", "onDeleteEvent(")
content = content.replace("viewModel.addCalendarEvent(", "onAddEvent(")
content = content.replace("import com.example.ui.viewmodel.LumiViewModel", "")

with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)

# TasksSection
with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel: LumiViewModel,", "onToggleTask: (Long, Boolean) -> Unit,\n    onDeleteTask: (com.example.data.local.entity.TaskEntity) -> Unit,\n    onAddTask: (String, String, String, Int, String) -> Unit,")
content = content.replace("viewModel.toggleTask(", "onToggleTask(")
content = content.replace("viewModel.deleteTask(", "onDeleteTask(")
content = content.replace("viewModel.addTask(", "onAddTask(")
content = content.replace("import com.example.ui.viewmodel.LumiViewModel", "")

with open("app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt", "w") as f:
    f.write(content)

# LifeHubScreen - replace `null /* viewModel removed */ = null /* viewModel removed */,` with the callbacks
with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

content = content.replace("null /* viewModel removed */ = null /* viewModel removed */,", "")

# We need to add the callbacks to LifeHubScreen signature!
old_sig = """fun LifeHubScreen(
    uiState: com.example.ui.viewmodel.LumiUiState,
    tasks: List<com.example.data.local.entity.TaskEntity>,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    wellnessLogs: List<com.example.data.local.entity.WellnessLogEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    onSetSubTab: (Int) -> Unit,
    onNavigateToChat: (String?) -> Unit
) {"""

new_sig = """fun LifeHubScreen(
    uiState: com.example.ui.viewmodel.LumiUiState,
    tasks: List<com.example.data.local.entity.TaskEntity>,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    wellnessLogs: List<com.example.data.local.entity.WellnessLogEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    onSetSubTab: (Int) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onAddEvent: (com.example.data.local.entity.CalendarEventEntity) -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (com.example.data.local.entity.TaskEntity) -> Unit,
    onAddTask: (String, String, String, Int, String) -> Unit,
    onNavigateToChat: (String?) -> Unit
) {"""

content = content.replace(old_sig, new_sig)

# Update ScheduleSection calls
content = content.replace("events = events,", "events = events,\n                        onDeleteEvent = onDeleteEvent,\n                        onAddEvent = onAddEvent,")

# Update TasksSection calls
content = content.replace("tasks = tasks,", "tasks = tasks,\n                        onToggleTask = onToggleTask,\n                        onDeleteTask = onDeleteTask,\n                        onAddTask = onAddTask,")

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)

# Update MainActivity.kt for LifeHubScreen
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

old_life = """onSetSubTab = { viewModel.setLifeHubSubTab(it) },"""
new_life = """onSetSubTab = { viewModel.setLifeHubSubTab(it) },
                        onDeleteEvent = { id -> viewModel.deleteCalendarEvent(id) },
                        onAddEvent = { event -> viewModel.addCalendarEvent(event) },
                        onToggleTask = { id, checked -> viewModel.toggleTask(id, checked) },
                        onDeleteTask = { task -> viewModel.deleteTask(task) },
                        onAddTask = { title, priority, cat, est, notes -> viewModel.addTask(title, priority, cat, est, notes) },"""

main_content = main_content.replace(old_life, new_life)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)

