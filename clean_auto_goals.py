with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "r") as f:
    content = f.read()

# Fix GoalPlanItemCard Signature
old_sig = """private fun GoalPlanItemCard(
    goal: GoalPlanEntity,
    goalPlans: List<com.example.data.local.entity.GoalPlanEntity>,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onDecomposeGoal: (String, String, String, String) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    onToggleMilestone: (Long, Long, Boolean) -> Unit,
    onExecuteMilestone: (Long, Long) -> Unit
) {"""

new_sig = """private fun GoalPlanItemCard(
    goal: GoalPlanEntity,
    getMilestonesForGoal: (Long) -> kotlinx.coroutines.flow.Flow<List<com.example.data.local.entity.GoalMilestoneEntity>>,
    onDeleteGoal: (Long) -> Unit,
    onToggleMilestone: (Long, Long, Boolean) -> Unit,
    onExecuteMilestone: (Long, Long) -> Unit
) {"""

content = content.replace(old_sig, new_sig)

with open("app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt", "w") as f:
    f.write(content)

