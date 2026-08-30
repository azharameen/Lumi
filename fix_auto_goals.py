with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "r") as f:
    content = f.read()

content = content.replace("viewModel: LumiViewModel", "goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,\n    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,\n    onDecomposeGoal: (String, String, String, String) -> Unit,\n    onDeleteGoal: (Long) -> Unit,\n    onToggleMilestone: (Long, Long, Boolean) -> Unit,\n    onExecuteMilestone: (Long, Long) -> Unit")

content = content.replace("val goalPlans by viewModel.allGoalPlans.collectAsState()", "")
content = content.replace("viewModel = viewModel", "getMilestonesForGoal = getMilestonesForGoal,\n                            onDeleteGoal = onDeleteGoal,\n                            onToggleMilestone = onToggleMilestone,\n                            onExecuteMilestone = onExecuteMilestone")

content = content.replace("viewModel.decomposeGoal(", "onDecomposeGoal(")
content = content.replace("viewModel.deleteGoal(", "onDeleteGoal(")
content = content.replace("viewModel.toggleMilestone(", "onToggleMilestone(")
content = content.replace("viewModel.executeMilestone(", "onExecuteMilestone(")
content = content.replace("viewModel.repository.getMilestonesForGoal(", "getMilestonesForGoal(")

content = content.replace("import com.example.ui.viewmodel.LumiViewModel", "")

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "r") as f:
    content = f.read()

content = content.replace("onStopFocusTimer: () -> Unit,", "onStopFocusTimer: () -> Unit,\n    goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,\n    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,\n    onDecomposeGoal: (String, String, String, String) -> Unit,\n    onDeleteGoal: (Long) -> Unit,\n    onToggleMilestone: (Long, Long, Boolean) -> Unit,\n    onExecuteMilestone: (Long, Long) -> Unit,")

content = content.replace("2 -> AutonomousGoalsScreen(", "2 -> AutonomousGoalsScreen(\n                        goalPlans = goalPlans,\n                        getMilestonesForGoal = getMilestonesForGoal,\n                        onDecomposeGoal = onDecomposeGoal,\n                        onDeleteGoal = onDeleteGoal,\n                        onToggleMilestone = onToggleMilestone,\n                        onExecuteMilestone = onExecuteMilestone,")

with open("app/src/main/java/com/example/ui/screens/LifeHubScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("onStopFocusTimer = { viewModel.stopFocusTimerWithSoundscape() },", "onStopFocusTimer = { viewModel.stopFocusTimerWithSoundscape() },\n                        goalPlans = viewModel.allGoalPlans.collectAsState().value,\n                        getMilestonesForGoal = { id -> viewModel.repository.getMilestonesForGoal(id) },\n                        onDecomposeGoal = { title, desc, cat, date -> viewModel.decomposeGoal(title, desc, cat, date) },\n                        onDeleteGoal = { id -> viewModel.deleteGoal(id) },\n                        onToggleMilestone = { mId, gId, checked -> viewModel.toggleMilestone(mId, gId, checked) },\n                        onExecuteMilestone = { mId, gId -> viewModel.executeMilestone(mId, gId) },")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
