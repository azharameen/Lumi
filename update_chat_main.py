with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    main_content = f.read()

old_chat = """NavDestination.Assistant.tabIndex -> ChatScreen(
                        viewModel = viewModel
                    )"""

new_chat = """NavDestination.Assistant.tabIndex -> ChatScreen(
                        uiState = uiState,
                        petStatus = viewModel.petStatus.collectAsState().value,
                        chatMessages = viewModel.chatMessages.collectAsState().value,
                        isVoiceEngineListening = viewModel.voiceEngine.isListening.collectAsState().value,
                        isVoiceEngineSpeaking = viewModel.voiceEngine.isSpeaking.collectAsState().value,
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onSetInputText = { text -> viewModel.setInputText(text) },
                        onShowCamera = { viewModel.setShowCamera(true) },
                        onStartVoiceListening = { viewModel.startVoiceListening() },
                        onStopVoiceListening = { viewModel.stopVoiceListening() },
                        onToggleVoiceOutput = { viewModel.toggleVoiceOutput() }
                    )"""

main_content = main_content.replace(old_chat, new_chat)

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(main_content)

