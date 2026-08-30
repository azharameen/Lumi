import re

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "r") as f:
    content = f.read()

# Fix GoalPlanItemCard signature
sig_old = """private fun GoalPlanItemCard(
    goal: GoalPlanEntity,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
            onExecuteMilestone: (Long, Long) -> Unit
) {"""
sig_new = """private fun GoalPlanItemCard(
    goal: GoalPlanEntity,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onAction: (com.example.ui.viewmodel.LumiUiAction) -> Unit
) {"""
content = content.replace(sig_old, sig_new)

# In case it looks different
sig_old_2 = """private fun GoalPlanItemCard(
    goal: GoalPlanEntity,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onDeleteGoal: (Long) -> Unit,
    onToggleMilestone: (Long, Long, Boolean) -> Unit,
    onExecuteMilestone: (Long, Long) -> Unit
) {"""
content = content.replace(sig_old_2, sig_new)

# Remove the commented out things from the item card invocation
call_old = """                    GoalPlanItemCard(
                        goal = goal, onAction = onAction,
                        getMilestonesForGoal = getMilestonesForGoal,
                            // onDeleteGoal =  onDeleteGoal,
                            // onToggleMilestone =  onToggleMilestone,
                            // onExecuteMilestone =  onExecuteMilestone
                    )"""
call_new = """                    GoalPlanItemCard(
                        goal = goal,
                        getMilestonesForGoal = getMilestonesForGoal,
                        onAction = onAction
                    )"""
content = content.replace(call_old, call_new)

# Fix onDecomposeGoal missing unresolved reference
# It might be in the TopHeader or somewhere.
content = content.replace("onDecomposeGoal(", "onAction(com.example.ui.viewmodel.LumiUiAction.DecomposeGoal(")
content = content.replace("onDeleteGoal(", "onAction(com.example.ui.viewmodel.LumiUiAction.DeleteGoal(")
content = content.replace("onToggleMilestone(", "onAction(com.example.ui.viewmodel.LumiUiAction.ToggleMilestone(")
content = content.replace("onExecuteMilestone(", "onAction(com.example.ui.viewmodel.LumiUiAction.ExecuteMilestone(")

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "w") as f:
    f.write(content)
