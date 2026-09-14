import os
import re

# 1. ChatRepositoryImpl.kt
with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'r') as f:
    text = f.read()

# Fix sendMessage signature
text = re.sub(
    r'suspend fun sendMessage\((.*?)\): ChatMessageEntity',
    r'suspend fun sendMessage(\1): ChatMessage',
    text
)
with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'w') as f:
    f.write(text)

# 2. ChatViewModel.kt
with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'r') as f:
    text = f.read()

text = text.replace('import com.example.data.local.entity.ChatMessageEntity', 'import com.example.domain.model.ChatMessage')
text = text.replace('ChatMessageEntity', 'ChatMessage')
with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'w') as f:
    f.write(text)

# 3. LumiApp.kt
with open('app/src/main/java/com/example/presentation/LumiApp.kt', 'r') as f:
    text = f.read()

text = text.replace('import com.example.data.local.entity.ChatMessageEntity', 'import com.example.domain.model.ChatMessage')
text = text.replace('ChatMessageEntity', 'ChatMessage')
with open('app/src/main/java/com/example/presentation/LumiApp.kt', 'w') as f:
    f.write(text)

