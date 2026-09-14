import re

file_path = 'app/src/main/java/com/example/presentation/overlay/PetOverlayRoot.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.domain.repository.LumiRepository\n',
    'import com.example.domain.repository.PetRepository\nimport com.example.domain.repository.ChatRepository\n'
)

content = content.replace(
    'repository: LumiRepository,',
    'petRepository: PetRepository,\n    chatRepository: ChatRepository,'
)

content = content.replace('repository.petStatus', 'petRepository.petStatus')
content = content.replace('repository.chatMessages', 'chatRepository.chatMessages')
content = content.replace('repository.setListening', 'petRepository.setListening')
content = content.replace('repository.setPetEmotion', 'petRepository.setPetEmotion')
content = content.replace('repository.sendMessage', 'chatRepository.sendMessage')
content = content.replace('repository.petTheCharacter()', 'petRepository.petTheAnimal()')
content = content.replace('repository.feedPet', 'petRepository.feedPet')


with open(file_path, 'w') as f:
    f.write(content)

