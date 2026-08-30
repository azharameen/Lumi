import re

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

old_sig = """fun LifeHubScreen(
    uiState: com.example.ui.viewmodel.LumiUiState,
    tasks: List<com.example.data.local.entity.TaskEntity>,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    wellnessLogs: List<com.example.data.local.entity.WellnessLogEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    onSetSubTab: (Int) -> Unit,
    onNavigateToChat: (prompt: String?) -> Unit
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
    dailyBriefing: com.example.domain.briefing.DailyBriefing?,
    onSpeakBriefing: () -> Unit,
    onToggleTask: (Long, Boolean) -> Unit,
    onDeleteTask: (com.example.data.local.entity.TaskEntity) -> Unit,
    onAddTask: (String, String, String, Int, String) -> Unit,
    goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onDecomposeGoal: (String, String, String, String) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onToggleMilestone: (Long, Long, Boolean) -> Unit,
    onExecuteMilestone: (Long, Long) -> Unit,
    soundState: com.example.service.SoundscapeState,
    onStartSoundscape: (com.example.service.SoundscapeType) -> Unit,
    onStopSoundscape: () -> Unit,
    onSetVolume: (Float) -> Unit,
    onStartFocusTimer: (Int) -> Unit,
    onStopFocusTimer: () -> Unit,
    onNavigateToChat: (prompt: String?) -> Unit
) {"""

content = content.replace(old_sig, new_sig)
with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)
