import re

with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "r") as f:
    content = f.read()

# Add collector for voiceEngine.isSpeaking
old_init = """    init {
        sensorsManager.startListening("""
new_init = """    init {
        viewModelScope.launch {
            voiceEngine.isSpeaking.collect { isSpeaking ->
                repository.setSpeaking(isSpeaking)
            }
        }
        sensorsManager.startListening("""
content = content.replace(old_init, new_init)

with open("app/src/main/java/com/example/ui/viewmodel/LumiViewModel.kt", "w") as f:
    f.write(content)
