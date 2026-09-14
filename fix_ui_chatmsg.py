import os

files = [
    'app/src/main/java/com/example/presentation/screens/chat/ChatMessageBubble.kt',
    'app/src/main/java/com/example/presentation/screens/ChatScreen.kt',
    'app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt',
    'app/src/main/java/com/example/presentation/overlay/PetOverlayRoot.kt',
    'app/src/main/java/com/example/presentation/LumiApp.kt'
]

for file in files:
    if os.path.exists(file):
        with open(file, 'r') as f:
            content = f.read()
        
        content = content.replace('import com.example.data.local.entity.ChatMessageEntity', 'import com.example.domain.model.ChatMessage')
        content = content.replace('ChatMessageEntity', 'ChatMessage')
        
        with open(file, 'w') as f:
            f.write(content)

