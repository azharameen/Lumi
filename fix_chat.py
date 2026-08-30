with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

# Update signature to include audioLevel
old_sig = """fun ChatScreen(
    uiState: com.example.ui.viewmodel.LumiUiState,
    petStatus: com.example.domain.model.PetStatus,
    chatMessages: List<com.example.data.local.entity.ChatMessageEntity>,
    isVoiceEngineListening: Boolean,
    isVoiceEngineSpeaking: Boolean,
    onSendMessage: (String) -> Unit,
    onSetInputText: (String) -> Unit,
    onShowCamera: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onToggleVoiceOutput: () -> Unit,
    modifier: Modifier = Modifier
) {"""

new_sig = """fun ChatScreen(
    uiState: com.example.ui.viewmodel.LumiUiState,
    petStatus: com.example.domain.model.PetStatus,
    chatMessages: List<com.example.data.local.entity.ChatMessageEntity>,
    isListening: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float,
    onSendMessage: (String) -> Unit,
    onSetInputText: (String) -> Unit,
    onShowCamera: () -> Unit,
    onStartVoiceListening: () -> Unit,
    onStopVoiceListening: () -> Unit,
    onToggleVoiceOutput: () -> Unit,
    modifier: Modifier = Modifier
) {"""

content = content.replace(old_sig, new_sig)

# Remove the state collections
lines = content.split('\n')
new_lines = []
for line in lines:
    if "by viewModel." in line and "collectAsState()" in line:
        continue
    new_lines.append(line)

content = '\n'.join(new_lines)

# Fix some remaining imports
content = content.replace("List<com.example.data.local.entity.ChatMessage>", "List<com.example.data.local.entity.ChatMessageEntity>")
content = content.replace("ChatMessage(", "ChatMessageEntity(")

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

content = content.replace("isVoiceEngineListening", "isListening")
content = content.replace("isVoiceEngineSpeaking", "isSpeaking")
content = content.replace("isSpeaking = viewModel.voiceEngine.isSpeaking.collectAsState().value,", "isSpeaking = viewModel.voiceEngine.isSpeaking.collectAsState().value,\n                        audioLevel = viewModel.voiceEngine.audioWaveformLevel.collectAsState().value,")

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)
