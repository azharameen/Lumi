import re

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "r") as f:
    content = f.read()

old_sig = """fun HomeScreen(
    viewModel: LumiViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToLifeHub: (subTab: Int) -> Unit
) {"""

new_sig = """fun HomeScreen(
    petStatus: com.example.domain.model.PetStatus,
    uiState: com.example.ui.viewmodel.LumiUiState,
    batteryStatus: com.example.service.BatteryStatus,
    networkStatus: com.example.service.NetworkStatus,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    tasks: List<com.example.data.local.entity.TaskEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    onPetPetted: () -> Unit,
    onPetTouched: () -> Unit,
    onTogglePetSleep: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onShowCamera: () -> Unit,
    onShowWardrobe: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToLifeHub: (Int) -> Unit
) {"""

content = re.sub(r'fun HomeScreen\s*\([^)]*viewModel:\s*LumiViewModel[^)]*\)\s*\{', new_sig, content, flags=re.MULTILINE)

removals = [
    r'\s*val petStatus by viewModel\.petStatus\.collectAsState\(\)',
    r'\s*val uiState by viewModel\.uiState\.collectAsState\(\)',
    r'\s*val batteryStatus by viewModel\.batteryStatus\.collectAsState\(\)',
    r'\s*val networkStatus by viewModel\.networkStatus\.collectAsState\(\)',
    r'\s*val tasks by viewModel\.allTasks\.collectAsState\(\)',
    r'\s*val events by viewModel\.allCalendarEvents\.collectAsState\(\)',
    r'\s*val isListening by viewModel\.voiceEngine\.isListening\.collectAsState\(\)',
    r'\s*val isSpeaking by viewModel\.voiceEngine\.isSpeaking\.collectAsState\(\)',
    r'import com\.example\.ui\.viewmodel\.LumiViewModel'
]

for r in removals:
    content = re.sub(r, "", content)

replacements = {
    "viewModel.onPetPetted()": "onPetPetted()",
    "viewModel.onPetTouched()": "onPetTouched()",
    "viewModel.togglePetSleep()": "onTogglePetSleep()",
    "viewModel.startVoiceListening()": "onStartVoiceListening()",
    "viewModel.stopVoiceListening()": "onStopVoiceListening()",
    "viewModel.setShowCamera(true)": "onShowCamera()",
    "viewModel.setShowWardrobeScreen(true)": "onShowWardrobe()",
    "viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)": "onNavigateToChat()",
    "viewModel.navigateToLifeHub(2)": "onNavigateToLifeHub(2)",
    "viewModel.navigateToLifeHub(1)": "onNavigateToLifeHub(1)",
    "viewModel.navigateToLifeHub(0)": "onNavigateToLifeHub(0)",
    "viewModel": "null /* viewModel removed */"
}

for old, new in replacements.items():
    if old != "viewModel":
        content = content.replace(old, new)

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
print("Done HomeScreen")
