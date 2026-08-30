import re
with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'r') as f:
    text = f.read()

# Replace startVoiceListening
old_start = r'fun startVoiceListening\(\) \{.*?\}'
new_start = '''fun startVoiceListening() { 
        viewModelScope.launch { repository.setListening(true) }
        voiceEngine.startListening { text -> 
            viewModelScope.launch { repository.setListening(false) }
            if (text.isNotBlank()) {
                sendMessageToAi(text)
            }
        } 
    }'''

text = re.sub(old_start, new_start, text, flags=re.DOTALL)

# Replace stopVoiceListening
old_stop = r'fun stopVoiceListening\(\) \{ voiceEngine.stopListening\(\) \}'
new_stop = '''fun stopVoiceListening() { 
        viewModelScope.launch { repository.setListening(false) }
        voiceEngine.stopListening() 
    }'''
text = re.sub(old_stop, new_stop, text, flags=re.DOTALL)

with open('app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt', 'w') as f:
    f.write(text)
