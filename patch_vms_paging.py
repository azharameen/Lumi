with open("app/src/main/java/com/example/ui/viewmodel/ChatViewModel.kt", "r") as f:
    content = f.read()

if "val pagedChatMessages = repository.pagedChatMessages.cachedIn(viewModelScope)" not in content:
    content = content.replace("import kotlinx.coroutines.flow.stateIn", "import kotlinx.coroutines.flow.stateIn\nimport androidx.paging.cachedIn")
    content = content.replace("val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages.stateIn(", "val pagedChatMessages = repository.pagedChatMessages.cachedIn(viewModelScope)\n\n    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages.stateIn(")

with open("app/src/main/java/com/example/ui/viewmodel/ChatViewModel.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/ui/viewmodel/WellnessViewModel.kt", "r") as f:
    content = f.read()

if "val pagedWellnessLogs = repository.pagedWellnessLogs.cachedIn(viewModelScope)" not in content:
    content = content.replace("import kotlinx.coroutines.flow.stateIn", "import kotlinx.coroutines.flow.stateIn\nimport androidx.paging.cachedIn")
    content = content.replace("val allWellnessLogs: StateFlow<List<WellnessLogEntity>> = repository.allWellnessLogs.stateIn(", "val pagedWellnessLogs = repository.pagedWellnessLogs.cachedIn(viewModelScope)\n\n    val allWellnessLogs: StateFlow<List<WellnessLogEntity>> = repository.allWellnessLogs.stateIn(")

with open("app/src/main/java/com/example/ui/viewmodel/WellnessViewModel.kt", "w") as f:
    f.write(content)
