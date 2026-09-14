import re

# PetOverlayRoot
file_path = 'app/src/main/java/com/example/presentation/overlay/PetOverlayRoot.kt'
with open(file_path, 'r') as f:
    content = f.read()
content = content.replace('import com.example.domain.repository.LumiRepository', 'import com.example.domain.repository.PetRepository')
content = content.replace('repository: LumiRepository', 'repository: PetRepository')
content = content.replace('repository.petTheCharacter()', 'repository.petTheAnimal()')
with open(file_path, 'w') as f:
    f.write(content)

# ChatViewModel
file_path = 'app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()
content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = content.replace('import com.example.domain.repository.LumiRepository', 'import com.example.domain.repository.ChatRepository\nimport com.example.domain.repository.PetRepository')
content = content.replace('val repository: LumiRepository', 'val chatRepository: ChatRepository, val petRepository: PetRepository')
content = content.replace('repository.pagedChatMessages', 'chatRepository.pagedChatMessages')
content = content.replace('repository.chatMessages', 'chatRepository.chatMessages')
content = content.replace('repository.streamingAiMessage', 'chatRepository.streamingAiMessage')
content = content.replace('repository.setSpeaking', 'petRepository.setSpeaking')
content = content.replace('repository.setListening', 'petRepository.setListening')
content = content.replace('repository.clearChatHistory', 'chatRepository.clearChatHistory')
content = content.replace('repository.deleteMessage', 'chatRepository.deleteMessage')
content = content.replace('repository.sendMessage', 'chatRepository.sendMessage')
content = content.replace('repository.pendingHitlActions', 'chatRepository.pendingHitlActions')
content = content.replace('repository.resolveHitlAction', 'chatRepository.resolveHitlAction')
with open(file_path, 'w') as f:
    f.write(content)

# PetViewModel
file_path = 'app/src/main/java/com/example/presentation/viewmodel/PetViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()
content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = content.replace('import com.example.domain.repository.LumiRepository', 'import com.example.domain.repository.PetRepository')
content = content.replace('val repository: LumiRepository', 'val repository: PetRepository')
content = content.replace('repository.petTheCharacter()', 'repository.petTheAnimal()')
with open(file_path, 'w') as f:
    f.write(content)


# WellnessViewModel
file_path = 'app/src/main/java/com/example/presentation/viewmodel/WellnessViewModel.kt'
with open(file_path, 'r') as f:
    content = f.read()
content = content.replace('import com.example.data.repository.LumiRepositoryImpl\n', '')
content = content.replace('import com.example.domain.repository.LumiRepository', '')
content = content.replace('val repository: LumiRepository,', '')
with open(file_path, 'w') as f:
    f.write(content)

