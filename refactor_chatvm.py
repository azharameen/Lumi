import re

with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('import com.example.data.local.entity.ChatMessageEntity\n', '')
content = content.replace('val chatMessages: StateFlow<List<ChatMessageEntity>>', 'val chatMessages: StateFlow<List<com.example.domain.model.ChatMessage>>')
content = content.replace('emptyList<ChatMessageEntity>()', 'emptyList<com.example.domain.model.ChatMessage>()')

with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'w') as f:
    f.write(content)
