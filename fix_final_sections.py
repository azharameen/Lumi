import re
import glob
import os

files_to_fix = [
    "app/src/main/java/com/example/ui/screens/lifehub/ScheduleSection.kt",
    "app/src/main/java/com/example/ui/screens/lifehub/TasksSection.kt",
    "app/src/main/java/com/example/ui/screens/AutonomousGoalsScreen.kt",
    "app/src/main/java/com/example/ui/screens/AmbientSoundscapesScreen.kt"
]

for filepath in files_to_fix:
    with open(filepath, "r") as f:
        content = f.read()
    
    # Remove any lingering onNavigateToChat parameters
    content = re.sub(r',\s*onNavigateToChat:\s*\(\(String\?\) -> Unit\)\?\s*=\s*null', '', content)
    content = re.sub(r'onNavigateToChat:\s*\(\(String\?\) -> Unit\)\?\s*=\s*null,', '', content)
    content = re.sub(r'onNavigateToChat:\s*\(String\?\)\s*->\s*Unit', '', content)
    content = re.sub(r',\s*onNavigateToChat:\s*\(String\?\)\s*->\s*Unit', '', content)
    
    with open(filepath, "w") as f:
        f.write(content)

# Fix NavDestination in MainActivity
with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()
if "import com.example.ui.screens.NavDestination" not in content:
    content = content.replace("import com.example.ui.screens.UserAccountScreen", "import com.example.ui.screens.UserAccountScreen\nimport com.example.ui.screens.NavDestination")
with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

