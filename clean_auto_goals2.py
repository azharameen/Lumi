with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "r") as f:
    content = f.read()

old_sig = """@Composable
private fun MilestoneItemRow(
    milestone: GoalMilestoneEntity,
    goalId: Long,
    goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onDecomposeGoal: (String, String, String, String) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onToggleMilestone: (Long, Long, Boolean) -> Unit,
    onExecuteMilestone: (Long, Long) -> Unit
) {"""

new_sig = """@Composable
private fun MilestoneItemRow(
    milestone: GoalMilestoneEntity,
    goalId: Long,
    onToggleMilestone: (Long, Long, Boolean) -> Unit,
    onExecuteMilestone: (Long, Long) -> Unit
) {"""
content = content.replace(old_sig, new_sig)

# Clean up call site
old_call = """MilestoneItemRow(
                            milestone = milestone,
                            goalId = goal.id,
                            getMilestonesForGoal = getMilestonesForGoal,
                            onDeleteGoal = onDeleteGoal,
                            onToggleMilestone = onToggleMilestone,
                            onExecuteMilestone = onExecuteMilestone
                        )"""

new_call = """MilestoneItemRow(
                            milestone = milestone,
                            goalId = goal.id,
                            onToggleMilestone = onToggleMilestone,
                            onExecuteMilestone = onExecuteMilestone
                        )"""

content = content.replace(old_call, new_call)

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "w") as f:
    f.write(content)

