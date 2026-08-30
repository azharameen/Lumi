import re

# Update LifeHubScreen
with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

sig_old = """    onSetSubTab: (Int) -> Unit,
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
    onNavigateToChat: (prompt: String?) -> Unit"""
sig_new = """    dailyBriefing: com.example.domain.briefing.DailyBriefing?,
    goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    soundState: com.example.service.SoundscapeState,
    onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit"""

content = content.replace(sig_old, sig_new)

# Now update the implementations inside LifeHubScreen where we dispatch to subcomponents.
content = content.replace("onSetSubTab(index)", "onAction(com.example.ui.viewmodel.LumiUiAction.SetLifeHubSubTab(index))")

tasks_call_old = """                    1 -> TasksSection(
                        tasks = tasks,
                        onToggleTask = onToggleTask,
                        onDeleteTask = onDeleteTask,
                        onAddTask = onAddTask,
                        
                        onNavigateToChat = onNavigateToChat
                    )"""
tasks_call_new = """                    1 -> TasksSection(
                        tasks = tasks,
                        onAction = onAction
                    )"""
content = content.replace(tasks_call_old, tasks_call_new)

goals_call_old = """                    2 -> AutonomousGoalsScreen(
                        goalPlans = goalPlans,
                        getMilestonesForGoal = getMilestonesForGoal,
                        onDecomposeGoal = onDecomposeGoal,
                        onDeleteGoal = onDeleteGoal,
                        onToggleMilestone = onToggleMilestone,
                        onExecuteMilestone = onExecuteMilestone,
                        
                        onNavigateToChat = onNavigateToChat
                    )"""
goals_call_new = """                    2 -> AutonomousGoalsScreen(
                        goalPlans = goalPlans,
                        getMilestonesForGoal = getMilestonesForGoal,
                        onAction = onAction
                    )"""
content = content.replace(goals_call_old, goals_call_new)

sounds_call_old = """                    3 -> AmbientSoundscapesScreen(
                        soundState = soundState,
                        onStartSoundscape = onStartSoundscape,
                        onStopSoundscape = onStopSoundscape,
                        onSetVolume = onSetVolume,
                        onStartFocusTimer = onStartFocusTimer,
                        onStopFocusTimer = onStopFocusTimer
                    )"""
sounds_call_new = """                    3 -> AmbientSoundscapesScreen(
                        soundState = soundState,
                        onAction = onAction
                    )"""
content = content.replace(sounds_call_old, sounds_call_new)

schedule_call_old = """                    else -> ScheduleSection(
                        events = events,
                        onDeleteEvent = onDeleteEvent,
                        onAddEvent = onAddEvent,
                        dailyBriefing = dailyBriefing,
                        onSpeakBriefing = onSpeakBriefing,
                        
                        onNavigateToChat = onNavigateToChat
                    )"""
schedule_call_new = """                    else -> ScheduleSection(
                        events = events,
                        dailyBriefing = dailyBriefing,
                        onAction = onAction
                    )"""
content = content.replace(schedule_call_old, schedule_call_new)

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)

