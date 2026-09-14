with open('app/src/main/java/com/example/presentation/LumiApp.kt', 'r') as f:
    content = f.read()

content = content.replace('ChatMessageEntity', 'ChatMessage')
content = content.replace('import com.example.data.local.entity.ChatMessage', 'import com.example.domain.model.ChatMessage')

with open('app/src/main/java/com/example/presentation/LumiApp.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace('ChatMessageEntity', 'ChatMessage')
content = content.replace('import com.example.data.local.entity.ChatMessage', 'import com.example.domain.model.ChatMessage')

with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'w') as f:
    f.write(content)
