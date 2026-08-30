with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# Add imports
if "LifeHubViewModel" not in content:
    content = content.replace("import com.example.ui.viewmodel.WellnessViewModel", "import com.example.ui.viewmodel.WellnessViewModel\nimport com.example.ui.viewmodel.LifeHubViewModel\nimport com.example.ui.viewmodel.PetViewModel")

# Add viewmodels
content = content.replace("private val wellnessViewModel: WellnessViewModel by viewModels()", "private val wellnessViewModel: WellnessViewModel by viewModels()\n    private val lifeHubViewModel: LifeHubViewModel by viewModels()\n    private val petViewModel: PetViewModel by viewModels()")

content = content.replace("fun LumiApp(viewModel: LumiViewModel, aiSettingsViewModel: AiSettingsViewModel, chatViewModel: ChatViewModel, wellnessViewModel: WellnessViewModel) {", "fun LumiApp(viewModel: LumiViewModel, aiSettingsViewModel: AiSettingsViewModel, chatViewModel: ChatViewModel, wellnessViewModel: WellnessViewModel, lifeHubViewModel: LifeHubViewModel, petViewModel: PetViewModel) {")

content = content.replace("LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel, chatViewModel = chatViewModel, wellnessViewModel = wellnessViewModel)", "LumiApp(viewModel = viewModel, aiSettingsViewModel = aiSettingsViewModel, chatViewModel = chatViewModel, wellnessViewModel = wellnessViewModel, lifeHubViewModel = lifeHubViewModel, petViewModel = petViewModel)")

# Update pet usages globally
content = content.replace("petStatus = viewModel.petStatus.collectAsStateWithLifecycle().value", "petStatus = petViewModel.petStatus.collectAsStateWithLifecycle().value")
content = content.replace("petPrimary = androidx.compose.ui.graphics.Color(viewModel.petStatus.collectAsStateWithLifecycle().value.bloubSkinColor.primaryHex)", "petPrimary = androidx.compose.ui.graphics.Color(petViewModel.petStatus.collectAsStateWithLifecycle().value.bloubSkinColor.primaryHex)")
content = content.replace("petSecondary = androidx.compose.ui.graphics.Color(viewModel.petStatus.collectAsStateWithLifecycle().value.bloubSkinColor.endHex)", "petSecondary = androidx.compose.ui.graphics.Color(petViewModel.petStatus.collectAsStateWithLifecycle().value.bloubSkinColor.endHex)")

# Note: The above is for when the petState is read globally, wait... In `setContent`, it currently uses `viewModel.petStatus`
content = content.replace("val petStatus by viewModel.petStatus.collectAsStateWithLifecycle()", "val petStatus by petViewModel.petStatus.collectAsStateWithLifecycle()")

# Update HomeScreen
content = content.replace("tasks = viewModel.allTasks.collectAsStateWithLifecycle().value,", "tasks = lifeHubViewModel.allTasks.collectAsStateWithLifecycle().value,")
content = content.replace("events = viewModel.allCalendarEvents.collectAsStateWithLifecycle().value,", "events = lifeHubViewModel.allCalendarEvents.collectAsStateWithLifecycle().value,")
content = content.replace("onPetPetted = { viewModel.onPetPetted() }", "onPetPetted = { petViewModel.onPetPetted() }")
content = content.replace("onPetTouched = { viewModel.onPetTouched() }", "onPetTouched = { petViewModel.onPetTouched() }")
content = content.replace("onTogglePetSleep = { viewModel.togglePetSleep() }", "onTogglePetSleep = { petViewModel.togglePetSleep() }")

# Update LifeHubScreen
content = content.replace("NavDestination.LifeHub.tabIndex -> LifeHubScreen(", "NavDestination.LifeHub.tabIndex -> LifeHubScreen(\n                        lifeHubViewModel = lifeHubViewModel,")
content = content.replace("dailyBriefing = viewModel.dailyBriefing.collectAsStateWithLifecycle().value,", "dailyBriefing = lifeHubViewModel.dailyBriefing.collectAsStateWithLifecycle().value,")
content = content.replace("goalPlans = viewModel.allGoalPlans.collectAsStateWithLifecycle().value,", "goalPlans = lifeHubViewModel.allGoalPlans.collectAsStateWithLifecycle().value,")
content = content.replace("getMilestonesForGoal = { id -> viewModel.repository.getMilestonesForGoal(id) },", "getMilestonesForGoal = { id -> lifeHubViewModel.getMilestonesForGoal(id) },")
content = content.replace("soundState = viewModel.soundscapeState.collectAsStateWithLifecycle().value,", "soundState = lifeHubViewModel.soundscapeState.collectAsStateWithLifecycle().value,")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)

