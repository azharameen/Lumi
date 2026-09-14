import re

file_path = 'app/src/main/java/com/example/framework/PetOverlayService.kt'
with open(file_path, 'r') as f:
    content = f.read()

content = content.replace('private lateinit var repository: PetRepository', 'private lateinit var petRepository: PetRepository\n    private lateinit var chatRepository: ChatRepository')
content = content.replace('repository = koin.get<PetRepository>()', 'petRepository = koin.get<PetRepository>()\n        chatRepository = koin.get<ChatRepository>()')
content = content.replace('petRepository = petRepository,', 'petRepository = petRepository,')

with open(file_path, 'w') as f:
    f.write(content)

