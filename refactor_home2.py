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
    onNavigateToLifeHub: (Int) -> Unit,
    onNavigateToAccount: () -> Unit,
    onNavigateToWellness: () -> Unit
) {"""

content = content.replace(old_sig, new_sig)

# We also need to fix the remaining viewModel calls in the modifier
content = content.replace("viewModel.setSelectedTab(NavDestination.Account.tabIndex)", "onNavigateToAccount()")
content = content.replace("viewModel.setSelectedTab(NavDestination.LifeHub.tabIndex)", "onNavigateToLifeHub(0)")
content = content.replace("viewModel.setSelectedTab(NavDestination.Wellness.tabIndex)", "onNavigateToWellness()")

with open("app/src/main/java/com/example/ui/screens/HomeScreen.kt", "w") as f:
    f.write(content)
