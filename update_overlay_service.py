import re

file_path = 'app/src/main/java/com/example/framework/PetOverlayService.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.domain.repository.LumiRepository\n',
    'import com.example.domain.repository.PetRepository\nimport com.example.domain.repository.ChatRepository\n'
)

content = content.replace('private lateinit var repository: LumiRepository', 'private lateinit var petRepository: PetRepository\n    private lateinit var chatRepository: ChatRepository')
content = content.replace('repository = koin.get<LumiRepository>()', 'petRepository = koin.get<PetRepository>()\n        chatRepository = koin.get<ChatRepository>()')
content = content.replace('repository.setOverlayActive', 'petRepository.setOverlayActive')
content = content.replace('if (::repository.isInitialized) {', 'if (::petRepository.isInitialized) {')
content = content.replace('repository = repository,', 'petRepository = petRepository,\n                        chatRepository = chatRepository,')

with open(file_path, 'w') as f:
    f.write(content)

