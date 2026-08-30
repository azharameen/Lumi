import re

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "r") as f:
    content = f.read()

# Replace the signature
old_sig = """fun UserAccountScreen(
    viewModel: LumiViewModel,
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
    memories: List<com.example.data.local.entity.MemoryEntity>,
    messages: List<com.example.data.local.entity.ChatMessage>,
    onUpdateProfile: (com.example.domain.account.UserProfileData) -> Unit,
    onAddUserFact: (String, String, Boolean) -> Unit,
    onRemoveUserFact: (String) -> Unit,
    onTogglePinFact: (String) -> Unit,
    onClearAiAnalytics: () -> Unit,
    onNavigateToChat: ((String?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {"""

if old_sig in content:
    content = content.replace(old_sig, new_sig)
else:
    # Handle possible formatting differences
    content = re.sub(
        r'fun UserAccountScreen\s*\([^)]*viewModel:\s*LumiViewModel[^)]*\)\s*\{',
        new_sig,
        content,
        flags=re.MULTILINE
    )

# Remove the state collections
state_removals = [
    r'\s*val userProfile by viewModel\.userProfile\.collectAsState\(\)',
    r'\s*val userFacts by viewModel\.userFacts\.collectAsState\(\)',
    r'\s*val petStatus by viewModel\.petStatus\.collectAsState\(\)',
    r'\s*val benchmarkStatus by viewModel\.benchmarkStatus\.collectAsState\(\)',
    r'\s*val tasks by viewModel\.allTasks\.collectAsState\(\)',
    r'\s*val events by viewModel\.allCalendarEvents\.collectAsState\(\)',
    r'\s*val memories by viewModel\.allMemories\.collectAsState\(\)',
    r'\s*val messages by viewModel\.chatMessages\.collectAsState\(\)',
    r'import com\.example\.ui\.viewmodel\.LumiViewModel'
]

for removal in state_removals:
    content = re.sub(removal, "", content)

# Replace viewModel calls
replacements = {
    "viewModel.updateUserProfile(": "onUpdateProfile(",
    "viewModel.addUserFact(": "onAddUserFact(",
    "viewModel.removeUserFact(": "onRemoveUserFact(",
    "viewModel.togglePinFact(": "onTogglePinFact(",
    "viewModel.clearAiAnalytics()": "onClearAiAnalytics()",
    "viewModel": "null /* viewModel removed */"
}

for old, new in replacements.items():
    if old != "viewModel":
        content = content.replace(old, new)

with open("app/src/main/java/com/example/ui/screens/UserAccountScreen.kt", "w") as f:
    f.write(content)
print("Done")
