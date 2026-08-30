import re

# OnboardingScreen
with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.runtime.*", "import androidx.compose.runtime.*\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle")
content = content.replace("userProfile.personaTone", "userProfile.personaTone")
content = content.replace("userProfile.userName", "userProfile.userName")
content = content.replace("userProfile.primaryFocusGoal", "userProfile.primaryFocusGoal")
# Wait, userProfile might be null?
content = content.replace("val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()", "val userProfile by viewModel.userProfile.collectAsStateWithLifecycle(initialValue = com.example.domain.account.UserProfileData())")

with open("app/src/main/java/com/example/ui/screens/OnboardingScreen.kt", "w") as f:
    f.write(content)

# MainActivity
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()
if "import com.example.ui.screens.NavDestination" not in content:
    content = content.replace("import com.example.ui.screens.UserAccountScreen", "import com.example.ui.screens.UserAccountScreen\nimport com.example.ui.screens.NavDestination")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

# ScheduleSection
with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "r") as f:
    content = f.read()
content = content.replace("onNavigateToChat(null)", "onAction(com.example.ui.viewmodel.LumiUiAction.NavigateToChat(null))")

with open("app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt", "w") as f:
    f.write(content)

