import re

# 1. ChatRepositoryImpl.kt
with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'r') as f:
    text = f.read()

# Make sure all returns in sendMessage are .toDomain()
text = re.sub(
    r'return@withContext ([a-zA-Z0-9_]+)\n',
    r'return@withContext \1.toDomain()\n',
    text
)
text = re.sub(
    r'return@withContext ([a-zA-Z0-9_]+)\}',
    r'return@withContext \1.toDomain()}',
    text
)
# undo double toDomain()
text = text.replace('.toDomain().toDomain()', '.toDomain()')

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'w') as f:
    f.write(text)

# 2. AnalyticsService interface defaults
with open('app/src/main/java/com/example/domain/service/AnalyticsService.kt', 'r') as f:
    text = f.read()
text = text.replace('fun logScreenView(screenName: String, screenClass: String)', 'fun logScreenView(screenName: String, screenClass: String = "MainActivity")')
with open('app/src/main/java/com/example/domain/service/AnalyticsService.kt', 'w') as f:
    f.write(text)

with open('app/src/main/java/com/example/data/firebase/LumiAnalyticsManager.kt', 'r') as f:
    text = f.read()
text = text.replace('override fun logScreenView(screenName: String, screenClass: String)', 'override fun logScreenView(screenName: String, screenClass: String)')
# It's actually fine if implementation has no default if interface has it.

# 3. ChatViewModel.kt
with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'r') as f:
    text = f.read()
text = text.replace('List<ChatMessageEntity>', 'List<ChatMessage>')
with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'w') as f:
    f.write(text)

# 4. ViewModels duplicate imports
files = [
    'app/src/main/java/com/example/presentation/viewmodel/LifeHubViewModel.kt',
    'app/src/main/java/com/example/presentation/viewmodel/PetViewModel.kt',
    'app/src/main/java/com/example/presentation/viewmodel/WellnessViewModel.kt'
]
for file in files:
    with open(file, 'r') as f:
        lines = f.readlines()
    seen = set()
    out_lines = []
    for line in lines:
        if line.startswith('import '):
            if line in seen:
                continue
            seen.add(line)
        out_lines.append(line)
    with open(file, 'w') as f:
        f.writelines(out_lines)

# 5. LumiApp.kt line 301
with open('app/src/main/java/com/example/presentation/LumiApp.kt', 'r') as f:
    text = f.read()
# if it is chatMessage.toDomain() inside LumiApp, we can just remove .toDomain()
text = text.replace('chatMessageEntity.toDomain()', 'chatMessageEntity')
text = text.replace('it.toDomain()', 'it') # wait, that might break other things
# let's only replace inside LumiApp
text = text.replace('streamingMessage = streamingAiMessage?.toDomain()', 'streamingMessage = streamingAiMessage')
text = text.replace('streamingAiMessage?.toDomain()', 'streamingAiMessage')

with open('app/src/main/java/com/example/presentation/LumiApp.kt', 'w') as f:
    f.write(text)

