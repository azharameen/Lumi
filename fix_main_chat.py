with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("chatViewModel = chatViewModel,", "")
content = content.replace("chatMessages = viewModel.chatMessages.collectAsStateWithLifecycle().value,", "chatMessages = chatViewModel.chatMessages.collectAsStateWithLifecycle().value,")
content = content.replace("onSendMessage = { text -> viewModel.sendMessage(text) },", "onSendMessage = { text -> chatViewModel.sendMessage(text) },")
content = content.replace("isListening = viewModel.voiceEngine.isListening.collectAsStateWithLifecycle().value,", "isListening = chatViewModel.voiceEngine.isListening.collectAsStateWithLifecycle().value,")
content = content.replace("isSpeaking = viewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle().value,", "isSpeaking = chatViewModel.voiceEngine.isSpeaking.collectAsStateWithLifecycle().value,")
content = content.replace("onStartVoiceListening = { viewModel.startVoiceListening() },", "onStartVoiceListening = { chatViewModel.startVoiceListening() },")
content = content.replace("onStopVoiceListening = { viewModel.stopVoiceListening() },", "onStopVoiceListening = { chatViewModel.stopVoiceListening() },")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
