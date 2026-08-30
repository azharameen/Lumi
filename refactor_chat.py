import re

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "r") as f:
    content = f.read()

old_sig = """fun ChatScreen(
    viewModel: LumiViewModel,
    modifier: Modifier = Modifier
) {"""

new_sig = """fun ChatScreen(
    uiState: com.example.ui.viewmodel.LumiUiState,
    petStatus: com.example.domain.model.PetStatus,
    chatMessages: List<com.example.data.local.entity.ChatMessage>,
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

content = re.sub(r'fun ChatScreen\s*\([^)]*viewModel:\s*LumiViewModel[^)]*\)\s*\{', new_sig, content, flags=re.MULTILINE)

removals = [
    r'\s*val uiState by viewModel\.uiState\.collectAsState\(\)',
    r'\s*val petStatus by viewModel\.petStatus\.collectAsState\(\)',
    r'\s*val messages by viewModel\.chatMessages\.collectAsState\(\)',
    r'\s*val isVoiceEngineListening by viewModel\.voiceEngine\.isListening\.collectAsState\(\)',
    r'\s*val isVoiceEngineSpeaking by viewModel\.voiceEngine\.isSpeaking\.collectAsState\(\)',
    r'import com\.example\.ui\.viewmodel\.LumiViewModel'
]

for r in removals:
    content = re.sub(r, "", content)

# For messages, the existing variable is called `messages` but I named it `chatMessages` in the signature.
content = content.replace("messages.size", "chatMessages.size")
content = content.replace("items(messages)", "items(chatMessages)")

replacements = {
    "viewModel.sendMessage(inputText)": "onSendMessage(inputText)",
    "viewModel.sendMessage(text)": "onSendMessage(text)",
    "viewModel.setInputText(text)": "onSetInputText(text)",
    "viewModel.setInputText(\"\")": "onSetInputText(\"\")",
    "viewModel.setShowCamera(true)": "onShowCamera()",
    "viewModel.startVoiceListening()": "onStartVoiceListening()",
    "viewModel.stopVoiceListening()": "onStopVoiceListening()",
    "viewModel.toggleVoiceOutput()": "onToggleVoiceOutput()",
    "viewModel": "null /* viewModel removed */"
}

for old, new in replacements.items():
    if old != "viewModel":
        content = content.replace(old, new)

with open("app/src/main/java/com/example/ui/screens/ChatScreen.kt", "w") as f:
    f.write(content)
print("Done ChatScreen")
