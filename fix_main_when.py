import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

# I will just write a custom script to replace the entire Crossfade body!
start_str = "Crossfade("
end_str = "if (uiState.showWardrobeScreen)"

start_idx = content.find(start_str)
end_idx = content.find(end_str)

if start_idx != -1 and end_idx != -1:
    new_crossfade = """Crossfade(
                targetState = uiState.selectedTab,
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    NavDestination.PetCompanion.tabIndex -> HomeScreen(
                        petStatus = viewModel.petStatus.collectAsState().value,
                        uiState = uiState,
                        batteryStatus = viewModel.batteryStatus.collectAsState().value,
                        networkStatus = viewModel.networkStatus.collectAsState().value,
                        events = viewModel.allCalendarEvents.collectAsState().value,
                        tasks = viewModel.allTasks.collectAsState().value,
                        isListening = viewModel.voiceEngine.isListening.collectAsState().value,
                        isSpeaking = viewModel.voiceEngine.isSpeaking.collectAsState().value,
                        onPetPetted = { viewModel.onPetPetted() },
                        onPetTouched = { viewModel.onPetTouched() },
                        onTogglePetSleep = { viewModel.togglePetSleep() },
                        onStartVoiceListening = { viewModel.startVoiceListening() },
                        onStopVoiceListening = { viewModel.stopVoiceListening() },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onShowWardrobe = { viewModel.setShowWardrobeScreen(true) },
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) },
                        onNavigateToAccount = { viewModel.setSelectedTab(NavDestination.Account.tabIndex) },
                        onNavigateToWellness = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) }
                    )
                    NavDestination.Assistant.tabIndex -> ChatScreen(
                        uiState = uiState,
                        petStatus = viewModel.petStatus.collectAsState().value,
                        chatMessages = viewModel.chatMessages.collectAsState().value,
                        isListening = viewModel.voiceEngine.isListening.collectAsState().value,
                        isSpeaking = viewModel.voiceEngine.isSpeaking.collectAsState().value,
                        audioLevel = viewModel.voiceEngine.audioWaveformLevel.collectAsState().value,
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onSetInputText = { text -> viewModel.setInputText(text) },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onStartVoiceListening = { viewModel.startVoiceListening() },
                        onStopVoiceListening = { viewModel.stopVoiceListening() },
                        onToggleVoiceOutput = { viewModel.toggleVoiceOutput() }
                    )
                    NavDestination.LifeHub.tabIndex -> LifeHubScreen(
                        uiState = uiState,
                        tasks = viewModel.allTasks.collectAsState().value,
                        events = viewModel.allCalendarEvents.collectAsState().value,
                        wellnessLogs = viewModel.allWellnessLogs.collectAsState().value,
                        memories = viewModel.allMemories.collectAsState().value,
                        onSetSubTab = { viewModel.setLifeHubSubTab(it) },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )
                    NavDestination.Wellness.tabIndex -> WellnessScreen(
                        viewModel = viewModel,
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) }
                    )
                    NavDestination.Account.tabIndex -> UserAccountScreen(
                        userProfile = userProfile,
                        userFacts = viewModel.userFacts.collectAsState().value,
                        petStatus = viewModel.petStatus.collectAsState().value,
                        benchmarkStatus = viewModel.benchmarkStatus.collectAsState().value ?: "",
                        tasks = viewModel.allTasks.collectAsState().value,
                        events = viewModel.allCalendarEvents.collectAsState().value,
                        memories = viewModel.allMemories.collectAsState().value,
                        messages = viewModel.chatMessages.collectAsState().value,
                        localModelCatalog = viewModel.localModelCatalog.collectAsState().value,
                        modelDownloadStates = viewModel.modelDownloadStates.collectAsState().value,
                        activeLocalModelId = viewModel.activeLocalModelId.collectAsState().value,
                        selectedAccelerator = viewModel.selectedAccelerator.collectAsState().value,
                        onUpdateProfile = { updated -> viewModel.updateUserProfile(updated) },
                        onAddUserFact = { cat, txt, isPinned -> viewModel.addUserFact(cat, txt, isPinned) },
                        onRemoveUserFact = { id -> viewModel.removeUserFact(id) },
                        onTogglePinFact = { id -> viewModel.togglePinFact(id) },
                        onClearAiAnalytics = { viewModel.clearAiAnalytics() },
                        onDownloadLocalModel = { id -> viewModel.downloadLocalModel(id) },
                        onCancelModelDownload = { id -> viewModel.cancelModelDownload(id) },
                        onDeleteLocalModel = { id -> viewModel.deleteLocalModel(id) },
                        onSetActiveLocalModel = { id -> viewModel.setActiveLocalModel(id) },
                        onSetHardwareAccelerator = { acc -> viewModel.setHardwareAccelerator(acc) },
                        onRunGemmaBenchmark = { viewModel.runGemmaBenchmark() },
                        onNavigateToChat = { prompt ->
                            viewModel.setSelectedTab(NavDestination.Assistant.tabIndex)
                            prompt?.let { viewModel.sendMessage(it) }
                        }
                    )
                    else -> HomeScreen(
                        petStatus = viewModel.petStatus.collectAsState().value,
                        uiState = uiState,
                        batteryStatus = viewModel.batteryStatus.collectAsState().value,
                        networkStatus = viewModel.networkStatus.collectAsState().value,
                        events = viewModel.allCalendarEvents.collectAsState().value,
                        tasks = viewModel.allTasks.collectAsState().value,
                        isListening = viewModel.voiceEngine.isListening.collectAsState().value,
                        isSpeaking = viewModel.voiceEngine.isSpeaking.collectAsState().value,
                        onPetPetted = { viewModel.onPetPetted() },
                        onPetTouched = { viewModel.onPetTouched() },
                        onTogglePetSleep = { viewModel.togglePetSleep() },
                        onStartVoiceListening = { viewModel.startVoiceListening() },
                        onStopVoiceListening = { viewModel.stopVoiceListening() },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onShowWardrobe = { viewModel.setShowWardrobeScreen(true) },
                        onNavigateToChat = { viewModel.setSelectedTab(NavDestination.Assistant.tabIndex) },
                        onNavigateToLifeHub = { subTab -> viewModel.navigateToLifeHub(subTab) },
                        onNavigateToAccount = { viewModel.setSelectedTab(NavDestination.Account.tabIndex) },
                        onNavigateToWellness = { viewModel.setSelectedTab(NavDestination.Wellness.tabIndex) }
                    )
                }
            }
            """
    content = content[:start_idx] + new_crossfade + content[end_idx:]
    with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
        f.write(content)
