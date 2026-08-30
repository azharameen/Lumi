import re

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

old_sig = """fun UserAccountScreen(
    userProfile: com.example.domain.account.UserProfileData,
    userFacts: List<com.example.domain.account.UserFactItem>,
    petStatus: com.example.domain.model.PetStatus,
    benchmarkStatus: String,
    tasks: List<com.example.data.local.entity.TaskEntity>,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    messages: List<com.example.data.local.entity.ChatMessageEntity>,
    onUpdateProfile: (com.example.domain.account.UserProfileData) -> Unit,
    onAddUserFact: (String, String, Boolean) -> Unit,
    onRemoveUserFact: (String) -> Unit,
    onTogglePinFact: (String) -> Unit,
    onClearAiAnalytics: () -> Unit,
    onNavigateToChat: ((String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {"""

new_sig = """fun UserAccountScreen(
    userProfile: com.example.domain.account.UserProfileData,
    userFacts: List<com.example.domain.account.UserFactItem>,
    petStatus: com.example.domain.model.PetStatus,
    benchmarkStatus: String,
    tasks: List<com.example.data.local.entity.TaskEntity>,
    events: List<com.example.data.local.entity.CalendarEventEntity>,
    memories: List<com.example.data.local.entity.PetMemoryEntity>,
    messages: List<com.example.data.local.entity.ChatMessageEntity>,
    localModelCatalog: List<com.example.data.remote.LocalModelInfo>,
    modelDownloadStates: Map<String, com.example.data.remote.ModelDownloadState>,
    activeLocalModelId: String?,
    selectedAccelerator: com.example.data.remote.HardwareAccelerator,
    onUpdateProfile: (com.example.domain.account.UserProfileData) -> Unit,
    onAddUserFact: (String, String, Boolean) -> Unit,
    onRemoveUserFact: (String) -> Unit,
    onTogglePinFact: (String) -> Unit,
    onClearAiAnalytics: () -> Unit,
    onDownloadLocalModel: (String) -> Unit,
    onCancelModelDownload: (String) -> Unit,
    onDeleteLocalModel: (String) -> Unit,
    onSetActiveLocalModel: (String) -> Unit,
    onSetHardwareAccelerator: (com.example.data.remote.HardwareAccelerator) -> Unit,
    onRunGemmaBenchmark: () -> Unit,
    onNavigateToChat: ((String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {"""

content = content.replace(old_sig, new_sig)

old_llm = """3 -> LlmSettingsSection(
                        userProfile = userProfile,
                        ,
                        benchmarkStatus = benchmarkStatus,
                        onUpdateProfile = { updated -> onUpdateProfile(updated) }
                    )"""

new_llm = """3 -> LlmSettingsSection(
                        userProfile = userProfile,
                        benchmarkStatus = benchmarkStatus,
                        localModelCatalog = localModelCatalog,
                        modelDownloadStates = modelDownloadStates,
                        activeLocalModelId = activeLocalModelId,
                        selectedAccelerator = selectedAccelerator,
                        onUpdateProfile = onUpdateProfile,
                        onDownloadLocalModel = onDownloadLocalModel,
                        onCancelModelDownload = onCancelModelDownload,
                        onDeleteLocalModel = onDeleteLocalModel,
                        onSetActiveLocalModel = onSetActiveLocalModel,
                        onSetHardwareAccelerator = onSetHardwareAccelerator,
                        onRunGemmaBenchmark = onRunGemmaBenchmark
                    )"""

content = content.replace(old_llm, new_llm)

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)
