import re

# 1. AppModule.kt missing parameters
with open('app/src/main/java/com/example/core/di/AppModule.kt', 'r') as f:
    text = f.read()

text = text.replace(
    'single { PetInteractionUseCase(get()) }',
    'single { PetInteractionUseCase(get(), get(), get()) }'
)
with open('app/src/main/java/com/example/core/di/AppModule.kt', 'w') as f:
    f.write(text)

# 2. ChatRepositoryImpl.kt line 137
with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'r') as f:
    text = f.read()

# Just in case, replace any remaining return@withContext responseMsg
text = re.sub(
    r'return@withContext responseMsg\n',
    r'return@withContext responseMsg.toDomain()\n',
    text
)
text = re.sub(
    r'return@withContext responseMsg\}$',
    r'return@withContext responseMsg.toDomain()}',
    text
)
# undo double
text = text.replace('.toDomain().toDomain()', '.toDomain()')

with open('app/src/main/java/com/example/data/repository/ChatRepositoryImpl.kt', 'w') as f:
    f.write(text)

# 3. ChatViewModel.kt list type
with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'r') as f:
    text = f.read()
text = text.replace('List<ChatMessageEntity>', 'List<ChatMessage>')
with open('app/src/main/java/com/example/presentation/viewmodel/ChatViewModel.kt', 'w') as f:
    f.write(text)

