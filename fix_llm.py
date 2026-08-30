import re

with open("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt", "r") as f:
    content = f.read()

old_sig = """fun LlmSettingsSection(
    userProfile: UserProfileData,
    viewModel: LumiViewModel,
    benchmarkStatus: String?,
    onUpdateProfile: (UserProfileData) -> Unit
) {"""

new_sig = """fun LlmSettingsSection(
    userProfile: UserProfileData,
    benchmarkStatus: String?,
    localModelCatalog: List<com.example.data.remote.LocalModelInfo>,
    modelDownloadStates: Map<String, com.example.data.remote.ModelDownloadState>,
    activeLocalModelId: String?,
    selectedAccelerator: com.example.data.remote.HardwareAccelerator,
    onUpdateProfile: (UserProfileData) -> Unit,
    onDownloadLocalModel: (String) -> Unit,
    onCancelModelDownload: (String) -> Unit,
    onDeleteLocalModel: (String) -> Unit,
    onSetActiveLocalModel: (String) -> Unit,
    onSetHardwareAccelerator: (com.example.data.remote.HardwareAccelerator) -> Unit,
    onRunGemmaBenchmark: () -> Unit
) {"""

content = content.replace(old_sig, new_sig)

removals = [
    r'\s*val localModelCatalog by viewModel\.localModelCatalog\.collectAsState\(\)',
    r'\s*val downloadStates by viewModel\.modelDownloadStates\.collectAsState\(\)',
    r'\s*val activeLocalModelId by viewModel\.activeLocalModelId\.collectAsState\(\)',
    r'\s*val selectedAccelerator by viewModel\.selectedAccelerator\.collectAsState\(\)',
    r'import com\.example\.ui\.viewmodel\.LumiViewModel'
]

for r in removals:
    content = re.sub(r, "", content)

# I also need to rename downloadStates to modelDownloadStates where it's used
content = content.replace("downloadStates[", "modelDownloadStates[")

replacements = {
    "viewModel.downloadLocalModel(": "onDownloadLocalModel(",
    "viewModel.cancelModelDownload(": "onCancelModelDownload(",
    "viewModel.deleteLocalModel(": "onDeleteLocalModel(",
    "viewModel.setActiveLocalModel(": "onSetActiveLocalModel(",
    "viewModel.setHardwareAccelerator(": "onSetHardwareAccelerator(",
    "viewModel.runGemmaBenchmark()": "onRunGemmaBenchmark()",
    "viewModel": "null"
}

for old, new in replacements.items():
    if old != "viewModel":
        content = content.replace(old, new)

with open("app/src/main/java/com/example/ui/screens/account/LlmSettingsSection.kt", "w") as f:
    f.write(content)
