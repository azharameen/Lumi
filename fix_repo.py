import re

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'r') as f:
    content = f.read()

# Fix timestamp
content = content.replace('_streamingAiMessage.value = ChatMessage(', '_streamingAiMessage.value = ChatMessage(timestamp = System.currentTimeMillis(), ')

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'w') as f:
    f.write(content)


with open('app/src/main/java/com/example/domain/usecase/chat/SendMessageUseCase.kt', 'r') as f:
    content = f.read()

content = content.replace('ChatMessageEntity', 'ChatMessage')
content = content.replace('import com.example.data.local.entity.ChatMessage', 'import com.example.domain.model.ChatMessage')

with open('app/src/main/java/com/example/domain/usecase/chat/SendMessageUseCase.kt', 'w') as f:
    f.write(content)
