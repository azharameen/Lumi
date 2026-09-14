import re

file_path = 'app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('import com.example.domain.repository.LumiRepository\n', 'import com.example.domain.repository.ChatRepository\nimport com.example.domain.repository.PetRepository\n')
content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = content.replace('val repository: LumiRepository,', 'val chatRepository: ChatRepository,\n    val petRepository: PetRepository,')

content = content.replace('repository.pagedChatMessages', 'chatRepository.pagedChatMessages')
content = content.replace('repository.chatMessages', 'chatRepository.chatMessages')
content = content.replace('repository.streamingAiMessage', 'chatRepository.streamingAiMessage')
content = content.replace('repository.pendingHitlActions', 'chatRepository.pendingHitlActions')

content = content.replace('repository.setSpeaking', 'petRepository.setSpeaking')
content = content.replace('repository.clearChatHistory', 'chatRepository.clearChatHistory')
content = content.replace('repository.deleteMessage', 'chatRepository.deleteMessage')
content = content.replace('repository.resolveHitlAction', 'chatRepository.resolveHitlAction')
content = content.replace('repository.sendMessage', 'chatRepository.sendMessage')
content = content.replace('repository.setListening', 'petRepository.setListening')

with open(file_path, 'w') as f:
    f.write(content)

